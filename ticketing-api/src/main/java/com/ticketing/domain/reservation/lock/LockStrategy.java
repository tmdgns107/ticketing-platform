package com.ticketing.domain.reservation.lock;

/** Concurrency-control strategy used to serialise holds on a single seat. */
public enum LockStrategy {
    /** SELECT ... FOR UPDATE — the DB row lock blocks contenders until commit. */
    PESSIMISTIC,
    /** @Version check on commit; losers retry a bounded number of times. */
    OPTIMISTIC,
    /** Redis SET NX lock around a plain-read transaction. */
    DISTRIBUTED
}
