//SERVER
package com.klassen;

import java.rmi.Remote; 
import java.rmi.RemoteException;

import javax.crypto.SealedObject; 

public interface ServerFunctions extends Remote {  
	String check_and_add_username(String username) throws RemoteException;
	void unregisterClient(String client) throws RemoteException;
	boolean isUser(String username) throws RemoteException;

	void bulletinBoard_add(int i, byte[] v, String tag) throws RemoteException;
    byte[] bulletinBoard_get(int idx, String b) throws RemoteException;
	int bulletinBoardGetSize()throws RemoteException;
	// void sendMessage(String message, ClientFunctions sender) throws RemoteException;

	// void registerClient(ClientFunctions client) throws RemoteException;
	// void unregisterClient(ClientFunctions client) throws RemoteException;

	// void sendOnlineList(ClientFunctions client) throws RemoteException;
	// void sendPrivateMessage(String message,ClientFunctions clientImpl,String receiver) throws RemoteException;
} 