package id.my.mufidjam.stockdashboard.repository;

import id.my.mufidjam.stockdashboard.domain.StockSearchDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface StockSearchRepository extends ReactiveElasticsearchRepository<StockSearchDocument, String> {
    Flux<StockSearchDocument> findBySectorIgnoreCase(String sector);
}
