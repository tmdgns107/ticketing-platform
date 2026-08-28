package com.ticketing.domain.performance;

import com.ticketing.global.common.BaseTimeEntity;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "performance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String venue;

    @Column(name = "opens_at", nullable = false)
    private Instant opensAt;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceStatus status;

    @Builder
    private Performance(String title, String venue, Instant opensAt, Instant startsAt, PerformanceStatus status) {
        this.title = title;
        this.venue = venue;
        this.opensAt = opensAt;
        this.startsAt = startsAt;
        this.status = status == null ? PerformanceStatus.SCHEDULED : status;
    }
}
