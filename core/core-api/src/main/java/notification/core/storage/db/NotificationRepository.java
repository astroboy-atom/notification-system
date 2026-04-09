package notification.core.storage.db;

import java.util.List;
import notification.core.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    boolean existsByNotificationKey(String key);

    List<NotificationEntity> findAllByNotificationStatus(NotificationStatus notificationStatus);
}
