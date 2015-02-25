package com.github.cambridgeAlphaTeam;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;
import java.util.HashMap;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.net.httpserver.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves files from a given directory.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class JarHandler implements HttpHandler {
  /* Where to serve files from */
  private final String servePath;
  /* Buffer size when reading in files */
  private static final int bufferSize = 10*1024;

  private static final Logger logger = LoggerFactory.getLogger(JarHandler.class);

  static final Map<String, String> contentTypes;
  static {
    contentTypes = new HashMap<>();
    contentTypes.put(".css", "text/css");
    contentTypes.put(".html", "text/html");
    contentTypes.put(".js", "text/javascript");
  }

  public JarHandler() {
    servePath = "";
  }

  public JarHandler(String servePath) {
    this.servePath = servePath;
  }

  public JarHandler(String servePath, String notFoundPath) {
    this.servePath = servePath;
  }

  public void handle(HttpExchange t) throws IOException {
    try {
      ClassLoader classLoader = this.getClass().getClassLoader();
      /* Always absolute, that is starts with "/" */
      String reqPath = t.getRequestURI().getPath();
      InputStream requestedFile = classLoader.getResourceAsStream
        (servePath + reqPath);

      if (null != requestedFile && !reqPath.endsWith("/")) {
        try {
          String extension = reqPath.substring(reqPath.lastIndexOf("."));
          t.getResponseHeaders().add("Content-Type", contentTypes.get(extension));
        } catch (IndexOutOfBoundsException e) {
          t.getResponseHeaders().add("Content-Type", "application/octet-stream");
        }

        ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
        copyStream(requestedFile, tmpStream);

        t.sendResponseHeaders(200, tmpStream.size());
        OutputStream os = t.getResponseBody();
        tmpStream.writeTo(os);
      } else {
        /* Try appending index.html */
        String greeter = reqPath + "index.html";
        InputStream greeterFile = classLoader.getResourceAsStream
          (servePath + greeter);

        if (null != greeterFile) {
          t.getResponseHeaders().add("Content-Type", contentTypes.get(".html"));

          ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
          copyStream(greeterFile, tmpStream);

          t.sendResponseHeaders(200, tmpStream.size());
          OutputStream os = t.getResponseBody();
          tmpStream.writeTo(os);
        } else {
          logger.info("Not found file: \"" + servePath + reqPath + "\"");
          t.getResponseHeaders().add("Content-Type", "text/plain");
          byte[] response = "Error 404: File not found".getBytes();
          t.sendResponseHeaders(404, response.length);
          OutputStream os = t.getResponseBody();
          os.write(response);
        }
      }
    } finally {
      t.close();
    }
  }

  /**
   * Writes a file into OutputStream os from InputStream is, then LEAVES
   * both is and os OPEN!
   */
  private int copyStream(InputStream is, OutputStream os) throws IOException {
    int n;
    byte[] buffer = new byte[bufferSize];

    while ((n = is.read(buffer)) > 0) {
      os.write(buffer, 0, n);
    }

    return n;
  }
}
