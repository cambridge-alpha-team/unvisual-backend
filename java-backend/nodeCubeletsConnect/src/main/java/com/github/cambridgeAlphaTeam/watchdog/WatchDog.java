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
  /* Set by setStartupTimeout, unset by notifyStillAlive */
  boolean startingUp = false;

  long timeoutMillis;
  long startupTimeoutMillis;
  long lastLifesign;

  private static final Logger logger =
    LoggerFactory.getLogger(WatchDog.class);

  public synchronized void notifyStillAlive(IWatchable who) {
    /* Only take messages from current task. */
    if (taskObject == who) {
      startingUp = false;
      lastLifesign = System.nanoTime();
      /* Wake up in case we are in startupTimeoutMillis sleep. */
      notify();
    }
  }

  public synchronized void notifyDying(IWatchable who) {
    /* Only restart current task. */
    if (taskObject == who) {
      logger.debug("Restarting task: " + taskObject);
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
    logger.debug("Starting task: " + taskObject);
    taskObject = creator.create();
    taskObject.setWatcher(this);
    taskThread = new Thread(taskObject);
    taskThread.start();
  }

  public synchronized void stopTask() {
    logger.debug("Stopping task: " + taskObject);
    taskObject.cleanup();
    startingUp = true;
  }

  public synchronized void restartTask() {
    stopTask();
    startTask();
  }

  public synchronized T getObject() {
    return taskObject;
  }

  public synchronized void setStartupTimeout(long startupTimeoutMillis) {
    this.startingUp = true;
    this.startupTimeoutMillis = startupTimeoutMillis;
  }

  public void setTimeout(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  public void run() {
    lastLifesign = System.nanoTime();
    while (!shutDown) {
      long timeout;
      if (startingUp)
      {
        logger.debug("Using startupTimeoutMillis of " + startupTimeoutMillis);
        timeout = startupTimeoutMillis;
      }
      else
      {
        logger.debug("Using timeoutMillis of " + timeoutMillis);
        timeout = timeoutMillis;
      }
      /* Acquire lock and go to sleep. */
      synchronized (this) {
        try {
          wait (timeout);
        } catch (InterruptedException e) {
          logger.error("Unexpected interrupt!", e);
        }

        /* Check timeout, if elapsed restart task.  Milliseconds are
         * converted to nanoseconds. */
        if (System.nanoTime() - lastLifesign > timeout*1000*1000) {
          logger.debug("Restarting due to timeout expiration.");
          restartTask();
        }
      }
    }
    /* Stop the task when shutting down. */
    stopTask();
  }
}
