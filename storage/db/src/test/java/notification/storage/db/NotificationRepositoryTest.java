package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import notification.enums.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class NotificationRepositoryTest extends IntegrationTestSupport {

    @Test
    @DisplayName("ID에 해당하는 알림이 존재하면 반환한다.")
    void findByIdOrThrowException() {
        NotificationEntity saved = notificationRepository.save(createNotification("notification-key"));

        NotificationEntity result = notificationRepository.findByIdOrThrowException(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getRecipientId()).isEqualTo(1L);
        assertThat(result.getEventId()).isEqualTo(100L);
        assertThat(result.getNotificationKey()).isEqualTo("notification-key");
        assertThat(result.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("ID에 해당하는 알림이 없으면 예외가 발생한다.")
    void findByIdOrThrowException_throwsException() {
        assertThatThrownBy(() -> notificationRepository.findByIdOrThrowException(Long.MAX_VALUE))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 알림입니다.");
    }

    @Test
    @DisplayName("수신자와 읽음 여부로 알림 페이지를 조회한다.")
    void findAllByRecipientIdAndIsRead() {
        NotificationEntity unread = notificationRepository.save(createNotificationForPage("unread-key", 1L, false));
        notificationRepository.save(createNotificationForPage("read-key", 1L, true));
        notificationRepository.save(createNotificationForPage("other-user-key", 2L, false));

        org.springframework.data.domain.Page<NotificationEntity> result =
                notificationRepository.findAllByRecipientIdAndIsRead(
                        1L,
                        false,
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
                );

        assertThat(result.getContent())
                .extracting(NotificationEntity::getId)
                .containsExactly(unread.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("수신자와 읽음 여부 조건의 알림 페이지 조회는 pageable을 따른다.")
    void findAllByRecipientIdAndIsRead_respectsPageable() {
        notificationRepository.save(createNotificationForPage("key-1", 1L, false));
        notificationRepository.save(createNotificationForPage("key-2", 1L, false));
        NotificationEntity third = notificationRepository.save(createNotificationForPage("key-3", 1L, false));

        org.springframework.data.domain.Page<NotificationEntity> result =
                notificationRepository.findAllByRecipientIdAndIsRead(
                        1L,
                        false,
                        PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"))
                );

        assertThat(result.getContent())
                .extracting(NotificationEntity::getId)
                .containsExactly(third.getId());
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("수신자와 읽음 여부 조건의 첫 페이지 조회는 마지막 페이지가 아니다.")
    void findAllByRecipientIdAndIsRead_firstPageMetadata() {
        NotificationEntity first = notificationRepository.save(createNotificationForPage("key-1", 1L, false));
        NotificationEntity second = notificationRepository.save(createNotificationForPage("key-2", 1L, false));
        NotificationEntity third = notificationRepository.save(createNotificationForPage("key-3", 1L, false));

        org.springframework.data.domain.Page<NotificationEntity> firstPage =
                notificationRepository.findAllByRecipientIdAndIsRead(
                        1L,
                        false,
                        PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"))
                );

        org.springframework.data.domain.Page<NotificationEntity> secondPage =
                notificationRepository.findAllByRecipientIdAndIsRead(
                        1L,
                        false,
                        PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"))
                );

        assertThat(firstPage.getContent())
                .extracting(NotificationEntity::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.isLast()).isFalse();
        assertThat(firstPage.getContent())
                .extracting(NotificationEntity::getId)
                .doesNotContain(third.getId());

        assertThat(secondPage.getContent())
                .extracting(NotificationEntity::getId)
                .containsExactly(third.getId());
        assertThat(secondPage.getTotalElements()).isEqualTo(3);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.isLast()).isTrue();
    }

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
    @DisplayName("nextAttemptAt이 지난 알림은 조회한다.")
    void findAllByNotificationStatusForUpdateSkipLocked_includesDueNextAttemptAt() {
        Instant now = Instant.now();
        NotificationEntity pending = notificationRepository.save(createPendingNotification("pending-key", now.minusSeconds(1)));

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
    @DisplayName("nextAttemptAt이 아직 도래하지 않은 알림만 있으면 조회하지 않는다.")
    void findAllByNotificationStatusForUpdateSkipLocked_returnsEmptyWhenAllNextAttemptAtAreFuture() {
        Instant now = Instant.now();
        notificationRepository.save(createPendingNotification("future-key-1", now.plusSeconds(10)));
        notificationRepository.save(createPendingNotification("future-key-2", now.plusSeconds(20)));

        List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                10
        );

        assertThat(notifications).isEmpty();
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
                lastClaimedAt,
                false
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
                null,
                false
        );
    }

    private NotificationEntity createNotificationForPage(String notificationKey, Long recipientId, boolean isRead) {
        return new NotificationEntity(
                null,
                recipientId,
                100L,
                notification.enums.NotificationType.AFTER_PAID,
                notification.enums.NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now(),
                Instant.now(),
                null,
                isRead
        );
    }
}
