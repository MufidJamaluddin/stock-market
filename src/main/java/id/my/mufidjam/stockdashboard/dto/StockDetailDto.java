package id.my.mufidjam.stockdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailDto {
    private String symbol;
    private String companyName;
    private String sector;
    private String exchange;
    private String currency;
    private BigDecimal lastPrice;
    private BigDecimal dayOpen;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private Long dayVolume;
    private BigDecimal changePercent;
    private List<PriceTickDto> recentHistory;
    private boolean fromCache;
}
