package notification.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import notification.enums.NotificationStatus;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
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
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatus(NotificationStatus.PENDING);

        notificationEntities.forEach(supplier::doSend);
        notificationEntities.forEach(NotificationEntity::done);
    }
}
