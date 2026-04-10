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

        notificationEntities.forEach(this::doSend);
    }

    private void doSend(NotificationEntity notificationEntity) {
        try {
            supplier.doSend(notificationEntity);
            notificationEntity.done();
        } catch (RetryableException e) {
            handleWhenRetryable(notificationEntity, e);
        } catch (Exception e) {
            handleFinalFailed(notificationEntity, e.getMessage());
        }
    }

    private void handleWhenRetryable(NotificationEntity notificationEntity, RetryableException e) {
        log.warn("알림 전송에 실패했습니다. id = {}, retryCount = {}", notificationEntity.getId(), notificationEntity.getRetryCount());

        if (notificationEntity.canRetry()) {
            notificationEntity.markRetry();
            return;
        }

        handleFinalFailed(notificationEntity, e.getMessage());
    }

    private void handleFinalFailed(NotificationEntity notificationEntity, String reason) {
        log.error("알림 전송에 최종 실패했습니다. id = {}, retryCount = {}", notificationEntity.getId(), notificationEntity.getRetryCount());

        notificationEntity.markFailed(reason);
    }
}
