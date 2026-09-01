package com.ticketing.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketing.domain.member.Member;
import com.ticketing.domain.member.MemberRepository;
import com.ticketing.domain.payment.pg.PgClient;
import com.ticketing.domain.reservation.ReservationRepository;
import com.ticketing.domain.reservation.ReservationService;
import com.ticketing.domain.reservation.dto.HoldReservationRequest;
import com.ticketing.domain.seat.SeatRepository;
import com.ticketing.domain.seat.SeatStatus;
import com.ticketing.global.error.BusinessException;
import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class PaymentFlowIT {

  private static final long SCHEDULE = 1L;

  @Autowired PaymentService paymentService;
  @Autowired PaymentRepository paymentRepository;
  @Autowired ReservationService reservationService;
  @Autowired ReservationRepository reservationRepository;
  @Autowired SeatRepository seatRepository;
  @Autowired MemberRepository memberRepository;

  @MockitoBean PgClient pgClient;

  private Long memberId;
  private Long seatId;
  private Long reservationId;

  @AfterEach
  void cleanup() {
    paymentRepository.deleteAll();
    reservationRepository.deleteAll();
    seatRepository
        .findAll()
        .forEach(
            s -> {
              if (s.getStatus() != SeatStatus.AVAILABLE) s.release();
            });
    seatRepository.flush();
  }

  private void arrangeHeldReservation(int seatIndex) {
    memberId =
        memberRepository.save(Member.create("pay-" + System.nanoTime() + "@t.com", "h", "pay")).getId();
    seatId =
        seatRepository
            .findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(SCHEDULE)
            .get(seatIndex)
            .getId();
    reservationId =
        reservationService
            .hold(memberId, new HoldReservationRequest(SCHEDULE, seatId), null)
            .id();
  }

  @Test
  void successfulPaymentConfirmsReservationAndSellsSeat() {
    arrangeHeldReservation(40);
    org.mockito.BDDMockito.given(pgClient.approve(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString()))
        .willReturn(new PgClient.PgApproval("pg_ok"));

    var response = paymentService.pay(memberId, reservationId, "key-" + System.nanoTime());

    assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
    assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.SOLD);
  }

  @Test
  void sameIdempotencyKeyIsNotChargedTwice() {
    arrangeHeldReservation(41);
    org.mockito.BDDMockito.given(pgClient.approve(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString()))
        .willReturn(new PgClient.PgApproval("pg_ok"));
    String key = "idem-" + System.nanoTime();

    var first = paymentService.pay(memberId, reservationId, key);
    var second = paymentService.pay(memberId, reservationId, key);

    assertThat(second.id()).isEqualTo(first.id());
    assertThat(paymentRepository.count()).isEqualTo(1);
    org.mockito.Mockito.verify(pgClient, org.mockito.Mockito.times(1))
        .approve(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void pgDeclineLeavesReservationPendingAndPaymentFailed() {
    arrangeHeldReservation(42);
    org.mockito.BDDMockito.given(pgClient.approve(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString()))
        .willThrow(new PgClient.PgException("declined"));

    assertThatThrownBy(() -> paymentService.pay(memberId, reservationId, "key-" + System.nanoTime()))
        .isInstanceOf(BusinessException.class);

    assertThat(paymentRepository.findByReservationId(reservationId).orElseThrow().getStatus())
        .isEqualTo(PaymentStatus.FAILED);
    assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.HELD);
  }
}
