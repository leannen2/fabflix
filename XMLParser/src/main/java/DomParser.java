import com.mysql.cj.x.protobuf.MysqlxPrepare;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;


public class DomParser {

    List<Movie> movies = new ArrayList<>();
    List<Cast> casts = new ArrayList<>();
    List <Actor> actors = new ArrayList<>();
    Document dom;

    public void run() {
        // get each Movie element and create a Movie object
        writeToErrorFile("Movie");
        parseMovieDocument("mains243.xml");

        writeToErrorFile("Cast");
        parseCastDocument("casts124.xml");

        writeToErrorFile("Actors");
        parseActorsDocument("actors63.xml");
        System.out.println("Actors size " + actors.size());
        System.out.println("START DATABASE UPDATE");
        updateDatabase();

        // iterate through the list and print the data
        printData();

    }

    private void parseXmlFile(String fileName) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(fileName);

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseMovieDocument(String fileName) {
        parseXmlFile(fileName);
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < nodeList.getLength(); i++) {
            // get the employee element
            Element directorFilmElement = (Element) nodeList.item(i);

            parseDirectorFilmElement(directorFilmElement);
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private void parseDirectorFilmElement(Element directorFilmElement) {

        // for each <employee> element get text or int values of
        // name ,id, age and name
        String director = getTextValue(directorFilmElement, "dirname");
        NodeList filmNodeList = directorFilmElement.getElementsByTagName("film");
        for (int i = 0; i < filmNodeList.getLength(); i++) {
            Element movieElement = (Element) filmNodeList.item(i);
            try {
                Movie parsedMovie = parseMovie(director, movieElement);
                movies.add(parsedMovie);
            } catch (NumberFormatException numberFormatException) {
                String movieTitle = getTextValue(movieElement, "t");
                writeToErrorFile("Movie: " + movieTitle + " has invalid year format. - " + numberFormatException);
            } catch (Exception e) {
                String movieId = getTextValue(movieElement, "fid");
                writeToErrorFile("MovieId: " + movieId + " is missing a required field.");
            }
        }
    }

    private Movie parseMovie(String director, Element movieElement) {
        String id = getTextValue(movieElement, "fid");
        String title = getTextValue(movieElement, "t");
        int year = getIntValue(movieElement, "year");
        ArrayList<String> genres = getGenres(movieElement);
        return new Movie(id, title, director, year, genres);
    }

    private ArrayList<String> getGenres(Element movieElement) {
        ArrayList<String> genres = new ArrayList<>();
        NodeList genreNodeList = movieElement.getElementsByTagName("cat");
        for (int i = 0; i < genreNodeList.getLength(); i++) {
            Element genreElement = (Element) genreNodeList.item(i);
            genres.add(genreElement.getFirstChild().getNodeValue().strip());
        }
        return genres;
    }

    private void parseCastDocument(String fileName) {
        parseXmlFile(fileName);
        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("m");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element castElement = (Element) nodeList.item(i);
            try {
                Cast parsedCast = parseCastElement(castElement);
                casts.add(parsedCast);
            } catch (Exception e) {
//                e.printStackTrace();
                String movieId = getTextValue(castElement, "f");
                writeToErrorFile("Cast row with movieId: " + movieId + " has invalid value(s).");
            }
        }
    }

    private Cast parseCastElement(Element castElement) {
        String movieId = getTextValue(castElement, "f");
        String starName = getTextValue(castElement, "a");

        return new Cast(movieId, starName);
    }

    private void parseActorsDocument(String fileName) {
        parseXmlFile(fileName);
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element actorElement = (Element) nodeList.item(i);
            try {
                Actor parsedActor = parseActorElement(actorElement);
                actors.add(parsedActor);
            } catch (Exception e) {
                writeToErrorFile(e.toString() + " - " + actorElement);
            }
        }
    }

    private Actor parseActorElement(Element actorElement) {
        String name = getTextValue(actorElement, "stagename");
        Integer birthYear;
        try {
            birthYear = getIntValue(actorElement, "dob");
        } catch (Exception e) {
            birthYear = null;
        }


        return new Actor(name, birthYear);
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            textVal = nodeList.item(0).getFirstChild().getNodeValue();

        }
        return textVal;
    }

    private void writeToErrorFile(String message) {
        try {
            FileWriter myWriter = new FileWriter("parseErrors.txt", true);
            myWriter.write(message + "\n");
            myWriter.close();
        } catch (IOException ex) {
            System.out.println("An error occurred while writing to file.");
        }
    }

    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    /**
     * Iterate through the list and print the
     * content to console
     */
    private void printData() {

//        System.out.println("Total parsed " + movies.size() + " movies");
//
//        for (Movie movie : movies) {
//            System.out.println("\t" + movie.toString());
//        }
//
//        System.out.println("Total parsed " + casts.size() + " casts");
//        for (Cast cast : casts) {
//            System.out.println("\t" + cast.toString());
//        }
    }

    private void updateDatabase() {
          try (Connection conn = DriverManager.getConnection("jdbc:" + "mysql" + ":///" + "moviedb" + "?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false",
                  "mytestuser", "My6$Password")) {
              updateMovieBatch(conn);
              updateStarBatch(conn);
              updateStarsInMoviesBatch(conn);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

    private void updateMovieBatch(Connection conn) {
        try {
            System.out.println("GET GENRES HASHSET");
            HashSet<String> genresSet = new HashSet<>();
            String genresQuery = "SELECT name from genres";
            PreparedStatement statement = conn.prepareStatement(genresQuery);
            ResultSet genresResultSet = statement.executeQuery();
            while (genresResultSet.next()) {
                genresSet.add(genresResultSet.getString("name"));
            }
            statement.close();
            System.out.println(genresSet);

            System.out.println("INSERT MOVIES");
            String insertMovie = "INSERT IGNORE INTO movies (id, title, year, director) " +
                    "VALUES (?, ?, ?, ?)";
            statement = conn.prepareStatement(insertMovie);

            for (Movie movie: movies) {
                if (movie.getDirector() == null) {
                    continue;
                }
//                System.out.println(movie.toString());
                statement.setString(1, movie.getId());
                statement.setString(2, movie.getTitle());
                statement.setInt(3, movie.getYear());
                statement.setString(4, movie.getDirector());
                if (statement.executeUpdate() > 0) {
                    updateGenreBatch(conn, genresSet, movie.getId(), movie.getGenres());
                }
//                statement.addBatch();


                //insertIntoRating(conn, movie.getId());
            }
//            statement.executeBatch();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertIntoRating(Connection conn, String movieId) {
        try {
            String insertRatingQuery = "INSERT INTO ratings (movieId, rating, numVotes) " +
                    "VALUES (?, null, null)";
            PreparedStatement statement = conn.prepareStatement(insertRatingQuery);
            statement.setString(1, movieId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGenreBatch(Connection conn, HashSet<String> genresSet, String movieId, ArrayList<String> genres) {
        try {
            String insertGenreInMoviesQuery = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) " +
                    "VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(insertGenreInMoviesQuery);

            for (String genre: genres) {
                if (!genresSet.contains(genre)) {
                    insertIntoGenre(conn, genre);
                    genresSet.add(genre);
                }
                statement.setInt(1, getGenreId(conn, genre));
                statement.setString(2, movieId);
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getGenreId(Connection conn, String name) {
        try {
            String genreIdQuery = "SELECT id from genres where name = ?";
            PreparedStatement statement = conn.prepareStatement(genreIdQuery);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void insertIntoGenre(Connection conn, String name) {
        try {
            String insertGenreQuery = "INSERT IGNORE INTO genres (name) " +
                    "VALUES (?)";
            PreparedStatement statement = conn.prepareStatement(insertGenreQuery);
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStarBatch(Connection conn) {
        System.out.println("INSERT STARS");
        try {
//            String starIdQuery = "SELECT MAX(id) from stars";
//            PreparedStatement statement = conn.prepareStatement(starIdQuery);
//            ResultSet resultSet = statement.executeQuery();
//            resultSet.next();
//            String starId = resultSet.getString("max(id)");
//            Integer starIdNum = Integer.parseInt(starId.substring(2)) + 1;

            HashSet<String> starNames = new HashSet<>();
            String starsQuery = "SELECT name from stars";
            PreparedStatement statement = conn.prepareStatement(starsQuery);
            ResultSet starResultSet = statement.executeQuery();
            while (starResultSet.next()) {
                starNames.add(starResultSet.getString("name"));
            }

            String insertStars = "CALL add_star(?, ?, @message)";
            statement = conn.prepareStatement(insertStars);


            for (Actor actor: actors) {
                String name = actor.getName();
                if (!starNames.contains(name)) {
                    statement.setString(1, name);
                    statement.setObject(2, actor.getBirthYear());
//                    statement.executeUpdate();
                    statement.addBatch();
                    starNames.add(name);
//                    starIdNum += 1;
                } else {
                    writeToErrorFile("Duplicate actor name: " + name);
                }
            }
            System.out.println("star execute batch: " + statement.executeBatch());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStarsInMoviesBatch(Connection conn) {
        System.out.println("INSERT STARS IN MOVIES");
        try {
            String insertStarQuery = "CALL add_star_in_movie(?, ?)";
            PreparedStatement statement = conn.prepareStatement(insertStarQuery);
            for (Cast cast: casts) {
                // System.out.println(cast.toString());
                statement.setString(1, cast.getActorName());
                statement.setString(2, cast.getMovieId());
//                try {
//                    statement.executeUpdate();
//                } catch (Exception e){
//                    writeToErrorFile(e.toString());
//                }
                statement.executeUpdate();
//                statement.addBatch();
            }
            System.out.println("EXECUTING STARS IN MOVIES BATCH");
//            statement.executeBatch();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStarId(Connection conn, String name) {
        try {
            String starIdQuery = "SELECT id from stars where name = ?";
            PreparedStatement statement = conn.prepareStatement(starIdQuery);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception{
        try {
            FileWriter myWriter = new FileWriter("parseErrors.txt");
            myWriter.write("");
            myWriter.close();
        } catch (IOException ex) {
            System.out.println("An error occurred while writing to file.");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");

        // create an instance
        DomParser domParserExample = new DomParser();

        // call run example
        domParserExample.run();
    }

}
