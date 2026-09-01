package com.ticketing.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Writes the {@link ApiResponse} envelope for unauthenticated requests (401). */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeError(response, ErrorCode.AUTH_REQUIRED);
    }

    void writeError(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(), ApiResponse.fail(code, code.getMessage()));
    }
}
