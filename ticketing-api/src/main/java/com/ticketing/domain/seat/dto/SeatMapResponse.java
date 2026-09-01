package com.ticketing.domain.seat.dto;

import com.ticketing.domain.seat.Seat;
import com.ticketing.domain.seat.SeatGrade;
import com.ticketing.domain.seat.SeatStatus;
import java.util.List;

public record SeatMapResponse(
        Long scheduleId,
        List<GradeView> grades,
        List<SeatView> seats
) {

    public record GradeView(Long id, String name, long price, long availableCount, long totalCount) {
        public static GradeView of(SeatGrade grade, long available, long total) {
            return new GradeView(grade.getId(), grade.getName(), grade.getPrice(), available, total);
        }
    }

    public record SeatView(
            Long id, String section, String rowLabel, int seatNo, Long gradeId, SeatStatus status) {
        public static SeatView from(Seat seat) {
            return new SeatView(
                    seat.getId(),
                    seat.getSection(),
                    seat.getRowLabel(),
                    seat.getSeatNo(),
                    seat.getSeatGradeId(),
                    seat.getStatus());
        }
    }
}
