//SERVER
package com.klassen;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SealedObject;

public class ServerFunctionsImpl extends UnicastRemoteObject implements ServerFunctions {

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


  @Override
  public void unregisterClient(String client) throws RemoteException{
    Server.unregisterClient(client);
  }
  @Override
  public void bulletinBoard_add(int boardidx, int i, byte[] v, String tag){
    Server.bulletinBoards.get(boardidx).add(i, v, tag);
  }

  @Override
  public byte[] bulletinBoard_get(int boardidx, int idx, String b){
    byte[] m = Server.bulletinBoards.get(boardidx).get(idx,b);
    return m;
  }

  @Override
  public int bulletinBoardGetSize(){
    return BulletinBoard.get_size();
  }

  @Override
  public int getNumberOfBoards(){
    return Server.bulletinBoards.size();
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