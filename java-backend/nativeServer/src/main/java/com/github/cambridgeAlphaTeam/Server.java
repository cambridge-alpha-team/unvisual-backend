package com.github.cambridgeAlphaTeam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.util.Formatter;

import com.github.cambridgeAlphaTeam.OscException;
import com.github.cambridgeAlphaTeam.OscSender;
import com.github.cambridgeAlphaTeam.ExecCubeletsConnection;
import com.github.cambridgeAlphaTeam.IWatchableCubeletsConnection;
import com.github.cambridgeAlphaTeam.watchdog.IWatchDog;
import com.github.cambridgeAlphaTeam.watchdog.ICreator;
import com.github.cambridgeAlphaTeam.watchdog.WatchDog;

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

        /* Note, longest prefix match is active for the contexts. */
        /* Osc context */
        server.createContext("/osc/run", new LoggerHandler(new RunCodeHandler()));
        server.createContext("/osc/stop", new LoggerHandler(new StopMusicHandler()));

        /* File context */
        server.createContext("/", new LoggerHandler(new FileHandler(args[0]))); // serves front end
        server.setExecutor(null); // creates a default executor
        server.start();

        /* Cubelets connection */
        IWatchDog<IWatchableCubeletsConnection> watchDog =
            new WatchDog<IWatchableCubeletsConnection>(
                    new ICreator<IWatchableCubeletsConnection>() {
                        @Override
                        public ExecCubeletsConnection create() {
                            try {
                                return new OscExecCubeletsConnection(args);
                            } catch (IOException e) {
                                logger.error("Unable to open process!", e);
                                return null;
                            }
                        }
                    });
        watchDog.setTimeout(2000);
        Thread watchDogThread = new Thread(watchDog);
        watchDogThread.start();

        logger.info("Server started");
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

    static class OscExecCubeletsConnection extends ExecCubeletsConnection {
        private static OscSender sender;

        public OscExecCubeletsConnection(final String[] cmdarray) throws IOException {
            super(cmdarray);
        }

        public void messageHandle(int[] cubeletValues) {
            StringBuilder strb = new StringBuilder();
            // Send all output to the Appendable object sb
            Formatter formatter = new Formatter(strb);

            formatter.format("def getCubeletValue(number, min, max, granularity)%n");
            formatter.format("  range = max-min%n");
            formatter.format("  unscaled = case number %n");
            formatter.format("               when 0 then %d%n", cubeletValues[0]);
            formatter.format("               when 1 then %d%n", cubeletValues[1]);
            formatter.format("               when 2 then %d%n", cubeletValues[2]);
            formatter.format("               when 3 then %d%n", cubeletValues[3]);
            formatter.format("               when 4 then %d%n", cubeletValues[4]);
            formatter.format("               when 5 then %d%n", cubeletValues[5]);

            /* TODO: What should the default be? */
            formatter.format("               else        %d%n", 0);
            formatter.format("             end%n");
            formatter.format("  scaled = range*unscaled/255%n");

            /* This floors to the granularity. */
            formatter.format("  roundoff = scaled % granularity%n");
            formatter.format("  return min + scaled - roundoff%n");
            formatter.format("end%n");

            sender.sendCode(strb.toString());
        }
    }
}
