package com.klassen;

import java.rmi.RemoteException;
//CLIENT
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.nimbus.State;

import java.util.*;
import java.security.SecureRandom;
import java.util.Base64;

public class Client {
  static ServerFunctions impl;
  ClientFunctionsImpl clientImpl;
  String username;
  Registry registry;
  static Gui gui;

  HashMap<String, CommunicationState> security_information;  //contains key = "A-B" with value CommunicationState: "K_ab, idx_ab, tag_ab"

  public Client() {
    security_information = new HashMap<>();
  }

  private void initializeServer() {

    try {
      // fire to localhost port 1099
      registry = LocateRegistry.getRegistry("localhost", 1099);
      impl = (ServerFunctions) registry.lookup("ServerService");
    } catch (Exception e) {

      e.printStackTrace();
      System.out.println("server initialize failed");
    }
  }

  public void initializeClient() {
    try {
      clientImpl = new ClientFunctionsImpl(username);
      registry.rebind("ClientService", clientImpl);
      // impl.registerClient(clientImpl);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("client initialize failed");
    }
  }

  public void shutdown() {
    try {
        if (impl != null && clientImpl != null) {
            //impl.unregisterClient(clientImpl);
            impl.unregisterClient(username);
            //registry.unbind("ClientService");
        }
        System.out.println("Client successfully unregistered and resources cleaned up.");
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error during client shutdown.");
    }
}

  public void messageInput(String message, String receiver) {
    try {
      System.out.println(message);
      // if (receiver == null) {
      //   //impl.sendMessage(message, clientImpl);
      // } else {
        //impl.sendPrivateMessage(message, clientImpl, receiver);
      // }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void showReceivedMessage(Boolean isPrivate, String sender, String message) {
    // if (!isPrivate) {
    //   gui.addMessageToChat("Group Chat", "[" + sender + "]: " + message, false);
    // } else {
      gui.addMessageToChat(sender, message, false);
    // }
  }

  public static boolean userInUse(String username) {
    try{
      if (impl.isUser(username)) {
        return true;
      }
    }catch(Exception e){
      e.printStackTrace();
    }
    
    return false;
  }

  public void setup_sender_receiver(String sender_a, String receiver_b ,String K_ab, String idx_ab, String tag_ab, String K_ba, String idx_ba, String tag_ba){  //moet nog opgeroepen worden bij initialisatie stap, bv in beide clients iets overtypen als begin
    String map_key = sender_a + "-" + receiver_b;
    security_information.put(map_key, new CommunicationState(K_ab, idx_ab, tag_ab));
    map_key = receiver_b + "-" + sender_a;
    security_information.put(map_key, new CommunicationState(K_ba, idx_ba, tag_ba));
  }

  public void send(String sender_receiver, String message){
    //needs to be implemented
  }

  public void receiveAB(){
    //needs to be implemented
  }

  private String encrypt(String data, String key){
    //needs to be implemented
    return null;
  }

  private String open(String encryptedData, String key){
    //needs to be implemented
    return null;
  }

  
  public static void main(String[] args) throws RemoteException {
    try {
      Client main = new Client();
      main.initializeServer();

      SwingUtilities.invokeLater(() -> {
        String username;
        ArrayList<String> usernames = new ArrayList<>();
        String username_check_result;
        try {
          do {

            username = JOptionPane.showInputDialog(null, "Enter your username:");

            username_check_result = impl.check_and_add_username(username);

            if (username == null || username.trim().isEmpty()) {
              JOptionPane.showMessageDialog(null, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (username_check_result.equals("Username already in use.")) {
              JOptionPane.showMessageDialog(null, "Username already taken. Try again.", "Error",
                  JOptionPane.ERROR_MESSAGE);
            }
          } while (username == null || username.trim().isEmpty()
              || username_check_result.equals("Username already in use."));

          main.username = username;

          usernames.add(username);

          
        } catch (Exception e) {
          e.printStackTrace();
        }
        main.initializeClient();
        gui = new Gui(main);
      });
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}

class CommunicationState{
  private String K;
  private String idx;
  private String tag;

  // private String sender;
  // private String receiver;

  public CommunicationState(String key, String idx, String tag){
    this.K = key;
    this.idx = idx;
    this.tag = tag;
  }
}