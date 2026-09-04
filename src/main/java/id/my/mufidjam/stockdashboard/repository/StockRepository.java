package id.my.mufidjam.stockdashboard.repository;

import id.my.mufidjam.stockdashboard.domain.Stock;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockRepository extends R2dbcRepository<Stock, Long> {

    Mono<Stock> findBySymbol(String symbol);

    Flux<Stock> findBySectorIgnoreCase(String sector);

    @Query("SELECT * FROM stocks ORDER BY symbol ASC")
    Flux<Stock> findAllOrderBySymbol();

    @Query("""
           UPDATE stocks
              SET last_price = :price,
                  day_high   = GREATEST(day_high, :price),
                  day_low    = LEAST(day_low, :price),
                  day_volume = day_volume + :volume,
                  updated_at = now()
            WHERE symbol = :symbol
           """)
    @Modifying
    Mono<Void> applyPriceTick(String symbol, java.math.BigDecimal price, Long volume);
}
