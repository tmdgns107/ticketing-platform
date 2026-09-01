CREATE TABLE schedule
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    performance_id BIGINT      NOT NULL,
    starts_at      DATETIME(6) NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_schedule_performance (performance_id, starts_at),
    CONSTRAINT fk_schedule_performance FOREIGN KEY (performance_id) REFERENCES performance (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE seat_grade
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    performance_id BIGINT       NOT NULL,
    name           VARCHAR(30)  NOT NULL,
    price          BIGINT       NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_seat_grade_performance (performance_id),
    CONSTRAINT fk_seat_grade_performance FOREIGN KEY (performance_id) REFERENCES performance (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE seat
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_id   BIGINT      NOT NULL,
    seat_grade_id BIGINT      NOT NULL,
    section       VARCHAR(20) NOT NULL,
    row_label     VARCHAR(10) NOT NULL,
    seat_no       INT         NOT NULL,
    status        VARCHAR(20) NOT NULL,
    version       BIGINT      NOT NULL DEFAULT 0,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_seat_position (schedule_id, section, row_label, seat_no),
    KEY idx_seat_schedule_status (schedule_id, status),
    CONSTRAINT fk_seat_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_seat_grade FOREIGN KEY (seat_grade_id) REFERENCES seat_grade (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
