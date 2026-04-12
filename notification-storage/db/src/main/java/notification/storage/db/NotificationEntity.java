package notification.storage.db;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notification.enums.NotificationChanel;
import notification.enums.NotificationStatus;
import notification.enums.NotificationType;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity {

    private static final int MAX_RETRY_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(value = EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(value = EnumType.STRING)
    private NotificationChanel notificationChanel;

    @Column(nullable = false, unique = true)
    private String notificationKey;

    @Enumerated(value = EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = true)
    private String failedReason;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = true)
    private Instant nextAttemptAt;

    public NotificationEntity(
            Long recipientId,
            Long eventId,
            NotificationType notificationType,
            NotificationChanel notificationChanel,
            String notificationKey
    ) {
        this(
                null,
                recipientId,
                eventId,
                notificationType,
                notificationChanel,
                notificationKey,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    public void done() {
        this.notificationStatus = NotificationStatus.DONE;
        this.failedReason = null;
        this.nextAttemptAt = null;
    }

    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    public void markRetry() {
        this.retryCount += 1;
        this.notificationStatus = NotificationStatus.PENDING;
        this.nextAttemptAt = Instant.now().plusMillis(calculateDelayMillis());
    }

    private long calculateDelayMillis() {
        long delay = 1000L;

        for (int i = 1; i < retryCount; i++) {
            delay *= 2;
        }

        return delay;
    }

    public void markFailed(String failedReason) {
        this.failedReason = failedReason;
        this.notificationStatus = NotificationStatus.FAILED;
        this.nextAttemptAt = null;
    }

    public void markInProgress() {
        this.notificationStatus = NotificationStatus.IN_PROGRESS;
    }

    public void markPending() {
        this.notificationStatus = NotificationStatus.PENDING;
    }
}
