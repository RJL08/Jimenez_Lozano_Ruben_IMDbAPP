package com.example.jimenez_lozano_ruben_imdbapp.api;

import com.example.jimenez_lozano_ruben_imdbapp.models.GenresResponse;
import com.example.jimenez_lozano_ruben_imdbapp.models.MovieSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TMDBApiService {

    @GET("discover/movie")
    Call<MovieSearchResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("sort_by") String sortBy,
            @Query("include_adult") boolean includeAdult,
            @Query("page") int page,
            @Query("primary_release_year") int primaryReleaseYear,
            @Query("with_genres") String genreId
    );

    @GET("genre/movie/list")
    Call<GenresResponse> getGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
}
