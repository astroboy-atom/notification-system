package notification.storage.db;

import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    default MemberEntity findByIdOrThrowException(Long id) throws NoSuchElementException {
        return this.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));
    }
}
