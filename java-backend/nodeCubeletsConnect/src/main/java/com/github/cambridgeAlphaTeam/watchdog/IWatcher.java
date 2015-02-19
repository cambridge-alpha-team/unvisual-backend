package com.github.cambridgeAlphaTeam.watchdog;

/**
 * This is the interface for the watchdog, as seen from the view of the
 * object being watched.
 * The important point here is the stillAlive() call which the object
 * should periodically call, to avoid its cleanup() method from being
 * called.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
public interface IWatcher {
  public void notifyStillAlive(IWatchable who);
  public void notifyDying(IWatchable who);
}
