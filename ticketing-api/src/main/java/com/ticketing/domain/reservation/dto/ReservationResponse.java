package com.ticketing.domain.reservation.dto;

import com.ticketing.domain.reservation.Reservation;
import com.ticketing.domain.reservation.ReservationStatus;
import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long scheduleId,
        Long seatId,
        long price,
        ReservationStatus status,
        Instant holdExpiresAt,
        Instant confirmedAt
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getScheduleId(),
                r.getSeatId(),
                r.getPrice(),
                r.getStatus(),
                r.getHoldExpiresAt(),
                r.getConfirmedAt());
    }
}
