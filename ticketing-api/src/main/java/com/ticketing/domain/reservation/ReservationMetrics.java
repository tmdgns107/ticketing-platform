package com.ticketing.domain.reservation;

import com.ticketing.domain.reservation.lock.LockStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Micrometer instrumentation for the seat-hold path, so the lock strategies can be compared
 * on the Grafana dashboard: {@code ticketing_reservation_hold_seconds} (timer, tagged by
 * strategy + outcome) and its derived count.
 */
@Component
public class ReservationMetrics {

    private final MeterRegistry registry;

    public ReservationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    void recordHold(LockStrategy strategy, String outcome, long nanos) {
        Timer.builder("ticketing.reservation.hold")
                .description("seat hold attempts")
                .tag("strategy", strategy.name().toLowerCase())
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(registry)
                .record(nanos, TimeUnit.NANOSECONDS);
    }
}
