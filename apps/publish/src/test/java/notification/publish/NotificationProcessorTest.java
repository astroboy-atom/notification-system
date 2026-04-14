package notification.publish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import notificaiton.supplier.AmbiguousCallException;
import notificaiton.supplier.RetryableException;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;
import notification.storage.db.NotificationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationProcessorTest extends IntegrationTestSupport {

    @Test
    @DisplayName("PENDING 알림을 claim해서 IN_PROGRESS로 바꾸고 ids를 반환한다.")
    void markInProgress() {
        NotificationEntity first = createPendingNotification(1L, "notification-key-1");
        NotificationEntity second = createPendingNotification(2L, "notification-key-2");
        Instant before = Instant.now();
        given(notificationRepository.findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                2
        )).willReturn(List.of(first, second));

        List<Long> ids = notificationProcessor.markInProgress(2);

        Instant after = Instant.now();
        verify(notificationRepository).findAllByNotificationStatusForUpdateSkipLocked(
                NotificationStatus.PENDING.name(),
                2
        );
        assertThat(ids).containsExactly(1L, 2L);
        assertThat(first.getNotificationStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
        assertThat(second.getNotificationStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
        assertThat(first.getLastClaimedAt()).isBetween(before, after);
        assertThat(second.getLastClaimedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("발송에 성공하면 알림 상태를 DONE으로 변경한다.")
    void process_marksDoneWhenSendSucceeds() {
        NotificationEntity notification = createInProgressNotification(1L, "notification-key");
        given(notificationRepository.findAllById(List.of(1L))).willReturn(List.of(notification));

        notificationProcessor.process(List.of(1L));

        assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.DONE);
        assertThat(notification.getFailedReason()).isNull();
        assertThat(notification.getNextAttemptAt()).isNotNull();
    }

    @Test
    @DisplayName("재시도 가능한 예외가 발생하면 재시도 상태로 되돌린다.")
    void process_marksRetryWhenRetryableExceptionOccurs() {
        NotificationEntity notification = createInProgressNotification(1L, "notification-key");
        Instant before = Instant.now();
        given(notificationRepository.findAllById(List.of(1L))).willReturn(List.of(notification));
        doThrow(new RetryableException()).when(notificationSupplier).doSend(any(NotificationEntity.class));

        notificationProcessor.process(List.of(1L));

        Instant after = Instant.now();
        assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getNextAttemptAt())
                .isBetween(before.plusSeconds(1), after.plusSeconds(1));
    }

    @Test
    @DisplayName("재시도 한도를 넘긴 예외면 FAILED로 종료한다.")
    void process_marksFailedWhenRetryableExceptionOccursAfterMaxRetries() {
        NotificationEntity notification = createInProgressNotification(1L, "notification-key", 3);
        given(notificationRepository.findAllById(List.of(1L))).willReturn(List.of(notification));
        doThrow(new RetryableException()).when(notificationSupplier).doSend(any(NotificationEntity.class));

        notificationProcessor.process(List.of(1L));

        assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getRetryCount()).isEqualTo(3);
        assertThat(notification.getNextAttemptAt()).isNotNull();
        verify(notificationMetrics).incrementFinalFailure(
                NotificationChanel.EMAIL,
                NotificationType.AFTER_PAID
        );
        verify(notificationMetrics, never()).incrementAmbiguousFailure(any(), any());
    }

    @Test
    @DisplayName("호출 결과가 불명확하면 상태를 그대로 두고 ambiguous 메트릭을 증가시킨다.")
    void process_keepsInProgressWhenCallIsAmbiguous() {
        NotificationEntity notification = createInProgressNotification(1L, "notification-key");
        Instant lastClaimedAt = notification.getLastClaimedAt();
        given(notificationRepository.findAllById(List.of(1L))).willReturn(List.of(notification));
        doThrow(new AmbiguousCallException()).when(notificationSupplier).doSend(any(NotificationEntity.class));

        notificationProcessor.process(List.of(1L));

        assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.IN_PROGRESS);
        assertThat(notification.getRetryCount()).isZero();
        assertThat(notification.getLastClaimedAt()).isEqualTo(lastClaimedAt);
        verify(notificationMetrics).incrementAmbiguousFailure(
                NotificationChanel.EMAIL,
                NotificationType.AFTER_PAID
        );
        verify(notificationMetrics, never()).incrementFinalFailure(any(), any());
    }

    @Test
    @DisplayName("예상하지 못한 예외가 발생하면 FAILED로 종료하고 실패 메트릭을 증가시킨다.")
    void process_marksFailedWhenUnexpectedExceptionOccurs() {
        NotificationEntity notification = createInProgressNotification(1L, "notification-key");
        given(notificationRepository.findAllById(List.of(1L))).willReturn(List.of(notification));
        doThrow(new IllegalStateException("send failed")).when(notificationSupplier).doSend(any(NotificationEntity.class));

        notificationProcessor.process(List.of(1L));

        assertThat(notification.getNotificationStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getFailedReason()).isEqualTo("send failed");
        assertThat(notification.getNextAttemptAt()).isNotNull();
        verify(notificationMetrics).incrementFinalFailure(
                NotificationChanel.EMAIL,
                NotificationType.AFTER_PAID
        );
        verify(notificationMetrics, never()).incrementAmbiguousFailure(any(), any());
    }
}
