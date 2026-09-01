package com.ticketing.domain.payment;

import com.ticketing.domain.payment.dto.PaymentResponse;
import com.ticketing.domain.payment.pg.PgClient;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProcessor processor;
    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;

    /**
     * Pay for a reservation. Safe to retry with the same {@code idempotencyKey}: a completed
     * payment is replayed rather than re-charged.
     */
    public PaymentResponse pay(Long memberId, Long reservationId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        Payment payment = processor.createOrGet(memberId, reservationId, idempotencyKey);
        if (payment.isTerminal()) {
            return PaymentResponse.from(payment);
        }

        try {
            PgClient.PgApproval approval = pgClient.approve(payment.getAmount(), idempotencyKey);
            PaymentResponse response = processor.applyApproval(payment.getId(), approval.transactionId());
            log.info("payment {} PAID (reservation {})", payment.getId(), reservationId);
            return response;
        } catch (PgClient.PgException e) {
            log.warn("payment {} failed at PG: {}", payment.getId(), e.getMessage());
            processor.applyFailure(payment.getId());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(Long memberId, Long paymentId) {
        Payment payment =
                paymentRepository
                        .findById(paymentId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        return PaymentResponse.from(payment);
    }
}
