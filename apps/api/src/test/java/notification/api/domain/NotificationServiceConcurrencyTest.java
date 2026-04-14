package notification.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import notification.storage.db.NotificationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class NotificationServiceConcurrencyTest extends IntegrationTestSupport {

    @Test
    @DisplayName("동시에 같은 알림이 접수되면 하나만 저장된다.")
    void addNotificationConcurrently() throws InterruptedException {
        NewNotification notification = new NewNotification(
                3L,
                3L,
                Instant.parse("2026-04-14T00:00:00Z"),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );
        Set<String> storedKeys = ConcurrentHashMap.newKeySet();
        AtomicLong sequence = new AtomicLong(1L);
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        given(notificationRepository.existsByNotificationKey("3:3:after_paid:email"))
                .willAnswer(invocation -> storedKeys.contains("3:3:after_paid:email"));
        given(notificationRepository.save(any(NotificationEntity.class)))
                .willAnswer(invocation -> {
                    NotificationEntity entity = invocation.getArgument(0);
                    if (!storedKeys.add(entity.getNotificationKey())) {
                        throw new DuplicateKeyException("duplicate notification");
                    }

                    return createNotificationEntity(
                            sequence.getAndIncrement(),
                            entity.getRecipientId(),
                            entity.getEventId(),
                            entity.getNotificationKey(),
                            entity.getNotificationStatus(),
                            entity.getRetryCount(),
                            entity.getFailedReason(),
                            entity.getRequestedAt(),
                            entity.getNextAttemptAt(),
                            entity.getLastClaimedAt(),
                            entity.getIsRead()
                    );
                });
        given(notificationRepository.count()).willAnswer(invocation -> (long) storedKeys.size());

        try {
            List<Runnable> tasks = List.of(
                    createAddNotificationTask(notification, successCount, readyLatch, startLatch, doneLatch),
                    createAddNotificationTask(notification, successCount, readyLatch, startLatch, doneLatch)
            );
            tasks.forEach(executorService::submit);
            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
        } finally {
            executorService.shutdown();
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(notificationRepository.existsByNotificationKey("3:3:after_paid:email")).isTrue();
    }

    private Runnable createAddNotificationTask(
            NewNotification notification,
            AtomicInteger successCount,
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
                notificationService.addNotification(notification);
                successCount.incrementAndGet();
            } catch (DuplicateKeyException ignored) {
            } finally {
                doneLatch.countDown();
            }
        };
    }
}
