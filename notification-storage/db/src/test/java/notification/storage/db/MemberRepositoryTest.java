package notification.storage.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest extends IntegrationTestSupport {

    @Test
    @DisplayName("ID에 해당하는 사용자가 존재하면 반환한다.")
    void findByIdOrThrowException() {
        MemberEntity saved = memberRepository.save(createMember());

        MemberEntity result = memberRepository.findByIdOrThrowException(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getInAppToken()).isEqualTo("in-app-token");
        assertThat(result.getIsAgreeInAppPush()).isTrue();
        assertThat(result.getIsAgreeEmail()).isTrue();
    }

    @Test
    @DisplayName("ID에 해당하는 사용자가 없으면 예외가 발생한다.")
    void findByIdOrThrowException_throwsException() {
        assertThatThrownBy(() -> memberRepository.findByIdOrThrowException(Long.MAX_VALUE))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }
}
