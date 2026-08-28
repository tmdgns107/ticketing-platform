/**
 * 좌석 도메인.
 * 공연 회차(Schedule)별 좌석과 등급, 좌석 재고를 관리한다.
 * 좌석 선점은 Redis(짧은 TTL)로 처리하고, 확정 시 DB 상태를 갱신한다.
 * TODO: Schedule/Seat/SeatGrade 엔티티, SeatHoldService(Redis), SeatQueryService.
 */
package com.ticketing.domain.seat;
