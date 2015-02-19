package com.github.cambridgeAlphaTeam;

/**
 * A watched thread (implementing {@link Watchable} will be passed an
 * object implementing this interface, and will have to periodically
 * call stillAlive().
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

interface WatchDogNotify {
  public void stillAlive();
}
