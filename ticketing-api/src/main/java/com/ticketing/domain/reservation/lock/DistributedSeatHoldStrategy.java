package com.ticketing.domain.reservation.lock;

import com.ticketing.domain.reservation.Reservation;
import com.ticketing.domain.reservation.ReservationCreator;
import com.ticketing.domain.reservation.ReservationProperties;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DistributedSeatHoldStrategy implements SeatHoldStrategy {

    private final DistributedLock distributedLock;
    private final ReservationCreator creator;
    private final ReservationProperties properties;

    @Override
    public LockStrategy id() {
        return LockStrategy.DISTRIBUTED;
    }

    @Override
    public Reservation hold(Long memberId, Long scheduleId, Long seatId) {
        String key = "lock:seat:" + seatId;
        return distributedLock.executeOrElse(
                key,
                properties.distributedLockTtl(),
                () -> creator.create(memberId, scheduleId, seatId, false),
                () -> {
                    throw new BusinessException(ErrorCode.SEAT_LOCK_CONFLICT);
                });
    }
}
