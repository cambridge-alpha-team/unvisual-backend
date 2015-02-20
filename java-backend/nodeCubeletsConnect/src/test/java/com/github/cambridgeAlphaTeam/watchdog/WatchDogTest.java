package com.github.cambridgeAlphaTeam.watchdog;

import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A unit test for the watchdog, to see if it times out and calls
 * cleanup().
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDogTest {
  private static final Logger logger =
    LoggerFactory.getLogger(WatchDogTest.class);

  @Test
  public void testRestart() {
    /* See below for WatchableTest definition */
    IWatchDog<WatchableTest> watchDog =
      new WatchDog<WatchableTest>(
    new ICreator<WatchableTest>() {
      @Override
      public WatchableTest create() {
        return new WatchableTest();
      }
    }
    );
    WatchableTest test = watchDog.getObject();
    Assert.assertNotNull(test);
    watchDog.setTimeout(100);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    for (int i = 0; i < 5 && !test.getCleanedUp(); i++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        /* Do nothing */
      }
    }

    Assert.assertTrue(test.getCleanedUp());
  }

  private static class WatchableTest implements IWatchable {
    private boolean cleanedUp = false;
    public boolean getCleanedUp() {
      return cleanedUp;
    }

    public WatchableTest() {
    }

    @Override
    public synchronized void cleanup() {
      cleanedUp = true;
      notify();
    }

    @Override
    public void run() {
      while (!cleanedUp) {
        synchronized (this) {
          try {
            wait();
          } catch (InterruptedException e) {
            /* Do nothing */
          }
        }
      }
    }

    @Override
    public void setWatcher(IWatcher w) {
      /* Do nothing, as we want cleanup to be called. */
    }
  }
}
