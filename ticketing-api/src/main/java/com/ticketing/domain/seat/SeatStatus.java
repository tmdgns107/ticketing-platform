package com.ticketing.domain.seat;

public enum SeatStatus {
    /** open for holding */
    AVAILABLE,
    /** temporarily held during a reservation attempt */
    HELD,
    /** paid for — terminal */
    SOLD
}
