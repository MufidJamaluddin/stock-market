package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.domain.Stock;
import id.my.mufidjam.stockdashboard.domain.StockSearchDocument;
import id.my.mufidjam.stockdashboard.repository.StockRepository;
import id.my.mufidjam.stockdashboard.repository.StockSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexInitializer implements ApplicationRunner {

    private final StockRepository stockRepository;
    private final StockSearchRepository stockSearchRepository;

    @Override
    public void run(ApplicationArguments args) {
        stockRepository.findAll()
                .collectList()
                .map(this::toDocuments)
                .flatMapMany(stockSearchRepository::saveAll)
                .doOnComplete(() -> log.info("Elasticsearch stock index seeded/refreshed"))
                .doOnError(e -> log.warn("Could not seed Elasticsearch index (will retry via Kafka ticks): {}",
                        e.getMessage()))
                .onErrorResume(e -> reactor.core.publisher.Flux.empty())
                .subscribe();
    }

    private List<StockSearchDocument> toDocuments(List<Stock> stocks) {
        return stocks.stream()
                .map(s -> StockSearchDocument.builder()
                        .id(s.getSymbol())
                        .symbol(s.getSymbol())
                        .companyName(s.getCompanyName())
                        .sector(s.getSector())
                        .exchange(s.getExchange())
                        .lastPrice(s.getLastPrice())
                        .changePercent(java.math.BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }
}
