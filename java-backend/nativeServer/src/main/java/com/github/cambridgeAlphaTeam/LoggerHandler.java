package com.github.cambridgeAlphaTeam;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.OutputStream;

import com.sun.net.httpserver.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves files from a given directory.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

class LoggerHandler implements HttpHandler {
  private final HttpHandler delegateHandler;
  private static final Logger logger =
    LoggerFactory.getLogger(LoggerHandler.class);

  public LoggerHandler(HttpHandler h) {
    delegateHandler = h;
  }

  public void handle(HttpExchange t) throws IOException {
    try {
      delegateHandler.handle(t);
    } catch (Exception e) {
      logger.error("Unhandled exception found!", e);

      try {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        pw.write("Something went wrong!\n");
        e.printStackTrace(pw);
        byte[] errorReturn = sw.toString().getBytes();
        pw.close();
        sw.close();

        t.sendResponseHeaders(500, errorReturn.length);
        OutputStream os = t.getResponseBody();
        os.write(errorReturn);
        t.close();
      } catch (IOException e1) {
        logger.error("Failed to notify client about the error!", e1);
      }
    }
  }
}
