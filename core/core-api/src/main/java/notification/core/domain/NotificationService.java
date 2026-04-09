package notification.core.domain;

import lombok.RequiredArgsConstructor;
import notification.core.storage.db.Notification;
import notification.core.storage.db.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Long addNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);

        return saved.getId();
    }
}
