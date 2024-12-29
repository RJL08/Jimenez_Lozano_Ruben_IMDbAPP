package com.example.jimenez_lozano_ruben_imdbapp.ui.adapter;

import android.content.Intent;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jimenez_lozano_ruben_imdbapp.MovieDetailsActivity;
import com.example.jimenez_lozano_ruben_imdbapp.R;
import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;

import java.util.List;
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {


    /**
     * Lista de películas que se mostrarán en el RecyclerView.
     */
    private List<Movies> movieList;

    /**
     * Listener para manejar los clics en cada película.
     */
    private OnMovieClickListener listener;

    /**
     * Listener para manejar los eventos de long click para agregar a favoritos.
     */
    private OnMovieLongClickListener longClickListener;

    /**
     * Constructor para inicializar la lista de películas y los listeners.
     *
     * @param movieList Lista de películas.
     * @param listener Listener para clics normales.
     * @param longClickListener Listener para clics largos.
     */
    public MovieAdapter(List<Movies> movieList, OnMovieClickListener listener, OnMovieLongClickListener longClickListener) {
        this.movieList = movieList;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    /**
     * Actualiza la lista de películas en el adaptador y notifica los cambios.
     *
     * @param newMovieList La nueva lista de películas a mostrar.
     */
    public void updateMovies(List<Movies> newMovieList) {
        this.movieList.clear();
        this.movieList.addAll(newMovieList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño de cada elemento de la lista desde el archivo XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_moive, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        // Obtener la película actual de la lista
        Movies movie = movieList.get(position);
        // Enlazar los datos de la película con el ViewHolder
        holder.bind(movie, listener, longClickListener);
    }

    @Override
    public int getItemCount() {
        // Devolver el tamaño de la lista de películas
        return movieList.size();
    }

    /**
     * ViewHolder para representar cada película en el RecyclerView.
     */
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        /**
         * Vista para mostrar la imagen de la película.
         */
        private ImageView movieImage;

        /**
         * Vista para mostrar el título de la película.
         */
        private TextView movieTitle;

        /**
         * Constructor del ViewHolder para inicializar las vistas.
         *
         * @param itemView Vista del elemento del RecyclerView.
         */
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializar las vistas del diseño
            movieImage = itemView.findViewById(R.id.movie_image);
            movieTitle = itemView.findViewById(R.id.movie_title);
        }

        /**
         * Método para enlazar los datos de la película y los listeners a las vistas.
         *
         * @param movie Película a mostrar.
         * @param listener Listener para clics normales.
         * @param longClickListener Listener para clics largos.
         */
        public void bind(Movies movie, OnMovieClickListener listener, OnMovieLongClickListener longClickListener) {
            // Establecer el título de la película
            movieTitle.setText(movie.getTitle());


            // Cargar la imagen de la película usando Glide
            Glide.with(itemView.getContext())
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.esperando)
                    .into(movieImage);

            // Configurar el listener para clics normales
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), MovieDetailsActivity.class);
                intent.putExtra("movie", movie); // Pasar la película como objeto Parcelable
                itemView.getContext().startActivity(intent);
            });

            // Configurar el listener para clics largos
            itemView.setOnLongClickListener(v -> {
                longClickListener.onMovieLongClick(movie);
                return true; // Indicar que el evento fue manejado
            });
        }
    }

    /**
     * Interfaz para manejar clics en una película.
     */
    public interface OnMovieClickListener {
        void onMovieClick(Movies movie);
    }

    /**
     * Interfaz para manejar clics largos en una película.
     */
    public interface OnMovieLongClickListener {
        void onMovieLongClick(Movies movie);
    }
}