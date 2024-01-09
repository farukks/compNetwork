
// 
import java.io.*;
import java.net.*;

public class Server {

    private static final int PORT = 9875;
    private static final int HTTP_PORT = 8080;
    private static final int HUMIDITY_PORT = 9788;
    private static boolean handshaking = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
                ServerSocket httpServerSocket = new ServerSocket(HTTP_PORT)) {

            System.out.println("Server started on port " + PORT + " waiting for connections...");
            CustomLogger.log("Server started on port " + PORT + " (Server.java)", "info");

            Thread httpServerThread = new Thread(() -> runServer(httpServerSocket));
            httpServerThread.start();

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                        ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                        ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    handleBasicServerRequest(input, output);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    
    private static void handleBasicServerRequest(ObjectInputStream input, ObjectOutputStream output)
            throws IOException, ClassNotFoundException {
        if (!handshaking) {
            String handshakeMessage = (String) input.readObject();
            if ("HANDSHAKE".equals(handshakeMessage)) {
                output.writeObject("ACK");
                handshaking = true;
                System.out.println("Handshake successful");
                CustomLogger.log("Handshake successfull. Response sent. (Server.java)", "info");

            } else {
                System.out.println("Invalid handshake message");
                CustomLogger.log("Handshake failed. (Server.java)", "warning");
            }
        } else {
            String message = (String) input.readObject();
            String directory = ".";

            writeToAppropriateTextFile(directory, message);
            System.out.println("Received: " + message);
        }
    }

    private static void runServer(ServerSocket httpServerSocket) {
        try {
            System.out.println("HTTP Server is listening on port " + HTTP_PORT + "...");
            CustomLogger.log("HTTP Server is listening on port " + HTTP_PORT + " (Server.java)", "info");
            while (true) {

                try (Socket clientSocket = httpServerSocket.accept()) {
                    handleHTTPRequest(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHTTPRequest(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            StringBuilder request = new StringBuilder();

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                request.append(line).append("\r\n");
            }

            String[] requestParts = request.toString().split(" ");
            if (requestParts.length >= 3) {
                String method = requestParts[0];
                String path = requestParts[1];

                String response;
                if ("GET".equals(method) && "/temperature".equals(path)) {
                    String temperatureHTML = generateTemperatureHTML();
                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: " + temperatureHTML.length() + "\r\n" +
                            "\r\n" +
                            temperatureHTML;
                } else if ("GET".equals(method) && "/humidity".equals(path)) {
                    String humidityHTML = generateHumidityHTML();
                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: " + humidityHTML.length() + "\r\n" +
                            "\r\n" +
                            humidityHTML;
                } else if ("GET".equals(method) && "/gethumidity".equals(path)) {
                    String lastHumidity = getLastHumidity();
                    String lastHumidityHTML = generateLastHumidityHTML(lastHumidity);
                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: " + lastHumidityHTML.length() + "\r\n" +
                            "\r\n" +
                            lastHumidityHTML;
                        CustomLogger.log("Last humidity valu received " + lastHumidity + " (Server.java)", "info");        
                } else {
                    response = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                            "<html><body><h1>404 Not Found</h1></body></html>";
                }

                writer.println(response);
            } else {
                System.out.println("Invalid HTTP request format");
                writer.println("HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                        "<html><body><h1>404 Not Found</h1></body></html>");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToAppropriateTextFile(String directory, String text) {
        if (text.contains("Temperature")) {
            writeToTextFile(directory, "Temperature.txt", text);
        } else if (text.contains("Humidity")) {
            writeToTextFile(directory, "Humidity.txt", text);
        }
    }

    private static void writeToTextFile(String directory, String fileName, String text) {
        try {
            String filePath = directory + File.separator + fileName;

            File file = new File(filePath);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(text);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateTemperatureHTML() {
        StringBuilder html = new StringBuilder();
        html.append(
                "<html><head><title>Temperature Data</title></head><body style='background: linear-gradient(to right, #4e89ae, #41ba9d); font-family: Arial, sans-serif;'>");
        html.append("<h2 style='color: white; text-align: center;'>Temperature Data</h2>");
        html.append(
                "<table border='2' style='width:70%; margin:auto; border-collapse: collapse; border-color: black;'>");
        html.append(
                "<tr style='background-color: #f15a5a; color: white; font-weight: bold;'><th style='border: 1px solid black; padding: 10px;'>Temperature</th><th style='border: 1px solid black; padding: 10px;'>Time</th></tr>");

        try {
            String directory = ".";
            String filePath = directory + File.separator + "Temperature.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Temperature") && line.contains("Timestamp")) {
                    String[] parts = line.split("\\|");
                    String temperaturePart = parts[0].trim();
                    String timePart = parts[1].trim();

                    String temperature = temperaturePart.split(":")[1].trim();
                    String[] timeParts = timePart.split(":");
                    String time = timeParts[1].trim() + ":" + timeParts[2].trim() + ":" + timeParts[3].trim();

                    html.append(
                            "<tr style='background-color: #fcc1c1;'><td style='border: 1px solid black; padding: 10px;'>")
                            .append(temperature).append("</td><td style='border: 1px solid black; padding: 10px;'>")
                            .append(time).append("</td></tr>");
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    private static String generateHumidityHTML() {
        StringBuilder html = new StringBuilder();
        html.append(
                "<html><head><title>Humidity Data</title></head><body style='background: linear-gradient(to right, #4e89ae, #41ba9d); font-family: Arial, sans-serif;'>");
        html.append("<h2 style='color: white; text-align: center;'>Humidity Data</h2>");
        html.append(
                "<table border='2' style='width:70%; margin:auto; border-collapse: collapse; border-color: black;'>");
        html.append(
                "<tr style='background-color: #f15a5a; color: white; font-weight: bold;'><th style='border: 1px solid black; padding: 10px;'>Humidity</th><th style='border: 1px solid black; padding: 10px;'>Time</th></tr>");

        try {
            String directory = ".";
            String filePath = directory + File.separator + "Humidity.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Humidity") && line.contains("Timestamp")) {
                    String[] parts = line.split("\\|");
                    String humidityPart = parts[0].trim();
                    String timePart = parts[1].trim();

                    String humidity = humidityPart.split(":")[1].trim();
                    String[] timeParts = timePart.split(":");
                    String time = timeParts[1].trim() + ":" + timeParts[2].trim() + ":" + timeParts[3].trim();
                    html.append(
                            "<tr style='background-color: #fcc1c1;'><td style='border: 1px solid black; padding: 10px;'>")
                            .append(humidity).append("</td><td style='border: 1px solid black; padding: 10px;'>")
                            .append(time).append("</td></tr>");
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    private static String generateLastHumidityHTML(String message) {
        StringBuilder html = new StringBuilder();
        html.append(
                "<html><head><title>Last Humidity Data</title></head><body style='background: linear-gradient(to right, #4e89ae, #41ba9d); font-family: Arial, sans-serif;'>");

        try {
            String directory = ".";
            String filePath = directory + File.separator + "Humidity.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String[] parts = message.split("\\|");
            String humidityPart = parts[0].trim();
            String timePart = parts[1].trim();

            String humidity = humidityPart.split(":")[1].trim();
            String[] timeParts = timePart.split(":");
            String time = timeParts[1].trim() + ":" + timeParts[2].trim() + ":" + timeParts[3].trim();

            html.append(
                    "<div style='text-align: center; margin-top: 50px; width: 70%; margin-left: auto; margin-right: auto; border: none;'>");
            html.append("<table border='2' style='width: 100%; border-collapse: collapse; border-color: black;'>");
            html.append(
                    "<tr style='background-color: #f15a5a; color: white; font-weight: bold;'><td style='border: 1px solid black; padding: 10px;'>Last Humidity</td><td style='border: 1px solid black; padding: 10px;'>")
                    .append(humidity).append("</td></tr>");
            html.append(
                    "<tr style='background-color: #fcc1c1; font-weight: bold;'><td style='border: 1px solid black; padding: 10px;'>Time</td><td style='border: 1px solid black; padding: 10px;'>")
                    .append(time).append("</td></tr>");
            html.append("</table>");
            html.append("</div>");

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        html.append("</table></body></html>");
        return html.toString();
    }

    private static String getLastHumidity() {
        try (DatagramSocket socket = new DatagramSocket(HUMIDITY_PORT)) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength());
            System.currentTimeMillis();
            CustomLogger.log("The last measured humidity sensor data is read " + message, "info");
            return message;

        } catch (IOException e) {
            return e.getMessage();
        }
    }

}
