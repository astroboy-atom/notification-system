package notification.core.storage.db;

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
import notification.core.enums.NotificationChanel;
import notification.core.enums.NotificationStatus;
import notification.core.enums.NotificationType;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @Getter
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

    @Enumerated(value = EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = true)
    private String failedReason;

    @Column(nullable = false)
    private Instant requestedAt;

    public Notification(Long recipientId, Long eventId, NotificationType notificationType, NotificationChanel notificationChanel) {
        this(
                null,
                recipientId,
                eventId,
                notificationType,
                notificationChanel,
                NotificationStatus.PENDING,
                0,
                null,
                Instant.now()
        );
    }
}
