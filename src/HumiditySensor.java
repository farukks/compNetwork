import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class HumiditySensor {

    private static final int GATEWAY_PORT = 9877; // Assuming a different port for UDP
    private static final String GATEWAY_HOST = "localhost";
    private static final int ALIVE_INTERVAL = 3000; // 3 seconds

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lastAliveTime = System.currentTimeMillis();

        while (true) {
            double humidity = generateHumidity(random);
            String timestamp = getTimestamp(dateFormat);

            if (humidity > 80) {
                sendToGateway("Humidity: " + humidity + "% | Timestamp: " + timestamp);
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAliveTime >= ALIVE_INTERVAL) {
                sendToGateway("ALIVE");
                lastAliveTime = currentTime;
            }

            try {
                Thread.sleep(1000); // Wait for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static double generateHumidity(Random random) {
        return 40 + random.nextDouble() * 50; // Generates random humidity between 40 and 90
    }

    public static String getTimestamp(SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date()); // Get current timestamp
    }

    public static void sendToGateway(String message) throws IOException {
        byte[] buffer = message.getBytes();
        InetAddress host = InetAddress.getByName(GATEWAY_HOST);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, host, GATEWAY_PORT);
        DatagramSocket socket = new DatagramSocket();

        socket.send(packet);
        socket.close();
    }
}
