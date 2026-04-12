package notification.supplier.direct;

import notification.enums.NotificationChanel;
import notification.storage.db.EventEntity;
import notification.storage.db.MemberEntity;
import notification.storage.db.NotificationEntity;

public interface NotificationChanelSender {

    boolean isSupport(NotificationChanel chanel);

    void send(NotificationEntity notificationEntity, MemberEntity memberEntity, EventEntity eventEntity);
}
