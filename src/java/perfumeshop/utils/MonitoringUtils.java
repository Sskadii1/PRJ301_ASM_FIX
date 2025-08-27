package perfumeshop.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Application monitoring and metrics utility
 * @author PerfumeShop Team
 */
public class MonitoringUtils {

    private static final Logger LOGGER = LoggingUtils.getLogger(MonitoringUtils.class);

    // Metrics counters
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong successfulRequests = new AtomicLong(0);
    private static final AtomicLong failedRequests = new AtomicLong(0);
    private static final AtomicLong totalOrders = new AtomicLong(0);
    private static final AtomicLong totalRevenue = new AtomicLong(0);

    // Performance metrics
    private static final Map<String, Long> operationStartTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> operationCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> operationTotalTime = new ConcurrentHashMap<>();

    // Error tracking
    private static final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();

    /**
     * Record a new request
     */
    public static void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * Record a successful request
     */
    public static void recordSuccessfulRequest() {
        successfulRequests.incrementAndGet();
    }

    /**
     * Record a failed request
     */
    public static void recordFailedRequest() {
        failedRequests.incrementAndGet();
    }

    /**
     * Record a new order
     * @param amount Order amount
     */
    public static void recordOrder(double amount) {
        totalOrders.incrementAndGet();
        totalRevenue.addAndGet((long)(amount * 100)); // Store in cents to avoid floating point issues
    }

    /**
     * Start timing an operation
     * @param operationName Operation name
     * @return Operation ID for later reference
     */
    public static String startOperation(String operationName) {
        String operationId = operationName + "_" + System.nanoTime();
        operationStartTimes.put(operationId, System.nanoTime());
        return operationId;
    }

    /**
     * End timing an operation
     * @param operationId Operation ID from startOperation
     * @param operationName Operation name
     */
    public static void endOperation(String operationId, String operationName) {
        Long startTime = operationStartTimes.remove(operationId);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;

            // Update operation metrics
            operationCounts.merge(operationName, 1L, Long::sum);
            operationTotalTime.merge(operationName, duration, Long::sum);

            // Log slow operations
            long durationMs = duration / 1_000_000;
            if (durationMs > 1000) { // Log operations taking more than 1 second
                LOGGER.log(Level.WARNING, "Slow operation detected - Name: {0}, Duration: {1}ms",
                          new Object[]{operationName, durationMs});
            }
        }
    }

    /**
     * Record an error
     * @param errorType Error type
     */
    public static void recordError(String errorType) {
        errorCounts.merge(errorType, 1, Integer::sum);
    }

    /**
     * Get application metrics
     * @return Metrics as formatted string
     */
    public static String getMetrics() {
        StringBuilder metrics = new StringBuilder();
        metrics.append("=== Application Metrics ===\n");
        metrics.append(String.format("Total Requests: %d\n", totalRequests.get()));
        metrics.append(String.format("Successful Requests: %d\n", successfulRequests.get()));
        metrics.append(String.format("Failed Requests: %d\n", failedRequests.get()));
        metrics.append(String.format("Success Rate: %.2f%%\n", getSuccessRate()));
        metrics.append(String.format("Total Orders: %d\n", totalOrders.get()));
        metrics.append(String.format("Total Revenue: $%.2f\n", getTotalRevenue()));

        if (!operationCounts.isEmpty()) {
            metrics.append("\n=== Performance Metrics ===\n");
            for (Map.Entry<String, Long> entry : operationCounts.entrySet()) {
                String operationName = entry.getKey();
                long count = entry.getValue();
                long totalTime = operationTotalTime.getOrDefault(operationName, 0L);
                double avgTime = count > 0 ? (totalTime / (double)count) / 1_000_000 : 0;

                metrics.append(String.format("%s: %d calls, avg %.2fms\n",
                           operationName, count, avgTime));
            }
        }

        if (!errorCounts.isEmpty()) {
            metrics.append("\n=== Error Summary ===\n");
            for (Map.Entry<String, Integer> entry : errorCounts.entrySet()) {
                metrics.append(String.format("%s: %d occurrences\n",
                           entry.getKey(), entry.getValue()));
            }
        }

        return metrics.toString();
    }

    /**
     * Get success rate percentage
     * @return Success rate
     */
    public static double getSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 100.0;

        return (successfulRequests.get() * 100.0) / total;
    }

    /**
     * Get total revenue
     * @return Total revenue
     */
    public static double getTotalRevenue() {
        return totalRevenue.get() / 100.0; // Convert back from cents
    }

    /**
     * Reset all metrics (useful for testing)
     */
    public static void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalOrders.set(0);
        totalRevenue.set(0);
        operationStartTimes.clear();
        operationCounts.clear();
        operationTotalTime.clear();
        errorCounts.clear();

        LOGGER.log(Level.INFO, "Application metrics reset");
    }

    /**
     * Health check
     * @return true if application is healthy
     */
    public static boolean isHealthy() {
        // Basic health checks
        double successRate = getSuccessRate();

        // Consider unhealthy if success rate is below 90%
        return successRate >= 90.0;
    }

    /**
     * Get health status
     * @return Health status string
     */
    public static String getHealthStatus() {
        boolean healthy = isHealthy();
        double successRate = getSuccessRate();

        return String.format("Status: %s, Success Rate: %.2f%%, Orders: %d, Revenue: $%.2f",
                           healthy ? "HEALTHY" : "UNHEALTHY",
                           successRate,
                           totalOrders.get(),
                           getTotalRevenue());
    }

    /**
     * Log current metrics
     */
    public static void logCurrentMetrics() {
        LOGGER.log(Level.INFO, "Current Application Metrics:\n{0}", getMetrics());
    }
}
