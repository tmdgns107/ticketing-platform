CREATE TABLE member
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(50)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
