package com.ticketing.domain.performance;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PerformanceController.class)
class PerformanceControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean PerformanceService performanceService;

  @Test
  void returnsPerformanceListInEnvelope() throws Exception {
    given(performanceService.findAll())
        .willReturn(
            List.of(
                new PerformanceResponse(
                    1L,
                    "Show",
                    "Hall",
                    Instant.parse("2026-09-10T11:00:00Z"),
                    Instant.parse("2026-10-01T10:00:00Z"),
                    PerformanceStatus.ON_SALE)));

    mockMvc
        .perform(get("/api/v1/performances"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].status").value("ON_SALE"));
  }

  @Test
  void mapsBusinessExceptionToErrorEnvelope() throws Exception {
    given(performanceService.findById(999L))
        .willThrow(new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND));

    mockMvc
        .perform(get("/api/v1/performances/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("PERFORMANCE_NOT_FOUND"));
  }

  @Test
  void rejectsNonNumericPathWith400() throws Exception {
    mockMvc
        .perform(get("/api/v1/performances/not-a-number"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));

    verifyNoInteractions(performanceService);
  }
}
