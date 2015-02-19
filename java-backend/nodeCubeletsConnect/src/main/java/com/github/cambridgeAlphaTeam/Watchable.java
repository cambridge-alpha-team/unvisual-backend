package com.github.cambridgeAlphaTeam;

/**
 * The interface a class must implement to be used with
 * {@link WatchDog}.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

interface Watchable extends Runnable {
  /**
   * This should set the watch dog, on which the stillAlive() method
   * should be called
   */
  public void setWatchDog(WatchDogNotify monitor);

  /**
   * This is for any clean up that should be done when a thread is being
   * forcefully stopped.
   */
  public void cleanup();
}
