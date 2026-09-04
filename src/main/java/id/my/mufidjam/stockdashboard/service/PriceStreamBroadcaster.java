package id.my.mufidjam.stockdashboard.service;

import id.my.mufidjam.stockdashboard.dto.PriceTickDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Multicast hot stream of price ticks, fed by the Kafka consumer
 */
@Slf4j
@Component
public class PriceStreamBroadcaster {

    private final Sinks.Many<PriceTickDto> sink =
            Sinks.many().multicast().onBackpressureBuffer(1024, false);

    public void publish(PriceTickDto tick) {
        Sinks.EmitResult result = sink.tryEmitNext(tick);
        if (result.isFailure()) {
            log.debug("Dropped tick for {} - no active subscribers or buffer full ({})",
                    tick.getSymbol(), result);
        }
    }

    public Flux<PriceTickDto> streamAll() {
        return sink.asFlux();
    }

    public Flux<PriceTickDto> streamSymbol(String symbol) {
        return sink.asFlux().filter(t -> t.getSymbol().equalsIgnoreCase(symbol));
    }
}
