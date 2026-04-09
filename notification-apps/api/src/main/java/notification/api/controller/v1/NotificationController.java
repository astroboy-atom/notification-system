package notification.api.controller.v1;

import lombok.RequiredArgsConstructor;
import notification.api.controller.v1.request.AddNotificationRequest;
import notification.api.domain.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
