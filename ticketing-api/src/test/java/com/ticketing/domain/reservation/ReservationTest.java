package com.ticketing.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReservationTest {

  private static final Instant NOW = Instant.parse("2026-09-01T00:00:00Z");

  private Reservation pending(Instant holdExpiresAt) {
    return Reservation.pending(1L, 1L, 1L, 100_000L, holdExpiresAt);
  }

  @Test
  void confirmSucceedsWithinHoldWindow() {
    Reservation r = pending(NOW.plusSeconds(60));

    r.confirm(NOW);

    assertThat(r.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(r.getConfirmedAt()).isEqualTo(NOW);
  }

  @Test
  void confirmAfterExpiryIsRejected() {
    Reservation r = pending(NOW.minusSeconds(1));

    assertThatThrownBy(() -> r.confirm(NOW))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.RESERVATION_EXPIRED);
  }

  @Test
  void cannotConfirmACancelledReservation() {
    Reservation r = pending(NOW.plusSeconds(60));
    r.cancel();

    assertThatThrownBy(() -> r.confirm(NOW)).isInstanceOf(BusinessException.class);
  }

  @Test
  void isExpiredOnlyWhilePendingAndPastDeadline() {
    assertThat(pending(NOW.minusSeconds(1)).isExpired(NOW)).isTrue();
    assertThat(pending(NOW.plusSeconds(1)).isExpired(NOW)).isFalse();

    Reservation confirmed = pending(NOW.plusSeconds(60));
    confirmed.confirm(NOW);
    assertThat(confirmed.isExpired(NOW.plusSeconds(120))).isFalse();
  }

  @Test
  void ownershipCheck() {
    Reservation r = pending(NOW.plusSeconds(60));
    assertThat(r.isOwnedBy(1L)).isTrue();
    assertThat(r.isOwnedBy(2L)).isFalse();
  }
}
