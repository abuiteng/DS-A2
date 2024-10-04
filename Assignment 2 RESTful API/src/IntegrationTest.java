import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    @Test
    public void testContentServerAndAggregationServerInteraction() throws Exception {
        // Mock valid weather data file
        String validFilePath = "src/weather_data.txt";
        String serverUrl = "http://localhost:8080";

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Send data to AggregationServer via PUT
        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath});

        // Verify that AggregationServer responds correctly to the PUT request
        URL url = new URL(serverUrl + "/aggregation");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");  // PUT because we are uploading data

        String actualData = outContent.toString();

        assertTrue(actualData.contains("Server response: 200 OK") || actualData.contains("Server response: 201 Created"),
                "AggregationServer should return 200 OK or 201 Created.");
    }

    @Test
    public void testEntireInteraction() throws Exception {
        // Mock valid weather data file
        String validFilePath = "src/weather_data.txt";
        String serverUrl = "http://localhost:8080";

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Send data to AggregationServer via PUT
        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath});

        // Verify that AggregationServer responds correctly to the PUT request
        URL url = new URL(serverUrl + "/aggregation");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");  // PUT because we are uploading data

        String ContentData = outContent.toString();

        assertTrue(ContentData.contains("Server response: 200 OK") || ContentData.contains("Server response: 201 Created"),
                "AggregationServer should return 200 OK or 201 Created.");

        // Capture the output from GETClient
        ByteArrayOutputStream outGETClient = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outGETClient));

        // Run GETClient to fetch the data from AggregationServer
        GETClient.setTestMode(true);
        GETClient.main(new String[]{serverUrl});

        // Assuming GETClient prints weather data to System.out, capture that output
        String expectedData = "Weather Data:" + System.lineSeparator() +
                "apparent_t: 9.5" + System.lineSeparator() +
                "wind_spd_kmh: 15" + System.lineSeparator() +
                "rel_hum: 60" + System.lineSeparator() +
                "lon: 138.6" + System.lineSeparator() +
                "dewpt: 5.7" + System.lineSeparator() +
                "wind_spd_kt: 8" + System.lineSeparator() +
                "wind_dir: S" + System.lineSeparator() +
                "time_zone: CST" + System.lineSeparator() +
                "air_temp: 13.3" + System.lineSeparator() +
                "cloud: Partly cloudy" + System.lineSeparator() +
                "local_date_time_full: 20230715160000" + System.lineSeparator() +
                "name: Adelaide (West Terrace / ngayirdapira)" + System.lineSeparator() +
                "id: IDS60901" + System.lineSeparator() +
                "state: SA" + System.lineSeparator() +
                "press: 1023.9" + System.lineSeparator() +
                "lat: -34.9" + System.lineSeparator();

        // Capture the actual data returned by GETClient
        String GETClientData = outGETClient.toString();

        // Verify that the data returned by GETClient matches the expected weather data
        assertEquals(expectedData, GETClientData, "GETClient should return the correct weather data.");
    }
}
