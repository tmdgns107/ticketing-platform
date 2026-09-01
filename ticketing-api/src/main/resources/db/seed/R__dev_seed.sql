-- Repeatable dev seed. Loaded only when `db/seed` is on spring.flyway.locations
-- (local profile). Must stay idempotent: re-runs whenever this file's checksum changes.

INSERT INTO performance (id, title, venue, opens_at, starts_at, status, created_at, updated_at)
VALUES (1, '2026 Spring Arena Tour - Seoul', 'KSPO Dome', '2026-09-10 20:00:00', '2026-10-01 19:00:00', 'ON_SALE', NOW(6), NOW(6)),
       (2, 'Indie Night Vol.7', 'Rolling Hall', '2026-09-15 12:00:00', '2026-09-28 18:00:00', 'SCHEDULED', NOW(6), NOW(6)),
       (3, 'Classical Gala 2026', 'Lotte Concert Hall', '2026-08-01 10:00:00', '2026-09-05 20:00:00', 'SOLD_OUT', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE title      = VALUES(title),
                        venue      = VALUES(venue),
                        opens_at   = VALUES(opens_at),
                        starts_at  = VALUES(starts_at),
                        status     = VALUES(status),
                        updated_at = NOW(6);
