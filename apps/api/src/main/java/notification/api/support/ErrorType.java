package notification.api.support;

public enum ErrorType {

    DUPLICATED_NOTIFICATION("이미 접수된 알림입니다."),
    NOT_FOUND_NOTIFICATION("존재하지 않는 알림입니다.");

    public final String message;

    ErrorType(String message) {
        this.message = message;
    }
}
