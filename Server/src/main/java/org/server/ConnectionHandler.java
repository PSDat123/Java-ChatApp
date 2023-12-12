package org.server;

import org.apache.commons.io.FilenameUtils;

import javax.naming.Name;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ConnectionHandler implements Runnable {
    private Client client;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream fin;
    private DataOutputStream fout;
    public ConnectionHandler(Socket socket) {
        this.client = new Client(socket);
    }
    private boolean done;
    @Override
    public void run() {
        try {
            done = false;
            this.out = new PrintWriter(client.getSocket().getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
            this.fin = new DataInputStream(client.getSocket().getInputStream());
            this.fout = new DataOutputStream(client.getSocket().getOutputStream());
            String msg;
            while (!done && (msg = this.recvLine()) != null) {
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
                        String type = "text";
                        String id = Database.saveChat(client.getUsername(), to, content, type);
                        if(id != null) {
                            for (ConnectionHandler ch : Server.connections) {
                                if (ch.client != null && ch.client.getUsername().equals(to)) {
                                    ch.sendLine("/message_from");
                                    ch.sendLine(this.client.getUsername());
                                    ch.sendLine(content);
                                    ch.sendLine(id);
                                    ch.sendLine(type);
                                    break;
                                }
                            }
                            sendLine("/chat_success");
                            sendLine(to);
                            sendLine(content);
                            sendLine(id);
                            sendLine(type);
                        }
                        else {
                            sendLine("/error_chat Không thể gửi tin nhắn!");
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
                                this.sendLine(log.get(3)); // type
                            }
                        }
                        break;
                    }
                    case "/remove_message": {
                        String id = this.recvLine().strip();
                        String chatUser = this.recvLine().strip();
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
                        break;
                    }
                    case "/remove_file": {
                        String id = this.recvLine().strip();
                        String chatUser = this.recvLine().strip();
                        String filenameToDelete = null;
                        if((filenameToDelete = Database.getFileNameFromMessage(this.client.getUsername(), id)) != null ) {
                            File fileToDelete = new File(filenameToDelete);
                            if(fileToDelete.delete() && Database.removeFile(this.client.getUsername(), id)) {
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
                        }
                        break;
                    }
                    case "/send_file": {
                        String to = this.recvLine().strip();
                        String orgFileName = this.recvLine().strip();
                        String ext = FilenameUtils.getExtension(orgFileName);
                        String filename = UUID.randomUUID().toString() + '.' + ext;
                        String filepath = "assets/" + filename;
                        recvFile(filepath);

                        String content = filepath + "|" + orgFileName;
                        String type = "file";
                        String id = Database.saveChat(client.getUsername(), to, content, type);
                        if(id != null) {
                            for (ConnectionHandler ch : Server.connections) {
                                if (ch.client != null && ch.client.getUsername().equals(to)) {
                                    ch.sendLine("/message_from");
                                    ch.sendLine(this.client.getUsername());
                                    ch.sendLine(content);
                                    ch.sendLine(id);
                                    ch.sendLine(type);
                                    break;
                                }
                            }
                            sendLine("/chat_success");
                            sendLine(to);
                            sendLine(content);
                            sendLine(id);
                            sendLine(type);
                        }
                        else {
                            sendLine("/error_chat Không thể gửi file!");
                        }
                        break;
                    }
                    case "/download_file": {
                        String serverFileName = this.recvLine().strip();
                        String orgFileName = this.recvLine().strip();
                        File fileToSend = new File(serverFileName);
                        if(fileToSend.exists() && !fileToSend.isDirectory()) {
                            this.sendLine("/download_file");
                            this.sendLine(orgFileName);
                            this.sendFile(fileToSend);
                        } else {
                            this.sendLine("/download_error");
                            this.sendLine("File không còn tồn tại trên server!");
                        }
                    }
                    case "/create_group": {
                        String name = this.recvLine().strip();
                        int n = Integer.parseInt(this.recvLine().strip());
                        ArrayList<String> userList = new ArrayList<>();
                        for (int i = 0; i < n; ++i) {
                            userList.add(this.recvLine().strip());
                        }
                        userList.add(this.client.getUsername());
                        String id = Database.createGroup(name, userList);
                        if (id != null) {
                            for (ConnectionHandler ch : Server.connections) {
                                if (ch.client != null &&  userList.contains(ch.client.getUsername())) {
                                    ch.sendLine("/new_group");
                                    ch.sendLine(id);
                                    ch.sendLine(name);
                                    ch.sendLine(Integer.toString(userList.size()));
                                    for (String user : userList) {
                                        ch.sendLine(user);
                                    }
                                }
                            }

                        }
                    }
                    case "/get_groups": {
                        String username = in.readLine().strip();
                        ArrayList<ArrayList<String>> groups = Database.getAllGroup(username);
                        this.sendLine("/group_list");
                        this.sendLine(Integer.toString(groups.size()));
                        for (ArrayList<String> group : groups) {
                            this.sendLine(group.get(0)); // id
                            this.sendLine(group.get(1)); // name
                            int size = group.size() - 2;
                            this.sendLine(Integer.toString(size));
                            for (int i = 0; i < size; ++i) {
                                this.sendLine(group.get(2 + i)); // usernames
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
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
//        System.out.println(file.length());
        file.length();
        fout.writeLong(file.length());

        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            fout.write(buffer, 0, bytes);
            fout.flush();
        }
        fileInputStream.close();
    }

    public void shutdown() {
        try {
            done = true;
            this.client.setUsername(null);
            in.close();
            fin.close();
            fout.close();
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
