package com.ticketing.domain.reservation;

import com.ticketing.domain.seat.Seat;
import com.ticketing.domain.seat.SeatGradeRepository;
import com.ticketing.domain.seat.SeatRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The transactional core shared by every {@link com.ticketing.domain.reservation.lock.SeatHoldStrategy}:
 * flip the seat to HELD and persist a PENDING reservation, atomically. The only thing that varies
 * between strategies is how the seat row is read ({@code pessimistic} = {@code SELECT ... FOR UPDATE}).
 */
@Component
@RequiredArgsConstructor
public class ReservationCreator {

    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationProperties properties;
    private final Clock clock;

    @Transactional
    public Reservation create(Long memberId, Long scheduleId, Long seatId, boolean pessimistic) {
        Seat seat =
                (pessimistic
                        ? seatRepository.findForUpdate(seatId, scheduleId)
                        : seatRepository.findByIdAndScheduleId(seatId, scheduleId))
                        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        seat.hold(); // AVAILABLE -> HELD, or throws SEAT_NOT_AVAILABLE

        long price =
                seatGradeRepository
                        .findById(seat.getSeatGradeId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND))
                        .getPrice();

        Instant now = clock.instant();
        Reservation reservation =
                Reservation.pending(
                        memberId, scheduleId, seatId, price, now.plus(properties.holdDuration()));
        return reservationRepository.save(reservation);
    }
}
