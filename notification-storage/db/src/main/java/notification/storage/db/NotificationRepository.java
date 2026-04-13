package notification.storage.db;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    boolean existsByNotificationKey(String key);

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
