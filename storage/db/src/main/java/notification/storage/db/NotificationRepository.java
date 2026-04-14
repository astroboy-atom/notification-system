package notification.storage.db;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    default NotificationEntity findByIdOrThrowException(Long id) throws NoSuchElementException {
        return this.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 알림입니다."));
    }

    boolean existsByNotificationKey(String key);

    Page<NotificationEntity> findAllByRecipientIdAndIsRead(Long recipientId, Boolean isRead, Pageable pageable);

    @Query(value = """
               SELECT *
               FROM notification_entity n
               WHERE n.notification_status = :status
                 AND (n.next_attempt_at IS NULL OR n.next_attempt_at <= CURRENT_TIMESTAMP(6))
               ORDER BY n.id
               LIMIT :limit
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationEntity> findAllByNotificationStatusForUpdateSkipLocked(
            @Param("status") String notificationStatus,
            @Param("limit") Integer limit
    );

    @Query(value = """
               SELECT *
               FROM notification_entity n
               WHERE n.notification_status = :status
                 AND n.last_claimed_at <= :claimedBefore
               ORDER BY n.id
               LIMIT :limit
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationEntity> findAllByNotificationStatusAndLastClaimedAtBeforeForUpdateSkipLocked(
            @Param("status") String notificationStatus,
            @Param("claimedBefore") Instant claimedBefore,
            @Param("limit") Integer limit
    );
}
