package com.example.jimenez_lozano_ruben_imdbapp;

import static android.content.Intent.getIntent;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import com.example.jimenez_lozano_ruben_imdbapp.ui.adapter.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

public class MovieListActivity extends AppCompatActivity {

    private RecyclerView moviesRecyclerView;
    private MovieAdapter movieAdapter;
    private List<Movies> moviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        // Inicializar RecyclerView
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));




        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("movies_list")) {
            moviesList = intent.getParcelableArrayListExtra("movies_list");
        } else {
            moviesList = new ArrayList<>();
        }

        // Inicializar FavoritesManager
        FavoritesManager favoritesManager = new FavoritesManager(this);

        // Configurar el adaptador
        movieAdapter = new MovieAdapter(
                moviesList,
                movie -> {
                    // onClick: Abrir detalles de la película
                    Intent detailIntent = new Intent(this, MovieDetailsActivity.class);
                    detailIntent.putExtra("movie", movie);
                    startActivity(detailIntent);
                },
                movie -> {
                    // onLongClick: Agregar película a favoritos
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    String userEmail = prefs.getString("userEmail", "");

                    if (userEmail.isEmpty()) {
                        Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean added = favoritesManager.addFavorite(
                            userEmail,
                            movie.getTitle(),
                            movie.getImageUrl(),
                            movie.getReleaseYear(),
                            movie.getRating()
                    );

                    if (added) {
                        Toast.makeText(this, "Película agregada a favoritos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al agregar a favoritos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        moviesRecyclerView.setAdapter(movieAdapter);
    }
}