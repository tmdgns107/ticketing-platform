package com.ticketing.domain.payment;

import com.ticketing.domain.payment.dto.PaymentRequest;
import com.ticketing.domain.payment.dto.PaymentResponse;
import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.security.CurrentMember;
import com.ticketing.global.security.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> pay(
            @CurrentMember LoginMember member,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok(
                paymentService.pay(member.id(), request.reservationId(), idempotencyKey));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> get(
            @CurrentMember LoginMember member, @PathVariable Long id) {
        return ApiResponse.ok(paymentService.get(member.id(), id));
    }
}
