package com.example.jimenez_lozano_ruben_imdbapp.ui.home;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jimenez_lozano_ruben_imdbapp.MovieDetailsActivity;
import com.example.jimenez_lozano_ruben_imdbapp.api.IMDBApiService;
import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.databinding.FragmentHomeBinding;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import com.example.jimenez_lozano_ruben_imdbapp.models.MovieOverviewResponse;
import com.example.jimenez_lozano_ruben_imdbapp.ui.adapter.MovieAdapter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList = new ArrayList<>();
    private FavoritesManager favoritesManager;
    private Movies movie;
    private List<Movies> cachedMovies = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar RecyclerView
        recyclerView = binding.recyclerViewTopMovies;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // Grid de 2 columnas

        // Inicializar el adaptador con la lógica de clics y clics largos
        adapter = new MovieAdapter(movieList, this::onMovieClick, this::onMovieLongClick);
        recyclerView.setAdapter(adapter);

        // Llamar al API para cargar el Top 10
        fetchTopMovies();

        return root;
    }

    private void fetchTopMovies() {
        // Crear un nuevo hilo para la solicitud
        new Thread(() -> {
            AsyncHttpClient client = new DefaultAsyncHttpClient();
            try {
                // Realizar la solicitud
                client.prepare("GET", "https://imdb-com.p.rapidapi.com/title/get-top-meter?topMeterTitlesType=ALL")
                        .setHeader("x-rapidapi-key", "8c8a3cbdefmsh5b39dc7ade88a71p1ca1bdjsn245a12339ee4")
                        .setHeader("x-rapidapi-host", "imdb-com.p.rapidapi.com")
                        .execute()
                        .toCompletableFuture()
                        .thenAccept(response -> {
                            if (response.getStatusCode() == 200) {
                                try {
                                    // Parsear la respuesta JSON
                                    String responseBody = response.getResponseBody();
                                    parseTopMovies(responseBody);
                                } catch (Exception e) {
                                    Log.e("API_ERROR", "Error al parsear la respuesta: " + e.getMessage());
                                }
                            } else {
                                Log.e("API_ERROR", "Error en la respuesta: " + response.getStatusCode());
                            }
                        })
                        .join();
            } catch (Exception e) {
                Log.e("API_ERROR", "Error en la llamada: " + e.getMessage());
            } finally {
                try {
                    client.close(); // Cerrar el cliente
                } catch (IOException e) {
                    Log.e("API_ERROR", "Error al cerrar el cliente: " + e.getMessage());
                }
            }
        }).start();
    }

    private void parseTopMovies(String responseBody) {
        try {
            // Parsear JSON
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray edges = jsonResponse.getJSONObject("data")
                    .getJSONObject("topMeterTitles")
                    .getJSONArray("edges");

            // Crear lista de películas
            List<Movies> tempMovieList = new ArrayList<>();
            int maxResults = Math.min(edges.length(), 10);
            for (int i = 0; i < maxResults; i++) {
                JSONObject node = edges.getJSONObject(i).getJSONObject("node");

                Movies movie = new Movies();
                movie.setId(node.optString("id", "N/A"));
                movie.setTitle(
                        node.getJSONObject("titleText").optString("text", "Título no disponible")
                );
                movie.setImageUrl(
                        node.has("primaryImage") && node.getJSONObject("primaryImage").has("url")
                                ? node.getJSONObject("primaryImage").getString("url")
                                : "" // URL por defecto vacía si no hay imagen
                );
                // Obtener la fecha completa (día/mes/año)
                if (node.has("releaseDate")) {
                    JSONObject releaseDate = node.getJSONObject("releaseDate");
                    String day = releaseDate.has("day") ? String.valueOf(releaseDate.getInt("day")) : null;
                    String month = releaseDate.has("month") ? String.valueOf(releaseDate.getInt("month")) : null;
                    String year = releaseDate.has("year") ? String.valueOf(releaseDate.getInt("year")) : null;

                    // Concatenar solo si los valores están disponibles
                    if (day != null && month != null && year != null) {
                        movie.setReleaseYear(day + "/" + month + "/" + year);
                    } else {
                        movie.setReleaseYear(null); // Dejar null si falta algún dato
                    }
                } else {
                    movie.setReleaseYear(null);
                }

                // Verifica si el campo rating existe antes de asignarlo
                // Obtener el rating*****
                // Obtener el rating
                if (node.has("ratingsSummary") && node.getJSONObject("ratingsSummary").has("aggregateRating")) {
                    String rating = node.getJSONObject("ratingsSummary").getString("aggregateRating");
                    movie.setRating(rating);
                    Log.d("MovieRating", "Película: " + movie.getTitle() + ", Rating: " + rating);
                } else {
                    movie.setRating(null); // Dejar null si no hay rating
                    Log.d("MovieRating", "Película: " + movie.getTitle() + ", Rating no disponible");
                }

                // Obtener el overview (descripción de la película)
                if (node.has("plot") && node.getJSONObject("plot").has("plotText")) {
                    String overview = node.getJSONObject("plot").getJSONObject("plotText").getString("plainText");
                    movie.setOverview(overview);
                    Log.d("MovieOverview", "Película: " + movie.getTitle() + ", Overview: " + overview);
                } else {
                    movie.setOverview(null); // Dejar null si no hay overview
                    Log.d("MovieOverview", "Película: " + movie.getTitle() + ", Overview no disponible");
                }

                tempMovieList.add(movie);
            }

            // Actualizar la lista en el hilo principal
            requireActivity().runOnUiThread(() -> {
                movieList.clear();
                movieList.addAll(tempMovieList);
                adapter.notifyDataSetChanged(); // Notificar cambios al RecyclerView
            });
        } catch (JSONException e) {
            Log.e("JSON_ERROR", "Error al parsear JSON: " + e.getMessage());
        }
    }

    /**
     * Método para obtener detalles de la película seleccionada.
     * @param movie
     */
  private void fetchMovieOverview(Movies movie) {
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl("https://imdb-com.p.rapidapi.com/")
              .addConverterFactory(GsonConverterFactory.create())
              .build();

      IMDBApiService apiService = retrofit.create(IMDBApiService.class);

      Call<MovieOverviewResponse> call = apiService.getMovieOverview(
              movie.getId(),
              "8c8a3cbdefmsh5b39dc7ade88a71p1ca1bdjsn245a12339ee4", // Clave API
              "imdb-com.p.rapidapi.com"
      );

      call.enqueue(new Callback<MovieOverviewResponse>() {
          @Override
          public void onResponse(@NonNull Call<MovieOverviewResponse> call, @NonNull Response<MovieOverviewResponse> response) {
              if (response.isSuccessful() && response.body() != null) {
                  MovieOverviewResponse details = response.body();

                  // Actualizar los detalles del objeto Movies
                  movie.setOverview(details.getData().getTitle().getPlot().getPlotText().getPlainText());
                  movie.setRating(details.getData().getTitle().getRatingsSummary().getAggregateRating() != null ?
                          String.valueOf(details.getData().getTitle().getRatingsSummary().getAggregateRating()) :
                          "No disponible");
                  MovieOverviewResponse.ReleaseDate releaseDate = details.getData().getTitle().getReleaseDate();
                  movie.setReleaseYear(releaseDate != null ?
                          releaseDate.getDay() + "/" + releaseDate.getMonth() + "/" + releaseDate.getYear() :
                          "Fecha no disponible");




                  // Iniciar la actividad con los detalles completos
                  Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
                  intent.putExtra("movie", movie); // Pasar el objeto actualizado
                  startActivity(intent);
              } else {
                  Toast.makeText(getContext(), "No se pudieron cargar los detalles de la película", Toast.LENGTH_SHORT).show();
              }
          }

          @Override
          public void onFailure(@NonNull Call<MovieOverviewResponse> call, @NonNull Throwable t) {
              Toast.makeText(getContext(), "Error al conectar con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
          }
      });
            }

    /**
     * Método para manejar el clic en una película. Abre la actividad de detalles.
     * @param movie
     */
    private void onMovieClick(Movies movie) {
        if (movie.getId() != null) {
            fetchMovieOverview(movie); // Llamar al método que obtiene los detalles del endpoint
        } else {
            Toast.makeText(getContext(), "Información incompleta para esta película", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchMovieOverviewForFavorites(Movies movie) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IMDBApiService apiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = apiService.getMovieOverview(
                movie.getId(),
                "8c8a3cbdefmsh5b39dc7ade88a71p1ca1bdjsn245a12339ee4", // Clave API
                "imdb-com.p.rapidapi.com"
        );

        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieOverviewResponse> call, @NonNull Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieOverviewResponse details = response.body();

                    // Actualizar los datos de la película
                    movie.setOverview(details.getData().getTitle().getPlot().getPlotText().getPlainText());
                    movie.setRating(details.getData().getTitle().getRatingsSummary().getAggregateRating() != null ?
                            String.valueOf(details.getData().getTitle().getRatingsSummary().getAggregateRating()) :
                            "No disponible");
                    MovieOverviewResponse.ReleaseDate releaseDate = details.getData().getTitle().getReleaseDate();
                    movie.setReleaseYear(releaseDate != null ?
                            releaseDate.getDay() + "/" + releaseDate.getMonth() + "/" + releaseDate.getYear() :
                            "Fecha no disponible");

                    // Agregar a favoritos
                    addMovieToFavorites(movie);
                } else {
                    Toast.makeText(getContext(), "No se pudieron cargar los detalles de la película", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieOverviewResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error al conectar con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMovieToFavorites(Movies movie) {
        // Inicializar el gestor de favoritos
        FavoritesManager favoritesManager = new FavoritesManager(requireContext());

        // Obtener el correo del usuario actual desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", ""); // Obtiene el correo del usuario

        if (userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cargar la lista de favoritos actual desde la base de datos
        Cursor cursor = favoritesManager.getFavoritesCursor(userEmail);
        List<Movies> existingFavorites = favoritesManager.getFavoritesList(cursor);

        // Verificar si se ha alcanzado el límite de películas (por ejemplo, 6)
        if (existingFavorites.size() >= 6) {
            Toast.makeText(getContext(), "No puedes añadir más de 6 películas a favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si la película ya está en favoritos
        for (Movies existingMovie : existingFavorites) {
            if (existingMovie.getId().equals(movie.getId())) {
                Toast.makeText(getContext(), "Esta película ya está en favoritos", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Asegurarse de que el rating y overview no sean nulos
        if (movie.getRating() == null || movie.getRating().isEmpty()) {
            movie.setRating("No disponible");
        }
        if (movie.getOverview() == null || movie.getOverview().isEmpty()) {
            movie.setOverview("Descripción no disponible");
        }

        // Agregar la película a favoritos
        boolean isAdded = favoritesManager.addFavorite(
                movie.getId(),              // ID de la película
                userEmail,                  // Email del usuario
                movie.getTitle(),           // Título de la película
                movie.getImageUrl(),        // URL de la imagen
                movie.getReleaseYear(),     // Fecha de lanzamiento
                movie.getRating(),          // Puntuación
                movie.getOverview()         // Descripción de la película
        );

        if (isAdded) {
            Toast.makeText(getContext(), "Película añadida a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error al añadir a favoritos", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Método para manejar el clic largo al agregar una película a favoritos.
     * Limita el máximo de películas en favoritos a 6.
     */
    public void onMovieLongClick(Movies movie) {

        if (movie.getRating() == null || movie.getOverview() == null) {
            // Obtener los detalles completos de la película antes de agregarla
            fetchMovieOverviewForFavorites( movie);
        } else {
            // Agregar la película directamente si ya tiene todos los detalles
            addMovieToFavorites(movie);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (movieList.isEmpty()) { // Evitar cargar datos si ya están cargados
            fetchTopMovies();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}