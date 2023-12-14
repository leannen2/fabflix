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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet{
    private DataSource dataSource;

    public void init(ServletConfig config) {

        try {
            super.init(config);
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long startTs = System.nanoTime();
        long tj = 0;
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameters
        String genre = request.getParameter("genre");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String alpha = request.getParameter("alpha");
        Integer numOfMoviesDisplay = Integer.parseInt(request.getParameter("numMoviesDisplay"));
        Integer pageNumber = Integer.parseInt(request.getParameter("pageNumber"));
        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");

        System.out.println("genre: " + genre + " title: " + title + " year: " + year + " starName: " + starName + " director: " + director);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Connection="+conn);
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String selectQuery =  "select * from movies as m ";
            ArrayList<String> whereQuery = new ArrayList<>();
            if (genre != null) {
                // selectQuery += ", genres as g, genres_in_movies as gim ";
                selectQuery += "INNER JOIN genres_in_movies AS gim ON gim.movieId = m.id " +
                        "INNER JOIN genres AS g ON g.id = gim.genreId ";
                whereQuery.add(" g.name = '" + genre + "' ");
            }
            if (title != null) {
                String[] prefixes = title.split(" ");
                String prefix = "+" + String.join("* +", prefixes) + "*";
                whereQuery.add(" MATCH(title) AGAINST ('" + prefix + "' IN BOOLEAN MODE) ");
            }
            if (year != null) {
                whereQuery.add(" m.year = ? ");
            }
            if (director != null) {
                whereQuery.add(" m.director LIKE '%" + director + "%' ");
            }
            if (starName != null) {
                whereQuery.add(" exists (select s.name from stars as s, stars_in_movies as sim where sim.movieId = m.id and sim.starId = s.id and s.name like '%" + starName + "%') ");
            }
            if (alpha != null) {
                if (alpha.equals("*")) {
                    whereQuery.add(" m.title REGEXP '^[^A-Za-z0-9]' ");
                } else {
                    whereQuery.add(" m.title LIKE '" + alpha + "%' ");
                }
            }
            String query = selectQuery + " LEFT JOIN ratings as r ON m.id = r.movieId " +
                    (!whereQuery.isEmpty() ? "WHERE" + String.join("and", whereQuery) : "");

            query += "order by " + order1 + ", " + order2 + " ";

            Integer offsetNum = (pageNumber-1) * numOfMoviesDisplay;
            query += "limit " + numOfMoviesDisplay + " offset " + offsetNum;

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            if (year != null) {
                statement.setInt(1, parseInt(year));
            }
//            System.out.println(statement);

            // Perform the query
            long startTj = System.nanoTime();
            ResultSet resultSet = statement.executeQuery();
            long endTj = System.nanoTime();
            tj += endTj - startTj;
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (resultSet.next()) {
                String movieId = resultSet.getString("id");
                String movieTitle = resultSet.getString("title");
                String movieYear = resultSet.getString("year");
                String movieDirector = resultSet.getString("director");
                String rating = resultSet.getString("rating");

                String genreQuery = "select g.name as genreName " +
                        "from genres as g, genres_in_movies as gim " +
                        "where g.id = gim.genreId and gim.movieId = ? " +
                        "order by genreName limit 3";

                PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
                genreStatement.setString(1, movieId);

                startTj = System.nanoTime();
                ResultSet genreSet = genreStatement.executeQuery();
                endTj = System.nanoTime();
                tj += endTj - startTj;

                JsonArray genreArray = new JsonArray();
                while (genreSet.next()) {
                    genreArray.add(genreSet.getString("genreName"));
                }
                genreSet.close();
                genreStatement.close();

                String starsQuery = "select s.id as starId, s.name as starName, count(sim.movieId) as moviesPlayed " +
                        "from stars as s, stars_in_movies as sim " +
                        "where s.id = sim.starId and sim.movieId = ? " +
                        "group by starId, starName " +
                        "order by starName asc, moviesPlayed " +
                        "desc limit 3";
                PreparedStatement starsStatement = conn.prepareStatement(starsQuery);
                starsStatement.setString(1, movieId);

                startTj = System.nanoTime();
                ResultSet starsSet = starsStatement.executeQuery();
                endTj = System.nanoTime();
                tj += endTj - startTj;

                JsonArray starsArray = new JsonArray();
                while (starsSet.next()) {
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("star_id", starsSet.getString("starId"));
                    starObject.addProperty("star_name", starsSet.getString("starName"));
                    starsArray.add(starObject);
                }
                starsSet.close();
                starsStatement.close();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("rating", rating);
                jsonObject.add("movie_genres", genreArray);
                jsonObject.add("movie_stars", starsArray);

                jsonArray.add(jsonObject);
            }
            resultSet.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
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
            System.out.println("movie list servlet done");
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources
        long ts = System.nanoTime() - startTs;

        try {
            String contextPath = getServletContext().getRealPath("/");
            String path = "/home/ubuntu/log.txt";
//            System.out.println(path);
            File file = new File(path);
            FileWriter myWriter = new FileWriter(file, true);
            myWriter.write(ts + " " + tj + "\n");
            myWriter.close();
//            System.out.println(ts + " " + tj);
        } catch (IOException ex) {
            System.out.println("An error occurred while writing to file.");
            System.out.println(ex.toString());
	    }
    }
}
