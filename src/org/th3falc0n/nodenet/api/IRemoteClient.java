package org.th3falc0n.nodenet.api;

import java.rmi.*;

import org.th3falc0n.nodenet.helper.LogWriter;


public interface IRemoteClient extends Remote {
  public Exception send(Packet packet, String address) throws RemoteException;
  public String getClientAddress() throws RemoteException;
  public boolean registerPacketHandler(int packetID, IClientCallHandler handler) throws RemoteException;
  public boolean registerTerminalCommand(String command, IClientCallHandler handler) throws RemoteException;
  public void postTerminalMessage(LogWriter l, String ln) throws RemoteException;
}