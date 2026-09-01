package com.ticketing.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.domain.member.Member;
import com.ticketing.domain.member.MemberRepository;
import com.ticketing.domain.reservation.dto.HoldReservationRequest;
import com.ticketing.domain.reservation.lock.LockStrategy;
import com.ticketing.domain.seat.Seat;
import com.ticketing.domain.seat.SeatRepository;
import com.ticketing.domain.seat.SeatStatus;
import com.ticketing.support.IntegrationTest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The project's headline test: fire many concurrent holds at one seat under each locking
 * strategy and assert that exactly one wins — no oversell, no lost update.
 */
@IntegrationTest
class ReservationConcurrencyIT {

  private static final int THREADS = 24;
  private static final long SCHEDULE_ID = 1L;

  @Autowired ReservationService reservationService;
  @Autowired SeatRepository seatRepository;
  @Autowired ReservationRepository reservationRepository;
  @Autowired MemberRepository memberRepository;

  private Long memberId;
  private Long seatId;

  @BeforeEach
  void setUp() {
    memberId =
        memberRepository
            .save(Member.create("racer-" + System.nanoTime() + "@t.com", "hash", "racer"))
            .getId();
    Seat seat =
        seatRepository.findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(SCHEDULE_ID).getFirst();
    seatId = seat.getId();
  }

  @AfterEach
  void tearDown() {
    reservationRepository.deleteAll();
    seatRepository
        .findById(seatId)
        .ifPresent(
            s -> {
              if (s.getStatus() != SeatStatus.AVAILABLE) {
                s.release();
                seatRepository.saveAndFlush(s);
              }
            });
  }

  @ParameterizedTest
  @EnumSource(LockStrategy.class)
  void exactlyOneHoldWinsUnderContention(LockStrategy strategy) throws InterruptedException {
    var request = new HoldReservationRequest(SCHEDULE_ID, seatId);
    var success = new AtomicInteger();
    var failure = new AtomicInteger();
    var ready = new CountDownLatch(THREADS);
    var go = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(THREADS);

    for (int i = 0; i < THREADS; i++) {
      pool.submit(
          () -> {
            ready.countDown();
            try {
              go.await();
              reservationService.hold(memberId, request, strategy);
              success.incrementAndGet();
            } catch (RuntimeException e) {
              failure.incrementAndGet();
            }
            return null;
          });
    }
    ready.await();
    go.countDown();
    pool.shutdown();
    assertThat(pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

    assertThat(success.get()).as("winners for %s", strategy).isEqualTo(1);
    assertThat(failure.get()).isEqualTo(THREADS - 1);
    assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo(SeatStatus.HELD);
    assertThat(reservationRepository.count()).isEqualTo(1);
  }
}
