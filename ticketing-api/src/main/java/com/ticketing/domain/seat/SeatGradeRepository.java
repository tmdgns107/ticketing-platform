package com.ticketing.domain.seat;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {

    List<SeatGrade> findByPerformanceIdOrderByPriceDesc(Long performanceId);
}
