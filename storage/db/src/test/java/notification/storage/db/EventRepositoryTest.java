package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventRepositoryTest extends IntegrationTestSupport {

    @Test
    @DisplayName("ID에 해당하는 이벤트가 존재하면 반환한다.")
    void findByIdOrThrowException() {
        EventEntity saved = eventRepository.save(createEvent());

        EventEntity result = eventRepository.findByIdOrThrowException(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getContents()).isEqualTo("event contents");
    }

    @Test
    @DisplayName("ID에 해당하는 이벤트가 없으면 예외가 발생한다.")
    void findByIdOrThrowException_throwsException() {
        assertThatThrownBy(() -> eventRepository.findByIdOrThrowException(Long.MAX_VALUE))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 이벤트입니다.");
    }
}
