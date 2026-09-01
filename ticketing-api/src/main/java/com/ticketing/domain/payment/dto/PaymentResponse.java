package com.ticketing.domain.payment.dto;

import com.ticketing.domain.payment.Payment;
import com.ticketing.domain.payment.PaymentStatus;

public record PaymentResponse(
        Long id,
        Long reservationId,
        long amount,
        PaymentStatus status,
        String pgTransactionId) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservationId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPgTransactionId());
    }
}
