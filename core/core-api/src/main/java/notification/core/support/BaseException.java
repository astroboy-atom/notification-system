package notification.core.support;

public class BaseException extends RuntimeException {

    public BaseException(ErrorType errorType) {
        super(errorType.message);
    }
}
