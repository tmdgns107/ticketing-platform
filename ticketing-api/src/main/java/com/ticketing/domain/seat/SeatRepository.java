package com.ticketing.domain.seat;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(Long scheduleId);

    long countByScheduleIdAndStatus(Long scheduleId, SeatStatus status);

    /** Plain read — used by the optimistic-locking strategy (phase 3). */
    Optional<Seat> findByIdAndScheduleId(Long id, Long scheduleId);

    /** SELECT ... FOR UPDATE — used by the pessimistic-locking strategy (phase 3). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id and s.scheduleId = :scheduleId")
    Optional<Seat> findForUpdate(@Param("id") Long id, @Param("scheduleId") Long scheduleId);
}
