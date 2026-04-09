package notification.core.domain;

import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationType;

public record NewNotification(
        Long recipientId,
        Long eventId,
        NotificationType notificationType,
        NotificationChanel notificationChanel
) {

    public String getTypeName() {
        return notificationType.name().toLowerCase();
    }

    public String getChanelName() {
        return notificationChanel.name().toLowerCase();
    }
}
