package perfumeshop.controller.web.cart_wishlist;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import perfumeshop.dal.ProductDAO;
import perfumeshop.model.Item;
import perfumeshop.model.Product;
import perfumeshop.model.Wishlist;

@WebServlet(name = "WishlistServlet", urlPatterns = {"/wishlist"})
public class WishlistServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Wishlist wishlist = null;
        Object o = session.getAttribute("wishlist");

        if (o != null) {
            wishlist = (Wishlist) o;
        } else {
            wishlist = new Wishlist();
        }

        ProductDAO pd = new ProductDAO();

        String role = request.getParameter("role");
        switch (role) {
            case "add": {
                String tid = request.getParameter("id");
                int id;
                try {
                    id = Integer.parseInt(tid);
                    Product p = pd.getProductByID(id);
                    Item t = new Item(p, 1); // Quantity is always 1 for wishlist
                    wishlist.addItem(t);
                } catch (Exception e) {
                }
                session.setAttribute("wishlist", wishlist);
                session.setAttribute("wishlistSize", wishlist.countItems());
                request.getRequestDispatcher("ajax/header_right_ajax.jsp").forward(request, response);
                break;
            }
            case "remove": {
                String tRid = request.getParameter("rid");
                int rid;
                try {
                    rid = Integer.parseInt(tRid);
                    wishlist.removeItem(rid);
                } catch (Exception e) {
                }
                session.setAttribute("wishlist", wishlist);
                session.setAttribute("wishlistSize", wishlist.countItems());
                request.getRequestDispatcher("ajax/header_right_ajax.jsp").forward(request, response);
                break;
            }
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}

