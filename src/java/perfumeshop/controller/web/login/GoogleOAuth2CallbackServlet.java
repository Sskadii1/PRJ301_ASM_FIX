package perfumeshop.controller.web.login;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import java.io.InputStreamReader;
import java.util.Arrays;
import perfumeshop.dal.UserDAO;
import perfumeshop.dal.WalletDAO;
import perfumeshop.model.Cart;
import perfumeshop.model.User;
import perfumeshop.model.Wallet;
import perfumeshop.model.Wishlist;

@WebServlet(name = "GoogleOAuth2CallbackServlet", urlPatterns = {"/google-oauth2-callback"})
public class GoogleOAuth2CallbackServlet extends HttpServlet {

    private static final String CLIENT_SECRET_FILE = "client_secret.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private GoogleClientSecrets clientSecrets;

    @Override
    public void init() throws ServletException {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/" + CLIENT_SECRET_FILE)));
        } catch (Exception e) {
            throw new ServletException("Error loading client secrets", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");
        if (code == null) {
            response.sendRedirect("login"); // Handle error or denial
            return;
        }

        String redirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/google-oauth2-callback";

        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Arrays.asList("email", "profile"))
                    .setAccessType("offline").build();

            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            Credential credential = flow.createAndStoreCredential(tokenResponse, null);

            Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("PerfumeShop").build();
            Userinfo userinfo = oauth2.userinfo().get().execute();

            String googleEmail = userinfo.getEmail();
            String googleName = userinfo.getName();
            String googleId = userinfo.getId();
            
            // --- Logic to integrate with your existing user management ---
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserByEmail(googleEmail);

            if (user == null) {
                // New user: register them
                user = new User();
                user.setUserName(googleEmail); // Using email as username for Google users
                user.setFullName(googleName);
                user.setEmail(googleEmail);
                user.setPassword(googleId); // Use Google ID as a temporary password, or generate a random one
                user.setRoleID(2); // Assuming roleID 2 for regular users
                userDAO.register(user); // Save new user to your database
                user = userDAO.getUserByEmail(googleEmail); // Retrieve the newly created user
            }

            // Invalidate old session and create a new one
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);

            session.setAttribute("account", user);
            // Also set other user attributes as needed, similar to LoginServlet
            WalletDAO wd = new WalletDAO();
            Wallet wallet = wd.getWalletByUserName(user.getUserName());
            session.setAttribute("wallet", wallet);
            session.setAttribute("imageUser", user.getImage());
            session.setAttribute("address", user.getAddress());
            session.setAttribute("name", user.getFullName());
            session.setAttribute("phone", user.getPhone());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("birthdate", user.getBirthdate());
            
            // Initialize empty cart and wishlist for the new session
            Cart cart = new Cart();
            session.setAttribute("cart", cart);
            session.setAttribute("listItemsInCart", cart.getListItems());
            session.setAttribute("cartSize", cart.getListItems().size());
            Wishlist wishlist = new Wishlist();
            session.setAttribute("wishlist", wishlist);
            session.setAttribute("wishlistSize", wishlist.countItems());

            response.sendRedirect(request.getContextPath() + "/home");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Google login failed: " + e.getMessage());
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
