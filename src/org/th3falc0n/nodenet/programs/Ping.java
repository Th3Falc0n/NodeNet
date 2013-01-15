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
    public void handlePacket(Packet pck) throws RemoteException {
      if(pck.subID == 0) client.send(new Packet(1, 1), pck.fromAddr);
      if(pck.subID == 1) client.postTerminalMessage(logger, "Pong!");
    }

    @Override
    public void handleCommand(String[] command) throws RemoteException {
      if(command[1] == null) {
        postUsage();
        return;
      }
      String address = command[1];
      client.send(new Packet(1, 0), address);
    }
    
    private void postUsage() throws RemoteException {
      client.postTerminalMessage(logger, "Usage: ping <String:address>");
    }
    
  }
  
  public static void runIntegrated() {
    try {
      client = NodeNetConnector.getRemoteClient();
      
      CallHandler handler = new Ping.CallHandler();
      client.registerPacketHandler(1, handler);
      client.registerTerminalCommand("ping", handler);
      client.postTerminalMessage(logger, "Registered Program");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
