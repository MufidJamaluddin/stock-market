package id.my.mufidjam.stockdashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Thin wrapper around Redis implementing:
 * <ul>
 *   <li><b>Cache-aside</b> ({@link #getOrLoad}) - read through cache, fall back to
 *       the supplier (DB/ES) on miss, then populate the cache with a TTL.</li>
 *   <li><b>Write-through</b> ({@link #put}) - used by the Kafka consumer to push
 *       the latest quote into the data grid the instant it changes.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public <T> Mono<T> getOrLoad(String key, Class<T> type, Duration ttl, Mono<T> loader) {
        return redisTemplate.opsForValue().get(key)
                .cast(type)
                .switchIfEmpty(
                        loader.flatMap(value -> put(key, value, ttl).thenReturn(value))
                );
    }

    public <T> Mono<Boolean> put(String key, T value, Duration ttl) {
        return redisTemplate.opsForValue().set(key, value, ttl);
    }

    public Mono<Boolean> evict(String key) {
        return redisTemplate.opsForValue().delete(key);
    }

    public Mono<Object> getRaw(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
