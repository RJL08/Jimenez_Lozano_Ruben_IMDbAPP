package com.example.jimenez_lozano_ruben_imdbapp.ui.home;


import android.content.Intent;
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
import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.databinding.FragmentHomeBinding;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import com.example.jimenez_lozano_ruben_imdbapp.ui.adapter.MovieAdapter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList = new ArrayList<>();

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
     * Lógica para manejar el evento de clic largo en una película.
     *
     * @param movie Película seleccionada.
     */
    private void onMovieLongClick(Movies movie) {
        FavoritesManager favoritesManager = new FavoritesManager(requireContext());

        // Obtener el correo del usuario actual (puedes obtenerlo de SharedPreferences o cualquier otro lugar)
        String userEmail = "usuario@ejemplo.com"; // Reemplázalo con la lógica para obtener el email

        try {
            // Llamar a addFavorite con los parámetros correctos
            favoritesManager.addFavorite(
                    userEmail,                 // Correo del usuario
                    movie.getTitle(),          // Título de la película
                    movie.getImageUrl(),       // URL de la imagen
                    movie.getReleaseYear()     // Fecha de lanzamiento
            );

            Toast.makeText(getContext(), "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al agregar a favoritos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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