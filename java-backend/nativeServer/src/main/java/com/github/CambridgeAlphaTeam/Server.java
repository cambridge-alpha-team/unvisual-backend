package com.github.CambridgeAlphaTeam;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;

/**
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 */
public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/hiworld", new ExampleHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started");
    }

    static class ExampleHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "Hello World!";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
