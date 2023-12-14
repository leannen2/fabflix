import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartRemoveItemServlet", urlPatterns = "/api/shopping-cart/remove-item")
public class ShoppingCartRemoveItemServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        String removeAllOfItem = request.getParameter("removeAll");
        System.out.println("item being removed from shopping cart: " + item);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        Map<String, Integer> shoppingCart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("errorMessage",  "shopping cart does not exist when item is being removed");
            response.setStatus(500);
            return;
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (shoppingCart) {
                if (removeAllOfItem != null && removeAllOfItem.equals("true")) {
                    shoppingCart.remove(item);
                } else {
                    Integer newQuant = shoppingCart.get(item)-1;
                    if (newQuant > 0) {
                        shoppingCart.put(item, shoppingCart.get(item)-1);
                    } else {
                        shoppingCart.remove(item);
                    }
                }


            }
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
