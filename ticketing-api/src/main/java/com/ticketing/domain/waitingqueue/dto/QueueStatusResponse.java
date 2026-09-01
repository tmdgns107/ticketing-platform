package com.ticketing.domain.waitingqueue.dto;

public record QueueStatusResponse(
        long scheduleId,
        boolean admitted,
        /** 1-based position while waiting; null once admitted or not in queue. */
        Long position,
        /** present only when admitted — send as X-Queue-Token on the reservation call. */
        String entryToken) {

    public static QueueStatusResponse waiting(long scheduleId, long position) {
        return new QueueStatusResponse(scheduleId, false, position, null);
    }

    public static QueueStatusResponse admitted(long scheduleId, String entryToken) {
        return new QueueStatusResponse(scheduleId, true, null, entryToken);
    }

    public static QueueStatusResponse notInQueue(long scheduleId) {
        return new QueueStatusResponse(scheduleId, false, null, null);
    }
}
