package org.th3falc0n.nodenet.programs;

import java.rmi.RemoteException;

import org.th3falc0n.nodenet.api.IClientCallHandler;
import org.th3falc0n.nodenet.api.IRemoteClient;
import org.th3falc0n.nodenet.api.TerminalProgram;
import org.th3falc0n.nodenet.api.NodeNetConnector;
import org.th3falc0n.nodenet.api.Packet;
import org.th3falc0n.nodenet.helper.LogWriter;

public class Ping extends TerminalProgram {
  static LogWriter logger = new LogWriter("ping");
  static IRemoteClient client = null;
  
  private static class CallHandler implements IClientCallHandler {
    private static final long serialVersionUID = 1L;

    @Override
    public void handlePacket(Packet pck) throws RemoteException { //Is called if a packet is received for this handler.
      if(pck.subID == 0) client.send(new Packet(1, 1), pck.fromAddr); //subID 0 is a ping request. Just answer this with a subID 1 (ping answer)
      if(pck.subID == 1) client.postTerminalMessage(logger, "Pong!"); //subID 1 is a ping answer. Post "Pong!" to terminal stdio.
    }

    @Override
    public void handleCommand(String[] command) throws RemoteException { //Is called if a terminal command is executed for this handler.
      if(command[1] == null) { //argument 1 is the address and may not be empty
        postUsage();
        return;
      }
      client.send(new Packet(1, 0), command[1]); //Send ping request packet.
    }
    
    private void postUsage() throws RemoteException {
      client.postTerminalMessage(logger, "Usage: ping <String:address>");
    }
    
  }
  
  public static void runIntegrated() { //runIntegrated is main equivalent for a program which is started from the Terminal thread.
    try {
      client = NodeNetConnector.getRemoteClient(); //Get the NodeNet-Client instance from RMI Server
      
      CallHandler handler = new Ping.CallHandler(); //Create instance of CallHandler
      client.registerPacketHandler(1, handler); //Register packetID 1 (TCP/IP port equivalent) requests to be handled by CallHandler
      client.registerTerminalCommand("ping", handler); //Register the Terminal command "ping" to be handled by CallHandler
      client.postTerminalMessage(logger, "Registered Program"); //Post "Registered Program" message to terminal stdio
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
