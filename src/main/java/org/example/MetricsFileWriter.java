package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsFileWriter {

    private final KafkaConsumer<String, String> totalCommitsConsumer;
    private final KafkaConsumer<String, String> totalCommittersConsumer;
    private final KafkaConsumer<String, String> commitsPerLanguageConsumer;
    private static final String OUTPUT_FILE = "output-metrics.txt";

    public MetricsFileWriter(KafkaConsumer<String, String> totalCommitsConsumer,
                             KafkaConsumer<String, String> totalCommittersConsumer,
                             KafkaConsumer<String, String> commitsPerLanguageConsumer) {
        this.totalCommitsConsumer = totalCommitsConsumer;
        this.totalCommittersConsumer = totalCommittersConsumer;
        this.commitsPerLanguageConsumer = commitsPerLanguageConsumer;
    }


    public void writeMetricsToFile() throws IOException {
        Map<String, String> metrics = new HashMap<>();

        totalCommitsConsumer.poll(java.time.Duration.ofMillis(1000)).forEach(record -> metrics.put("total_commits", record.value()));
        totalCommittersConsumer.poll(java.time.Duration.ofMillis(1000)).forEach(record -> metrics.put("total_committers", record.value()));
        commitsPerLanguageConsumer.poll(java.time.Duration.ofMillis(1000)).forEach(record -> metrics.put(record.key(), record.value()));

        writeToJSON(metrics);
    }

    private void writeToJSON(Map<String, String> metrics) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(OUTPUT_FILE), metrics);
    }
}

