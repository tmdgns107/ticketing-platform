package com.ticketing.domain.reservation;

import com.ticketing.domain.reservation.lock.LockStrategy;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reservation")
public record ReservationProperties(
        Duration holdDuration,
        LockStrategy defaultLockStrategy,
        int optimisticMaxAttempts,
        Duration distributedLockTtl) {

    public ReservationProperties {
        if (holdDuration == null) {
            holdDuration = Duration.ofMinutes(5);
        }
        if (defaultLockStrategy == null) {
            defaultLockStrategy = LockStrategy.OPTIMISTIC;
        }
        if (optimisticMaxAttempts <= 0) {
            optimisticMaxAttempts = 3;
        }
        if (distributedLockTtl == null) {
            distributedLockTtl = Duration.ofSeconds(3);
        }
    }
}
