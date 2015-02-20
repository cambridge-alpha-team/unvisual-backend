package com.github.cambridgeAlphaTeam;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.BindException;

import com.sun.net.httpserver.*;

/**
 * Demo for FileHandler.java, serves all files either in the current
 * directory (if no arguments are given) or in the directory given by
 * the first argument.
 * The second argument optionally represents a file to send when we
 * get a file not found.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
public class FileServerDemo {

  public static void main(String[] args) throws IOException {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

      if (args.length == 1 && new File(args[0]).exists()) {
        server.createContext("/", new LoggerHandler(new FileHandler(args[0])));
      } else if (args.length == 2 && new File(args[0]).exists()
                 && new File(args[1]).exists()) {
        server.createContext("/", new LoggerHandler(new FileHandler(args[0],
                             args[1])));
      } else {
        server.createContext("/", new LoggerHandler(new FileHandler()));
      }

      server.setExecutor(null); // creates a default executor
      server.start();
      System.out.println("Server started");
    } catch (BindException e) {
      System.out.println("Can not bind to port: " + e.getMessage());
    }
  }
}
