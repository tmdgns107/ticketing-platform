package com.ticketing.domain.payment.pg;

/** Payment-gateway boundary. The real impl would call an external PG over HTTP. */
public interface PgClient {

    /**
     * @param idempotencyKey forwarded to the PG so retries don't double-charge
     * @return an approval with the PG transaction id
     * @throws PgException if the PG declines or is unreachable
     */
    PgApproval approve(long amount, String idempotencyKey);

    void cancel(String pgTransactionId);

    record PgApproval(String transactionId) {}

    class PgException extends RuntimeException {
        public PgException(String message) {
            super(message);
        }
    }
}
