package org.client;

import org.client.components.Group;
import org.client.components.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream fout;
    private DataInputStream fin;
    private boolean done = false;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            fout = new DataOutputStream(client.getOutputStream());
            fin = new DataInputStream(client.getInputStream());

            String inMsg;
            while ((inMsg = in.readLine()) != null) {
                if (inMsg.startsWith("/register_error")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2) {
                        Main.authScreen.setRegisterAlert(split[1], "error");
                    }
                }
                else if (inMsg.startsWith("/register_success")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2) {
                        Main.authScreen.setRegisterAlert(split[1], "success");
                    }
                }
                else if (inMsg.startsWith("/login_error")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2) {
                        Main.authScreen.setLoginAlert(split[1], "error");
                    }
                }
                else if (inMsg.startsWith("/login_success")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2) {
//                        Main.authScreen.setLoginAlert(split[1], "success");
                        if (Main.chatScreen == null) {
                            Main.authScreen.dispose();
                            Main.authScreen.setVisible(false);
                            Main.authScreen = null;
                            Main.chatScreen = new ChatScreen(split[1]);
                        }
                    }
                }
                else if (inMsg.startsWith("/user_list")) {
                    int n = Integer.parseInt(in.readLine().strip());
                    ArrayList<String> users = new ArrayList<>();
                    for (int i = 0; i < n; ++i) {
                        users.add(in.readLine().strip());
                    }
                    Main.chatScreen.updateUserList(users);
                    this.sendLine("/get_online_users");
                }
                else if (inMsg.startsWith("/online_user_list")) {
                    int n = Integer.parseInt(in.readLine().strip());
                    for (int i = 0; i < n; ++i) {
                        Main.chatScreen.activateUser(in.readLine().strip());

                    }
                }
                else if (inMsg.startsWith("/user_online")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2 && !split[1].equals(Main.chatScreen.getUsername())) {
                        Main.chatScreen.activateUser(split[1]);
                    }
                }
                else if (inMsg.startsWith("/user_offline")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2 && !split[1].equals(Main.chatScreen.getUsername())) {
                        Main.chatScreen.deactivateUser(split[1]);
                    }
                }
                else if (inMsg.startsWith("/message_from")) {
                    String username = in.readLine().strip();
                    String content = in.readLine().strip();
                    String id = in.readLine().strip();
                    String type = in.readLine().strip();
                    if (!type.startsWith("group")) {
                        User user = Main.chatScreen.getUserComp(username);
                        if (user != null && user.getInitializedStatus()) {
                            user.addToChatLog(username, content, id, type);
                            if (username.equals(Main.chatScreen.getCurrentChatUser())) {
                                Main.chatScreen.addMessage(username, content, id, type);
                            }
                        }
                    } else {
                        Group group = Main.chatScreen.getGroupComp(username);
                        String chatUser = in.readLine().strip();
                        if (group != null && group.getInitializedStatus()) {
                            group.addToChatLog(chatUser, content, id, type);
                            if (username.equals(Main.chatScreen.getCurrentChatGroup())) {
                                Main.chatScreen.addMessage(chatUser, content, id, type);
                            }
                        }
                    }

                }
                else if (inMsg.startsWith("/chat_log")) {
                    String username = in.readLine().strip();
                    int n = Integer.parseInt(in.readLine().strip());
                    User user = Main.chatScreen.getUserComp(username);
                    if (user != null) {
                        for (int i = 0; i < n; ++i) {
                            String from = in.readLine().strip();
                            String content = in.readLine().strip();
                            String id = in.readLine().strip();
                            String type = in.readLine().strip();
                            user.addToChatLog(from, content, id, type);
                        }

                        if (username.equals(Main.chatScreen.getCurrentChatUser())) {
                            Main.chatScreen.updateMsgList();
                        }
                    }
                }
                else if (inMsg.startsWith("/chat_success")) {
                    String username = in.readLine().strip();
                    String content = in.readLine().strip();
                    String id = in.readLine().strip();
                    String type = in.readLine().strip();
                    User user = Main.chatScreen.getUserComp(username);
                    if (user != null && user.getInitializedStatus()) {
                        user.addToChatLog(Main.chatScreen.getUsername(), content, id, type);
                    }
                    if (username.equals(Main.chatScreen.getCurrentChatUser())) {
                        Main.chatScreen.addMessage(Main.chatScreen.getUsername(), content, id, type);
                    }
                }
                else if (inMsg.startsWith("/new_user")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2 && !split[1].equals(Main.chatScreen.getUsername())) {
                        Main.chatScreen.addUser(split[1]);
                    }
                }
                else if (inMsg.startsWith("/remove_message")) {
                    String chatUser = in.readLine().strip();
                    String id = in.readLine().strip();
                    User user = Main.chatScreen.getUserComp(chatUser);
                    if (user != null) {
                        user.removeFromChatLog(id);
                    }
                    if (chatUser.equals(Main.chatScreen.getCurrentChatUser())) {
                        Main.chatScreen.updateMsgList(false);
                    }
                }
                else if (inMsg.startsWith("/remove_group_message")) {
                    String group_id = in.readLine().strip();
                    String id = in.readLine().strip();
                    Group group = Main.chatScreen.getGroupComp(group_id);
                    if (group != null) {
                        group.removeFromChatLog(id);
                    }
                    if (group_id.equals(Main.chatScreen.getCurrentChatGroup())) {
                        Main.chatScreen.updateMsgList(false);
                    }
                }
                else if (inMsg.startsWith("/download_file")) {
                    String filename = in.readLine().strip();
                    String home = System.getProperty("user.home");
                    String folder = home + "\\Downloads\\";
                    String filepath = folder + filename;
                    recvFile(filepath);
                    Main.chatScreen.isDownloading = false;
                    new Thread(() -> {
                        Object[] options = {"OK", "Mở folder"};
                        int input = JOptionPane.showOptionDialog(Main.chatScreen.getContentPane(), "Tải file thành công! File được lưu tại " + filepath, "Tải thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                        if (input == 1) {
                            try {
                                Desktop desktop = Desktop.getDesktop();
                                desktop.open(new File(folder));
                            } catch (IOException e) {
                                // ignore
                            }
                        }
                    }).start();
                }
                else if (inMsg.startsWith("/download_error")) {
                    String message = in.readLine().strip();
                    Main.chatScreen.isDownloading = false;
                    new Thread(() -> {
                        JOptionPane.showMessageDialog(Main.chatScreen.getContentPane(), message,"Lỗi", JOptionPane.ERROR_MESSAGE);
                    }).start();
                }
                else if (inMsg.startsWith("/group_list")) {
                    ArrayList<ArrayList<String>> groups = new ArrayList<>();
                    int n = Integer.parseInt(in.readLine().strip());
                    for (int i = 0; i < n; ++i) {
                        ArrayList<String> group = new ArrayList<>();
                        String id = in.readLine().strip();
                        String name = in.readLine().strip();
                        group.add(id);
                        group.add(name);
                        int m = Integer.parseInt(in.readLine().strip());
                        for (int j = 0; j < m; ++j) {
                            group.add(in.readLine().strip());
                        }
                        groups.add(group);
                    }
                    Main.chatScreen.updateGroupList(groups);
                }
                else if (inMsg.startsWith("/new_group")) {
                    ArrayList<String> group = new ArrayList<>();
                    String id = in.readLine().strip();
                    String name = in.readLine().strip();
                    group.add(id);
                    group.add(name);
                    int m = Integer.parseInt(in.readLine().strip());
                    for (int j = 0; j < m; ++j) {
                        group.add(in.readLine().strip());
                    }
                    Main.chatScreen.addGroup(group);
                }
                else if (inMsg.startsWith("/group_chat_log")) {
                    String group_id = in.readLine().strip();
                    int n = Integer.parseInt(in.readLine().strip());
                    Group group = Main.chatScreen.getGroupComp(group_id);
                    if (group != null) {
                        for (int i = 0; i < n; ++i) {
                            String from = in.readLine().strip();
                            String content = in.readLine().strip();
                            String id = in.readLine().strip();
                            String type = in.readLine().strip();
                            group.addToChatLog(from, content, id, type);
                        }

                        if (group_id.equals(Main.chatScreen.getCurrentChatGroup())) {
                            Main.chatScreen.updateMsgList();
                        }
                    }
                }
                System.out.println(inMsg);
            }
        } catch (Exception e) {
            shutdown();
        }
        System.out.println("SHUT DOWNED!");
    }

    public void shutdown() {
        try {
            System.out.println("Closing...");
            done = true;
            in.close();
            fin.close();
            fout.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (Exception e) {
            // ignore
        }
        System.out.println("Closed");
    }

    public void sendLine(String msg) {
        try {
            out.println(msg);
        } catch (Exception e) {
            System.err.println(e);
            shutdown();
        }
    }
    public void recvFile(String filename) throws Exception {
        int bytes = 0;
        File file = new File(filename);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file,false);

        long size = fin.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = fin.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
//        System.out.println("File is Received");
        fileOutputStream.close();
    }
    public void sendFile(File file) throws Exception {
        int bytes = 0;
        FileInputStream fileInputStream = new FileInputStream(file);

        fout.writeLong(file.length());

        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            fout.write(buffer, 0, bytes);
            fout.flush();

        }
        fileInputStream.close();
    }

}