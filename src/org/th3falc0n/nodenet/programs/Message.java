package org.th3falc0n.nodenet.programs;

import java.rmi.RemoteException;

import org.th3falc0n.nodenet.api.IClientCallHandler;
import org.th3falc0n.nodenet.api.IRemoteClient;
import org.th3falc0n.nodenet.api.NodeNetConnector;
import org.th3falc0n.nodenet.api.Packet;
import org.th3falc0n.nodenet.api.TerminalProgram;
import org.th3falc0n.nodenet.helper.LogWriter;

public class Message extends TerminalProgram {
  static LogWriter logger = new LogWriter("msg");
  static IRemoteClient client = null;

  private static class CallHandler implements IClientCallHandler {
    private static final long serialVersionUID = 1L;

    @Override
    public void handlePacket(Packet pck) throws RemoteException {
      client.postTerminalMessage(logger, "From: " + pck.fromAddr);
      if(pck.subID == 0) client.postTerminalMessage(logger, "Message: " + new String(pck.data));
    }

    @Override
    public void handleCommand(String[] command) throws RemoteException {
      if(command[1] == null) {
        postUsage();
        return;
      }
      String address = command[1];
      if(command[2] == null) {
        postUsage();
        return;
      }
      client.send(new Packet(2, 0, command[2].getBytes()), address);
    }
    
    private void postUsage() throws RemoteException {
      client.postTerminalMessage(logger, "Usage: msg <String:address> <String:message>");
    }
    
  }
  
  public static void runIntegrated() {
    try {
      client = NodeNetConnector.getRemoteClient();
      
      CallHandler handler = new Message.CallHandler();
      client.registerPacketHandler(2, handler);
      client.registerTerminalCommand("msg", handler);
      client.postTerminalMessage(logger, "Registered Program");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
