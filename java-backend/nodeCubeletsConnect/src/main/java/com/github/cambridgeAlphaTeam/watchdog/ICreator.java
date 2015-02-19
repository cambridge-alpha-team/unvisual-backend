package com.github.cambridgeAlphaTeam.watchdog;

/**
 * This is the interface with a sole responsibility of creating an
 * object (that is of the type T, a type parameter).
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */
public interface ICreator<T> {
  public T create();
}
