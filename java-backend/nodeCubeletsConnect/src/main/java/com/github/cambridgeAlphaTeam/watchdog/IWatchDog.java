package com.github.cambridgeAlphaTeam.watchdog;

/**
 * This is the interface for the watchdog, as seen from the view of the
 * class creating the watched object.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
public interface IWatchDog<T extends IWatchable> extends Runnable,
  IWatcher {
  public T getObject();
  public void setTimeout(long timeoutMillis);
  public void shutDown();
}
