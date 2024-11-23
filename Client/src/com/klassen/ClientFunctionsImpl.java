// package com.klassen;

// import java.rmi.RemoteException;
// import java.rmi.server.UnicastRemoteObject;
// import java.util.ArrayList;
// import java.util.List;

// import javax.swing.SwingUtilities;

// public class ClientFunctionsImpl extends UnicastRemoteObject implements ClientFunctions{
//     private final String id;
    

//     public ClientFunctionsImpl(String id) throws RemoteException {
//         this.id = id;
//     }

//     // @Override
//     // public void receiveMessage(boolean isPrivate, String message, String sender) {
//     //     System.out.println("[" + sender + "]: " + message);
//     //     Client.showReceivedMessage(isPrivate,sender,message);
//     // }

//     @Override
//     public String getId() {
//         return id;
//     }


// }
