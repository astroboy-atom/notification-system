package notification.supplier.direct;

import java.util.List;
import lombok.RequiredArgsConstructor;
import notificaiton.supplier.NotificationSupplier;
import notification.storage.db.EventEntity;
import notification.storage.db.EventRepository;
import notification.storage.db.MemberEntity;
import notification.storage.db.MemberRepository;
import notification.storage.db.NotificationEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
class DirectNotificationSupplier implements NotificationSupplier {

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final List<NotificationChanelSender> senders;

    @Override
    public void doSend(NotificationEntity notificationEntity) {
        MemberEntity memberEntity = memberRepository.findByIdOrThrowException(notificationEntity.getRecipientId());
        EventEntity eventEntity = eventRepository.findByIdOrThrowException(notificationEntity.getEventId());
        NotificationChanelSender sender = findSendersOrThrow(notificationEntity);

        sender.send(notificationEntity, memberEntity, eventEntity);
    }

    @Override
    public boolean isSupportIdempotency(NotificationEntity notificationEntity) {
        return false;
    }

    @Override
    public boolean isAlreadySend(NotificationEntity notificationEntity) {
        NotificationChanelSender sender = findSendersOrThrow(notificationEntity);

        return sender.isAlreadySend(notificationEntity);
    }

    private NotificationChanelSender findSendersOrThrow(NotificationEntity notificationEntity) {
        return senders.stream()
                .filter(it -> it.isSupport(notificationEntity.getNotificationChanel()))
                .findFirst()
                .orElseThrow(SenderNotFoundException::new);
    }
}
