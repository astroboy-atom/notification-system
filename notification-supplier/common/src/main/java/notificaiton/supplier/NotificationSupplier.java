package notificaiton.supplier;

import notification.storage.db.NotificationEntity;

public interface NotificationSupplier {

    void doSend(NotificationEntity notificationEntity);

    boolean isSupportIdempotency(NotificationEntity notificationEntity);

    boolean isAlreadySend(NotificationEntity notificationEntity);
}
