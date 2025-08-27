package perfumeshop.utils;

import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Security utilities for input validation and protection
 * @author PerfumeShop Team
 */
public class SecurityUtils {

    private static final Logger LOGGER = LoggingUtils.getLogger(SecurityUtils.class);

    // XSS protection patterns
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>|<[^>]+>|javascript:|on\\w+\\s*=",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // SQL injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(\\b(union|select|insert|delete|update|drop|create|alter|exec|execute)\\b)|" +
        "(\\b(and|or)\\s+\\d+\\s*=\\s*\\d+)|" +
        "(\\bscript\\b)|" +
        "(\\bor\\s+\\d+\\s*=\\s*\\d+)|" +
        "(\\band\\s+\\d+\\s*=\\s*\\d+)",
        Pattern.CASE_INSENSITIVE
    );

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Phone validation pattern (Vietnamese)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+84|84|0)[3|5|7|8|9][0-9]{8}$"
    );

    /**
     * Sanitize input to prevent XSS attacks
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String sanitized = input.trim();

        // Remove potential XSS content
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");

        // Escape HTML characters
        sanitized = sanitized.replace("&", "&amp;")
                           .replace("<", "&lt;")
                           .replace(">", "&gt;")
                           .replace("\"", "&quot;")
                           .replace("'", "&#x27;")
                           .replace("/", "&#x2F;");

        return sanitized;
    }

    /**
     * Validate input for SQL injection
     * @param input Input string to validate
     * @return true if safe, false if potentially dangerous
     */
    public static boolean isSqlInjectionSafe(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format (Vietnamese)
     * @param phone Phone number to validate
     * @return true if valid phone format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validate input length
     * @param input Input string
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if within valid length range
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return minLength == 0;
        }

        int length = input.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Generate CSRF token
     * @return CSRF token
     */
    public static String generateCSRFToken() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Validate CSRF token
     * @param session HTTP session
     * @param token Token to validate
     * @return true if valid token
     */
    public static boolean validateCSRFToken(HttpSession session, String token) {
        if (session == null || token == null || token.trim().isEmpty()) {
            return false;
        }

        String sessionToken = (String) session.getAttribute("csrfToken");
        return token.equals(sessionToken);
    }

    /**
     * Set CSRF token in session
     * @param session HTTP session
     * @return Generated CSRF token
     */
    public static String setCSRFToken(HttpSession session) {
        String token = generateCSRFToken();
        session.setAttribute("csrfToken", token);
        return token;
    }

    /**
     * Validate request parameters for common attacks
     * @param request HTTP request
     * @param parameters Parameter names to validate
     * @return true if all parameters are safe
     */
    public static boolean validateRequestParameters(HttpServletRequest request, String... parameters) {
        for (String param : parameters) {
            String value = request.getParameter(param);
            if (value != null && !isSqlInjectionSafe(value)) {
                LOGGER.log(Level.WARNING, "SQL injection attempt detected in parameter: {0}", param);
                LoggingUtils.logSecurityEvent(LOGGER, request, "SQL_INJECTION_ATTEMPT",
                                            "Parameter: " + param + ", Value: " + value);
                return false;
            }
        }
        return true;
    }

    /**
     * Check for suspicious request patterns
     * @param request HTTP request
     * @return true if request looks suspicious
     */
    public static boolean isSuspiciousRequest(HttpServletRequest request) {
        // Check for common attack patterns
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        // Check for suspicious user agents
        if (userAgent != null && (userAgent.contains("sqlmap") ||
                                 userAgent.contains("nmap") ||
                                 userAgent.contains("masscan"))) {
            LoggingUtils.logSecurityEvent(LOGGER, request, "SUSPICIOUS_USER_AGENT",
                                        "User-Agent: " + userAgent);
            return true;
        }

        // Check for unusual request patterns
        String requestURI = request.getRequestURI();
        if (requestURI.contains("..") || requestURI.contains("//")) {
            LoggingUtils.logSecurityEvent(LOGGER, request, "DIRECTORY_TRAVERSAL_ATTEMPT",
                                        "URI: " + requestURI);
            return true;
        }

        return false;
    }

    /**
     * Rate limiting check (simple implementation)
     * @param session HTTP session
     * @param action Action to rate limit
     * @param maxRequests Maximum requests allowed
     * @param timeWindowMs Time window in milliseconds
     * @return true if within rate limit, false if exceeded
     */
    public static boolean checkRateLimit(HttpSession session, String action,
                                       int maxRequests, long timeWindowMs) {
        String key = "rate_limit_" + action;
        Long lastRequestTime = (Long) session.getAttribute(key + "_time");
        Integer requestCount = (Integer) session.getAttribute(key + "_count");

        long currentTime = System.currentTimeMillis();

        if (lastRequestTime == null || (currentTime - lastRequestTime) > timeWindowMs) {
            // Reset or initialize rate limit
            session.setAttribute(key + "_time", currentTime);
            session.setAttribute(key + "_count", 1);
            return true;
        } else {
            // Within time window, check count
            if (requestCount < maxRequests) {
                session.setAttribute(key + "_count", requestCount + 1);
                return true;
            } else {
                // Rate limit exceeded
                LOGGER.log(Level.WARNING, "Rate limit exceeded for action: {0}", action);
                return false;
            }
        }
    }

    /**
     * Validate file upload security
     * @param filename Original filename
     * @param allowedExtensions Allowed file extensions (without dot)
     * @return true if filename is safe
     */
    public static boolean isSafeFilename(String filename, String... allowedExtensions) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Remove path information
        String cleanFilename = filename.replaceAll("[\\\\/:*?\"<>|]", "");

        // Check for suspicious patterns
        if (cleanFilename.contains("..") || cleanFilename.startsWith(".")) {
            return false;
        }

        // Check extension
        int lastDot = cleanFilename.lastIndexOf('.');
        if (lastDot == -1) {
            return allowedExtensions.length == 0; // Allow files without extension
        }

        String extension = cleanFilename.substring(lastDot + 1).toLowerCase();

        for (String allowedExt : allowedExtensions) {
            if (extension.equals(allowedExt.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Hash password (simple implementation - in production use bcrypt or similar)
     * @param password Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate secure random string
     * @param length Length of the string
     * @return Random string
     */
    public static String generateSecureRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
}
