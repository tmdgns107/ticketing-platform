package com.ticketing.domain.seat;

import com.ticketing.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A single showing of a performance. Seat inventory is scoped to a schedule. */
@Getter
@Entity
@Table(name = "schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    private Schedule(Long performanceId, Instant startsAt) {
        this.performanceId = performanceId;
        this.startsAt = startsAt;
    }

    public static Schedule of(Long performanceId, Instant startsAt) {
        return new Schedule(performanceId, startsAt);
    }
}
