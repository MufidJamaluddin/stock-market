package id.my.mufidjam.stockdashboard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "stocks")
public class StockSearchDocument {

    @Id
    private String id; // == symbol

    @Field(type = FieldType.Keyword)
    private String symbol;

    @Field(type = FieldType.Text)
    private String companyName;

    @Field(type = FieldType.Keyword)
    private String sector;

    @Field(type = FieldType.Keyword)
    private String exchange;

    @Field(type = FieldType.Double)
    private BigDecimal lastPrice;

    @Field(type = FieldType.Double)
    private BigDecimal changePercent;
}
