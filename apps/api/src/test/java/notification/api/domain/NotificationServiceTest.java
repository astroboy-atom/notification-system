package notification.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import notification.api.support.BaseException;
import notification.api.support.ErrorType;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;
import notification.storage.db.NotificationEntity;
import notification.storage.db.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("신규 알림을 생성한다.")
    void addNotification() {
        NewNotification notification = new NewNotification(
                1L,
                1L,
                Instant.now(),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );

        Long savedId = notificationService.addNotification(notification);

        Optional<NotificationEntity> result = notificationRepository.findById(savedId);

        assertThat(result).hasValueSatisfying(saved -> {
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getRecipientId()).isEqualTo(1L);
            assertThat(saved.getEventId()).isEqualTo(1L);
            assertThat(saved.getNotificationType()).isEqualTo(NotificationType.AFTER_PAID);
            assertThat(saved.getNotificationChanel()).isEqualTo(NotificationChanel.EMAIL);
            assertThat(saved.getNotificationKey()).isEqualTo("1:1:after_paid:email");
            assertThat(saved.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(saved.getRetryCount()).isZero();
            assertThat(saved.getFailedReason()).isNull();
            assertThat(saved.getRequestedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("이미 접수된 알림이면 예외가 발생한다.")
    void validateAlreadyAdded() {
        NewNotification notification = new NewNotification(
                2L,
                2L,
                Instant.now(),
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL
        );

        notificationService.addNotification(notification);

        assertThatThrownBy(() -> notificationService.addNotification(notification))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorType.DUPLICATED_NOTIFICATION.message);
    }

    @Test
    @DisplayName("알림 ID로 알림 상세를 조회한다.")
    void getNotification() {
        Instant reservedAt = Instant.parse("2026-04-14T00:00:00Z");
        NotificationEntity saved = notificationRepository.save(createNotificationEntity(1L, "detail-key", false, reservedAt));

        Notification notification = notificationService.getNotification(saved.getId());

        assertThat(notification.id()).isEqualTo(saved.getId());
        assertThat(notification.recipientId()).isEqualTo(1L);
        assertThat(notification.eventId()).isEqualTo(100L);
        assertThat(notification.reservedAt()).isEqualTo(reservedAt);
        assertThat(notification.requestAt()).isEqualTo(saved.getRequestedAt());
        assertThat(notification.notificationKey()).isEqualTo("detail-key");
        assertThat(notification.notificationType()).isEqualTo(NotificationType.AFTER_PAID);
        assertThat(notification.notificationChanel()).isEqualTo(NotificationChanel.EMAIL);
        assertThat(notification.notificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID면 예외가 발생한다.")
    void getNotification_throwsException() {
        assertThatThrownBy(() -> notificationService.getNotification(Long.MAX_VALUE))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorType.NOT_FOUND_NOTIFICATION.message);
    }

    @Test
    @DisplayName("수신자와 읽음 여부로 알림 목록을 페이지 조회한다.")
    void getNotifications() {
        Instant firstReservedAt = Instant.parse("2026-04-14T00:00:00Z");
        Instant secondReservedAt = Instant.parse("2026-04-14T00:01:00Z");
        notificationRepository.save(createNotificationEntity(1L, "key-1", false, firstReservedAt));
        notificationRepository.save(createNotificationEntity(1L, "key-2", false, secondReservedAt));
        notificationRepository.save(createNotificationEntity(1L, "read-key", true, Instant.parse("2026-04-14T00:02:00Z")));
        notificationRepository.save(createNotificationEntity(2L, "other-key", false, Instant.parse("2026-04-14T00:03:00Z")));

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
        assertThat(result.data().get(0).reservedAt()).isEqualTo(firstReservedAt);
        assertThat(result.data().get(0).notificationStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    private NotificationEntity createNotificationEntity(
            Long recipientId,
            String notificationKey,
            boolean isRead,
            Instant reservedAt
    ) {
        return new NotificationEntity(
                null,
                recipientId,
                100L,
                NotificationType.AFTER_PAID,
                NotificationChanel.EMAIL,
                notificationKey,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now(),
                reservedAt,
                null,
                isRead
        );
    }
}
