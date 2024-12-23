package com.example.jimenez_lozano_ruben_imdbapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.jimenez_lozano_ruben_imdbapp.models.Movies;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private FavoritesDatabaseHelper dbHelper;

    public FavoritesManager(Context context) {
        dbHelper = new FavoritesDatabaseHelper(context);
    }

    public boolean addFavorite(String userEmail, String movieTitle, String movieImage, String releaseDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FavoritesDatabaseHelper.COLUMN_USER_EMAIL, userEmail); // Dinámico
        values.put(FavoritesDatabaseHelper.COLUMN_MOVIE_TITLE, movieTitle);
        values.put(FavoritesDatabaseHelper.COLUMN_MOVIE_IMAGE, movieImage);
        values.put(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE, releaseDate);

        long result = db.insert(FavoritesDatabaseHelper.TABLE_NAME, null, values);
        db.close();

        return result != -1; // Retorna true si la inserción fue exitosa
    }

    /**
     * Elimina una película de la lista de favoritos de la base de datos.
     *
     * @param userEmail El correo del usuario actual.
     * @param movieTitle El título de la película a eliminar.
     * @return true si la película fue eliminada exitosamente, false en caso contrario.
     */
    public boolean removeFavorite(String userEmail, String movieTitle) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(
                FavoritesDatabaseHelper.TABLE_NAME,
                FavoritesDatabaseHelper.COLUMN_USER_EMAIL + "=? AND " + FavoritesDatabaseHelper.COLUMN_MOVIE_TITLE + "=?",
                new String[]{userEmail, movieTitle}
        );
        db.close();
        return rowsDeleted > 0;
    }


    public Cursor getFavoritesCursor(String userEmail) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(
                FavoritesDatabaseHelper.TABLE_NAME,
                null,
                FavoritesDatabaseHelper.COLUMN_USER_EMAIL + " = ?",
                new String[]{userEmail},
                null, null, null
        );
    }


    public List<Movies> getFavoritesList(Cursor cursor) {
        List<Movies> favoriteMovies = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movies movie = new Movies();
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_MOVIE_TITLE)));
                movie.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_MOVIE_IMAGE)));
                movie.setReleaseYear(cursor.getString(cursor.getColumnIndexOrThrow(FavoritesDatabaseHelper.COLUMN_RELEASE_DATE)));
                favoriteMovies.add(movie);
            } while (cursor.moveToNext());
            cursor.close(); // Cerrar el cursor después de usarlo
        }
        return favoriteMovies;
    }
}
