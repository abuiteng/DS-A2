import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * ContentServer manages the weather data file and sends its content
 * to the AggregationServer. It allows for automatic data sending on
 * startup and provides a mechanism to update data on demand.
 */
public class ContentServer {

    private static String serverUrl; // URL of the AggregationServer
    private static String filePath; // Path to the weather data file
    private static LamportClock lamportClock = new LamportClock(); // Lamport clock instance
    static boolean isTestMode = false; // Introduce a flag for test mode

    static void setTestMode(boolean testMode) {
        isTestMode = testMode;
    }

    /**
     * Main method to start the ContentServer.
     * Reads command-line arguments for server URL and file path,
     * and initializes the data sending mechanism.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java ContentServer <server-url> <file-path>");
            System.exit(1);
        }

        serverUrl = args[0];
        filePath = args[1];

        // Automatically send the weather_data.txt on startup
        sendWeatherData();

        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        try {
            while (true) {
                if (isTestMode) break;
                System.out.print("Enter 'update' to resend the weather data or 'exit' to quit: ");
                command = consoleReader.readLine();

                if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Exiting...");
                    break;
                } else if ("update".equalsIgnoreCase(command)) {
                    sendWeatherData();
                } else {
                    System.out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the weather data from the specified file to the AggregationServer.
     * Reads the file contents, creates a JSON payload, and sends it via a PUT request.
     */
    private static void sendWeatherData() {
        try {
            File weatherDataFile = new File(filePath);
            if (!weatherDataFile.exists()) {
                System.err.println("File not found: " + filePath);
                return;
            }

            // Read the file contents and create a valid JSON object
            StringBuilder fileContent = new StringBuilder();
            try (BufferedReader fileReader = new BufferedReader(new FileReader(weatherDataFile))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                    //System.out.println(line);
                }
            }

            // Parse the data to create a valid JSON object
            String[] lines = fileContent.toString().split("\n");
            JSONObject dataJson = new JSONObject();

            for (String line : lines) {
                String[] keyValue = line.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    dataJson.put(key, value);
                } else if (keyValue.length < 2) {
                    System.err.println("File not formatted correctly: " + filePath);
                    return;
                }
            }

            // Create the complete JSON payload
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("id", weatherDataFile.getName());
            jsonPayload.put("data", dataJson);

            // Log the JSON payload being sent
            System.out.println("Sending JSON: " + jsonPayload.toString());

            // Send the PUT request
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(jsonPayload.toString().length()));

            try (OutputStream outputStream = conn.getOutputStream()) {
                outputStream.write(jsonPayload.toString().getBytes());
                outputStream.flush();
            }

            // Get the server response
            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();

            // Print the server response
            System.out.println("Server response: " + responseCode + " " + responseMessage);

            // Read response body if needed
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * LamportClock provides a simple implementation of a logical clock
     * for synchronizing events across distributed systems.
     */
    public static class LamportClock {
        private int counter = 0;

        //Increments the clock by one.
        public synchronized void increment() {
            counter++;
        }


        //Returns the current value of the clock.
        public synchronized int getClock() {
            return counter;
        }

        //Updates the clock based on the received clock value.
        public synchronized void update(int otherClock) {
            counter = Math.max(counter, otherClock) + 1;
        }
    }
}
