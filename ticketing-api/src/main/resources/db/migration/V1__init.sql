-- Baseline schema for the ticketing platform.
-- Only the `performance` table is materialised so far; other domains land in later migrations.

CREATE TABLE performance
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL,
    venue      VARCHAR(100),
    opens_at   DATETIME(6)  NOT NULL,
    starts_at  DATETIME(6)  NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_performance_opens_at (opens_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
