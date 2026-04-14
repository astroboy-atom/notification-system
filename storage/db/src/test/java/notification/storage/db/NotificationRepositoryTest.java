package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import notification.enums.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationRepositoryTest extends IntegrationTestSupport {

    @Test
    @DisplayName("키에 해당하는 알림이 존재하면 true를 반환한다.")
    void existsByNotificationKey() {
        String notificationKey = "1:100:after_paid:email";

        NotificationEntity notification = createNotification(notificationKey);
        notificationRepository.save(notification);

        boolean exists = notificationRepository.existsByNotificationKey(notificationKey);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("키에 해당하는 알림이 존재하지 않으면 false를 반환한다.")
    void notExistsByNotificationKey() {
        boolean exists = notificationRepository.existsByNotificationKey("missing-key");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("상태에 해당하는 알림만 조회한다.")
    void findAllByNotificationStatusForUpdateSkipLocked_filtersByStatus() {
        NotificationEntity pending = notificationRepository.save(createNotification("pending-key"));
        NotificationEntity done = notificationRepository.save(createNotification("done-key"));
        done.done();
        notificationRepository.save(done);

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                10
        );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(pending.getId());
    }

    @Test
    @DisplayName("batch size만큼 알림을 조회한다.")
    void findAllByNotificationStatusForUpdateSkipLocked_respectsBatchSize() {
        NotificationEntity first = notificationRepository.save(createNotification("key-1"));
        NotificationEntity second = notificationRepository.save(createNotification("key-2"));
        notificationRepository.save(createNotification("key-3"));

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                2
        );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("조건에 맞는 알림이 없으면 빈 컬렉션을 반환한다.")
    void findAllByNotificationStatusForUpdateSkipLocked_returnsEmptyCollection() {
        NotificationEntity done = notificationRepository.save(createNotification("done-key"));
        done.done();
        notificationRepository.save(done);

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                10
        );

        assertThat(notifications).isEmpty();
    }

    @Test
    @DisplayName("nextAttemptAt이 null인 알림도 조회한다.")
    void findAllByNotificationStatusForUpdateSkipLocked_includesNullNextAttemptAt() {
        NotificationEntity pending = notificationRepository.save(createPendingNotification("pending-key", null));

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                10
        );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(pending.getId());
    }

    @Test
    @DisplayName("nextAttemptAt이 지나지 않은 알림은 조회하지 않는다.")
    void findAllByNotificationStatusForUpdateSkipLocked_excludesFutureNextAttemptAt() {
        Instant now = Instant.now();
        NotificationEntity past = notificationRepository.save(createPendingNotification("past-key", now.minusSeconds(1)));
        notificationRepository.save(createPendingNotification("future-key", now.plusSeconds(30)));

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                10
        );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(past.getId());
    }

    @Test
    @DisplayName("일정 시간 지난 IN_PROGRESS 상태의 알림만 조회한다.")
    void findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked() {
        Instant now = Instant.now();
        NotificationEntity stale = notificationRepository.save(createInProgressNotification("stale-key", now.minusSeconds(31)));
        notificationRepository.save(createInProgressNotification("recent-key", now.minusSeconds(10)));

        List<NotificationEntity> notifications =
                notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                        NotificationStatus.IN_PROGRESS.name(),
                        now.minusSeconds(30),
                        10
                );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(stale.getId());
    }

    @Test
    @DisplayName("일정 시간 지난 IN_PROGRESS 상태의 알림만 batch size만큼 조회한다.")
    void findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked_respectsBatchSize() {
        Instant now = Instant.now();
        NotificationEntity first = notificationRepository.save(createInProgressNotification("key-1", now.minusSeconds(31)));
        NotificationEntity second = notificationRepository.save(createInProgressNotification("key-2", now.minusSeconds(32)));
        notificationRepository.save(createInProgressNotification("key-3", now.minusSeconds(33)));

        List<NotificationEntity> notifications =
                notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                        NotificationStatus.IN_PROGRESS.name(),
                        now.minusSeconds(30),
                        2
                );

        assertThat(notifications)
                .extracting(NotificationEntity::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("조건에 맞는 IN_PROGRESS 알림이 없으면 빈 컬렉션을 반환한다.")
    void findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked_returnsEmptyCollection() {
        Instant now = Instant.now();
        notificationRepository.save(createInProgressNotification("recent-key", now.minusSeconds(10)));
        notificationRepository.save(createNotification("pending-key"));

        List<NotificationEntity> notifications =
                notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                        NotificationStatus.IN_PROGRESS.name(),
                        now.minusSeconds(30),
                        10
                );

        assertThat(notifications).isEmpty();
    }

    private NotificationEntity createInProgressNotification(String notificationKey, Instant lastClaimedAt) {
        return new NotificationEntity(
                null,
                1L,
                100L,
                notification.enums.NotificationType.AFTER_PAID,
                notification.enums.NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.IN_PROGRESS,
                0,
                null,
                Instant.now(),
                Instant.now(),
                lastClaimedAt
        );
    }

    private NotificationEntity createPendingNotification(String notificationKey, Instant nextAttemptAt) {
        return new NotificationEntity(
                null,
                1L,
                100L,
                notification.enums.NotificationType.AFTER_PAID,
                notification.enums.NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now(),
                nextAttemptAt,
                null
        );
    }
}
