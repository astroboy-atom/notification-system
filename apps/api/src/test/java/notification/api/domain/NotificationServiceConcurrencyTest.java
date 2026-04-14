package notification.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import notification.storage.db.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceConcurrencyTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAllInBatch();
    }

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
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

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
            } finally {
                doneLatch.countDown();
            }
        };
    }
}
