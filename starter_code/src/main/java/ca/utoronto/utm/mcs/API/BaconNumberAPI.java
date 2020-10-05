package ca.utoronto.utm.mcs.API;

import java.io.IOException;
import java.io.OutputStream;

import ca.utoronto.utm.mcs.Neo4JConnector;
import ca.utoronto.utm.mcs.exceptions.BadRequestException;
import ca.utoronto.utm.mcs.exceptions.NotFoundException;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BaconNumberAPI implements HttpHandler{
	public void handle(HttpExchange r) {
        try {
            if(r.getRequestMethod().equals("GET")) {
            	if(r.getRequestURI().toString().equals("/api/v1/computeBaconNumber"))
            		handleGetNumber(r);
            	else if(r.getRequestURI().toString().equals("/api/v1/computeBaconPath"))
            		handleGetPath(r);
            }else{
                r.sendResponseHeaders(400, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public void handleGetNumber(HttpExchange r) throws IOException {
    	String actorId;
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
            String baconNumber = nb.computeBaconNumber(actorId);
            nb.close();
            
            r.sendResponseHeaders(200, baconNumber.length());
            OutputStream os = r.getResponseBody();
            os.write(baconNumber.getBytes());
            os.close();
        } catch (BadRequestException e){
            r.sendResponseHeaders(400, -1);
        } catch (NotFoundException b){
            r.sendResponseHeaders(404, -1);
        } catch(Exception J){
            r.sendResponseHeaders(500, -1);
        }
    }
	
	public void handleGetPath(HttpExchange r) throws Exception {
    	String actorId;
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
            String baconPath = nb.computeBaconPath(actorId);
            nb.close();
            
            r.sendResponseHeaders(200, baconPath.length());
            OutputStream os = r.getResponseBody();
            os.write(baconPath.getBytes());
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
