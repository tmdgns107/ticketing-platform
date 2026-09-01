package com.ticketing.domain.waitingqueue;

/** Redis key layout for the per-schedule waiting queue. */
final class WaitingQueueKeys {

    private WaitingQueueKeys() {}

    /** ZSET of waiting members, score = enqueue epoch millis. */
    static String waitZset(long scheduleId) {
        return "queue:wait:" + scheduleId;
    }

    /** STRING per admitted member, TTL-bounded. Value = entry token. */
    static String active(long scheduleId, long memberId) {
        return "queue:active:" + scheduleId + ":" + memberId;
    }

    /** STRING per entry token, TTL-bounded. Value = "{scheduleId}:{memberId}". */
    static String entry(String token) {
        return "queue:entry:" + token;
    }
}
