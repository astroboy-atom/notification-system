package notification.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationType;
import notification.core.storage.db.Notification;
import notification.core.storage.db.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("신규 알림을 생성한다.")
    void addNotification() {
        Notification notification = new Notification(1L, 1L, NotificationType.AFTER_PAIED, NotificationChanel.EMAIL);

        Long savedId = notificationService.addNotification(notification);

        Optional<Notification> result = notificationRepository.findById(savedId);
        assertThat(result)
                .isNotEmpty()
                .map(Notification::getId)
                .hasValue(notification.getId());
    }
}
