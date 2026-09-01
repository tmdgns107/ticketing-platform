package com.ticketing.domain.reservation;

import com.ticketing.domain.reservation.dto.HoldReservationRequest;
import com.ticketing.domain.reservation.dto.ReservationResponse;
import com.ticketing.domain.reservation.lock.LockStrategy;
import com.ticketing.domain.reservation.lock.SeatHoldStrategy;
import com.ticketing.domain.seat.Seat;
import com.ticketing.domain.seat.SeatRepository;
import com.ticketing.domain.seat.ScheduleRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReservationService {

    private static final int EXPIRY_BATCH = 200;

    private final Map<LockStrategy, SeatHoldStrategy> strategies = new EnumMap<>(LockStrategy.class);
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReservationProperties properties;
    private final Clock clock;

    public ReservationService(
            List<SeatHoldStrategy> seatHoldStrategies,
            ReservationRepository reservationRepository,
            SeatRepository seatRepository,
            ScheduleRepository scheduleRepository,
            ReservationProperties properties,
            Clock clock) {
        seatHoldStrategies.forEach(s -> this.strategies.put(s.id(), s));
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.scheduleRepository = scheduleRepository;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Holds a seat and opens a PENDING reservation. {@code overrideStrategy} lets a caller
     * (load test / benchmark) pick the concurrency-control scheme per request; production
     * traffic uses {@code app.reservation.default-lock-strategy}.
     */
    public ReservationResponse hold(
            Long memberId, HoldReservationRequest request, LockStrategy overrideStrategy) {
        if (!scheduleRepository.existsById(request.scheduleId())) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
        }
        LockStrategy chosen =
                overrideStrategy != null ? overrideStrategy : properties.defaultLockStrategy();
        Reservation reservation =
                strategies.get(chosen).hold(memberId, request.scheduleId(), request.seatId());
        log.info(
                "reservation {} held via {} (member {}, seat {})",
                reservation.getId(),
                chosen,
                memberId,
                request.seatId());
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(Long memberId, Long reservationId) {
        return ReservationResponse.from(loadOwned(memberId, reservationId));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMine(Long memberId) {
        return reservationRepository.findByMemberIdOrderByIdDesc(memberId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse confirm(Long memberId, Long reservationId) {
        Reservation reservation = loadOwned(memberId, reservationId);
        reservation.confirm(clock.instant());
        seat(reservation.getSeatId()).markSold();
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse cancel(Long memberId, Long reservationId) {
        Reservation reservation = loadOwned(memberId, reservationId);
        reservation.cancel();
        seat(reservation.getSeatId()).release();
        return ReservationResponse.from(reservation);
    }

    /** Sweeps expired holds back to AVAILABLE. Invoked by {@code ReservationExpiryScheduler}. */
    @Transactional
    public int expireOverdue() {
        Instant now = clock.instant();
        List<Reservation> overdue =
                reservationRepository.findByStatusAndHoldExpiresAtBefore(
                        ReservationStatus.PENDING, now, Limit.of(EXPIRY_BATCH));
        for (Reservation reservation : overdue) {
            reservation.expire();
            seatRepository
                    .findById(reservation.getSeatId())
                    .ifPresent(Seat::release);
        }
        if (!overdue.isEmpty()) {
            log.info("expired {} overdue reservation(s)", overdue.size());
        }
        return overdue.size();
    }

    private Reservation loadOwned(Long memberId, Long reservationId) {
        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!reservation.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.RESERVATION_FORBIDDEN);
        }
        return reservation;
    }

    private Seat seat(Long seatId) {
        return seatRepository
                .findById(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
    }
}
