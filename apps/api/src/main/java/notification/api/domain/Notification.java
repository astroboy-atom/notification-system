package notification.api.domain;

import java.time.Instant;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;

public record Notification(
        Long id,
        Long recipientId,
        Long eventId,
        Instant requestedAt,
        String notificationKey,
        NotificationType notificationType,
        NotificationChanel notificationChanel,
        NotificationStatus notificationStatus
) {
}
