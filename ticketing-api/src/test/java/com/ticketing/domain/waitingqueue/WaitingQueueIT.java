package com.ticketing.domain.waitingqueue;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@IntegrationTest
class WaitingQueueIT {

  private static final long SCHEDULE = 777L;

  @Autowired WaitingQueueService queue;
  @Autowired StringRedisTemplate redis;

  @Test
  void promotesHeadOfQueueAndMintsAnEntryToken() {
    redis.delete(redis.keys("queue:*"));

    var s1 = queue.enter(SCHEDULE, 101L);
    var s2 = queue.enter(SCHEDULE, 102L);
    var s3 = queue.enter(SCHEDULE, 103L);
    assertThat(s1.position()).isEqualTo(1);
    assertThat(s3.position()).isEqualTo(3);

    int promoted = queue.promote(SCHEDULE);
    assertThat(promoted).isGreaterThanOrEqualTo(2);

    var afterFirst = queue.status(SCHEDULE, 101L);
    assertThat(afterFirst.admitted()).isTrue();
    assertThat(afterFirst.entryToken()).isNotBlank();

    var admission = queue.resolveEntryToken(afterFirst.entryToken()).orElseThrow();
    assertThat(admission.scheduleId()).isEqualTo(SCHEDULE);
    assertThat(admission.memberId()).isEqualTo(101L);
  }

  @Test
  void reEnteringDoesNotLosePosition() {
    redis.delete(redis.keys("queue:*"));
    queue.enter(SCHEDULE, 201L);
    queue.enter(SCHEDULE, 202L);

    var again = queue.enter(SCHEDULE, 201L);

    assertThat(again.position()).isEqualTo(1);
  }
}
