package com.ticketing.domain.reservation;

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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "hold_expires_at", nullable = false)
    private Instant holdExpiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    private Reservation(Long memberId, Long scheduleId, Long seatId, long price, Instant holdExpiresAt) {
        this.memberId = memberId;
        this.scheduleId = scheduleId;
        this.seatId = seatId;
        this.price = price;
        this.status = ReservationStatus.PENDING;
        this.holdExpiresAt = holdExpiresAt;
    }

    public static Reservation pending(
            Long memberId, Long scheduleId, Long seatId, long price, Instant holdExpiresAt) {
        return new Reservation(memberId, scheduleId, seatId, price, holdExpiresAt);
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    public boolean isExpired(Instant now) {
        return status == ReservationStatus.PENDING && holdExpiresAt.isBefore(now);
    }

    public void confirm(Instant now) {
        requirePending();
        if (holdExpiresAt.isBefore(now)) {
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = now;
    }

    public void cancel() {
        requirePending();
        this.status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        requirePending();
        this.status = ReservationStatus.EXPIRED;
    }

    private void requirePending() {
        if (status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }
    }
}
