package com.example.jimenez_lozano_ruben_imdbapp.ui.gallery;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        adapter = new FavoritesAdapter(requireContext(),favoriteList); // Crear el adaptador
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
        String userEmail = "usuario@ejemplo.com"; // Cambiar por el correo del usuario logueado
        Cursor cursor = favoritesManager.getFavoritesCursor(userEmail); // Obtener el cursor
        favoriteList.clear();
        favoriteList.addAll(favoritesManager.getFavoritesList(cursor)); // Convertir el cursor a lista y agregarlo
        adapter.notifyDataSetChanged(); // Notificar cambios al RecyclerView
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding al destruir la vista
    }
}
