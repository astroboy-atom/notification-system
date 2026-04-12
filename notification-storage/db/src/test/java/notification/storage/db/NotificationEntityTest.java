package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationEntityTest {

    @Test
    @DisplayName("알림 엔티티를 생성하면 초기 상태로 설정한다.")
    void create() {
        Instant before = Instant.now();

        NotificationEntity notificationEntity = createNotification("notification-key");

        Instant after = Instant.now();

        assertThat(notificationEntity.getId()).isNull();
        assertThat(notificationEntity.getRecipientId()).isEqualTo(1L);
        assertThat(notificationEntity.getEventId()).isEqualTo(100L);
        assertThat(notificationEntity.getNotificationType()).isEqualTo(NotificationType.AFTER_PAID);
        assertThat(notificationEntity.getNotificationChanel()).isEqualTo(NotificationChanel.EMAIL);
        assertThat(notificationEntity.getNotificationKey()).isEqualTo("notification-key");
        assertThat(notificationEntity.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notificationEntity.getRetryCount()).isZero();
        assertThat(notificationEntity.getFailedReason()).isNull();
        assertThat(notificationEntity.getRequestedAt()).isBetween(before, after);
        assertThat(notificationEntity.getNextAttemptAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("완료 처리하면 상태가 DONE으로 바뀌고 실패 정보와 다음 재시도 시간이 제거된다.")
    void done() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markFailed("failed");

        notificationEntity.done();

        assertThat(notificationEntity.getNotificationStatus()).isEqualTo(NotificationStatus.DONE);
        assertThat(notificationEntity.getFailedReason()).isNull();
        assertThat(notificationEntity.getNextAttemptAt()).isNull();
    }

    @Test
    @DisplayName("재시도 횟수가 최대 미만이면 재시도할 수 있다.")
    void canRetry() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markRetry();
        notificationEntity.markRetry();

        boolean result = notificationEntity.canRetry();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("재시도 횟수가 최대치에 도달하면 더 이상 재시도할 수 없다.")
    void canNotRetryWhenReachedMaxRetryCount() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markRetry();
        notificationEntity.markRetry();
        notificationEntity.markRetry();

        boolean result = notificationEntity.canRetry();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("재시도 처리하면 횟수가 증가하고 상태가 PENDING으로 바뀌며 1초 뒤로 재시도 시간이 설정된다.")
    void markRetry() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markInProgress();
        Instant before = Instant.now();

        notificationEntity.markRetry();

        Instant after = Instant.now();

        assertThat(notificationEntity.getRetryCount()).isEqualTo(1);
        assertThat(notificationEntity.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notificationEntity.getNextAttemptAt())
                .isBetween(before.plusMillis(1000), after.plusMillis(1000));
    }

    @Test
    @DisplayName("재시도 횟수에 따라 다음 재시도 시간이 지수 백오프로 증가한다.")
    void markRetryWithExponentialBackoff() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markRetry();
        Instant before = Instant.now();

        notificationEntity.markRetry();

        Instant after = Instant.now();

        assertThat(notificationEntity.getRetryCount()).isEqualTo(2);
        assertThat(notificationEntity.getNextAttemptAt())
                .isBetween(before.plusMillis(2000), after.plusMillis(2000));
    }

    @Test
    @DisplayName("실패 처리하면 상태가 FAILED로 바뀌고 실패 사유를 저장하며 다음 재시도 시간을 제거한다.")
    void markFailed() {
        NotificationEntity notificationEntity = createNotification("notification-key");
        notificationEntity.markRetry();

        notificationEntity.markFailed("failed reason");

        assertThat(notificationEntity.getNotificationStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notificationEntity.getFailedReason()).isEqualTo("failed reason");
        assertThat(notificationEntity.getNextAttemptAt()).isNull();
    }

    @Test
    @DisplayName("진행 중 처리하면 상태가 IN_PROGRESS로 바뀐다.")
    void markInProgress() {
        NotificationEntity notificationEntity = createNotification("notification-key");

        notificationEntity.markInProgress();

        assertThat(notificationEntity.getNotificationStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
    }

    private NotificationEntity createNotification(String notificationKey) {
        return new NotificationEntity(
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey
        );
    }
}
