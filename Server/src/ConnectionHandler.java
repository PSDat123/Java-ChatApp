import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
            this.sendLine("Please enter your username: ");
            client.setUsername(this.recvLine());
            System.out.println(client.getUsername() + " connected!");
            String msg;
            while ((msg = this.recvLine()) != null) {
                if (msg.startsWith("/quit")) {
                    Server.broadcast(client.getUsername() + " left the chat");
                    this.shutdown();
                } else {
                    System.out.println(client.getUsername() + ": " + msg);
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
            in.close();
            out.close();
            if (!client.getSocket().isClosed()) {
                client.getSocket().close();
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
