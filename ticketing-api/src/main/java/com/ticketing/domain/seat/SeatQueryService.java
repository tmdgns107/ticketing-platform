package com.ticketing.domain.seat;

import com.ticketing.domain.seat.dto.ScheduleResponse;
import com.ticketing.domain.seat.dto.SeatMapResponse;
import com.ticketing.domain.seat.dto.SeatMapResponse.GradeView;
import com.ticketing.domain.seat.dto.SeatMapResponse.SeatView;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatQueryService {

    private final ScheduleRepository scheduleRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final SeatRepository seatRepository;

    public List<ScheduleResponse> getSchedules(Long performanceId) {
        return scheduleRepository.findByPerformanceIdOrderByStartsAt(performanceId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    /**
     * Full seat map for a schedule. This is a hot, cache-friendly read — phase 6 will
     * put a short-TTL cache in front of it.
     */
    public SeatMapResponse getSeatMap(Long scheduleId) {
        Schedule schedule =
                scheduleRepository
                        .findById(scheduleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<Seat> seats =
                seatRepository.findByScheduleIdOrderBySectionAscRowLabelAscSeatNoAsc(scheduleId);

        Map<Long, Long> totalByGrade =
                seats.stream()
                        .collect(Collectors.groupingBy(Seat::getSeatGradeId, Collectors.counting()));
        Map<Long, Long> availableByGrade =
                seats.stream()
                        .filter(Seat::isAvailable)
                        .collect(Collectors.groupingBy(Seat::getSeatGradeId, Collectors.counting()));

        List<GradeView> grades =
                seatGradeRepository.findByPerformanceIdOrderByPriceDesc(schedule.getPerformanceId())
                        .stream()
                        .map(
                                g ->
                                        GradeView.of(
                                                g,
                                                availableByGrade.getOrDefault(g.getId(), 0L),
                                                totalByGrade.getOrDefault(g.getId(), 0L)))
                        .toList();

        List<SeatView> seatViews = seats.stream().map(SeatView::from).toList();
        return new SeatMapResponse(scheduleId, grades, seatViews);
    }

    /** Convenience for other domains that just need a schedule to exist. */
    public Schedule getSchedule(Long scheduleId) {
        return scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }
}
