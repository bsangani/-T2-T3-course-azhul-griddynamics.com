package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.model.CommitMessage;
import org.example.model.GitHubEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@EnableKafka
public class GitHubEventConsumer {

    @Value("${kafka.output-topic}")
    private String outputTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public GitHubEventConsumer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.input-topic}", groupId = "my-consumer-group")
    public void consumeGitHubEvent(ConsumerRecord<String, String> record) {
        try {
            GitHubEvent event = objectMapper.readValue(record.value(), GitHubEvent.class);

            String username = event.getOwner().getLogin();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime commitTime = event.getCommit().getDate();
            Duration duration = Duration.between(commitTime, now);

            if (duration.toHours() <= Duration.ofHours(24L).toHours()) {
                CommitMessage commitMessage = new CommitMessage();
                commitMessage.setCommitId(event.getCommitId());
                commitMessage.setRepo(event.getRepo());
                commitMessage.setUsername(username);
                commitMessage.setCommitDate(event.getCommit().getDate());
                commitMessage.setLanguage(event.getLanguage());

                String message = objectMapper.writeValueAsString(commitMessage);
                // Send commit message to the output Kafka topic
                kafkaTemplate.send(outputTopic, event.getLanguage(), message);
            } else {
                System.out.println("Commit message is older than the specified interval. Skipping.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
