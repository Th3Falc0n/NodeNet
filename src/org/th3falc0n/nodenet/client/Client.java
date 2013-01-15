package org.th3falc0n.nodenet.client;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.th3falc0n.nodenet.PacketSizeException;
import org.th3falc0n.nodenet.ProtocolFormatException;
import org.th3falc0n.nodenet.api.Packet;
import org.th3falc0n.nodenet.helper.ByteArrayHelper;
import org.th3falc0n.nodenet.helper.LogWriter;

public class Client {
  private static Client $Instance = null;
  
  private Socket server = null;
  private PublicKey key_pub;
  private PrivateKey key_pri;
  
  private CipherInputStream in = null;
  private CipherOutputStream out = null;
  
  LogWriter logger = null;
  
  private Random rnd = new Random();
  
  byte[] addr = null;
  
  ReactionThread reathr;
  RemoteAccessWrapper reawra;
  
  public static Client getInstance() {
    return $Instance;
  }
  
  public static Client init(LogWriter l, String rootNode, int port) {    
    if($Instance != null) {
      return $Instance;
    }
    $Instance = new Client(l, rootNode, port);
    return $Instance;
  }
  
  private Client(LogWriter l, String rootNode, int port) {
    this.logger = l;
    
    logger.println("Connecting...");
    try {
      server = new Socket(rootNode, port);
    } catch (IOException e) {
      logger.println("Cannot connect: " + e.getMessage());
      System.exit(1);
    }
    
    try {
      logger.println("Generating clientside keypair...");
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(1024);
      
      KeyPair kp = kpg.generateKeyPair();
      
      key_pub = kp.getPublic();
      key_pri = kp.getPrivate();
    } catch (NoSuchAlgorithmException e) {
      logger.println("Error while creating keypair: " + e.getMessage());
    }

    try {
      logger.println("Sending RSA key for AES init...");
      server.getOutputStream().write(key_pub.getEncoded());
      server.getOutputStream().flush();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      System.exit(1);
    }
    
    try {
      logger.println("Initializing AES stream...");
      byte[] wrappedKey = new byte[128];
      server.getInputStream().read(wrappedKey);

      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.UNWRAP_MODE, key_pri);
      Key key = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
      
      cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key);

      in = new CipherInputStream(server.getInputStream(), cipher);
      
      cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key);

      out = new CipherOutputStream(server.getOutputStream(), cipher);
    }
    catch (Exception e) {
      logger.println("Error while creating AES streams: " + e.getMessage());
    }
    
    try {
      logger.println("Sending handshake...");
   
      while(!handshake());
      
      logger.println("Handshake successfull! Addr: " + ByteArrayHelper.addressToString(addr));
    }
    catch (IOException e) { 
      logger.println("Unexpected Exception: " + e.getClass().getCanonicalName() + e.getMessage());
    } catch (ProtocolFormatException e) {
      logger.println("Unexpected Exception: " + e.getClass().getCanonicalName() + e.getMessage());
    }
    
    logger.println("Starting RAW...");
    
    try {
      reawra = new RemoteAccessWrapper();
    } catch (RemoteException e2) {
      logger.println("Can't open RAW: " + e2.getMessage());
    }
    
    logger.println("Starting receive thread...");
    
    reathr = new ReactionThread();
    reathr.start();
  }

  private boolean handshake() throws IOException, ProtocolFormatException {
    byte[] hsPacket = new byte[9];
    
    rnd.nextBytes(hsPacket);
    hsPacket[0] = 1;
    
    addr = Arrays.copyOfRange(hsPacket, 1, 9);
    
    send(hsPacket);
    
    byte[] result = receive(1);
    
    if(result[0] != 1) throw new ProtocolFormatException("Illegal server answer (Handshake)");
    if(result[1] == 0) return false;
    else return true;
  }
  
  Exception sendPacket(Packet packet, String address) {
    try {
      if(packet.data.length > 254 * 16) {
        throw new PacketSizeException("Packet data too long");
      }
      
      byte[] raw = new byte[32 + packet.data.length];
      byte[] dst = ByteArrayHelper.stringToAddress(address);
      
      ByteArrayHelper.insertInto(raw, dst, 8);                                          //dest Address for the server
      ByteArrayHelper.insertInto(raw, Client.$Instance.addr, 24);                       //source Address in the CliCliPacket
      ByteArrayHelper.insertInto(raw, packet.data, 32, packet.data.length); //Add the additional data. 
      
      byte length = (byte)((packet.data.length + (16 - packet.data.length % 16) % 16) / 16);
      
      raw[0]  = 127; //It's a client packet
      raw[1]  = (byte) (length + 1);
      
      raw[16] = 127; //Client packet magic number
      raw[17] = (byte) length;
      raw[18] = (byte) ((packet.subID >> 8) & 0xFF);
      raw[19] = (byte) (packet.subID & 0xFF);
      raw[20] = (byte) ((packet.packetID >> 24) & 0xFF);
      raw[21] = (byte) ((packet.packetID >> 16) & 0xFF);
      raw[22] = (byte) ((packet.packetID >> 8) & 0xFF);
      raw[23] = (byte) (packet.packetID & 0xFF);
      
      send(raw);
     }
    catch (Exception e)  {
      return e;
    }
    return null;
  }
  
  void send(byte[] raw) throws IOException {
    byte[] buffer = Arrays.copyOf(raw, raw.length + (16 - raw.length % 16) % 16);
    out.write(buffer);
    out.flush();
  }
  
  byte[] receive(int size) throws IOException {
    byte[] buffer = new byte[size * 16];
    in.read(buffer);
    return buffer;
  }
}
