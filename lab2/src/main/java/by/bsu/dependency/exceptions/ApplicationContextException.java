package by.bsu.dependency.exceptions;

public class ApplicationContextException extends RuntimeException {
    public ApplicationContextException(String message) {
        super(message);
    }

    public ApplicationContextException() {}

    public ApplicationContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationContextException(Throwable cause) {
        super(cause);
    }

    public ApplicationContextException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}