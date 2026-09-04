package id.my.mufidjam.stockdashboard.controller;

import id.my.mufidjam.stockdashboard.dto.SearchResultDto;
import id.my.mufidjam.stockdashboard.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Flux<SearchResultDto> search(@RequestParam("q") String query) {
        return searchService.search(query);
    }
}
