package notification.core.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import notification.core.domain.NotificationSupplier;
import notification.core.enums.NotificationStatus;
import notification.core.storage.db.NotificationEntity;
import notification.core.storage.db.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class NotificationPublisher {

    private final NotificationRepository notificationRepository;
    private final NotificationSupplier supplier;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<NotificationEntity> notificationEntities = notificationRepository.findAllByNotificationStatus(NotificationStatus.PENDING);

        notificationEntities.forEach(supplier::doSend);

        notificationEntities.forEach(NotificationEntity::done);
    }
}
