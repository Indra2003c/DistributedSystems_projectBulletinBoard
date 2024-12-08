package com.klassen;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Gui extends JFrame {
    private JList<String> chatList;
    private DefaultListModel<String> chatListModel;
    private static JPanel chatAreaPanel;
    private static JScrollPane chatScrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private JButton addChatButton;
    private JButton reloadButton;
    JLabel chatNameLabel;
    private static Map<String, JPanel> chatPanels; // opslaan panels voor elke chat
    private static String currentChat;
    private String username;
    private Client client;

    private Timer reloadTimer;

    public Gui(Client client) {
        super("Chat Application - " + client.username);
        this.username = client.username;
        this.client = client;

        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setFixedCellHeight(50);
        chatList.setBackground(new Color(17, 27, 33));
        chatList.setSelectionBackground(new Color(42, 57, 66));
        chatList.setSelectionForeground(Color.WHITE);
        chatList.setForeground(Color.WHITE);
        chatList.setFont(new Font("Arial", Font.PLAIN, 16));
        chatList.addListSelectionListener(e -> switchChat(chatList.getSelectedValue()));

        chatAreaPanel = new JPanel();
        chatAreaPanel.setLayout(new BoxLayout(chatAreaPanel, BoxLayout.Y_AXIS));
        chatAreaPanel.setBackground(new Color(7, 24, 32));

        chatScrollPane = new JScrollPane(chatAreaPanel);
        chatScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        inputField = new JTextField();
        inputField.setBackground(new Color(45, 56, 62));
        inputField.setFont(new Font("Arial", Font.PLAIN, 18));
        inputField.setForeground(Color.WHITE);
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 176, 95));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 18));
        sendButton.addActionListener(e -> sendMessage());

        addChatButton = new JButton("New chat");
        addChatButton.setFont(new Font("Arial", Font.BOLD, 18));
        addChatButton.setBackground(new Color(0, 176, 95));
        addChatButton.setForeground(Color.WHITE);
        addChatButton.addActionListener(e -> addChat());
        
        reloadButton = new JButton("");
        reloadButton.setIcon(new ImageIcon("data\\icons-refresh.png"));
        reloadButton.setFont(new Font("Arial", Font.BOLD, 18));
        reloadButton.setBackground(new Color(0, 176, 95));
        reloadButton.setForeground(Color.WHITE);
        reloadButton.addActionListener(e -> reloadFunction());

        chatPanels = new HashMap<>();
        //currentChat = ""; //Group Chat
        //chatPanels.put(currentChat, new JPanel());
        //chatPanels.get(currentChat).setLayout(new BoxLayout(chatPanels.get(currentChat), BoxLayout.Y_AXIS));
        //chatPanels.get(currentChat).setBackground(new Color(7, 24, 32));
        //chatListModel.addElement(currentChat);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(new Color(17, 27, 33));
        leftPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
        leftPanel.add(addChatButton, BorderLayout.SOUTH);
        //leftPanel.add(reloadButton, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        JPanel chatHeaderPanel = new JPanel();
        chatHeaderPanel.setLayout(new BorderLayout());

        
        chatNameLabel = new JLabel(currentChat);
        chatHeaderPanel.setBackground(new Color(17, 27, 33));
        chatNameLabel.setBackground(new Color(17, 27, 33));
        chatNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        chatNameLabel.setForeground(Color.WHITE);
        chatHeaderPanel.add(chatNameLabel, BorderLayout.WEST);
        chatHeaderPanel.add(reloadButton, BorderLayout.EAST);

        
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Timer voor auto reload elke 2 sec
        reloadTimer = new Timer(2000, e -> reloadFunction()); // 2000 milliseconds = 2 seconds
        reloadTimer.start();

        add(leftPanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        Gui.this,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    client.shutdown(); // shutdown functie oproepen voor afsluiten
                    System.exit(0);
                }
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        // if (currentChat.equals("Group Chat")) {
        //     addMessageToChat(currentChat, "(me): " + message, true);
        //     client.messageInput(message, null);
        // } else {
            addMessageToChat(currentChat, message, true);
            client.messageInput(message, currentChat);
        //}

        inputField.setText("");
    }

    private void addChat() {
        String chatName = JOptionPane.showInputDialog(this, "Enter username for private chat:");
        if (chatName != null && !chatName.trim().isEmpty() && !chatPanels.containsKey(chatName)) {
            if (Client.userInUse(chatName)) {
                JPanel newChatPanel = new JPanel();
                newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
                newChatPanel.setBackground(new Color(17, 27, 33));

                


                String [] security_information_client = client.generate_initial_security_information_for_connection(chatName); //we generaten de initiële secret key voor de chat A (sender) --> B (wanneer we hetzelfde doen voor B, genereren we de secret key voor de chat B --> A)

                String[] security_information_other_party = add_security_information(chatName);
                if(security_information_other_party != null){
                    chatPanels.put(chatName, newChatPanel);
                    chatListModel.addElement(chatName);
                    client.set_up_connection(security_information_client, security_information_other_party, chatName);

                }
            } else {
                JOptionPane.showMessageDialog(null, "Unkown user", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private String[] add_security_information(String chatName){
        String[] ret = new String[3];
        boolean validInput = false;

        while (!validInput) {
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

            //input secret key
            JLabel secret_key_Label = new JLabel("Enter the secret key of " + chatName);
            JTextField secret_key_Field = new JTextField(20);
            inputPanel.add(secret_key_Label);
            inputPanel.add(secret_key_Field);

            //input idx
            JLabel idx_Label = new JLabel("Enter the idx of " + chatName);
            JTextField idx_Field = new JTextField(20);
            inputPanel.add(idx_Label);
            inputPanel.add(idx_Field);

            //input tag
            JLabel tag_Label = new JLabel("Enter the tag of " + chatName);
            JTextField tag_Field = new JTextField(20);
            inputPanel.add(tag_Label);
            inputPanel.add(tag_Field);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Private Chat", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return null; // Cancelled, return null to stop the process
            }

            if (result == JOptionPane.OK_OPTION) {
                String sKey = secret_key_Field.getText().trim();
                String idx = idx_Field.getText().trim();
                String tag = tag_Field.getText().trim();
    
                // Controleer of de invoervelden geldig zijn
                if(sKey.isEmpty() || idx.isEmpty() || tag.isEmpty()){
                    JOptionPane.showMessageDialog(this, "Fill in all the security details!", "Error", JOptionPane.ERROR_MESSAGE);  
                }else{
                    ret[0] = sKey;
                    ret[1] = idx;
                    ret[2] = tag;
                    validInput = true;
                }
            }
        }
        return ret;
    }

    private void reloadFunction() {
        //System.out.println("Reloading chat...");

        client.reload();
    }

    private void switchChat(String chatName) {
        if (chatName == null) {
            return;
        }
        currentChat = chatName;
        chatNameLabel.setText(currentChat);

        chatAreaPanel.removeAll();
        chatAreaPanel.add(chatPanels.get(chatName));
        chatAreaPanel.revalidate();
        chatAreaPanel.repaint();
    }

    public static void addMessageToChat(String chatName, String message, boolean isSender) {
        JPanel chatPanel = chatPanels.get(chatName);

        JLabel messageLabel = new JLabel("<html><p style='width: 200px;'>" + message + "</p></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (isSender) {
            messageLabel.setBackground(new Color(5, 70, 64));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        } else {
            messageLabel.setBackground(new Color(33, 46, 54));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        }

        chatPanel.add(messageLabel);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Scroll to bottom
        if (chatName.equals(currentChat)) {
            chatAreaPanel.add(chatPanel);
            chatAreaPanel.revalidate();
            chatAreaPanel.repaint();
            JScrollBar verticalScrollBar = chatScrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        }

    }

}
