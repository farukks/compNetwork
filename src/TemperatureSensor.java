import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TemperatureSensor {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        simulateSensorDataTransmission();
    }

    public static void simulateSensorDataTransmission() throws IOException, ClassNotFoundException {
        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (true) {
            double temperature = generateTemperature(random);
            String timestamp = getTimestamp(dateFormat);

            sendToGateway(temperature, timestamp);

            try {
                Thread.sleep(1000); // Wait for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static double generateTemperature(Random random) {
        return 20 + random.nextDouble() * 10; // Generates random temperature between 20 and 30
    }

    public static String getTimestamp(SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date()); // Get current timestamp
    }

    public static void sendToGateway(double temperature, String timestamp) throws IOException, ClassNotFoundException {
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        socket = new Socket(host.getHostName(), 9876);
        //write to socket using ObjectOutputStream
        oos = new ObjectOutputStream(socket.getOutputStream());

        String message1 = "Temperature: " + temperature + "°C | Timestamp: " + timestamp;
        oos.writeObject(message1);
        oos.close();

    }
}