# KafkaInterview

**Project have two parts:**

**First one:** 

Read data from local file used Kafka Producer API, put data to "input-topic". Using Kafka Consumer API consume
the messages, parse it to "GitHubEvent" class, take massages only for last hours and put them to "output-topic". 
Put to output topics with "username" as a key of message. Create partition for "output-topic" due 
to ".username" possible values.

**Second one:**

Read data from the "output-topic" topic, calculate metrics and write these metrics into the file.
Exactly once semantics (Kafka Streams) should be achieved. 
The format of the destination file is JSON and will be stored in the local filesystem.

**Documentation**

This file contains all commands regard to start Kafka, create and manage topics. 
