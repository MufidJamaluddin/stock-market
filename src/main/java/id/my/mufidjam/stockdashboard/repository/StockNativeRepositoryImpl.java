package id.my.mufidjam.stockdashboard.repository;

import id.my.mufidjam.stockdashboard.dto.OhlcBarDto;
import id.my.mufidjam.stockdashboard.dto.StockDto;
import io.r2dbc.spi.Row;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class StockNativeRepositoryImpl implements StockNativeRepository {

    private static final Set<String> ALLOWED_BUCKETS =
            Set.of("second", "minute", "5 minutes", "hour", "day");

    private final DatabaseClient databaseClient;

    private StockDto mapStockRow(Row row) {
        return StockDto.builder()
                .symbol(row.get("symbol", String.class))
                .companyName(row.get("company_name", String.class))
                .sector(row.get("sector", String.class))
                .exchange(row.get("exchange", String.class))
                .lastPrice(row.get("last_price", BigDecimal.class))
                .dayOpen(row.get("day_open", BigDecimal.class))
                .dayHigh(row.get("day_high", BigDecimal.class))
                .dayLow(row.get("day_low", BigDecimal.class))
                .dayVolume(row.get("day_volume", Long.class))
                .changePercent(row.get("change_percent", BigDecimal.class))
                .build();
    }

    private OhlcBarDto mapOhlcRow(Row row, String symbol) {
        return OhlcBarDto.builder()
                .symbol(symbol)
                .bucket(row.get("bucket", OffsetDateTime.class))
                .open(row.get("open_price", BigDecimal.class))
                .high(row.get("high_price", BigDecimal.class))
                .low(row.get("low_price", BigDecimal.class))
                .close(row.get("close_price", BigDecimal.class))
                .volume(row.get("total_volume", Long.class))
                .build();
    }

    private static final String CHANGE_PCT_EXPR = """
            CASE
                WHEN day_open = 0 THEN NULL
                ELSE ROUND(
                    ((last_price - day_open) / day_open) * 100,
                    4
                )
            END
            """;

    @Override
    public Flux<StockDto> findTopGainers(int limit) {

        String sql = """
                SELECT
                    symbol,
                    company_name,
                    sector,
                    exchange,
                    last_price,
                    day_open,
                    day_high,
                    day_low,
                    day_volume,
                    %s AS change_percent
                FROM stocks
                ORDER BY change_percent DESC NULLS LAST
                LIMIT :limit
                """.formatted(CHANGE_PCT_EXPR);

        return databaseClient.sql(sql)
                .bind("limit", limit)
                .map((row, metadata) -> mapStockRow(row))
                .all();
    }

    @Override
    public Flux<StockDto> findTopLosers(int limit) {

        String sql = """
                SELECT
                    symbol,
                    company_name,
                    sector,
                    exchange,
                    last_price,
                    day_open,
                    day_high,
                    day_low,
                    day_volume,
                    %s AS change_percent
                FROM stocks
                ORDER BY change_percent ASC NULLS LAST
                LIMIT :limit
                """.formatted(CHANGE_PCT_EXPR);

        return databaseClient.sql(sql)
                .bind("limit", limit)
                .map((row, metadata) -> mapStockRow(row))
                .all();
    }

    @Override
    public Flux<StockDto> findAllWithChangePercent() {

        String sql = """
                SELECT
                    symbol,
                    company_name,
                    sector,
                    exchange,
                    last_price,
                    day_open,
                    day_high,
                    day_low,
                    day_volume,
                    %s AS change_percent
                FROM stocks
                ORDER BY symbol ASC
                """.formatted(CHANGE_PCT_EXPR);

        return databaseClient.sql(sql)
                .map((row, metadata) -> mapStockRow(row))
                .all();
    }

    private String getBucketExpression(String bucketInterval) {

        if (!ALLOWED_BUCKETS.contains(bucketInterval)) {
            bucketInterval = "minute";
        }

        return switch (bucketInterval) {

            case "second" ->
                    "date_trunc('second', ts)";

            case "minute" ->
                    "date_trunc('minute', ts)";

            case "5 minutes" ->
                    """
                    date_trunc('hour', ts)
                    + (
                        floor(extract(minute from ts) / 5)
                        * interval '5 minutes'
                    )
                    """;

            case "hour" ->
                    "date_trunc('hour', ts)";

            case "day" ->
                    "date_trunc('day', ts)";

            default ->
                    "date_trunc('minute', ts)";
        };
    }

    @Override
    public Flux<OhlcBarDto> findOhlcBars(
            String symbol,
            String bucketInterval,
            int limit
    ) {

        String bucketExpression = getBucketExpression(bucketInterval);

        String sql = """
                WITH bucketed AS (
                    SELECT
                        %s AS bucket,
                        price,
                        volume,
                        ts
                    FROM stock_price_history
                    WHERE symbol = :symbol
                ),
                aggregated AS (
                    SELECT
                        bucket,
                        MAX(price) AS high_price,
                        MIN(price) AS low_price,
                        SUM(volume) AS total_volume
                    FROM bucketed
                    GROUP BY bucket
                ),
                first_prices AS (
                    SELECT DISTINCT ON (bucket)
                        bucket,
                        price AS open_price
                    FROM bucketed
                    ORDER BY bucket, ts ASC
                ),
                last_prices AS (
                    SELECT DISTINCT ON (bucket)
                        bucket,
                        price AS close_price
                    FROM bucketed
                    ORDER BY bucket, ts DESC
                )
                SELECT
                    a.bucket,
                    f.open_price,
                    a.high_price,
                    a.low_price,
                    l.close_price,
                    a.total_volume
                FROM aggregated a
                JOIN first_prices f ON f.bucket = a.bucket
                JOIN last_prices l ON l.bucket = a.bucket
                ORDER BY a.bucket DESC
                LIMIT :limit
                """.formatted(bucketExpression);

        return databaseClient.sql(sql)
                .bind("symbol", symbol)
                .bind("limit", limit)
                .map((row, metadata) -> mapOhlcRow(row, symbol))
                .all();
    }

    @Override
    public Flux<StockDto> findSectorLeaders(
            String sector,
            int limit
    ) {

        String sql = """
                SELECT
                    symbol,
                    company_name,
                    sector,
                    exchange,
                    last_price,
                    day_open,
                    day_high,
                    day_low,
                    day_volume,
                    %s AS change_percent
                FROM stocks
                WHERE sector ILIKE :sector
                ORDER BY change_percent DESC NULLS LAST
                LIMIT :limit
                """.formatted(CHANGE_PCT_EXPR);

        return databaseClient.sql(sql)
                .bind("sector", sector)
                .bind("limit", limit)
                .map((row, metadata) -> mapStockRow(row))
                .all();
    }
}
