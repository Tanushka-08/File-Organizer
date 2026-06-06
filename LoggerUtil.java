import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtil {

    private static final String LOG_FILE_NAME = "organizer_log.txt";

    public static void writeLog(String folderPath, String message) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            return;
        }

        File logFile = new File(folderPath, LOG_FILE_NAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write("[" + getCurrentTime() + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Logging failed: " + e.getMessage());
        }
    }

    public static void clearLog(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            return;
        }

        File logFile = new File(folderPath, LOG_FILE_NAME);

        try {
            if (logFile.exists()) {
                new PrintWriter(logFile).close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not clear log file.");
        }
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}