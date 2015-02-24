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
  public void testTimeoutRestart() {
    /* See below for WatchableTest definition. */
    IWatchDog<WatchableTest> watchDog =
    new WatchDog<WatchableTest>(new ICreator<WatchableTest>() {
      @Override
      public WatchableTest create() {
        return new WatchableTest();
      }
    });
    WatchableTest test = watchDog.getObject();
    Assert.assertNotNull(test);
    watchDog.setTimeout(100);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    for (int i = 0; i < 5 && !test.getCleanedUp(); i++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        /* Do nothing. */
      }
    }

    Assert.assertTrue(test.getCleanedUp());
    watchDog.shutDown();
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
            /* Do nothing. */
          }
        }
      }
    }

    @Override
    public void setWatcher(IWatcher w) {
      /* Do nothing, as we want cleanup to be called. */
    }
  }

  @Test
  public void testDyingRestart() {
    IWatchDog<DyingTest> watchDog =
    new WatchDog<DyingTest>(new ICreator<DyingTest>() {
      @Override
      public DyingTest create() {
        return new DyingTest();
      }
    });

    DyingTest test1 = watchDog.getObject();
    Assert.assertNotNull(test1);
    watchDog.setTimeout(10);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    /* Shorter than timeout so that we know cleanup was called because
     * of notifyDying. */
    long endTime = System.nanoTime()+5*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(100);
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    DyingTest test2 = watchDog.getObject();
    Assert.assertNotEquals(test1, test2);

    watchDog.shutDown();
  }

  private static class DyingTest implements IWatchable {
    IWatcher watcher;

    @Override
    public void cleanup() {
    }

    @Override
    public void run() {
      try {
        /* Do nothing. */
      } finally {
        watcher.notifyDying(this);
      }
    }

    @Override
    public void setWatcher(IWatcher w) {
      watcher = w;
    }
  }

  @Test
  public void testAliveDontRestart() {
    IWatchDog<DontRestartTest> watchDog =
    new WatchDog<DontRestartTest>(new ICreator<DontRestartTest>() {
      @Override
      public DontRestartTest create() {
        return new DontRestartTest();
      }
    });

    DontRestartTest test1 = watchDog.getObject();
    Assert.assertNotNull(test1);
    watchDog.setTimeout(100);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    long endTime = System.nanoTime()+200*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(100);
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    DontRestartTest test2 = watchDog.getObject();
    Assert.assertEquals(test1, test2);

    watchDog.shutDown();
  }

  private static class DontRestartTest implements IWatchable {
    IWatcher watcher;
    boolean stop = false;

    @Override
    public void cleanup() {
      stop = true;
    }

    @Override
    public void setWatcher(IWatcher w) {
      watcher = w;
    }

    @Override
    public void run() {
      while (!stop) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          /* Do nothing. */
        }
        watcher.notifyStillAlive(this);
      }
    }
  }

  @Test
  public void testAliveDontDoubleRestart() {
    IWatchDog<DontDoubleRestartTest> watchDog =
      new WatchDog<DontDoubleRestartTest>(new
    ICreator<DontDoubleRestartTest>() {
      @Override
      public DontDoubleRestartTest create() {
        return new DontDoubleRestartTest();
      }
    });

    DontDoubleRestartTest test = watchDog.getObject();
    Assert.assertNotNull(test);
    watchDog.setTimeout(5);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    long endTime = System.nanoTime()+10*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(10);
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    Assert.assertEquals(test.getCleanups(), 1);

    watchDog.shutDown();
  }

  private static class DontDoubleRestartTest implements IWatchable {
    IWatcher watcher;
    int cleanups = 0;
    public int getCleanups() {
      return cleanups;
    }

    @Override
    public void cleanup() {
      cleanups++;
    }

    @Override
    public void setWatcher(IWatcher w) {
      watcher = w;
    }

    @Override
    public void run() {
      try {
        while (cleanups == 0) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            /* Do nothing. */
          }
        }
      } finally {
        watcher.notifyDying(this);
      }
    }
  }

  @Test
  public void testStartupTimer() {
    CountStartupsCreator countStartupsCreator = new CountStartupsCreator();

    IWatchDog<WatchableTest> watchDog = new WatchDog<WatchableTest>(countStartupsCreator);

    IWatchable test = watchDog.getObject();
    Assert.assertNotNull(test);
    watchDog.setTimeout(50*1000);
    watchDog.setStartupTimeout(5);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    long endTime = System.nanoTime()+10*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(10);
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    Assert.assertEquals(1, countStartupsCreator.getStartupCount());

    watchDog.shutDown();
  }

  private static class CountStartupsCreator implements ICreator<WatchableTest> {
      private int startupCount = 0;
      public int getStartupCount() { return startupCount; }

      @Override
      public WatchableTest create() {
        startupCount++;
        return new WatchableTest();
      }
    }
}
