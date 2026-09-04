package id.my.mufidjam.stockdashboard.repository;

import id.my.mufidjam.stockdashboard.dto.OhlcBarDto;
import id.my.mufidjam.stockdashboard.dto.StockDto;
import reactor.core.publisher.Flux;

public interface StockNativeRepository {
    Flux<StockDto> findTopGainers(int limit);
    Flux<StockDto> findTopLosers(int limit);
    Flux<StockDto> findAllWithChangePercent();
    Flux<OhlcBarDto> findOhlcBars(String symbol, String bucketInterval, int limit);
    Flux<StockDto> findSectorLeaders(String sector, int limit);
}
