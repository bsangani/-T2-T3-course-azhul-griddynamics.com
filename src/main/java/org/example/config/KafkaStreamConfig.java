package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.example.model.CommitMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeMap;

@Configuration
@EnableKafkaStreams
public class KafkaStreamConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KafkaStreams kafkaStreams(StreamsBuilder streamsBuilder) throws Exception {
        KStream<String, String> commitStream = streamsBuilder.stream("output-topic");

        // Metric 1: Total number of commits (count of all commit messages)
        commitStream
                .mapValues(this::convertToCommitMessage)
                .groupByKey()
                .count()
                .toStream()
                .mapValues(count -> "Total Commits: " + count)
                .to("total-commits-output");

        // Metric 2: Total number of committers (unique committers name)
        commitStream
                .mapValues(this::convertToCommitMessage)
                .mapValues(CommitMessage::getUsername)  // Extract committer from CommitMessage
                .groupBy((key, name) -> name)
                .count()
                .toStream()
                .mapValues(count -> "Total Committers: " + count)
                .to("total-committers-output");

        // Metric 3: Total number of commits per programming language
        commitStream
                .mapValues(this::convertToCommitMessage)
                .mapValues(CommitMessage::getLanguage)
                .groupBy((key, language) -> language)
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
                .count()
                .toStream()
                .map((windowedLanguage, count) -> {
                    String language = windowedLanguage.key();
                    long startTime = windowedLanguage.window().start();
                    long endTime = windowedLanguage.window().end();
                    return new KeyValue<>(language + " [" + startTime + "-" + endTime + "]", "Commits: " + count);
                })
                .to("commits-per-language-output", Produced.with(Serdes.String(), Serdes.String()));


        // Metric 4: Top 5 committers
        commitStream
                .mapValues(this::convertToCommitMessage)
                .groupBy((key, commitMessage) -> commitMessage.getUsername())
                .count(Materialized.as("commit-count-store"))
                .toStream()
                .to("commit-count-output");

        KStream<String, Long> commitCountStream = streamsBuilder.stream("commit-count-output");

        commitCountStream
                .groupByKey()
                .aggregate(() -> new TreeMap<Long, String>(Comparator.reverseOrder()),
                        (aggKey, newValue, aggValue) -> {
                            String committer = aggKey;
                            Long commitCount = newValue;
                            aggValue.put(commitCount, committer);
                            if (aggValue.size() > 5) {
                                aggValue.pollLastEntry();
                            }
                            return aggValue;
                        }
                )
                .toStream()
                .map((aggKey, committersMap) -> {
                    String topCommittersMessage = "Top 5 committers: " +
                            String.join(", ", committersMap.values());
                    return new KeyValue<>("Top 5 Committers", topCommittersMessage);
                })
                .to("top-committers-output", Produced.with(Serdes.String(), Serdes.String()));

        Properties config = streamsConfig();
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        return new KafkaStreams(streamsBuilder.build(), config);

    }

    private Properties streamsConfig() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "commit-analyzer");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return config;
    }

    private CommitMessage convertToCommitMessage(String value) {
        try {
            return objectMapper.readValue(value, CommitMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
