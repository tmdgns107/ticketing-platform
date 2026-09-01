package com.ticketing.domain.reservation.lock;

import com.ticketing.domain.reservation.Reservation;
import com.ticketing.domain.reservation.ReservationCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PessimisticSeatHoldStrategy implements SeatHoldStrategy {

    private final ReservationCreator creator;

    @Override
    public LockStrategy id() {
        return LockStrategy.PESSIMISTIC;
    }

    @Override
    public Reservation hold(Long memberId, Long scheduleId, Long seatId) {
        // SELECT ... FOR UPDATE inside the transaction: contenders block here until commit,
        // then see status = HELD and fail fast with SEAT_NOT_AVAILABLE.
        return creator.create(memberId, scheduleId, seatId, true);
    }
}
