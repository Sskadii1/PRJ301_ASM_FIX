package perfumeshop.controller.web.login;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// CÁC IMPORT CẦN THAY ĐỔI VÀ THÊM MỚI
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow; // QUAN TRỌNG: Import lớp này
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List; // Thêm import này

@WebServlet(name = "GoogleLoginServlet", urlPatterns = {"/google-login"})
public class GoogleLoginServlet extends HttpServlet {

    private static final String CLIENT_SECRET_FILE = "client_secret.json"; // Place this in web/WEB-INF/
    private static final List<String> SCOPES = Arrays.asList("email", "profile");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    private GoogleClientSecrets clientSecrets;
    private GoogleAuthorizationCodeFlow flow; // Biến này sẽ giữ cấu hình xác thực

    @Override
    public void init() throws ServletException {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/" + CLIENT_SECRET_FILE)));

            // *** PHẦN SỬA ĐỔI QUAN TRỌNG ***
            // Khởi tạo đối tượng 'flow' một lần duy nhất khi servlet được tải
            // Đối tượng này chứa tất cả cấu hình cần thiết
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setAccessType("offline") // Chuyển setAccessType vào đây
                    .build();

        } catch (Exception e) {
            throw new ServletException("Error loading client secrets or initializing flow", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String redirectUri = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/google-oauth2-callback";

        // *** PHẦN SỬA ĐỔI QUAN TRỌNG ***
        // Sử dụng đối tượng 'flow' đã được khởi tạo trong init() để tạo URL
        String authorizationUrl = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();

        response.sendRedirect(authorizationUrl);
    }
}