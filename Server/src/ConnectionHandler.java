import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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
                            boolean success = Auth.login(username, password);
                            if (!success) {
                                this.sendLine("/login_error Tên đăng nhập hoặc mật khẩu sai!");
                            }
                            else {
                                this.sendLine("/login_success Đăng nhập thàng công!");
                                client.setUsername(username);
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
                            boolean success = Auth.register(username, password);
                            if (!success) {
                                this.sendLine("/register_error Tên đăng nhập đã tồn tại!");
                            }
                            else {
                                this.sendLine("/register_success Đăng kí thành công, hãy đăng nhập!");
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            this.sendLine("/register_error Tên đăng nhập đã tồn tại!");
                        }
                        break;
                    }
                    case "/chat": {
                        String to = this.recvLine();
                        String content = this.recvLine();
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
        } catch (IOException e) {
            // ignore
        }
    }
}
