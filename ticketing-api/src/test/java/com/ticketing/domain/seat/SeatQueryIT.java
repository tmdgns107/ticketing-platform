package com.ticketing.domain.seat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Exercises the dev seed grid (schedule 1 → 80 seats) through the public endpoints. */
@IntegrationTest
@AutoConfigureMockMvc
class SeatQueryIT {

  @Autowired MockMvc mockMvc;

  @Test
  void seatMapExposesGradesAndSeatsFromTheSeed() throws Exception {
    mockMvc
        .perform(get("/api/v1/schedules/1/seats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.scheduleId").value(1))
        .andExpect(jsonPath("$.data.seats.length()").value(80))
        .andExpect(jsonPath("$.data.grades[?(@.name == 'VIP')].totalCount").value(20))
        .andExpect(jsonPath("$.data.grades[?(@.name == 'R')].totalCount").value(60));
  }

  @Test
  void schedulesAreListedForAPerformance() throws Exception {
    mockMvc
        .perform(get("/api/v1/performances/1/schedules"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].performanceId").value(1));
  }

  @Test
  void unknownScheduleReturns404() throws Exception {
    mockMvc
        .perform(get("/api/v1/schedules/999999/seats"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("SCHEDULE_NOT_FOUND"));
  }
}
