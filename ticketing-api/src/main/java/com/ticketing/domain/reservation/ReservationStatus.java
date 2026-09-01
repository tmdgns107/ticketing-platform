package com.ticketing.domain.reservation;

public enum ReservationStatus {
    /** seat held, awaiting payment */
    PENDING,
    /** paid — terminal */
    CONFIRMED,
    /** released by the user before payment — terminal */
    CANCELLED,
    /** hold window elapsed without payment — terminal */
    EXPIRED
}
