package com.klassen;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface ClientFunctions extends Remote {
    // void receiveMessage(boolean isPrivate, String message, String sender) throws RemoteException;
    String getId() throws RemoteException;
}
