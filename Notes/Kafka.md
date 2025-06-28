✅ 1. Kafka Basics
* What is Kafka and why it’s used (high-throughput, distributed, fault-tolerant messaging)
* Kafka vs JMS/RabbitMQ
* Core concepts:
* Producer, Consumer
* Broker, Topic, Partition
* Offset, Consumer Group
* Leader/Follower

⸻

✅ 2. Kafka Architecture
* Cluster and broker setup
* Topics and partitions
* Leader election and replication
* Zookeeper role (Kafka 2.x) and KRaft mode (Kafka 3.x+)
* High availability and fault tolerance

⸻

✅ 3. Kafka Producer API (Java)
* Creating Kafka producers
* ProducerRecord, KafkaProducer
* Acknowledgment modes (acks = 0, 1, all)
* Key serialization and partitioning logic
* Error handling and retries
* Idempotent producers

⸻

✅ 4. Kafka Consumer API (Java)
* Creating Kafka consumers
* KafkaConsumer, poll(), subscribe()
* Consumer groups and load balancing
* Manual vs auto offset commit
* Rebalancing and partition assignment strategies
* Consuming from a specific offset or timestamp
* Handling duplicates and idempotent processing

⸻

✅ 5. Message Serialization
* Kafka serializers/deserializers
* String, ByteArray
* JSON, Avro, Protobuf (Schema Registry)
* Custom serializers and deserializers

⸻

✅ 6. Kafka with Spring Boot
* Using Spring Kafka (spring-kafka)
* @KafkaListener, KafkaTemplate
* Listener container config (ConcurrentKafkaListenerContainerFactory)
* Error handling and retry logic
* Dead Letter Topics (DLTs)
* Producer and Consumer configuration

⸻

✅ 7. Kafka Streams API
* Stream processing directly on Kafka topics
* Stateless vs stateful transformations
* map, filter, join, windowed operations
* KStream, KTable, GlobalKTable
* Use cases vs Kafka Connect / external stream processors

⸻

✅ 8. Kafka Connect
* Moving data into/from Kafka using connectors (source/sink)
* JDBC, File, ElasticSearch, S3 connectors
* Custom connector creation
* Connect cluster configuration

⸻

✅ 9. Kafka Security
* SSL encryption (TLS)
* SASL Authentication (PLAIN, SCRAM)
* ACLs (Access Control Lists)
* Securing with tools like Kafka-ACLs, Kerberos (optional)

⸻

✅ 10. Monitoring and Metrics
* Kafka JMX metrics
* Tools: Prometheus + Grafana, Confluent Control Center
* Consumer lag monitoring
* Dead-letter queues

⸻

✅ 11. Error Handling & Retries
* Deserialization errors
* Retry strategies: fixed backoff, exponential backoff
* Dead-letter topics (DLTs)
* Poison pill messages

⸻

✅ 12. Performance Tuning
* Batching and compression
* Linger.ms, batch.size, buffer.memory
* fetch.min.bytes, fetch.max.wait.ms
* Parallelism in consumers
* Partitioning strategies

⸻

✅ 13. High Availability & Scalability
* Partitioning strategy and balancing
* Replication factor and ISR (In-Sync Replicas)
* Avoiding data loss (min.insync.replicas, acks=all)
* Multi-cluster architecture (MirrorMaker)

⸻

✅ 14. Schema Registry (with Avro/Protobuf)
* Storing message schemas centrally
* Evolving schemas with compatibility rules
* Strong typing in Kafka messages

⸻

✅ 15. Real-World Design Patterns
* Event Sourcing with Kafka
* CQRS using Kafka streams
* Saga Pattern with Kafka (choreography-based coordination)
* Idempotent consumers (exactly-once processing)
* Outbox pattern with DB-Kafka sync

⸻

✅ 16. Testing Kafka
* Embedded Kafka for unit/integration tests
* Kafka TestContainers
* Simulating partitions, lag, retries