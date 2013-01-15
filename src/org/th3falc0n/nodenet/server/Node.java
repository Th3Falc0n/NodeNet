package org.th3falc0n.nodenet.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Node {
  public static final String _VERSION = "srv0.0.1";
  
  public static void main(String args[]) {
    try {      
      System.out.println("Welcome to NodeNet " + _VERSION);
      System.out.println("This is a server node");
      
      System.out.println("Creating ServerSocket... (listening on Port 9232)");
      ServerSocket sock = new ServerSocket(9232);
      
      boolean active = true;
      
      while(active) {
        Socket client = sock.accept();
        (new ClientThread(client)).start();
      }
      
      sock.close();
      
    } catch (IOException e) {
      System.out.println("Exception: " + e.getMessage());
    }
  }
}
