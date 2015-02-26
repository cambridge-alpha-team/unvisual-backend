package com.github.cambridgeAlphaTeam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

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

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
/**
 * This class serves frontend and also responds to its queries.
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
public class Server {

    private static OscSender sender;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    /* Command line options parser */
    private static final Options options = new Options();
    static {
        /* addOption(String short option,
         *           String long option,
         *           boolean has arguments,
         *           String description)
         *
         * where a short option is like "-h" and a long option is like
         * "--help" (which takes no arguments). The description is for
         * generating the help message.
         */
        options.addOption ("h", "help", false, "Print this help message");
        options.addOption ("u", "usage", false, "Print the list of options");
        options.addOption ("c", "cubelets-program", true, "Cubelets program, " +
                "further uses of \"-c\" will act as arguments to the "+
                "cubelets program.");
        options.addOption ("f", "frontend", true, "The location of the frond-end files. " +
                "These are normally found in the Jar under \"unvisula-frontend/\" " +
                "but for development it is easier to not have to repack jar (a backend task) " +
                "everytime the frontend changes.");
    }

    public static void main(String[] args) throws IOException, OscException {
        /* Parsing command line, may terminate program. {{{ */
        /* The parser for the options. */
        CommandLineParser parser = new GnuParser();

        CommandLine parsedCommanLine;
        try {
            parsedCommanLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Unable to parse command line options!");
            System.err.println(e.getMessage());
            System.err.println("Help:");
            printHelp(System.err);
            return;
        }

        if (parsedCommanLine.hasOption('h')) {
            printHelp(System.out);
            return;
        }

        if (parsedCommanLine.hasOption('u')) {
            printUsage();
            return;
        }
        /* }}} */

        sender = new OscSender();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        /* Note, longest prefix match is active for the contexts. */
        /* Osc context */
        server.createContext("/osc/run", new LoggerHandler(new RunCodeHandler()));
        server.createContext("/osc/stop", new LoggerHandler(new StopMusicHandler()));

        if (parsedCommanLine.hasOption("f")) {
            /* File context */
            server.createContext("/", new LoggerHandler(new FileHandler
                        (parsedCommanLine.getOptionValue("f")))); // serves front end
        } else {
            /* Jar handler context, don't add trailing slash! */
            server.createContext("/", new LoggerHandler
                (new JarHandler("unvisual-frontend"))); // serves front end
        }

        server.start();
        logger.info("Server started");

        if (parsedCommanLine.hasOption("c")) {
            final String[] cubeletsProcessCmd = parsedCommanLine.getOptionValues("c");
            /* Cubelets connection */
            final ExecCubeletsConnection.SaveKnownCubelets knownCubelets = new ExecCubeletsConnection.SaveKnownCubelets();
            IWatchDog<IWatchableCubeletsConnection> watchDog =
                new WatchDog<IWatchableCubeletsConnection>(
                        new ICreator<IWatchableCubeletsConnection>() {
                            @Override
                            public OscExecCubeletsConnection create() {
                                try {
                                    return new OscExecCubeletsConnection(cubeletsProcessCmd, knownCubelets, sender);
                                } catch (IOException e) {
                                    logger.error("Unable to open process!", e);
                                    return null;
                                }
                            }
                        });
                watchDog.setTimeout(2000);
            Thread watchDogThread = new Thread(watchDog);
            watchDogThread.start();
        } else {
           logger.warn("No cubelet program provided: cubelet functionality not being used");
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
            t.close();
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
            t.close();
        }
    }

    static class OscExecCubeletsConnection extends ExecCubeletsConnection {
        private OscSender sender;

        public OscExecCubeletsConnection(final String[] cmdarray, SaveKnownCubelets knownCubelets, OscSender sender) throws IOException {
            super(cmdarray, knownCubelets);
            this.sender = sender;
            /* Fill in Sonic Pi with something */
            logger.debug("Feeding zero cubelet values to Sonic Pi");
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

            logger.debug(strb.toString());

            try {
                sender.sendCode(strb.toString());
            } catch (OscException e) {
                logger.error("Unhandler OscException in trying to send cubelet values", e);
            }
        }
    }

    public static void printUsage() {
        final PrintWriter stdout = new PrintWriter(System.out);
        new HelpFormatter().printUsage(stdout, 72, "java -jar unvisual.jar", options);
        stdout.close();
    }

    public static void printHelp(OutputStream outs) {
        final PrintWriter outw = new PrintWriter(outs);
        /* printHelp(int width, String cmdLineSyntax, String header, Options options, String footer) */
        /* public void printHelp(PrintWriter pw,
         *                       int width,
         *                       String cmdLineSyntax,
         *                       String header,
         *                       Options options,
         *                       int leftPad,
         *                       int descPad,
         *                       String footer)
         */
        new HelpFormatter().printHelp(outw, 72, "java -jar unvisual.jar",
                "", options, 2, 4, "");
        outw.close();
    }
}
