package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.domain.Stock;
import id.my.mufidjam.stockdashboard.domain.StockPriceTick;
import id.my.mufidjam.stockdashboard.domain.StockSearchDocument;
import id.my.mufidjam.stockdashboard.dto.PriceTickDto;
import id.my.mufidjam.stockdashboard.repository.StockPriceRepository;
import id.my.mufidjam.stockdashboard.repository.StockRepository;
import id.my.mufidjam.stockdashboard.repository.StockSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;

import java.time.Duration;

/**
 * Consumes {@link MarketDataGeneratorService.TickEvent} messages from Kafka and
 * fans each tick out to every downstream store in one reactive pipeline:
 * Postgres (history + latest snapshot), Redis (write-through quote cache),
 * Elasticsearch (search index refresh), and the in-memory broadcaster
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceConsumerService {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockSearchRepository stockSearchRepository;
    private final CacheService cacheService;
    private final PriceStreamBroadcaster broadcaster;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static final Duration QUOTE_TTL = Duration.ofSeconds(15);

    @PostConstruct
    public void start() {
        kafkaReceiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .concatMap(record ->
                        processTick(record.value())
                                .doOnSuccess(v -> record.receiverOffset().acknowledge())
                                .onErrorResume(err -> {
                                    log.error("Failed processing tick record, skipping", err);
                                    record.receiverOffset().acknowledge();
                                    return Mono.empty();
                                })
                )
                .subscribe();
    }

    private Mono<Void> processTick(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, MarketDataGeneratorService.TickEvent.class))
                .flatMap(this::handleTick)
                .then();
    }

    private Mono<Void> handleTick(MarketDataGeneratorService.TickEvent event) {
        return stockRepository.findBySymbol(event.symbol())
                .flatMap(stock -> persistHistory(stock, event)
                        .then(updateLatestPrice(event))
                        .then(writeThroughCache(event))
                        .then(refreshSearchIndex(stock, event))
                        .then(broadcast(event)))
                .doOnError(e -> log.warn("Tick handling failed for {}: {}", event.symbol(), e.getMessage()))
                .then();
    }

    private Mono<StockPriceTick> persistHistory(Stock stock, MarketDataGeneratorService.TickEvent event) {
        StockPriceTick tick = StockPriceTick.builder()
                .stockId(stock.getId())
                .symbol(event.symbol())
                .price(event.price())
                .volume(event.volume())
                .changePercent(event.changePercent())
                .ts(event.ts())
                .build();
        return stockPriceRepository.save(tick);
    }

    private Mono<Void> updateLatestPrice(MarketDataGeneratorService.TickEvent event) {
        return stockRepository.applyPriceTick(event.symbol(), event.price(), event.volume());
    }

    private Mono<Boolean> writeThroughCache(MarketDataGeneratorService.TickEvent event) {
        PriceTickDto dto = toDto(event);
        return cacheService.put("quote:" + event.symbol(), dto, QUOTE_TTL);
    }

    private Mono<StockSearchDocument> refreshSearchIndex(Stock stock, MarketDataGeneratorService.TickEvent event) {
        StockSearchDocument doc = StockSearchDocument.builder()
                .id(stock.getSymbol())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .exchange(stock.getExchange())
                .lastPrice(event.price())
                .changePercent(event.changePercent())
                .build();
        return stockSearchRepository.save(doc);
    }

    private Mono<Void> broadcast(MarketDataGeneratorService.TickEvent event) {
        return Mono.fromRunnable(() -> broadcaster.publish(toDto(event)));
    }

    private PriceTickDto toDto(MarketDataGeneratorService.TickEvent event) {
        return PriceTickDto.builder()
                .symbol(event.symbol())
                .price(event.price())
                .volume(event.volume())
                .changePercent(event.changePercent())
                .ts(event.ts())
                .build();
    }
}
