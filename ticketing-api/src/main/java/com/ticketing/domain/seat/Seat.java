package com.ticketing.domain.seat;

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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_grade_id", nullable = false)
    private Long seatGradeId;

    @Column(nullable = false, length = 20)
    private String section;

    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Column(name = "seat_no", nullable = false)
    private int seatNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    /** Optimistic-lock guard, exercised by the reservation benchmark in phase 3. */
    @Version
    private long version;

    private Seat(Long scheduleId, Long seatGradeId, String section, String rowLabel, int seatNo) {
        this.scheduleId = scheduleId;
        this.seatGradeId = seatGradeId;
        this.section = section;
        this.rowLabel = rowLabel;
        this.seatNo = seatNo;
        this.status = SeatStatus.AVAILABLE;
    }

    public static Seat of(Long scheduleId, Long seatGradeId, String section, String rowLabel, int seatNo) {
        return new Seat(scheduleId, seatGradeId, section, rowLabel, seatNo);
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void hold() {
        if (status != SeatStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        this.status = SeatStatus.HELD;
    }

    public void release() {
        if (status == SeatStatus.SOLD) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        this.status = SeatStatus.AVAILABLE;
    }

    public void markSold() {
        if (status != SeatStatus.HELD) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        this.status = SeatStatus.SOLD;
    }
}
