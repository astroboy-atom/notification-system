package notification.api.controller.v1;

import lombok.RequiredArgsConstructor;
import notification.api.controller.v1.request.AddNotificationRequest;
import notification.api.controller.v1.request.ScheduleNotificationRequest;
import notification.api.domain.Notification;
import notification.api.domain.NotificationService;
import notification.api.support.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/v1/notifications")
    public ResponseEntity<Long> addNotification(@RequestBody AddNotificationRequest request) {
        Long notificationId = notificationService.addNotification(request.toNotification());

        return ResponseEntity.ok(notificationId);
    }

    @PostMapping("/v1/notifications/scheduled")
    public ResponseEntity<Long> addScheduledNotification(@RequestBody ScheduleNotificationRequest request) {
        Long notificationId = notificationService.addNotification(request.toNotification());

        return ResponseEntity.ok(notificationId);
    }

    @GetMapping("/v1/notifications/{id}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotification(id);

        return ResponseEntity.ok(notification);
    }

    @GetMapping("/v1/notifications/{recipientId}")
    public ResponseEntity<Page<Notification>> getRecipientNotifications(
            @PathVariable Long recipientId,
            @RequestParam Boolean isRead,
            @PageableDefault Pageable pageable
    ) {
        Page<Notification> notifications = notificationService.getNotifications(recipientId, isRead, pageable);

        return ResponseEntity.ok(notifications);
    }
}
