package notification.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.enums.NotificationStatus;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
class NotificationPublisher {

    private static final int BATCH_SIZE = 5;
    private static final String TARGET_STATUS = NotificationStatus.PENDING.name();

    private final NotificationRepository notificationRepository;
    private final NotificationSupplier supplier;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void publish() {

        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(TARGET_STATUS, BATCH_SIZE);

        notificationEntities.forEach(supplier::doSend);

        notificationEntities.forEach(NotificationEntity::done);
    }
}
