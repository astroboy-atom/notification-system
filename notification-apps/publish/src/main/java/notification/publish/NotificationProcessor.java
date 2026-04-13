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

    /**
     * 현재 스레드에서 claim한 notification ids 반환
     */
    @Transactional
    public List<Long> markInProgress(int batchSize) {
        String targetStatus = NotificationStatus.PENDING.name();
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(targetStatus, batchSize);

        notificationEntities.forEach(NotificationEntity::markInProgress);

        return notificationEntities.stream()
                .map(NotificationEntity::getId)
                .toList();
    }

    /**
     * 현재 스레드에서 claim한 notification ids를 처리한다.
     * 발송이 길어지면, recovery에서 소비할 수 있다.
     */
    @Transactional
    public void process(List<Long> ids) {
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllById(ids);

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
