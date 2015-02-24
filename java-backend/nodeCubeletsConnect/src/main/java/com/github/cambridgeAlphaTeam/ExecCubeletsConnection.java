package com.github.cambridgeAlphaTeam;

import com.github.cambridgeAlphaTeam.watchdog.IWatcher;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Arrays;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A more compact approach than using ServerCubeletsConnection, this
 * uses traditional UNIX pipes for messaging.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class ExecCubeletsConnection implements
  IWatchableCubeletsConnection {
  private IWatcher watcher;
  private boolean stop = false;

  private int[] cubeletValues;

  private static ObjectMapper mapper = new ObjectMapper();
  Process cubeletsProcess;

  private static final Logger logger =
    LoggerFactory.getLogger(ExecCubeletsConnection.class);

  public ExecCubeletsConnection(final String[] cmdarray) throws
    IOException {
    /* One for each face of the Bluetooth cube */
    cubeletValues = new int[6];

    cubeletsProcess = Runtime.getRuntime().exec(cmdarray);
  }

  @Override
  public void run() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
            cubeletsProcess.getInputStream()));

      Thread logSTDERR = new Thread() {
        @Override
        public void run() {
          BufferedReader reader = new BufferedReader(new InputStreamReader(
                cubeletsProcess.getErrorStream()));
          while (!stop) {
            String line;
            try {
              line = reader.readLine();
            } catch (IOException e) {
              logger.error("Failed to read an STDERR line from cubelets child process.", e);
              return;
            }

            if (line != null) {
              logger.error("STDERR:" + line);
            }
          }
        }
      };
      logSTDERR.run();

      while (!stop) {
        String line;
        try {
          line = reader.readLine();
        } catch (IOException e) {
          logger.error("Failed to read a line from cubelets child process.", e);
          return;
        }

        if (line != null) {
          watcher.notifyStillAlive(this);

          logger.debug("Got a line: ");
          logger.debug(line);

          try {
            Map<Integer, Integer> readValue = mapper.readValue(line,
            new TypeReference<Map<Integer, Integer>>() { });
            setCubeletValues(readValue);
          } catch (IOException e) {
            logger.error("Failed to convert line read to JSON.", e);
          }
        }
      }

      try {
        reader.close();
      } catch (IOException e) {
        logger.error("Failed to close STDIN.", e);
      }
    } finally {
      watcher.notifyDying(this);
    }
  }

  @Override
  public void setWatcher(IWatcher w) {
    watcher = w;
  }

  @Override
  public void cleanup() {
    stop = true;
  }

  public synchronized int[] getCubeletValues() {
    return cubeletValues;
  }

  public synchronized void setCubeletValues(Map<Integer, Integer>
      cubeletsMap) {
    logger.debug(Arrays.toString(cubeletValues) + "\t" + cubeletsMap);

    SortedSet<Integer> keys = new TreeSet<Integer>(cubeletsMap.keySet());

    int i = 0;
    for (Integer key : keys) {
      if (i < 6) {
        Integer value = cubeletsMap.get(key);
        if (value != null) {
          cubeletValues[i] = value;
        }
        i++;
      } else {
        break;
      }
    }

    messageHandle(cubeletValues);
  }

  public void messageHandle(int[] cubeletValues) {
    /* In case subclassing, you can override this. It is called when
     * cubelet values change.
     */
  }
}
