SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS member_entity
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    email                VARCHAR(255) NOT NULL,
    in_app_token         VARCHAR(255) NOT NULL,
    is_agree_in_app_push BIT(1)       NOT NULL,
    is_agree_email       BIT(1)       NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS event_entity
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    contents VARCHAR(255) NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_entity
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id        BIGINT                                            NOT NULL,
    event_id            BIGINT                                            NOT NULL,
    notification_type   ENUM ('AFTER_PAID')                               NOT NULL,
    notification_chanel ENUM ('EMAIL', 'IN_APP')                          NOT NULL,
    notification_key    VARCHAR(255)                                      NOT NULL UNIQUE,
    notification_status ENUM ('PENDING', 'IN_PROGRESS', 'DONE', 'FAILED') NOT NULL,
    retry_count         INT                                               NOT NULL,
    failed_reason       VARCHAR(255)                                      NULL,
    requested_at        DATETIME(6)                                       NOT NULL,
    next_attempt_at     DATETIME(6)                                       NOT NULL,
    last_claimed_at     DATETIME(6)                                       NULL,
    is_read             BIT(1)                                            NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO member_entity (id, email, in_app_token, is_agree_in_app_push, is_agree_email)
VALUES (1, 'dummy1@notification.local', 'dummy-in-app-token-1', true, true),
       (2, 'dummy2@notification.local', 'dummy-in-app-token-2', true, true),
       (3, 'dummy3@notification.local', 'dummy-in-app-token-3', true, true),
       (4, 'dummy4@notification.local', 'dummy-in-app-token-4', true, true),
       (5, 'dummy5@notification.local', 'dummy-in-app-token-5', true, true),
       (6, 'dummy6@notification.local', 'dummy-in-app-token-6', true, true),
       (7, 'dummy7@notification.local', 'dummy-in-app-token-7', true, true),
       (8, 'dummy8@notification.local', 'dummy-in-app-token-8', true, true),
       (9, 'dummy9@notification.local', 'dummy-in-app-token-9', true, true),
       (10, 'dummy10@notification.local', 'dummy-in-app-token-10', true, true);

INSERT INTO event_entity (id, contents)
VALUES (1, '더미 이벤트 1'),
       (2, '더미 이벤트 2'),
       (3, '더미 이벤트 3'),
       (4, '더미 이벤트 4'),
       (5, '더미 이벤트 5'),
       (6, '더미 이벤트 6'),
       (7, '더미 이벤트 7'),
       (8, '더미 이벤트 8'),
       (9, '더미 이벤트 9'),
       (10, '더미 이벤트 10');
