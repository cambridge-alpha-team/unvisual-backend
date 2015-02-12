package com.github.cambridgeAlphaTeam;

/**
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 */
public class OscException extends Exception {
    
    private static final long serialVersionUID = 5009978710935067174L; // generated

    public OscException(Exception e) {
        super(e);
    }
    
    public OscException(String message) {
        super(message);
    }

}
