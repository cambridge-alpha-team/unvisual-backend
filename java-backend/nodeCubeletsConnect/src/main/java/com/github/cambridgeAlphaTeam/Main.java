package com.github.cambridgeAlphaTeam;

import com.github.cambridgeAlphaTeam.watchdog.IWatchable;
import com.github.cambridgeAlphaTeam.watchdog.IWatchDog;
import com.github.cambridgeAlphaTeam.watchdog.ICreator;
import com.github.cambridgeAlphaTeam.watchdog.WatchDog;

import java.util.Arrays;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger logger =
    LoggerFactory.getLogger(Main.class);

  public static void main (final String[] args) throws
    NumberFormatException,
    IOException {
    if (args.length > 0) {
      //ICubeletsConnection conn = new ServerCubeletsConnection(
      //  Integer.parseInt(args[0]));

      IWatchDog<IWatchableCubeletsConnection> watchDog =
        new WatchDog<IWatchableCubeletsConnection>(
      new ICreator<IWatchableCubeletsConnection>() {
        @Override
        public ExecCubeletsConnection create() {
          try {
            return new ExecCubeletsConnection(args);
          } catch (IOException e) {
            logger.error("Unable to open process!", e);
            return null;
          }
        }
      });
      watchDog.setTimeout(2000);
      Thread watchDogThread = new Thread(watchDog);
      watchDogThread.start();

      while (true) {
        System.out.println("|---------------|");
        for (int value : watchDog.getObject().getCubeletValues()) {
          System.out.println("|\t" + value + "\t|");
        }
        System.out.println("|---------------|");

        try {
          Thread.sleep(1000);
        } catch(InterruptedException e) {
          /* Do nothing. */
        }
      }
    } else {
      System.out.println("Not enough arguments, expected program to " +
                         "execute and arguments (if any)");
    }
  }
}
