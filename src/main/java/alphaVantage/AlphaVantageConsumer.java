package alphaVantage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AlphaVantageConsumer {
    private static final String CSV_FILE = "alphaVantageCrypto.csv";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroup2");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, String> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Collections.singletonList("cryptoETH"));

        try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(CSV_FILE, true), CSVFormat.DEFAULT)) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record.value(), csvPrinter);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private static void processRecord(String jsonMessage, CSVPrinter csvPrinter) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);


        String date = jsonNode.path("date").asText();
        JsonNode dataNode = jsonNode.path("data");


        String open = dataNode.path("1a. open (RSD)").asText();
        String high = dataNode.path("2a. high (RSD)").asText();
        String low = dataNode.path("3a. low (RSD)").asText();
        String close = dataNode.path("4a. close (RSD)").asText();
        String volume = dataNode.path("5. volume").asText();
        String cap = dataNode.path("6. market cap (USD)").asText();

        csvPrinter.printRecord(date, open, high, low, close, volume,cap);
        csvPrinter.flush();
    }
}
