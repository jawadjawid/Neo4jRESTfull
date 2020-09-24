package ca.utoronto.utm.mcs.Models;

public class RelationshipModel
{
    private static String actorId;
    private static String movieId;

    public RelationshipModel() {
    }

    public static String getActorId() {
        return actorId;
    }

    public static void setActorId(String actorId) {
        RelationshipModel.actorId = actorId;
    }

    public static String getMovieId() {
        return movieId;
    }

    public static void setMovieId(String movieId) {
        RelationshipModel.movieId = movieId;
    }
}
