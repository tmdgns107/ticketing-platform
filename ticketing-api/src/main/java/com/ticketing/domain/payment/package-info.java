/**
 * 결제 도메인.
 * 외부 PG 연동(모의), 멱등키(Idempotency-Key) 기반 중복 결제 방지, 결제 취소/환불.
 * TODO: Payment 엔티티, PaymentService, PgClient(mock), 멱등성 저장소, PaymentController.
 */
package com.ticketing.domain.payment;
