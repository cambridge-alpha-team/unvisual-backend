package com.github.cambridgeAlphaTeam.watchdog;

/**
 * This is the interface objects that want to be restarted by the
 * WatchDog should implement.
 * It will automatically be run.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
interface IWatchable extends Runnable {
  public void cleanup();
  public void setWatcher(IWatcher w);
}
