package org.client;

public class Main {
    public static AuthScreen authScreen;
    public static ChatScreen chatScreen;
    public static Client client;
    public static Thread clientThread;
    public static void main(String[] args) {
        authScreen = new AuthScreen();
//        chatScreen = new ChatScreen();
        client = new Client();
        clientThread = new Thread(client);
        clientThread.start();
    }
}
