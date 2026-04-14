package notification.storage.db;

import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    default EventEntity findByIdOrThrowException(Long id) throws NoSuchElementException {
        return this.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 이벤트입니다."));
    }
}
