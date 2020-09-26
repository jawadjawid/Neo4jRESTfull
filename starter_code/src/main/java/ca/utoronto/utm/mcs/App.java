package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import ca.utoronto.utm.mcs.API.AddActor;
import ca.utoronto.utm.mcs.API.AddMovie;
import ca.utoronto.utm.mcs.API.AddRelationship;

import com.sun.net.httpserver.HttpServer;

public class
App
{
    static int PORT = 8080;
    static HttpServer server;

    public static void Initialize(){
        server.createContext("/api/v1/addActor", new AddActor());
        server.createContext("/api/v1/addMovie", new AddMovie());
        server.createContext("/api/v1/addRelationship", new AddRelationship());
        server.createContext("/api/v1/getActor", new AddActor());
        server.createContext("/api/v1/getMovie", new AddMovie());
    }

    public static void main(String[] args) throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        Initialize();
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
