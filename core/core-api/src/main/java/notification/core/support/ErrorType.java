package notification.core.support;

public enum ErrorType {

    DUPLICATED_NOTIFICATION("이미 접수된 알림입니다.");

    public final String message;

    ErrorType(String message) {
        this.message = message;
    }
}
