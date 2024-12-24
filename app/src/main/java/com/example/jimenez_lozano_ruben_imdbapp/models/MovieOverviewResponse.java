package com.example.jimenez_lozano_ruben_imdbapp.models;

import com.google.gson.annotations.SerializedName;

public class MovieOverviewResponse {
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("title")
        public Title title;
    }

    public static class Title {
        @SerializedName("releaseDate")
        public ReleaseDate releaseDate;

        @SerializedName("ratingsSummary")
        public RatingsSummary ratingsSummary;

        @SerializedName("plot")
        public Plot plot;
    }

    public static class ReleaseDate {
        @SerializedName("day")
        public Integer day;

        @SerializedName("month")
        public Integer month;

        @SerializedName("year")
        public Integer year;
    }

    public static class RatingsSummary {
        @SerializedName("aggregateRating")
        public Double aggregateRating;
    }

    public static class Plot {
        @SerializedName("plotText")
        public PlotText plotText;
    }

    public static class PlotText {
        @SerializedName("plainText")
        public String plainText;
    }
}