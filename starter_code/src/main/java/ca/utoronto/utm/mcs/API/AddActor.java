package ca.utoronto.utm.mcs.API;

import java.io.IOException;
import java.io.OutputStream;

import ca.utoronto.utm.mcs.Models.ActorModel;
import ca.utoronto.utm.mcs.Neo4JConnector;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddActor implements HttpHandler
{
    private static ActorModel actorModel;

    public AddActor(ActorModel actorModel) {
        actorModel = actorModel;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException{
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String name = actorModel.getName();
        String actorId = actorModel.getActorId();

        if(!deserialized.has("name") || !deserialized.has("actorId")){
            r.sendResponseHeaders(400, -1);
        }

        name = deserialized.getString("name");
        actorId = deserialized.getString("actorId");

        Neo4JConnector nb = new Neo4JConnector();
        nb.insertActor(name, actorId);
        nb.close();

        actorModel.setName(name);
        actorModel.setActorId(actorId);

        r.sendResponseHeaders(200, -1);
    }
}
