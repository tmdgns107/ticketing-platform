package com.ticketing.domain.reservation;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberIdOrderByIdDesc(Long memberId);

    boolean existsBySeatIdAndStatus(Long seatId, ReservationStatus status);

    List<Reservation> findByStatusAndHoldExpiresAtBefore(
            ReservationStatus status, Instant threshold, Limit limit);
}
