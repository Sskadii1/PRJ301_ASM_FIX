package perfumeshop.filter;

import perfumeshop.utils.LoggingUtils;
import perfumeshop.utils.SecurityUtils;
import perfumeshop.utils.MonitoringUtils;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security filter to protect against common web vulnerabilities
 * @author PerfumeShop Team
 */
@WebFilter("/*")
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = LoggingUtils.getLogger(SecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.log(Level.INFO, "SecurityFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Record request for monitoring
        MonitoringUtils.recordRequest();

        try {
            // Check for suspicious requests
            if (SecurityUtils.isSuspiciousRequest(httpRequest)) {
                LOGGER.log(Level.WARNING, "Blocking suspicious request from: {0}", httpRequest.getRemoteAddr());
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                MonitoringUtils.recordFailedRequest();
                return;
            }

            // Rate limiting for sensitive endpoints
            String requestURI = httpRequest.getRequestURI();
            if (isSensitiveEndpoint(requestURI)) {
                if (!SecurityUtils.checkRateLimit(httpRequest.getSession(), requestURI, 10, 60000)) {
                    LOGGER.log(Level.WARNING, "Rate limit exceeded for: {0}", requestURI);
                    httpResponse.sendError(HttpServletResponse.SC_TOO_MANY_REQUESTS, "Rate limit exceeded");
                    MonitoringUtils.recordFailedRequest();
                    return;
                }
            }

            // Validate request parameters for SQL injection
            if (!SecurityUtils.validateRequestParameters(httpRequest, getParameterNames(httpRequest))) {
                LOGGER.log(Level.SEVERE, "SQL injection attempt blocked from: {0}", httpRequest.getRemoteAddr());
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameters");
                MonitoringUtils.recordFailedRequest();
                return;
            }

            // Add security headers
            addSecurityHeaders(httpResponse);

            // Continue with the request
            chain.doFilter(request, response);

            // Record successful request
            MonitoringUtils.recordSuccessfulRequest();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in security filter", e);
            MonitoringUtils.recordFailedRequest();
            throw e;
        }
    }

    @Override
    public void destroy() {
        LOGGER.log(Level.INFO, "SecurityFilter destroyed");
    }

    /**
     * Check if the endpoint is sensitive and requires rate limiting
     */
    private boolean isSensitiveEndpoint(String requestURI) {
        return requestURI.contains("/login") ||
               requestURI.contains("/register") ||
               requestURI.contains("/checkout") ||
               requestURI.contains("/admin");
    }

    /**
     * Get parameter names from request (for validation)
     */
    private String[] getParameterNames(HttpServletRequest request) {
        return request.getParameterMap().keySet().toArray(new String[0]);
    }

    /**
     * Add security headers to response
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        // Prevent XSS attacks
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy (basic)
        response.setHeader("Content-Security-Policy",
                          "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");

        // HSTS (HTTP Strict Transport Security) - only for HTTPS
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    }
}
