package com.klassen;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        currentChat = "Group Chat";
        chatPanels.put(currentChat, new JPanel());
        chatPanels.get(currentChat).setLayout(new BoxLayout(chatPanels.get(currentChat), BoxLayout.Y_AXIS));
        chatPanels.get(currentChat).setBackground(new Color(7, 24, 32));
        chatListModel.addElement(currentChat);

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

        
        JLabel chatNameLabel = new JLabel(currentChat);
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
        if (currentChat.equals("Group Chat")) {
            addMessageToChat(currentChat, "(me): " + message, true);
            client.messageInput(message, null);
        } else {
            addMessageToChat(currentChat, message, true);
            client.messageInput(message, currentChat);
        }

        inputField.setText("");
    }

    private void addChat() {
        String chatName = JOptionPane.showInputDialog(this, "Enter username for private chat:");
        if (chatName != null && !chatName.trim().isEmpty() && !chatPanels.containsKey(chatName)) {
            if (Client.userInUse(chatName)) {
                JPanel newChatPanel = new JPanel();
                newChatPanel.setLayout(new BoxLayout(newChatPanel, BoxLayout.Y_AXIS));
                newChatPanel.setBackground(new Color(17, 27, 33));

                chatPanels.put(chatName, newChatPanel);
                chatListModel.addElement(chatName);
            } else {
                JOptionPane.showMessageDialog(null, "Unkown user", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private void reloadFunction() {
        System.out.println("Reloading chat...");

        client.reload();
    }

    private void switchChat(String chatName) {
        if (chatName == null) {
            return;
        }
        currentChat = chatName;
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
