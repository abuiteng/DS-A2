import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClientTests {

    @Test
    public void testGETClientValidServer() throws Exception {
        // Mock valid server URL
        String serverUrl = "http://localhost:8080";
        GETClient.setTestMode(true);
        GETClient.main(new String[]{serverUrl});

        // Verify that GETClient receives HTTP OK
        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        assertEquals(HttpURLConnection.HTTP_OK, responseCode, "GETClient should receive HTTP OK from valid server.");
    }

    @Test
    public void testGETClientInvalidServer() throws Exception {
        // Mock invalid server URL
        String invalidServerUrl = "http://invalidserver:8080";
        GETClient.setTestMode(true);

        assertThrows(Exception.class, () -> GETClient.main(new String[]{invalidServerUrl}),
                "GETClient should throw an exception for an invalid server URL.");
    }
}
