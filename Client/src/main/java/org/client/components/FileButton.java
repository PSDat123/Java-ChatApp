package org.client.components;

import org.client.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class FileButton extends JButton {
    JFileChooser fc;
    public FileButton() {
        fc = new JFileChooser();
        try {
            this.setIcon(getIcon());
        } catch (Exception e) {
            // ignore
        }

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = fc.showOpenDialog(Main.chatScreen.getContentPane());
                if (choice == JFileChooser.APPROVE_OPTION) {
//                    copy();
                    System.out.println(fc.getSelectedFile());
                }
            }
        });
    }
    public ImageIcon getIcon() {
        return new ImageIcon(Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/attach.png"))) {
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
