package org.th3falc0n.nodenet.client;

import java.util.Arrays;
import org.th3falc0n.nodenet.api.Packet;
import org.th3falc0n.nodenet.helper.ByteArrayHelper;
import org.th3falc0n.nodenet.helper.LogWriter;

class ReactionThread extends Thread {
  private volatile boolean toBePaused = false, isPaused = false;
  LogWriter logger = new LogWriter("reathr");
  
  @Override
  public void run() {
    super.run();

    while (true) {
      if (toBePaused) {
        isPaused = true;
      } else {
        isPaused = false;

        try {
          byte[] packet = Client.getInstance().receive(1);
          
          if(packet[0] == 127) {    
            byte[] data = packet[1] != 0 ? Client.getInstance().receive(packet[1]) : new byte[0];
            
            short subID  = (short) (((short)packet[2] << 8) + ((short)packet[3]));
            int packetID = ((int)packet[4] << 24) + ((int)packet[5] << 16) + ((int)packet[6] << 8) + ((int)packet[7]);
            
            if(Client.getInstance().reawra.packetHandlers.containsKey(packetID)) {
              Packet pck = new Packet();
              pck.data = data;
              pck.subID = subID;
              pck.packetID = packetID;
              pck.fromAddr = ByteArrayHelper.addressToString(Arrays.copyOfRange(packet, 8, 16));
              
              Client.getInstance().reawra.packetHandlers.get(packetID).handlePacket(pck);
            }
            else
            {
              logger.println("Received packet without registered handler.");
            }                      
          } else {
            if(packet[0] == 2) {
            }

            logger.println("error");
          }
        }
        catch (Exception e) {
          logger.println("Exception: " + e.getMessage());
        }
      }
    }
  }

  void pause() {
    toBePaused = true;
    while (!isPaused)
      ;
  }

  void unpause() {
    toBePaused = false;
  }
}
