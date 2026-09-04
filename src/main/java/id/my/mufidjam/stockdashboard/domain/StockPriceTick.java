package id.my.mufidjam.stockdashboard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("stock_price_history")
public class StockPriceTick {

    @Id
    private Long id;

    @Column("stock_id")
    private Long stockId;

    @Column("symbol")
    private String symbol;

    @Column("price")
    private BigDecimal price;

    @Column("volume")
    private Long volume;

    @Column("change_percent")
    private BigDecimal changePercent;

    @Column("ts")
    private OffsetDateTime ts;
}
