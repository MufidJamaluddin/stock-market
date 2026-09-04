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
@Table("stocks")
public class Stock {

    @Id
    private Long id;

    @Column("symbol")
    private String symbol;

    @Column("company_name")
    private String companyName;

    @Column("sector")
    private String sector;

    @Column("exchange")
    private String exchange;

    @Column("currency")
    private String currency;

    @Column("base_price")
    private BigDecimal basePrice;

    @Column("last_price")
    private BigDecimal lastPrice;

    @Column("day_open")
    private BigDecimal dayOpen;

    @Column("day_high")
    private BigDecimal dayHigh;

    @Column("day_low")
    private BigDecimal dayLow;

    @Column("day_volume")
    private Long dayVolume;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
