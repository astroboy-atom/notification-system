package notification.api.domain;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import notification.api.support.BaseException;
import notification.api.support.ErrorType;
import notification.api.support.Page;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationKeyGenerator keyGenerator;

    @Transactional
    public Long addNotification(NewNotification notification) {
        String notificationKey = keyGenerator.generate(notification);
        validateAlreadyAdded(notificationKey);

        return saveEntity(notification, notificationKey).getId();
    }

    private void validateAlreadyAdded(String key) {
        if (notificationRepository.existsByNotificationKey(key)) {
            throw new BaseException(ErrorType.DUPLICATED_NOTIFICATION);
        }
    }

    private NotificationEntity saveEntity(NewNotification notification, String notificationKey) {
        NotificationEntity entity = toEntity(notification, notificationKey);

        return notificationRepository.save(entity);
    }

    private NotificationEntity toEntity(NewNotification notification, String notificationKey) {
        return new NotificationEntity(
                notification.recipientId(),
                notification.eventId(),
                notification.notificationType(),
                notification.notificationChanel(),
                notificationKey,
                notification.reservedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long recipientId, Boolean isRead, Pageable pageable) {
        var pageResult = notificationRepository.findAllByRecipientIdAndIsRead(recipientId, isRead, pageable)
                .map(this::toDomain);

        return Page.of(pageResult);
    }

    @Transactional(readOnly = true)
    public Notification getNotification(Long id) {
        try {
            NotificationEntity entity = notificationRepository.findByIdOrThrowException(id);
            return toDomain(entity);
        } catch (NoSuchElementException e) {
            throw new BaseException(ErrorType.NOT_FOUND_NOTIFICATION);
        }
    }

    private Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getRecipientId(),
                entity.getEventId(),
                entity.getRequestedAt(),
                entity.getNotificationKey(),
                entity.getNotificationType(),
                entity.getNotificationChanel(),
                entity.getNotificationStatus()
        );
    }
}
