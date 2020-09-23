package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import ca.utoronto.utm.mcs.API.AddActor;
import ca.utoronto.utm.mcs.Models.ActorModel;

import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    static HttpServer server;

    public static void Initialize(){
        ActorModel actorModel = new ActorModel();
        server.createContext("/api/v1/addActor", new AddActor(actorModel));
    }

    public static void main(String[] args) throws IOException
    {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        Initialize();
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
