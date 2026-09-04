package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.domain.Stock;
import id.my.mufidjam.stockdashboard.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Backend data generator. On a fixed schedule, produces a randomized-walk price
 * tick for every stock in the universe (Java Streams pipeline) and publishes the
 * batch onto Kafka - the entry point of the "Kafka & Stream Based Application"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataGeneratorService {

    private final StockRepository stockRepository;
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${app.kafka.topic}")
    private String topic;

    @Value("${app.market.generator.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${app.market.generator.interval-ms:2000}")
    public void generateTicks() {
        if (!enabled) {
            return;
        }

        stockRepository.findAll()
                .collectList()
                .flatMapMany(this::buildSenderRecords)
                .as(kafkaSender::send)
                .doOnError(err -> log.error("Failed to publish market tick batch", err))
                .subscribe(result -> log.debug("Published tick to partition {}",
                        result.recordMetadata().partition()));
    }

    private Flux<SenderRecord<String, String, String>> buildSenderRecords(List<Stock> stocks) {
        List<SenderRecord<String, String, String>> records = stocks.stream()
                .map(this::randomWalk)
                .map(this::toSenderRecord)
                .collect(Collectors.toList());
        return Flux.fromIterable(records);
    }

    private TickEvent randomWalk(Stock stock) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // +/- up to 0.8% move per tick
        double pctMove = (rnd.nextDouble() - 0.5) * 0.016;
        BigDecimal current = stock.getLastPrice() != null ? stock.getLastPrice() : stock.getBasePrice();
        BigDecimal delta = current.multiply(BigDecimal.valueOf(pctMove));
        BigDecimal newPrice = current.add(delta).setScale(4, RoundingMode.HALF_UP);
        if (newPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            newPrice = BigDecimal.valueOf(0.01);
        }

        long volume = rnd.nextLong(100, 10_000);

        BigDecimal open = stock.getDayOpen() != null ? stock.getDayOpen() : stock.getBasePrice();
        BigDecimal changePercent = open.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : newPrice.subtract(open)
                        .divide(open, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(4, RoundingMode.HALF_UP);

        return new TickEvent(stock.getSymbol(), newPrice, volume, changePercent, OffsetDateTime.now());
    }

    private SenderRecord<String, String, String> toSenderRecord(TickEvent event) {
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize tick event", e);
        }
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.symbol(), json);
        return SenderRecord.create(record, event.symbol());
    }

    public record TickEvent(
            String symbol,
            BigDecimal price,
            Long volume,
            BigDecimal changePercent,
            OffsetDateTime ts) {
    }
}
