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
  //public static BulletinBoard bulletinBoard;
  static ArrayList<BulletinBoard> bulletinBoards = new ArrayList<>();
  public static int NUMBER_BULLETINBOARDS = 3;
  private Server(){

    for(int i = 0; i< NUMBER_BULLETINBOARDS; i++){
      bulletinBoards.add(new BulletinBoard());
    }
    


  }
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

  public static boolean isInUserNameList(String username){
    return username_list.contains(username);
  }
  static public synchronized void registerClient(String client) throws RemoteException {  //ClientFunctions
    username_list.add(client);   //clients.add(client);
  }

  static public synchronized void unregisterClient(String client) throws RemoteException { //ClientFunctions
    username_list.remove(client);
    System.out.println("removed: "+client);
  }  
}
