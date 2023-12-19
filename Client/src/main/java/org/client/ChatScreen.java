package org.client;

import org.client.components.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ChatScreen extends JFrame implements ActionListener {
    public static Color OFFLINE = new Color(232, 70, 70);
    public static Color ONLINE = new Color(70, 232, 70);
    public static Color GROUP = new Color(70, 70, 232);

    public boolean isDownloading = false;
    private String username;
    private JScrollPane scrollPane;
    private JScrollPane messageScrollPane;
    private JScrollPane groupScrollPane;
    private JTextField textField;
    private GridBagConstraints mainGBC;
    private GridBagConstraints userListGBC;
    private GridBagConstraints groupListGBC;
    private GridBagConstraints chatGBC;
    private JPanel userPane;
    private JPanel userList;
    private JPanel groupPane;
    private JPanel groupList;
    private JPanel chatPanel;
    private JPanel messagePanel;
    private Font defaultFont = new Font("SansSerif", Font.PLAIN, 16);
    private Font boldFont = new Font("SansSerif", Font.BOLD, 16);
    private HashMap<String, User> userMap;
    private HashMap<String, Group> groupMap;
    private String currentChatUser;
    private String currentChatGroup;
    private String previousUser;
    private int currentLine = 0;
    private String originalTitle;
    private JButton addGroupBtn;
    public ChatScreen(String username) {
        this.username = username;
        setTitle("ChatApp - " + username);
        originalTitle = this.getTitle();

        userMap = new HashMap<>();
        userList = new JPanel(new GridBagLayout());
        userPane = new JPanel(new GridBagLayout());
        scrollPane = new JScrollPane(userPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userPane.setMinimumSize(new Dimension(100,500));
        scrollPane.setPreferredSize(new Dimension(80,1));
        scrollPane.setMinimumSize(new Dimension(80,1));
        userListGBC = new GridBagConstraints();
        userListGBC.fill = GridBagConstraints.HORIZONTAL;
        userListGBC.gridwidth = 1;
        userListGBC.gridheight = 1;
        userListGBC.weightx = 1.0;
        userListGBC.gridx = 0;
        userPane.add(userList, userListGBC);
        userListGBC.weighty = 1.0;
        userListGBC.fill = GridBagConstraints.BOTH;
        userPane.add(new JLabel(), userListGBC);

//        userListPanel.setPreferredSize(new Dimension(0, 425));
//        userjlist.setPreferredSize(new Dimension(0,0));
//        scrollPane.setMaximumSize(new Dimension(100, 425));

        setLayout(new GridBagLayout());
        mainGBC = new GridBagConstraints();
        mainGBC.insets = new Insets(5, 5, 5, 5);
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        mainGBC.weightx = 0.4;
        mainGBC.weighty = 1.0;
        mainGBC.gridwidth = 2;
        mainGBC.gridheight = 3;
        add(scrollPane, mainGBC);

        groupMap = new HashMap<>();
        groupList = new JPanel(new GridBagLayout());
        groupPane = new JPanel(new GridBagLayout());
        groupScrollPane = new JScrollPane(groupPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupScrollPane.setPreferredSize(new Dimension(1,1));
        groupPane.setMinimumSize(new Dimension(100,500));
        groupListGBC = new GridBagConstraints();
        groupListGBC.fill = GridBagConstraints.HORIZONTAL;
        groupListGBC.gridwidth = 1;
        groupListGBC.gridheight = 1;
        groupListGBC.weightx = 1.0;
        groupListGBC.gridx = 0;
        groupPane.add(groupList, groupListGBC);
        groupListGBC.weighty = 1.0;
        groupListGBC.fill = GridBagConstraints.BOTH;
        groupPane.add(new JLabel(), groupListGBC);

        mainGBC.gridx = 0;
        mainGBC.gridy = 3;
        mainGBC.weightx = 0.4;
        mainGBC.weighty = 1.0;
        mainGBC.gridwidth = 2;
        mainGBC.gridheight = 3;
        add(groupScrollPane, mainGBC);

        groupListGBC.fill = GridBagConstraints.HORIZONTAL;
        groupListGBC.gridwidth = 1;
        groupListGBC.gridheight = 1;
        groupListGBC.weightx = 1.0;
        groupListGBC.gridx = 0;
        addGroupBtn = new JButton("+ Tạo nhóm");
        addGroupBtn.setHorizontalAlignment(SwingConstants.LEFT);
        groupList.add(addGroupBtn, groupListGBC);
        addGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddGroupModal modal = new AddGroupModal();
            }
        });
        // Chat GBC
        JPanel chatPanelContainer = new JPanel(new GridBagLayout());
//        leftChatPanel = new JPanel(new GridBagLayout());
//        JPanel middleChatPanel = new JPanel(new BorderLayout());
//        middleChatPanel.add(new JLabel(), BorderLayout.CENTER);
//        rightChatPanel = new JPanel(new GridBagLayout());
        GridBagConstraints tmpGBC = new GridBagConstraints();

        tmpGBC.gridy = 0;
        tmpGBC.fill = GridBagConstraints.HORIZONTAL;
        chatPanelContainer.add(Box.createRigidArea(new Dimension(0, 10)) , tmpGBC);

        tmpGBC.gridy = 1;
        tmpGBC.gridx = 0;
        tmpGBC.fill = GridBagConstraints.VERTICAL;
        chatPanelContainer.add(Box.createRigidArea(new Dimension(10, 0)) , tmpGBC);
        tmpGBC.gridx = 2;
        chatPanelContainer.add(Box.createRigidArea(new Dimension(10, 0)) , tmpGBC);

        tmpGBC.gridx = 0;
        tmpGBC.gridy = 2;
        tmpGBC.gridwidth = 3;
        tmpGBC.fill = GridBagConstraints.HORIZONTAL;
        chatPanelContainer.add(Box.createRigidArea(new Dimension(0, 10)) , tmpGBC);

        tmpGBC.gridy = 1;
        tmpGBC.gridx = 1;
        tmpGBC.gridwidth = 1;
        tmpGBC.weightx = 1.0;
        tmpGBC.weighty = 1.0;
        tmpGBC.fill = GridBagConstraints.BOTH;
        chatPanel = new JPanel(new GridBagLayout());
        chatPanelContainer.add(chatPanel, tmpGBC);
        messagePanel = new JPanel(new GridBagLayout());
        chatGBC = new GridBagConstraints();
        chatGBC.gridx = 0;
        chatGBC.gridy = 0;
        chatGBC.fill = GridBagConstraints.HORIZONTAL;
        chatGBC.weightx = 1.0;
        chatPanel.add(messagePanel, chatGBC);
        chatGBC.gridx = 0;
        chatGBC.gridy = 1;
        chatGBC.fill = GridBagConstraints.BOTH;
        chatGBC.weighty = 1.0;
        chatPanel.add(new JLabel(), chatGBC);

        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.weightx = 1.0;
        mainGBC.weighty = 1.0;
        mainGBC.gridheight = 6;
        mainGBC.gridwidth = 8;
        mainGBC.gridx = 2;
        mainGBC.gridy = 0;
//        JList<String> msgjlist = new JList<>(messageList);
//        msgjlist.setFont(defaultFont);
        // Text Field
        chatPanelContainer.setMinimumSize(new Dimension(0,500));
        messageScrollPane = new JScrollPane(chatPanelContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageScrollPane.setPreferredSize(new Dimension(1,1));
        add(messageScrollPane, mainGBC);

        mainGBC.gridx = 2;
        mainGBC.gridy = 6;
        mainGBC.gridheight = 1;
        mainGBC.gridwidth = 7;
        mainGBC.weightx = 1.0;
        mainGBC.weighty = 0;
        textField = new JTextField(20);
        textField.setFont(defaultFont);
        textField.addActionListener(this);
        textField.setActionCommand("chat");
        add(textField, mainGBC);
        // Upload file button

        mainGBC.gridx = 9;
        mainGBC.gridy = 6;
        mainGBC.gridheight = 1;
        mainGBC.gridwidth = 1;
        mainGBC.weightx = 0;
        mainGBC.weighty = 0;
        FileButton fileButton = new FileButton();
        add(fileButton, mainGBC);


        mainGBC.gridx = 0;
        mainGBC.gridy = 6;
        mainGBC.gridwidth = 2;
        JButton logoutBtn = new JButton("Đăng xuất");
        add(logoutBtn, mainGBC);

        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.client.sendLine("/logout");
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent we) {
                Main.client.sendLine("/get_users");
                Main.client.sendLine("/get_groups");
                Main.client.sendLine(username);
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.clientThread.isAlive()) {
                    Main.client.sendLine("/quit");
                    Main.client.shutdown();
                } else {
                    System.exit(0);
                }
            }
        });

        setSize(700, 500);
//        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
//        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "chat": {
                if (currentChatUser != null) {
                    String content = textField.getText();
                    Main.client.sendLine("/chat");
                    Main.client.sendLine(currentChatUser);
                    Main.client.sendLine(content);
                    textField.setText("");
                } else if (currentChatGroup != null) {
                    String content = textField.getText();
                    Main.client.sendLine("/group_chat");
                    Main.client.sendLine(currentChatGroup);
                    Main.client.sendLine(content);
                    textField.setText("");
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
    public void updateGroupList(ArrayList<ArrayList<String>> groups) {
        if (!groups.isEmpty()) {
            groupList.removeAll();
            groupListGBC.fill = GridBagConstraints.HORIZONTAL;
            groupListGBC.gridwidth = 1;
            groupListGBC.gridheight = 1;
            groupListGBC.weightx = 1.0;
            groupListGBC.gridx = 0;
            groupList.add(addGroupBtn, groupListGBC);
            for (ArrayList<String> group : groups) {
                groupMap.put(group.get(0), new Group(group));
                groupList.add(groupMap.get(group.get(0)), groupListGBC);
            }
            invalidate();
            validate();
            repaint();
        }
    }

    public void addGroup(ArrayList<String> group) {
        groupListGBC.fill = GridBagConstraints.HORIZONTAL;
        groupListGBC.gridwidth = 1;
        groupListGBC.gridheight = 1;
        groupListGBC.weightx = 1.0;
        groupListGBC.gridx = 0;
        groupMap.put(group.get(0), new Group(group));
        groupList.add(groupMap.get(group.get(0)), groupListGBC);
        invalidate();
        validate();
        repaint();
    }
    private JPanel genNameContainer(String username, boolean left) {
        GridBagConstraints tmpGBC2 = new GridBagConstraints();
        JPanel nameContainer = new JPanel(new GridBagLayout());
        tmpGBC2.gridwidth = 1;

        tmpGBC2.fill = GridBagConstraints.HORIZONTAL;
        tmpGBC2.weightx = 0.3;
        tmpGBC2.gridx = left ? 0 : 1;
        String displayName = username.length() < 26 ? username : username.substring(0, 25) + "...";
        JLabel name = new JLabel(displayName, left ? SwingConstants.LEFT: SwingConstants.RIGHT);
        name.setForeground(new Color(0, 0,0, 125));
        nameContainer.add(name, tmpGBC2);

        tmpGBC2.fill = GridBagConstraints.HORIZONTAL;
        tmpGBC2.weightx = 1.0;
        tmpGBC2.gridx = left ? 1 : 0;
        nameContainer.add(new JLabel(), tmpGBC2);
        return nameContainer;
    }
    private JPanel genMessageContainer(String from, String content, String id, String type, boolean left) {
        GridBagConstraints tmpGBC2 = new GridBagConstraints();
        JPanel messageContainer = new JPanel(new GridBagLayout());
        tmpGBC2.gridwidth = 1;
        tmpGBC2.fill = GridBagConstraints.HORIZONTAL;
        tmpGBC2.weightx = 0;
        tmpGBC2.gridx = left ? 0 : 1;
        Message msg = new Message(from, content, id, type);
        messageContainer.add(msg, tmpGBC2);
        tmpGBC2.weightx = 1.0;
        tmpGBC2.gridx = left ? 1 : 0;
        messageContainer.add(new JLabel(), tmpGBC2);
        return messageContainer;
    }
    public void updateMsgList() {
        updateMsgList(true);
    }
    public void updateMsgList(boolean scrollToBottom) {
        int currentScroll = messageScrollPane.getVerticalScrollBar().getValue();
        clearMessageList();
        ArrayList<ArrayList<String>> chatLog;
        if (currentChatUser != null) {
            chatLog = userMap.get(currentChatUser).getChatLog();
        } else if (currentChatGroup != null) {
            chatLog = groupMap.get(currentChatGroup).getChatLog();
        } else {
            chatLog = new ArrayList<>();
        }
        GridBagConstraints tmpGBC = new GridBagConstraints();
        tmpGBC.gridheight = 1;
        currentLine = 0;
//        System.out.println(currentScroll);
        messagePanel.setVisible(false);
        for (ArrayList<String> log : chatLog) {
            if (!log.get(0).equals(previousUser)) {
                tmpGBC.gridy = currentLine;
                tmpGBC.fill = GridBagConstraints.HORIZONTAL;
                tmpGBC.weightx = 1.0;
                if (!log.get(0).equals(this.username)) {
                    JPanel nameContainer = genNameContainer(log.get(0), true);
                    tmpGBC.gridx = 0;
                    messagePanel.add(nameContainer, tmpGBC);
                    tmpGBC.gridx = 1;
                    messagePanel.add(new JLabel(), tmpGBC);
                }
                else {
                    JPanel nameContainer = genNameContainer(log.get(0), false);
                    tmpGBC.gridx = 0;
                    messagePanel.add(new JLabel(), tmpGBC);
                    tmpGBC.gridx = 1;
                    messagePanel.add(nameContainer, tmpGBC);
                }
                tmpGBC.gridy = ++currentLine;
                tmpGBC.gridx = 0;
                messagePanel.add(Box.createRigidArea(new Dimension(0, 3)), tmpGBC);
                previousUser = log.get(0);
                ++currentLine;
            }
            tmpGBC.gridy = currentLine;
            tmpGBC.fill = GridBagConstraints.HORIZONTAL;
            tmpGBC.weightx = 1.0;
            if (!log.get(0).equals(this.username)) {
                JPanel messageContainer = genMessageContainer(log.get(0), log.get(1), log.get(2), log.get(3), true);
                tmpGBC.gridx = 0;
                messagePanel.add(messageContainer, tmpGBC);
                tmpGBC.gridx = 1;
                messagePanel.add(new JLabel(), tmpGBC);
            }
            else {
                JPanel messageContainer = genMessageContainer(log.get(0), log.get(1), log.get(2), log.get(3),false);
                tmpGBC.gridx = 0;
                messagePanel.add(new JLabel(), tmpGBC);
                tmpGBC.gridx = 1;
                messagePanel.add(messageContainer, tmpGBC);
            }
            tmpGBC.gridy = ++currentLine;
            tmpGBC.gridx = 0;
            messagePanel.add(Box.createRigidArea(new Dimension(0, 3)), tmpGBC);
            ++currentLine;
        }
        messagePanel.setVisible(true);
        invalidate();
        validate();
        repaint();
        if (scrollToBottom) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum() );
            });
        }
        else {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
                vertical.setValue(currentScroll);
            });
        }
    }

    public String getUsername() {
        return username;
    }
    public User getUserComp(String username) {
        return userMap.get(username);
    }
    public Group getGroupComp(String id) {
        return groupMap.get(id);
    }
    public String getOriginalTitle() { return originalTitle; }

    public void addMessage(String from, String content, String id, String type) {
        GridBagConstraints tmpGBC = new GridBagConstraints();
        tmpGBC.gridheight = 1;
        if (!from.equals(previousUser)) {
            tmpGBC.fill = GridBagConstraints.HORIZONTAL;
            tmpGBC.weightx = 1.0;
            tmpGBC.gridy = currentLine;
            if (!from.equals(this.username)) {
                JPanel nameContainer = genNameContainer(from, true);
                tmpGBC.gridx = 0;
                messagePanel.add(nameContainer, tmpGBC);
                tmpGBC.gridx = 1;
                messagePanel.add(new JLabel(), tmpGBC);
            }
            else {
                JPanel nameContainer = genNameContainer(from, false);
                tmpGBC.gridx = 0;
                messagePanel.add(new JLabel(), tmpGBC);
                tmpGBC.gridx = 1;
                messagePanel.add(nameContainer, tmpGBC);
            }
            tmpGBC.gridy = ++currentLine;
            tmpGBC.gridx = 0;
            messagePanel.add(Box.createRigidArea(new Dimension(0, 3)), tmpGBC);
            previousUser = from;
            ++currentLine;
        }
        tmpGBC.gridy = currentLine;
        tmpGBC.fill = GridBagConstraints.HORIZONTAL;
        tmpGBC.weightx = 1.0;
        if (!from.equals(this.username)) {
            JPanel messageContainer = genMessageContainer(from, content, id, type, true);
            tmpGBC.gridx = 0;
            messagePanel.add(messageContainer, tmpGBC);
            tmpGBC.gridx = 1;
            messagePanel.add(new JLabel(), tmpGBC);
        }
        else {
            JPanel messageContainer = genMessageContainer(from, content, id, type, false);
            tmpGBC.gridx = 0;
            messagePanel.add(new JLabel(), tmpGBC);
            tmpGBC.gridx = 1;
            messagePanel.add(messageContainer, tmpGBC);
        }
        tmpGBC.gridy = ++currentLine;
        tmpGBC.gridx = 0;
        messagePanel.add(Box.createRigidArea(new Dimension(0, 3)), tmpGBC);
        ++currentLine;
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
            vertical.setValue( vertical.getMaximum() );
        });
        invalidate();
        validate();
        repaint();
    }
    public void clearMessageList() {
        previousUser = null;
        messagePanel.removeAll();
        invalidate();
        validate();
        repaint();
    }

//    public void addChatLog(ArrayList<ArrayList<String>> chatLog) {
//        for (ArrayList<String> log : chatLog) {
//            this.addMessage(log.get(0), log.get(1));
//        }
//    }
    public void activateUser(String user) {
        userMap.get(user).setBackground(ChatScreen.ONLINE);
    }
    public void deactivateUser(String user) {
        userMap.get(user).setBackground(ChatScreen.OFFLINE);
    }
    public String getCurrentChatUser() {
        return currentChatUser;
    }
    public void setCurrentChatUser(String username) {
        currentChatUser = username;
    }
    public String getCurrentChatGroup() {
        return currentChatGroup;
    }
    public void setCurrentChatGroup(String id) {
        currentChatGroup = id;
    }
    public ArrayList<String> getUserListExceptSelf() {
        ArrayList<String> users = new ArrayList<>(userMap.keySet());
        users.remove(this.username);
        return users;
    }
}
