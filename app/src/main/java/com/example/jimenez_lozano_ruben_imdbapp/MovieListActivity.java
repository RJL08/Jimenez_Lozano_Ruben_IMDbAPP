package com.example.jimenez_lozano_ruben_imdbapp;

import static android.content.Intent.getIntent;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Configurar adaptador
        movieAdapter = new MovieAdapter(
                moviesList,
                movie -> {
                    // Manejar clic en una película (por ejemplo, abrir detalles)
                    Intent detailIntent = new Intent(this, MovieDetailsActivity.class);
                    detailIntent.putExtra("movie_id", movie.getId());
                    startActivity(detailIntent);
                },
                movie -> {
                    // Manejar clic largo para agregar a favoritos
                    Toast.makeText(this, "Agregado a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                }
        );
        moviesRecyclerView.setAdapter(movieAdapter);
    }
}