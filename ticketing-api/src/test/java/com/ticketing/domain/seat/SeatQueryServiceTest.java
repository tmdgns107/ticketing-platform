package com.ticketing.domain.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ticketing.domain.seat.dto.SeatMapResponse;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeatQueryServiceTest {

  @Mock ScheduleRepository scheduleRepository;
  @Mock SeatGradeRepository seatGradeRepository;
  @Mock SeatRepository seatRepository;

  @InjectMocks SeatQueryService seatQueryService;

  @Test
  void seatMapForUnknownScheduleFails() {
    given(scheduleRepository.findById(999L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> seatQueryService.getSeatMap(999L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
  }

  @Test
  void seatMapCountsAvailableSeatsPerGrade() {
    Schedule schedule = Schedule.of(1L, Instant.parse("2026-10-01T10:00:00Z"));
    ReflectionTestUtils.setField(schedule, "id", 1L);
    given(scheduleRepository.findById(1L)).willReturn(Optional.of(schedule));

    SeatGrade vip = SeatGrade.of(1L, "VIP", 165_000);
    ReflectionTestUtils.setField(vip, "id", 10L);
    given(seatGradeRepository.findByPerformanceIdOrderByPriceDesc(1L)).willReturn(List.of(vip));

    Seat s1 = seat(10L);
    Seat s2 = seat(10L);
    Seat s3 = seat(10L);
    s3.hold(); // one of three is unavailable
    given(seatRepository.findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(1L))
        .willReturn(List.of(s1, s2, s3));

    SeatMapResponse map = seatQueryService.getSeatMap(1L);

    assertThat(map.seats()).hasSize(3);
    assertThat(map.grades()).hasSize(1);
    assertThat(map.grades().getFirst().availableCount()).isEqualTo(2);
    assertThat(map.grades().getFirst().totalCount()).isEqualTo(3);
  }

  private static Seat seat(Long gradeId) {
    return Seat.of(1L, gradeId, "VIP", "A", 1);
  }
}
