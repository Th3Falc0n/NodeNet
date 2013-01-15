package org.th3falc0n.nodenet.client;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.th3falc0n.nodenet.Config;
import org.th3falc0n.nodenet.api.IClientCallHandler;
import org.th3falc0n.nodenet.api.IRemoteClient;
import org.th3falc0n.nodenet.api.Packet;
import org.th3falc0n.nodenet.helper.ByteArrayHelper;
import org.th3falc0n.nodenet.helper.LogWriter;

public class RemoteAccessWrapper extends UnicastRemoteObject implements IRemoteClient {  
  private static final long serialVersionUID = -2477501125975563906L;
  private static final LogWriter logger = new LogWriter("RAW");
  
  Map<Integer, IClientCallHandler> packetHandlers = new HashMap<Integer, IClientCallHandler>();
  RemoteAccessWrapper() throws RemoteException {
    super();
    
    System.setProperty("java.security.policy", "file://" + (new File("rmi.policy")).getAbsolutePath());
    //System.setProperty("java.rmi.server.codebase", "file://" + (new File("/")).getAbsolutePath());
    
    if(System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }
    
    try {      
      Registry registry = LocateRegistry.createRegistry(9231);
      registry.rebind(Config._REMOTE_OBJECT_NAME, this);
      logger.println("RAW bound");
    } catch (Exception e) {
        logger.println("Exception while trying to bind RAW: " + e.getMessage());
    }
  }

  @Override
  public Exception send(Packet packet, String address) throws RemoteException {
    return Client.getInstance().sendPacket(packet, address);
  }

  @Override
  public boolean registerPacketHandler(int packetID, IClientCallHandler handler) throws RemoteException {    
    if(packetHandlers.containsKey(packetID)) return false;
    
    packetHandlers.put(packetID, handler);
    
    logger.println("Allocated packetID=" + packetID);
    
    return true;
  }

  @Override
  public String getClientAddress() throws RemoteException {
    return ByteArrayHelper.addressToString(Client.getInstance().addr);
  }

  @Override
  public boolean registerTerminalCommand(String command, IClientCallHandler terminalHandler) throws RemoteException {
    if(Terminal.commands.containsKey(command)) return false;
    Terminal.commands.put(command, terminalHandler);
    return true;
  }

  @Override
  public void postTerminalMessage(LogWriter l, String ln) throws RemoteException {
    l.println(ln);
  }

}
