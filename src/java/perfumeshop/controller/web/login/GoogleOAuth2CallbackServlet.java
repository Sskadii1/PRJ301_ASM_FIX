package perfumeshop.controller.web.login;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "GoogleOAuth2CallbackServlet", urlPatterns = {"/google-oauth2-callback"})
public class GoogleOAuth2CallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Google OAuth disabled for demo purposes
        HttpSession session = request.getSession();
        
        // Clear any existing session
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to login with error message
        response.sendRedirect("login.jsp?error=Google OAuth is disabled in demo mode");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}