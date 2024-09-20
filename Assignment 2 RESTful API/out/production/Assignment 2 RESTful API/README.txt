Works with:
java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" AggregationServer
java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" ContentServer "http://localhost:4567" "C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\out\production\Assignment 2 RESTful API\weather_data.txt"java -cp ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" GETClient http://localhost:4567

Where ".;C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\lib\json-20210307.jar" is the absolute path to the JSON parser .jar file.
And 'C:\Users\aiden\IdeaProjects\Assignment 2 RESTful API\out\production\Assignment 2 RESTful API\weather_data.txt' is the absolute path to the input weather data .txt file.