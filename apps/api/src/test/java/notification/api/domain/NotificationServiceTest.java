package notification.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import notification.api.support.BaseException;
import notification.api.support.ErrorType;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import notification.enums.NotificationStatus;
import notification.storage.db.NotificationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class NotificationServiceTest extends IntegrationTestSupport {

    @Test
    @DisplayName("신규 알림을 생성한다.")
    void addNotification() {
        NewNotification notification = new NewNotification(
                1L,
                1L,
                Instant.parse("2026-04-14T00:00:00Z"),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );
        given(notificationRepository.existsByNotificationKey("1:1:after_paid:email")).willReturn(false);
        given(notificationRepository.save(any(NotificationEntity.class)))
                .willAnswer(invocation -> {
                    NotificationEntity entity = invocation.getArgument(0);
                    return createNotificationEntity(
                            1L,
                            entity.getRecipientId(),
                            entity.getEventId(),
                            entity.getNotificationKey(),
                            entity.getNotificationStatus(),
                            entity.getRetryCount(),
                            entity.getFailedReason(),
                            entity.getRequestedAt(),
                            entity.getNextAttemptAt(),
                            entity.getLastClaimedAt(),
                            entity.getIsRead()
                    );
                });

        Long savedId = notificationService.addNotification(notification);
        ArgumentCaptor<NotificationEntity> entityCaptor = ArgumentCaptor.forClass(NotificationEntity.class);

        verify(notificationRepository).save(entityCaptor.capture());

        NotificationEntity saved = entityCaptor.getValue();

        assertThat(savedId).isEqualTo(1L);
        assertThat(saved.getId()).isNull();
        assertThat(saved.getRecipientId()).isEqualTo(1L);
        assertThat(saved.getEventId()).isEqualTo(1L);
        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.AFTER_PAID);
        assertThat(saved.getNotificationChanel()).isEqualTo(NotificationChanel.EMAIL);
        assertThat(saved.getNotificationKey()).isEqualTo("1:1:after_paid:email");
        assertThat(saved.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getFailedReason()).isNull();
        assertThat(saved.getRequestedAt()).isNotNull();
        assertThat(saved.getNextAttemptAt()).isEqualTo(Instant.parse("2026-04-14T00:00:00Z"));
        assertThat(saved.getIsRead()).isFalse();
    }

    @Test
    @DisplayName("이미 접수된 알림이면 예외가 발생한다.")
    void validateAlreadyAdded() {
        NewNotification notification = new NewNotification(
                2L,
                2L,
                Instant.parse("2026-04-14T00:00:00Z"),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );
        given(notificationRepository.existsByNotificationKey("2:2:after_paid:email")).willReturn(true);

        assertThatThrownBy(() -> notificationService.addNotification(notification))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorType.DUPLICATED_NOTIFICATION.message);
    }

    @Test
    @DisplayName("알림 ID로 알림 상세를 조회한다.")
    void getNotification() {
        NotificationEntity saved = createNotificationEntity(
                1L,
                1L,
                100L,
                "detail-key",
                NotificationStatus.PENDING,
                0,
                null,
                Instant.parse("2026-04-14T00:00:00Z"),
                Instant.parse("2026-04-14T00:01:00Z"),
                null,
                false
        );
        given(notificationRepository.findByIdOrThrowException(1L)).willReturn(saved);

        Notification notification = notificationService.getNotification(1L);

        assertThat(notification.id()).isEqualTo(1L);
        assertThat(notification.recipientId()).isEqualTo(1L);
        assertThat(notification.eventId()).isEqualTo(100L);
        assertThat(notification.requestedAt()).isEqualTo(saved.getRequestedAt());
        assertThat(notification.notificationKey()).isEqualTo("detail-key");
        assertThat(notification.notificationType()).isEqualTo(NotificationType.AFTER_PAID);
        assertThat(notification.notificationChanel()).isEqualTo(NotificationChanel.EMAIL);
        assertThat(notification.notificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID면 예외가 발생한다.")
    void getNotification_throwsException() {
        given(notificationRepository.findByIdOrThrowException(Long.MAX_VALUE))
                .willThrow(new NoSuchElementException("존재하지 않는 알림입니다."));

        assertThatThrownBy(() -> notificationService.getNotification(Long.MAX_VALUE))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorType.NOT_FOUND_NOTIFICATION.message);
    }

    @Test
    @DisplayName("수신자와 읽음 여부로 알림 목록을 페이지 조회한다.")
    void getNotifications() {
        NotificationEntity first = createNotificationEntity(
                1L,
                1L,
                100L,
                "key-1",
                NotificationStatus.PENDING,
                0,
                null,
                Instant.parse("2026-04-14T00:00:00Z"),
                Instant.parse("2026-04-14T00:01:00Z"),
                null,
                false
        );
        NotificationEntity second = createNotificationEntity(
                2L,
                1L,
                100L,
                "key-2",
                NotificationStatus.PENDING,
                0,
                null,
                Instant.parse("2026-04-14T00:02:00Z"),
                Instant.parse("2026-04-14T00:03:00Z"),
                null,
                false
        );
        given(notificationRepository.findAllByRecipientIdAndIsRead(eq(1L), eq(false), any()))
                .willReturn(new PageImpl<>(
                        List.of(first),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                        2
                ));

        notification.api.support.Page<Notification> result = notificationService.getNotifications(
                1L,
                false,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(result.isLastPage()).isFalse();
        assertThat(result.totalPage()).isEqualTo(2L);
        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).recipientId()).isEqualTo(1L);
        assertThat(result.data().get(0).notificationKey()).isEqualTo("key-1");
        assertThat(result.data().get(0).requestedAt()).isEqualTo(first.getRequestedAt());
        assertThat(result.data().get(0).notificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(second.getNotificationKey()).isEqualTo("key-2");
    }
}
