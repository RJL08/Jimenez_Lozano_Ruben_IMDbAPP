package com.example.jimenez_lozano_ruben_imdbapp.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesDatabaseHelper;
import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.databinding.FragmentGalleryBinding;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import com.example.jimenez_lozano_ruben_imdbapp.ui.adapter.FavoritesAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding; // Para usar ViewBinding
    private RecyclerView recyclerView; // RecyclerView para mostrar los favoritos
    private FavoritesAdapter adapter; // Adaptador personalizado
    private List<Movies> favoriteList = new ArrayList<>(); // Lista de películas favoritas
    private FavoritesManager favoritesManager; // Gestor de favoritos

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializar el ViewModel (ya existente)
        GalleryViewModel galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        // Inflar el diseño usando ViewBinding
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar el RecyclerView
        recyclerView = binding.recyclerViewFavorites; // Asegúrate de que ID coincide con tu XML
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Lista vertical
        adapter = new FavoritesAdapter(requireContext(), favoriteList, this); // Crear el adaptador
        recyclerView.setAdapter(adapter); // Vincular el adaptador al RecyclerView

        // Inicializar el FavoritesManager
        favoritesManager = new FavoritesManager(requireContext());

        // Cargar favoritos desde la base de datos
        loadFavorites();

        return root;
    }

    /**
     * Método para cargar los favoritos desde la base de datos.
     */
    private void loadFavorites() {
        // Obtener el correo del usuario actual desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", ""); // Obtiene el correo del usuario actual

        if (userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            Log.e("GalleryFragment", "Error: Correo del usuario vacío");
            return;
        }

        // Cargar favoritos del usuario
        Cursor cursor = favoritesManager.getFavoritesCursor(userEmail);
        if (cursor != null && cursor.getCount() > 0) {
            favoriteList.clear();
            favoriteList.addAll(favoritesManager.getFavoritesList(cursor));
            adapter.notifyDataSetChanged(); // Actualizar el RecyclerView
            Log.d("GalleryFragment", "Favoritos cargados correctamente: " + favoriteList.size());
        } else {
            Log.d("GalleryFragment", "No hay favoritos para el usuario: " + userEmail);
            Toast.makeText(getContext(), "No tienes películas favoritas", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para manejar el clic largo al agregar una película a favoritos.
     * Limita el máximo de películas en favoritos a 6.
     */
    public void onMovieLongClick(Movies movie) {
        // Obtener el correo del usuario actual desde SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", ""); // Obtén el correo del usuario

        if (userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si ya se alcanzó el límite de 6 películas
        if (favoriteList.size() >= 6) {
            Toast.makeText(getContext(), "No puedes añadir más de 6 películas a favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Añadir la película a favoritos
        boolean isAdded = favoritesManager.addFavorite(userEmail, movie.getTitle(), movie.getImageUrl(), movie.getReleaseYear());

        if (isAdded) {
            favoriteList.add(movie); // Agregarla a la lista local
            adapter.notifyItemInserted(favoriteList.size() - 1); // Actualizar el RecyclerView
            Toast.makeText(getContext(), "Agregada a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error: No se pudo agregar a favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding al destruir la vista
    }
}