package notification.core.api.v1.request;

import notification.core.domain.NewNotification;
import notification.core.storage.db.NotificationEntity;
import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationType;

public record AddNotificationRequest(
        Long recipientId,
        Long eventId,
        NotificationType notificationType,
        NotificationChanel notificationChanel
) {

    public NewNotification toNotification() {
        return new NewNotification(recipientId, eventId, notificationType, notificationChanel);
    }
}
