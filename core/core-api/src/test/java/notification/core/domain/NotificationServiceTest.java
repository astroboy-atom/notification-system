package notification.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationStatus;
import notification.core.enums.NotificationType;
import notification.core.storage.db.NotificationEntity;
import notification.core.storage.db.NotificationRepository;
import notification.core.support.BaseException;
import notification.core.support.ErrorType;
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
        NewNotification notification = new NewNotification(1L, 1L, NotificationType.AFTER_PAIED, NotificationChanel.EMAIL);

        Long savedId = notificationService.addNotification(notification);

        Optional<NotificationEntity> result = notificationRepository.findById(savedId);

        assertThat(result).hasValueSatisfying(saved -> {
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getRecipientId()).isEqualTo(1L);
            assertThat(saved.getEventId()).isEqualTo(1L);
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.AFTER_PAIED);
            assertThat(saved.getNotificationChanel()).isEqualTo(NotificationChanel.EMAIL);
            assertThat(saved.getNotificationKey()).isEqualTo("1:1:after_paied:email");
            assertThat(saved.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(saved.getRetryCount()).isZero();
            assertThat(saved.getFailedReason()).isNull();
            assertThat(saved.getRequestedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("이미 접수된 알림이면 예외가 발생한다.")
    void validateAlreadyAdded() {
        NewNotification notification = new NewNotification(2L, 2L, NotificationType.AFTER_PAIED, NotificationChanel.EMAIL);

        notificationService.addNotification(notification);

        assertThatThrownBy(() -> notificationService.addNotification(notification))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorType.DUPLICATED_NOTIFICATION.message);
    }
}
