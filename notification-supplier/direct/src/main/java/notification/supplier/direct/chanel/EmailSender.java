package notification.supplier.direct.chanel;

import lombok.extern.slf4j.Slf4j;
import notification.supplier.direct.NotificationChanelSender;
import notification.enums.NotificationChanel;
import notification.storage.db.EventEntity;
import notification.storage.db.MemberEntity;
import notification.storage.db.NotificationEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class EmailSender implements NotificationChanelSender {

    @Override
    public boolean isSupport(NotificationChanel chanel) {
        return NotificationChanel.EMAIL == chanel;
    }

    @Override
    public void send(NotificationEntity notificationEntity, MemberEntity memberEntity, EventEntity eventEntity) {
        if (!memberEntity.getIsAgreeEmail()) {
            throw new IllegalStateException("이메일 동의를 수행하지 않았습니다.");
        }

        log.info(
                "이메일 발송을 수행했습니다. notification id = {} recipient id = {} recipient email = {}",
                notificationEntity.getId(),
                memberEntity.getId(),
                memberEntity.getEmail()
        );
    }
}
