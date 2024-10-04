import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ContentServerTest {

    @Test
    public void testSendWeatherData_validFile() {
        // Mock a valid file path and server URL
        String validFilePath = "src/weather_data.txt"; // Path to a valid test file
        String serverUrl = "http://localhost:8080"; // Assuming server is running locally on port 8080

        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, validFilePath}); // Call ContentServer with valid parameters

        File file = new File(validFilePath);
        assertTrue(file.exists(), "The file should exist.");
    }

    @Test
    public void testSendWeatherData_invalidFile() {
        // Mock an invalid file path
        String invalidFilePath = "src/nonexistent_file.txt";
        String serverUrl = "http://localhost:8080";

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));

        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, invalidFilePath});

        assertEquals("File not found: src/nonexistent_file.txt" + System.lineSeparator(), outContent.toString(), "Should return File not found");
    }

    @Test
    public void testSendWeatherData_malformedData() {
        // Mock a file path with malformed data
        String malformedFilePath = "src/malformed_weather_data.txt";
        String serverUrl = "http://localhost:8080";

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));

        ContentServer.setTestMode(true);
        ContentServer.main(new String[]{serverUrl, malformedFilePath});

        assertEquals("File not formatted correctly: src/malformed_weather_data.txt" + System.lineSeparator(), outContent.toString(), "Should give error for unformatted data");
    }
}
