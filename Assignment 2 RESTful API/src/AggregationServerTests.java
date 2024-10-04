import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.net.HttpURLConnection;
import java.net.URL;

public class AggregationServerTests {

    @Test
    public void testAggregationServerValidPutRequest() throws Exception {
        // Mock valid server URL and valid PUT request
        String serverUrl = "http://localhost:8080";
        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, "src/weather_data.txt"});

        URL url = new URL(serverUrl + "/aggregation");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");

        int responseCode = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_CREATED, responseCode, "AggregationServer should return HTTP Created for valid PUT request.");
    }

    @Test
    public void testAggregationServerInvalidPutRequest() throws Exception {
        // Mock valid server URL and invalid PUT request
        String serverUrl = "http://localhost:8080";

        URL url = new URL(serverUrl + "/invalid-endpoint");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");

        int responseCode = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, responseCode, "AggregationServer should return HTTP Not Found for invalid endpoint.");
    }
}
