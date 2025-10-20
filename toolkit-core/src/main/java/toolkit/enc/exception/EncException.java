package toolkit.enc.exception;


public class EncException extends RuntimeException{
    public EncException(String message) {
        super(message);
    }

    public EncException(Throwable cause) {
        super(cause);
    }

    public EncException(String message, Throwable cause) {
        super(message, cause);
    }
}
