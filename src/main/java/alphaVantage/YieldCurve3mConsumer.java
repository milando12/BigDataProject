package alphaVantage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class YieldCurve3mConsumer {
    private static final String CSV_FILE = "yieldCurveData3m.csv";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "YieldCurveConsumerGroup3m");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, String> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Collections.singletonList("yieldCurve3m"));

        try (FileWriter fileWriter = new FileWriter(CSV_FILE, true)) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record.value(), fileWriter);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private static void processRecord(String jsonMessage, FileWriter fileWriter) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);

        String date = jsonNode.path("date").asText();
        String value = jsonNode.path("value").asText();

        fileWriter.append(date).append(",").append(value).append("\n");
        fileWriter.flush();
    }
}
