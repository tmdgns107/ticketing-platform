package com.ticketing.domain.waitingqueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.domain.waitingqueue.WaitingQueueService.Admission;
import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.security.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards the reservation endpoint: when {@code app.waiting-queue.enforce=true}, the caller must
 * present a valid {@code X-Queue-Token} (minted by queue promotion) belonging to themselves.
 *
 * <p>Known simplification: the token is verified against the authenticated member but not against
 * the schedule in the request body.
 *
 * <p>Not a {@code @Component} — {@code @WebMvcTest} auto-detects {@code HandlerInterceptor} beans,
 * so it is instantiated by {@link com.ticketing.global.config.WebConfig} instead.
 */
public class QueueAdmissionInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Queue-Token";
    static final String ADMITTED_SCHEDULE_ATTR = "queue.admittedScheduleId";

    private final WaitingQueueService waitingQueueService;
    private final WaitingQueueProperties properties;
    private final ObjectMapper objectMapper;

    public QueueAdmissionInterceptor(
            WaitingQueueService waitingQueueService,
            WaitingQueueProperties properties,
            ObjectMapper objectMapper) {
        this.waitingQueueService = waitingQueueService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!properties.enforce() || !"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Optional<Admission> admission =
                waitingQueueService.resolveEntryToken(request.getHeader(HEADER));
        Long memberId = currentMemberId();
        if (admission.isEmpty() || memberId == null || admission.get().memberId() != memberId) {
            writeQueueRejected(response);
            return false;
        }
        request.setAttribute(ADMITTED_SCHEDULE_ATTR, admission.get().scheduleId());
        return true;
    }

    private Long currentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof LoginMember m ? m.id() : null;
    }

    private void writeQueueRejected(HttpServletResponse response) throws Exception {
        response.setStatus(ErrorCode.QUEUE_NOT_ALLOWED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.fail(ErrorCode.QUEUE_NOT_ALLOWED, ErrorCode.QUEUE_NOT_ALLOWED.getMessage()));
    }
}
