package notification.api.controller.v1.request;

import java.time.Instant;
import notification.api.domain.NewNotification;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;

public record AddNotificationRequest(
        Long recipientId,
        Long eventId,
        NotificationType notificationType,
        NotificationChanel notificationChanel
) {

    public NewNotification toNotification() {
        return new NewNotification(recipientId, eventId, Instant.now(), notificationType, notificationChanel);
    }
}
