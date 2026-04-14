package notification.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import notification.enums.NotificationChanel;
import notification.enums.NotificationType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private final MeterRegistry meterRegistry;

    public void incrementFinalFailure(NotificationChanel notificationChanel, NotificationType notificationType) {
        Counter.builder("notification.final_failure.total")
                .tag("channel", notificationChanel.name())
                .tag("type", notificationType.name())
                .register(meterRegistry)
                .increment();
    }

    public void incrementAmbiguousFailure(NotificationChanel notificationChanel, NotificationType notificationType) {
        Counter.builder("notification.ambiguous.total")
                .tag("channel", notificationChanel.name())
                .tag("type", notificationType.name())
                .register(meterRegistry)
                .increment();
    }
}
