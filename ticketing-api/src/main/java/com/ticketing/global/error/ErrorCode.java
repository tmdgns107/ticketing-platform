package com.ticketing.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다."),

    // performance
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 정보를 찾을 수 없습니다."),

    // reservation / seat
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "이미 선점된 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "예매할 수 없는 좌석입니다."),
    RESERVATION_EXPIRED(HttpStatus.GONE, "선점 시간이 만료되었습니다."),

    // waiting queue
    QUEUE_NOT_ALLOWED(HttpStatus.TOO_MANY_REQUESTS, "아직 입장 순서가 아닙니다."),

    // payment
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED, "결제에 실패했습니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "이미 처리된 결제 요청입니다.");

    private final HttpStatus status;
    private final String message;
}
