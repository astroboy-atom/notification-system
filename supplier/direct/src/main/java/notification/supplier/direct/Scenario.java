package notification.supplier.direct;

import lombok.RequiredArgsConstructor;
import notificaiton.supplier.AmbiguousCallException;
import notificaiton.supplier.RetryableException;
import notification.storage.db.NotificationEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * for test
 * 수신인 ID가 1이면, retryable exception -> 2번째 재시도에 성공
 * 수신인 ID가 2이면, retryable exception -> 모든 재시도에서 실패(final fail)
 * 수신인 ID가 3이면, none retryable exception
 * 수신인 ID가 4이면, ambiguous call exception
 * 수신인 ID가 5이면, process crash -> sender 수행 후 ambiguous call exception
 */
@Component
@RequiredArgsConstructor
class Scenario {

    private static final long RETRY_THEN_SUCCESS_RECIPIENT_ID = 1L;
    private static final long ALWAYS_RETRYABLE_FAILURE_RECIPIENT_ID = 2L;
    private static final long NON_RETRYABLE_FAILURE_RECIPIENT_ID = 3L;
    private static final long AMBIGUOUS_CALL_RECIPIENT_ID = 4L;
    private static final long POST_SEND_CRASH_RECIPIENT_ID = 5L;

    private final ApplicationContext context;

    public void execute(NotificationEntity notificationEntity) {
        long recipientId = notificationEntity.getRecipientId();

        if (recipientId == RETRY_THEN_SUCCESS_RECIPIENT_ID) {
            retryThenSuccess(notificationEntity);
            return;
        }

        if (recipientId == ALWAYS_RETRYABLE_FAILURE_RECIPIENT_ID) {
            throw new RetryableException();
        }

        if (recipientId == NON_RETRYABLE_FAILURE_RECIPIENT_ID) {
            throw new IllegalStateException("none retryable exception");
        }

        if (recipientId == AMBIGUOUS_CALL_RECIPIENT_ID) {
            throw new AmbiguousCallException();
        }

        if (recipientId == POST_SEND_CRASH_RECIPIENT_ID) {
            SpringApplication.exit(context, () -> 1);
            System.exit(1);
        }
    }

    public void retryThenSuccess(NotificationEntity notificationEntity) {
        if (notificationEntity.getRetryCount() < 2) {
            throw new RetryableException();
        }

        throw new IllegalStateException("none retryable exception");
    }
}
