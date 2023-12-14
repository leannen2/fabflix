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
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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

        String emailAddress = request.getParameter("emailAddress");
        String password = request.getParameter("password");
        String recaptcha = request.getParameter("recaptcha");

        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        if (recaptcha == null) {
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", "complete recaptcha verification");
                out.write(jsonObject.toString());

                // Log error to localhost log
                request.getServletContext().log("Error:", e);
                // Set response status to 500 (Internal Server Error)
                response.setStatus(500);

                out.close();
                return;
            }
        } else {
            System.out.println("skip recaptcha verification");
        }


        try (Connection conn = dataSource.getConnection()) {
            String query = "select * from customers where email = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, emailAddress);

            ResultSet resultSet = statement.executeQuery();
            System.out.println("login result set" + resultSet);
            JsonObject jsonObject = new JsonObject();
            boolean success = false;
            if (resultSet.next()) {
                String encryptedPassword = resultSet.getString("password");
                System.out.println("encrypted password " + encryptedPassword);
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                System.out.println("success " + success);
            }

            if (!success) {
                jsonObject.addProperty("login_auth", "failed");
            } else {
                HttpSession session = request.getSession(true);
                session.setAttribute("customerId", resultSet.getInt("id"));
                jsonObject.addProperty("login_auth", "valid");
            }

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
