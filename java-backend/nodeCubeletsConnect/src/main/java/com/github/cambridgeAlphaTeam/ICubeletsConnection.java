package com.github.cambridgeAlphaTeam;

/**
 * Interface to represent a cubelets connection.
 * @author Kovacsics Robert &lt;rmk35@cam.ac.uk&gt;
 */

interface ICubeletsConnection {
  public int[] getCubeletValues();

  /* In case anonymous subclassing, you can override this. It is called
   * when cubelet values change. */
  public void messageHandle(int[] cubeletValues);
}
