package com.ticketing.domain.waitingqueue;

import com.ticketing.domain.waitingqueue.dto.QueueStatusResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

/**
 * Per-schedule waiting room backed by Redis. Members join a sorted set (score = enqueue time);
 * a scheduler promotes the head of the queue into a TTL-bounded "active" state and mints a
 * short-lived entry token that the reservation endpoint requires.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private static final String SCHEDULES_KEY = "queue:schedules";

    private final StringRedisTemplate redis;
    private final WaitingQueueProperties properties;

    public QueueStatusResponse enter(long scheduleId, long memberId) {
        Optional<String> activeToken = activeToken(scheduleId, memberId);
        if (activeToken.isPresent()) {
            return QueueStatusResponse.admitted(scheduleId, activeToken.get());
        }
        String waitKey = WaitingQueueKeys.waitZset(scheduleId);
        String member = String.valueOf(memberId);
        Double score = redis.opsForZSet().score(waitKey, member);
        if (score == null) {
            redis.opsForZSet().add(waitKey, member, Instant.now().toEpochMilli());
            redis.opsForSet().add(SCHEDULES_KEY, String.valueOf(scheduleId));
        }
        return status(scheduleId, memberId);
    }

    public QueueStatusResponse status(long scheduleId, long memberId) {
        Optional<String> activeToken = activeToken(scheduleId, memberId);
        if (activeToken.isPresent()) {
            return QueueStatusResponse.admitted(scheduleId, activeToken.get());
        }
        Long rank = redis.opsForZSet().rank(WaitingQueueKeys.waitZset(scheduleId), String.valueOf(memberId));
        return rank == null
                ? QueueStatusResponse.notInQueue(scheduleId)
                : QueueStatusResponse.waiting(scheduleId, rank + 1);
    }

    /** Promotes up to {@code promoteBatchSize} members for every schedule with an active queue. */
    public int promoteDue() {
        Set<String> scheduleIds = redis.opsForSet().members(SCHEDULES_KEY);
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return 0;
        }
        int promoted = 0;
        for (String raw : scheduleIds) {
            long scheduleId = Long.parseLong(raw);
            promoted += promote(scheduleId);
            Long remaining = redis.opsForZSet().size(WaitingQueueKeys.waitZset(scheduleId));
            if (remaining == null || remaining == 0L) {
                redis.opsForSet().remove(SCHEDULES_KEY, raw);
            }
        }
        if (promoted > 0) {
            log.info("promoted {} member(s) from waiting queues", promoted);
        }
        return promoted;
    }

    int promote(long scheduleId) {
        Set<TypedTuple<String>> head =
                redis.opsForZSet().popMin(WaitingQueueKeys.waitZset(scheduleId), properties.promoteBatchSize());
        if (head == null || head.isEmpty()) {
            return 0;
        }
        for (TypedTuple<String> tuple : head) {
            String member = tuple.getValue();
            if (member == null) {
                continue;
            }
            long memberId = Long.parseLong(member);
            String token = UUID.randomUUID().toString();
            redis.opsForValue()
                    .set(WaitingQueueKeys.active(scheduleId, memberId), token, properties.activeTtl());
            redis.opsForValue()
                    .set(
                            WaitingQueueKeys.entry(token),
                            scheduleId + ":" + memberId,
                            properties.entryTokenTtl());
        }
        return head.size();
    }

    /** Resolves an {@code X-Queue-Token} to its admission, if still valid. */
    public Optional<Admission> resolveEntryToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String value = redis.opsForValue().get(WaitingQueueKeys.entry(token));
        if (value == null) {
            return Optional.empty();
        }
        int sep = value.indexOf(':');
        return Optional.of(
                new Admission(
                        Long.parseLong(value.substring(0, sep)), Long.parseLong(value.substring(sep + 1))));
    }

    private Optional<String> activeToken(long scheduleId, long memberId) {
        return Optional.ofNullable(redis.opsForValue().get(WaitingQueueKeys.active(scheduleId, memberId)));
    }

    public record Admission(long scheduleId, long memberId) {}
}
