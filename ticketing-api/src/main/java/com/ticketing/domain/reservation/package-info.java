/**
 * 예매 도메인.
 * 좌석 선점 → 예매 생성(PENDING) → 결제 완료 시 CONFIRMED, 미결제 시 만료/취소.
 * 동시성 제어(비관적 락 / 낙관적 락 / 분산 락) 비교 실험의 중심이 되는 패키지.
 * TODO: Reservation 엔티티, ReservationService, 만료 스케줄러, ReservationController.
 */
package com.ticketing.domain.reservation;
