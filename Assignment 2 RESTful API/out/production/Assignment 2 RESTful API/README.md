# Weather Aggregation System

This system includes three components that work together to manage and retrieve weather data:

1. **AggregationServer.java**: A server that aggregates weather data from different content servers.
2. **ContentServer.java**: Sends weather data to the `AggregationServer`.
3. **GETClient.java**: Requests and retrieves the aggregated weather data from the `AggregationServer`.

## Prerequisites

- **JSON Library**: The system uses the `json-20210307.jar` library to handle JSON data. Ensure this library is available at the specified directory.

## Files Overview

1. **AggregationServer.java**:
   - Starts a server that listens for PUT and GET requests.
   - Persists weather data to `weather_data.json`.
   - Cleans up stale data based on a timeout.

2. **ContentServer.java**:
   - Sends weather data from a specified file to the `AggregationServer`.
   - Allows updates or resending of the data upon user command.

3. **GETClient.java**:
   - Sends a GET request to retrieve weather data from the `AggregationServer`.
   - Continuously allows the user to request the data or exit.

## How to Run

### Step 1: Run AggregationServer

To start the AggregationServer on a specified port (for example, port 4567), use the following command:

```bash
java -cp ".;path/to/json-20210307.jar" AggregationServer 4567
```

Replace `path/to/json-20210307` with the actual path to your json-20210307.jar file.

You can change the port number as needed. If no port is specified, it will default to `4567`.

### Step 2: Run ContentServer

The `ContentServer` requires the `AggregationServer` URL and the path to the weather data file as arguments. Run it with the following command:

```bash
java -cp ".;path to json-20210307.jar" ContentServer http://localhost:4567 "path/to/weather_data.txt"
```

Replace `path/to/json-20210307` with the actual path to your json-20210307.jar file.

Replace `path/to/weather_data.txt` with the actual path to your weather data file.

### Step 3: Run GETClient

To retrieve weather data from the `AggregationServer`, run the `GETClient` with the following command:

```bash
java -cp ".;path to json-20210307.jar" GETClient http://localhost:4567
```

Replace `path/to/json-20210307` with the actual path to your json-20210307.jar file.

### Step 4: Interactions

- **ContentServer**: Once the `ContentServer` is running, it will automatically send weather data to the `AggregationServer`. You can type `update` to resend the data or `exit` to quit the server.
- **GETClient**: The `GETClient` prompts for commands. Type `request` to fetch the weather data from the server or `exit` to close the client.

### Example:

For my system I use the commands:
```bash
java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" AggregationServer 4567
java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" ContentServer http://localhost:4567 "C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\out\production\Assignment 2 RESTful API\weather_data.txt"
java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" GETClient http://localhost:4567
```
Running each in their own terminal of course.

### Test Mode for ContentServer:

To facilitate automated testing, a `boolean` flag `isTestMode` has been added to the `ContentServer.java` file. When `isTestMode` is set to `true`, the program does not enter into a continuous loop, which allows the test cases to execute without manual interaction. This flag is useful for running tests on the `ContentServer` without requiring user input.

By default, this flag is set to `false`, which means the server will run continuously, waiting for user commands. For testing purposes, `isTestMode` is set to  `true` in order to allow the test cases to finish running without user input.

Both `ContentServerTest` `GETClientTests` and `IntegrationTest` make use of this feature, and you do not need to adjust anything for this to work.

However, you must start `AggregationServer` on `port:8080` before running these test cases.