package com.github.cambridgeAlphaTeam;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * A simple hack to test cubelets connection.
 * This is only for test purposes, use the TCP approach instead, as this
 * may block unexpectedly.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

class ExecCubeletsConnection extends Thread implements
  CubeletsConnection
{ private int[] cubeletValues;
  private static ObjectMapper mapper = new ObjectMapper();
  private static final Logger logger = Logger.getLogger(
                                         ServerCubeletsConnection.class);
  Process cubeletsProcess;

  public ExecCubeletsConnection(final String[] cmdarray) throws
    IOException
  { /* One for each face of the Bluetooth cube */
    cubeletValues = new int[6];

    cubeletsProcess = Runtime.getRuntime().exec(cmdarray);
    setDaemon(true);
    start();
  }

  @Override
  public void run()
  { BufferedReader reader = new BufferedReader(new InputStreamReader(
          cubeletsProcess.getInputStream()));
    while (true)
    { String line;
      try
      { line = reader.readLine();
      }
      catch (IOException e)
      { logger.error(e);
        return;
      }

      if (line != null)
      { logger.debug("Got a line: ");
        logger.debug(line);
        try
        { Map<Integer, Integer> readValue = mapper.readValue(line,
          new TypeReference<Map<Integer, Integer>>() { });
          setCubeletValues(readValue);
        }
        catch (IOException e)
        { logger.error(e);
        }
      }
    }
  }

  public synchronized int[] getCubeletValues()
  { return cubeletValues;
  }

  public synchronized void setCubeletValues(Map<Integer, Integer>
      cubeletsMap)
  { logger.debug("" + cubeletValues + "\t" + cubeletsMap);
    SortedSet<Integer> keys = new TreeSet<Integer>(cubeletsMap.keySet());
    int i = 0;
    for (Integer key : keys)
    { if (i < 6)
      { Integer value = cubeletsMap.get(key);
        if (value != null)
        { cubeletValues[i] = value;
        }
        i++;
      }
      else
      { break;
      }
    }
  }
}
