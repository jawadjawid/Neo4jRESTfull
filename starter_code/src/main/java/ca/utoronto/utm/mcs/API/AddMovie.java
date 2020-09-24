package ca.utoronto.utm.mcs.API;

import java.io.IOException;

import ca.utoronto.utm.mcs.Models.MovieModel;
import ca.utoronto.utm.mcs.Neo4JConnector;
import ca.utoronto.utm.mcs.exceptions.BadRequestException;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddMovie implements HttpHandler
{
    private static MovieModel movieModel;

    public AddMovie(MovieModel movieModel) {
        movieModel = movieModel;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            }else{
                r.sendResponseHeaders(400, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException{
        String name = movieModel.getName();
        String movieId = movieModel.getMovieId();

        try{
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            name = deserialized.getString("name");
            movieId = deserialized.getString("movieId");
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
        }

        try{
            Neo4JConnector nb = new Neo4JConnector();
            nb.addMovie(name, movieId);
            nb.close();
            r.sendResponseHeaders(200, -1);

        } catch (BadRequestException e){
            r.sendResponseHeaders(400, -1);
        } catch(Exception J){
            r.sendResponseHeaders(500, -1);
        }
    }
}
