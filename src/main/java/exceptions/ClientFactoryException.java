package exceptions;

public class ClientFactoryException extends Exception {
    private ErrorCode errorCode;

    public enum ErrorCode {
        CLIENT_NOT_FOUND, GET_CLIENT_FAILURE
    }

    public ClientFactoryException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ClientFactoryException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
