package perfumeshop.controller.web.cart_wishlist;

import perfumeshop.dal.OrderDAO;
import perfumeshop.dal.ProductDAO;
import perfumeshop.dal.WalletDAO;
import perfumeshop.exception.PaymentException;
import perfumeshop.exception.ValidationException;
import perfumeshop.exception.DaoException;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import perfumeshop.model.Cart;
import perfumeshop.model.Email;
import perfumeshop.model.Item;
import perfumeshop.model.Order;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;

/**
 * Shopping cart servlet handling cart operations and checkout
 * @author PerfumeShop Team
 */
@WebServlet(name = "ViewCartServlet", urlPatterns = {"/viewcart"})
public class ViewCartServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ViewCartServlet.class.getName());

    // Constants for request attributes
    private static final String ATTR_MESSAGE1 = "message1";
    private static final String ATTR_MESSAGE2 = "message2";
    private static final String ATTR_CART = "cart";
    private static final String ATTR_LIST_ITEMS = "listItemsInCart";
    private static final String ATTR_CART_SIZE = "cartSize";
    private static final String ATTR_WALLET = "wallet";

    // Constants for error messages
    private static final String MSG_ORDER_SUCCESS = "Order Success";
    private static final String MSG_ORDER_FAIL = "Order Fail";
    private static final String MSG_INSUFFICIENT_BALANCE = "The balance in the account is not enough to make this transaction";
    private static final String MSG_NETWORK_ERROR = "Check your network status again";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ViewCartServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ViewCartServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP GET method for cart operations (remove items)
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String clientIP = getClientIP(request);

        try {
            LOGGER.log(Level.INFO, "Cart GET request from IP: {0}", clientIP);

            // Get or create cart
            Cart cart = getOrCreateCart(session);

            // Handle item removal if rid parameter is present
            String productIdParam = request.getParameter("rid");
            if (productIdParam != null && !productIdParam.trim().isEmpty()) {
                handleItemRemoval(cart, productIdParam, session);
            }

            // Update session attributes
            updateCartSessionAttributes(session, cart);

            // Forward to cart page
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error in cart GET: {0}", e.getMessage());
            request.setAttribute(ATTR_MESSAGE1, MSG_ORDER_FAIL);
            request.setAttribute(ATTR_MESSAGE2, e.getMessage());
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in cart GET", e);
            request.setAttribute(ATTR_MESSAGE1, MSG_ORDER_FAIL);
            request.setAttribute(ATTR_MESSAGE2, "An unexpected error occurred. Please try again.");
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        }
    }

    /**
     * Get or create cart from session
     */
    private Cart getOrCreateCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(ATTR_CART);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(ATTR_CART, cart);
            LOGGER.log(Level.INFO, "Created new cart for session");
        }
        return cart;
    }

    /**
     * Handle item removal from cart
     */
    private void handleItemRemoval(Cart cart, String productIdParam, HttpSession session) {
        try {
            int productId = Integer.parseInt(productIdParam.trim());
            if (productId <= 0) {
                throw new ValidationException("Invalid product ID: " + productIdParam, "productId");
            }

            boolean removed = cart.removeItem(productId);
            if (removed) {
                LOGGER.log(Level.INFO, "Removed product ID {0} from cart", productId);
            } else {
                LOGGER.log(Level.WARNING, "Product ID {0} not found in cart", productId);
            }

        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid product ID format: " + productIdParam, "productId");
        }
    }

    /**
     * Update cart session attributes
     */
    private void updateCartSessionAttributes(HttpSession session, Cart cart) {
        List<Item> list = cart.getListItems();
        session.setAttribute(ATTR_CART, cart);
        session.setAttribute(ATTR_LIST_ITEMS, list);
        session.setAttribute(ATTR_CART_SIZE, list.size());

        LOGGER.log(Level.INFO, "Updated cart session attributes - size: {0}", list.size());
    }

    /**
     * Handles the HTTP POST method for checkout processing
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String clientIP = getClientIP(request);

        try {
            LOGGER.log(Level.INFO, "Checkout POST request from IP: {0}", clientIP);

            // Validate session and get required objects
            Cart cart = validateAndGetCart(session);
            User user = validateAndGetUser(session);
            Wallet wallet = validateAndGetWallet(session);

            // Validate cart contents
            validateCartForCheckout(cart);

            // Check balance
            double totalAmount = cart.getFinalTotal();
            checkSufficientBalance(wallet, totalAmount, user);

            // Process order
            processOrder(cart, user, wallet, session, request, response);

        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error during checkout: {0}", e.getMessage());
            setErrorAttributes(request, MSG_ORDER_FAIL, e.getMessage());
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        } catch (PaymentException e) {
            LOGGER.log(Level.SEVERE, "Payment error during checkout", e);
            setErrorAttributes(request, MSG_ORDER_FAIL, e.getMessage());
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        } catch (DaoException e) {
            LOGGER.log(Level.SEVERE, "Database error during checkout", e);
            setErrorAttributes(request, MSG_ORDER_FAIL, "Database error occurred. Please try again.");
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during checkout", e);
            setErrorAttributes(request, MSG_ORDER_FAIL, "An unexpected error occurred. Please try again.");
            request.getRequestDispatcher("viewcart.jsp").forward(request, response);
        }
    }

    /**
     * Validate and get cart from session
     */
    private Cart validateAndGetCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(ATTR_CART);
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Cart is empty or not found", "cart");
        }
        return cart;
    }

    /**
     * Validate and get user from session
     */
    private User validateAndGetUser(HttpSession session) {
        User user = (User) session.getAttribute("account");
        if (user == null) {
            throw new ValidationException("User not logged in", "user");
        }
        return user;
    }

    /**
     * Validate and get wallet from session
     */
    private Wallet validateAndGetWallet(HttpSession session) {
        Wallet wallet = (Wallet) session.getAttribute(ATTR_WALLET);
        if (wallet == null) {
            throw new ValidationException("Wallet not found", "wallet");
        }
        return wallet;
    }

    /**
     * Validate cart contents for checkout
     */
    private void validateCartForCheckout(Cart cart) {
        try {
            cart.validate();
        } catch (IllegalStateException e) {
            throw new ValidationException("Invalid cart contents: " + e.getMessage(), "cart");
        }
    }

    /**
     * Check if user has sufficient balance
     */
    private void checkSufficientBalance(Wallet wallet, double totalAmount, User user) {
        double balance = wallet.getBalance();
        if (balance < totalAmount) {
            LOGGER.log(Level.WARNING, "Insufficient balance for user {0}: required={1}, available={2}",
                      new Object[]{user.getUserName(), totalAmount, balance});
            throw new PaymentException(MSG_INSUFFICIENT_BALANCE,
                                     PaymentException.PaymentErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    /**
     * Process the order with improved transaction handling
     */
    private void processOrder(Cart cart, User user, Wallet wallet, HttpSession session,
                            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        OrderDAO orderDAO = new OrderDAO();
        WalletDAO walletDAO = new WalletDAO();

        double totalAmount = cart.getFinalTotal();

        LOGGER.log(Level.INFO, "Processing order for user {0}, amount: {1}",
                  new Object[]{user.getUserName(), totalAmount});

        // Use synchronized block to prevent race conditions
        synchronized (this) {
            try {
                // Double-check balance before processing
                Wallet currentWallet = walletDAO.getWalletByUserName(user.getUserName());
                if (currentWallet == null) {
                    throw new PaymentException("Wallet not found during order processing",
                                             PaymentException.PaymentErrorCode.GATEWAY_ERROR);
                }

                if (currentWallet.getBalance() < totalAmount) {
                    throw new PaymentException("Insufficient balance during order processing",
                                             PaymentException.PaymentErrorCode.INSUFFICIENT_BALANCE);
                }

                // Create order
                orderDAO.addOrder(user, cart);

                // Order successful - proceed with post-processing
                handleSuccessfulOrder(cart, user, currentWallet, walletDAO, totalAmount, session, request, response);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Order processing failed for user: " + user.getUserName(), e);
                throw new DaoException("Order creation failed: " + e.getMessage(), "create", "order", e);
            }
        }
    }

    /**
     * Handle successful order processing with improved error handling
     */
    private void handleSuccessfulOrder(Cart cart, User user, Wallet wallet, WalletDAO walletDAO,
                                     double totalAmount, HttpSession session, HttpServletRequest request,
                                     HttpServletResponse response) throws ServletException, IOException {

        LocalDateTime currentDateTime = LocalDateTime.now();
        boolean emailSent = false;
        boolean walletUpdated = false;

        try {
            // Send confirmation email (non-critical operation)
            try {
                sendOrderConfirmationEmail(user, totalAmount);
                emailSent = true;
                LOGGER.log(Level.INFO, "Confirmation email sent to: {0}", user.getEmail());
            } catch (Exception emailEx) {
                LOGGER.log(Level.WARNING, "Failed to send confirmation email to: " + user.getEmail(), emailEx);
                // Don't fail the whole operation for email issues
            }

            // Update wallet balance (critical operation)
            walletDAO.deductionMoney(user.getUserName(), totalAmount);
            wallet = walletDAO.getWalletByUserName(user.getUserName());
            if (wallet != null) {
                session.setAttribute(ATTR_WALLET, wallet);
                walletUpdated = true;
                LOGGER.log(Level.INFO, "Wallet updated successfully for user: {0}", user.getUserName());
            }

            // Clear cart (critical operation)
            session.removeAttribute(ATTR_CART);
            session.removeAttribute(ATTR_LIST_ITEMS);
            session.setAttribute(ATTR_CART_SIZE, 0);

            LOGGER.log(Level.INFO, "Order completed successfully for user: {0}", user.getUserName());

            // Set success message with details
            String successMessage = MSG_ORDER_SUCCESS;
            if (!emailSent) {
                successMessage += " (Lưu ý: Không thể gửi email xác nhận)";
            }
            setSuccessAttributes(request, successMessage, null);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in post-order processing for user: " + user.getUserName(), e);

            // Try to rollback if wallet was updated but cart clearing failed
            if (walletUpdated && (session.getAttribute(ATTR_CART) != null)) {
                LOGGER.log(Level.WARNING, "Attempting to rollback wallet update due to cart clearing failure");
                try {
                    walletDAO.inputMoney(user.getUserName(), totalAmount);
                    LOGGER.log(Level.INFO, "Wallet rollback successful for user: {0}", user.getUserName());
                } catch (Exception rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Wallet rollback failed for user: " + user.getUserName(), rollbackEx);
                }
            }

            throw new DaoException("Error in post-order processing: " + e.getMessage(), "update", "post_order", e);
        }
    }

    /**
     * Send order confirmation email
     */
    private void sendOrderConfirmationEmail(User user, double totalAmount) {
        try {
            Email emailHandler = new Email();
            String subject = emailHandler.subjectOrder(user.getFullName());
            String message = emailHandler.messageOrder(LocalDateTime.now(), totalAmount, user.getAddress());
            emailHandler.sendEmail(subject, message, user.getEmail());

            LOGGER.log(Level.INFO, "Confirmation email sent to: {0}", user.getEmail());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send confirmation email", e);
            // Don't throw exception here - email failure shouldn't stop order
        }
    }

    /**
     * Set error attributes
     */
    private void setErrorAttributes(HttpServletRequest request, String message1, String message2) {
        request.setAttribute(ATTR_MESSAGE1, message1);
        request.setAttribute(ATTR_MESSAGE2, message2);
    }

    /**
     * Set success attributes
     */
    private void setSuccessAttributes(HttpServletRequest request, String message1, String message2) {
        request.setAttribute(ATTR_MESSAGE1, message1);
        request.setAttribute(ATTR_MESSAGE2, message2);
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
