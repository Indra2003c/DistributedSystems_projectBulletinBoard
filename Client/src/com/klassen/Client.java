package com.klassen;

import java.io.IOException;
import java.rmi.RemoteException;
//CLIENT
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.nimbus.State;

import java.util.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Client {
  static ServerFunctions impl;
  // ClientFunctionsImpl clientImpl;
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

  // public void initializeClient() {
  //   try {
  //     clientImpl = new ClientFunctionsImpl(username);
  //     registry.rebind("ClientService", clientImpl);
  //     // impl.registerClient(clientImpl);
  //   } catch (Exception e) {
  //     e.printStackTrace();
  //     System.out.println("client initialize failed");
  //   }
  // }

  public void shutdown() {
    try {
        if (impl != null ) { //&& clientImpl != null
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

  public void setup_sender_receiver(String sender_a, String receiver_b ,SecretKey K_ab, int idx_ab, String tag_ab, SecretKey K_ba, int idx_ba, String tag_ba){  //moet nog opgeroepen worden bij initialisatie stap, bv in beide clients iets overtypen als begin
    String map_key = sender_a + "-" + receiver_b;
    security_information.put(map_key, new CommunicationState(K_ab, idx_ab, tag_ab));
    map_key = receiver_b + "-" + sender_a;
    security_information.put(map_key, new CommunicationState(K_ba, idx_ba, tag_ba));
  }

  private String hash(String x){
    //needs to be implemented
    return null;
  }

  private SecretKey KDF(SecretKey k){
    //needs to be implemented
    //nieuwe key, afgeleid uit oude key
    return null;
  }

  public void send(String message, String receiver){
    String map_key = username + "-" + receiver; //getting the right key for this sender-receiver pair

    //idx': element of real number between {0,...,n-1} (with n the size of the bulletinboard)
    Random random = new Random(); //used to generate random numbers (used for generating a new idx)
    int new_idx = random.nextInt(BulletinBoard.get_size()); //inclusive zero and exclusive n
    //tag' element van reeel getal T
    String new_tag = "nog te doen"; //NOG DOEN

    //u = encrypt(message || idx' || tag', sender_receiver)
    //you're the sender
    SealedObject u = encrypt(message + "__" + new_idx + "__" + new_tag, map_key);

    //write(idx_AB, u hash(tagAB)) in bulletin board, use the original/old idx and tag: the new_idx and new_tag are meant for the next message
    impl.bulletinBoard_add(security_information.get(map_key).get_idx(), u, hash(security_information.get(map_key).get_tag()));

    //replace the old tag and idx in security_information with tag' (= new_tag) and idx' (= new_idx) for this sender-receiver pair
    security_information.get(map_key).set_idx(new_idx);
    security_information.get(map_key).set_tag(new_tag);

    //K_ab (in security_information) = KDF(K_ab)
    //replace the old K in security_information with the new_K for this sender-receiver pair
    SecretKey new_K = KDF(security_information.get(map_key).get_K());
    security_information.get(map_key).set_K(new_K);
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

  private SealedObject encrypt(String value, String map_key){ //value =  message || idx || tag
    SecretKey sKey = security_information.get(map_key).get_K(); //get the symmetric key of the sender
    try{
      //encryption of value (= "message || idx || tag")
      Cipher c = Cipher.getInstance("AES");
      c.init(Cipher.ENCRYPT_MODE, sKey);
      SealedObject encrypted_message = new SealedObject(value, c);

      //returning the encrypted message
      return encrypted_message;
    }catch (NoSuchAlgorithmException e){
      System.out.println("NoSuchAlgorithmException: " + e.getMessage());
    }catch (NoSuchPaddingException e){
      System.out.println("NoSuchPaddingException: " + e.getMessage());
    }catch (InvalidKeyException e){
      System.out.println("InvalidKeyException: " + e.getMessage());
    }catch (IllegalBlockSizeException e) {
      System.out.println("IllegalBlockSizeException: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }

    //returning null if the try/catch fails
    return null;
  }

  private String open(SealedObject value, String map_key){
    SecretKey sKey = security_information.get(map_key).get_K(); //get the symmetric key of the receiver (wich is identical to the one of the sender)
    try{
      //decryption of value (= the received message)
      Cipher c = Cipher.getInstance("AES");
      c.init(Cipher.DECRYPT_MODE, sKey);
      String decrypted_message = (String)value.getObject(c);

      //seperating m || idx  || tag
      String[] parts = decrypted_message.split("__"); //we assume a message format of: message__idx__tag
      String message = parts[0];

      //get the current state of the client
      CommunicationState current_state = security_information.get(map_key);

      //update the idx and the tag after receiving the message
      String idx = parts[1];
      String tag = parts[2];
      current_state.set_idx(Integer.parseInt(idx));
      current_state.set_tag(tag);

      //returning of the decrypted message
      return message;
    }catch (NoSuchAlgorithmException e){
      System.out.println("NoSuchAlgorithmException: " + e.getMessage());
    }catch (NoSuchPaddingException e){
      System.out.println("NoSuchPaddingException: " + e.getMessage());
    }catch (InvalidKeyException e){
      System.out.println("InvalidKeyException: " + e.getMessage());
    }catch (IllegalBlockSizeException e) {
      System.out.println("IllegalBlockSizeException: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } catch (ClassNotFoundException e) {
      System.out.println("ClassNotFoundException: " + e.getMessage());
    } catch (BadPaddingException e) {
      System.out.println("BadPaddingException: " + e.getMessage());
    }

    //returning null if the try/catch fails
    return null;
  }

  //TODO: om de zoveel tijd moet client pollen om te kijken of er message is voor hem (receive doen) of met reload knop
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
        //main.initializeClient();
        gui = new Gui(main);
      });
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}

class CommunicationState{
  private SecretKey K;
  private int idx;
  private String tag;

  // private String sender;
  // private String receiver;

  public CommunicationState(SecretKey key, int idx, String tag){
    this.K = key;
    this.idx = idx;
    this.tag = tag;
  }

  //getters
  public SecretKey get_K(){
    return K;
  }

  public int get_idx(){
    return idx;
  }

  public String get_tag(){
    return tag;
  }

  //setters
  public void set_idx(int new_idx){
    idx = new_idx;
  }

  public void set_tag(String new_tag){
    tag = new_tag;
  }

  public void set_K(SecretKey new_K){
    K = new_K;
  }
}