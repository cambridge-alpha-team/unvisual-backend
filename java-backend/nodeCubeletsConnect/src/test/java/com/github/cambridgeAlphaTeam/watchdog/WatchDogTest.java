package com.github.cambridgeAlphaTeam.watchdog;

import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for the watchdog.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDogTest {
  private static final Logger logger =
    LoggerFactory.getLogger(WatchDogTest.class);

  /**
   * Test to see if watchdog calls cleanup on after the timeout expires.
   */
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
    watchDog.setTimeout(5);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    for (int i = 0; i < 5 && !test.getCleanedUp(); i++) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        /* Do nothing. */
      }
    }

    Assert.assertTrue(test.getCleanedUp());
    watchDog.shutDown();
  }

  /**
   * Do nothing watchable object.
   */
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

  /**
   * Tests the try-finally based notifyDying() call, which should create
   * new objects.
   */
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

  /**
   * Constantly call notifyDying() on watcher object.
   */
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

  /**
   * Tests to see if cleanup() is called when it should not be.
   * (That is when the object keeps calling notifyStillAlive().)
   */
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
    watchDog.setTimeout(10);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    long endTime = System.nanoTime()+50*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(50);
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    DontRestartTest test2 = watchDog.getObject();
    Assert.assertEquals(test1, test2);

    watchDog.shutDown();
  }

  /**
   * A well-behaved (that is it calls notifyStillAlive()) object. Though
   * in theory an object also calling notifyDying() is even better
   * behaved (this is tested in DontDoubleRestartTest).
   */
  private static class DontRestartTest implements IWatchable {
    IWatcher watcher;
    boolean stop = false;

    @Override
    public synchronized void cleanup() {
      stop = true;
      notify();
    }

    @Override
    public synchronized void setWatcher(IWatcher w) {
      watcher = w;
    }

    @Override
    public synchronized void run() {
      while (!stop) {
        try {
          wait(5);
        } catch (InterruptedException e) {
          /* Do nothing. */
        }
        watcher.notifyStillAlive(this);
      }
    }
  }

  /**
   * Tests to see if failing to call notifyStillAlive(), then stopping
   * due to cleanup being called and calling notifyDying() due to the
   * try-finally block would call cleanup one more time.
   */
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

    Assert.assertEquals(1, test.getCleanups());

    watchDog.shutDown();
  }

  /**
   * Counts the number of times cleanup has been called. Notify is
   * deliberately set to a too high time, so that the watchdog calls
   * cleanup(), but otherwise this is a well-behaved object.
   */
  private static class DontDoubleRestartTest implements IWatchable {
    IWatcher watcher;
    int cleanups = 0;
    public synchronized int getCleanups() {
      return cleanups;
    }

    @Override
    public synchronized void cleanup() {
      cleanups++;
      notify();
    }

    @Override
    public synchronized void setWatcher(IWatcher w) {
      watcher = w;
    }

    @Override
    public synchronized void run() {
      try {
        while (cleanups == 0) {
          try {
            wait(1*1000); /* One second */
          } catch (InterruptedException e) {
            /* Do nothing. */
          }
        }
      } finally {
        watcher.notifyDying(this);
      }
    }
  }

  /**
   * Checks that when the task is (re)started, it uses the startup
   * timeout (hence the normal timeout is set to a high value).
   */
  @Test
  public void testStartupTimer() {
    CountStartupsCreator countStartupsCreator = new CountStartupsCreator();

    IWatchDog<WatchableTest> watchDog = new WatchDog<WatchableTest>(countStartupsCreator);

    IWatchable test = watchDog.getObject();
    Assert.assertNotNull(test);
    watchDog.setTimeout(1*1000);
    watchDog.setStartupTimeout(10);

    Thread watchDogThread = new Thread(watchDog);
    watchDogThread.start();

    long endTime = System.nanoTime()+10*1000*1000;
    while (System.nanoTime() < endTime) {
      try {
        Thread.sleep(25); /* 12ms to give a small margin. By 10ms, *
                           * should have restarted test twice.     */
      } catch(InterruptedException e) {
        /* Do nothing. */
      }
    }

    /* Starting up timer reset after process death. */
    Assert.assertEquals(2, countStartupsCreator.getStartupCount());

    watchDog.shutDown();
  }

  /**
   * Counts the number of times WatchableTest is created (which is a
   * task that fails to call notifyStillAlive() so times out).
   * This is a creator object, unlike the ones above, which were
   * watchable objects.
   */
  private static class CountStartupsCreator implements ICreator<WatchableTest> {
    private int startupCount = 0;
    public int getStartupCount() { return startupCount; }

    @Override
    public WatchableTest create() {
      startupCount++;
      return new WatchableTest();
    }
  }

  /**
   * Checks to see if the timer switches to the normal one, after the
   * first notifyStillAlive().
   */
  @Test
  public void testAliveTimer() {
    IWatchDog<DontRestartTest> watchDog =
    new WatchDog<DontRestartTest>(new ICreator<DontRestartTest>() {
      @Override
      public DontRestartTest create() {
        return new DontRestartTest();
      }
    });

    DontRestartTest test1 = watchDog.getObject();
    Assert.assertNotNull(test1);
    /* If timer never switches, we would never call cleanup(). */
    watchDog.setStartupTimeout(50);
    /* DontRestartTest uses 5ms. */
    watchDog.setTimeout(1);

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

    DontRestartTest test2 = watchDog.getObject();
    Assert.assertNotEquals(test1, test2);

    watchDog.shutDown();
  }
}
