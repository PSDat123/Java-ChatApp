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

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            done = true;
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String msg = inReader.readLine();
                    if (msg.equals("/quit")) {
                        out.println(msg);
                        inReader.close();
                        shutdown();
                    }
                    else {
                        out.println(msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
    public static void main(String[] args) {
        Client cl = new Client();
        cl.run();
    }

}