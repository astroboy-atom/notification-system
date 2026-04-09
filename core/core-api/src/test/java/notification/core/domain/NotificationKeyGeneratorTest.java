package notification.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationKeyGeneratorTest {

    private final NotificationKeyGenerator notificationKeyGenerator = new NotificationKeyGenerator();

    @Test
    @DisplayName("알림 정보를 기반으로 키를 생성한다.")
    void generate() {
        NewNotification notification = new NewNotification(
                1L,
                100L,
                NotificationType.AFTER_PAIED,
                NotificationChanel.EMAIL
        );

        String notificationKey = notificationKeyGenerator.generate(notification);

        assertThat(notificationKey).isEqualTo("1:100:after_paied:email");
    }
}
