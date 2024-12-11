package com.klassen;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator; //zijn van een externe library (zie jar file in de map "lib")
import org.bouncycastle.crypto.params.HKDFParameters;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
//CLIENT
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.nimbus.State;
import java.security.SecureRandom;
import java.util.*;
import java.util.Base64.Encoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Client {
  static ServerFunctions impl;
  String username;
  Registry registry;
  static Gui gui;
  int board_size=0;
  int number_of_boards = 0;
  private MessageDigest sha;

  HashMap<String, CommunicationState> security_information;  //contains key = "A__B" with value CommunicationState: "K_ab, idx_ab, tag_ab"

  public Client() {
    security_information = new HashMap<>();
    try {
      sha = MessageDigest.getInstance("SHA-256");

    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  private void initializeServer() {

    try {
      // fire to localhost port 1099
      registry = LocateRegistry.getRegistry("localhost", 1099);
      impl = (ServerFunctions) registry.lookup("ServerService");
      this.board_size = impl.bulletinBoardGetSize();
      this.number_of_boards = impl.getNumberOfBoards();
    } catch (Exception e) {

      e.printStackTrace();
      System.out.println("server initialize failed");
    }
  }

  public void shutdown() {
    try {
        if (impl != null ) { 
            impl.unregisterClient(username);
        }
        System.out.println("Client successfully unregistered and resources cleaned up.");
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error during client shutdown.");
    }
}

  public void messageInput(String message, String receiver) {
    try {
      send(message, receiver);
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

  public void setup_sender_receiver(String a, String b ,SecretKey K_ab, int board_idx_ab, int board_idx_ba, int idx_ab, String tag_ab, SecretKey K_ba, int idx_ba, String tag_ba){ 
    String map_key = a + "__" + b;
    security_information.put(map_key, new CommunicationState(K_ab, board_idx_ab, idx_ab, tag_ab));
    map_key = b + "__" + a;
    security_information.put(map_key, new CommunicationState(K_ba, board_idx_ba, idx_ba, tag_ba));
  }

  private String hash(String b){
        try{
          byte[] b_bytes = b.getBytes();
          sha.update(b_bytes);
          byte[] hash_b = sha.digest();
          String hash_b_string = Base64.getEncoder().encodeToString(hash_b);
          return hash_b_string;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
  }

  private SecretKey KDF(SecretKey k){
    //nieuwe key, afgeleid uit oude key
    byte[] existingKey = k.getEncoded();
        
    HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new org.bouncycastle.crypto.digests.SHA256Digest());
    hkdf.init(new HKDFParameters(existingKey, null, null));

    // Afgeleide sleutel genereren (bijv. 256 bits = 32 bytes)
    byte[] derivedKey = new byte[existingKey.length];
    hkdf.generateBytes(derivedKey, 0, derivedKey.length);

    return new SecretKeySpec(derivedKey, "AES");
  }

  public void send(String message, String receiver){
    String map_key = username + "__" + receiver; //getting the right key for this sender__receiver pair

    //idx': element of real number between {0,...,n-1} (with n the size of the bulletinboard)
    //Random random = new Random(); //used to generate random numbers (used for generating a new idx)
    SecureRandom sec_rand = new SecureRandom();
    int new_idx = sec_rand.nextInt(board_size); //inclusive zero and exclusive n

    int new_boardidx = sec_rand.nextInt(number_of_boards);
    //tag' element van  T
    String new_tag = generateSafeToken();

    //u = encrypt(message || idx' || tag', sender_receiver)
    //you're the sender
    SealedObject u = encrypt(message + "__" + new_boardidx + "__"+ new_idx + "__" + new_tag, map_key);
    byte[] u_byte = null;
    try{
      u_byte = sealedObject_to_byteArray(u);
    }catch(IOException e){
      System.out.println("IOException: " + e.getMessage());
    }

    //write(idx_AB, u hash(tagAB)) in bulletin board, use the original/old idx and tag: the new_idx and new_tag are meant for the next message
    //impl.bulletinBoard_add(security_information.get(map_key).get_idx(), u, hash(security_information.get(map_key).get_tag()));
    try{
      impl.bulletinBoard_add(security_information.get(map_key).get_boardidx(), security_information.get(map_key).get_idx(), u_byte, hash(security_information.get(map_key).get_tag()));
    }catch(RemoteException e){
      System.out.println("RemoteException: " + e.getMessage());
    }

    //replace the old tag and idx in security_information with tag' (= new_tag) and idx' (= new_idx) for this sender__receiver pair
    security_information.get(map_key).set_idx(new_idx);
    security_information.get(map_key).set_tag(new_tag);
    security_information.get(map_key).set_boardidx(new_boardidx);

    //K_ab (in security_information) = KDF(K_ab)
    //replace the old K in security_information with the new_K for this sender__receiver pair
    SecretKey new_K = KDF(security_information.get(map_key).get_K());
    security_information.get(map_key).set_K(new_K);
  }

  public void reload(){
    //for all chats: try receive
    for(String key: security_information.keySet()){
      receiveAB(key);
    }
  }

  public void receiveAB(String map_key){
    CommunicationState state = security_information.get(map_key);
    //u = get(idx_ab, tag_ab) uit bulletin board
    byte[] u_byte = null;
    try{
      u_byte = impl.bulletinBoard_get(state.get_boardidx(),state.get_idx(), state.get_tag());
    }catch(RemoteException e){
      System.out.println("RemoteException: " + e.getMessage());
    }

    SealedObject u = null;
    try{
      u = byteArray_to_sealedObject(u_byte);
    }catch(Exception e){
      
    }

    if(u != null){
      String message = open(u, map_key);
      if(message != null){
        String[] parts = message.split("__"); 
        String text = parts[0];
        int new_boardidx = Integer.parseInt(parts[1]);
        int idx_new =  Integer.parseInt(parts[2]);
        String tag_new = parts[3];
        

        SecretKey K_ab = KDF(state.get_K());
        security_information.replace(map_key, new CommunicationState(K_ab,new_boardidx, idx_new, tag_new));

        String[] sender_receiver = map_key.split("__");
        showReceivedMessage(sender_receiver[0], text);
      }
    }
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

      //returning of the decrypted message
      return decrypted_message;
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
        gui = new Gui(main);
      });
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public byte[] sealedObject_to_byteArray(SealedObject so) throws IOException{
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
  
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
  
    objectOutputStream.writeObject(so);
  
    objectOutputStream.close();
  
    return byteArrayOutputStream.toByteArray();
  }

  public SealedObject byteArray_to_sealedObject(byte[] b) throws Exception{
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);
  
    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
  
    SealedObject so = (SealedObject) objectInputStream.readObject();
  
    objectInputStream.close();
  
    return so;
  }

  private String generateSafeToken() {
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];
    random.nextBytes(bytes);
    Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    String token = encoder.encodeToString(bytes);
    return token;
}

  String[] generate_initial_security_information_for_connection(String chatName){
    String[] ret = new String[4];
    
    SecureRandom sec_rand = new SecureRandom();
    int idx = sec_rand.nextInt(board_size); //inclusive zero and exclusive n
    
    int boardidx = sec_rand.nextInt(number_of_boards);
    //tag' element van  T
    String tag = generateSafeToken();
    
    SecretKey sKey = null;
    try{
      KeyGenerator keygen = KeyGenerator.getInstance("AES"); //symmetric key
      sKey = keygen.generateKey();
    }catch(NoSuchAlgorithmException e){
      System.out.println("NoSuchAlgorithmException: " + e.getMessage());
    }
    
    if(sKey != null){
      String encodedKey = Base64.getEncoder().encodeToString(sKey.getEncoded());
      System.out.println(); //voor mooiere formatting
      System.out.println("--------------------Security information of " + username + " (for chat "+ chatName + " - "+ username +")"+"--------------------");
      System.out.println("effectieve SecretKey in Base64: " + encodedKey); // om te kunnen ingeven bij de andere client
      System.out.println("boardidx: " + boardidx);
      System.out.println("idx: " + idx);
      System.out.println("tag: " + tag);
      System.out.println("---------------------------------------------------------------------------");

      ret[0] = encodedKey;
      ret[1] = String.valueOf(boardidx);
      ret[2] = String.valueOf(idx);
      ret[3] = tag;
    }

    return ret;
  }

  void set_up_connection(String[] security_information_client, String[] security_information_other_party, String chatName){
    byte[] decodedKey_1 = Base64.getDecoder().decode(security_information_client[0]);
    SecretKey K_ab = new SecretKeySpec(decodedKey_1, 0, decodedKey_1.length, "AES");
    int board_idx_ab = Integer.parseInt(security_information_client[1]);
    int idx_ab = Integer.parseInt(security_information_client[2]);
    String tag_ab = security_information_client[3];

    byte[] decodedKey_2 = Base64.getDecoder().decode(security_information_other_party[0]);
    SecretKey K_ba = new SecretKeySpec(decodedKey_2, 0, decodedKey_2.length, "AES");
    int board_idx_ba = Integer.parseInt(security_information_other_party[1]);
    int idx_ba = Integer.parseInt(security_information_other_party[2]);
    String tag_ba = security_information_other_party[3];

    setup_sender_receiver(username, chatName, K_ab, board_idx_ab, board_idx_ba,  idx_ab, tag_ab, K_ba, idx_ba, tag_ba);
  }

}

class CommunicationState{
  private SecretKey K;
  private int boardidx;
  private int idx;
  private String tag;

  public CommunicationState(SecretKey key, int boardidx, int idx, String tag){
    this.boardidx = boardidx;
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

  public int get_boardidx(){
    return boardidx;
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

  public void set_boardidx(int new_boardidx){
    boardidx = new_boardidx;
  }
}