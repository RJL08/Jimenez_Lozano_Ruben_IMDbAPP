package com.example.jimenez_lozano_ruben_imdbapp.api;

import com.example.jimenez_lozano_ruben_imdbapp.models.MovieOverviewResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface IMDBApiService {

    @GET("title/get-overview")
    Call<MovieOverviewResponse> getMovieOverview(
            @Query("tconst") String movieId,
            @Header("x-rapidapi-key") String apiKey,
            @Header("x-rapidapi-host") String apiHost
    );

}
