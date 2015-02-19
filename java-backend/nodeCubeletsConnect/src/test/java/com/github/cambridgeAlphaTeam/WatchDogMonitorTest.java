package com.github.cambridgeAlphaTeam;

import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tests WatchDogMonitor for (a) killing threads after a timeout,
 * (b) calling cleanup on them.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDogMonitorTest {
  private static final Logger logger =
    LoggerFactory.getLogger(ServerCubeletsConnection.class);

  @Test
  public void testRestartingThread() {
    KillTest test  = new KillTest();
    WatchDog watch = new WatchDogMonitor();
    watch.setTask(test, 100);
    watch.startTask();

    Thread watchThread = new Thread(watch);
    Thread taskThread = watch.getTaskThread();

    Assert.assertNotNull(taskThread);
    Assert.assertTrue(taskThread.isAlive());

    watchThread.start();

    for (int i = 0; i < 5 && test.getKilled(); i++) {
      try {
        Thread.sleep(100);
      } catch(InterruptedException e) {
        /* Do nothing */
      }
    }

    Assert.assertFalse(test.getKilled());
    watch.shutDown();
  }

  private static class KillTest implements Watchable {
    private boolean started = false;
    private boolean killed = false;
    public boolean getStarted() {
      return started;
    }
    public boolean getKilled() {
      return killed;
    }

    public void setWatchDog(WatchDog monitor) {
      /* Do nothing, as want this to be killed. */
    }

    public synchronized void cleanup() {
      killed = true;
    }

    public void run() {
      synchronized (this) {
        /* If not started already */
        if (!started)
        {
          started = true;
          killed = false;
        }
        else
        {
          /* If this is a restart, just do nothing */
          return;
        }
      }

      boolean localKilled = false;
      while (!localKilled) {
        /* Busy wait, waiting to be killed. */
        synchronized (this) {
          localKilled = killed;
        }
      }
    }

    public void setWatchDog(WatchDogNotify w) {
      /* Do nothing, as not going to call stillAlive() */
    }
  }
}
