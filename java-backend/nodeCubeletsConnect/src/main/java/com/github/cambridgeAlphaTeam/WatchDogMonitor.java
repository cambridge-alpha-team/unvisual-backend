package com.github.cambridgeAlphaTeam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of WatchDog that works on timeouts.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDogMonitor implements WatchDog, WatchDogNotify {
  private long lastLifesign;
  private long timeoutMillis;

  private boolean shutDown;

  private Thread taskThread;
  private Watchable task;

  private static final Logger logger =
    LoggerFactory.getLogger(ServerCubeletsConnection.class);

  /* Note, this is not synchronized! This is so that watched threads
   * don't have to stall. This means that we have a corner case when
   * this and the killing get out of order, and the thread is killed
   * before it is notified.
   * This just essentially means that we have a slightly blurred
   * timeout.
   */
  public void stillAlive() {
    lastLifesign = System.nanoTime();
  }

  public synchronized void setTask(Watchable task, long timeoutMillis) {
    if (null == task) {
      throw new NullPointerException ("The task given is null!");
    } else {
      this.task = task;
      this.taskThread = new Thread(task);
      this.timeoutMillis = timeoutMillis;
      task.setWatchDog(this);
    }
  }

  public synchronized void startTask() {
    if (null == task) {
      throw new NullPointerException ("You have not set a task yet!");
    } else {
      taskThread = new Thread (task);

      if (!taskThread.isAlive()) {
        taskThread.start();
      }

      /* In case the watchdog thread is waiting for a task. */
      notify();
    }
  }

  public synchronized void restartTask() {
    stopTask();
    startTask();
  }

  public synchronized void stopTask() {
    task.cleanup();
  }

  public synchronized void unsetTask() {
    taskThread = null;
    task = null;
  }

  public synchronized void shutDown() {
    shutDown = true;
    /* If we are just waiting */
    notify();
  }

  public Thread getTaskThread() {
    return taskThread;
  }

  public void run() {
    boolean localShutDown = false;
    while (!localShutDown) {
      synchronized (this) {
        localShutDown = shutDown;

        if (null != taskThread && taskThread.isAlive()) {
          /* Stops thread if lastLifesign is too old. */
          try {
            Thread.sleep(timeoutMillis);
          } catch (InterruptedException e) {
            logger.warn("Unexpected interrupt, ignoring: ", e);
          }

          if ((System.nanoTime() - lastLifesign) > timeoutMillis*1000) {
            stopTask();
          }
        } else {
          /* Wait for a thread to watch */
          try {
            wait();
          } catch (InterruptedException e) {
            logger.warn("Unexpected interrupt, ignoring: ", e);
          }
        }
      }
    }
  }
}
