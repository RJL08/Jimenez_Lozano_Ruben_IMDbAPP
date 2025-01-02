package com.example.jimenez_lozano_ruben_imdbapp.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.databinding.FragmentGalleryBinding;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import com.example.jimenez_lozano_ruben_imdbapp.ui.adapter.FavoritesAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
        adapter = new FavoritesAdapter(requireContext(), favoriteList); // Crear el adaptador
        recyclerView.setAdapter(adapter); // Vincular el adaptador al RecyclerView

        // Inicializar el FavoritesManager
        favoritesManager = new FavoritesManager(requireContext());

        // Cargar favoritos desde la base de datos
        loadFavorites();


        // Configurar el botón de compartir
        Button shareButton = binding.shareButton;
        shareButton.setOnClickListener(v -> {
            // Solicitar permisos antes de compartir
            requestBluetoothPermission();
            // Compartir favoritos
            shareFavoritesAsJSON();
        });



        return root;
    }



    private final ActivityResultLauncher<String[]> bluetoothPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean isBluetoothConnectGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false);
                Boolean isLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

                if (isBluetoothConnectGranted != null && isBluetoothConnectGranted &&
                        isLocationGranted != null && isLocationGranted) {
                    Toast.makeText(getContext(), "Permisos de Bluetooth concedidos.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Permisos de Bluetooth denegados.", Toast.LENGTH_SHORT).show();
                    showPermissionDeniedDialog();
                }
            });

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Permisos necesarios")
                .setMessage("Esta funcionalidad requiere acceso a Bluetooth y ubicación para compartir tus películas favoritas. Por favor, otorga los permisos desde la configuración de la aplicación.")
                .setPositiveButton("Configurar", (dialog, which) -> {
                    // Redirigir a la configuración de la aplicación
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void requestBluetoothPermission() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Mostrar la alerta explicando la importancia de los permisos
                    showPermissionDeniedDialog();
                } else {
                    // Solicitar permisos normalmente
                    bluetoothPermissionLauncher.launch(new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    });
                }
            } else {
                Toast.makeText(getContext(), "No se requiere permiso en versiones anteriores.", Toast.LENGTH_SHORT).show();
            }
        }

    /**
     * Convierte la lista de favoritos a JSON y permite compartirla.
     */
    private void shareFavoritesAsJSON() {
        if (favoriteList.isEmpty()) {
            Toast.makeText(getContext(), "No hay favoritos para compartir.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray jsonArray = new JSONArray();
        for (Movies movie : favoriteList) {
            try {
                JSONObject jsonMovie = new JSONObject();
                jsonMovie.put("id", movie.getId());
                jsonMovie.put("overview", movie.getOverview() != null ? movie.getOverview() : ""); // Dejar vacío si es null
                jsonMovie.put("posterUrl", movie.getImageUrl() != null ? movie.getImageUrl() : ""); // Dejar vacío si es null
                jsonMovie.put("rating", movie.getRating() != null ? movie.getRating() : "0.0"); // Dejar "0.0" si es null
                jsonMovie.put("releaseDate", movie.getReleaseYear() != null ? movie.getReleaseYear() : ""); // Dejar vacío si es null
                jsonMovie.put("title", movie.getTitle() != null ? movie.getTitle() : "Sin título"); // Dejar "Sin título" si es null

                jsonArray.put(jsonMovie);
            } catch (JSONException e) {
                Log.e("GalleryFragment", "Error al crear JSON: " + e.getMessage());
            }
        }

        String jsonString = jsonArray.toString();
        jsonString = jsonString.replace("\\/", "/");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Películas Favoritas en JSON")
                .setMessage(jsonString)
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();

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

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // Recargar favoritos al reanudar el fragmento
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding al destruir la vista
    }
}