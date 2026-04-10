package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import notification.enums.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
    @DisplayName("주어진 ID 목록의 알림 상태를 새 트랜잭션에서 일괄 변경한다.")
    void updateByIdsInWithNewTx() {
        NotificationEntity first = notificationRepository.save(createNotification("key-1"));
        NotificationEntity second = notificationRepository.save(createNotification("key-2"));
        NotificationEntity third = notificationRepository.save(createNotification("key-3"));

        TransactionStatus outerTransaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            notificationRepository.updateByIdsInWithNewTx(
                    List.of(first.getId(), second.getId()),
                    NotificationStatus.IN_PROGRESS.name()
            );
        } finally {
            transactionManager.rollback(outerTransaction);
        }

        assertThat(notificationRepository.findAllById(List.of(first.getId(), second.getId())))
                .extracting(NotificationEntity::getNotificationStatus)
                .containsOnly(NotificationStatus.IN_PROGRESS);

        assertThat(notificationRepository.findAllById(List.of(third.getId())))
                .extracting(NotificationEntity::getNotificationStatus)
                .containsOnly(NotificationStatus.PENDING);
    }
}
