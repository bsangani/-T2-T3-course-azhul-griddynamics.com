# KafkaInterview

**Project have two parts:**

**First one:** 

Read data from local file used Kafka Producer API, put data to "input-topic". Using Kafka Consumer API consume
the messages, parse it to "GitHubEvent" class, take massages only for last 3h and put them to "output-topic". 
Put to output topics with "GitHubEvent.username" as a key of message. Create partition for "output-topic" due 
to "GitHubEvent.username" possible values.

**Second one:**

Read data from the "output-topic" topic, calculate metrics and write these metrics into the file.
Exactly once semantics (Kafka Streams) should be achieved. 
The format of the destination file can be CSV or JSON and can be stored in the local filesystem.

**Documentation**

This file contains all commands regard to start Kafka, create and manage topics. 

**ToDo**

- Use multi-broker Kafka configuration.
- Test a few acknowledge modes, Replication facto 2.
- Implement Kafka messages analyzer, using Kafka Streams and Kafka consumer API.
Metrics: Total number of commits. Total number of committers. Total number of commits for each programming language.
- Your project should include at least 1 integration end2end test and unit tests for common classes.(Use embedded Kafka)
