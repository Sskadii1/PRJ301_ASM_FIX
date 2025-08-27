package perfumeshop.exception;

/**
 * Exception thrown when validation fails
 * @author PerfumeShop Team
 */
public class ValidationException extends RuntimeException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
    }

    public ValidationException(String message, String fieldName) {
        super(buildMessage(message, fieldName));
        this.fieldName = fieldName;
    }

    public ValidationException(String message, String fieldName, Throwable cause) {
        super(buildMessage(message, fieldName), cause);
        this.fieldName = fieldName;
    }

    private static String buildMessage(String message, String fieldName) {
        if (fieldName != null) {
            return "Validation failed for field '" + fieldName + "': " + message;
        }
        return message;
    }

    public String getFieldName() {
        return fieldName;
    }
}
