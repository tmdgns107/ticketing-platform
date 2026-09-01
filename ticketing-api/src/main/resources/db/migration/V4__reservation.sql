CREATE TABLE reservation
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    member_id       BIGINT      NOT NULL,
    schedule_id     BIGINT      NOT NULL,
    seat_id         BIGINT      NOT NULL,
    price           BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL,
    hold_expires_at DATETIME(6) NOT NULL,
    confirmed_at    DATETIME(6) NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_reservation_member (member_id, id),
    KEY idx_reservation_sweep (status, hold_expires_at),
    KEY idx_reservation_seat (seat_id),
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES seat (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
