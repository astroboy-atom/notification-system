package notification.api.domain;

import static org.mockito.Mockito.reset;

import java.time.Instant;
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
    protected NotificationService notificationService;

    @Autowired
    protected NotificationKeyGenerator notificationKeyGenerator;

    @MockitoBean
    protected NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        reset(notificationRepository);
    }

    protected NotificationEntity createNotificationEntity(
            Long id,
            Long recipientId,
            Long eventId,
            String notificationKey,
            NotificationStatus notificationStatus,
            int retryCount,
            String failedReason,
            Instant requestedAt,
            Instant nextAttemptAt,
            Instant lastClaimedAt,
            boolean isRead
    ) {
        return new NotificationEntity(
                id,
                recipientId,
                eventId,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                notificationStatus,
                retryCount,
                failedReason,
                requestedAt,
                nextAttemptAt,
                lastClaimedAt,
                isRead
        );
    }

    @Configuration
    @Import({NotificationService.class, NotificationKeyGenerator.class})
    static class TestConfiguration {
    }
}
