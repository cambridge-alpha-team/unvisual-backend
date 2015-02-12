package com.github.cambridgeAlphaTeam;

import java.net.SocketException;
import java.util.Date;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;

/**
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 */
public class OscTestReceiver implements OSCListener
{

  @Override
  public void acceptMessage(Date time, OSCMessage message)
  { System.out.println("MESSAGE RECEIVED");
    System.out.println("Address: " + message.getAddress());
    System.out.println("Message: " + message.getArguments().get(0));
  }

  public static void main(String[] args) throws SocketException
  { OSCPortIn in = new OSCPortIn(12345);
    in.addListener("/run-code", new OscTestReceiver());
    System.out.println("Starting listener");
    in.startListening();
  }

}
