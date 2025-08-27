package perfumeshop.dal;

import perfumeshop.utils.DBContext;
import perfumeshop.exception.DaoException;
import perfumeshop.exception.ValidationException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import perfumeshop.model.Cart;
import perfumeshop.model.Item;
import perfumeshop.model.Order;
import perfumeshop.model.User;

/**
 * Data Access Object for Order entity with improved transaction management and error handling
 * @author PerfumeShop Team
 */
public class OrderDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());

    // SQL Constants
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) FROM Orders";
    private static final String INSERT_ORDER_SQL =
        "INSERT INTO [dbo].[Orders] ([Date], [UserName], [TotalMoney], [status]) VALUES (?, ?, ?, ?)";
    private static final String GET_LAST_ORDER_ID_SQL = "SELECT TOP 1 [OrderID] FROM [dbo].[Orders] ORDER BY [OrderID] DESC";
    private static final String INSERT_ORDER_DETAIL_SQL =
        "INSERT INTO [dbo].[OrderDetails] ([OrderID], [ProductID], [Quantity], [UnitPrice], [Discount]) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_PRODUCT_QUANTITY_SQL =
        "UPDATE Products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
    private static final String TOTAL_MONEY_MONTH_SQL =
        "SELECT SUM([TotalMoney]) FROM [Orders] WHERE MONTH([Date]) = ? AND YEAR([Date]) = ?";
    private static final String TOTAL_MONEY_WEEK_SQL =
        "SELECT SUM(TotalMoney) FROM Orders WHERE DAY([Date]) BETWEEN ? AND ? AND MONTH([Date]) = ? AND YEAR([Date]) = ? AND DATEPART(dw, [Date]) = ?";
    private static final String TOTAL_ALL_MONEY_SQL = "SELECT SUM([TotalMoney]) FROM Orders";
    private static final String SELECT_ALL_ORDERS_SQL = "SELECT * FROM Orders ORDER BY status ASC";
    private static final String UPDATE_STATUS_SQL = "UPDATE [dbo].[Orders] SET [status] = ? WHERE [OrderID] = ?";

    /**
     * Get total number of orders in the system
     * @return Total number of orders
     */
    public int getNumberOrders() {
        LOGGER.log(Level.INFO, "Getting total number of orders");

        try (PreparedStatement st = connection.prepareStatement(COUNT_ORDERS_SQL);
             ResultSet rs = st.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt(1);
                LOGGER.log(Level.INFO, "Found {0} orders", count);
                return count;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting order count", e);
            throw new DaoException("Failed to get order count", "count", "orders", e);
        }

        return 0;
    }

    /**
     * Add a new order with transaction management
     * @param user Customer placing the order
     * @param cart Shopping cart containing items
     * @throws DaoException if database operation fails
     * @throws ValidationException if input validation fails
     */
    public void addOrder(User user, Cart cart) {
        // Validate inputs
        if (user == null) {
            throw new ValidationException("User cannot be null", "user");
        }
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Cart cannot be null or empty", "cart");
        }

        // Validate cart contents
        cart.validate();

        LOGGER.log(Level.INFO, "Creating order for user: {0}, cart items: {1}",
                  new Object[]{user.getUserName(), cart.getProductCount()});

        Connection conn = null;
        try {
            conn = connection;
            conn.setAutoCommit(false); // Start transaction

            LocalDateTime orderDate = LocalDateTime.now();

            // Insert order
            int orderId = insertOrder(conn, user, cart, orderDate);

            // Insert order details
            insertOrderDetails(conn, orderId, cart);

            // Update product quantities
            updateProductQuantities(conn, cart);

            conn.commit(); // Commit transaction
            LOGGER.log(Level.INFO, "Order created successfully with ID: {0}", orderId);

        } catch (SQLException e) {
            rollbackTransaction(conn);
            LOGGER.log(Level.SEVERE, "Database error while creating order", e);
            throw new DaoException("Failed to create order due to database error", "create", "order", e);
        } catch (Exception e) {
            rollbackTransaction(conn);
            LOGGER.log(Level.SEVERE, "Unexpected error while creating order", e);
            throw new DaoException("Unexpected error while creating order", "create", "order", e);
        } finally {
            restoreAutoCommit(conn);
        }
    }

    /**
     * Insert order record
     */
    private int insertOrder(Connection conn, User user, Cart cart, LocalDateTime orderDate) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(INSERT_ORDER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, orderDate.toLocalDate().toString());
            st.setString(2, user.getUserName());
            st.setDouble(3, cart.getTotalMoney());
            st.setInt(4, 0); // Status: pending

            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = st.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Insert order details
     */
    private void insertOrderDetails(Connection conn, int orderId, Cart cart) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(INSERT_ORDER_DETAIL_SQL)) {
            for (Item item : cart.getListItems()) {
                st.setInt(1, orderId);
                st.setInt(2, item.getProduct().getId());
                st.setInt(3, item.getQuantity());
                st.setDouble(4, item.getProduct().getPrice());
                st.setDouble(5, item.getProduct().getDiscount());

                st.executeUpdate();
            }
        }
    }

    /**
     * Update product quantities after order
     */
    private void updateProductQuantities(Connection conn, Cart cart) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(UPDATE_PRODUCT_QUANTITY_SQL)) {
            for (Item item : cart.getListItems()) {
                st.setInt(1, item.getQuantity());
                st.setInt(2, item.getProduct().getId());
                st.setInt(3, item.getQuantity()); // Ensure sufficient quantity

                int affectedRows = st.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Insufficient quantity for product: " + item.getProduct().getName());
                }
            }
        }
    }

    /**
     * Rollback transaction on error
     */
    private void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.log(Level.WARNING, "Transaction rolled back");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error during rollback", e);
            }
        }
    }

    /**
     * Restore auto-commit mode
     */
    private void restoreAutoCommit(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error restoring auto-commit", e);
            }
        }
    }

    /**
     * Get total money for a specific month and year
     * @param month Month (1-12)
     * @param year Year
     * @return Total money for the period
     * @throws ValidationException if month/year is invalid
     * @throws DaoException if database operation fails
     */
    public double getTotalMoneyMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12", "month");
        }
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year must be reasonable", "year");
        }

        LOGGER.log(Level.INFO, "Getting total money for {0}/{1}", new Object[]{month, year});

        try (PreparedStatement st = connection.prepareStatement(TOTAL_MONEY_MONTH_SQL)) {
            st.setInt(1, month);
            st.setInt(2, year);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    LOGGER.log(Level.INFO, "Total money for {0}/{1}: {2}",
                              new Object[]{month, year, total});
                    return total;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting monthly total", e);
            throw new DaoException("Failed to get monthly total", "calculate", "monthly_total", e);
        }

        return 0.0;
    }

    /**
     * Get total money for a specific week
     * @param dayOfWeek Day of week (1=Sunday, 7=Saturday)
     * @param fromDay Start day of week period
     * @param toDay End day of week period
     * @param year Year
     * @param month Month
     * @return Total money for the week
     * @throws ValidationException if parameters are invalid
     * @throws DaoException if database operation fails
     */
    public double getTotalMoneyWeek(int dayOfWeek, int fromDay, int toDay, int year, int month) {
        // Validate inputs
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new ValidationException("Day of week must be between 1 and 7", "dayOfWeek");
        }
        if (fromDay < 1 || fromDay > 31 || toDay < 1 || toDay > 31) {
            throw new ValidationException("Day must be between 1 and 31", "day");
        }
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12", "month");
        }
        if (year < 2000 || year > 2100) {
            throw new ValidationException("Year must be reasonable", "year");
        }

        LOGGER.log(Level.INFO, "Getting weekly total for day {0}, period {1}-{2}/{3}/{4}",
                  new Object[]{dayOfWeek, fromDay, toDay, month, year});

        try (PreparedStatement st = connection.prepareStatement(TOTAL_MONEY_WEEK_SQL)) {
            st.setInt(1, fromDay);
            st.setInt(2, toDay);
            st.setInt(3, month);
            st.setInt(4, year);
            st.setInt(5, dayOfWeek);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    LOGGER.log(Level.INFO, "Weekly total: {0}", total);
                    return total;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting weekly total", e);
            throw new DaoException("Failed to get weekly total", "calculate", "weekly_total", e);
        }

        return 0.0;
    }

    /**
     * Get total sum of all orders
     * @return Total sum of all orders
     */
    public double getTotalSumAllOrders() {
        LOGGER.log(Level.INFO, "Getting total sum of all orders");

        try (PreparedStatement st = connection.prepareStatement(TOTAL_ALL_MONEY_SQL);
             ResultSet rs = st.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble(1);
                LOGGER.log(Level.INFO, "Total sum of all orders: {0}", total);
                return total;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total sum", e);
            throw new DaoException("Failed to get total sum", "calculate", "total_sum", e);
        }

        return 0.0;
    }

    /**
     * Get all orders sorted by status
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        LOGGER.log(Level.INFO, "Getting all orders");

        List<Order> orders = new ArrayList<>();

        try (PreparedStatement st = connection.prepareStatement(SELECT_ALL_ORDERS_SQL);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("OrderID"));
                order.setDate(rs.getTimestamp("Date").toLocalDateTime());
                order.setUserName(rs.getString("UserName"));
                order.setTotal(rs.getDouble("TotalMoney"));
                order.setStatus(rs.getBoolean("status"));

                orders.add(order);
            }

            LOGGER.log(Level.INFO, "Retrieved {0} orders", orders.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all orders", e);
            throw new DaoException("Failed to get all orders", "retrieve", "orders", e);
        }

        return orders;
    }

    /**
     * Update order status
     * @param orderId Order ID to update
     * @throws ValidationException if orderId is invalid
     * @throws DaoException if database operation fails
     */
    public void updateOrderStatus(int orderId) {
        if (orderId <= 0) {
            throw new ValidationException("Order ID must be positive", "orderId");
        }

        LOGGER.log(Level.INFO, "Updating status for order ID: {0}", orderId);

        try (PreparedStatement st = connection.prepareStatement(UPDATE_STATUS_SQL)) {
            st.setInt(1, orderId);

            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new DaoException("Order not found with ID: " + orderId, "update", "order");
            }

            LOGGER.log(Level.INFO, "Order status updated successfully for ID: {0}", orderId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order status", e);
            throw new DaoException("Failed to update order status", "update", "order_status", e);
        }
    }

    // Simple fallback methods for compatibility
    public double sumAllMoneyOrder() {
        return getTotalSumAllOrders();
    }

    public List<Order> getAll() {
        return getAllOrders();
    }
}
