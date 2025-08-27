package perfumeshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Simple database connection pool implementation
 * In production, consider using HikariCP, Apache Commons DBCP, or similar
 * @author PerfumeShop Team
 */
public class ConnectionPoolUtils {

    private static final Logger LOGGER = LoggingUtils.getLogger(ConnectionPoolUtils.class);

    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_MIN_CONNECTIONS = 2;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds

    private static ConnectionPoolUtils instance;
    private final ConcurrentLinkedQueue<Connection> availableConnections;
    private final ConcurrentLinkedQueue<Connection> usedConnections;
    private final AtomicInteger connectionCount;
    private final ConnectionConfig config;

    /**
     * Database connection configuration
     */
    public static class ConnectionConfig {
        private String url;
        private String username;
        private String password;
        private int maxConnections = DEFAULT_MAX_CONNECTIONS;
        private int minConnections = DEFAULT_MIN_CONNECTIONS;

        public ConnectionConfig(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        // Getters and setters
        public String getUrl() { return url; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public int getMaxConnections() { return maxConnections; }
        public int getMinConnections() { return minConnections; }

        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public void setMinConnections(int minConnections) { this.minConnections = minConnections; }
    }

    /**
     * Private constructor for singleton
     */
    private ConnectionPoolUtils(ConnectionConfig config) {
        this.config = config;
        this.availableConnections = new ConcurrentLinkedQueue<>();
        this.usedConnections = new ConcurrentLinkedQueue<>();
        this.connectionCount = new AtomicInteger(0);

        initializePool();
        LOGGER.log(Level.INFO, "Connection pool initialized with max {0} connections",
                  config.getMaxConnections());
    }

    /**
     * Get singleton instance
     * @param config Database configuration
     * @return ConnectionPoolUtils instance
     */
    public static synchronized ConnectionPoolUtils getInstance(ConnectionConfig config) {
        if (instance == null) {
            instance = new ConnectionPoolUtils(config);
        }
        return instance;
    }

    /**
     * Initialize connection pool
     */
    private void initializePool() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Create minimum number of connections
            for (int i = 0; i < config.getMinConnections(); i++) {
                availableConnections.offer(createConnection());
            }

        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Database driver not found", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    /**
     * Create a new database connection
     * @return New database connection
     */
    private Connection createConnection() throws RuntimeException {
        try {
            Connection connection = DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
            );

            connectionCount.incrementAndGet();
            LOGGER.log(Level.FINE, "Created new database connection. Total: {0}",
                      connectionCount.get());

            return connection;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database connection", e);
            throw new RuntimeException("Failed to create database connection", e);
        }
    }

    /**
     * Get a connection from the pool
     * @return Database connection
     * @throws SQLException if unable to get connection
     */
    public Connection getConnection() throws SQLException {
        Connection connection = availableConnections.poll();

        if (connection == null) {
            // No available connections, try to create a new one
            if (connectionCount.get() < config.getMaxConnections()) {
                connection = createConnection();
            } else {
                // Wait for an available connection
                long startTime = System.currentTimeMillis();
                while (connection == null && (System.currentTimeMillis() - startTime) < CONNECTION_TIMEOUT) {
                    try {
                        Thread.sleep(10);
                        connection = availableConnections.poll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Connection request interrupted", e);
                    }
                }

                if (connection == null) {
                    throw new SQLException("Connection timeout - no available connections");
                }
            }
        }

        // Validate connection
        if (!isValidConnection(connection)) {
            LOGGER.log(Level.WARNING, "Invalid connection detected, creating new one");
            connection = createConnection();
        }

        usedConnections.offer(connection);
        return connection;
    }

    /**
     * Return connection to the pool
     * @param connection Connection to return
     */
    public void returnConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        usedConnections.remove(connection);

        try {
            if (isValidConnection(connection)) {
                availableConnections.offer(connection);
                LOGGER.log(Level.FINE, "Connection returned to pool");
            } else {
                LOGGER.log(Level.WARNING, "Invalid connection returned, closing");
                connection.close();
                connectionCount.decrementAndGet();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error while returning connection to pool", e);
            try {
                connection.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error closing invalid connection", ex);
            }
            connectionCount.decrementAndGet();
        }
    }

    /**
     * Validate if connection is still valid
     * @param connection Connection to validate
     * @return true if valid
     */
    private boolean isValidConnection(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Close all connections and shutdown pool
     */
    public void shutdown() {
        LOGGER.log(Level.INFO, "Shutting down connection pool");

        // Close available connections
        Connection connection;
        while ((connection = availableConnections.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection during shutdown", e);
            }
        }

        // Close used connections (they should be returned first)
        while ((connection = usedConnections.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing used connection during shutdown", e);
            }
        }

        connectionCount.set(0);
        LOGGER.log(Level.INFO, "Connection pool shutdown complete");
    }

    /**
     * Get pool statistics
     * @return Pool statistics as string
     */
    public String getPoolStatistics() {
        return String.format("Connection Pool Stats - Total: %d, Available: %d, Used: %d, Max: %d",
                           connectionCount.get(),
                           availableConnections.size(),
                           usedConnections.size(),
                           config.getMaxConnections());
    }

    /**
     * Get current pool size
     * @return Current number of connections
     */
    public int getPoolSize() {
        return connectionCount.get();
    }

    /**
     * Get number of available connections
     * @return Number of available connections
     */
    public int getAvailableConnections() {
        return availableConnections.size();
    }

    /**
     * Get number of used connections
     * @return Number of used connections
     */
    public int getUsedConnections() {
        return usedConnections.size();
    }
}
