package perfumeshop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Order entity representing a customer's purchase order
 * @author PerfumeShop Team
 */
public class Order {

    private int orderId;
    private String userName;
    private double total;
    private LocalDateTime date;
    private boolean status;

    // Constructors
    public Order() {
        this.date = LocalDateTime.now();
        this.status = false;
    }

    public Order(int orderId, LocalDateTime date, String userName, double total, boolean status) {
        this.orderId = orderId;
        this.userName = userName;
        this.total = total;
        this.date = date != null ? date : LocalDateTime.now();
        this.status = status;
    }

    // Legacy constructor for backward compatibility
    public Order(int orderId, java.util.Date date, String userName, double total, boolean status) {
        this.orderId = orderId;
        this.userName = userName;
        this.total = total;
        this.date = date != null ? date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                                : LocalDateTime.now();
        this.status = status;
    }

    // Getters and Setters with validation
    public int getOrderId() {
        return orderId;
    }

    // Alias method for backward compatibility
    public int getId() {
        return getOrderId();
    }

    public void setOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        this.orderId = orderId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.userName = userName.trim();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        if (total < 0) {
            throw new IllegalArgumentException("Total cannot be negative");
        }
        this.total = Math.round(total * 100.0) / 100.0; // Round to 2 decimal places
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date != null ? date : LocalDateTime.now();
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    // Business methods
    public boolean isCompleted() {
        return status;
    }

    public void markAsCompleted() {
        this.status = true;
    }

    public void markAsPending() {
        this.status = false;
    }

    public String getFormattedTotal() {
        return String.format("$%.2f", total);
    }

    public String getFormattedDate() {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getStatusText() {
        return status ? "Completed" : "Pending";
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Order order = (Order) obj;
        return orderId == order.orderId &&
               Objects.equals(userName, order.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userName);
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, user='%s', total=%.2f, date='%s', status='%s'}",
                           orderId, userName, total, getFormattedDate(), getStatusText());
    }
}
