package org.server;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    int port;
    public static ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private Boolean done = false;
    private ExecutorService pool;
    public Server(int port) {
        this.port = port;
        connections = new ArrayList<>();
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            pool = Executors.newCachedThreadPool();
            System.out.println("Listening on port 8080");
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            this.shutdown();
        }
    }

    public static void broadcast(String msg) {
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch.getClient().getUsername() != null) {
                ch.sendLine(msg);
            }
        }
    }
    public static void broadcast(String msg, String except) {
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch.getClient().getUsername() != null && !Objects.equals(ch.getClient().getUsername(), except)) {
                ch.sendLine(msg);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Database.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Server server = new Server(8080);
        server.run();
    }
}