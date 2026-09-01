package com.ticketing.domain.waitingqueue;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.waiting-queue")
public record WaitingQueueProperties(
        boolean enforce,
        int promoteBatchSize,
        Duration promoteInterval,
        Duration activeTtl,
        Duration entryTokenTtl) {

    public WaitingQueueProperties {
        if (promoteBatchSize <= 0) {
            promoteBatchSize = 50;
        }
        if (promoteInterval == null) {
            promoteInterval = Duration.ofSeconds(3);
        }
        if (activeTtl == null) {
            activeTtl = Duration.ofMinutes(5);
        }
        if (entryTokenTtl == null) {
            entryTokenTtl = Duration.ofMinutes(5);
        }
    }
}
