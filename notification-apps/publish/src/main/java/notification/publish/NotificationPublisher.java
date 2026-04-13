package notification.publish;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class NotificationPublisher {

    private static final int BATCH_SIZE = 5;

    private final NotificationProcessor notificationProcessor;

    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<Long> ids = notificationProcessor.markInProgress(BATCH_SIZE);
        notificationProcessor.process(ids);
    }
}
