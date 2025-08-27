package perfumeshop.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Logging utility class for standardized logging across the application
 * @author PerfumeShop Team
 */
public class LoggingUtils {

    private static final String LOG_FILE = "perfume_shop.log";
    private static boolean initialized = false;

    /**
     * Initialize logging configuration
     */
    public static synchronized void initializeLogging() {
        if (initialized) {
            return;
        }

        try {
            // Create logger for the application
            Logger rootLogger = Logger.getLogger("perfumeshop");
            rootLogger.setLevel(Level.ALL);

            // Remove default handlers
            Logger.getLogger("").getHandlers();

            // Create console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());

            // Create file handler
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            // Add handlers to root logger
            Logger.getLogger("").addHandler(consoleHandler);
            Logger.getLogger("").addHandler(fileHandler);

            initialized = true;

        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
        }
    }

    /**
     * Get logger for a specific class
     * @param clazz Class to get logger for
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        initializeLogging();
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Log user action
     * @param logger Logger instance
     * @param request HTTP request
     * @param action Action performed
     * @param details Additional details
     */
    public static void logUserAction(Logger logger, HttpServletRequest request,
                                   String action, String details) {
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        logger.log(Level.INFO, "User Action - IP: {0}, Action: {1}, Details: {2}, UserAgent: {3}",
                  new Object[]{clientIP, action, details, userAgent});
    }

    /**
     * Log security event
     * @param logger Logger instance
     * @param request HTTP request
     * @param event Security event type
     * @param details Event details
     */
    public static void logSecurityEvent(Logger logger, HttpServletRequest request,
                                      String event, String details) {
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        logger.log(Level.WARNING, "Security Event - IP: {0}, Event: {1}, Details: {2}, UserAgent: {3}",
                  new Object[]{clientIP, event, details, userAgent});
    }

    /**
     * Log performance metrics
     * @param logger Logger instance
     * @param operation Operation name
     * @param startTime Start time
     * @param endTime End time
     */
    public static void logPerformance(Logger logger, String operation, long startTime, long endTime) {
        long duration = endTime - startTime;
        logger.log(Level.INFO, "Performance - Operation: {0}, Duration: {1}ms",
                  new Object[]{operation, duration});
    }

    /**
     * Log database operation
     * @param logger Logger instance
     * @param operation Database operation
     * @param table Table name
     * @param recordId Record ID (if applicable)
     */
    public static void logDatabaseOperation(Logger logger, String operation, String table, Object recordId) {
        if (recordId != null) {
            logger.log(Level.INFO, "Database - Operation: {0}, Table: {1}, RecordID: {2}",
                      new Object[]{operation, table, recordId});
        } else {
            logger.log(Level.INFO, "Database - Operation: {0}, Table: {1}",
                      new Object[]{operation, table});
        }
    }

    /**
     * Log error with context
     * @param logger Logger instance
     * @param request HTTP request (optional)
     * @param error Error message
     * @param exception Exception (optional)
     */
    public static void logError(Logger logger, HttpServletRequest request, String error, Exception exception) {
        String clientIP = request != null ? getClientIP(request) : "N/A";
        String requestURI = request != null ? request.getRequestURI() : "N/A";

        if (exception != null) {
            logger.log(Level.SEVERE, "Error - IP: {0}, URI: {1}, Message: {2}",
                      new Object[]{clientIP, requestURI, error});
            logger.log(Level.SEVERE, "Exception details", exception);
        } else {
            logger.log(Level.SEVERE, "Error - IP: {0}, URI: {1}, Message: {2}",
                      new Object[]{clientIP, requestURI, error});
        }
    }

    /**
     * Log business transaction
     * @param logger Logger instance
     * @param transactionType Type of transaction
     * @param amount Transaction amount
     * @param status Transaction status
     * @param details Additional details
     */
    public static void logBusinessTransaction(Logger logger, String transactionType,
                                            double amount, String status, String details) {
        logger.log(Level.INFO, "Business Transaction - Type: {0}, Amount: {1}, Status: {2}, Details: {3}",
                  new Object[]{transactionType, amount, status, details});
    }

    /**
     * Get client IP address from request
     * @param request HTTP request
     * @return Client IP address
     */
    private static String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * Log application startup
     * @param logger Logger instance
     */
    public static void logApplicationStart(Logger logger) {
        logger.log(Level.INFO, "===========================================");
        logger.log(Level.INFO, "PerfumeShop Application Started");
        logger.log(Level.INFO, "Timestamp: {0}", new java.util.Date());
        logger.log(Level.INFO, "Java Version: {0}", System.getProperty("java.version"));
        logger.log(Level.INFO, "===========================================");
    }

    /**
     * Log application shutdown
     * @param logger Logger instance
     */
    public static void logApplicationShutdown(Logger logger) {
        logger.log(Level.INFO, "===========================================");
        logger.log(Level.INFO, "PerfumeShop Application Shutting Down");
        logger.log(Level.INFO, "Timestamp: {0}", new java.util.Date());
        logger.log(Level.INFO, "===========================================");
    }
}
