package com.example.jimenez_lozano_ruben_imdbapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.jimenez_lozano_ruben_imdbapp.api.IMDBApiService;
import com.example.jimenez_lozano_ruben_imdbapp.ui.MovieOverviewResponse;

import retrofit2.Response;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieDetailsActivity extends AppCompatActivity {
    private ImageView imageMovie;
    private TextView titleMovie, releaseDate, rating, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Inicializar vistas
        imageMovie = findViewById(R.id.image_movie);
        titleMovie = findViewById(R.id.title_movie);
        releaseDate = findViewById(R.id.release_date_view);
        rating = findViewById(R.id.rating_view);
        description = findViewById(R.id.description_view);

        // Obtener datos desde el Intent
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("movie_id");
        String movieTitle = intent.getStringExtra("movie_title");
        String movieImage = intent.getStringExtra("movie_image");

        // Mostrar datos iniciales (título e imagen)
        titleMovie.setText(movieTitle != null ? movieTitle : "Título no disponible");
        Glide.with(this)
                .load(movieImage != null ? movieImage : R.drawable.esperando) // Imagen por defecto si no hay URL
                .placeholder(R.drawable.esperando)
                .error(R.drawable.esperando)
                .into(imageMovie);

        // Verificar si hay un ID de película para buscar más detalles
        if (movieId != null) {
            fetchMovieDetails(movieId);
        } else {
            Toast.makeText(this, "Error: No se encontró información de la película", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMovieDetails(String movieId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IMDBApiService apiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = apiService.getMovieOverview(
                movieId,
                "3ef3f2c2a3msh17da27eb24608e1p12db6bjsn62d2b74752ff",
                "imdb-com.p.rapidapi.com"
        );

        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse movie = response.body();

                    String releaseDateText = (movie.data.title.releaseDate != null) ?
                            movie.data.title.releaseDate.day + "/" +
                                    movie.data.title.releaseDate.month + "/" +
                                    movie.data.title.releaseDate.year :
                            "Fecha no disponible";

                    String ratingText = (movie.data.title.ratingsSummary != null &&
                            movie.data.title.ratingsSummary.aggregateRating != null) ?
                            String.valueOf(movie.data.title.ratingsSummary.aggregateRating) :
                            "No hay valoraciones disponibles";

                    String descriptionText = (movie.data.title.plot != null &&
                            movie.data.title.plot.plotText != null) ?
                            movie.data.title.plot.plotText.plainText :
                            "Sin descripción";

                    // Log para depuración
                    Log.d("MOVIE_DETAILS", "Release Date: " + releaseDateText);
                    Log.d("MOVIE_DETAILS", "Rating: " + ratingText);
                    Log.d("MOVIE_DETAILS", "Description: " + descriptionText);

                    // Actualizar la UI
                    runOnUiThread(() -> {
                        releaseDate.setText("Release Date: " + releaseDateText);
                        rating.setText("Rating: " + ratingText);
                        description.setText(descriptionText);
                    });
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error en la llamada: " + t.getMessage());
            }
        });
    }



}