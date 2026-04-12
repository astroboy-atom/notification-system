package notificaiton.supplier;

import notification.storage.db.NotificationEntity;

public interface NotificationSupplier {

    void doSend(NotificationEntity notificationEntity);
}
