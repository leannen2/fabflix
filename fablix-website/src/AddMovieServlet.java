import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/add-movie")
public class AddMovieServlet extends HttpServlet {
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
        PrintWriter out = response.getWriter();

        String movieTitle = request.getParameter("movieTitle");
        String movieDirector = request.getParameter("movieDirector");
        String movieYear = request.getParameter("movieYear");
        String starName = request.getParameter("starName");
        String genre = request.getParameter("genre");

        JsonObject responseJsonObject = new JsonObject();


        try (Connection conn = dataSource.getConnection()) {
            String addMovieCall = "CALL add_movie(?, ?, ?, ?, ?, @message)";
            PreparedStatement statement = conn.prepareStatement(addMovieCall);
            statement.setString(1, movieTitle);
            statement.setInt(2, Integer.parseInt(movieYear));
            statement.setString(3, movieDirector);
            statement.setString(4, starName);
            statement.setString(5, genre);
            int numberOfUpdates = statement.executeUpdate();
            System.out.println("Add Movie Updates: " + numberOfUpdates);
            statement.close();

            String callMessage = "SELECT @message";
            statement = conn.prepareStatement(callMessage);
            ResultSet messageResultSet = statement.executeQuery();
            if (messageResultSet.next()) {
                String message = messageResultSet.getString("@message");
                responseJsonObject.addProperty("message", message);
            }
            out.write(responseJsonObject.toString());
            response.setStatus(200);
            statement.close();

        } catch (Exception e){
            responseJsonObject.addProperty("errorMessage", e.getMessage());
            out.write(responseJsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }


        response.getWriter().write(responseJsonObject.toString());
    }
}
