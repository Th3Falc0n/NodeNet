package org.th3falc0n.nodenet.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {
  static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  
  public static String readLine() {
    try {
      return reader.readLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "";
  }
}
