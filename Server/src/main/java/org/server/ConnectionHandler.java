package org.server;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionHandler implements Runnable {
    private Client client;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream fin;
    public ConnectionHandler(Socket socket) {
        this.client = new Client(socket);
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(client.getSocket().getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
            this.fin = new DataInputStream(client.getSocket().getInputStream());
//            this.sendLine("Please enter your username: ");
//            client.setUsername(this.recvLine());
//            System.out.println(client.getUsername() + " connected!");
            String msg;
            while ((msg = this.recvLine()) != null) {
                switch (msg) {
                    case "/quit": {
                        if (client.getUsername() != null) {
                            Server.broadcast("/user_offline " + client.getUsername(), client.getUsername());
                            this.client.setUsername(null);
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
                                Server.broadcast("/new_user " + username);
                            }
                        } catch (NoSuchAlgorithmException e) {
                            this.sendLine("/register_error Tên đăng nhập đã tồn tại!");
                        }
                        break;
                    }
                    case "/chat": {
                        String to = this.recvLine().strip();
                        String content = this.recvLine().strip();
                        String id = Database.saveChat(client.getUsername(), to, content);
                        if(id != null) {
                            for (ConnectionHandler ch : Server.connections) {
                                if (ch.client != null && ch.client.getUsername().equals(to)) {
                                    ch.sendLine("/message_from");
                                    ch.sendLine(this.client.getUsername());
                                    ch.sendLine(content);
                                    ch.sendLine(id);
                                    break;
                                }
                            }
                            sendLine("/chat_success");
                            sendLine(to);
                            sendLine(content);
                            sendLine(id);
                        }
                        else {
                            sendLine("/error_chat can't save chat!");
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
                    case "/get_online_users": {
                        ArrayList<String> online_users = new ArrayList<>();
                        for (ConnectionHandler ch : Server.connections) {
                            if (ch.client != null && ch.client.getUsername() != null) {
                                online_users.add(ch.client.getUsername());
                            }
                        }
                        this.sendLine("/online_user_list");
                        this.sendLine(Integer.toString(online_users.size()));
                        for (String user : online_users) {
                            this.sendLine(user);
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
                                this.sendLine(log.get(0)); // from
                                this.sendLine(log.get(1)); // content
                                this.sendLine(log.get(2)); // id
                            }
                        }
                        break;
                    }
                    case "/remove_message": {
                        String id = this.recvLine().strip();
                        String chatUser = this.recvLine().strip();
//                        System.out.println("DELETE " + id);
                        if(Database.removeMessage(this.client.getUsername(), id)) {
                            for (ConnectionHandler ch : Server.connections) {
                                if (ch.client != null && ch.client.getUsername().equals(chatUser)) {
                                    ch.sendLine("/remove_message");
                                    ch.sendLine(this.client.getUsername());
                                    ch.sendLine(id);
                                    break;
                                }
                            }
                            this.sendLine("/remove_message");
                            this.sendLine(chatUser);
                            this.sendLine(id);
                        }
//                        String content = this.recvLine().strip();
//                        ArrayList<String> online_users = new ArrayList<>();
//                        for (ConnectionHandler ch : Server.connections) {
//                            if (ch.client.getUsername() != null) {
//                                online_users.add(ch.client.getUsername());
//                            }
//                        }
//                        this.sendLine("/online_user_list");
//                        this.sendLine(Integer.toString(online_users.size()));
//                        for (String user : online_users) {
//                            this.sendLine(user);
//                        }
                        break;
                    }
                }
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
            this.client.setUsername(null);
            in.close();
            fin.close();
            out.close();
            if (!client.getSocket().isClosed()) {
                client.getSocket().close();
            }
            this.client = null;
            Server.connections.remove(this);
        } catch (IOException e) {
            // ignore
        }
    }

    public Client getClient() {
        return client;
    }
}
