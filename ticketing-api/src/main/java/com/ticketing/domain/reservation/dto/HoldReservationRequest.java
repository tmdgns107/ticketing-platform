package com.ticketing.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HoldReservationRequest(
        @NotNull @Positive Long scheduleId,
        @NotNull @Positive Long seatId
) {
}
