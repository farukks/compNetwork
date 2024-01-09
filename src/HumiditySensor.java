import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class HumiditySensor {

    // Ports and addresses
    private static final int GATEWAY_PORT = 9877; // UDP port of the Gateway
    private static final String GATEWAY_HOST = "localhost";
    private static final int SERVER_PORT = 9788; // UDP port of the Server

    // Interval for sending "ALIVE" messages to the Gateway
    private static final int ALIVE_INTERVAL = 3000; // 3 seconds

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lastAliveTime = System.currentTimeMillis();

        while (true) {
            double humidity = generateHumidity(random);
            String timestamp = getTimestamp(dateFormat);

            // Send data to the Server
            sendToServer("Last Humidity: " + humidity + "% | Timestamp: " + timestamp);

            // If humidity is greater than 80%, also send data to the Gateway
            if (humidity > 80) {
                sendToGateway("Humidity: " + humidity + "% | Timestamp: " + timestamp);
            }

            long currentTime = System.currentTimeMillis();

            // Send "ALIVE" message to the Gateway at regular intervals
            if (currentTime - lastAliveTime >= ALIVE_INTERVAL) {
                sendToGateway("ALIVE");
                lastAliveTime = currentTime;
            }

            try {
                // Sleep for 1 second before the next iteration
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to generate random humidity values
    public static double generateHumidity(Random random) {
        return 40 + random.nextDouble() * 50;
    }

    // Method to get the current timestamp
    public static String getTimestamp(SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date());
    }

    // Method to send data to the Gateway
    public static void sendToGateway(String message) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = message.getBytes();
            InetAddress host = InetAddress.getByName(GATEWAY_HOST);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, host, GATEWAY_PORT);
            socket.send(packet);

            CustomLogger.log("Humidity sensor data sent from the humidity sensor to the gateway. (HumiditySensor.java) " + message, "info");

            socket.close();
        }
    }

    // Method to send data to the Server
    public static void sendToServer(String message) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = message.getBytes();
            InetAddress host = InetAddress.getByName(GATEWAY_HOST);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, host, SERVER_PORT);
            socket.send(packet);
            socket.close();
        }
    }
}
