package ca.utoronto.utm.mcs.Models;

public class MovieModel
{
    private static String name;
    private static String movieId;

    public MovieModel() {
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        MovieModel.name = name;
    }

    public static String getMovieId() {
        return movieId;
    }

    public static void setMovieId(String movieId) {
        MovieModel.movieId = movieId;
    }
}
