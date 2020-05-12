# Kafka CLI

## Startup

```
zkserver

cd C:\kafka_2.12-2.5.0
kafka-server-start.bat config\server.properties
```

By default -
*  Zookeeper runs on port 2181.
*  Kafka runs on port 9092.

## kafka-topics

```
kafka-topics --bootstrap-server localhost:9092 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics --bootstrap-server localhost:9092 --list
first_topic

kafka-topics --bootstrap-server localhost:9092 --topic first_topic --describe
Topic: first_topic      PartitionCount: 3       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: first_topic      Partition: 0    Leader: 0       Replicas: 0     Isr: 0
        Topic: first_topic      Partition: 1    Leader: 0       Replicas: 0     Isr: 0
        Topic: first_topic      Partition: 2    Leader: 0       Replicas: 0     Isr: 0

#Broken on Windows!
kafka-topics --bootstrap-server localhost:9092 --topic first_topic --delete

```

## kafka-console-producer

```
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic
>Hi, I'm Avi,
>and I'm learning Apache Kafka!
>I've set up a producer from the CLI, so
>all set to move on!

kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all
>Remember how
>we learned about acknowledgements
>back in theory?

```

* If we make a producer produce to a topic that does not already exist, then it will initialize that topic with some undesirable defaults.
* To edit these defaults, make the changes in Log Basics of config/server.properties.


# kafka-console-consumer

```
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic
#This won't show the messages already sent!
#Only shows new messages in real time.

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning
Hi, I'm Avi,
I've set up a producer from the CLI, so
Remember how
and I'm learning Apache Kafka!
all set to move on!
we learned about acknowledgements
back in theory?
#The order is not total, it is per partition.
#As Kafka does not guarantee order for different partititons.

```

# Consumers in Groups

```
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application --from-beginning
Hi, I'm Avi,
I've set up a producer from the CLI, so
Remember how
and I'm learning Apache Kafka!
all set to move on!
we learned about acknowledgements
back in theory?
# Multiple of these consumers can run in a group.
# They will dynamically adjust if some of them are shut down.

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-application --from-beginning
#No more output.
#This is because of how Kafka's offsets have been committed.

```

# kafka-consumer-groups

```
kafka-consumer-groups --bootstrap-server localhost:9092 --list
my-first-application

kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application
Consumer group 'my-first-application' has no active members.

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
my-first-application first_topic     0          3               3               0               -               -               -
my-first-application first_topic     1          3               3               0               -               -               -
my-first-application first_topic     2          1               1               0               -               -               -


kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --to-earliest --execute --topic first_topic

GROUP                          TOPIC                          PARTITION  NEW-OFFSET
my-first-application           first_topic                    0          0
my-first-application           first_topic                    1          0
my-first-application           first_topic                    2          0


kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-application
Consumer group 'my-first-application' has no active members.

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
my-first-application first_topic     0          0               3               3               -               -               -
my-first-application first_topic     1          0               3               3               -               -               -
my-first-application first_topic     2          0               1               1               -               -               -


kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-application --reset-offsets --shift-by 1 --execute --topic first_topic

GROUP                          TOPIC                          PARTITION  NEW-OFFSET
my-first-application           first_topic                    0          1
my-first-application           first_topic                    1          1
my-first-application           first_topic                    2          1


```

## Key-Values

```
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
> key,value
> another key,another value

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,

```



