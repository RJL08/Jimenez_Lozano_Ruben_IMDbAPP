package com.example.jimenez_lozano_ruben_imdbapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Movies implements Parcelable {
    private String id; // ID de la película (tconst)
    private String title; // Título de la película
    private String imageUrl; // URL de la imagen
    private String releaseYear; // Año de lanzamiento
    private String rating; // Puntuación de la película (opcional para el overview)

    public Movies(){

    }

    protected Movies(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        releaseYear = in.readString();
    }

    public static final Creator<Movies> CREATOR = new Creator<Movies>() {
        @Override
        public Movies createFromParcel(Parcel in) {
            return new Movies(in);
        }

        @Override
        public Movies[] newArray(int size) {
            return new Movies[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(releaseYear);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}