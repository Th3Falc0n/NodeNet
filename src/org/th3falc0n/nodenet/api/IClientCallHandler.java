package org.th3falc0n.nodenet.api;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientCallHandler extends Remote, Serializable {
  public void handlePacket(Packet pck) throws RemoteException;
  public void handleCommand(String[] command) throws RemoteException;
}
