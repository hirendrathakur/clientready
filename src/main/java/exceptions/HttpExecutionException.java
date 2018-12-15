package exceptions;

public class HttpExecutionException extends Exception {
    public HttpExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
