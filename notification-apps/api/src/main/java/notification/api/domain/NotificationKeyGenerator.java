package notification.api.domain;

import org.springframework.stereotype.Component;

@Component
class NotificationKeyGenerator {

    private static final String KEY_FORMAT = "%d:%d:%s:%s";

    public String generate(NewNotification newNotification) {
        String type = newNotification.getTypeName();
        String chanel = newNotification.getChanelName();

        return KEY_FORMAT.formatted(newNotification.recipientId(), newNotification.eventId(), type, chanel);
    }
}
