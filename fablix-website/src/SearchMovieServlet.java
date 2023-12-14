import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/api/search-movie")
public class SearchMovieServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // setup the response json arrray
        JsonArray jsonArray = new JsonArray();

        // get the query string from parameter
        String query = request.getParameter("query");

        // return the empty json array if query is null or empty
        if (query == null || query.trim().isEmpty()) {
            response.getWriter().write(jsonArray.toString());
            return;
        }

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // example query: SELECT id, title FROM movies WHERE MATCH(title) AGAINST ('+miss* +impo*' IN BOOLEAN MODE) returns 'Mission Impossible'
            String movieQuery = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            String[] prefixes = query.split(" ");
            String prefix = "+" + String.join("* +", prefixes) + "*";
            PreparedStatement statement = conn.prepareStatement(movieQuery);

            statement.setString(1, prefix);
            System.out.println("full text query: " + statement);

            ResultSet searchResults = statement.executeQuery();

            while (searchResults.next()) {
                String movieId = searchResults.getString("id");
                String movieTitle = searchResults.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
            }
            out.write(jsonArray.toString());
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
            System.out.println("full text search done");
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
