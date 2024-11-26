//SERVER
package com.klassen;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SealedObject;

public class ServerFunctionsImpl extends UnicastRemoteObject implements ServerFunctions {
  //private List<String> username_list;
  //private List<ClientFunctions> clients;

  public ServerFunctionsImpl() throws RemoteException { //List<String> username_list, List<ClientFunctions> clients
    //this.username_list = username_list; // referentie naar de username list die de server heeft
    //this.clients = clients;
  }

  @Override
  public String check_and_add_username(String username) {
    if (Server.isInUserNameList(username)) {
      return "Username already in use.";
    } else {
      try{
        Server.registerClient(username);
        return "Username registered.";
      } catch(Exception e){
        e.printStackTrace();
      }
      return "Problem registering user";
    }
  }

  @Override
  public boolean isUser(String username) throws RemoteException{
    return(Server.isInUserNameList(username));
  }





  // @Override
  // public void sendMessage(String message, ClientFunctions sender) { // broadcast
  //   try {
  //     Server.broadcastMessage(message, sender); // Broadcast message to all clients
  //   } catch (RemoteException e) {
  //     e.printStackTrace();
  //   }
  // }

  // @Override
  // public synchronized void registerClient(ClientFunctions client) throws RemoteException {
  //   Server.registerClient(client);
  // }

  // @Override
  // public void unregisterClient(ClientFunctions client) throws RemoteException{
  //   Server.unregisterClient(client);
  // }

  @Override
  public void unregisterClient(String client) throws RemoteException{
    Server.unregisterClient(client);
  }
  @Override
  public void bulletinBoard_add(int i, SealedObject v, String tag){
    Server.bulletinBoard.add(i, v, tag);
  }

  @Override
  public SealedObject bulletinBoard_get(int i, String b){
    SealedObject m = Server.bulletinBoard.get(i,b);
    return m;
  }

  @Override
  public int bulletinBoardGetSize(){
    return BulletinBoard.get_size();
  }

  // @Override
  // public void sendOnlineList(ClientFunctions client) throws RemoteException{
  //   Server.sendOnlineList(client);
  // }

  // @Override
  // public void sendPrivateMessage(String message,ClientFunctions clientImpl,String receiver)throws RemoteException{
  //   Server.sendPrivateMessage(message, clientImpl, receiver);
  // }
}