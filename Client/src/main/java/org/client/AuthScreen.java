package org.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Objects;

public class AuthScreen extends JFrame implements ActionListener {
    private JTabbedPane tabbedPane;
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginBtn;
    private JLabel loginAlert;
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JButton registerBtn;
    private JLabel registerAlert;
    public AuthScreen() {
        setTitle("Ứng dụng chat - 21127243");
        tabbedPane = new JTabbedPane();
        JPanel loginPanel = new JPanel();
        JPanel registerPanel = new JPanel();
        // Login panel
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        JLabel loginUsernameLabel = new JLabel("Tên đăng nhập");
        JLabel loginPasswordLabel = new JLabel("Mật khẩu");
        loginUsernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginUsernameField = new JTextField(15);
        loginPasswordField = new JPasswordField(15);
        loginUsernameField.setMaximumSize(loginUsernameField.getPreferredSize());
        loginPasswordField.setMaximumSize(loginPasswordField.getPreferredSize());

        loginPanel.add(Box.createRigidArea(new Dimension(0, 80)));
        loginPanel.add(loginUsernameLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        loginPanel.add(loginUsernameField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginPanel.add(loginPasswordLabel);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        loginPanel.add(loginPasswordField);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginBtn = new JButton("Đăng nhập");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(this);
        loginBtn.setActionCommand("login");
        loginPanel.add(loginBtn);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        loginAlert = new JLabel();
        loginAlert.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(loginAlert);
        tabbedPane.add("Đăng nhập", loginPanel);

        // Register Panel
        registerPanel.setLayout(new BoxLayout(registerPanel, BoxLayout.Y_AXIS));
        JLabel registerUsernameLabel = new JLabel("Tên đăng nhập");
        JLabel registerPasswordLabel = new JLabel("Mật khẩu");
        registerUsernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerUsernameField = new JTextField(15);
        registerPasswordField = new JPasswordField(15);
        registerUsernameField.setMaximumSize(registerUsernameField.getPreferredSize());
        registerPasswordField.setMaximumSize(registerPasswordField.getPreferredSize());

        registerPanel.add(Box.createRigidArea(new Dimension(0, 80)));
        registerPanel.add(registerUsernameLabel);
        registerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        registerPanel.add(registerUsernameField);
        registerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        registerPanel.add(registerPasswordLabel);
        registerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        registerPanel.add(registerPasswordField);
        registerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        registerBtn = new JButton("Đăng kí");
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(this);
        registerBtn.setActionCommand("register");
        registerPanel.add(registerBtn);
        registerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        registerAlert = new JLabel();
        registerAlert.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerPanel.add(registerAlert);
        tabbedPane.add("Đăng kí", registerPanel);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.clientThread.isAlive()) {
                    Main.client.sendLine("/quit");
                }
                System.exit(0);
            }
        });

        setLayout(new BorderLayout(20, 15));
        add(tabbedPane, BorderLayout.CENTER);
        setSize(400, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
//        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "login": {
                String username = loginUsernameField.getText();
                String password = new String(loginPasswordField.getPassword());
                Main.client.sendLine("/login");
                Main.client.sendLine(username);
                Main.client.sendLine(password);
                break;
            }
            case "register": {
                String username = registerUsernameField.getText();
                String password = new String(registerPasswordField.getPassword());
                Main.client.sendLine("/register");
                Main.client.sendLine(username);
                Main.client.sendLine(password);
                break;
            }
        }
    }

    public void setRegisterAlert(String msg, String type) {
        if (type.equals("success")) {
            registerAlert.setForeground(Color.GREEN);
        }
        else {
            registerAlert.setForeground(Color.RED);
        }
        registerAlert.setText(msg);
    }
    public void setLoginAlert(String msg, String type) {
        if (type.equals("success")) {
            loginAlert.setForeground(Color.GREEN);
        }
        else {
            loginAlert.setForeground(Color.RED);
        }
        loginAlert.setText(msg);
    }
}
