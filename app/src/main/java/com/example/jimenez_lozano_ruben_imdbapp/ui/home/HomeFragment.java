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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList = new ArrayList<>();
    private FavoritesManager favoritesManager;

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
                movie.setId(node.getString("id"));
                movie.setTitle(node.getJSONObject("titleText").getString("text"));
                movie.setImageUrl(node.getJSONObject("primaryImage").getString("url"));
                movie.setReleaseYear(node.getJSONObject("releaseDate").getString("year"));

                // Verifica si el campo rating existe antes de asignarlo
                if (node.has("rating") && node.getJSONObject("rating").has("value")) {
                    movie.setRating(node.getJSONObject("rating").getString("value"));
                } else {
                    movie.setRating("0"); // Valor por defecto solo si no hay rating
                }

                // Verifica si el campo overview existe antes de asignarlo
                if (node.has("overview") && !node.isNull("overview")) {
                    movie.setOverview(node.getString("overview"));
                } else {
                    movie.setOverview(""); // Valor por defecto si no hay descripción
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

    private void fetchMovieOverview(Movies movie) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IMDBApiService apiService = retrofit.create(IMDBApiService.class);

        Call<MovieOverviewResponse> call = apiService.getMovieOverview(
                movie.getId(),
                "TU_API_KEY",
                "imdb-com.p.rapidapi.com"
        );

    }


    private void onMovieClick(Movies movie) {
        if (movie.getId() == null || movie.getTitle() == null || movie.getImageUrl() == null) {
            Log.e("HomeFragment", "Datos incompletos para la película: " + movie);
            Toast.makeText(getContext(), "Error: Información incompleta de la película", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), MovieDetailsActivity.class);
        intent.putExtra("movie_id", movie.getId()); // ID de la película
        intent.putExtra("movie_title", movie.getTitle()); // Título de la película
        intent.putExtra("movie_image", movie.getImageUrl()); // URL de la imagen
        startActivity(intent);
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