import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartAddItemServlet", urlPatterns = "/api/shopping-cart/add-item")
public class ShoppingCartAddItemServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        System.out.println("new item being added to shopping cart: " + item);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        Map<String, Integer> shoppingCart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new HashMap<String, Integer>();
            shoppingCart.put(item, 1);
            session.setAttribute("shoppingCart", shoppingCart);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (shoppingCart) {
                if (!shoppingCart.containsKey(item)) {
                    shoppingCart.put(item, 0);
                }
                shoppingCart.put(item, shoppingCart.get(item)+1);
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonObject shoppingCartJsonObject = new JsonObject();
        for (Map.Entry<String, Integer> me :
                shoppingCart.entrySet()) {

            shoppingCartJsonObject.addProperty(me.getKey(), me.getValue());
        }
        responseJsonObject.add("shoppingObject", shoppingCartJsonObject);

        response.getWriter().write(responseJsonObject.toString());
    }
}
