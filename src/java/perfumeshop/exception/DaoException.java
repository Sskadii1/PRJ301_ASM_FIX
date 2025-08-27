package perfumeshop.exception;

/**
 * Exception thrown when DAO operations fail
 * @author PerfumeShop Team
 */
public class DaoException extends RuntimeException {

    private final String operation;
    private final String entityName;

    public DaoException(String message) {
        super(message);
        this.operation = null;
        this.entityName = null;
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.entityName = null;
    }

    public DaoException(String message, String operation, String entityName) {
        super(buildMessage(message, operation, entityName));
        this.operation = operation;
        this.entityName = entityName;
    }

    public DaoException(String message, String operation, String entityName, Throwable cause) {
        super(buildMessage(message, operation, entityName), cause);
        this.operation = operation;
        this.entityName = entityName;
    }

    private static String buildMessage(String message, String operation, String entityName) {
        StringBuilder sb = new StringBuilder();
        if (operation != null && entityName != null) {
            sb.append("Failed to ").append(operation).append(" ").append(entityName).append(": ");
        }
        sb.append(message);
        return sb.toString();
    }

    public String getOperation() {
        return operation;
    }

    public String getEntityName() {
        return entityName;
    }
}
