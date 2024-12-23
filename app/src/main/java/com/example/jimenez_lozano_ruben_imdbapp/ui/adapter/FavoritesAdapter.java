package com.example.jimenez_lozano_ruben_imdbapp.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.jimenez_lozano_ruben_imdbapp.ui.gallery.GalleryFragment;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<Movies> favoriteList;
    private Context context;
    private GalleryFragment galleryFragment; // Referencia al fragmento

    public FavoritesAdapter(Context context, List<Movies> favoriteList, GalleryFragment galleryFragment) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.galleryFragment = galleryFragment; // Asignar el fragmento correctamente
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

        // Manejar clic largo para agregar a favoritos
        holder.itemView.setOnLongClickListener(v -> {
            if (galleryFragment != null) {
                galleryFragment.onMovieLongClick(movie); // Llama a onMovieLongClick en el Fragment
            } else {
                Toast.makeText(context, "Error: Fragmento no encontrado", Toast.LENGTH_SHORT).show();
            }
            return true; // Indicar que se manejó el clic largo
        });

        // Manejar clic largo para eliminar de favoritos
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Eliminar de Favoritos");
            builder.setMessage("¿Estás seguro de que quieres eliminar " + movie.getTitle() + " de favoritos?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                FavoritesManager favoritesManager = new FavoritesManager(holder.itemView.getContext());
                SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String userEmail = prefs.getString("userEmail", ""); // Obtener el correo del usuario actual

                boolean isRemoved = favoritesManager.removeFavorite(userEmail, movie.getTitle());
                if (isRemoved) {
                    favoriteList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(holder.itemView.getContext(), movie.getTitle() + " eliminado de favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.show();

            return true; // Indicar que se manejó el clic largo
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
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
