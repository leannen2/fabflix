import java.util.ArrayList;
import java.util.List;

public class Movie {
    private final String id;

    private final String title;

    private final String director;

    private final int year;

    private ArrayList<String> genres;

    public Movie(String id, String title, String director, int year, ArrayList<String> genres) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.year = year;
        this.genres = genres;
    }

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getDirector() { return director; }

    public int getYear() { return year; }

    public ArrayList<String> getGenres() { return genres; }

    public String toString() {
        return  "Id: " + getId() + ", " +
                "Title: " + getTitle() + ", " +
                "Director: " + getDirector() + ", " +
                "Year: " + getYear() + ", " +
                "Genres: " + String.join(", ", genres) + ".";
    }
}
