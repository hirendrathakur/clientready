package exceptions;

public class ClientExecutionException extends Exception {
    public ClientExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
