package com.ticketing.domain.reservation.lock;

import com.ticketing.domain.reservation.Reservation;
import com.ticketing.domain.reservation.ReservationCreator;
import com.ticketing.domain.reservation.ReservationProperties;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptimisticSeatHoldStrategy implements SeatHoldStrategy {

    private final ReservationCreator creator;
    private final ReservationProperties properties;

    @Override
    public LockStrategy id() {
        return LockStrategy.OPTIMISTIC;
    }

    @Override
    public Reservation hold(Long memberId, Long scheduleId, Long seatId) {
        int maxAttempts = properties.optimisticMaxAttempts();
        for (int attempt = 1; ; attempt++) {
            try {
                // The retry loop must sit OUTSIDE the transaction: the @Version conflict only
                // surfaces on commit, so each attempt needs its own transaction.
                return creator.create(memberId, scheduleId, seatId, false);
            } catch (OptimisticLockingFailureException e) {
                if (attempt >= maxAttempts) {
                    log.debug("optimistic hold gave up after {} attempts (seat {})", attempt, seatId);
                    throw new BusinessException(ErrorCode.SEAT_LOCK_CONFLICT);
                }
                backoff(attempt);
            }
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(Math.min(20L * attempt, 100L));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SEAT_LOCK_CONFLICT);
        }
    }
}
