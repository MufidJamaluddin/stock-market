package id.my.mufidjam.stockdashboard.repository;

import id.my.mufidjam.stockdashboard.domain.StockPriceTick;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface StockPriceRepository extends R2dbcRepository<StockPriceTick, Long> {

    @Query("""
           SELECT * FROM stock_price_history
            WHERE symbol = :symbol
            ORDER BY ts DESC
            LIMIT :limit
           """)
    Flux<StockPriceTick> findRecentBySymbol(String symbol, int limit);
}
