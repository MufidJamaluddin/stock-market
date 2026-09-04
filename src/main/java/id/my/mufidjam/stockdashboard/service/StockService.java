package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.domain.Stock;
import id.my.mufidjam.stockdashboard.dto.OhlcBarDto;
import id.my.mufidjam.stockdashboard.dto.PriceTickDto;
import id.my.mufidjam.stockdashboard.dto.StockDetailDto;
import id.my.mufidjam.stockdashboard.dto.StockDto;
import id.my.mufidjam.stockdashboard.exception.StockNotFoundException;
import id.my.mufidjam.stockdashboard.repository.StockNativeRepository;
import id.my.mufidjam.stockdashboard.repository.StockPriceRepository;
import id.my.mufidjam.stockdashboard.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockNativeRepository stockNativeRepository;
    private final CacheService cacheService;

    @Value("${app.cache.detail-ttl-seconds:30}")
    private long detailTtlSeconds;

    public Flux<StockDto> getDashboard() {
        return stockNativeRepository.findAllWithChangePercent();
    }

    public Flux<StockDto> getTopGainers(int limit) {
        return stockNativeRepository.findTopGainers(limit);
    }

    public Flux<StockDto> getTopLosers(int limit) {
        return stockNativeRepository.findTopLosers(limit);
    }

    public Flux<StockDto> getSectorLeaders(String sector, int limit) {
        return stockNativeRepository.findSectorLeaders(sector, limit);
    }

    public Flux<OhlcBarDto> getOhlcBars(String symbol, String bucket, int limit) {
        return stockNativeRepository.findOhlcBars(symbol.toUpperCase(), bucket, limit);
    }

    public Mono<StockDetailDto> getStockDetail(String symbol) {
        String cacheKey = "detail:" + symbol.toUpperCase();

        return cacheService.getRaw(cacheKey)
                .cast(StockDetailDto.class)
                .map(cached -> cached.toBuilder().fromCache(true).build())
                .switchIfEmpty(loadDetailFromSource(symbol).flatMap(detail ->
                        cacheService.put(cacheKey, detail, Duration.ofSeconds(detailTtlSeconds))
                                .thenReturn(detail)));
    }

    private Mono<StockDetailDto> loadDetailFromSource(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase())
                .switchIfEmpty(Mono.error(new StockNotFoundException(symbol)))
                .flatMap(stock -> stockPriceRepository.findRecentBySymbol(stock.getSymbol(), 50)
                        .collectList()
                        .map(ticks -> toDetailDto(stock, ticks)));
    }

    private StockDetailDto toDetailDto(Stock stock, List<id.my.mufidjam.stockdashboard.domain.StockPriceTick> ticks) {
        List<PriceTickDto> history = ticks.stream()
                .map(t -> PriceTickDto.builder()
                        .symbol(t.getSymbol())
                        .price(t.getPrice())
                        .volume(t.getVolume())
                        .changePercent(t.getChangePercent())
                        .ts(t.getTs())
                        .build())
                .collect(Collectors.toList());

        java.math.BigDecimal changePercent = stock.getDayOpen() != null
                && stock.getDayOpen().compareTo(java.math.BigDecimal.ZERO) != 0
                ? stock.getLastPrice().subtract(stock.getDayOpen())
                        .divide(stock.getDayOpen(), 6, java.math.RoundingMode.HALF_UP)
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .setScale(4, java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

        return StockDetailDto.builder()
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .exchange(stock.getExchange())
                .currency(stock.getCurrency())
                .lastPrice(stock.getLastPrice())
                .dayOpen(stock.getDayOpen())
                .dayHigh(stock.getDayHigh())
                .dayLow(stock.getDayLow())
                .dayVolume(stock.getDayVolume())
                .changePercent(changePercent)
                .recentHistory(history)
                .fromCache(false)
                .build();
    }
}
