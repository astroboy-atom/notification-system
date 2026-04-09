package notification.core.domain;

import lombok.RequiredArgsConstructor;
import notification.core.storage.db.NotificationEntity;
import notification.core.storage.db.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationKeyGenerator keyGenerator;

    @Transactional
    public Long addNotification(NewNotification notification) {
        NotificationEntity entity = new NotificationEntity(
                notification.recipientId(),
                notification.eventId(),
                notification.notificationType(),
                notification.notificationChanel(),
                keyGenerator.generate(notification)
        );

        NotificationEntity saved = notificationRepository.save(entity);

        return saved.getId();
    }
}
