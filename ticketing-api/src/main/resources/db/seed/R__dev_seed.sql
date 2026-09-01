-- Repeatable dev seed. Loaded only when `db/seed` is on spring.flyway.locations
-- (local profile). Must stay idempotent: re-runs whenever this file's checksum changes.

INSERT INTO performance (id, title, venue, opens_at, starts_at, status, created_at, updated_at)
VALUES (1, '2026 Spring Arena Tour - Seoul', 'KSPO Dome', '2026-09-10 20:00:00', '2026-10-01 19:00:00', 'ON_SALE', NOW(6), NOW(6)),
       (2, 'Indie Night Vol.7', 'Rolling Hall', '2026-09-15 12:00:00', '2026-09-28 18:00:00', 'SCHEDULED', NOW(6), NOW(6)),
       (3, 'Classical Gala 2026', 'Lotte Concert Hall', '2026-08-01 10:00:00', '2026-09-05 20:00:00', 'SOLD_OUT', NOW(6), NOW(6)) AS new
ON DUPLICATE KEY UPDATE title      = new.title,
                        venue      = new.venue,
                        opens_at   = new.opens_at,
                        starts_at  = new.starts_at,
                        status     = new.status,
                        updated_at = NOW(6);

-- One schedule + two grades for performance 1.
INSERT INTO schedule (id, performance_id, starts_at, created_at, updated_at)
VALUES (1, 1, '2026-10-01 19:00:00', NOW(6), NOW(6)) AS new
ON DUPLICATE KEY UPDATE starts_at = new.starts_at, updated_at = NOW(6);

INSERT INTO seat_grade (id, performance_id, name, price, created_at, updated_at)
VALUES (1, 1, 'VIP', 165000, NOW(6), NOW(6)),
       (2, 1, 'R', 132000, NOW(6), NOW(6)) AS new
ON DUPLICATE KEY UPDATE name = new.name, price = new.price, updated_at = NOW(6);

-- 80 seats for schedule 1: VIP rows A-B x 10, R rows A-D x 15.
INSERT INTO seat (schedule_id, seat_grade_id, section, row_label, seat_no, status, version, created_at, updated_at)
WITH RECURSIVE nums (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM nums WHERE n < 80)
SELECT 1,
       IF(n <= 20, 1, 2)                                              AS seat_grade_id,
       IF(n <= 20, 'VIP', 'R')                                        AS section,
       IF(n <= 20,
          CHAR(64 + CEIL(n / 10.0) USING utf8mb4),
          CHAR(64 + CEIL((n - 20) / 15.0) USING utf8mb4))             AS row_label,
       IF(n <= 20, ((n - 1) % 10) + 1, ((n - 21) % 15) + 1)           AS seat_no,
       'AVAILABLE',
       0,
       NOW(6),
       NOW(6)
FROM nums
ON DUPLICATE KEY UPDATE updated_at = NOW(6);
