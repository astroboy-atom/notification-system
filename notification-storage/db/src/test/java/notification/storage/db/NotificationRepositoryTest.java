package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;

import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("키에 해당하는 알림이 존재하면 true를 반환한다.")
    void existsByNotificationKey() {
        String notificationKey = "1:100:after_paid:email";

        NotificationEntity notification = new NotificationEntity(
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey
        );
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

    @SpringBootApplication
    static class TestApplication {
    }
}
