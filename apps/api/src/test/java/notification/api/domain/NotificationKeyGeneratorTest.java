package notification.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
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
                Instant.parse("2026-04-14T00:00:00Z"),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );

        String notificationKey = notificationKeyGenerator.generate(notification);

        assertThat(notificationKey).isEqualTo("1:100:after_paid:email");
    }
}
