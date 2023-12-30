import java.io.*;
import java.net.*;

public class server {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started, waiting for connections...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    // Receive and process handshake
                    String handshakeMessage = (String) input.readObject();
                    if ("HANDSHAKE".equals(handshakeMessage)) {

                        // Send acknowledgment
                        output.writeObject("ACK");

                        // Receive device info
                        String message = (String) input.readObject();
                        System.out.println("Received: " + message);

                        // The server can now wait for further messages from the gateway
                        // Implement logic here to handle incoming sensor messages

                    } else {
                        System.out.println("Invalid handshake message");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
