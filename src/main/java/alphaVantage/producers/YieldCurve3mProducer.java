package alphaVantage.producers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class YieldCurve3mProducer {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(properties);

        try {
            String alphaVantageApiKey = "TL8NM6S7JD4XLR2Q";
            String apiUrl = "https://www.alphavantage.co/query?function=TREASURY_YIELD&interval=monthly&maturity=3month&apikey=" + alphaVantageApiKey;
            String jsonString = fetchDataFromApi(apiUrl);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            JsonNode dataArray = rootNode.get("data");

            dataArray.forEach(dataEntry -> {
                String date = dataEntry.get("date").asText();
                String value = dataEntry.get("value").asText();

                ObjectNode messageContent = objectMapper.createObjectNode();
                messageContent.put("date", date);
                messageContent.put("value", value);

                try {
                    String messageValue = objectMapper.writeValueAsString(messageContent);
                    ProducerRecord<String, String> record = new ProducerRecord<>("yieldCurve3m", messageValue);
                    System.out.println("Sending message: " + messageValue);
                    producer.send(record);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fetchDataFromApi(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }
        return response.toString();
    }
}
