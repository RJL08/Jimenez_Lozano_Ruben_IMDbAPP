package com.example.jimenez_lozano_ruben_imdbapp;

import static android.content.Intent.getIntent;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

        /* Inicializar FavoritesManager
        FavoritesManager favoritesManager = new FavoritesManager(this);
        */



        // Configurar el adaptador
        movieAdapter = new MovieAdapter(
                moviesList,
                movie -> {
                    // onClick: Abrir detalles de la película
                    Intent detailIntent = new Intent(this, MovieDetailsActivity.class);
                    detailIntent.putExtra("movie", movie); // Mantiene la funcionalidad Parcelable
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

                    FavoritesManager favoritesManager = new FavoritesManager(this);

                    // Obtener la lista de favoritos actuales
                    Cursor cursor = favoritesManager.getFavoritesCursor(userEmail);
                    List<Movies> existingFavorites = favoritesManager.getFavoritesList(cursor);

                    // Cerrar el cursor
                    if (cursor != null) {
                        cursor.close();
                    }

                    // Validar duplicados
                    for (Movies favorite : existingFavorites) {
                        if (favorite.getTitle().equals(movie.getTitle())) {
                            Toast.makeText(this, "Esta película ya está en favoritos", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Validar límite de favoritos
                    if (existingFavorites.size() >= 6) {
                        Toast.makeText(this, "No puedes añadir más de 6 películas a favoritos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        // Agregar a favoritos
                        boolean added = favoritesManager.addFavorite(
                                movie.getId(),              // Nuevo argumento: ID de la película
                                userEmail,                  // Email del usuario
                                movie.getTitle(),           // Título de la película
                                movie.getImageUrl(),        // URL de la imagen
                                movie.getReleaseYear(),     // Fecha de lanzamiento
                                movie.getRating(),          // Puntuación
                                movie.getOverview()         // Nuevo argumento: Descripción de la película
                        );

                        if (added) {
                            Toast.makeText(this, "Película agregada a favoritos", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al agregar a favoritos", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MovieListActivity", "Error al agregar a favoritos", e);
                    }
                }
        );

        moviesRecyclerView.setAdapter(movieAdapter);
    }
}