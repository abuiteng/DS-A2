import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * GETClient allows users to interact with the AggregationServer
 * to retrieve weather data on demand. It supports user commands
 * to either request data or exit the application.
 */
public class GETClient {

    /**
     * Main method to execute the GETClient application.
     * Initializes user input handling and manages requests to the server.
     *
     * @param args Command-line arguments containing the server URL.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java GETClient <server-url>");
            System.exit(1);
        }

        String serverUrl = args[0];
        Scanner scanner = new Scanner(System.in);

        while (true) {  // Infinite loop to keep the connection persistent
            System.out.println("Enter 'request' to get weather data or 'exit' to quit:");

            String command = scanner.nextLine().trim().toLowerCase();

            if (command.equals("exit")) {
                System.out.println("Exiting GETClient...");
                break;
            } else if (command.equals("request")) {
                sendGetRequest(serverUrl);
            } else {
                System.out.println("Invalid command. Please enter 'request' or 'exit'.");
            }
        }
        scanner.close(); // Close the scanner resource
    }

    /**
     * Sends a GET request to the specified server URL.
     * Processes the server's response and prints the weather data.
     */
    private static void sendGetRequest(String serverUrl) {
        try {
            // Construct the URL object
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Get the response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());

                // Since the JSON response has a nested object with the ID as the key
                String id = jsonResponse.keys().next();
                JSONObject weatherData = jsonResponse.getJSONObject(id).getJSONObject("data");

                // Print the weather data
                System.out.println("Weather Data:");
                // Print each attribute and its value
                weatherData.keySet().forEach(key -> {
                    System.out.println(key + ": " + weatherData.get(key));
                });
            } else {
                System.err.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
