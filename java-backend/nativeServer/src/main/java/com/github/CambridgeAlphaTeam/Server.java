package com.github.CambridgeAlphaTeam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.github.cambridgeAlphaTeam.OscException;
import com.github.cambridgeAlphaTeam.OscSender;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 */
public class Server {

    private static OscSender sender;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws IOException, OscException {
        if (args.length != 1) {
            System.err.println("Give precisely one argument, the location of the front end files to serve");
            return;
        }
        sender = new OscSender();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/hiworld", new LoggerHandler(new ExampleHandler()));
        server.createContext("/rest/osc/run", new LoggerHandler(new RunCodeHandler()));
        server.createContext("/rest/osc/stop", new LoggerHandler(new StopMusicHandler()));
        server.createContext("/", new LoggerHandler(new FileHandler(args[0]))); // serves front end
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

    static class RunCodeHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            logger.debug("Request to run code received");
            BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
            StringBuilder strb = new StringBuilder();
            while (br.ready()) {
                strb.append(br.readLine() + "\n"); // not sure this is the best way of doing this?
            }
            try {
                logger.debug("Sending code to Sonic Pi:\n" + strb.toString());
                sender.sendCode(strb.toString());
            } catch (OscException e) {
                throw new IOException("Sending code to Sonic Pi server failed", e);
            }
            t.sendResponseHeaders(200, 0);
        }
    }

    static class StopMusicHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            logger.debug("Request to stop music received");
            try {
                logger.debug("Sending stop request to Sonic Pi");
                sender.stopAll();
            } catch (OscException e) {
                throw new IOException("Sending message to Sonic Pi server failed", e);
            }
            t.sendResponseHeaders(200, 0);
        }
    }

}
