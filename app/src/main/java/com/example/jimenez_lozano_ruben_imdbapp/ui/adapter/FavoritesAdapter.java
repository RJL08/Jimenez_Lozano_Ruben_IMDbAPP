package com.example.jimenez_lozano_ruben_imdbapp.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.jimenez_lozano_ruben_imdbapp.R;
import com.example.jimenez_lozano_ruben_imdbapp.database.FavoritesManager;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Movies> favoriteList;
    private Context context;

    public FavoritesAdapter(Context context, List<Movies> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Movies movie = favoriteList.get(position);

        // Vincular datos de la película al ViewHolder
        holder.bind(movie);

        // Manejar clic largo para eliminar de favoritos
        holder.itemView.setOnLongClickListener(v -> {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Eliminar de Favoritos");
                builder.setMessage("¿Estás seguro de que quieres eliminar " + movie.getTitle() + " de favoritos?");
                builder.setPositiveButton("Sí", (dialog, which) -> removeMovie(position, movie));
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                builder.show();
            } catch (Exception e) {
                Toast.makeText(context, "Error inesperado al manejar clic largo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FavoritesAdapter", "Error en onLongClick: " + e.getMessage());
            }
            return true; // Indicar que se manejó el clic largo
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    /**
     * Elimina una película de favoritos y actualiza la lista.
     *
     * @param position La posición del elemento en la lista.
     * @param movie    La película a eliminar.
     */
    private void removeMovie(int position, Movies movie) {
        try {
            // Validar el índice
            if (position < 0 || position >= favoriteList.size()) {
                Toast.makeText(context, "Error: índice inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear instancia de FavoritesManager
            FavoritesManager favoritesManager = new FavoritesManager(context);
            SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String userEmail = prefs.getString("userEmail", "");

            // Eliminar de la base de datos
            boolean isRemoved = favoritesManager.removeFavorite(userEmail, movie.getTitle());
            if (isRemoved) {
                // Eliminar de la lista local y notificar al adaptador
                favoriteList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, movie.getTitle() + " eliminado de favoritos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show();
            }
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(context, "Error: índice fuera de rango. " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("FavoritesAdapter", "IndexOutOfBoundsException: " + e.getMessage());
        } catch (SQLiteException e) {
            Toast.makeText(context, "Error en la base de datos. " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("FavoritesAdapter", "SQLiteException: " + e.getMessage());
        } catch (Exception e) {
            Toast.makeText(context, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("FavoritesAdapter", "Unexpected Exception: " + e.getMessage());
        }
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private TextView movieTitle;
        private ImageView movieImage;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movie_title);
            movieImage = itemView.findViewById(R.id.movie_image);
        }

        public void bind(Movies movie) {
            movieTitle.setText(movie.getTitle());
            Glide.with(itemView.getContext())
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.esperando)
                    .into(movieImage);
        }
    }
}
