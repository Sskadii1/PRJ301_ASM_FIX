package perfumeshop.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Simple caching utility with TTL (Time To Live) support
 * @author PerfumeShop Team
 */
public class CacheUtils {

    private static final Logger LOGGER = LoggingUtils.getLogger(CacheUtils.class);

    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    static {
        // Schedule cleanup task every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(CacheUtils::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
        LOGGER.log(Level.INFO, "Cache cleanup scheduler started");
    }

    /**
     * Cache entry with expiration time
     */
    private static class CacheEntry {
        private final Object value;
        private final long expirationTime;

        public CacheEntry(Object value, long ttlMillis) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + ttlMillis;
        }

        public Object getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    /**
     * Put value in cache with TTL
     * @param key Cache key
     * @param value Value to cache
     * @param ttlMillis Time to live in milliseconds
     */
    public static void put(String key, Object value, long ttlMillis) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }

        cache.put(key, new CacheEntry(value, ttlMillis));
        LOGGER.log(Level.FINE, "Cached value for key: {0}, TTL: {1}ms", new Object[]{key, ttlMillis});
    }

    /**
     * Put value in cache with default TTL (10 minutes)
     * @param key Cache key
     * @param value Value to cache
     */
    public static void put(String key, Object value) {
        put(key, value, TimeUnit.MINUTES.toMillis(10));
    }

    /**
     * Get value from cache
     * @param key Cache key
     * @return Cached value or null if not found or expired
     */
    public static Object get(String key) {
        if (key == null) {
            return null;
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            LOGGER.log(Level.FINE, "Expired cache entry removed for key: {0}", key);
            return null;
        }

        LOGGER.log(Level.FINE, "Cache hit for key: {0}", key);
        return entry.getValue();
    }

    /**
     * Get value from cache with type casting
     * @param <T> Type of the cached value
     * @param key Cache key
     * @param type Class of the expected type
     * @return Cached value or null if not found, expired, or wrong type
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> type) {
        Object value = get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Remove value from cache
     * @param key Cache key
     * @return true if value was removed
     */
    public static boolean remove(String key) {
        CacheEntry removed = cache.remove(key);
        if (removed != null) {
            LOGGER.log(Level.FINE, "Cache entry removed for key: {0}", key);
            return true;
        }
        return false;
    }

    /**
     * Clear all cache entries
     */
    public static void clear() {
        int size = cache.size();
        cache.clear();
        LOGGER.log(Level.INFO, "Cache cleared, removed {0} entries", size);
    }

    /**
     * Check if key exists in cache and is not expired
     * @param key Cache key
     * @return true if key exists and is valid
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Get cache size
     * @return Number of entries in cache
     */
    public static int size() {
        return cache.size();
    }

    /**
     * Get cache statistics
     * @return Cache statistics as string
     */
    public static String getStatistics() {
        return String.format("Cache Statistics - Size: %d", cache.size());
    }

    /**
     * Cleanup expired entries
     */
    private static void cleanupExpiredEntries() {
        int originalSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removedCount = originalSize - cache.size();

        if (removedCount > 0) {
            LOGGER.log(Level.INFO, "Cache cleanup completed, removed {0} expired entries", removedCount);
        }
    }

    /**
     * Shutdown the cache system
     */
    public static void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.log(Level.INFO, "Cache system shutdown");
    }
}
