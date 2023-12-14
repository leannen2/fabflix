import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        Map<String, Integer> shoppingCart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new HashMap<String, Integer>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonObject shoppingCartJsonObject = new JsonObject();
        for (Map.Entry<String, Integer> me :
                shoppingCart.entrySet()) {

            shoppingCartJsonObject.addProperty(me.getKey(), me.getValue());
        }
        response.setStatus(200);
        responseJsonObject.add("shoppingObject", shoppingCartJsonObject);

        response.getWriter().write(responseJsonObject.toString());
    }
}
