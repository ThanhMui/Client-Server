/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aboullaite;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mohammed
 */
// For every client's connection we call this class -- class serrver
public class clientThread extends Thread
{
  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private static final Map<String,Integer> listUsers = new HashMap<>() ;
  private static final Map<String,String> pairUsers = new HashMap<>() ;
  

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      
      /* Enter & Check Name */
      while (true) {
        os.println("Enter your name nao.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {// tim thấy trong chuỗi k có @ thì k bảo nhập tên nữa
            if( listUsers.size()>0){
                if(listUsers.containsKey(name)){
                    os.println("Name is exist! Please choose another Name!");
                } else {
                    /*  
                        Status of User
                        1: waiting 
                        2: chatting
                    */
                    listUsers.put(name, 1);
                    break;
                }
            } else {
                /*  
                    Status of User
                    1: waiting 
                    2: chatting
                */
                listUsers.put(name, 1);
                break;
            }
        } else {
          os.println("The name should not contain '@' character.");
        }
      }
      
      /* Welcome the new the client. */
//      os.println("Welcome " + name
//          + " to our chat room.\nTo leave enter /quit in a new line.");

        os.println("Please waiting to match with another user...");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = name;
            break;
          }
        }
      }
      
        /* Random User Chat */
        boolean isbreak = false;
        while (!isbreak) {
            if(pairUsers.containsKey(name) || pairUsers.containsValue(name)){
                break;
            }
            
            for (Map.Entry<String,Integer> entry : listUsers.entrySet()){
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                String username = entry.getKey();
                if(username.equals(name)){
                    continue;
                }
                Integer stt = entry.getValue();
                if(stt == 1){
                    pairUsers.put(name, username);
                    entry.setValue(2);
                    break;
                }
            }
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch(Exception ex){
                break;
            }
//            break;
        }
        /* Random User Chat */
        
        while(true) {
            synchronized (this) {
                String username = "";
                for (Map.Entry<String,String> entry : pairUsers.entrySet()){
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if(key.equals(name)){
                        username = value;
                    }
                    if(value.equals(name)){
                        username = key;
                    }
                }
                
                for (int i = 0; i < threads.length; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(username)) {
//                    threads[i].os.println("You are match with <" + name + ">. Are you accept chat: y/n ?");
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                    this.os.println("You are match with <" + username + ">. Are you accept chat: y/n ?");
                    break;
                  }
                }
                break;
            }
        }
        
        
      /* Start the conversation. */
//      while (true) {
//        String line = is.readLine();
//        if (line.startsWith("/quit")) {
//            
//          break;
//        }
//        /* If the message is private sent it to the given client. */
//        if (line.startsWith("@")) {
//            /* Chat Client vs Client */
//          String[] words = line.split("\\s", 2);
//          if (words.length > 1 && words[1] != null) {
//            words[1] = words[1].trim();
//            if (!words[1].isEmpty()) {
//              synchronized (this) {
//                for (int i = 0; i < maxClientsCount; i++) {
//                  if (threads[i] != null && threads[i] != this
//                      && threads[i].clientName != null
//                      && threads[i].clientName.equals(words[0])) {
//                    threads[i].os.println("<" + name + "> " + words[1]);
//                    /*
//                     * Echo this message to let the client know the private
//                     * message was sent.
//                     */
//                    this.os.println(">" + name + "> " + words[1]);
//                      System.out.println("<" + name + "> " + words[1]);
//                    break;
//                  }
//                }
//              }
//            }
//          }
//        } else {
//            /* Chat Group */
//            /* The message is public, broadcast it to all other clients. */
//            synchronized (this) {
//              for (int i = 0; i < maxClientsCount; i++) {
//                if (threads[i] != null && threads[i].clientName != null) {
//                  threads[i].os.println("<" + name + "> " + line);
//                    System.out.println("<" + name + "> " + line);
//                }
//              }
//            }
//        }
//      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("*** The user " + name
                + " is leaving the chat room !!! ***");
          }
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}
