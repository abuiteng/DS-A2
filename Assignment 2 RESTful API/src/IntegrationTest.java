import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
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

        // Send data to AggregationServer
        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath});

        // Verify that AggregationServer responds correctly
        URL url = new URL(serverUrl + "/aggregation");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "AggregationServer should return HTTP OK.");
    }

    @Test
    public void testEntireInteraction() throws Exception {
        // Mock valid weather data file
        String validFilePath = "src/weather_data.txt";
        String serverUrl = "http://localhost:8080";

        // Send data to AggregationServer using ContentServer
        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath});

        // Verify that AggregationServer responds with HTTP OK
        URL url = new URL(serverUrl + "/aggregation");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "AggregationServer should return HTTP OK.");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Run GETClient to fetch the data from AggregationServer
        GETClient.setTestMode(true);
        GETClient.main(new String[]{serverUrl});

        // Capture the output from GETClient (you will need to modify GETClient to return data or capture output)
        // For this example, let's assume we can redirect its output to a string or check the output stream

        // Assuming GETClient stores the weather data in a field for testing purposes
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
                "name: Adelaide (West Terrace /  ngayirdapira)" + System.lineSeparator() +
                "id: IDS60901" + System.lineSeparator() +
                "state: SA" + System.lineSeparator() +
                "press: 1023.9" + System.lineSeparator() +
                "lat: -34.9" + System.lineSeparator();
        String actualData = outContent.toString();

        // Verify that the data returned by GETClient matches the expected weather data
        assertEquals(expectedData, actualData, "GETClient should return the correct weather data.");
    }

}
