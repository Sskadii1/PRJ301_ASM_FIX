package perfumeshop.exception;

/**
 * Exception thrown when business logic fails
 * @author PerfumeShop Team
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public BusinessException(String message, String errorCode) {
        super(buildMessage(message, errorCode));
        this.errorCode = errorCode;
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(buildMessage(message, errorCode), cause);
        this.errorCode = errorCode;
    }

    private static String buildMessage(String message, String errorCode) {
        if (errorCode != null) {
            return "[" + errorCode + "] " + message;
        }
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
