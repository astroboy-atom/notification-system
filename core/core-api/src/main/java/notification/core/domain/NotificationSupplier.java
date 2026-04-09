package notification.core.domain;

import lombok.extern.slf4j.Slf4j;
import notification.core.storage.db.NotificationEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSupplier {

    public void doSend(NotificationEntity notificationEntity) {
        log.info("[send] 알림을 전송합니다. n = {}", notificationEntity.toString());
    }
}
