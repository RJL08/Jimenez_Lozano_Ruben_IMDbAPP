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
                        .setHeader("x-rapidapi-key", "3ef3f2c2a3msh17da27eb24608e1p12db6bjsn62d2b74752ff")
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
                movie.setReleaseYear(
                        node.has("releaseDate") && node.getJSONObject("releaseDate").has("year")
                                ? node.getJSONObject("releaseDate").getString("year")
                                : "Fecha no disponible"
                );

                // Verifica si el campo rating existe antes de asignarlo
                if (node.has("rating") && node.getJSONObject("rating").has("value")) {
                    movie.setRating(node.getJSONObject("rating").getString("value"));
                } else {
                    movie.setRating("No disponible"); // Valor por defecto si no hay rating
                }

                // Verifica si el campo overview existe antes de asignarlo
                if (node.has("overview") && !node.isNull("overview")) {
                    movie.setOverview(node.getString("overview"));
                } else {
                    movie.setOverview("Descripción no disponible"); // Valor por defecto
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
              "3ef3f2c2a3msh17da27eb24608e1p12db6bjsn62d2b74752ff", // Clave API
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

    /**
     * Método para manejar el clic largo al agregar una película a favoritos.
     * Limita el máximo de películas en favoritos a 6.
     */
    public void onMovieLongClick(Movies movie) {
        FavoritesManager favoritesManager = new FavoritesManager(requireContext());

        // Obtener el correo del usuario actual desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", ""); // Reemplaza con la lógica para obtener el email

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
            if (existingMovie.getTitle().equals(movie.getTitle())) {
                Toast.makeText(getContext(), "Esta película ya está en favoritos", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Asegurarse de que el rating es válido antes de añadirlo
        if (movie.getRating() == null || movie.getRating().isEmpty()) {
            movie.setRating("0"); // Asignar un valor predeterminado
        }

        // Agregar la película a favoritos
        boolean isAdded = favoritesManager.addFavorite(
                userEmail,
                movie.getTitle(),
                movie.getImageUrl(),
                movie.getReleaseYear(),
                movie.getRating()
        );

        if (isAdded) {
            Toast.makeText(getContext(), "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error al agregar a favoritos", Toast.LENGTH_SHORT).show();
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