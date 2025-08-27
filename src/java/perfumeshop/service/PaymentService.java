package perfumeshop.service;

import perfumeshop.exception.PaymentException;
import perfumeshop.exception.ValidationException;
import perfumeshop.model.Cart;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;
import perfumeshop.model.Item;
import perfumeshop.model.Product;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service layer for payment business logic
 * @author PerfumeShop Team
 */
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    /**
     * Validate payment before processing
     * @param user Customer
     * @param cart Shopping cart
     * @param wallet Customer wallet
     * @throws ValidationException if validation fails
     * @throws PaymentException if payment validation fails
     */
    public void validatePayment(User user, Cart cart, Wallet wallet) {
        validatePaymentInputs(user, cart, wallet);

        double totalAmount = cart.getFinalTotal();
        checkSufficientBalance(wallet, totalAmount, user);

        LOGGER.log(Level.INFO, "Payment validation passed for user: {0}", user.getUserName());
    }

    /**
     * Process wallet payment
     * @param user Customer
     * @param cart Shopping cart
     * @param wallet Customer wallet
     * @return Payment result
     * @throws PaymentException if payment fails
     */
    public PaymentResult processWalletPayment(User user, Cart cart, Wallet wallet) {
        validatePayment(user, cart, wallet);

        double amount = cart.getFinalTotal();

        LOGGER.log(Level.INFO, "Processing wallet payment for user {0}, amount: {1}",
                  new Object[]{user.getUserName(), amount});

        try {
            // In a real implementation, this would involve more complex payment processing
            // For now, we just validate the balance (which is already done above)

            PaymentResult result = new PaymentResult();
            result.setSuccess(true);
            result.setAmount(amount);
            result.setTransactionId(generateTransactionId());
            result.setPaymentMethod("Wallet");
            result.setMessage("Payment processed successfully");

            LOGGER.log(Level.INFO, "Wallet payment successful for user: {0}", user.getUserName());
            return result;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Wallet payment failed for user: " + user.getUserName(), e);
            throw new PaymentException("Payment processing failed",
                                     PaymentException.PaymentErrorCode.GATEWAY_ERROR, e);
        }
    }

    /**
     * Calculate payment fees
     * @param amount Base amount
     * @param paymentMethod Payment method
     * @return Payment fees
     */
    public double calculatePaymentFees(double amount, String paymentMethod) {
        if (amount <= 0) {
            throw new ValidationException("Amount must be positive", "amount");
        }

        // Different payment methods might have different fees
        switch (paymentMethod.toLowerCase()) {
            case "wallet":
                return 0.0; // No fees for wallet payments
            case "vnpay":
                return amount * 0.01; // 1% fee for VNPay
            case "credit_card":
                return amount * 0.02; // 2% fee for credit cards
            default:
                return amount * 0.015; // 1.5% for other methods
        }
    }

    /**
     * Get payment method display name
     * @param paymentMethod Payment method code
     * @return Display name
     */
    public String getPaymentMethodDisplayName(String paymentMethod) {
        switch (paymentMethod.toLowerCase()) {
            case "wallet":
                return "Ví Shop";
            case "vnpay":
                return "VNPay";
            case "credit_card":
                return "Thẻ tín dụng";
            case "atm":
                return "Thẻ ATM";
            default:
                return "Phương thức khác";
        }
    }

    /**
     * Validate payment amount
     * @param amount Amount to validate
     * @throws ValidationException if amount is invalid
     */
    public void validatePaymentAmount(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Payment amount must be positive", "amount");
        }

        if (amount > 10000) { // Maximum order limit
            throw new ValidationException("Payment amount exceeds maximum limit of $10,000", "amount");
        }

        // Check for reasonable minimum amount (avoid microtransactions)
        if (amount < 1.0) {
            throw new ValidationException("Payment amount must be at least $1.00", "amount");
        }

        // Check for potential floating point precision issues
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new ValidationException("Invalid payment amount", "amount");
        }
    }

    /**
     * Validate cart contents for payment
     * @param cart Shopping cart to validate
     * @throws ValidationException if cart is invalid for payment
     */
    public void validateCartForPayment(perfumeshop.model.Cart cart) {
        if (cart == null) {
            throw new ValidationException("Cart cannot be null", "cart");
        }

        if (cart.isEmpty()) {
            throw new ValidationException("Cannot process payment for empty cart", "cart");
        }

        // Validate each item in cart
        List<perfumeshop.model.Item> items = cart.getListItems();
        if (items == null) {
            throw new ValidationException("Cart items list is null", "cart");
        }

        for (perfumeshop.model.Item item : items) {
            if (item == null) {
                throw new ValidationException("Cart contains null item", "cart");
            }

            if (item.getProduct() == null) {
                throw new ValidationException("Cart item has no product information", "cart");
            }

            if (item.getQuantity() <= 0) {
                throw new ValidationException("Cart item has invalid quantity: " + item.getQuantity(), "cart");
            }

            if (item.getProduct().getPrice() <= 0) {
                throw new ValidationException("Product has invalid price: " + item.getProduct().getPrice(), "cart");
            }

            // Check for maximum quantity per item
            if (item.getQuantity() > 100) {
                throw new ValidationException("Maximum quantity per item is 100", "cart");
            }
        }

        // Validate total calculations
        double calculatedTotal = cart.getFinalTotal();
        validatePaymentAmount(calculatedTotal);

        LOGGER.log(Level.INFO, "Cart validation passed - items: {0}, total: {1}",
                  new Object[]{items.size(), calculatedTotal});
    }

    /**
     * Validate user information for payment
     * @param user User to validate
     * @throws ValidationException if user information is invalid
     */
    public void validateUserForPayment(perfumeshop.model.User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null", "user");
        }

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            throw new ValidationException("Username cannot be null or empty", "userName");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty", "email");
        }

        // Basic email validation
        if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new ValidationException("Invalid email format", "email");
        }

        // Validate email length
        if (user.getEmail().length() > 254) {
            throw new ValidationException("Email address is too long", "email");
        }

        // Validate username length and format
        if (user.getUserName().length() < 3 || user.getUserName().length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters", "userName");
        }

        LOGGER.log(Level.INFO, "User validation passed for: {0}", user.getUserName());
    }

    /**
     * Validate wallet for payment
     * @param wallet Wallet to validate
     * @param requiredAmount Required amount for payment
     * @throws ValidationException if wallet is invalid
     */
    public void validateWalletForPayment(perfumeshop.model.Wallet wallet, double requiredAmount) {
        if (wallet == null) {
            throw new ValidationException("Wallet cannot be null", "wallet");
        }

        if (wallet.getUserName() == null || wallet.getUserName().trim().isEmpty()) {
            throw new ValidationException("Wallet has no associated username", "wallet");
        }

        double balance = wallet.getBalance();

        // Validate balance is not negative
        if (balance < 0) {
            throw new ValidationException("Wallet balance cannot be negative", "wallet");
        }

        // Validate balance is not too high (prevent potential fraud)
        if (balance > 50000) {
            throw new ValidationException("Wallet balance exceeds maximum allowed amount", "wallet");
        }

        // Validate sufficient funds
        if (balance < requiredAmount) {
            throw new ValidationException(
                String.format("Insufficient funds. Required: $%.2f, Available: $%.2f",
                             requiredAmount, balance),
                "wallet");
        }

        // Check for potential precision issues
        if (Double.isNaN(balance) || Double.isInfinite(balance)) {
            throw new ValidationException("Invalid wallet balance", "wallet");
        }

        LOGGER.log(Level.INFO, "Wallet validation passed - balance: {0}, required: {1}",
                  new Object[]{balance, requiredAmount});
    }

    /**
     * Comprehensive payment validation
     * @param user Customer
     * @param cart Shopping cart
     * @param wallet Customer wallet
     * @throws ValidationException if any validation fails
     */
    public void validateCompletePayment(perfumeshop.model.User user, perfumeshop.model.Cart cart,
                                      perfumeshop.model.Wallet wallet) {
        try {
            // Validate user first
            validateUserForPayment(user);

            // Validate cart
            validateCartForPayment(cart);

            // Validate wallet
            double requiredAmount = cart.getFinalTotal();
            validateWalletForPayment(wallet, requiredAmount);

            // Cross-validation: ensure wallet belongs to user
            if (!user.getUserName().equals(wallet.getUserName())) {
                throw new ValidationException("Wallet does not belong to user", "wallet");
            }

            LOGGER.log(Level.INFO, "Complete payment validation passed for user: {0}",
                      user.getUserName());

        } catch (ValidationException e) {
            // Add context to validation errors
            throw new ValidationException(
                String.format("Payment validation failed: %s", e.getMessage()),
                e.getField());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during payment validation", e);
            throw new ValidationException("Unexpected error during payment validation", "system");
        }
    }

    /**
     * Format payment amount for display
     * @param amount Amount to format
     * @return Formatted string
     */
    public String formatPaymentAmount(double amount) {
        return String.format("$%.2f", amount);
    }

    // Private helper methods
    private void validatePaymentInputs(User user, Cart cart, Wallet wallet) {
        if (user == null) {
            throw new ValidationException("User cannot be null", "user");
        }
        if (cart == null) {
            throw new ValidationException("Cart cannot be null", "cart");
        }
        if (wallet == null) {
            throw new ValidationException("Wallet cannot be null", "wallet");
        }

        if (cart.isEmpty()) {
            throw new ValidationException("Cannot process payment for empty cart", "cart");
        }
    }

    private void checkSufficientBalance(Wallet wallet, double amount, User user) {
        double balance = wallet.getBalance();
        if (balance < amount) {
            LOGGER.log(Level.WARNING, "Insufficient balance for user {0}: required={1}, available={2}",
                      new Object[]{user.getUserName(), amount, balance});
            throw new PaymentException("Insufficient account balance",
                                     PaymentException.PaymentErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Payment result class
     */
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private double amount;
        private String paymentMethod;
        private String message;

        // Constructors
        public PaymentResult() {}

        public PaymentResult(boolean success, String transactionId, double amount,
                           String paymentMethod, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("PaymentResult{success=%s, transactionId='%s', amount=%.2f, paymentMethod='%s'}",
                               success, transactionId, amount, paymentMethod);
        }
    }
}
