package id.my.mufidjam.stockdashboard.controller;

import id.my.mufidjam.stockdashboard.dto.OhlcBarDto;
import id.my.mufidjam.stockdashboard.dto.StockDetailDto;
import id.my.mufidjam.stockdashboard.dto.StockDto;
import id.my.mufidjam.stockdashboard.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public Flux<StockDto> dashboard() {
        return stockService.getDashboard();
    }

    @GetMapping("/gainers")
    public Flux<StockDto> gainers(@RequestParam(defaultValue = "5") int limit) {
        return stockService.getTopGainers(limit);
    }

    @GetMapping("/losers")
    public Flux<StockDto> losers(@RequestParam(defaultValue = "5") int limit) {
        return stockService.getTopLosers(limit);
    }

    @GetMapping("/sector/{sector}")
    public Flux<StockDto> sectorLeaders(@PathVariable String sector,
                                         @RequestParam(defaultValue = "10") int limit) {
        return stockService.getSectorLeaders(sector, limit);
    }

    @GetMapping("/{symbol}")
    public Mono<StockDetailDto> detail(@PathVariable String symbol) {
        return stockService.getStockDetail(symbol);
    }

    @GetMapping("/{symbol}/ohlc")
    public Flux<OhlcBarDto> ohlc(@PathVariable String symbol,
                                 @RequestParam(defaultValue = "minute") String bucket,
                                 @RequestParam(defaultValue = "50") int limit) {
        return stockService.getOhlcBars(symbol, bucket, limit);
    }
}
