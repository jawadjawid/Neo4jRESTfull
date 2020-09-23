package ca.utoronto.utm.mcs.Models;

public class ActorModel
{
    private static String name;
    private static String actorId;

    public ActorModel() {
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        ActorModel.name = name;
    }

    public static String getActorId() {
        return actorId;
    }

    public static void setActorId(String actorId) {
        ActorModel.actorId = actorId;
    }
}
