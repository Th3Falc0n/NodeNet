package org.th3falc0n.nodenet.api;

import java.io.Serializable;

public class Packet implements Serializable {
  private static final long serialVersionUID = -8180792470063976869L;
  
  public Packet() {
    
  }
  
  public Packet(int pID, int sID) {
    packetID = pID;
    subID = (short)sID;
  }
  
  public Packet(int pID, int sID, byte[] d) {
    packetID = pID;
    subID = (short)sID;
    data = d;
  }
  
  public int    packetID   = 0;
  public short  subID      = 0;
  public byte[] data = new byte[0];
  public String fromAddr   = "";
}
