# Study Notes

## Topics, Partitions and Offsets

*  Topic is a stream of data, identified by name.
*  Can have multiple topics.
*  Each topic is split into partitions.
*  Partitions are ordered, with each message getting an incremental ID called offset.
*  Offsets only make sense for a particular partition.
*  i.e, topic one partition 0 offset 5 may happen after topic one partition 1 offset 5.
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

---

## Producer Settings

*  If you have replication.factor=3, min.insync.replicas=2, acks=all you can tolerate at most 1 broker going down, or you'll have an exception when producer sends.
*  retries setting sends data again upon transient failures (exceptions like above). retry.backoff.ms sets interval. delivery.timeout.ms sets maximum time for retries. 
*  If key based ordering is used, then retries can mess up the order of messages!

## Idempotent Producer

*  Normal case - Producer -> produce -> Kafka -> commit -> ack -> Producer
*  Now, if ack fails, Producer sends a duplicate message through retry, and Kafka commits it twice!
*  With Idempotent Producer, the produce request has an ID. Kafka sees that it's already got that id, so it sends back another ack, hopefully not failing this time.

## Message Compression

*  Compression batches many messages together into one. Smaller producer request size.
*  Better latency, throughput, disk utilization.
*  Producers and consumers must dedicate some CPU cycles to compression and decompression though. (minor)
*  By default, up to 5 messages at a time in flight, i.e can send 5 individual messages at a time. If there's more than five appearing to producer, then Kafka starts smartly batching the messages that are waiting.
*  At the cost of a small delay, linger.ms=5 waits around 5ms to batch together messages and increase throughput. Any message bigger than batch.size won't be batched and at that point Kafka will send forward.
*  snappy is a good compression algorithm for JSON/log text messages.

---

## Idempotent Consumer

*  Idempotent consumers overcome the duplicate message problem of at least once delivery semantics, i.e offsets are committed only after processing is done, if processing fails duplicate messages may be sent.
*  Now, if each message had a unique ID, we could store them at unique positions in a database, or do some unique process only for them, i.e processing a message twice would mean overwriting a duplicate at the same position etc. so the entire system isn't affected.


## Consumer Poll Behavior

*  Unlike other messenger buses, Kafka consumers poll the broker. Consumers keep asking the broker for data immediately if possible.
*  Some settings - fetch.min.bytes, max.poll.records, max.partitions.fetch.bytes, fetch.max.bytes.

## Offset Commits

*  Either offsets are committed at regular intervals and batches are synchronously processed.
*  Or a batch is built up, and when ready, manually committed.

## Controlling Consumer Liveliness

*  In addition to polling, consumers also talk to brokers in another way. Heartbeats are sent to one broker acting as a consumer coordinator.
*  If heartbeat stops, rebalancing would occur.
*  Additionally, polling must also be done regularly. max.poll.interval.ms is the maximum time after which no poll means consumer is dead.
*  If processing takes time like in Spark, Kafka might think the consumer is dead. So check this.

---

# Guidelines

*  Small cluster (<6 brokers) - 2 * #brokers partitions.
*  Big cluster (>12 brokers) - 1 * #brokers partitions.
*  Choosing number of partitions right is very important. While larger number of partitions are trending, too many, like 1000 for a topic is wasteful.
*  If we need many consumers, or producer sends a lot of data, we need to adjust the partitions, because clusters can have groups of at most the number of partitions.
*  Replication factor atleast 2, usually 3, sometimes 4, never 1. Need to consider the latency of acks and higher disk space.




