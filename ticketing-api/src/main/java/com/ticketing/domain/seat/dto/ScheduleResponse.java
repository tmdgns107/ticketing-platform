package com.ticketing.domain.seat.dto;

import com.ticketing.domain.seat.Schedule;
import java.time.Instant;

public record ScheduleResponse(Long id, Long performanceId, Instant startsAt) {
    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getId(), schedule.getPerformanceId(), schedule.getStartsAt());
    }
}
