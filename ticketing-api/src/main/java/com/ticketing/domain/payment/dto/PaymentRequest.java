package com.ticketing.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentRequest(@NotNull @Positive Long reservationId) {
}
