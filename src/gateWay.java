import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Gateway {

    // Ports and addresses
    private static final int TCP_PORT = 9876; // TCP port for temperature sensor
    private static final int UDP_PORT = 9877; // UDP port for humidity sensor
    private static final int SERVER_PORT = 9875;
    private static final String SERVER_HOST = "localhost";

    // Timeouts
    private static final long TEMP_SENSOR_TIMEOUT = 3000; // 3 seconds
    private static final long HUMIDITY_SENSOR_TIMEOUT = 7000; // 7 seconds

    // Timestamps and handshaking flag
    private static long lastTempSensorMessageTime = System.currentTimeMillis();
    private static long lastHumiditySensorMessageTime = System.currentTimeMillis();
    private static boolean handshaking = false;

    public static void main(String args[]) throws IOException {

        // Scheduled executor for sensor status checks
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(Gateway::checkSensorStatus, 1, 1, TimeUnit.SECONDS);

        // Threads for starting TCP and UDP listeners
        new Thread(Gateway::startTcpListener).start();
        new Thread(Gateway::startUdpListener).start();
    }

    // Method for establishing a connection with the server
    private static void establishConnectionWithServer(String message) throws IOException {
        try (Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
                ObjectOutputStream serverOutput = new ObjectOutputStream(serverSocket.getOutputStream());
                ObjectInputStream serverInput = new ObjectInputStream(serverSocket.getInputStream())) {

            if (!handshaking) {
                // Send handshake message
                serverOutput.writeObject("HANDSHAKE");

                // Wait for the server's response
                String response = (String) serverInput.readObject();
                if ("ACK".equals(response)) {
                    System.out.println("Handshake successful");
                    handshaking = true;
                } else {
                    System.out.println("Handshake failed");
                }
            } else {
                // Send regular sensor data to the server
                serverOutput.writeObject(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
        }
    }

    // Method for starting the TCP listener
    private static void startTcpListener() {
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("TCP Listener started on port " + TCP_PORT);
            CustomLogger.log("TCP Listener started on port " + TCP_PORT + " (Gateway.java)", "info");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

                    // Read sensor data from the TCP socket
                    String message = (String) ois.readObject();
                    lastTempSensorMessageTime = System.currentTimeMillis();

                    // Establish a connection with the server and log the data
                    establishConnectionWithServer(message);
                    CustomLogger.log("Data sent to server " + message + " (Gateway.java)", "info");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
        }
    }

    // Method for starting the UDP listener
    private static void startUdpListener() {
        try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
            System.out.println("UDP Listener started on port " + UDP_PORT);
            CustomLogger.log("UDP Listener started on port " + UDP_PORT + " (Gateway.java)", "info");
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Read sensor data from the UDP packet
                String message = new String(packet.getData(), 0, packet.getLength());
                lastHumiditySensorMessageTime = System.currentTimeMillis();

                // Establish a connection with the server and log the data
                establishConnectionWithServer(message);
                CustomLogger.log("Data sent to server " + message + " (Gateway.java)", "info");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
        }
    }

    // Method for checking the status of temperature and humidity sensors
    private static void checkSensorStatus() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTempSensorMessageTime > TEMP_SENSOR_TIMEOUT) {
            try {
                // Notify the server that the temperature sensor is off
                String message = "TEMP SENSOR OFF";
                establishConnectionWithServer(message);
                CustomLogger.log("Data sent to server " + message + " (Gateway.java)", "info");
                lastTempSensorMessageTime = currentTime;
            } catch (IOException e) {
                e.printStackTrace();
                CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
            }
        }
        if (currentTime - lastHumiditySensorMessageTime > HUMIDITY_SENSOR_TIMEOUT) {
            try {
                // Notify the server that the humidity sensor is off
                String message = "HUMIDITY SENSOR OFF";
                establishConnectionWithServer(message);
                CustomLogger.log("Data sent to server " + message + " (Gateway.java)", "info");
                lastHumiditySensorMessageTime = currentTime;
            } catch (IOException e) {
                e.printStackTrace();
                CustomLogger.log(e.getMessage() + " (Gateway.java)", "error");
            }
        }
    }
}
