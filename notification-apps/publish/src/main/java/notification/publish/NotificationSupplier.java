package notification.publish;

import lombok.extern.slf4j.Slf4j;
import notification.storage.db.NotificationEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSupplier {

    public void doSend(NotificationEntity notificationEntity) {
        log.info("[send] 알림을 전송합니다. n = {}", notificationEntity);

        if (notificationEntity.getRecipientId() == 1L) { // 항상 실패
            throw new RetryableException();
        } else if (notificationEntity.getRecipientId() == 2L) { // 2번 재시도 이후 성공
            if (notificationEntity.getRetryCount() < 2) {
                throw new RetryableException();
            }
        }
    }
}
