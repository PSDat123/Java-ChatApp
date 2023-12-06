import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                        Main.authScreen.setLoginAlert(split[1], "success");
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