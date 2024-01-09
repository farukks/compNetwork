import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomLogger {

    // Path to the log file
    private static final String LOG_FILE_PATH = "log.log";

    // Log method to write messages to the log file
    public static void log(String message, String level) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            // Format the log entry and write it to the log file
            String logEntry = formatLogEntry(message, level);
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to format log entries with timestamp, log level, and message
    private static String formatLogEntry(String message, String level) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());

        // Return the formatted log entry
        return "[" + formattedDate + "] [" + level.toUpperCase() + "] " + message;
    }
}

