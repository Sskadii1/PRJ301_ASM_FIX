/*
 * VNPay Demo Utilities
 * For educational purposes only - NOT for production use
 */
package perfumeshop.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;

/**
 * VNPay Demo Utilities - Simulate VNPay API for educational purposes
 * @author VNPay Demo Team
 */
public class VNPayDemoUtils {

    // Demo configuration constants
    public static final String DEMO_TMN_CODE = "DEMO_TMN";
    public static final String DEMO_HASH_SECRET = "DEMO_SECRET_KEY";
    public static final String DEMO_PAYMENT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /**
     * Generate VNPay secure hash
     * @param params Map of parameters
     * @param secretKey Secret key for hashing
     * @return Secure hash string
     */
    public static String generateSecureHash(Map<String, String> params, String secretKey) {
        try {
            // Sort parameters by key
            TreeMap<String, String> sortedParams = new TreeMap<>(params);

            // Build hash data
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    hashData.append(entry.getKey()).append("=").append(value).append("&");
                }
            }

            // Remove last &
            String hashString = hashData.toString();
            if (hashString.endsWith("&")) {
                hashString = hashString.substring(0, hashString.length() - 1);
            }

            // Add secret key
            hashString += secretKey;

            // Generate SHA256 hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(hashString.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verify VNPay secure hash
     * @param params Map of parameters (including vnp_SecureHash)
     * @param secretKey Secret key for verification
     * @return true if hash is valid, false otherwise
     */
    public static boolean verifySecureHash(Map<String, String> params, String secretKey) {
        if (!params.containsKey("vnp_SecureHash")) {
            return false;
        }

        String receivedHash = params.get("vnp_SecureHash");

        // Create a copy of params without the hash
        TreeMap<String, String> paramsWithoutHash = new TreeMap<>(params);
        paramsWithoutHash.remove("vnp_SecureHash");

        // Generate expected hash
        String expectedHash = generateSecureHash(paramsWithoutHash, secretKey);

        return receivedHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * Generate unique transaction reference
     * @return Transaction reference string
     */
    public static String generateTxnRef() {
        return "VNP" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Convert USD to VND (demo exchange rate)
     * @param usdAmount Amount in USD
     * @return Amount in VND (long)
     */
    public static long convertUSDtoVND(double usdAmount) {
        // Demo exchange rate: 1 USD = 23,000 VND
        return (long) (usdAmount * 23000 * 100); // Multiply by 100 for VNPay format
    }

    /**
     * Simulate VNPay payment result
     * @param successRate Success rate (0.0 to 1.0)
     * @return Payment result map
     */
    public static Map<String, String> simulatePaymentResult(double successRate) {
        TreeMap<String, String> result = new TreeMap<>();

        boolean isSuccess = Math.random() < successRate;

        if (isSuccess) {
            result.put("vnp_ResponseCode", "00");
            result.put("vnp_TransactionStatus", "00");
            result.put("vnp_Message", "Successful.");
            result.put("vnp_BankCode", "DEMO_BANK");
            result.put("vnp_CardType", "DEMO_CARD");
        } else {
            result.put("vnp_ResponseCode", "01");
            result.put("vnp_TransactionStatus", "01");
            result.put("vnp_Message", "Transaction failed.");
            result.put("vnp_BankCode", "DEMO_BANK");
        }

        result.put("vnp_TmnCode", DEMO_TMN_CODE);
        result.put("vnp_Amount", "10000000"); // 100,000 VND
        result.put("vnp_OrderInfo", "Demo payment");
        result.put("vnp_PayDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        result.put("vnp_TxnRef", generateTxnRef());

        return result;
    }

    /**
     * Get VNPay response description by response code
     * @param responseCode VNPay response code
     * @return Description in Vietnamese
     */
    public static String getResponseDescription(String responseCode) {
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "01":
                return "Giao dịch đã tồn tại";
            case "02":
                return "Merchant không hợp lệ";
            case "03":
                return "Dữ liệu gửi sang không đúng định dạng";
            case "04":
                return "Khởi tạo GD không thành công do Website đang bị tạm khóa";
            case "05":
                return "Giao dịch không thành công do: Quý khách nhập sai mật khẩu thanh toán quá số lần quy định";
            case "06":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu thanh toán";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "08":
                return "Giao dịch không thành công do: Hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "09":
                return "Giao dịch không thành công do: Thẻ/tài khoản của khách hàng bị khóa.";
            case "10":
                return "Giao dịch không thành công do: Nhập sai số tiền";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12":
                return "Giao dịch không thành công do: Thẻ/tài khoản của khách hàng không đủ số dư";
            case "13":
                return "Giao dịch không thành công do Quý khách xác nhận cancel giao dịch";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì.";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Mã lỗi không xác định: " + responseCode;
        }
    }

    /**
     * Format amount for display
     * @param amount Amount in VND (long)
     * @return Formatted string
     */
    public static String formatAmount(long amount) {
        // Remove the last 2 digits (VNPay format)
        double displayAmount = amount / 100.0;
        return String.format("%,.0f", displayAmount) + " VND";
    }

    /**
     * Format amount for display in USD
     * @param usdAmount Amount in USD
     * @return Formatted string
     */
    public static String formatUSDAmount(double usdAmount) {
        return String.format("$%,.2f", usdAmount);
    }
}
