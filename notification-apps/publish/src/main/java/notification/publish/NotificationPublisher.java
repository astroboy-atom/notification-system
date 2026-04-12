package notification.publish;

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
        notificationProcessor.markInProgress(BATCH_SIZE);
        notificationProcessor.process(BATCH_SIZE);
    }
}
