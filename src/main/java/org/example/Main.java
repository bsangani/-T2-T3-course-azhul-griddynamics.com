package org.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class Main {

    @Autowired
    private MetricsFileWriter metricsFileWriter;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner run(GitHubEventProducer gitHubEventProducer) {
        return args -> {
            gitHubEventProducer.sendGitHubEventsFromFile();
            metricsFileWriter.writeMetricsToFile();
        };
    }
}