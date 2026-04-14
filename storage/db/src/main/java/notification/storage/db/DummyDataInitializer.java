package notification.storage.db;

import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO : 테스트 환경에서는 제거
@Slf4j
@Component
@RequiredArgsConstructor
class DummyDataInitializer implements ApplicationRunner {

    private static final int DUMMY_COUNT = 10;

    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedDummyMemberIfEmpty();
        seedDummyEventIfEmpty();
    }

    private void seedDummyMemberIfEmpty() {
        if (memberRepository.count() > 0) {
            return;
        }

        IntStream.rangeClosed(1, DUMMY_COUNT)
                .mapToObj(index -> new MemberEntity(
                        null,
                        "dummy" + index + "@notification.local",
                        "dummy-in-app-token-" + index,
                        true,
                        true
                ))
                .forEach(memberRepository::save);

        log.info("더미 사용자 {}명을 생성했습니다.", DUMMY_COUNT);
    }

    private void seedDummyEventIfEmpty() {
        if (eventRepository.count() > 0) {
            return;
        }

        IntStream.rangeClosed(1, DUMMY_COUNT)
                .mapToObj(index -> new EventEntity(
                        null,
                        "더미 이벤트 " + index
                ))
                .forEach(eventRepository::save);

        log.info("더미 이벤트 {}개를 생성했습니다.", DUMMY_COUNT);
    }
}
