package com.klassen;
//SERVER

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.css.Counter;

public class Server {
  static ArrayList<String> username_list = new ArrayList<>();
  static private List<ClientFunctions> clients = new ArrayList<>();

  private void startServer() {
    try {
      // create on port 1099
      Registry registry = LocateRegistry.createRegistry(1099);

      // create a new service named ServerService
      registry.rebind("ServerService", new ServerFunctionsImpl()); //username_list, clients

    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("system is ready");
  }

  public static void main(String[] args) {
    Server main = new Server();
    main.startServer();
  }

  public static void addToUserNameList(String username){
    username_list.add(username);
  }
  public static boolean isInUserNameList(String username){
    return username_list.contains(username);
  }
  static public synchronized void registerClient(ClientFunctions client) throws RemoteException {
    clients.add(client);
  }

  static public synchronized void unregisterClient(ClientFunctions client) throws RemoteException {
    clients.remove(client);
    System.out.println("removed: "+client);
  }

  public static synchronized void broadcastMessage(String message, ClientFunctions sender) throws RemoteException {
    for (ClientFunctions client : clients) {
      if(!client.getId().equals(sender.getId()) ){
        String sender_name = sender.getId();
        System.out.println("[" + sender_name + "]: " + message);
        client.receiveMessage(false, message, sender_name); 
      }
      
    }
  }

  public static void sendOnlineList(ClientFunctions client)throws RemoteException{
    int size = clients.size();
    ArrayList<String> list_online = new ArrayList<>();
    for(ClientFunctions c: clients){
      list_online.add(c.getId());
    }
    String list_online_string = list_online.toString();
    client.receiveMessage(false, list_online_string, "Online list");
  }

  public static void sendPrivateMessage(String message,ClientFunctions sender,String receiver) throws RemoteException{
    for (ClientFunctions client : clients) {
      if(!client.getId().equals(sender.getId()) && client.getId().equals(receiver)){
        String sender_name = sender.getId();
        System.out.println("[ private mess from " + sender_name + "]: " + message);
        client.receiveMessage(true,message, sender_name);  
      }
      
    }
  }
}
