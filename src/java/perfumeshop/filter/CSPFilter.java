package perfumeshop.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Content Security Policy Filter to allow external resources
 * @author PerfumeShop Team
 */
@WebFilter(urlPatterns = {"/*"})
public class CSPFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Set Content Security Policy to allow external resources
        String cspPolicy = "default-src 'self'; " +
                          "style-src 'self' 'unsafe-inline' " +
                          "https://stackpath.bootstrapcdn.com " +
                          "https://cdnjs.cloudflare.com " +
                          "https://maxcdn.bootstrapcdn.com " +
                          "https://use.fontawesome.com " +
                          "https://mdbootstrap.com " +
                          "https://fonts.googleapis.com " +
                          "https://cdn.tailwindcss.com " +
                          "https://bizweb.dktcdn.net " +
                          "https://hstatic.net; " +
                          "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                          "https://stackpath.bootstrapcdn.com " +
                          "https://cdnjs.cloudflare.com " +
                          "https://maxcdn.bootstrapcdn.com " +
                          "https://ajax.googleapis.com " +
                          "https://cdn.jsdelivr.net " +
                          "https://bizweb.dktcdn.net; " +
                          "font-src 'self' " +
                          "https://fonts.gstatic.com " +
                          "https://fonts.googleapis.com " +
                          "https://cdnjs.cloudflare.com " +
                          "https://maxcdn.bootstrapcdn.com " +
                          "https://use.fontawesome.com " +
                          "https://hstatic.net; " +
                          "img-src 'self' data: https:; " +
                          "connect-src 'self'; " +
                          "frame-src 'self';";
        
        httpResponse.setHeader("Content-Security-Policy", cspPolicy);
        
        // Also set X-Content-Type-Options for security
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Filter cleanup
    }
}
