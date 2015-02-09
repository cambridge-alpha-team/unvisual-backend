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
    
    private static InetAddress localhost;
    private static final int port = 12345;
    private OSCPortOut sender;
    
    public OscSender() throws SocketException, UnknownHostException {
        localhost = InetAddress.getLocalHost();
        sender = new OSCPortOut(localhost, port);
    }
    
    public void close() {
        sender.close();
    }
    
    public void send(OSCMessage message) throws IOException {
        sender.send(message);
    }
    
    public void sendCode(String code) throws IOException {
        OSCMessage toSend = new OSCMessage("/run-code");
        toSend.addArgument(code);
        send(toSend);
    }
    
    public static void main(String[] args) throws IOException {
        OscSender oscs = new OscSender();
        oscs.sendCode("This is a test");
        oscs.close();
        System.out.println("OSC message sent");
    }

}
