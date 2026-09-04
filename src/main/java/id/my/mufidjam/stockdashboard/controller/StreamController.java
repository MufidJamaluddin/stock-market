package id.my.mufidjam.stockdashboard.controller;

import id.my.mufidjam.stockdashboard.dto.PriceTickDto;
import id.my.mufidjam.stockdashboard.service.PriceStreamBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StreamController {

    private final PriceStreamBroadcaster broadcaster;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceTickDto> streamAll() {
        return broadcaster.streamAll();
    }

    @GetMapping(value = "/{symbol}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceTickDto> streamSymbol(@PathVariable String symbol) {
        return broadcaster.streamSymbol(symbol);
    }
}
