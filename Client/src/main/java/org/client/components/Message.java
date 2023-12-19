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
    public String type;
    public String align;
    public ArrayList<ArrayList<String>> chatLog;
    public Message(String username, String content, String id, String type) {
        this.chatLog = new ArrayList<>();
        this.username = username;
        this.id = id;
        this.content = content;
        this.type = type;
        color = Color.WHITE;
        colorOver = Color.WHITE;
        colorClick = Color.GRAY;
        borderColor = Color.WHITE;

        setBackground(color);
        setBorder(BorderFactory.createMatteBorder(8, 16, 10, 16, Color.WHITE));
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        String[] split;
        if ((type.equals("file") || type.equals("group_file")) && (split = content.split("\\|", 2)).length == 2) {
            String serverFileName = split[0];
            String orgFilename = split[1];
            String wrapped = WordUtils.wrap(orgFilename, 30, "\n", true);
            this.setIcon(fetchIcon());
            this.setText("<html>" + wrapped.replaceAll("\\n", "<br>") + "</html>");
            addActionListener(new ActionListener() {
                final Object[] options = {"Có", "Không"};
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (username.equals(Main.chatScreen.getUsername())) {
                        int input = JOptionPane.showOptionDialog(Main.chatScreen.getContentPane(), "Bạn có muốn xoá tin nhắn này?", "Xoá tin nhắn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                        if (input == 0) {
                            if (type.equals("group_file") && Main.chatScreen.getCurrentChatGroup() != null) {
                                Main.client.sendLine("/remove_group_file");
                                Main.client.sendLine(id);
                                Main.client.sendLine(Main.chatScreen.getCurrentChatGroup());
                            } else {
                                Main.client.sendLine("/remove_file");
                                Main.client.sendLine(id);
                                Main.client.sendLine(Main.chatScreen.getCurrentChatUser());
                            }
                        }
                    }
                    else {
                        if (!Main.chatScreen.isDownloading) {
                            Main.chatScreen.isDownloading = true;
                            Main.client.sendLine("/download_file");
                            Main.client.sendLine(serverFileName);
                            Main.client.sendLine(orgFilename);
                        }
                    }
                }
            });
        } else {
            String wrapped = WordUtils.wrap(content, 30, "\n", true);

            this.setText("<html>" + wrapped.replaceAll("\\n", "<br>") + "</html>");
            addActionListener(new ActionListener() {
                final Object[] options = {"Có", "Không"};
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (username.equals(Main.chatScreen.getUsername())) {
                        int input = JOptionPane.showOptionDialog(Main.chatScreen.getContentPane(), "Bạn có muốn xoá tin nhắn này?", "Xoá tin nhắn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                        if (input == 0) {
                            if (type.equals("group_text")) {
                                Main.client.sendLine("/remove_group_message");
                                Main.client.sendLine(id);
                                Main.client.sendLine(Main.chatScreen.getCurrentChatGroup());
                            }
                            else {
                                Main.client.sendLine("/remove_message");
                                Main.client.sendLine(id);
                                Main.client.sendLine(Main.chatScreen.getCurrentChatUser());
                            }
                        }
                    }
                }
            });
        }

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
    public ImageIcon fetchIcon() {
        return new ImageIcon(Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/download.png"))) {
            @Override
            public int getIconWidth() {
                return 24;
            }
            @Override
            public int getIconHeight() {
                return 24;
            }
            @Override
            public synchronized void paintIcon(Component c, Graphics g,
                                               int x, int y) {
                g.drawImage(getImage(), x, y, 24, 24, null);
            }
        };
    }
}
