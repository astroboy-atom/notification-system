package notification.api.controller.v1.response;

import java.time.Instant;
import java.util.List;
import notification.api.domain.Notification;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;

// notification domain에서 숨기고 싶은 부분이 있는 경우
public record NotificationResponse(
        Long id,
        Long recipientId,
        Long eventId,
        Instant reservedAt,
        NotificationType notificationType,
        NotificationChanel notificationChanel,
        NotificationStatus notificationStatus
) {

    public static List<NotificationResponse> of(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationResponse::of)
                .toList();
    }

    public static NotificationResponse of(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.recipientId(),
                notification.eventId(),
                notification.reservedAt(),
                notification.notificationType(),
                notification.notificationChanel(),
                notification.notificationStatus()
        );
    }
}
