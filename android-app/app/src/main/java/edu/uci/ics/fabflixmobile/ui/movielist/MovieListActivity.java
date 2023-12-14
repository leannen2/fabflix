package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
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
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "18.118.33.56";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    private int pageNumber = 1;
    private ArrayList<Movie> movies = new ArrayList<>();
    private MovieListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setMovieList();

        final Button previousButton = binding.previous;
        final Button nextButton = binding.next;
        previousButton.setOnClickListener(view -> handlePreviousClick());
        nextButton.setOnClickListener(view -> handleNextClick());
    }

    public void setMovieList() {
        Bundle extras = getIntent().getExtras();
        String query = "";
        if (extras != null) {
            query = extras.getString("query");
            fetchMovies(query);
        }
        System.out.println("movies list: " + movies);
        adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %d", position, movie.getTitle(), movie.getYear());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            handleMovieClick(position);
        });
    }


    public void fetchMovies(String query) {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest movieListRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movie-list?title=" + query + "&pageNumber=" + pageNumber + "&numMoviesDisplay=10",
                response -> {
                    System.out.println("movie list response: " + response);
                    try {
                        JSONArray responseJsonArray = new JSONArray(response);
//                        finish();
                        for (int i = 0; i < responseJsonArray.length(); i++) {
                            movies.add(generateMovie(responseJsonArray.getJSONObject(i)));
                        }
                        System.out.println("movies list: " + movies);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        System.out.println("exception in fetchMovies" + e.toString());
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("title", query);
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

    public void handleMovieClick(int position) {
        Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
        SingleMoviePage.putExtra("movieId", movies.get(position).getId());
        startActivity(SingleMoviePage);
    }

    public void handlePreviousClick() {
        System.out.println("previous clicked");
        if (pageNumber > 1) {
            pageNumber -= 1;
            movies.clear();
            Bundle extras = getIntent().getExtras();
            String query = "";
            if (extras != null) {
                query = extras.getString("query");
                fetchMovies(query);
            } else {
                @SuppressLint("DefaultLocale") String message = String.format("On first page.");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleNextClick() {
        System.out.println("next clicked");
        if (movies.size() == 10) {
            pageNumber += 1;
            movies.clear();
            Bundle extras = getIntent().getExtras();
            String query = "";
            if (extras != null) {
                query = extras.getString("query");
                fetchMovies(query);
            }
        } else {
            @SuppressLint("DefaultLocale") String message = String.format("End of list. No more pages.");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}