import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETClientTests {

    @Test
    public void testGETClientValidServer() throws Exception {
        // Mock valid server URL
        String serverUrl = "http://localhost:8080";
        String validFilePath = "src/weather_data.txt";

        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath});

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
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Mock invalid server URL
        String invalidServerUrl = "http://invalidserver:8080";
        GETClient.setTestMode(true);
        GETClient.main(new String[]{invalidServerUrl});

        String actualData = outContent.toString();

        assertTrue(actualData.contains("Something went wrong"),
                "Something should have gone wrong");
    }
}
