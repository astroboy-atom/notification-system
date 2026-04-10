package notification.storage.db;

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
               ORDER BY n.id
               LIMIT :limit
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<NotificationEntity> findAllByNotificationStatusForUpdateSkipLocked(
            @Param("status") String notificationStatus,
            @Param("limit") Integer limit
    );
}
