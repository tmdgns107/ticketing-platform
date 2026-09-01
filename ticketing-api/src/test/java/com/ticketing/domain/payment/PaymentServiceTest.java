package com.ticketing.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ticketing.domain.payment.pg.PgClient;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock PaymentProcessor processor;
  @Mock PaymentRepository paymentRepository;
  @Mock PgClient pgClient;

  @InjectMocks PaymentService paymentService;

  @Test
  void rejectsMissingIdempotencyKey() {
    assertThatThrownBy(() -> paymentService.pay(1L, 1L, "  "))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
  }

  @Test
  void replaysAlreadyCompletedPaymentWithoutCallingPg() {
    Payment paid = Payment.pending(1L, 1L, 1000L, "key-1");
    paid.markPaid("pg_x");
    given(processor.createOrGet(1L, 1L, "key-1")).willReturn(paid);

    var response = paymentService.pay(1L, 1L, "key-1");

    assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
    verify(pgClient, never()).approve(anyLong(), anyString());
  }

  @Test
  void marksPaymentFailedWhenPgDeclines() {
    Payment pending = Payment.pending(1L, 1L, 1000L, "key-2");
    setId(pending, 9L);
    given(processor.createOrGet(1L, 1L, "key-2")).willReturn(pending);
    given(pgClient.approve(anyLong(), any())).willThrow(new PgClient.PgException("declined"));

    assertThatThrownBy(() -> paymentService.pay(1L, 1L, "key-2"))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PAYMENT_FAILED);

    verify(processor).applyFailure(9L);
    verify(processor, never()).applyApproval(anyLong(), anyString());
  }

  private static void setId(Payment payment, long id) {
    org.springframework.test.util.ReflectionTestUtils.setField(payment, "id", id);
  }
}
