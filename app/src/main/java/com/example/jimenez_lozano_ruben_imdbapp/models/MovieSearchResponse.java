package com.example.jimenez_lozano_ruben_imdbapp.models;

import android.graphics.Movie;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieSearchResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<TMDBMovie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;



    // Constructor vacío
    public MovieSearchResponse() {
    }

    // Getters y Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<TMDBMovie> getResults() {
        return results;
    }

    public void setResults(List<TMDBMovie> results) {
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public String toString() {
        return "MovieSearchResponse{" +
                "page=" + page +
                ", results=" + results +
                ", totalResults=" + totalResults +
                ", totalPages=" + totalPages +
                '}';
    }
}