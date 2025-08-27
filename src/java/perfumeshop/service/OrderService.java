package perfumeshop.service;

import perfumeshop.dal.OrderDAO;
import perfumeshop.dal.WalletDAO;
import perfumeshop.exception.DaoException;
import perfumeshop.exception.PaymentException;
import perfumeshop.exception.ValidationException;
import perfumeshop.model.Cart;
import perfumeshop.model.Order;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for order business logic
 * @author PerfumeShop Team
 */
public class OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private final OrderDAO orderDAO;
    private final WalletDAO walletDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.walletDAO = new WalletDAO();
    }

    /**
     * Process checkout with full validation and business logic
     * @param user Customer placing the order
     * @param cart Shopping cart
     * @return Order ID if successful
     * @throws ValidationException if validation fails
     * @throws PaymentException if payment fails
     * @throws DaoException if database operation fails
     */
    public int processCheckout(User user, Cart cart) {
        validateCheckoutInputs(user, cart);

        double totalAmount = cart.getFinalTotal();
        Wallet wallet = validateWalletAndBalance(user, totalAmount);

        LOGGER.log(Level.INFO, "Processing checkout for user {0}, amount: {1}",
                  new Object[]{user.getUserName(), totalAmount});

        // Get order count before processing
        int ordersBefore = orderDAO.getNumberOrders();

        try {
            // Process the order
            orderDAO.addOrder(user, cart);

            // Verify order was created
            int ordersAfter = orderDAO.getNumberOrders();
            if (ordersBefore >= ordersAfter) {
                throw new DaoException("Order creation verification failed", "verify", "order_creation");
            }

            // Update wallet balance
            updateWalletBalance(user, totalAmount);

            LOGGER.log(Level.INFO, "Checkout completed successfully for user: {0}", user.getUserName());
            return ordersAfter; // Return the new order count as reference

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Checkout failed for user: " + user.getUserName(), e);
            throw e; // Re-throw the original exception
        }
    }

    /**
     * Validate checkout inputs
     */
    private void validateCheckoutInputs(User user, Cart cart) {
        if (user == null) {
            throw new ValidationException("User cannot be null", "user");
        }
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Cart cannot be null or empty", "cart");
        }

        // Validate cart contents
        try {
            cart.validate();
        } catch (IllegalStateException e) {
            throw new ValidationException("Invalid cart contents: " + e.getMessage(), "cart");
        }

        // Validate user contact information
        if (!user.hasValidContactInfo()) {
            throw new ValidationException("User must have valid email or phone number", "user");
        }
    }

    /**
     * Validate wallet and check balance
     */
    private Wallet validateWalletAndBalance(User user, double totalAmount) {
        Wallet wallet = walletDAO.getWalletByUserName(user.getUserName());
        if (wallet == null) {
            throw new PaymentException("Wallet not found for user",
                                     PaymentException.PaymentErrorCode.GATEWAY_ERROR);
        }

        double balance = wallet.getBalance();
        if (balance < totalAmount) {
            LOGGER.log(Level.WARNING, "Insufficient balance for user {0}: required={1}, available={2}",
                      new Object[]{user.getUserName(), totalAmount, balance});
            throw new PaymentException("Insufficient account balance",
                                     PaymentException.PaymentErrorCode.INSUFFICIENT_BALANCE);
        }

        return wallet;
    }

    /**
     * Update wallet balance after successful payment
     */
    private void updateWalletBalance(User user, double amount) {
        try {
            walletDAO.deductionMoney(user.getUserName(), amount);
            LOGGER.log(Level.INFO, "Wallet updated for user {0}, amount deducted: {1}",
                      new Object[]{user.getUserName(), amount});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update wallet for user: " + user.getUserName(), e);
            throw new PaymentException("Failed to update wallet balance",
                                     PaymentException.PaymentErrorCode.GATEWAY_ERROR, e);
        }
    }

    /**
     * Get all orders
     * @return List of all orders
     */
    public List<Order> getAllOrders() {
        try {
            return orderDAO.getAllOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get all orders", e);
            throw new DaoException("Failed to retrieve orders", "retrieve", "orders", e);
        }
    }

    /**
     * Get order by ID
     * @param orderId Order ID
     * @return Order if found
     * @throws ValidationException if orderId is invalid
     */
    public Order getOrderById(int orderId) {
        if (orderId <= 0) {
            throw new ValidationException("Order ID must be positive", "orderId");
        }

        try {
            // For now, we'll use a workaround since getOrderById is not implemented in DAO
            // This is a demo implementation - in production, you'd implement this properly
            LOGGER.log(Level.INFO, "Attempting to get order by ID: {0}", orderId);

            // You could implement this by getting all orders and filtering,
            // but that's not efficient for production
            List<Order> allOrders = orderDAO.getAllOrders();
            for (Order order : allOrders) {
                if (order.getOrderId() == orderId) {
                    LOGGER.log(Level.INFO, "Order found: {0}", orderId);
                    return order;
                }
            }

            LOGGER.log(Level.WARNING, "Order not found: {0}", orderId);
            return null;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get order by ID: " + orderId, e);
            throw new DaoException("Failed to retrieve order", "retrieve", "order", e);
        }
    }

    /**
     * Update order status
     * @param orderId Order ID
     * @throws ValidationException if orderId is invalid
     * @throws DaoException if database operation fails
     */
    public void updateOrderStatus(int orderId) {
        if (orderId <= 0) {
            throw new ValidationException("Order ID must be positive", "orderId");
        }

        try {
            orderDAO.updateOrderStatus(orderId);
            LOGGER.log(Level.INFO, "Order status updated: {0}", orderId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update order status: " + orderId, e);
            throw new DaoException("Failed to update order status", "update", "order_status", e);
        }
    }

    /**
     * Get total money for month
     * @param month Month (1-12)
     * @param year Year
     * @return Total money
     */
    public double getTotalMoneyMonth(int month, int year) {
        try {
            return orderDAO.getTotalMoneyMonth(month, year);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get monthly total", e);
            throw new DaoException("Failed to calculate monthly total", "calculate", "monthly_total", e);
        }
    }

    /**
     * Get total money for week
     * @param dayOfWeek Day of week (1-7)
     * @param fromDay Start day
     * @param toDay End day
     * @param year Year
     * @param month Month
     * @return Total money
     */
    public double getTotalMoneyWeek(int dayOfWeek, int fromDay, int toDay, int year, int month) {
        try {
            return orderDAO.getTotalMoneyWeek(dayOfWeek, fromDay, toDay, year, month);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get weekly total", e);
            throw new DaoException("Failed to calculate weekly total", "calculate", "weekly_total", e);
        }
    }

    /**
     * Get total sum of all orders
     * @return Total sum
     */
    public double getTotalSumAllOrders() {
        try {
            return orderDAO.getTotalSumAllOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get total sum", e);
            throw new DaoException("Failed to calculate total sum", "calculate", "total_sum", e);
        }
    }

    /**
     * Get total number of orders
     * @return Total count
     */
    public int getTotalOrderCount() {
        try {
            return orderDAO.getNumberOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get order count", e);
            throw new DaoException("Failed to get order count", "count", "orders", e);
        }
    }

    /**
     * Validate order data
     * @param order Order to validate
     * @throws ValidationException if validation fails
     */
    public void validateOrder(Order order) {
        if (order == null) {
            throw new ValidationException("Order cannot be null", "order");
        }

        if (order.getUserName() == null || order.getUserName().trim().isEmpty()) {
            throw new ValidationException("Order must have a valid username", "userName");
        }

        if (order.getTotal() < 0) {
            throw new ValidationException("Order total cannot be negative", "total");
        }

        if (order.getTotal() > 10000) {
            throw new ValidationException("Order total exceeds maximum limit", "total");
        }

        if (order.getDate() == null) {
            throw new ValidationException("Order must have a valid date", "date");
        }

        // Validate date is not in the future
        if (order.getDate().isAfter(java.time.LocalDateTime.now())) {
            throw new ValidationException("Order date cannot be in the future", "date");
        }

        LOGGER.log(Level.INFO, "Order validation passed for: {0}", order.getUserName());
    }

    /**
     * Get orders by user
     * @param userName Username
     * @return List of orders for the user
     * @throws ValidationException if userName is invalid
     */
    public List<Order> getOrdersByUser(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new ValidationException("Username cannot be null or empty", "userName");
        }

        try {
            // Note: This would require a new DAO method - for now use workaround
            List<Order> allOrders = orderDAO.getAllOrders();
            List<Order> userOrders = new ArrayList<>();

            for (Order order : allOrders) {
                if (userName.equals(order.getUserName())) {
                    userOrders.add(order);
                }
            }

            LOGGER.log(Level.INFO, "Found {0} orders for user: {1}",
                      new Object[]{userOrders.size(), userName});
            return userOrders;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get orders for user: " + userName, e);
            throw new DaoException("Failed to retrieve user orders", "retrieve", "user_orders", e);
        }
    }

    /**
     * Cancel order (soft delete)
     * @param orderId Order ID to cancel
     * @param userName Username requesting cancellation
     * @throws ValidationException if parameters are invalid
     * @throws DaoException if database operation fails
     */
    public void cancelOrder(int orderId, String userName) {
        if (orderId <= 0) {
            throw new ValidationException("Order ID must be positive", "orderId");
        }
        if (userName == null || userName.trim().isEmpty()) {
            throw new ValidationException("Username cannot be null or empty", "userName");
        }

        try {
            // Verify order belongs to user before canceling
            Order order = getOrderById(orderId);
            if (order == null) {
                throw new ValidationException("Order not found", "orderId");
            }

            if (!userName.equals(order.getUserName())) {
                throw new ValidationException("Order does not belong to user", "userName");
            }

            // Update order status to cancelled
            orderDAO.updateOrderStatus(orderId); // You might need to modify this method

            LOGGER.log(Level.INFO, "Order cancelled successfully: {0} by user: {1}",
                      new Object[]{orderId, userName});

        } catch (ValidationException e) {
            throw e; // Re-throw validation exceptions
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to cancel order: " + orderId, e);
            throw new DaoException("Failed to cancel order", "update", "order_cancel", e);
        }
    }

    /**
     * Get order statistics
     * @return Map containing order statistics
     */
    public java.util.Map<String, Object> getOrderStatistics() {
        try {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalOrders", getTotalOrderCount());
            stats.put("totalRevenue", getTotalSumAllOrders());

            // Get current month revenue
            int currentMonth = java.time.LocalDateTime.now().getMonthValue();
            int currentYear = java.time.LocalDateTime.now().getYear();
            stats.put("currentMonthRevenue", getTotalMoneyMonth(currentMonth, currentYear));

            LOGGER.log(Level.INFO, "Order statistics retrieved successfully");
            return stats;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get order statistics", e);
            throw new DaoException("Failed to calculate order statistics", "calculate", "statistics", e);
        }
    }
}
