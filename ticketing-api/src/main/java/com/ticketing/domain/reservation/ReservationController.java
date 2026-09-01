package com.ticketing.domain.reservation;

import com.ticketing.domain.reservation.dto.HoldReservationRequest;
import com.ticketing.domain.reservation.dto.ReservationResponse;
import com.ticketing.domain.reservation.lock.LockStrategy;
import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.security.CurrentMember;
import com.ticketing.global.security.LoginMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> hold(
            @CurrentMember LoginMember member,
            @Valid @RequestBody HoldReservationRequest request,
            @RequestParam(name = "lockStrategy", required = false) LockStrategy lockStrategy) {
        return ApiResponse.ok(reservationService.hold(member.id(), request, lockStrategy));
    }

    @GetMapping
    public ApiResponse<List<ReservationResponse>> myReservations(@CurrentMember LoginMember member) {
        return ApiResponse.ok(reservationService.getMine(member.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> get(
            @CurrentMember LoginMember member, @PathVariable Long id) {
        return ApiResponse.ok(reservationService.get(member.id(), id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<ReservationResponse> confirm(
            @CurrentMember LoginMember member, @PathVariable Long id) {
        return ApiResponse.ok(reservationService.confirm(member.id(), id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancel(
            @CurrentMember LoginMember member, @PathVariable Long id) {
        return ApiResponse.ok(reservationService.cancel(member.id(), id));
    }
}
