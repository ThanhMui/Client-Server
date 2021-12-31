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
public class ClientThread extends Thread
{
  private String clientName = null;
  public Boolean isAccepted = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final ClientThread[] threads;
  private int maxClientsCount;
  private static final Map<String,Integer> listUsers = new HashMap<>() ;
  private static final Map<String,String> pairUsers = new HashMap<>() ;
  

  public ClientThread(Socket clientSocket, ClientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

    public void run() {
    int maxClientsCount = this.maxClientsCount;
    ClientThread[] threads = this.threads;
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
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.");

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
            
            boolean isPair = false;
            String username = "";
            for (Map.Entry<String,Integer> entry : listUsers.entrySet()){
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                username = entry.getKey();
                if(username.equals(name)){
                    continue;
                }
                Integer stt = entry.getValue();
                if(stt == 1){
                    isPair = true;
                    break;
                }
            }

            if(isPair){
                for (Map.Entry<String,Integer> entry : listUsers.entrySet()){
                    if(entry.getKey().equals(username)){        
                        entry.setValue(2);
                    }
                }
                pairUsers.put(name, username);
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

                this.os.println("You are match with <" + username + ">. Are you accept chat: y/n ?");
                break;
            }
        }
        
        
        while(true) {
            String line = is.readLine();
            if(line != ""){
                if("y".equals(line)) {
                    this.isAccepted = true;
                    break;
                } else {
                    this.isAccepted = false;
                    break;
                }
            }
        }

        boolean isFirstTime = true;
        while(true) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch(Exception ex){
                break;
            }
            synchronized (this) {
                ClientThread thr = getThreadByClientName(name);
                String username = getUsernameMatched(name);
                if(this.isAccepted != null && thr.isAccepted != null){

                    if(this.isAccepted == true && thr.isAccepted == true){
                        // Start Chat
                        this.os.println("Congratulation <" + username + "> accepted! Let's start!");
                        while (true) {
                            String line = is.readLine();
                            if (line.startsWith("/quit")) {
                              break;
                            }
                            thr.os.println("<" + name + "> " + line.trim());
                        }
                        break;
                    } else {
                        
                        this.os.println("Sorry <" + username + "> is not accepted!");
                        break;
                    }
                } else if(thr.isAccepted == null){
                    if(isFirstTime){
                        this.os.println("Please waiting <" + username + "> accepted!");
                        isFirstTime = false;
                    }
                }
            }
        }

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

    public ClientThread getThreadByClientName(String name){
        String username = getUsernameMatched(name);

        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null && threads[i] != this
                && threads[i].clientName != null
                && threads[i].clientName.equals(username)) {
                return threads[i];
            }
        }
        return null;
    } 
    
    public String getUsernameMatched(String name){
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

        return username;
    }
}
