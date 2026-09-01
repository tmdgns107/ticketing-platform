package com.ticketing.domain.payment;

import com.ticketing.global.common.BaseTimeEntity;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "pg_transaction_id", length = 80)
    private String pgTransactionId;

    private Payment(Long reservationId, Long memberId, long amount, String idempotencyKey) {
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment pending(
            Long reservationId, Long memberId, long amount, String idempotencyKey) {
        return new Payment(reservationId, memberId, amount, idempotencyKey);
    }

    public boolean isTerminal() {
        return status != PaymentStatus.PENDING;
    }

    public void markPaid(String pgTransactionId) {
        requirePending();
        this.status = PaymentStatus.PAID;
        this.pgTransactionId = pgTransactionId;
    }

    public void markFailed() {
        requirePending();
        this.status = PaymentStatus.FAILED;
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
        }
    }
}
