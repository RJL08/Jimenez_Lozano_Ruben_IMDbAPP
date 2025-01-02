package com.example.jimenez_lozano_ruben_imdbapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


    public class FavoritesDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "favorites_db";
        private static final int DATABASE_VERSION = 4;
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_EMAIL = "user_email";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_MOVIE_IMAGE = "movie_image";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_MOVIE_RATING = "movie_rating";
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";


        public FavoritesDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

                String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                        COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                        COLUMN_MOVIE_IMAGE + " TEXT NOT NULL, " +
                        COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        COLUMN_MOVIE_RATING + " TEXT NOT NULL, " +
                        COLUMN_MOVIE_OVERVIEW + " TEXT);"; // Nueva columna para descripción
                db.execSQL(createTable);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // Eliminar la tabla existente
            onCreate(db); // Crear la tabla con el nuevo esquema

        }
    }
