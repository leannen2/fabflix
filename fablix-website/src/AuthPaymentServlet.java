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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthPaymentServlet", urlPatterns = "/api/auth-payment")
public class AuthPaymentServlet extends HttpServlet {
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
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNum");
        String expirationDate = request.getParameter("expirationDate");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "select id as count from creditcards "
                    + "where firstName = ? and lastName = ? and id = ? and expiration = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, creditCardNumber);
            statement.setString(4, expirationDate);

            System.out.println("Auth Payment Servlet: " + statement);

            ResultSet resultSet = statement.executeQuery();
            JsonObject jsonObject = new JsonObject();
            if (resultSet.next()) {
                jsonObject.addProperty("payment_auth", "valid");
            } else {
                jsonObject.addProperty("payment_auth", "failed");
            }
//            while (resultSet.next()) {
//                if (resultSet.getInt("count") == 0) {
//                    jsonObject.addProperty("payment_auth", "failed");
//                } else if (resultSet.getInt("count") == 1) {
//                    jsonObject.addProperty("payment_auth", "valid");
//                }
//            }
            System.out.println(resultSet.getFetchSize());

            resultSet.close();
            statement.close();
            out.write(jsonObject.toString());
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
