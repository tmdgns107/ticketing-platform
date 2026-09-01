package com.ticketing.domain.seat;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByPerformanceIdOrderByStartsAt(Long performanceId);
}
