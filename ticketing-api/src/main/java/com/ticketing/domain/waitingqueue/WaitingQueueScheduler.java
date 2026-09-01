package com.ticketing.domain.waitingqueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingQueueScheduler {

    private final WaitingQueueService waitingQueueService;

    @Scheduled(fixedDelayString = "${app.waiting-queue.promote-interval:PT3S}")
    public void promote() {
        try {
            waitingQueueService.promoteDue();
        } catch (RuntimeException e) {
            log.error("waiting-queue promotion failed", e);
        }
    }
}
