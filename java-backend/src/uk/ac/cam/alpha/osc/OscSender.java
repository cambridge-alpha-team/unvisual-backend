package uk.ac.cam.alpha.osc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

/**
 * @author Isaac Dunn &lt;ird28@cam.ac.uk&gt;
 */
public class OscSender {
    
    private static final int port = 4557;
    private OSCPortOut sender;
    private static final String EXAMPLE_CODE =
            "loop do sample :perc_bell, rate: (rrand 0.125, 1.5)\n sleep rrand(0.1, 2)\n end ";
    
    public OscSender() throws OscException {
        try {
            /* Warning for future to avoid repeat of pain:
             * do not use InetAddress.getlocalHost() */
            InetAddress localhost = InetAddress.getByName("127.0.0.1");
            sender = new OSCPortOut(localhost, port);
        } catch (SocketException e) {
            throw new OscException(e);
        } catch (UnknownHostException e) {
            throw new OscException(e);
        }
    }
    
    public void close() {
        sender.close();
    }
    
    public void send(OSCMessage message) throws OscException {
        try {
            sender.send(message);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OscException(e);
        }
    }
    
    public void sendCode(String code) throws OscException {
        OSCMessage toSend = new OSCMessage("/run-code");
        toSend.addArgument(code);
        send(toSend);
    }
    
    public void stopAll() throws OscException {
        send(new OSCMessage("/stop-all-jobs"));
    }
    
    public static void main(String[] args) throws OscException {
        OscSender oscs = new OscSender();
        oscs.sendCode(EXAMPLE_CODE);
        oscs.close();
    }

}
