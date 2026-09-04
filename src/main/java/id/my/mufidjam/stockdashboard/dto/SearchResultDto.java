package id.my.mufidjam.stockdashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String symbol;
    private String companyName;
    private String sector;
    private String exchange;
    private BigDecimal lastPrice;
    private BigDecimal changePercent;
}
