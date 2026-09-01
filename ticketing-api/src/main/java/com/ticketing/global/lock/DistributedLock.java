package com.ticketing.global.lock;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * A minimal single-node Redis lock: {@code SET key token NX PX ttl} to acquire, and a
 * compare-and-delete Lua script to release only our own token. Good enough to demonstrate
 * the distributed-lock strategy; a production setup would use Redisson (fencing tokens,
 * watchdog renewal, red-lock).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private static final RedisScript<Long> RELEASE_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class);

    private final StringRedisTemplate redis;

    /**
     * Runs {@code work} while holding {@code key}. Does not wait: if the lock is already held,
     * {@code onContended} is returned instead.
     */
    public <T> T executeOrElse(String key, Duration ttl, Supplier<T> work, Supplier<T> onContended) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, token, ttl);
        if (!Boolean.TRUE.equals(acquired)) {
            return onContended.get();
        }
        try {
            return work.get();
        } finally {
            try {
                redis.execute(RELEASE_SCRIPT, List.of(key), token);
            } catch (RuntimeException e) {
                log.warn("failed to release lock {}: {}", key, e.getMessage());
            }
        }
    }
}
