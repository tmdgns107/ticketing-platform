package com.ticketing.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.domain.member.Member;
import com.ticketing.domain.member.MemberRepository;
import com.ticketing.domain.reservation.dto.HoldReservationRequest;
import com.ticketing.domain.reservation.lock.LockStrategy;
import com.ticketing.domain.seat.SeatRepository;
import com.ticketing.domain.seat.SeatStatus;
import com.ticketing.global.error.BusinessException;
import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ReservationFlowIT {

  private static final long SCHEDULE_ID = 1L;

  @Autowired ReservationService reservationService;
  @Autowired SeatRepository seatRepository;
  @Autowired ReservationRepository reservationRepository;
  @Autowired MemberRepository memberRepository;

  private Long member(String tag) {
    return memberRepository
        .save(Member.create(tag + "-" + System.nanoTime() + "@t.com", "h", tag))
        .getId();
  }

  private Long freeSeat(int index) {
    return seatRepository
        .findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(SCHEDULE_ID)
        .get(index)
        .getId();
  }

  @AfterEach
  void cleanup() {
    reservationRepository.deleteAll();
    seatRepository
        .findAll()
        .forEach(
            s -> {
              if (s.getStatus() != SeatStatus.AVAILABLE) {
                s.release();
              }
            });
    seatRepository.flush();
  }

  @Test
  void holdThenConfirmMarksSeatSold() {
    Long memberId = member("buyer");
    Long seatId = freeSeat(0);

    var held = reservationService.hold(memberId, new HoldReservationRequest(SCHEDULE_ID, seatId), LockStrategy.PESSIMISTIC);
    var confirmed = reservationService.confirm(memberId, held.id());

    assertThat(confirmed.status()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.SOLD);
  }

  @Test
  void anotherMemberCannotConfirmMyReservation() {
    Long owner = member("owner");
    Long intruder = member("intruder");
    Long seatId = freeSeat(1);

    var held = reservationService.hold(owner, new HoldReservationRequest(SCHEDULE_ID, seatId), null);

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> reservationService.confirm(intruder, held.id()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void expirySweepReleasesHeldSeat() {
    Long memberId = member("slow");
    Long seatId = freeSeat(2);
    reservationService.hold(memberId, new HoldReservationRequest(SCHEDULE_ID, seatId), null);

    // force the hold into the past
    var reservation = reservationRepository.findAll().getFirst();
    org.springframework.test.util.ReflectionTestUtils.setField(
        reservation, "holdExpiresAt", java.time.Instant.now().minusSeconds(1));
    reservationRepository.saveAndFlush(reservation);

    int expired = reservationService.expireOverdue();

    assertThat(expired).isEqualTo(1);
    assertThat(seatRepository.findById(seatId).orElseThrow().getStatus())
        .isEqualTo(SeatStatus.AVAILABLE);
  }
}
