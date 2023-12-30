import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class gateWay {

    private static final int TCP_PORT = 9876; // TCP port for temperature sensor
    private static final int UDP_PORT = 9877; // UDP port for humidity sensor
    private static final int SERVER_PORT = 12345;
    private static final String SERVER_HOST = "localhost";
    private static final long TEMP_SENSOR_TIMEOUT = 3000; // 3 seconds
    private static final long HUMIDITY_SENSOR_TIMEOUT = 7000; // 7 seconds

    private static long lastTempSensorMessageTime = System.currentTimeMillis();
    private static long lastHumiditySensorMessageTime = System.currentTimeMillis();

    public static void main(String args[]) throws IOException {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(gateWay::checkSensorStatus, 1, 1, TimeUnit.SECONDS);

        new Thread(gateWay::startTcpListener).start();
        new Thread(gateWay::startUdpListener).start();
    }

    private static void establishConnectionWithServer(String message) throws IOException  {
        try (Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream serverOutput = new ObjectOutputStream(serverSocket.getOutputStream());
             ObjectInputStream serverInput = new ObjectInputStream(serverSocket.getInputStream())) {

            // Send handshake message
            serverOutput.writeObject("HANDSHAKE");

            // Wait for server's response
            String response = (String) serverInput.readObject();
            if ("ACK".equals(response)) {
                System.out.println("Handshake successful");

                // Send device information

                serverOutput.writeObject(message);
            } else {
                System.out.println("Handshake failed");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void startTcpListener() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP Listener started on port " + TCP_PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

                    String message = (String) ois.readObject();
                    lastTempSensorMessageTime = System.currentTimeMillis();
                    System.out.println("TCP: " + message);
                    establishConnectionWithServer(message);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startUdpListener() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Listener started on port " + UDP_PORT);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                lastHumiditySensorMessageTime = System.currentTimeMillis();
                System.out.println("UDP: " + message);
                establishConnectionWithServer(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkSensorStatus() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTempSensorMessageTime > TEMP_SENSOR_TIMEOUT) {
            try {
                establishConnectionWithServer("TEMP SENSOR OFF");
                lastTempSensorMessageTime = currentTime;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentTime - lastHumiditySensorMessageTime > HUMIDITY_SENSOR_TIMEOUT) {
            try {
                establishConnectionWithServer("HUMIDITY SENSOR OFF");
                lastHumiditySensorMessageTime = currentTime;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
