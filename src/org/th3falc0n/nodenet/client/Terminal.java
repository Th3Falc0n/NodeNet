package org.th3falc0n.nodenet.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.th3falc0n.nodenet.api.IClientCallHandler;
import org.th3falc0n.nodenet.helper.Console;
import org.th3falc0n.nodenet.helper.LogWriter;
import org.th3falc0n.nodenet.programs.Message;
import org.th3falc0n.nodenet.programs.Ping;

public class Terminal {  
  static LogWriter logger = new LogWriter("client");
  
  static Client cli = null;
  
  static Map<String, IClientCallHandler> commands = new HashMap<String, IClientCallHandler>();
  
  public static void main(String[] args) {     
    System.out.print("Node Address: ");
    String naddr = Console.readLine();
    
    cli = Client.init(logger, naddr, 9232);
    
    Ping.runIntegrated();
    Message.runIntegrated();
    
    while(true) {
      String l = Console.readLine();
      
      Matcher mat =  Pattern.compile("(\".*?\"|[^ ]+),?").matcher(l);
      List<String> matches = new ArrayList<String>();
      while(mat.find()){
          matches.add(mat.group(1).replace("\"", ""));
      }
      
      String[] command = new String[matches.size()];
      matches.toArray(command);
      
      if(commands.containsKey(command[0].toLowerCase())) {
        try {
          commands.get(command[0].toLowerCase()).handleCommand(command);
        } catch (RemoteException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else
      {
        logger.println("Unknown command");
      }
      
    }
    
  }
}
