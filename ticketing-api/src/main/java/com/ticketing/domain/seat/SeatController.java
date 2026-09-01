package com.ticketing.domain.seat;

import com.ticketing.domain.seat.dto.ScheduleResponse;
import com.ticketing.domain.seat.dto.SeatMapResponse;
import com.ticketing.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatQueryService seatQueryService;

    @GetMapping("/performances/{performanceId}/schedules")
    public ApiResponse<List<ScheduleResponse>> getSchedules(@PathVariable Long performanceId) {
        return ApiResponse.ok(seatQueryService.getSchedules(performanceId));
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    public ApiResponse<SeatMapResponse> getSeatMap(@PathVariable Long scheduleId) {
        return ApiResponse.ok(seatQueryService.getSeatMap(scheduleId));
    }
}
