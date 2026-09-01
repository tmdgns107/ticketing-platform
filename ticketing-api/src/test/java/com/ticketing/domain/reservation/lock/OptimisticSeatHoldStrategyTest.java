package com.ticketing.domain.reservation.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ticketing.domain.reservation.Reservation;
import com.ticketing.domain.reservation.ReservationCreator;
import com.ticketing.domain.reservation.ReservationProperties;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class OptimisticSeatHoldStrategyTest {

  @Mock ReservationCreator creator;

  private final ReservationProperties props =
      new ReservationProperties(null, null, 3, null);

  @Test
  void retriesOnVersionConflictThenSucceeds() {
    OptimisticSeatHoldStrategy strategy = new OptimisticSeatHoldStrategy(creator, props);
    Reservation ok = Reservation.pending(1L, 1L, 1L, 1000L, Instant.now());
    given(creator.create(eq(1L), eq(1L), eq(1L), anyBoolean()))
        .willThrow(new ObjectOptimisticLockingFailureException("seat", 1L))
        .willReturn(ok);

    Reservation result = strategy.hold(1L, 1L, 1L);

    assertThat(result).isSameAs(ok);
    verify(creator, times(2)).create(any(), any(), any(), anyBoolean());
  }

  @Test
  void givesUpWithLockConflictAfterMaxAttempts() {
    OptimisticSeatHoldStrategy strategy = new OptimisticSeatHoldStrategy(creator, props);
    given(creator.create(any(), any(), any(), anyBoolean()))
        .willThrow(new ObjectOptimisticLockingFailureException("seat", 1L));

    assertThatThrownBy(() -> strategy.hold(1L, 1L, 1L))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.SEAT_LOCK_CONFLICT);

    verify(creator, times(3)).create(any(), any(), any(), anyBoolean());
  }
}
