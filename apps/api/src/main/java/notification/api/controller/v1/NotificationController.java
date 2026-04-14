package notification.api.controller.v1;

import java.util.List;
import lombok.RequiredArgsConstructor;
import notification.api.controller.v1.request.AddNotificationRequest;
import notification.api.controller.v1.request.ScheduleNotificationRequest;
import notification.api.controller.v1.response.AddNotificationResponse;
import notification.api.controller.v1.response.NotificationResponse;
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
    public ResponseEntity<AddNotificationResponse> addNotification(@RequestBody AddNotificationRequest request) {
        Long notificationId = notificationService.addNotification(request.toNotification());
        AddNotificationResponse response = new AddNotificationResponse(notificationId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/v1/notifications/scheduled")
    public ResponseEntity<AddNotificationResponse> addScheduledNotification(@RequestBody ScheduleNotificationRequest request) {
        Long notificationId = notificationService.addNotification(request.toNotification());
        AddNotificationResponse response = new AddNotificationResponse(notificationId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/notifications/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotification(id);
        NotificationResponse response = NotificationResponse.of(notification);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/notifications/{recipientId}")
    public ResponseEntity<Page<NotificationResponse>> getRecipientNotifications(
            @PathVariable Long recipientId,
            @RequestParam Boolean isRead,
            @PageableDefault Pageable pageable
    ) {
        Page<Notification> page = notificationService.getNotifications(recipientId, isRead, pageable);
        List<NotificationResponse> responses = NotificationResponse.of(page.data());

        return ResponseEntity.ok(Page.convertData(page, responses));
    }
}
