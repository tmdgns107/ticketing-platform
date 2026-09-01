package com.ticketing.domain.reservation.lock;

import com.ticketing.domain.reservation.Reservation;

/** Holds a seat and creates its PENDING reservation under a specific concurrency-control scheme. */
public interface SeatHoldStrategy {

    LockStrategy id();

    Reservation hold(Long memberId, Long scheduleId, Long seatId);
}
