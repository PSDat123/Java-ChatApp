package org.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionHandler implements Runnable {
    private Client client;
    private PrintWriter out;
    private BufferedReader in;
    public ConnectionHandler(Socket socket) {
        this.client = new Client(socket);
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(client.getSocket().getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
//            this.sendLine("Please enter your username: ");
//            client.setUsername(this.recvLine());
//            System.out.println(client.getUsername() + " connected!");
            String msg;
            while ((msg = this.recvLine()) != null) {
                switch (msg) {
                    case "/quit": {
                        if (client.getUsername() != null) {
                            Server.broadcast(client.getUsername() + " left the chat");
                        }
                        this.shutdown();
                        break;
                    }
                    case "/login": {
                        String username = this.recvLine().strip();
                        String password = this.recvLine().strip();
                        try {
                            boolean success = Database.loginUser(username, password);
                            if (!success) {
                                this.sendLine("/login_error Tên đăng nhập hoặc mật khẩu sai!");
                            }
                            else {
                                client.setUsername(username);
                                this.sendLine("/login_success " + username);
                                Server.broadcast("/user_online " + username);
//                                client.setPassword(password);
                            }
                        } catch (NoSuchAlgorithmException e) {
                            this.sendLine("/login_error Lỗi hệ thống");
                        }
                        break;
                    }
                    case "/register": {
                        String username = this.recvLine().strip();
                        String password = this.recvLine().strip();
                        try {
                            boolean success = Database.registerUser(username, password);
                            if (!success) {
                                this.sendLine("/register_error Tên đăng nhập đã tồn tại!");
                            }
                            else {
                                this.sendLine("/register_success Đăng kí thành công, hãy đăng nhập!");
                            }
                        } catch (NoSuchAlgorithmException e) {
                            this.sendLine("/register_error Tên đăng nhập đã tồn tại!");
                        }
                        break;
                    }
                    case "/chat": {
                        String to = this.recvLine().strip();
                        String content = this.recvLine().strip();
                        for (ConnectionHandler ch : Server.connections) {
                            if (ch.client.getUsername().equals(to)) {
                                ch.sendLine("/message_from");
                                ch.sendLine(this.client.getUsername());
                                ch.sendLine(content);
                            }
                        }
                        if(!Database.saveChat(client.getUsername(), to, content)) {
                            sendLine("/error_chat can't save chat!");
                        }
                        else {
                            sendLine("/chat_success");
                        }
                        break;
                    }
                    case "/get_users": {
                        ArrayList<String> userList = Database.getAllUser();
                        this.sendLine("/user_list");
                        this.sendLine(Integer.toString(userList.size()));
                        for (String username : userList) {
                            this.sendLine(username);
                        }
                        break;
                    }
                    case "/get_chat_log_from": {
                        String username = this.recvLine().strip();
                        ArrayList<ArrayList<String>> logs = Database.getChatLog(client.getUsername(), username);
                        if (logs != null && !logs.isEmpty()) {
                            this.sendLine("/chat_log");
                            this.sendLine(username);
                            this.sendLine(Integer.toString(logs.size()));
                            for (ArrayList<String> log : logs) {
                                this.sendLine(log.get(0));
                                this.sendLine(log.get(1));
                            }
                        }
                        break;
                    }
                }
//                if (msg.startsWith("/quit")) {
//                } else {
////                    System.out.println(client.getUsername() + ": " + msg);
//                    System.out.println(msg);
//                }
            }
        } catch (IOException e) {
            this.shutdown();
        }

    }
    public void sendLine(String msg) {
        this.out.println(msg);
    }
    public void send(String msg) {
        this.out.print(msg);
    }
    public String recvLine() throws IOException {
        return this.in.readLine();
    }
    public void shutdown() {
        try {
            in.close();
            out.close();
            if (!client.getSocket().isClosed()) {
                client.getSocket().close();
            }
            Server.connections.remove(this);
        } catch (IOException e) {
            // ignore
        }
    }

    public Client getClient() {
        return client;
    }
}
