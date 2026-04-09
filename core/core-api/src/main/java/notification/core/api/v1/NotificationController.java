package notification.core.api.v1;

import lombok.RequiredArgsConstructor;
import notification.core.api.v1.request.AddNotificationRequest;
import notification.core.domain.NotificationService;
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
