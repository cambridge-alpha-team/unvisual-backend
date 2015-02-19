package com.github.cambridgeAlphaTeam.watchdog;

/**
 * This is a concrete implementation of {@link IWatchDog}.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

public class WatchDog<T extends IWatchable> implements IWatchDog<T> {
  ICreator<T> creator;
  T taskObject;
  Thread taskThread;

  long millis;
  long lastLifesign;

  public void stillAlive() {
    lastLifesign = System.nanoTime();
  }

  public WatchDog(ICreator<T> creator) {
    this.creator = creator;
    startTask();
  }

  public void startTask() {
    taskObject = creator.create();
    taskThread = new Thread(taskObject);
  }

  public T getObject() {
    return taskObject;
  }

  public void setTimeout(long millis) {
    this.millis = millis;
  }

  public void run() {
  }
}
