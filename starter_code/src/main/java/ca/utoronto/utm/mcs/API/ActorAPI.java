package ca.utoronto.utm.mcs.API;

import java.io.IOException;
import java.io.OutputStream;

import ca.utoronto.utm.mcs.Neo4JConnector;
import ca.utoronto.utm.mcs.exceptions.BadRequestException;
import ca.utoronto.utm.mcs.exceptions.NotFoundException;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ActorAPI implements HttpHandler
{
    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT") && r.getRequestURI().toString().equals("/api/v1/addActor")) {
                handlePut(r);
            }else if(r.getRequestMethod().equals("GET") &&  r.getRequestURI().toString().equals("/api/v1/getActor")) {
                handleGet(r);
            }else {
            	r.sendResponseHeaders(400, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePut(HttpExchange r) throws Exception{
        String name, actorId;

        try{
	        String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        name = deserialized.getString("name");
	        actorId = deserialized.getString("actorId");
        } catch (JSONException e) {
             r.sendResponseHeaders(400, -1);
             return;
        }

        try{
            Neo4JConnector nb = new Neo4JConnector();
            nb.addActor(name, actorId);
            nb.close();
            r.sendResponseHeaders(200, -1);
        } catch (BadRequestException e){
            r.sendResponseHeaders(400, -1);
        } catch(Exception J){
            r.sendResponseHeaders(500, -1);
        }
    }
    
    public void handleGet(HttpExchange r) throws IOException {
    	String actorId;
    	String actorData;
    	
    	try {
	        String body = Utils.convert(r.getRequestBody());
	        JSONObject deserialized = new JSONObject(body);
	        actorId = deserialized.getString("actorId");
        } catch (JSONException e) {
             r.sendResponseHeaders(400, -1);
            return;
        }
    	
    	try{
            Neo4JConnector nb = new Neo4JConnector();
            actorData = nb.getActor(actorId);
            nb.close();
            r.sendResponseHeaders(200, actorData.length());
            OutputStream os = r.getResponseBody();
            os.write(actorData.getBytes());
            os.close();
        } catch (BadRequestException e){
            r.sendResponseHeaders(400, -1);
        } catch (NotFoundException b){
            r.sendResponseHeaders(404, -1);
        } catch(Exception J){
            r.sendResponseHeaders(500, -1);
        }
    }
}
