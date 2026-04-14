package notification.supplier.mq;

import lombok.extern.slf4j.Slf4j;
import notificaiton.supplier.NotificationSupplier;
import notification.storage.db.NotificationEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageBrokerNotificationSupplier implements NotificationSupplier {

    @Override
    public void doSend(NotificationEntity notificationEntity) {
        log.info(
                "메시지 브로커로 메시지를 전송합니다. notification id = {} topic = {}",
                notificationEntity.getId(),
                notificationEntity.getNotificationChanel().name()
        );
    }

    @Override
    public boolean isSupportIdempotency(NotificationEntity notificationEntity) {
        return true;
    }

    @Override
    public boolean isAlreadySend(NotificationEntity notificationEntity) {
        return true;
    }
}
