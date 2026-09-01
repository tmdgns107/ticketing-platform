package com.ticketing.domain.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Releases seats whose hold window elapsed without payment. Single-instance safe only;
 * a multi-node deployment would guard this with a ShedLock-style lease.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${app.reservation.expiry-scan-interval:PT10S}")
    public void sweep() {
        try {
            reservationService.expireOverdue();
        } catch (RuntimeException e) {
            log.error("reservation expiry sweep failed", e);
        }
    }
}
