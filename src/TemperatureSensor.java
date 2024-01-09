import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.IOException;

public class TemperatureSensor {

    private static final int GATEWAY_PORT = 9876; // Gateway TCP port
    private static final String GATEWAY_HOST = "localhost";

    public static void main(String[] args) throws IOException {
        // Simulate the transmission of temperature sensor data
        simulateSensorDataTransmission();
    }

    public static void simulateSensorDataTransmission() throws IOException {
        // Create a random number generator
        Random random = new Random();
        // Create a date formatter for timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Infinite loop to simulate continuous data transmission
        while (true) {
            // Generate a random temperature
            double temperature = generateTemperature(random);
            // Get the current timestamp
            String timestamp = getTimestamp(dateFormat);

            // Send temperature data to the gateway
            sendToGateway(temperature, timestamp);

            try {
                // Pause for 1000 milliseconds (1 second)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Print stack trace if interrupted during sleep
                e.printStackTrace();
            }
        }
    }

    // Method to generate a random temperature
    public static double generateTemperature(Random random) {
        // Generate a random temperature between 20 and 30 degrees Celsius
        return 20 + random.nextDouble() * 10;
    }

    // Method to get the current timestamp
    public static String getTimestamp(SimpleDateFormat dateFormat) {
        // Format the current date and time as a string
        return dateFormat.format(new Date());
    }

    // Method to send temperature data to the gateway
    public static void sendToGateway(double temperature, String timestamp) throws IOException {
        // Create a socket to connect to the gateway
        try (Socket socket = new Socket(GATEWAY_HOST, GATEWAY_PORT);
             // Create an object output stream to write objects to the socket
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            // Create a message with temperature and timestamp
            String message = "Temperature: " + temperature + "°C | Timestamp: " + timestamp;
            // Write the message to the object output stream
            oos.writeObject(message);
            
            // Log the temperature sensor data transmission
            CustomLogger.log("Temperature sensor data sent from the temperature sensor to the gateway. (TemperatureSensor.java) " + message, "info");

            oos.close(); // Removed this line to keep the stream open for potential future use
        }
    }
}
