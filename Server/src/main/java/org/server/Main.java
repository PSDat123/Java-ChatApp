package org.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {
    private Font boldFont = new Font("SansSerif", Font.BOLD, 20);
    private boolean isRunning = false;
    private Server server;
    private Thread t;
    Main() {
        setTitle("ChatApp Server - 21127243");
//        JPanel innerPanel = new JPanel();
        JButton listenBtn = new JButton("Start listening");
        JLabel notification = new JLabel();
        notification.setAlignmentX(Component.CENTER_ALIGNMENT);
        listenBtn.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        gbc.gridy = 1;
        JLabel header = new JLabel("ChatApp Server");
        header.setFont(boldFont);
        add(header, gbc);
        gbc.gridy = 3;
        add(listenBtn, gbc);
        gbc.gridy = 4;
        add(Box.createRigidArea(new Dimension(0, 10)), gbc);
        gbc.gridy = 5;
        add(notification, gbc);

        gbc.gridy = 0;
        add(Box.createRigidArea(new Dimension(0, 10)), gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        add(new JLabel(), gbc);
        gbc.gridy = 6;
        add(new JLabel(), gbc);

        listenBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning) {
                    try {
                        Database.init();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    isRunning = true;
                    server = new Server(8080);
                    t = new Thread(server);
                    t.start();
                    notification.setForeground(Color.GREEN);
                    notification.setText("Server listening on port 8080!");
                    listenBtn.setText("Stop listening");
                } else {
                    try {
                        Database.shutdown();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    isRunning = false;
                    server.shutdown();
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    notification.setForeground(Color.RED);
                    notification.setText("Server stopped!");
                    listenBtn.setText("Start listening");
                }

            }
        });
        setSize(new Dimension(300, 200));
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

    }
    public static void main(String[] args) {
        new Main();
    }
}
