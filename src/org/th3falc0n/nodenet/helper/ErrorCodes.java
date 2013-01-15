package org.th3falc0n.nodenet.helper;

public class ErrorCodes {
  public static String resolveErrorCode(byte code) {
    switch(code) {
    case 100:
      return "Client not available";
    default:
      return "Unknown";
    }
  }
}
