package com.github.cambridgeAlphaTeam.watchdog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a concrete implementation of {@link IWatchDog}.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDog<T extends IWatchable> implements IWatchDog<T> {
  final ICreator<T> creator;
  T taskObject;
  Thread taskThread;
  boolean shutDown = false;

  long timeoutMillis;
  long lastLifesign;

  private static final Logger logger =
    LoggerFactory.getLogger(WatchDog.class);

  public synchronized void notifyStillAlive(IWatchable who) {
    /* Only take messages from current task. */
    if (taskObject == who) {
      lastLifesign = System.nanoTime();
    }
  }

  public synchronized void notifyDying(IWatchable who) {
    /* Only restart current task. */
    if (taskObject == who) {
      restartTask();
    }
  }

  public synchronized void shutDown() {
    shutDown = true;
    notify();
  }

  public WatchDog(ICreator<T> creator) {
    this.creator = creator;
    startTask();
  }

  public synchronized void startTask() {
    taskObject = creator.create();
    taskObject.setWatcher(this);
    taskThread = new Thread(taskObject);
    taskThread.start();
  }

  public synchronized void stopTask() {
    taskObject.cleanup();
  }

  public synchronized void restartTask() {
    stopTask();
    startTask();
  }

  public synchronized T getObject() {
    return taskObject;
  }

  public void setTimeout(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  public void run() {
    while (!shutDown) {
      /* Acquire lock and go to sleep. */
      synchronized (this) {
        try {
          wait (timeoutMillis);
        } catch (InterruptedException e) {
          logger.error("Unexpected interrupt!", e);
        }

        /* Check timeout, if elapsed restart task.  Milliseconds are
         * converted to nanoseconds. */
        if (System.nanoTime() - lastLifesign > timeoutMillis*1000*1000) {
          restartTask();
        }
      }
    }
    /* Stop the task when shutting down. */
    stopTask();
  }
}
