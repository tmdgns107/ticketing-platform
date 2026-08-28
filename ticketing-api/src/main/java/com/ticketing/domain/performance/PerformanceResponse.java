package com.ticketing.domain.performance;

import java.time.Instant;

public record PerformanceResponse(
        Long id,
        String title,
        String venue,
        Instant opensAt,
        Instant startsAt,
        PerformanceStatus status
) {
    public static PerformanceResponse from(Performance performance) {
        return new PerformanceResponse(
                performance.getId(),
                performance.getTitle(),
                performance.getVenue(),
                performance.getOpensAt(),
                performance.getStartsAt(),
                performance.getStatus()
        );
    }
}
