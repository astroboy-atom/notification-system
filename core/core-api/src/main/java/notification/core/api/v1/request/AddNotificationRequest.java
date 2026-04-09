package notification.core.api.v1.request;

import notification.core.storage.db.Notification;
import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationType;

public record AddNotificationRequest(
        Long recipientId,
        Long eventId,
        NotificationType notificationType,
        NotificationChanel chanel
) {

    public Notification toNotification() {
        return new Notification(recipientId, eventId, notificationType, chanel);
    }
}
