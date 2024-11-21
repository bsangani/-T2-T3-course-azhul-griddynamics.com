package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.model.GitHubEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class GitHubEventProducer {
    @Value("${kafka.input-topic}")
    private String inputTopic;

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public GitHubEventProducer(KafkaProducer<String, String> producer, ObjectMapper objectMapper) {
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    public void sendGitHubEventsFromFile() throws IOException {
        List<String> lines = Files.readAllLines(new File("github_events.json").toPath());
        for (String line : lines) {
            try {
                GitHubEvent event = objectMapper.readValue(line, GitHubEvent.class);
                String message = objectMapper.writeValueAsString(event);
                producer.send(new ProducerRecord<>(inputTopic, event.getRepo(), message));
                System.out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
