package com.ticketing;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.domain.performance.PerformanceRepository;
import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class TicketingApplicationIT {

  @Autowired PerformanceRepository performanceRepository;

  @Test
  void contextLoadsAndFlywaySchemaIsQueryable() {
    // If the context is up and V1 ran, the mapped table exists and the count query succeeds.
    assertThat(performanceRepository.count()).isGreaterThanOrEqualTo(0);
  }
}
