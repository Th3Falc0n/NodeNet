package org.th3falc0n.nodenet.helper;

import java.io.Serializable;

public class LogWriter implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -8371136468996943147L;
  
  private String caption = "n/a";
  
  public LogWriter(String c) {
    caption = c;
  }
  
  public void setCaption(String c) {
    caption = c;
  }
  
  public void println(String ln) {
    System.out.println("[" + caption + "] " + ln);
  }

}
