package com.github.cambridgeAlphaTeam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.util.Formatter;
import java.util.Arrays;

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
        if (args.length < 2) {
            System.err.println("Usage:\njava -jar \"this jar file\" " +
                "\"location of frontend\" " +
                "\"program returning cubelet values\" " +
                "[optional arguments for said program]");
            return;
        }
        final String[] cubeletsProcessCmd = Arrays.copyOfRange(args, 1, args.length);

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
                        public OscExecCubeletsConnection create() {
                            try {
                                return new OscExecCubeletsConnection(cubeletsProcessCmd, sender);
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
        private OscSender sender;

        public OscExecCubeletsConnection(final String[] cmdarray, OscSender sender) throws IOException {
            super(cmdarray);
            this.sender = sender;
            /* Fill in Sonic Pi with something */
            messageHandle(new int[]{0, 0, 0, 0, 0, 0});
        }

        public void messageHandle(int[] cubeletValues) {
            StringBuilder strb = new StringBuilder();
            // Send all output to the Appendable object sb
            Formatter formatter = new Formatter(strb);

            formatter.format("def getCubeletValue(number, min, max, granularity, default)%n");
            formatter.format("  range = max-min%n");
            formatter.format("  unscaled = case number %n");
            formatter.format("               when 1 then %d%n", cubeletValues[0]);
            formatter.format("               when 2 then %d%n", cubeletValues[1]);
            formatter.format("               when 3 then %d%n", cubeletValues[2]);
            formatter.format("               when 4 then %d%n", cubeletValues[3]);
            formatter.format("               when 5 then %d%n", cubeletValues[4]);
            formatter.format("               when 6 then %d%n", cubeletValues[5]);
            formatter.format("               else        default%n");
            formatter.format("             end%n");
            formatter.format("  scaled = range*unscaled/255.0%n");

            /* This floors to the granularity. */
            formatter.format("  roundoff = scaled %% granularity%n");
            formatter.format("  return min + scaled - roundoff%n");
            formatter.format("end%n");

            logger.info(strb.toString());

            try {
                sender.sendCode(strb.toString());
            } catch (OscException e) {
                logger.error("Unhandler OscException in trying to send cubelet values", e);
            }
        }
    }
}
