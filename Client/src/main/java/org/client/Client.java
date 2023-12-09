package org.client;

import org.client.components.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private boolean done = false;


    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

//            InputHandler inHandler = new InputHandler();
//            Thread t = new Thread(inHandler);
//            t.start();

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
                            Main.chatScreen = new ChatScreen();
                            Main.chatScreen.setUsername(split[1]);
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

                    User user = Main.chatScreen.getUserComp(username);
                    if (user != null) {
                        user.addToChatLog(username, content);
                        if (username.equals(Main.chatScreen.getCurrentChatUser())) {
                            Main.chatScreen.addMessage(username, content);
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
                            user.addToChatLog(from, content);
                        }

                        if (username.equals(Main.chatScreen.getCurrentChatUser())) {
                            Main.chatScreen.updateMsgList();
                        }
                    }
                }
                else if (inMsg.startsWith("/chat_success")) {
                    String username = in.readLine().strip();
                    String content = in.readLine().strip();
                    Main.chatScreen.addMessage(username, content);
                }
                else if (inMsg.startsWith("/new_user")) {
                    String[] split = inMsg.split(" ", 2);
                    if (split.length == 2 && !split[1].equals(Main.chatScreen.getUsername())) {
                        Main.chatScreen.addUser(split[1]);
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
//    class InputHandler implements Runnable {
//        @Override
//        public void run() {
//            try {
//                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
//                while (!done) {
//                    String msg = inReader.readLine();
//                    if (msg.equals("/quit")) {
//                        out.println(msg);
//                        inReader.close();
//                        shutdown();
//                    }
//                    else {
//                        out.println(msg);
//                    }
//                }
//            } catch (IOException e) {
//                shutdown();
//            }
//        }
//    }
//

}