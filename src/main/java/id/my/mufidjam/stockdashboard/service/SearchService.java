package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.domain.StockSearchDocument;
import id.my.mufidjam.stockdashboard.dto.SearchResultDto;
import id.my.mufidjam.stockdashboard.repository.StockSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * Search-detail page backing service. Queries Elasticsearch for typo-tolerant,
 * multi-field symbol/company/sector search
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final StockSearchRepository stockSearchRepository;
    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;
    private final CacheService cacheService;

    @Value("${app.cache.search-ttl-seconds:60}")
    private long searchTtlSeconds;

    public Flux<SearchResultDto> search(String rawQuery) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.isEmpty()) {
            return Flux.empty();
        }

        String cacheKey = "search:" + q.toLowerCase();

        return cacheService.getRaw(cacheKey)
                .cast(List.class)
                .flatMapMany(Flux::fromIterable)
                .cast(SearchResultDto.class)
                .switchIfEmpty(runSearch(q).collectList()
                        .flatMapMany(results -> cacheService
                                .put(cacheKey, results, Duration.ofSeconds(searchTtlSeconds))
                                .thenMany(Flux.fromIterable(results))));
    }

    private Flux<SearchResultDto> runSearch(String q) {
        String esQuery = """
                {
                  "bool": {
                    "should": [
                      { "match": { "symbol": { "query": "%s", "boost": 3 } } },
                      { "match": { "companyName": { "query": "%s", "fuzziness": "AUTO", "boost": 2 } } },
                      { "match": { "sector": { "query": "%s" } } },
                      { "wildcard": { "symbol": { "value": "*%s*", "case_insensitive": true } } }
                    ],
                    "minimum_should_match": 1
                  }
                }
                """.formatted(q, q, q, q.toLowerCase());

        Query query = new StringQuery(esQuery);

        return reactiveElasticsearchTemplate
                .search(query, StockSearchDocument.class)
                .map(hit -> toDto(hit.getContent()));
    }

    private SearchResultDto toDto(StockSearchDocument doc) {
        return SearchResultDto.builder()
                .symbol(doc.getSymbol())
                .companyName(doc.getCompanyName())
                .sector(doc.getSector())
                .exchange(doc.getExchange())
                .lastPrice(doc.getLastPrice())
                .changePercent(doc.getChangePercent())
                .build();
    }
}
