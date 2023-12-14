import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT * FROM movies AS m " +
                    "LEFT JOIN ratings as r ON m.id = r.movieId " +
                    "WHERE m.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet resultSet = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            while (resultSet.next()) {
                String movieId = resultSet.getString("id");
                String movieTitle = resultSet.getString("title");
                String movieYear = resultSet.getString("year");
                String movieDirector = resultSet.getString("director");
                String rating = resultSet.getString("rating");
                BigDecimal moviePrice = resultSet.getBigDecimal("price");

                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", rating);
                jsonObject.addProperty("movie_price", moviePrice);
            }

            resultSet.close();

            String genresQuery = "select g.name as genreName from genres as g, genres_in_movies as gim where g.id = gim.genreId and gim.movieId = ? order by genreName";
            statement = conn.prepareStatement(genresQuery);
            statement.setString(1, id);
            resultSet = statement.executeQuery();

            JsonArray genresArray = new JsonArray();

            while (resultSet.next()) {
                genresArray.add(resultSet.getString("genreName"));
            }
            resultSet.close();

            jsonObject.add("movie_genres", genresArray);

            String starsQuery = "select s.id as starId, s.name as starName, count(*) as c "
                + "from stars_in_movies as sim, stars as s "
                + "where s.id = sim.starId and sim.starId in ("
                + "select sim.starId "
                + "from stars_in_movies as sim "
                + "where sim.movieId = ? )"
                + "group by starId, starName "
                + "order by c desc, starName asc ";
            statement = conn.prepareStatement(starsQuery);
            statement.setString(1, id);
            resultSet = statement.executeQuery();

            JsonArray starsArray = new JsonArray();
            while (resultSet.next()) {
                JsonObject starObject = new JsonObject();
                starObject.addProperty("star_id", resultSet.getString("starId"));
                starObject.addProperty("star_name", resultSet.getString("starName"));
                starsArray.add(starObject);
            }
            resultSet.close();
            statement.close();
            jsonObject.add("movie_stars", starsArray);

            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
