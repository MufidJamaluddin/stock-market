package id.my.mufidjam.stockdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTickDto {
    private String symbol;
    private BigDecimal price;
    private Long volume;
    private BigDecimal changePercent;
    private OffsetDateTime ts;
}
