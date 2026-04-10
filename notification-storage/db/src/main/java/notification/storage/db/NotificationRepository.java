package notification.storage.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
                UPDATE notification_entity n
                SET n.notification_status = :status
                where n.id in :ids
            
            """, nativeQuery = true)
    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateByIdsInWithNewTx(
            @Param("ids") List<Long> ids,
            @Param("status") String notificationStatus
    );
}
