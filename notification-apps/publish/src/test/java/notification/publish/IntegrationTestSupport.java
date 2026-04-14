package notification.publish;

import static org.mockito.Mockito.reset;

import java.time.Instant;
import notificaiton.supplier.NotificationSupplier;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = IntegrationTestSupport.TestConfiguration.class)
abstract class IntegrationTestSupport {

    @Autowired
    protected NotificationProcessor notificationProcessor;

    @Autowired
    protected NotificationRecoveryProcessor notificationRecoveryProcessor;

    @MockitoBean
    protected NotificationRepository notificationRepository;

    @MockitoBean
    protected NotificationSupplier notificationSupplier;

    @AfterEach
    void tearDown() {
        reset(notificationRepository, notificationSupplier);
    }

    protected NotificationEntity createPendingNotification(Long id, String notificationKey) {
        return createPendingNotification(id, notificationKey, Instant.now());
    }

    protected NotificationEntity createPendingNotification(Long id, String notificationKey, Instant nextAttemptAt) {
        return new NotificationEntity(
                id,
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now(),
                nextAttemptAt,
                null
        );
    }

    protected NotificationEntity createInProgressNotification(Long id, String notificationKey) {
        return createInProgressNotification(id, notificationKey, 0);
    }

    protected NotificationEntity createInProgressNotification(Long id, String notificationKey, int retryCount) {
        return new NotificationEntity(
                id,
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.IN_PROGRESS,
                retryCount,
                null,
                Instant.now(),
                null,
                Instant.now().minusSeconds(10)
        );
    }

    protected NotificationEntity createInProgressNotification(String notificationKey, Instant lastClaimedAt) {
        return new NotificationEntity(
                null,
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.IN_PROGRESS,
                0,
                null,
                Instant.now(),
                Instant.now(),
                lastClaimedAt
        );
    }

    @Configuration
    @Import({NotificationProcessor.class, NotificationRecoveryProcessor.class})
    static class TestConfiguration {
    }
}
