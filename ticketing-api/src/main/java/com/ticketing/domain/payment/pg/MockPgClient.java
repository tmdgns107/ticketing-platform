package com.ticketing.domain.payment.pg;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stand-in PG. Approves after a tiny simulated round trip, declining a configurable fraction of
 * calls ({@code app.payment.pg.failure-rate}, default 0) so failure handling can be exercised.
 */
@Slf4j
@Component
public class MockPgClient implements PgClient {

    private final double failureRate;

    public MockPgClient(@Value("${app.payment.pg.failure-rate:0.0}") double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public PgApproval approve(long amount, String idempotencyKey) {
        sleepQuietly();
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new PgException("PG declined (simulated)");
        }
        return new PgApproval("pg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
    }

    @Override
    public void cancel(String pgTransactionId) {
        sleepQuietly();
        log.info("PG cancel (simulated): {}", pgTransactionId);
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
