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

    // auth / member
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),

    // performance
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 정보를 찾을 수 없습니다."),

    // schedule / seat
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "공연 회차를 찾을 수 없습니다."),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_HELD(HttpStatus.CONFLICT, "이미 선점된 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.CONFLICT, "예매할 수 없는 좌석입니다."),
    RESERVATION_EXPIRED(HttpStatus.GONE, "선점 시간이 만료되었습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예매 정보를 찾을 수 없습니다."),
    RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 예매가 아닙니다."),
    SEAT_LOCK_CONFLICT(HttpStatus.CONFLICT, "다른 사용자가 좌석을 선점하고 있습니다. 다시 시도해주세요."),

    // waiting queue
    QUEUE_NOT_ALLOWED(HttpStatus.TOO_MANY_REQUESTS, "아직 입장 순서가 아닙니다."),

    // payment
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED, "결제에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "이미 처리된 결제 요청입니다."),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다."),
    IDEMPOTENCY_KEY_MISMATCH(HttpStatus.CONFLICT, "동일한 Idempotency-Key 로 다른 요청을 보낼 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
