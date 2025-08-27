package perfumeshop.utils;

import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Performance monitoring utilities
 * @author PerfumeShop Team
 */
public class PerformanceUtils {

    private static final Logger LOGGER = LoggingUtils.getLogger(PerformanceUtils.class);

    /**
     * Measure execution time of a Runnable
     * @param operationName Name of the operation
     * @param runnable Code to execute
     */
    public static void measureExecutionTime(String operationName, Runnable runnable) {
        long startTime = System.nanoTime();
        try {
            runnable.run();
        } finally {
            long endTime = System.nanoTime();
            logExecutionTime(operationName, startTime, endTime);
        }
    }

    /**
     * Measure execution time of a Supplier and return its result
     * @param <T> Return type
     * @param operationName Name of the operation
     * @param supplier Code to execute
     * @return Result of the supplier
     */
    public static <T> T measureExecutionTime(String operationName, Supplier<T> supplier) {
        long startTime = System.nanoTime();
        try {
            T result = supplier.get();
            return result;
        } finally {
            long endTime = System.nanoTime();
            logExecutionTime(operationName, startTime, endTime);
        }
    }

    /**
     * Log execution time with performance monitoring
     */
    private static void logExecutionTime(String operationName, long startTime, long endTime) {
        long durationNano = endTime - startTime;
        long durationMs = durationNano / 1_000_000;

        // Update monitoring metrics
        MonitoringUtils.endOperation(operationName + "_perf", operationName);

        // Log performance information
        if (durationMs > 1000) {
            LOGGER.log(Level.WARNING, "Slow operation: {0} took {1}ms",
                      new Object[]{operationName, durationMs});
        } else if (durationMs > 100) {
            LOGGER.log(Level.INFO, "Operation: {0} took {1}ms",
                      new Object[]{operationName, durationMs});
        } else {
            LOGGER.log(Level.FINE, "Operation: {0} took {1}ms",
                      new Object[]{operationName, durationMs});
        }
    }

    /**
     * Create a performance-monitored wrapper for database operations
     * @param operationName Operation name
     * @return Performance monitoring ID
     */
    public static String startDatabaseOperation(String operationName) {
        return MonitoringUtils.startOperation(operationName + "_db");
    }

    /**
     * End database operation monitoring
     * @param operationId Operation ID from startDatabaseOperation
     * @param operationName Operation name
     */
    public static void endDatabaseOperation(String operationId, String operationName) {
        MonitoringUtils.endOperation(operationId, operationName + "_db");
    }

    /**
     * Create a performance-monitored wrapper for service operations
     * @param operationName Operation name
     * @return Performance monitoring ID
     */
    public static String startServiceOperation(String operationName) {
        return MonitoringUtils.startOperation(operationName + "_service");
    }

    /**
     * End service operation monitoring
     * @param operationId Operation ID from startServiceOperation
     * @param operationName Operation name
     */
    public static void endServiceOperation(String operationId, String operationName) {
        MonitoringUtils.endOperation(operationId, operationName + "_service");
    }

    /**
     * Performance monitoring aspect for methods
     * Usage: @Around("@annotation(perfumeshop.annotation.PerformanceMonitor)")
     * This would require AspectJ or similar AOP framework
     */
    public static void monitorMethod(String className, String methodName, Runnable method) {
        String operationName = className + "." + methodName;
        String operationId = MonitoringUtils.startOperation(operationName);

        try {
            method.run();
        } finally {
            MonitoringUtils.endOperation(operationId, operationName);
        }
    }

    /**
     * Monitor memory usage
     * @param operationName Operation name
     */
    public static void logMemoryUsage(String operationName) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        LOGGER.log(Level.INFO, "Memory usage for {0} - Used: {1}MB, Free: {2}MB, Total: {3}MB",
                  new Object[]{operationName,
                              usedMemory / (1024 * 1024),
                              freeMemory / (1024 * 1024),
                              totalMemory / (1024 * 1024)});
    }

    /**
     * Check if system has sufficient memory
     * @param requiredMB Required memory in MB
     * @return true if sufficient memory available
     */
    public static boolean hasSufficientMemory(long requiredMB) {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long freeMemoryMB = freeMemory / (1024 * 1024);

        boolean sufficient = freeMemoryMB >= requiredMB;
        if (!sufficient) {
            LOGGER.log(Level.WARNING, "Insufficient memory - Required: {0}MB, Available: {1}MB",
                      new Object[]{requiredMB, freeMemoryMB});
        }

        return sufficient;
    }

    /**
     * Force garbage collection (use with caution)
     */
    public static void forceGarbageCollection() {
        LOGGER.log(Level.INFO, "Forcing garbage collection");
        System.gc();

        // Wait a bit for GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get system performance metrics
     * @return Performance metrics as formatted string
     */
    public static String getSystemPerformanceMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        StringBuilder metrics = new StringBuilder();
        metrics.append("=== System Performance Metrics ===\n");
        metrics.append(String.format("Processors: %d\n", runtime.availableProcessors()));
        metrics.append(String.format("Total Memory: %dMB\n", totalMemory / (1024 * 1024)));
        metrics.append(String.format("Used Memory: %dMB\n", usedMemory / (1024 * 1024)));
        metrics.append(String.format("Free Memory: %dMB\n", freeMemory / (1024 * 1024)));
        metrics.append(String.format("Max Memory: %dMB\n", maxMemory / (1024 * 1024)));
        metrics.append(String.format("Memory Usage: %.2f%%\n",
                   (usedMemory * 100.0) / maxMemory));

        return metrics.toString();
    }

    /**
     * Performance-optimized sleep method
     * @param millis Milliseconds to sleep
     */
    public static void optimizedSleep(long millis) {
        if (millis <= 0) {
            return;
        }

        long startTime = System.currentTimeMillis();
        long remaining = millis;

        while (remaining > 0) {
            try {
                Thread.sleep(Math.min(remaining, 100)); // Sleep in smaller chunks
                remaining = millis - (System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
