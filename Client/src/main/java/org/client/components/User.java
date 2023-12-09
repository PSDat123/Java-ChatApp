package org.client.components;

import org.client.ChatScreen;
import org.client.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class User extends JButton {
    public String username;
    public ArrayList<ArrayList<String>> chatLog;
    public User(String username) {
        super(username);
        this.chatLog = new ArrayList<>();
        this.username = username;
        this.setContentAreaFilled(false);
        this.setBackground(ChatScreen.OFFLINE);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.chatScreen.setCurrentChatUser(username);
                Main.chatScreen.clearMessageList();
                if (chatLog.isEmpty()) {
                    Main.client.sendLine("/get_chat_log_from");
                    Main.client.sendLine(username);
                }
                else {
                    Main.chatScreen.updateMsgList();
                }
//                Main.chatScreen.clearMessageList();
//                Main.chatScreen.addChatLog(chatLog);

            }
        });
    }

    public void addToChatLog(String from, String content) {
        chatLog.add(new ArrayList<>(Arrays.asList(from, content)));
    }
    public ArrayList<ArrayList<String>> getChatLog() {
        return chatLog;
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(
                new Point(0, 0),
                Color.WHITE,
                new Point(0, getHeight()),
                getBackground()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }
}
