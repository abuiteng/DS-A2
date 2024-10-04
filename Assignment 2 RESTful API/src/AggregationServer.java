import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

/**
 * AggregationServer handles incoming requests for weather data, storing it
 * and allowing clients to upload updates. It maintains a Lamport clock
 * for synchronization and manages stale data cleanup.
 */
public class AggregationServer {
    private static int PORT = 4567;
    private static final int TIMEOUT = 30000; // 30 seconds
    private static final int MAX_ENTRIES = 20;
    private static final String FILE_PATH = "weather_data.json";

    private static Map<String, String> weatherData = new LinkedHashMap<>();
    private static Map<String, Long> lastUpdated = new HashMap<>();
    private static Map<String, Boolean> clientHasUploadedData = new HashMap<>();
    private static LamportClock lamportClock = new LamportClock();

    /**
     * Main method to start the Aggregation Server.
     * Loads existing weather data and listens for incoming connections.
     *
     * @param args Command-line arguments for port configuration.
     */
    public static void main(String[] args) throws IOException {
        loadWeatherDataFromFile();

        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port > 0) {
                    PORT = port;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default 4567");
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Aggregation Server started on port " + PORT);
            Runtime.getRuntime().addShutdownHook(new Thread(AggregationServer::saveWeatherDataToFile));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }
    }

    /**
     * Loads existing weather data from the specified file.
     * Each line in the file is expected to be a valid JSON object.
     */
    private static void loadWeatherDataFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    JSONObject jsonObject = new JSONObject(line);
                    String id = jsonObject.getString("id");
                    weatherData.put(id, jsonObject.toString());
                    lastUpdated.put(id, System.currentTimeMillis());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current weather data to a file.
     * Each entry is written as a new line in the JSON format.
     */
    private static void saveWeatherDataToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String data : weatherData.values()) {
                writer.write(data);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles client connections in a separate thread.
     * Processes incoming requests and delegates to appropriate handlers.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = clientSocket.getInputStream();
                 OutputStream outputStream = clientSocket.getOutputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String requestLine = reader.readLine();

                if (requestLine == null) {
                    sendErrorResponse(outputStream, 400, "Bad Request");
                    return;
                }

                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0];
                String path = requestParts[1];

                if (method.equals("PUT")) {
                    handlePutRequest(reader, outputStream);
                } else if (method.equals("GET")) {
                    handleGetRequest(outputStream);
                } else {
                    sendErrorResponse(outputStream, 400, "Bad Request");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Handles PUT requests to update weather data.
         * Reads the request body, updates the data, and sends the appropriate response.
         *
         * @param reader The BufferedReader to read the request.
         * @param outputStream The OutputStream to send the response.
         * @throws IOException if an I/O error occurs.
         */
        private void handlePutRequest(BufferedReader reader, OutputStream outputStream) throws IOException {
            StringBuilder requestBody = new StringBuilder();
            String line;
            int contentLength = 0;

            // Read headers to get content length
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(line.substring("Content-Length: ".length()).trim());
                }
            }

            // Read the request body
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                requestBody.append(buffer);
            }

            try {
                JSONObject jsonObject = new JSONObject(requestBody.toString());
                String id = jsonObject.getString("id");
                lamportClock.increment();

                // Update weather data
                weatherData.put(id, jsonObject.toString());
                lastUpdated.put(id, System.currentTimeMillis());
                saveWeatherDataToFile();

                // Clean up stale data
                cleanUpStaleData();

                // Maintain the number of entries
                if (weatherData.size() > MAX_ENTRIES) {
                    Iterator<String> iterator = weatherData.keySet().iterator();
                    while (iterator.hasNext() && weatherData.size() > MAX_ENTRIES) {
                        String key = iterator.next();
                        long lastUpdate = lastUpdated.getOrDefault(key, 0L);
                        if (System.currentTimeMillis() - lastUpdate > TIMEOUT) {
                            iterator.remove();
                            lastUpdated.remove(key);
                        }
                    }
                }

                // Determine if this is the first time the client has uploaded data
                boolean isFirstUpload = !clientHasUploadedData.getOrDefault(id, false);

                clientHasUploadedData.put(id, true);

                // Write response: 201 for first upload, 200 for subsequent
                if (isFirstUpload) {
                    sendResponse(outputStream, 201, "Created");
                } else {
                    sendResponse(outputStream, 200, "OK");
                }
            } catch (Exception e) {
                sendErrorResponse(outputStream, 500, "Internal Server Error");
            }

        }

        /**
         * Cleans up stale data from the weather data storage.
         * Removes entries that haven't been updated within the defined timeout period.
         */
        private void cleanUpStaleData() {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<String, Long>> iterator = lastUpdated.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                if (currentTime - entry.getValue() > TIMEOUT) {
                    weatherData.remove(entry.getKey());
                    iterator.remove();
                }
            }
        }

        /**
         * Handles GET requests to retrieve weather data.
         * Generates a JSON response containing the current weather data.
         *
         * @param outputStream The OutputStream to send the response.
         * @throws IOException if an I/O error occurs.
         */
        private void handleGetRequest(OutputStream outputStream) throws IOException {
            lamportClock.increment();
            cleanUpStaleData();

            JSONObject responseJson = new JSONObject();
            for (Map.Entry<String, String> entry : weatherData.entrySet()) {
                responseJson.put(entry.getKey(), new JSONObject(entry.getValue()));
            }

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: application/json");
            writer.println("Content-Length: " + responseJson.toString().getBytes().length);
            writer.println();
            writer.println(responseJson.toString());
            writer.flush();
        }

        /**
         * Sends a standard HTTP response.
         *
         * @param outputStream The OutputStream to send the response.
         * @param statusCode The HTTP status code.
         * @param message The status message.
         * @throws IOException if an I/O error occurs.
         */
        private void sendResponse(OutputStream outputStream, int statusCode, String message) throws IOException {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            writer.println("HTTP/1.1 " + statusCode + " " + message);
            writer.println("Content-Length: 0");
            writer.println();
            writer.flush();
        }

        /**
         * Sends an error response for HTTP errors.
         *
         * @param outputStream The OutputStream to send the response.
         * @param statusCode The HTTP status code.
         * @param message The error message.
         * @throws IOException if an I/O error occurs.
         */
        private void sendErrorResponse(OutputStream outputStream, int statusCode, String message) throws IOException {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            writer.println("HTTP/1.1 " + statusCode + " " + message);
            writer.println("Content-Length: 0");
            writer.println();
            writer.flush();
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
