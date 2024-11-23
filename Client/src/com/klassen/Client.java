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

  private static void showReceivedMessage(String sender, String message) {
      gui.addMessageToChat(sender, message, false);
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

  private String hash(String x){
    //needs to be implemented
    return null;
  }

  private String KDF(String k){
    //needs to be implemented
    //nieuwe key, afgeleid uit oude key
    return null;
  }

  public void send(String message){ //String sender_receiver, 
    //needs to be implemented

    //idx' element van reeel getal tussen {0,...,n-1} met size van bulletinboard n
    //tag' element van reeel getal T

    //u = encrypt(message || idx' || tag', sender_receiver)
    //you're the sender

    //write(idx_AB, u hash(tagAB)) in bulletin board, oorspronkelijke idx en tag
      //oorspronkelijke idx, want de idx' is voor bericht erna analoog voor tag

    //put tag' and idx' from security_information with key "sender-receiver"
      //in plaats van oorspronkelijke tag en idx

    //K_ab (in security_information) = KDF(K_ab)
    
  }

  public void receiveAB(){
    //needs to be implemented

    //u = get(idx_ab, tag_ab) uit bulletin board

    //if (u != null)
      //and (m||idx'||tag')=open(u) is succesfull
      //then
        //put in security_information idx and tag, needed for next message:
        //idx_ab = idx'_ab veranderen in security infromation
        //tag_ab = tag'_ab veranderen in security infromation

        //K_ab = KDF(K_ab) in security information

        //m is received message =>showReceivedMessage(sender, m)
        //showreceivedMesssage or return m...
    
    //indien return m. dan hier in de else: return null


    
  }

  private String encrypt(String value, String map_key){
    //needs to be implemented
    //value =  message || idx || tag

    //encrypt "message || idx || tag" with the symmetric key  from security_information with key map_key
    
    //return encrypted message

    return null;
  }

  private String open(String value, String map_key){
    //needs to be implemented

    //decrypt with symmetrix key in security_information map_key

    //seperate m || idx  || tag
    

    //return decrypted message

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