package notification.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notificaiton.supplier.AmbiguousCallException;
import notificaiton.supplier.NotificationSupplier;
import notificaiton.supplier.RetryableException;
import notification.enums.NotificationStatus;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {

    private final NotificationRepository notificationRepository;
    private final NotificationSupplier notificationSupplier;

    @Transactional
    public void markInProgress(int batchSize) {
        String targetStatus = NotificationStatus.PENDING.name();
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(targetStatus, batchSize);

        notificationEntities.forEach(NotificationEntity::markInProgress);
    }

    // TODO : 현재 구조는 IN_PROGRESS(markInProgress 호출 후 정상 상태, 크래시로 인한 호출 불확정 상태)를 모두 처리하고 있다.
    @Transactional
    public void process(int batchSize) {
        String targetStatus = NotificationStatus.IN_PROGRESS.name();
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(targetStatus, batchSize);

        notificationEntities.forEach(this::doSend);
    }

    private void doSend(NotificationEntity notificationEntity) {
        try {
            notificationSupplier.doSend(notificationEntity);
            notificationEntity.done();
        } catch (RetryableException e) {
            handleRetryable(notificationEntity, e);
        } catch (AmbiguousCallException ignore) {
        } catch (Exception e) {
            handleFinalFailed(notificationEntity, e.getMessage());
        }
    }

    private void handleRetryable(NotificationEntity notificationEntity, RetryableException e) {
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
