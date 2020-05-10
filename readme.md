# Theory

## Topics, Partitions and Offsets

*  Topic is a stream of data, identified by name.
*  Can have multiple topics.
*  Each topic is split into partitions.
*  Partitions are ordered, with each message getting an incremental ID called offset.
*  Offsets only make sense for a particular partition.
*  i.e, topic a partition 0 offset 5 may happen after topic a partition 1 offset 5.
*  Data is only kept for a limited time.


## Brokers

*  Kafka cluster consists of brokers, which are like different servers.
*  Each broker has an ID, and contains certain partitions of topics.
*  If you connect to one broker, you can connect to any broker of the cluster.


## Topic Replication

*  Topics have replication factor >1 (2 to 3 is good)
*  i.e, if one partition for a topic breaks down, there's a duplicate on another broker.
*  At any time there's just one leader out of these replicated partition, which can recieve and serve data.
*  Rest are called ISR or in-sync replica.


## Producers

*  Producers send data to topics. Automatically knows which broker/partition to write to. So load balance is done for us.
*  Also automatically recover from failures.
*  Can choose to recieve acknowledgement of data writes -
   *  acks=0 - doesn't wait for acknowledgement. (possible data loss)
   *  acks=1 - waits for leader acknowledgement. (less data loss)
   *  acks=all - leader + ISR acknowledgement. (no data loss)
*  They can choose to send key with message -
   *  key=null the message sent roundrobin to brokers (1, 2, 3, 1, 2, ...)
   *  key=something else like ID, messages for key go to same partition through key hashing.


## Consumers

*  They read data from a topic. They are programmed to know which broker to read from.
*  If broker fails, they can handle recovery. 
*  Data is read in order within partitions.
*  Consumers can have their own groups - consumer groups.
*  Each consumer in the group reads from a particular partition.
*  If there's more consumers than partitions, some stay inactive. (Usually not done, but in case some consumers fail, the inactive takes over)

## Consumer Offsets

*  Kafka keeps track of till what offset a consumer group has read.
*  They're actually in a Kafka topic called __consumer_offsets.
*  When consumer has been fed data, it should be committing the offsets. If consumer dies and later restarts, it can use the information about offsets to start from there again.
*  When to commit offsets (delivery semantics) -
   *  At most once - soon as message received. If processing goes wrong message is lost, not read again.
   *  At least once(preferred) - After the message is received and processed. Can result in duplicate processing, so have to make sure processing is idempotent. (reprocessing won't affect the system)
   *  Exactly once - Kafka to Kafka using Streams API or Kafka to External using idempotent consumer.

## Zookeeper

*  Zookeper manages and keeps a list of brokers.
*  Performs leader election for partitions.
*  Lets Kafka know about changes (new topic, broker down etc.)
*  Zookeeper has odd number of servers. One leader server manages writes, rest reads.
*  Zookeeper does not keep information about offsets. That is up to Kafka.


## Kafka Guarantees

*  Messages are appended to topic-partition in order they are sent.
*  Consumers read them in order they're stored.
*  If replication factor is N, producers and consumers can handle N-1 brokers being down.
*  That's why 3 is a good idea, one broker can be down and another for maintenance and things will be OK.
*  As long as no. of partitions is constant, same message key goes to same partition through hashing.

# Kafka CLI

## Startup

```
cd C:\kafka_2.12-2.5.0
zookeeper-server-start.bat config\zookeeper.properties

cd C:\kafka_2.12-2.5.0
kafka-server-start.bat config\server.properties
```

## 
