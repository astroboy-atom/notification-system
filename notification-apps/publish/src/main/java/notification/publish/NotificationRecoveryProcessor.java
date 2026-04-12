package notification.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notificaiton.supplier.NotificationSupplier;
import notification.enums.NotificationStatus;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 1. InProgress 마킹 이후, publisher 크래시 상황
 * 2. InProgress 마킹 -> Supplier 전달 -> 성공, 실패 트랜잭션 커밋 실패
 * - case 1. DB 장애 -> InProgress 조회 불가
 * - case 2. DB 커밋 실패 -> 재시도 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRecoveryProcessor {

    private static final int BATCH_SIZE = 5;
    private static final String TARGET_STATUS = NotificationStatus.IN_PROGRESS.name();

    private final NotificationRepository notificationRepository;
    private final NotificationSupplier notificationSupplier;

    @Transactional
    @Scheduled(fixedDelay = 10000)
    public void publish() {
        List<NotificationEntity> notificationEntities =
                notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(TARGET_STATUS, BATCH_SIZE);

        notificationEntities.forEach(this::recovery);
    }

    private void recovery(NotificationEntity notificationEntity) {
        if (notificationSupplier.isSupportIdempotency(notificationEntity)) {
            notificationEntity.markPending();
            return;
        }

        if (notificationSupplier.isAlreadySend(notificationEntity)) {
            notificationEntity.done();
            return;
        }

        notificationEntity.markPending();
    }
}
