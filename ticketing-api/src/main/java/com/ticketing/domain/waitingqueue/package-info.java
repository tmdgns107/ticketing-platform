/**
 * 대기열 도메인.
 * 오픈 시점 순간 트래픽을 흡수하기 위한 입장 대기열.
 * Redis Sorted Set 기반으로 대기 순번을 관리하고, 일정 주기마다 상위 N명을
 * "입장 허용(active)" 상태로 승격시킨다. 예매 API는 active 토큰을 요구한다.
 * TODO: WaitingQueueService(등록/조회/승격), 스케줄러, 입장 토큰 검증 인터셉터.
 */
package com.ticketing.domain.waitingqueue;
