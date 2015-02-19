package com.github.cambridgeAlphaTeam;

/**
 * A watch dog is a class implementing this interface that if a timeout
 * expires kills the code set by
 * setTask(Watchable task, int timeoutMillis).
 * The task should periodically call stillAlive() specified by the
 * WatchDogNotify interface.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

interface WatchDog extends Runnable {
  /**
   * This does not start the task, call startTask afterwards!
   */
  public void setTask(Watchable task, long timeoutMillis);

  public void startTask();
  public void restartTask();
  public void stopTask();

  /**
   * This does not stop the task, call stopTask() before!
   */
  public void unsetTask();

  /**
   * Call this when you want to shutdown the watchdog thread.
   */
  public void shutDown();

  /**
   * This does nothing with the watchdog, so joining to the returned
   * thread may kill your joining thread too.
   */
  public Thread getTaskThread();
}
