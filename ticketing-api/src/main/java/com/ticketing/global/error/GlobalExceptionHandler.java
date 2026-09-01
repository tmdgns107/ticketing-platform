package com.ticketing.global.error;

import com.ticketing.global.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("business error: {} - {}", code.name(), e.getMessage());
        return toResponse(code, e.getMessage());
    }

    /** Bean-validation failures on @RequestBody DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    /** Validation on @PathVariable / @RequestParam (Spring 6.1+ method validation). */
    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleParamValidation(Exception e) {
        log.debug("param validation failed: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
    }

    /** Wrong type in the path/query (e.g. /performances/abc), missing param, unreadable body. */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception e) {
        log.debug("malformed request: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, "지원하지 않는 요청 메서드입니다."));
    }

    /** Unknown route / missing static resource — return a clean 404 envelope, not 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException e) {
        return toResponse(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("unexpected error", e);
        return toResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code, message));
    }
}
