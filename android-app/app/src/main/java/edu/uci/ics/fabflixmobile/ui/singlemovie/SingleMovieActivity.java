package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListViewAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private final String host = "18.118.33.56";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    TextView title;
    TextView year;

    TextView director;

    TextView genres;

    TextView stars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        title = binding.title;
        year = binding.year;
        director = binding.director;
        genres = binding.genres;
        stars = binding.stars;

        Bundle extras = getIntent().getExtras();
        String movieId = "";
        if (extras == null) {
            System.out.println("No movieId in intent");
        } else {
            movieId = extras.getString("movieId");
            fetchAndInsertMovie(movieId);
        }
    }

    public void fetchAndInsertMovie(String movieId) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest movieListRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + movieId,
                response -> {
                    System.out.println("movie response: " + response);
                    try {
                        JSONObject movieJsonObject = new JSONObject(response);
                        Movie movie = generateMovie(movieJsonObject);
                        String directorText = "Directed By: " + movie.getDirector();
                        String genreText = "Genres: " + String.join(", ", movie.getGenres());
                        String starText = "Stars: " + String.join(", ", movie.getStars());
                        title.setText(movie.getTitle());
                        year.setText(movie.getYear() + "");
                        director.setText(directorText);
                        genres.setText(genreText);
                        stars.setText(starText);

                    } catch (Exception e) {
                        System.out.println("exception in fetchAndInsertMovie" + e.toString());
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                final Map<String, String> params = new HashMap<>();
                params.put("id", movieId);
                return params;
            }
        };
        queue.add(movieListRequest);
    }

    private Movie generateMovie(JSONObject movieObject) {
        try {
            JSONArray genres = movieObject.getJSONArray("movie_genres");
            ArrayList<String> genreList = new ArrayList<>();
            for (int i = 0; i < genres.length(); i++) {
                genreList.add(genres.getString(i));
            }

            JSONArray stars = movieObject.getJSONArray("movie_stars");
            ArrayList<String> starList = new ArrayList<>();
            for (int i = 0; i < stars.length(); i++) {
                starList.add(stars.getJSONObject(i).getString("star_name"));
            }

            return new Movie(movieObject.getString("movie_id"),
                    movieObject.getString("movie_title"),
                    Short.parseShort(movieObject.getString("movie_year")),
                    movieObject.getString("movie_director"),
                    genreList,
                    starList);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }
}