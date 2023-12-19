package org.client.components;

import org.client.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class AddGroupModal extends JDialog {
    private JButton submitBtn;
    private JList<String> userList;
    private JTextField nameField;
    public AddGroupModal() {
        super(Main.chatScreen, true);
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        JPanel labelPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html>Hãy chọn thành viên để thêm vào nhóm<br><br>(Giữ shift/ctrl để chọn nhiều thành viên)</html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        labelPanel.add(label, BorderLayout.CENTER);
        labelPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
        labelPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.NORTH);
        labelPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.WEST);
        labelPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);

        innerPanel.add(labelPanel, BorderLayout.NORTH);
        DefaultListModel<String> userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userList);
        userList.setMinimumSize(new Dimension(0, 0));

        innerPanel.add(scrollPane, BorderLayout.CENTER);
        innerPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.WEST);
        innerPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.NORTH);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
        buttonPanel.add(Box.createRigidArea(new Dimension(80, 0)), BorderLayout.WEST);
        buttonPanel.add(Box.createRigidArea(new Dimension(80, 0)), BorderLayout.EAST);

        submitBtn = new JButton("OK");
        buttonPanel.add(submitBtn, BorderLayout.CENTER);
        innerPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel nameLabelPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel("Nhập tên của nhóm");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabelPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.NORTH);
        nameLabelPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
        nameLabelPanel.add(nameLabel, BorderLayout.CENTER);
        nameField = new JTextField(15);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());
        namePanel.add(nameLabelPanel, BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        namePanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.WEST);
        namePanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(namePanel, BorderLayout.NORTH);
        add(innerPanel, BorderLayout.CENTER);
        setTitle("Tạo nhóm");

        ArrayList<String> users = Main.chatScreen.getUserListExceptSelf();
        userListModel.addAll(users);

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> selectedUsers =  userList.getSelectedValuesList();
                if (!selectedUsers.isEmpty() && !nameField.getText().isEmpty()) {
                    Main.client.sendLine("/create_group");
                    Main.client.sendLine(nameField.getText());
                    Main.client.sendLine(Integer.toString(selectedUsers.size()));
                    for (String user : selectedUsers) {
                        Main.client.sendLine(user);
                    }
                }
                dispose();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                super.windowClosing(e);
            }
        });

        pack();
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

    }
}
