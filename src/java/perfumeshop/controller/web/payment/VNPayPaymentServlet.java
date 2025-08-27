/*
 * VNPay Payment Servlet - Demo Version
 * For educational purposes only
 */
package perfumeshop.controller.web.payment;

import perfumeshop.dal.OrderDAO;
import perfumeshop.dal.ProductDAO;
import perfumeshop.dal.WalletDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import perfumeshop.model.Cart;
import perfumeshop.model.Email;
import perfumeshop.model.Item;
import perfumeshop.model.Order;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;
import perfumeshop.utils.VNPayDemoUtils;

/**
 * VNPay Payment Servlet - Demo Implementation
 * @author VNPay Demo Team
 */
@WebServlet(name = "VNPayPaymentServlet", urlPatterns = {"/vnpay_payment"})
public class VNPayPaymentServlet extends HttpServlet {

    // Demo VNPay configuration
    private static final String VNPAY_RETURN_URL = "/vnpay_result";

    /**
     * Processes requests for both HTTP GET and POST methods.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet VNPayPaymentServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet VNPayPaymentServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP GET method.
     * Redirect to payment page
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");

        // Check if cart is empty
        if (cart == null || cart.getListItems().isEmpty()) {
            request.setAttribute("error", "Giỏ hàng trống!");
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
            return;
        }

        // Check if user is logged in
        User user = (User) session.getAttribute("account");
        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        // Redirect to VNPay payment page
        response.sendRedirect("vnpay_payment.jsp");
    }

    /**
     * Handles the HTTP POST method.
     * Process payment request
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Cart cart = (Cart) session.getAttribute("cart");
        User user = (User) session.getAttribute("account");

        // Validate cart and user
        if (cart == null || cart.getListItems().isEmpty()) {
            request.setAttribute("error", "Giỏ hàng trống!");
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
            return;
        }

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            // Get form parameters
            String customerName = request.getParameter("customerName");
            String customerEmail = request.getParameter("customerEmail");
            String customerPhone = request.getParameter("customerPhone");
            String customerAddress = request.getParameter("customerAddress");
            String orderDescription = request.getParameter("orderDescription");

            // Validate required fields
            if (customerName == null || customerName.trim().isEmpty() ||
                customerEmail == null || customerEmail.trim().isEmpty() ||
                customerPhone == null || customerPhone.trim().isEmpty() ||
                customerAddress == null || customerAddress.trim().isEmpty()) {

                request.setAttribute("error", "Vui lòng điền đầy đủ thông tin!");
                request.getRequestDispatcher("vnpay_payment.jsp").forward(request, response);
                return;
            }

            // Calculate total amount (VND)
            double totalAmount = cart.getTotalMoney() + 3; // Include shipping fee
            long vnpAmount = VNPayDemoUtils.convertUSDtoVND(totalAmount);

            // Generate transaction reference
            String txnRef = VNPayDemoUtils.generateTxnRef();

            // Generate VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNPayDemoUtils.DEMO_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + (orderDescription != null ? orderDescription : "Perfume Shop"));
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", request.getRequestURL().toString().replace("/vnpay_payment", VNPAY_RETURN_URL));
            vnpParams.put("vnp_IpAddr", getClientIpAddress(request));
            vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            // Store order info in session for later processing
            session.setAttribute("vnp_txn_ref", txnRef);
            session.setAttribute("vnp_amount", totalAmount);
            session.setAttribute("customer_info", Map.of(
                "name", customerName,
                "email", customerEmail,
                "phone", customerPhone,
                "address", customerAddress,
                "description", orderDescription != null ? orderDescription : ""
            ));

            // Generate secure hash
            String vnpSecureHash = VNPayDemoUtils.generateSecureHash(vnpParams, VNPayDemoUtils.DEMO_HASH_SECRET);
            vnpParams.put("vnp_SecureHash", vnpSecureHash);

            // Build payment URL
            StringBuilder paymentUrl = new StringBuilder(VNPayDemoUtils.DEMO_PAYMENT_URL);
            paymentUrl.append("?");
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                paymentUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                paymentUrl.append("=");
                paymentUrl.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
                paymentUrl.append("&");
            }

            // Remove last &
            String finalUrl = paymentUrl.toString();
            if (finalUrl.endsWith("&")) {
                finalUrl = finalUrl.substring(0, finalUrl.length() - 1);
            }

            // For demo purposes, simulate VNPay response instead of redirecting
            simulateVNPayPayment(request, response, session, cart, user, totalAmount);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Có lỗi xảy ra trong quá trình xử lý thanh toán: " + e.getMessage());
            request.getRequestDispatcher("vnpay_payment.jsp").forward(request, response);
        }
    }

    /**
     * Simulate VNPay payment response (for demo purposes)
     * Enhanced with better error handling and validation
     */
    private void simulateVNPayPayment(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, Cart cart, User user, double totalAmount)
            throws ServletException, IOException {

        OrderDAO od = new OrderDAO();
        ProductDAO pd = new ProductDAO();
        WalletDAO wd = new WalletDAO();
        Wallet wallet = null;

        try {
            // Validate wallet exists and has sufficient balance
            wallet = wd.getWalletByUserName(user.getUserName());
            if (wallet == null) {
                throw new RuntimeException("Wallet not found for user: " + user.getUserName());
            }

            if (wallet.getBalance() < totalAmount) {
                request.setAttribute("payment_status", "failed");
                request.setAttribute("error_message", "Số dư không đủ để thực hiện thanh toán.");
                request.getRequestDispatcher("vnpay_result.jsp").forward(request, response);
                return;
            }

            // Validate cart one more time
            if (cart == null || cart.isEmpty()) {
                request.setAttribute("payment_status", "failed");
                request.setAttribute("error_message", "Giỏ hàng trống hoặc không hợp lệ.");
                request.getRequestDispatcher("vnpay_result.jsp").forward(request, response);
                return;
            }

            // Simulate VNPay payment processing with more realistic logic
            Map<String, String> vnpayResponse = VNPayDemoUtils.simulatePaymentResult(0.85); // 85% success rate

            String responseCode = vnpayResponse.get("vnp_ResponseCode");
            boolean paymentSuccess = "00".equals(responseCode);

            if (paymentSuccess) {
                // Payment successful - process order with better transaction handling
                processSuccessfulPayment(od, pd, wd, wallet, cart, user, totalAmount, session, request);

                // Set success attributes
                request.setAttribute("payment_status", "success");
                request.setAttribute("order_id", vnpayResponse.get("vnp_TxnRef"));
                request.setAttribute("transaction_id", vnpayResponse.get("vnp_TxnRef"));
                request.setAttribute("amount", totalAmount);
                request.setAttribute("payment_method", "VNPay");
                request.setAttribute("bank_code", vnpayResponse.get("vnp_BankCode"));
                request.setAttribute("card_type", vnpayResponse.get("vnp_CardType"));
                request.setAttribute("pay_date", vnpayResponse.get("vnp_PayDate"));

            } else {
                // Payment failed - set appropriate error message based on response code
                String errorMessage = VNPayDemoUtils.getResponseDescription(responseCode);
                request.setAttribute("payment_status", "failed");
                request.setAttribute("error_message", "Thanh toán thất bại: " + errorMessage);
                request.setAttribute("vnp_response_code", responseCode);
            }

        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Payment processing error: " + e.getMessage());
            e.printStackTrace();

            request.setAttribute("payment_status", "failed");
            request.setAttribute("error_message", "Có lỗi xảy ra trong quá trình xử lý thanh toán: " + e.getMessage());
        } finally {
            // Always clean up session attributes
            cleanupSessionAttributes(session);
            request.getRequestDispatcher("vnpay_result.jsp").forward(request, response);
        }
    }

    /**
     * Process successful payment with improved error handling
     */
    private void processSuccessfulPayment(OrderDAO od, ProductDAO pd, WalletDAO wd, Wallet wallet,
                                        Cart cart, User user, double totalAmount, HttpSession session,
                                        HttpServletRequest request) throws Exception {

        LocalDateTime currentDateTime = LocalDateTime.now();

        // Use synchronized block to prevent race conditions in order creation
        synchronized (this) {
            int ordersBefore = od.getNumberOrders();
            od.addOrder(user, cart);
            int ordersAfter = od.getNumberOrders();

            if (ordersBefore >= ordersAfter) {
                throw new RuntimeException("Không thể tạo đơn hàng do lỗi hệ thống");
            }
        }

        try {
            // Send confirmation email
            Email handleEmail = new Email();
            String subject = handleEmail.subjectOrder(user.getFullName());
            String message = handleEmail.messageOrder(currentDateTime, totalAmount, user.getAddress());
            handleEmail.sendEmail(subject, message, user.getEmail());

            // Update wallet balance
            wd.deductionMoney(user.getUserName(), totalAmount);
            wallet = wd.getWalletByUserName(user.getUserName());
            session.setAttribute("wallet", wallet);

            // Clear cart
            session.removeAttribute("cart");
            session.removeAttribute("listItemsInCart");
            session.setAttribute("cartSize", 0);

        } catch (Exception e) {
            // If email or wallet update fails, we should still consider the payment successful
            // since the order was created, but log the error
            System.err.println("Post-payment processing error: " + e.getMessage());
            // Don't throw exception here as the order was already created
        }
    }

    /**
     * Clean up temporary session attributes
     */
    private void cleanupSessionAttributes(HttpSession session) {
        session.removeAttribute("vnp_txn_ref");
        session.removeAttribute("vnp_amount");
        session.removeAttribute("customer_info");
    }



    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "VNPay Payment Servlet - Demo Version";
    }
}
