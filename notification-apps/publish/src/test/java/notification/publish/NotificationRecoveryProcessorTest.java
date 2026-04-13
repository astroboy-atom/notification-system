package notification.publish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import notificaiton.supplier.NotificationSupplier;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRecoveryProcessorTest {

    private static final Duration CLAIM_TIMEOUT = Duration.ofMinutes(5);
    private static final int BATCH_SIZE = 5;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSupplier notificationSupplier;

    @InjectMocks
    private NotificationRecoveryProcessor notificationRecoveryProcessor;

    private NotificationEntity staleNotification;

    @BeforeEach
    void setUp() {
        staleNotification = createInProgressNotification("stale-key", Instant.now().minusSeconds(301));
    }

    @Test
    @DisplayName("claim timeout 기준의 IN_PROGRESS 조회를 요청한다.")
    void recovery_queriesTimedOutNotifications() {
        Instant before = Instant.now();
        given(notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                eq(NotificationStatus.IN_PROGRESS.name()),
                any(Instant.class),
                eq(BATCH_SIZE)
        )).willReturn(List.of());

        notificationRecoveryProcessor.recovery();

        Instant after = Instant.now();
        ArgumentCaptor<Instant> claimedBeforeCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(notificationRepository).findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                eq(NotificationStatus.IN_PROGRESS.name()),
                claimedBeforeCaptor.capture(),
                eq(BATCH_SIZE)
        );

        assertThat(claimedBeforeCaptor.getValue().plus(CLAIM_TIMEOUT))
                .isBetween(before, after);
    }

    @Test
    @DisplayName("supplier가 멱등성을 지원하면 IN_PROGRESS 알림을 PENDING으로 되돌린다.")
    void recovery_marksPendingWhenSupplierSupportsIdempotency() {
        given(notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                eq(NotificationStatus.IN_PROGRESS.name()),
                any(Instant.class),
                eq(BATCH_SIZE)
        )).willReturn(List.of(staleNotification));
        given(notificationSupplier.isSupportIdempotency(staleNotification)).willReturn(true);

        notificationRecoveryProcessor.recovery();

        assertThat(staleNotification.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("supplier가 멱등성을 지원하지 않고 이미 전송된 알림이면 DONE으로 변경한다.")
    void recovery_marksDoneWhenAlreadySent() {
        given(notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                eq(NotificationStatus.IN_PROGRESS.name()),
                any(Instant.class),
                eq(BATCH_SIZE)
        )).willReturn(List.of(staleNotification));
        given(notificationSupplier.isSupportIdempotency(staleNotification)).willReturn(false);
        given(notificationSupplier.isAlreadySend(staleNotification)).willReturn(true);

        notificationRecoveryProcessor.recovery();

        assertThat(staleNotification.getNotificationStatus()).isEqualTo(NotificationStatus.DONE);
    }

    @Test
    @DisplayName("supplier가 멱등성을 지원하지 않고 전송 기록도 없으면 PENDING으로 되돌린다.")
    void recovery_marksPendingWhenNotSent() {
        given(notificationRepository.findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
                eq(NotificationStatus.IN_PROGRESS.name()),
                any(Instant.class),
                eq(BATCH_SIZE)
        )).willReturn(List.of(staleNotification));
        given(notificationSupplier.isSupportIdempotency(staleNotification)).willReturn(false);
        given(notificationSupplier.isAlreadySend(staleNotification)).willReturn(false);

        notificationRecoveryProcessor.recovery();

        assertThat(staleNotification.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    private NotificationEntity createInProgressNotification(String notificationKey, Instant lastClaimedAt) {
        return new NotificationEntity(
                null,
                1L,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.IN_PROGRESS,
                0,
                null,
                Instant.now(),
                Instant.now(),
                lastClaimedAt
        );
    }
}
