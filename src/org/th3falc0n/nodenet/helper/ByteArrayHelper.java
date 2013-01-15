package org.th3falc0n.nodenet.helper;

import org.th3falc0n.nodenet.AddressFormatException;

public class ByteArrayHelper {
  public static String addressToString(byte[] addr) {
    if(addr.length != 8) return "n/a";
    
    StringBuilder addrs = new StringBuilder();
    
    for(int i = 0; i < 8; i++) {
      addrs.append(String.format("%02x", addr[i]) + (i != 7 ? ":" : ""));
    }
    
    return addrs.toString();
  }
  
  public static byte[] stringToAddress(String in) throws AddressFormatException {
    String[] raw = in.split(":");
    if(raw.length != 8) throw new AddressFormatException("Illegal length");
    
    byte[] addr = hexStringToByteArray(in.replace(":", ""));
    
    return addr;
  }
  
  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }
  
  public static byte[] insertInto(byte[] input, byte[] addr, int pos) {
    return insertInto(input, addr, pos, 8);
  }

  public static byte[] insertInto(byte[] input, byte[] addr, int pos, int length) {
    for(int i = 0; i < length; i++) {
      int n = i + pos;
      input[n] = addr[i];
    }
    return input;
  }
}
