package com.example.jimenez_lozano_ruben_imdbapp.models;

import com.google.gson.annotations.SerializedName;

public class MovieOverviewResponse {
    @SerializedName("data")
    public Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("title")
        public Title title;

        public Title getTitle() {
            return title;
        }
    }

    public static class Title {
        @SerializedName("releaseDate")
        public ReleaseDate releaseDate;

        @SerializedName("ratingsSummary")
        public RatingsSummary ratingsSummary;

        @SerializedName("plot")
        public Plot plot;

        public ReleaseDate getReleaseDate() {
            return releaseDate;
        }

        public RatingsSummary getRatingsSummary() {
            return ratingsSummary;
        }

        public Plot getPlot() {
            return plot;
        }
    }

    public static class ReleaseDate {
        @SerializedName("day")
        public Integer day;

        @SerializedName("month")
        public Integer month;

        @SerializedName("year")
        public Integer year;

        public Integer getDay() {
            return day;
        }

        public Integer getMonth() {
            return month;
        }

        public Integer getYear() {
            return year;
        }
    }

    public static class RatingsSummary {
        @SerializedName("aggregateRating")
        public Double aggregateRating;

        public Double getAggregateRating() {
            return aggregateRating;
        }
    }

    public static class Plot {
        @SerializedName("plotText")
        public PlotText plotText;

        public PlotText getPlotText() {
            return plotText;
        }
    }

    public static class PlotText {
        @SerializedName("plainText")
        public String plainText;

        public String getPlainText() {
            return plainText;
        }
    }
}