package main;

import com.sun.net.httpserver.HttpServer;
import main.http.handlers.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.System.*;
import static main.kernel.ControllerKernel.loadControllers;

public class Main {
    static HttpServer server;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        Main.server = server;

        loadControllers("main.http.controllers");
        loadControllers("main.http.controllers.api");

        server.createContext("/", new RequestHandler());

        server.start();
        out.println(" Server started on port 8080");
    }
}
