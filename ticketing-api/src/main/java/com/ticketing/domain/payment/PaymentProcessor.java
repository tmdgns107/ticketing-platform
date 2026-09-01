package com.ticketing.domain.payment;

import com.ticketing.domain.payment.dto.PaymentResponse;
import com.ticketing.domain.reservation.ReservationService;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The transactional steps of a payment, split so the (slow, external) PG call happens between
 * two short transactions rather than inside one long-held one.
 */
@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final ReservationService reservationService;

    /** Idempotent: a repeat {@code idempotencyKey} returns the row created the first time. */
    @Transactional
    public Payment createOrGet(Long memberId, Long reservationId, String idempotencyKey) {
        var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            if (!payment.getMemberId().equals(memberId)
                    || !payment.getReservationId().equals(reservationId)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_MISMATCH);
            }
            return payment;
        }

        long amount = reservationService.amountDue(memberId, reservationId);
        try {
            return paymentRepository.saveAndFlush(
                    Payment.pending(reservationId, memberId, amount, idempotencyKey));
        } catch (DataIntegrityViolationException race) {
            return paymentRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED));
        }
    }

    @Transactional
    public PaymentResponse applyApproval(Long paymentId, String pgTransactionId) {
        Payment payment = load(paymentId);
        payment.markPaid(pgTransactionId);
        reservationService.confirm(payment.getMemberId(), payment.getReservationId());
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse applyFailure(Long paymentId) {
        Payment payment = load(paymentId);
        payment.markFailed();
        return PaymentResponse.from(payment);
    }

    private Payment load(Long paymentId) {
        return paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
