import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("customerId");
        String saleDate = request.getParameter("saleDate");

        Map<String, Integer> shoppingCart = (HashMap<String, Integer>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new HashMap<String, Integer>();
            session.setAttribute("shoppingCart", shoppingCart);
        }

        PrintWriter out = response.getWriter();
        JsonArray rowsInserted = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {

            for (Map.Entry<String, Integer> me :
                    shoppingCart.entrySet()) {

//                for (int i = 0; i < me.getValue(); i++) {
                    String query = "INSERT INTO sales (customerId, movieId, saleDate, quantity) " +
//                            "VALUES (" + customerId + "," + me.getKey() + "," + "2023-10-29" + ")";
                            "VALUES(?, ?, ?, ?)";

                    PreparedStatement statement = conn.prepareStatement(query);
                    statement.setInt(1, customerId);
                    statement.setString(2, me.getKey());
                    statement.setString(3, saleDate);
                    statement.setInt(4, me.getValue());

                    System.out.println("update query: " + statement);
                    System.out.println("update returns: " + statement.executeUpdate());
                    JsonObject row = new JsonObject();
                    row.addProperty("customerId", customerId);
                    row.addProperty("movieId", me.getKey());
                    row.addProperty("salesDate", saleDate);
                    row.addProperty("quantity", me.getValue());

                    statement.close();
                    statement = conn.prepareStatement("SELECT LAST_INSERT_ID() AS id");
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        row.addProperty("saleId", resultSet.getInt("id"));
                    }
                    statement.close();
                    resultSet.close();
                    rowsInserted.add(row);
//                }
            }
            out.write(rowsInserted.toString());
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}
