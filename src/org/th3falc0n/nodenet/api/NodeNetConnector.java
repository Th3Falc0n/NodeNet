package org.th3falc0n.nodenet.api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.th3falc0n.nodenet.Config;

public class NodeNetConnector {
  public static IRemoteClient getRemoteClient() throws RemoteException, NotBoundException {    
    Registry registry = LocateRegistry.getRegistry(9231);
    IRemoteClient client = (IRemoteClient) registry.lookup(Config._REMOTE_OBJECT_NAME);
    
    return client;
  }
}
