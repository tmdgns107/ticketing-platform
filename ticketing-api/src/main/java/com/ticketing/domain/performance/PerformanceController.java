package com.ticketing.domain.performance;

import com.ticketing.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping
    public ApiResponse<List<PerformanceResponse>> getPerformances() {
        return ApiResponse.ok(performanceService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PerformanceResponse> getPerformance(@PathVariable Long id) {
        return ApiResponse.ok(performanceService.findById(id));
    }
}
