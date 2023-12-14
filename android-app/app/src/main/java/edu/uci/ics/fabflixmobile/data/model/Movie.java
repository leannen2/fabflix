package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String title;
    private final short year;
    private final String director;
    private ArrayList<String> genres;
    private ArrayList<String> stars;

    public Movie(String id, String name, short year, String director, ArrayList<String> genres, ArrayList<String> stars) {
        this.id = id;
        this.title = name;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public String getId() { return id; }

    public String getTitle() {
        return title;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() { return director; }

    public ArrayList<String> getGenres() { return genres; }

    public ArrayList<String> getStars() { return stars; }
}