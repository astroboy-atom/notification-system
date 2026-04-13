package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import notification.enums.NotificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

class NotificationRepositoryConcurrencyTest extends IntegrationTestSupport {

    private static final int CONCURRENCY = 5;

    @Test
    @DisplayName("FOR UPDATE SKIP LOCKED 조회는 동시에 읽어도 같은 알림을 중복으로 가져오지 않는다.")
    void findAllByNotificationStatusForUpdateSkipLocked_readsUniqueRows() throws InterruptedException {
        for (int i = 1; i <= 5; i++) {
            notificationRepository.save(createNotification("key-" + i));
        }

        List<Long> readIds = new CopyOnWriteArrayList<>();
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENCY);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENCY);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);

        try {
            for (int i = 0; i < CONCURRENCY; i++) {
                executor.submit(readPendingNotifications(readIds, readyLatch, startLatch, doneLatch));
            }

            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
        } finally {
            executor.shutdown();
        }

        assertThat(readIds).hasSize(5);
        assertThat(Set.copyOf(readIds)).hasSize(5);
    }

    @Test
    @DisplayName("일정 시간 지난 IN_PROGRESS 조회도 동시에 읽어도 같은 알림을 중복으로 가져오지 않는다.")
    void findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked_readsUniqueRows() throws InterruptedException {
        Instant now = Instant.now();

        for (int i = 1; i <= 5; i++) {
            notificationRepository.save(createInProgressNotification("key-" + i, now.minusSeconds(31 + i)));
        }

        List<Long> readIds = new CopyOnWriteArrayList<>();
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENCY);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENCY);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);

        try {
            for (int i = 0; i < CONCURRENCY; i++) {
                executor.submit(readStaleInProgressNotifications(now, readIds, readyLatch, startLatch, doneLatch));
            }

            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
        } finally {
            executor.shutdown();
        }

        assertThat(readIds).hasSize(5);
        assertThat(Set.copyOf(readIds)).hasSize(5);
    }

    private Runnable readPendingNotifications(
            List<Long> readIds,
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch
    ) {
        return () -> {
            readyLatch.countDown();

            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.executeWithoutResult(status -> {
                    List<NotificationEntity> notifications = notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                            NotificationStatus.PENDING.name(),
                            5
                    );

                    notifications.forEach(NotificationEntity::done);
                    notificationRepository.saveAll(notifications);
                    readIds.addAll(notifications.stream().map(NotificationEntity::getId).toList());
                });
            } finally {
                doneLatch.countDown();
            }
        };
    }

    private Runnable readStaleInProgressNotifications(
            Instant now,
            List<Long> readIds,
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch
    ) {
        return () -> {
            readyLatch.countDown();

            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.executeWithoutResult(status -> {
                    List<NotificationEntity> notifications =
                            notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                                    NotificationStatus.IN_PROGRESS.name(),
                                    now.minusSeconds(30),
                                    5
                            );

                    notifications.forEach(NotificationEntity::markPending);
                    notificationRepository.saveAll(notifications);
                    readIds.addAll(notifications.stream().map(NotificationEntity::getId).toList());
                });
            } finally {
                doneLatch.countDown();
            }
        };
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
}
