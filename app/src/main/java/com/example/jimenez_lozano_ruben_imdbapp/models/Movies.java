package com.example.jimenez_lozano_ruben_imdbapp.models;

public class Movies {
    private String id; // ID de la película (tconst)
    private String title; // Título de la película
    private String imageUrl; // URL de la imagen
    private String releaseYear; // Año de lanzamiento
    private String rating; // Puntuación de la película (opcional para el overview)

    public Movies(){

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