package com.ticketing.domain.seat;

import com.ticketing.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A price tier (VIP / R / S ...) for a performance. */
@Getter
@Entity
@Table(name = "seat_grade")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatGrade extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private long price;

    private SeatGrade(Long performanceId, String name, long price) {
        this.performanceId = performanceId;
        this.name = name;
        this.price = price;
    }

    public static SeatGrade of(Long performanceId, String name, long price) {
        return new SeatGrade(performanceId, name, price);
    }
}
