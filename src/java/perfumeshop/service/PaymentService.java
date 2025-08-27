package perfumeshop.service;

import perfumeshop.exception.PaymentException;
import perfumeshop.exception.ValidationException;
import perfumeshop.model.Cart;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;
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

        if (amount > 10000) { // Arbitrary limit for demo
            throw new ValidationException("Payment amount exceeds maximum limit", "amount");
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
