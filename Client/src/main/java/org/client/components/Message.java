package org.client.components;

import org.apache.commons.text.WordUtils;
import org.client.ChatScreen;
import org.client.Main;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Message extends JButton {
    private int radius = 15;
    private boolean over;
    private Color color;
    private Color colorOver;
    private Color colorClick;
    private Color borderColor;
    public String username;
    public String id;
    public String content;
    public ArrayList<ArrayList<String>> chatLog;
    public Message(String username, String content, String id) {
        this.chatLog = new ArrayList<>();
        this.username = username;
        this.content = content;
        this.id = id;
        color = Color.WHITE;
        colorOver = Color.WHITE;
        colorClick = Color.GRAY;
        borderColor = Color.WHITE;
        setBackground(color);
        String wrapped = WordUtils.wrap(content, 30, "\n", true);
        this.setText("<html>" + wrapped.replaceAll("\\n", "<br>") + "</html>");
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);

//        this.setBackground(ChatScreen.OFFLINE);
        addActionListener(new ActionListener() {
            final Object[] options = {"Có", "Không"};
            @Override
            public void actionPerformed(ActionEvent e) {
                if (username.equals(Main.chatScreen.getUsername())) {
                    int input = JOptionPane.showOptionDialog(Main.chatScreen.getContentPane(), "Bạn có muốn xoá tin nhắn này?", "Xoá tin nhắn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                    if (input == 0) {
                        Main.client.sendLine("/remove_message");
                        Main.client.sendLine(id);
                        Main.client.sendLine(Main.chatScreen.getCurrentChatUser());
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent me) {
                setBackground(colorOver);
                over = true;
            }

            @Override
            public void mouseExited(MouseEvent me) {
                setBackground(color);
                over = false;

            }

            @Override
            public void mousePressed(MouseEvent me) {
                setBackground(colorClick);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (over) {
                    setBackground(colorOver);
                } else {
                    setBackground(color);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //  Paint Border
        g2.setColor(borderColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.setColor(getBackground());
        //  Border set 2 Pix
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

//        g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);
        super.paintComponent(grphcs);
    }
}
