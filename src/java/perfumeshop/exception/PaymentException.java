package perfumeshop.exception;

/**
 * Exception thrown when payment operations fail
 * @author PerfumeShop Team
 */
public class PaymentException extends BusinessException {

    public enum PaymentErrorCode {
        INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient account balance"),
        PAYMENT_FAILED("PAYMENT_FAILED", "Payment processing failed"),
        INVALID_PAYMENT_METHOD("INVALID_PAYMENT_METHOD", "Invalid payment method"),
        PAYMENT_TIMEOUT("PAYMENT_TIMEOUT", "Payment timeout"),
        GATEWAY_ERROR("GATEWAY_ERROR", "Payment gateway error");

        private final String code;
        private final String description;

        PaymentErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private final PaymentErrorCode paymentErrorCode;

    public PaymentException(String message) {
        super(message);
        this.paymentErrorCode = null;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.paymentErrorCode = null;
    }

    public PaymentException(String message, PaymentErrorCode errorCode) {
        super(message, errorCode.getCode());
        this.paymentErrorCode = errorCode;
    }

    public PaymentException(String message, PaymentErrorCode errorCode, Throwable cause) {
        super(message, errorCode.getCode(), cause);
        this.paymentErrorCode = errorCode;
    }

    public PaymentErrorCode getPaymentErrorCode() {
        return paymentErrorCode;
    }
}
