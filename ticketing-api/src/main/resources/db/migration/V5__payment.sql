CREATE TABLE payment
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id    BIGINT      NOT NULL,
    member_id         BIGINT      NOT NULL,
    amount            BIGINT      NOT NULL,
    status            VARCHAR(20) NOT NULL,
    idempotency_key   VARCHAR(80) NOT NULL,
    pg_transaction_id VARCHAR(80) NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_idempotency_key (idempotency_key),
    KEY idx_payment_reservation (reservation_id),
    KEY idx_payment_member (member_id, id),
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
