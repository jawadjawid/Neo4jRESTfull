package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import ca.utoronto.utm.mcs.API.ActorAPI;
import ca.utoronto.utm.mcs.API.BaconNumberAPI;
import ca.utoronto.utm.mcs.API.MovieAPI;
import ca.utoronto.utm.mcs.API.RelationshipAPI;

import com.sun.net.httpserver.HttpServer;

public class
App
{
    static int PORT = 8080;
    static HttpServer server;

    public static void Initialize(){
        server.createContext("/api/v1/addActor", new ActorAPI());
        server.createContext("/api/v1/addMovie", new MovieAPI());
        server.createContext("/api/v1/addRelationship", new RelationshipAPI());
        server.createContext("/api/v1/getActor", new ActorAPI());
        server.createContext("/api/v1/getMovie", new MovieAPI());
        server.createContext("/api/v1/hasRelationship", new RelationshipAPI());
        server.createContext("/api/v1/computeBaconNumber", new BaconNumberAPI());
        server.createContext("/api/v1/computeBaconPath", new BaconNumberAPI());
    }

    public static void main(String[] args) throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        Initialize();
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
