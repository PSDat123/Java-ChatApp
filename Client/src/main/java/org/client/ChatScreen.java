package org.client;

import org.client.components.User;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ChatScreen extends JFrame implements ActionListener {
    private String username;
    private JScrollPane scrollPane;
    private JScrollPane messageScrollPane;
//    private JPanel userList;
//    private JPanel mainPanel;
    private JTextField textField;
    private GridBagConstraints mainGBC;
    private GridBagConstraints userListGBC;
    private DefaultListModel<String> messageList;
    private DefaultListModel<User> userListModel;
    private JPanel userPane;
    private JPanel userList;
    private Font defaultFont = new Font("SansSerif", Font.PLAIN, 16);
    private Font boldFont = new Font("SansSerif", Font.BOLD, 16);
    private HashMap<String, User> userMap;
    private String currentChatUser;
    public ChatScreen() {
        setTitle("Ứng dụng chat - 21127243");
        userMap = new HashMap<>();
        userList = new JPanel(new GridBagLayout());
        userPane = new JPanel(new GridBagLayout());
        scrollPane = new JScrollPane(userPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userPane.setMinimumSize(new Dimension(0,425));

        userListGBC = new GridBagConstraints();
        userListGBC.fill = GridBagConstraints.HORIZONTAL;
        userListGBC.gridwidth = 1;
        userListGBC.gridheight = 1;
        userListGBC.weightx = 1.0;
        userListGBC.gridx = 0;
        userPane.add(userList, userListGBC);
        userListGBC.weighty = 1.0;
        userListGBC.fill = GridBagConstraints.VERTICAL;
        userPane.add(new JLabel(), userListGBC);

//        userListPanel.setPreferredSize(new Dimension(0, 425));
//        userjlist.setPreferredSize(new Dimension(0,0));
        scrollPane.setPreferredSize(new Dimension(100, 0));
        scrollPane.setMaximumSize(new Dimension(100, 0));
        setLayout(new GridBagLayout());
        mainGBC = new GridBagConstraints();
        mainGBC.insets = new Insets(5, 5, 5, 5);
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        mainGBC.weightx = 0.3;
        mainGBC.weighty = 1.0;
        mainGBC.gridwidth = 2;
        mainGBC.gridheight = 5;
        add(scrollPane, mainGBC);

        mainGBC.weightx = 1.0;
        mainGBC.weighty = 1.0;
        mainGBC.gridheight = 4;
        mainGBC.gridwidth = 8;
        mainGBC.gridx = 2;
        mainGBC.gridy = 0;
        messageList = new DefaultListModel<>();
        JList<String> msgjlist = new JList<>(messageList);
        msgjlist.setFont(defaultFont);
        msgjlist.setMinimumSize(new Dimension(0,425));
        messageScrollPane = new JScrollPane(msgjlist, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(messageScrollPane, mainGBC);
        mainGBC.gridx = 2;
        mainGBC.gridy = 4;
        mainGBC.gridheight = 1;
        mainGBC.gridwidth = 8;
        mainGBC.weightx = 1.0;
        mainGBC.weighty = 0;
        textField = new JTextField(20);
        textField.setFont(defaultFont);
        textField.addActionListener(this);
        textField.setActionCommand("chat");
        add(textField, mainGBC);



        setSize(700, 425);
//        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
//        pack();
        setVisible(true);

//        userjlist.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                if (!e.getValueIsAdjusting()) {
//                    System.out.println(userjlist.getSelectedValue());
//                }
//            }
//        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                Main.client.sendLine("/get_users");
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.clientThread.isAlive()) {
                    Main.client.sendLine("/quit");
                }
                System.exit(0);
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "chat": {
                if (currentChatUser != null) {
                    String content = textField.getText();
                    userMap.get(currentChatUser).addToChatLog(username, content);
                    Main.client.sendLine("/chat");
                    Main.client.sendLine(currentChatUser);
                    Main.client.sendLine(content);
                }
                break;
            }
        }
    }

    public void updateUserList(ArrayList<String> users) {
        if (!users.isEmpty()) {
            userList.removeAll();
            userListGBC.fill = GridBagConstraints.HORIZONTAL;
            userListGBC.gridwidth = 1;
            userListGBC.gridheight = 1;
            userListGBC.weightx = 1.0;
            userListGBC.gridx = 0;
            for (String user : users) {
                userMap.put(user, new User(user));
                userList.add(userMap.get(user), userListGBC);
            }
            invalidate();
            validate();
            repaint();
        }
    }

    public void addUser(String user) {
        userListGBC.fill = GridBagConstraints.HORIZONTAL;
        userListGBC.gridwidth = 1;
        userListGBC.gridheight = 1;
        userListGBC.weightx = 1.0;
        userListGBC.gridx = 0;
        userMap.put(user, new User(user));
        userList.add(userMap.get(user), userListGBC);
        invalidate();
        validate();
        repaint();
    }
    public void updateMsgList() {
        clearMessageList();
        ArrayList<ArrayList<String>> chatLog = userMap.get(currentChatUser).getChatLog();
        ArrayList<String> lines = new ArrayList<>();
        for (ArrayList<String> log : chatLog) {
            lines.add(String.format("%s: %s", log.get(0), log.get(1)));
        }
        messageList.addAll(lines);
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
            vertical.setValue( vertical.getMaximum() );
        });
    }

    public void setUsername(String username) {
        this.username = username;
        this.setTitle("ChatApp - " + username);
    }
    public String getUsername() {
        return username;
    }
    public User getUserComp(String username) {
        return userMap.get(username);
    }
    public void addMessage(String from, String content) {
        messageList.addElement(String.format("%s: %s", from ,content));
        JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }
    public void clearMessageList() {
        messageList.clear();
    }

//    public void addChatLog(ArrayList<ArrayList<String>> chatLog) {
//        for (ArrayList<String> log : chatLog) {
//            this.addMessage(log.get(0), log.get(1));
//        }
//    }
    public void activateUser(String user) {

    }
    public String getCurrentChatUser() {
        return currentChatUser;
    }
    public void setCurrentChatUser(String username) {
        currentChatUser = username;
    }


}
