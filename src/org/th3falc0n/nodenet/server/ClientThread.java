package org.th3falc0n.nodenet.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.th3falc0n.nodenet.ProtocolFormatException;
import org.th3falc0n.nodenet.helper.ByteArrayHelper;
import org.th3falc0n.nodenet.helper.LogWriter;

public class ClientThread extends Thread {
  public static Map<String, ClientThread> clients = new HashMap<String, ClientThread>();
  
  public volatile List<byte[]> sendBuffer = new ArrayList<byte[]>();
  
  private Socket client = null;
  private PublicKey key_cli;
  
  private KeyGenerator keygen;
  private SecureRandom random = new SecureRandom();
  private SecretKey key_aes;
  
  LogWriter logger = new LogWriter("Client <unknown>");

  private CipherInputStream in = null;
  private CipherOutputStream out = null;

  ClientThread(Socket c) {
    super();
    client = c;
    if(client == null) {
      logger.println("Trying to open client thread without socket...");
    }
  }
  
  @Override
  public void run() {
    super.run();

    try {
      logger.println("Trying to receive client public key...");
      
      byte[] rawkey = new byte[162];
      client.getInputStream().read(rawkey);
      
      key_cli = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rawkey));
      
    } catch (IOException e) {
      logger.println("Failed to read client public key: " + e.getMessage());
    } catch (InvalidKeySpecException e) {
      logger.println("Failed to read client public key: " + e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      logger.println("Failed to read client public key: " + e.getMessage());
    }
    
    createAESKey();
    try {
      sendEncAESKey(client.getOutputStream());
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    try {
      logger.println("Initializing AES stream...");
      
      Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, key_aes);

      in = new CipherInputStream(client.getInputStream(), cipher);
      
      cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key_aes);

      out = new CipherOutputStream(client.getOutputStream(), cipher);
    }
    catch (Exception e) {
      logger.println("Error while creating AES streams: " + e.getMessage());
    }
    
    try {
      logger.println("Awaiting handshake...");
      
      while(!handshake());
      
      ClientSendThread sthr = new ClientSendThread(this);
      
      sthr.start();
      
      while(true) {
        byte[] header = receive(1); //receive the header.
        
        logger.println("Client request " + header[0]);
        
        if(header[0] == 127) { //Just forward/route this client->client packet.
          logger.println("Client to Client");
          byte[] cliaddr = Arrays.copyOfRange(header, 8, 16); //Extract the client address
          
          byte[] clicli = receive(header[1]);
          
          if(clients.containsKey(ByteArrayHelper.addressToString(cliaddr))) {
            logger.println("Adding client request to send stack...");
            clients.get(ByteArrayHelper.addressToString(cliaddr)).sendBuffer.add(clicli);
          }
          else
          {
            logger.println("Client not available");
            send(new byte[]{2, 100}); //Client not available
          }  
        }
      }
      
      
    } catch (IOException e) {
      logger.println("Unexpected exception: " + e.getClass().getCanonicalName()  + e.getMessage());
    } catch (ProtocolFormatException e) {
      logger.println("Unexpected exception: " + e.getClass().getCanonicalName()  + e.getMessage());
    }
  }
  
  private boolean handshake() throws IOException, ProtocolFormatException {
    byte[] hsPacket = receive(1);
    
    if(hsPacket[0] != 1) {
      throw new ProtocolFormatException("Illegal client request (Handshake)");
    }
    
    byte[] cliaddr = Arrays.copyOfRange(hsPacket, 1, 9);
    
    if(!clients.containsKey(cliaddr)) {
      clients.put(ByteArrayHelper.addressToString(cliaddr), this);
      logger.setCaption("Client " + ByteArrayHelper.addressToString(cliaddr));
      logger.println("Handshake successfull...");
      send(new byte[] {1, 1});
      return true;
    }
    else {
      logger.println("Handshake failed (address in use)...");
      send(new byte[] {1, 0});
      return false;
    }
  }
  
  void send(byte[] raw) throws IOException {
    byte[] buffer = Arrays.copyOf(raw, raw.length + (16 - raw.length % 16) % 16);
    out.write(buffer);
    out.flush();
  }
  
  private byte[] receive(int size) throws IOException {
    byte[] buffer = new byte[size * 16];
    in.read(buffer);
    return buffer;
  }
  
  private void createAESKey() {
    try {
      keygen = KeyGenerator.getInstance("AES");
    } catch (NoSuchAlgorithmException e) {
      logger.println("Failed to create AES Key: " + e.getMessage());
    }
    random = new SecureRandom();
    keygen.init(random);
    key_aes = keygen.generateKey();
  }
  
  private void sendEncAESKey(OutputStream stream) {
    try {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.WRAP_MODE, key_cli);
      byte[] encryptedAesKey = cipher.wrap(key_aes);
      stream.write(encryptedAesKey);
    }
    catch (Exception e) {
      logger.println("Failed to send AES Key: " + e.getMessage());
    }
  }
}
