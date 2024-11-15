package com.klassen;

import java.rmi.RemoteException;
//CLIENT
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.*;

public class Client {
  static ServerFunctions impl;
  ClientFunctionsImpl clientImpl;
  String username;
  Registry registry;
  static Gui gui;

  public Client() {

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
      impl.registerClient(clientImpl);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("client initialize failed");
    }
  }

  public void shutdown() {
    try {
        if (impl != null && clientImpl != null) {
            impl.unregisterClient(clientImpl);
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
      if (receiver == null) {
        impl.sendMessage(message, clientImpl);
      } else {
        impl.sendPrivateMessage(message, clientImpl, receiver);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static void showReceivedMessage(Boolean isPrivate, String sender, String message) {
    if (!isPrivate) {
      gui.addMessageToChat("Group Chat", "[" + sender + "]: " + message, false);
    } else {
      gui.addMessageToChat(sender, message, false);
    }
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