package com.ticketing.domain.waitingqueue;

import com.ticketing.domain.waitingqueue.dto.QueueStatusResponse;
import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.security.CurrentMember;
import com.ticketing.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/waiting-queue")
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @PostMapping("/{scheduleId}/enter")
    public ApiResponse<QueueStatusResponse> enter(
            @CurrentMember LoginMember member, @PathVariable long scheduleId) {
        return ApiResponse.ok(waitingQueueService.enter(scheduleId, member.id()));
    }

    @GetMapping("/{scheduleId}/status")
    public ApiResponse<QueueStatusResponse> status(
            @CurrentMember LoginMember member, @PathVariable long scheduleId) {
        return ApiResponse.ok(waitingQueueService.status(scheduleId, member.id()));
    }
}
