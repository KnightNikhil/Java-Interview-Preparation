# Kafka Production Guide — Complete Reference

Table of Contents
1. Summary & Mental Model
2. Partitioning and Ordering
3. Production-Grade Issues (Overview)
4. Deep Dives: Common Failure Modes and Solutions
   - 4.1 DB Updated but Event Not Published
   - 4.2 Duplicate Events (At-Least-Once Delivery)
   - 4.3 Consumer Lag
   - 4.4 Ordering Breaks
   - 4.5 Hot Partitions
   - 4.6 Poison Messages
   - 4.7 Schema Evolution Failures
   - 4.8 Rebalance Storms
   - 4.9 No Backpressure
   - 4.10 Security Gaps
   - 4.11 Monitoring Blindness
   - 4.12 Large Message Payloads
   - 4.13 Event Misuse
   - 4.14 Eventual Consistency Confusion
5. What This Means for You (Checklist)
6. Next Steps (Options)
7. Mock Interview: Kafka (Scenario-Based + Follow-ups)
   - Q1 Message Loss in Production
   - Q2 Duplicate Processing
   - Q3 Ordering Issue
   - Q4 Consumer Lag
   - Q5 Reprocessing Data
   - Q6 DB + Kafka Consistency
   - Q7 Broker Failure
   - Q8 Partition Increase
   - Q9 Retry & DLQ Design
   - Q10 Exactly Once Processing
   - Rapid Fire
8. Hands-on: Spring Boot + Kafka Examples
   - Producer (Reliable)
   - Consumer (Manual Offset Commit)
   - Retry + Dead Letter Queue (DLQ)
   - Idempotent Consumer
   - Outbox Pattern (Transactional)
   - High Throughput Optimization
   - Parallel Processing Consumer
9. Interview Traps & Common Gotchas
10. Outbox Pattern & CDC with Debezium (In Depth)
    - Why Outbox Exists
    - Step-by-Step Implementation (Spring Boot)
    - CDC + Debezium
11. Debezium Setup (High-Level)
12. Failure Scenarios, Trade-offs, and When Not to Use CDC
13. Final Perfect Interview Answer (Short)
14. Appendix: Short Tips & Configs

---

#  Kafka Basics

## What is Kafka?
Apache Kafka is a distributed event streaming platform used for building real-time data pipelines and streaming applications. It is highly scalable, fault-tolerant, and designed for high-throughput data ingestion and processing.

### Why Kafka is Used?
- **High Throughput**: Capable of handling millions of messages per second.
- **Distributed**: Scales horizontally and handles data replication.
- **Fault-Tolerant**: Automatically recovers from failures.
- **Durable**: Messages are stored on disk and replicated across brokers.
    - When we say “disk” in Kafka (or any computing system), we mean persistent storage hardware (HDD/SSD) — not RAM, where data is physically stored so it survives system restarts.
- **Real-Time Processing**: Ideal for time-sensitive data delivery and streaming.

---

## Kafka vs JMS / RabbitMQ

| Feature                | Kafka                          | JMS / RabbitMQ                |
|------------------------|--------------------------------|-------------------------------|
| Messaging Model        | Log-based publish-subscribe    | Message queue (push model)    |
| Performance            | High throughput & low latency  | Lower throughput              |
| Persistence            | Persistent with disk storage   | Mostly in-memory              |
| Scalability            | Highly scalable horizontally   | Limited scalability           |
| Replayability          | Yes (via offset)               | No replay once consumed       |
| Consumer Model         | Pull-based                     | Push-based                    |


### Important Concepts:

#### Append-only log

- An append-only log is a data storage model where new data is only ever appended to the end of a file (or log), never overwritten or deleted immediately.

- Think of it like a ledger or journal:
    - You add new entries at the end.
    - Old entries remain intact until they expire based on retention policy.

- Kafka can ingest millions of trades per second because it never rewrites old trades.
- Consumers (microservices) can reprocess trade data by resetting offsets → useful for risk analysis, debugging, or reconciliation.

- **Why Append-Only Log Makes Kafka Fast**
    -	**No random disk writes** → only sequential writes.
    -	**No locking** → consumers read independently.
    -	**Batch-friendly** → Kafka writes batches in one append operation.
    -	**Efficient replication** → brokers replicate segments without reordering.

#### Kafka Scales Horizontally

- Horizontal scaling means: You add more servers (brokers) to the Kafka cluster to increase capacity — both in terms of throughput and storage — instead of making a single server more powerful.

- This is different from vertical scaling, where you add CPU, RAM, or disk to a single machine.

- Kafka is designed for horizontal scalability because of:
    - Partitioning of topics.
    - Replication across brokers.
    - Distributed processing.

#### Why Kafka Needs Data Replication

- Kafka is designed to be:
    - Highly available (no downtime if a broker fails).
    - Fault tolerant (data is not lost if hardware crashes).

- Replication ensures:
    - Copies of data are stored on multiple brokers.
    - If one broker goes down → another broker still has the data.
    - Consumers can continue reading without interruption.

- Each partition has 1 leader broker and 1 or more replicas.
- Leader handles all reads/writes for that partition.
- Replicas store identical copies.

#### Disk Performance in Kafka

Traditionally disks are slower than memory — but Kafka avoids this problem by:
- **Writing sequentially** → disks are fast at sequential writes(adding a new entry to the end of a journal without moving anything else.).
    - **Why Sequential Writes Are Fast**  - Disk performance depends heavily on how data is written.
        - Sequential writes minimize seek time and rotational latency.
        - Kafka writes data in large batches to maximize throughput.

- **Batching messages** → reduces disk I/O overhead.
    - Batching means grouping multiple messages together before sending them to Kafka, rather than sending each message individually.
    - A batch is simply a bundle of messages sent together in one network request.
    - Without batching:
        - Each message = one network call → high latency and CPU overhead.
    - With batching:
        - One request carries many messages → fewer network round trips → higher throughput.

- **Using OS page cache** → frequently read data stays in memory.

- This means Kafka can treat disk storage almost like memory for streaming workloads.

Batching is one of the biggest reasons Kafka can handle massive traffic.
Let’s explain it from first principles, then go deeper into producer, broker, consumer, trade-offs, and failures.

⸻

**What “Batching” Really Means in Kafka**

Kafka does not send or store messages one-by-one.
It groups multiple messages together into a batch and treats them as a unit.

This happens at multiple levels:
-	Producer side
-	Network
-	Broker storage
-	Consumer fetch

⸻

**Why Batching Is Needed (Core Reason)**

Sending 1 message at a time means:
-	Network call per message
-	Disk write per message
-	Syscall per message

That kills throughput.

Batching converts:

`1000 messages × 1000 syscalls`

into:

`1 batch × 1 syscall`

That’s the real win.

⸻

**Producer-Side Batching (MOST IMPORTANT)**

Producer flow (simplified)
```
Application
    ↓
Producer Buffer
    ↓
Batch per partition
    ↓
Send to broker
```
Kafka producer buffers messages in memory and sends them in batches.


**How Producer Forms Batches**

Kafka batches per partition.

If you send:
```java
producer.send("orders", key1, msg1);
producer.send("orders", key1, msg2);
producer.send("orders", key1, msg3);
```


All go to:
```
orders-3 partition
```


They become:
```
Batch {
msg1,
msg2,
msg3
}
```
**Note -** Messages with different keys → different partitions → different batches

⸻

**Producer Configs That Control Batching**

- batch.size

Default: 16 KB

Max size of a batch per partition.
-	Larger batch → better throughput
-	Smaller batch → lower latency

⸻

- linger.ms (VERY IMPORTANT)

Default: 0 ms

“How long should the producer wait to fill a batch before sending?”

Example:

`linger.ms = 5`

- Producer waits up to 5 ms to collect more messages.

- This is intentional delay to improve batching.

⸻

- buffer.memory

Total memory for all producer batches.

If full:
-	Producer blocks
-	Or throws exception

⸻

**Example Timeline (Concrete)**

Assume:
```
batch.size = 32 KB
linger.ms = 10 ms
```


Timeline:
```text
T0: msg1 arrives
T1: msg2 arrives
T2: msg3 arrives
...
T8: batch fills to 32 KB → send immediately
OR
T10: linger timeout → send whatever collected

```

⸻

**Broker-Side Batching (Disk Efficiency)**

Kafka stores data as:

Log Segment
├── Batch 1
├── Batch 2
├── Batch 3

Each batch:
-	Written sequentially
-	Compressed together
-	Indexed once

This makes Kafka:
-	Disk-friendly
-	Cache-friendly
-	Extremely fast

⸻

7️⃣ Compression Happens at Batch Level

Kafka compresses batches, not individual messages.

Supported:
-	Snappy
-	LZ4
-	ZSTD
-	GZIP

Why batch compression matters:
-	Better compression ratio
-	Lower CPU
-	Less network traffic

⸻

8️⃣ Consumer-Side Batching

Consumers don’t fetch 1 message either.

They fetch:

A chunk of log data (multiple batches)

Controlled by:
-	fetch.min.bytes
-	fetch.max.bytes
-	fetch.max.wait.ms

Consumer loop:

poll()
→ returns many records
→ you iterate


⸻

9️⃣ Batching + Ordering Guarantee

Important rule:

Kafka preserves ordering within a partition, even with batching.

Batch does NOT break ordering.

msg1 → msg2 → msg3

Still delivered in order.

⸻

🔟 What Happens If Producer Crashes Mid-Batch?
-	Messages still in buffer → lost
-	Messages sent but not acked → may be retried

This is why:
-	acks=all
-	Idempotent producer

are critical for reliability.

⸻

11️⃣ Batching vs Latency Tradeoff (Very Important)

Use Case	Batch Size	Linger
Payments	Small	Very low
Logs	Large	Higher
Analytics	Very large	High
Notifications	Medium	Medium

Kafka always trades:

Latency vs Throughput

⸻

1 Real High-Traffic Example (Flash Sale)

Without batching:
-	50k orders/sec
-	50k network calls
-	Broker melts

With batching:
-	50k orders/sec
-	~500 batches/sec
-	Stable system

This is why Kafka survives traffic spikes.

⸻

13️⃣ Common Mistakes with Batching

❌ linger.ms = 0 everywhere
❌ Tiny batch.size
❌ Too many partitions (reduces batch fill rate)
❌ Using random keys (breaks batching)

⸻

14️⃣ Mental Model (Lock This In)

Think of Kafka batching like:
-	Messages = letters
-	Batch = envelope
-	Broker = post office

Sending letters one-by-one is slow.
Sending envelopes is fast.

⸻

15️⃣ Interview-Grade Summary

“Kafka achieves high throughput by batching messages per partition at the producer, storing them as compressed log batches on disk, and fetching them in chunks at the consumer. Batching reduces network calls, disk I/O, and CPU overhead while preserving ordering within partitions.”

⸻

## but, while waiting for the time to create a batch, if we dont, wait few message would have been already delievered?

Excellent question. This is exactly the right confusion, and clearing this gives you a true Kafka-level understanding.

Short answer first, then deep dive:

Yes — some messages could be delivered earlier, but Kafka intentionally waits to batch because the throughput gain is worth the tiny latency cost.
And Kafka does not block already-sent messages.

Now let’s break this properly.

⸻

1️⃣ Where batching actually happens (critical)

Batching happens on the PRODUCER side, before the message is sent to Kafka.

Your App
↓
Producer Buffer (batch)
↓
Network call
↓
Kafka Broker

So the question becomes:

“Why not send immediately instead of waiting?”

⸻

What actually happens when batching is enabled

Kafka producer does not say:

“Wait and block everything”

Instead, it says:

“Send when ANY of these conditions is met”

Producer sends a batch when:
1.	Batch is full (batch.size)
2.	Time limit reached (linger.ms)
3.	Buffer pressure (memory is filling)
4.	Flush / close is called

So messages are not stuck unnecessarily.

⸻

3️⃣ Your exact question, rephrased correctly

“While waiting to form a batch, couldn’t earlier messages already be delivered?”

Answer:

Yes — but only if batching is disabled or linger.ms = 0.

When batching is enabled:
-	Kafka intentionally delays sending
-	To aggregate more messages
-	To reduce network & disk overhead

This delay is measured in milliseconds, not seconds.

⸻

4️⃣ Why Kafka chooses to wait (this is the key insight)

Let’s compare:

❌ Immediate send (no batching)

Message 1 → network call
Message 2 → network call
Message 3 → network call

Problems:
-	TCP overhead per message
-	Syscall per message
-	Disk write per message
-	Context switching

Result:
-	Low throughput
-	High CPU usage
-	Broker overload

⸻

✅ Batched send

Wait 5 ms
Collect 200 messages
Send once

Benefits:
-	1 network call
-	Sequential disk write
-	Fewer syscalls
-	OS page cache efficient

Result:
-	Massive throughput improvement

⸻

5️⃣ But doesn’t waiting increase latency?

Yes — slightly.

Example:
-	linger.ms = 5
-	Worst case latency increase = 5 ms

In return:
-	Throughput ↑ 10–100×
-	Broker stability ↑
-	Cost ↓

This is a deliberate trade-off.

⸻

6️⃣ Important nuance (very important)

Kafka does NOT delay already-sent messages.

Once a batch is sent:
-	It is immediately available to consumers
-	Consumers can read it even while producer is batching new messages

So batching:
-	Does NOT block consumption
-	Does NOT block other partitions
-	Only affects when producer sends

⸻

7️⃣ Real-world traffic scenario (high traffic)

Let’s say:
-	Traffic = 50k msgs/sec
-	linger.ms = 5

In 5 ms:

≈ 250 messages collected

Producer sends almost continuously.

So in reality:
-	Producer rarely waits full linger.ms
-	Batch fills quickly
-	Latency impact is near zero

⸻

8️⃣ Low traffic scenario (important edge case)

Traffic = 5 msgs/sec
linger.ms = 5

Here:
-	Producer will wait full 5 ms
-	Latency noticeable

Solution:
-	Lower linger.ms
-	Or disable batching for that topic

Kafka allows per-producer tuning.

⸻

9️⃣ Why Kafka doesn’t “send immediately and also batch”

This sounds logical but is inefficient:
-	First message alone = small packet → wasted call
-	Next batch = extra call

This doubles overhead.

Kafka prefers:

Send once, send efficiently

⸻

🔟 Mental model (remember this)

Kafka batching is like an elevator:
It waits a moment for more people because running half-empty is wasteful.

⸻

11️⃣ Interview-ready answer

If asked:

“Does batching delay messages unnecessarily?”

Answer:

“Kafka batching introduces a small, configurable delay to aggregate messages. This delay is bounded by linger.ms and is usually negligible under high traffic, while the throughput and resource efficiency gains are significant.”

⸻

1 Final takeaway
-	Yes, messages could be sent earlier
-	Kafka chooses not to, on purpose
-	Delay is tiny and bounded
-	Throughput gains are huge
-	Under high traffic, delay ≈ 0

⸻

Next logical topics you might want:
-	Producer buffer & backpressure
-	acks=0/1/all and latency impact
-	Exactly-once vs batching
-	Why batching + retries is tricky
-	Consumer-side fetch batching

Just tell me what to go into next.

---

## Core Concepts

### Producer
A **producer** sends messages (records) to Kafka **topics**.

```java
ProducerRecord<String, String> record = new ProducerRecord<>("topic-name", "key", "value");
producer.send(record);
```

### Consumer
A **consumer** reads messages from topics. Consumers can belong to **consumer groups**.

```java
consumer.subscribe(Collections.singletonList("topic-name"));
ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
```

### Consumer Group
A **consumer group** is a set of consumers that work together to consume data from a topic. Kafka ensures that each partition is consumed by only one consumer in the group.


**How Consumers Read from Partitions**
- A Kafka consumer group assigns partitions to consumers:
-	Each consumer reads one or more partitions.
-	Inside each partition → order is preserved.
-	Across partitions → processing is parallel → order can vary.

- Example:
```
Consumer group "risk-service"
Consumer 1 → Partition 0
Consumer 2 → Partition 1
```
They read in parallel → no global ordering.

### Broker
A Kafka Broker is a single Kafka server instance that:
-	Stores data (partitions)
-	Serves client requests (produce & consume)
-	Participates in replication
-	Takes part in leader election
-	Communicates with other brokers in the cluster

### Topic
- A **topic** is a named stream of records. Producers write to topics, and consumers read from them.
- A category to which records are sent by producers.

### Partition
Each topic is split into **partitions** to allow for parallelism and scalability.

### Offset
- Each record within a partition has a unique **offset** that identifies it. Consumers use offsets to keep track of what has been read.
- Offset is assigned by the broker, not the producer

**What Is Offset Commit?**

Offset commit is the act of persisting the consumer’s read position so that:
-	On restart
-	On rebalance
-	On failure

…the consumer can resume from the correct place.

Important: Kafka commits the next offset, not the last processed offset.

**Where Are Offsets Stored?**

Internal Topic: __consumer_offsets
-	Offsets are stored in a Kafka topic
-	Topic name: __consumer_offsets
-	Partitions are spread across brokers
-	Replication applies like any other topic

Stored data includes:
-	Consumer group ID
-	Topic name
-	Partition number
-	Committed offset
-	Metadata (optional)
-	Timestamp

Key insight: Offset commits are just Kafka messages written to an internal topic.


Below is a clear, risk-focused explanation of Auto Commit vs Manual Offset Commit, using timeline / timestamp-based examples to show exactly where things go wrong. This is one of the most important Kafka concepts for interviews and production safety.

⸻

1. Auto Offset Commit

What is Auto Commit?

In auto commit, Kafka automatically commits offsets at a fixed interval, independent of your message processing logic.

Configuration

enable.auto.commit=true
auto.commit.interval.ms=3000

Meaning:
•	Every 3 seconds, Kafka commits the latest polled offset
•	Kafka does not know whether your code has finished processing

⸻

Auto Commit Timeline Example (At-Most-Once Risk)

Scenario
•	Topic: orders
•	Partition: orders-0
•	Messages: offsets 100, 101, 102
•	Auto commit interval: 3 seconds

⸻

Timeline

T0  (10:00:00) → consumer.poll()
fetched offsets 100, 101, 102

T1  (10:00:01) → processing offset 100
T2  (10:00:02) → processing offset 101

T3  (10:00:03) → AUTO COMMIT happens
committed offset = 103

T4  (10:00:04) → application CRASHES


⸻

What Happens After Restart?
•	Kafka sees committed offset = 103
•	Consumer resumes from 103
•	Offset 102 was never processed

Risk

❌ Message loss

Delivery Guarantee

At-most-once

⸻

Why Auto Commit Is Dangerous
•	Commit happens even if processing fails
•	No coordination with business logic
•	Crash between commit and processing = data loss
•	Debugging is hard in production

⸻

2. Manual Offset Commit

What is Manual Commit?

In manual commit, your application decides when offsets are committed, usually after successful processing.

Configuration

enable.auto.commit=false


⸻

Manual Commit Timeline Example (At-Least-Once Risk)

Scenario
•	Topic: payments
•	Partition: payments-0
•	Messages: offsets 200, 201, 202

⸻

Timeline

T0  (11:00:00) → consumer.poll()
fetched offsets 200, 201, 202

T1  (11:00:01) → processing offset 200
T2  (11:00:02) → processing offset 201
T3  (11:00:03) → processing offset 202

T4  (11:00:04) → application CRASHES
(before commitSync)


⸻

What Happens After Restart?
•	No offset was committed
•	Kafka resumes from offset 200
•	Offsets 200, 201, 202 are reprocessed

Risk

⚠️ Duplicate processing

Delivery Guarantee

At-least-once

⸻

Why Manual Commit Is Safer
•	No data loss
•	You control commit timing
•	Works well with retries and idempotency
•	Preferred for financial / banking systems

⸻

3. Visual Comparison (Timestamp View)

Time	Auto Commit	Manual Commit
10:00:00	poll records	poll records
10:00:02	processing	processing
10:00:03	offset committed	not committed
10:00:04	crash	crash
Restart	skips messages	reprocesses messages
Risk	data loss	duplicates


⸻

4. Commit Position Detail (Important Interview Point)

Kafka commits the next offset.

Example:
•	Processed offset 101
•	Committed offset = 102

Meaning:

“I am done with everything before 102”

⸻

5. commitSync vs commitAsync (Manual Commit)

commitSync()
•	Blocking
•	Guarantees commit success
•	Slower
•	Safer

commitAsync()
•	Non-blocking
•	Faster
•	May fail silently
•	Needs callback handling

Best practice:
•	Use commitAsync during normal flow
•	Use commitSync on shutdown or rebalance

⸻

6. Auto Commit vs Manual Commit — Summary Table

Aspect	Auto Commit	Manual Commit
Control	Kafka	Application
Commit timing	Time-based	Logic-based
Risk	Data loss	Duplicates
Delivery	At-most-once	At-least-once
Production use	❌ Avoid	✅ Recommended
Debuggability	Poor	Good


⸻

7. How Exactly-Once Solves Both Risks

Kafka Exactly-Once Semantics (EOS):
•	Combines message processing + offset commit
•	Uses transactions
•	No data loss
•	No duplicates

But:
•	Higher complexity
•	Not always required

⸻

8. Interview One-Liner

Auto commit can lead to message loss because offsets may be committed before processing completes, whereas manual commit gives at-least-once delivery by committing offsets only after successful processing, trading data loss for possible duplicate processing.

⸻

If you want, next I can explain:
•	Why Kafka commits the next offset (off-by-one confusion)
•	How to design idempotent consumers
•	Exactly-once with timeline example
•	Rebalance + offset commit edge cases

Just tell me what to cover next.

### Leader/Follower
- Each partition has a **leader** (handling reads/writes) and zero or more **followers** (replicas for redundancy).
- **Followers** replicate the leader’s data and take over if the leader fails.

---

## Follow-Up Interview Questions

### Q: What happens when a consumer fails?
**A:** Kafka will reassign the partitions that were assigned to the failed consumer to other active consumers in the group.

### Q: How does Kafka handle backpressure?
**A:** Kafka allows consumers to pull messages at their own pace and provides offset management to avoid overload.

### Q: Can messages be reprocessed?
**A:** Yes, by resetting the consumer offset to an earlier position or to the beginning of the partition.

---


###  2. Kafka Architecture

####  Cluster and Broker Setup

- **Kafka Cluster**: A Kafka cluster consists of multiple **brokers**, each running on a separate machine (or container).
- **Broker**: A Kafka broker is a single Kafka server that handles read/write requests from clients (producers and consumers), manages topic partitions, and persists data.

```
Kafka Cluster
 ├── Broker 1
 ├── Broker 2
 └── Broker 3
```

- Topics are abstractions. Brokers store partitions of topics.
- Each broker has a unique ID and is responsible for one or more **partitions** of a topic.
- Kafka scales horizontally: you add more brokers to handle more load.

---

####  Topics and Partitions

- **Topic**: A category to which records are sent by producers. This does not store data, no physical presence only abstraction, this helps us keep category of data together, data is stored in partitions.
- **Partition**:
    - A sequence of messages ordered by offset.
    - Stored as a log file on disk.
    - Associated with an index file for fast lookups.

Partition structure:
```
Partition log → [Message 0] → [Message 1] → [Message 2] → ...
```
Each message in the partition has:
- An offset → unique sequential ID within the partition.
- A key (optional) → used for partitioning and ordering.
- A value → the actual message data.
- Metadata (timestamp, headers).


- Topics are split into partitions to allow parallel processing and scalability.

**How it works:**
1.	A topic is created with:
      -	Number of partitions
      -   Replication factor
2.	Each partition is assigned to multiple brokers
3.	One broker becomes the leader for that partition
4.	Other brokers host replicas

- **Note:** In Kafka, partitions of a single topic are deliberately spread across different brokers to maximize throughput, balance load, and ensure fault tolerance.

```
Topic: trades
Partitions: 6
Brokers: 3
```
Kafka might distribute partitions like this:
```
Broker 1 → Partition 0, Partition 3  
Broker 2 → Partition 1, Partition 4  
Broker 3 → Partition 2, Partition 5
```
So, different partitions of the same topic live on different brokers.

- With replication
```
Topic: trades
Partitions: P0, P1, P2, P3, P4, P5

Broker 1 → P0 (Leader), P3 (Leader), P1 (Follower)
Broker 2 → P1 (Leader), P4 (Leader), P0 (Follower)
Broker 3 → P2 (Leader), P5 (Leader), P4 (Follower)```
```
Each broker hosts different partitions for different topics → maximum parallelism.

- Each message within a partition has an **offset**, a unique sequence number.
- **Partitioning** allows:
    - Horizontal scaling
    - Load distribution
    - Message ordering per partition

#### Q. What if there are multiple topics, how will the structure look like then?
- When there are multiple topics, each topic is split into partitions and those partitions are distributed and replicated across brokers,
- so every broker ends up hosting partitions from multiple topics while each topic spans multiple brokers.
- Basically, every broker can have partitions for all the topics(based on replication factor, no of broker and partitions per topic etc).

#### Q. If partitions are in diff broker then how are they sequential?
- When we say Kafka partitions are sequential, we mean within each partition, not across partitions.
- If partitions are spread across brokers → global ordering is not guaranteed. Only per-partition ordering is guaranteed means messages within a single partition are strictly ordered by offset.

- Example:
```
Topic: trades
Partitions: 2
```
Broker distribution:
```
Partition 0 → Broker 1  
Partition 1 → Broker 2
```
If producer sends:
```
Trade A (Partition 0)  
Trade B (Partition 1)  
Trade C (Partition 0)  
Trade D (Partition 1)
```
The offset order inside each partition:
```
Partition 0 (Broker 1): Trade A → Trade C  
Partition 1 (Broker 2): Trade B → Trade D
```
But globally → there is no single sequential order across partitions.

---

####  Leader Election and Replication

- **Replication** ensures **fault tolerance**.
- Each partition has:
    - One **leader**: handles all read/write for that partition.
    - Zero or more **followers**: replicate the leader's data.

```
Partition 0:
 - Leader: Broker 1
 - Follower: Broker 2
```

- If a broker with the leader partition fails, a follower is elected as the new leader by **Apache ZooKeeper** (or KRaft in Kafka 3+).

**Replication Flow**
1.	Producer sends data to the leader of a partition.
2.	Leader writes data to its log (append-only).
3.	Followers pull data from the leader asynchronously.
4.	Once followers replicate, they become in-sync replicas (ISR).

- Replication is **asynchronous** by default — leader doesn’t wait for all replicas unless configured (controlled by acks setting).
- When Kafka says replication is asynchronous, it means:
    - The leader broker of a partition does not wait for follower replicas to confirm that they have written a message before acknowledging the producer.
    - The producer gets acknowledgment as soon as the leader writes the data to its own log (depending on acks setting).
    - This is different from synchronous replication (e.g., traditional relational databases) where every replica must confirm before a write is considered complete.

- **How the acks setting changes replication behavior**

| Settings | Behavior                              | Safety                                                              |
|----------|---------------------------------------|---------------------------------------------------------------------|
| acks=0   | No acknowledgment; fire and forget    | Fast but unsafe (data loss possible)                                |
| acks=1   | Leader acknowledges receipt           | Safe if leader is reliable; risk if leader fails before replication |
| acks=all | All in-sync replicas must acknowledge | Safest; ensures data is replicated before confirming                |

- **Why Asynchronous Replication is the Default**

- Kafka is designed for high throughput and low latency:
- Waiting for all replicas (synchronous) slows down throughput — each write must wait for network round trips.
- Asynchronous replication allows Kafka to write quickly while still replicating in the background.

- This works because:
- Kafka still keeps replication fast enough that followers catch up quickly.
- Failover to a replica (if leader fails) is supported without noticeable downtime.
- Producers can trade durability for speed using acks.

- **How to Achieve Synchronous-like Replication**
```text
acks=all
min.insync.replicas=2
```
- acks=all: leader waits for acknowledgment from all in-sync replicas before confirming to producer.
- min.insync.replicas=2: requires at least two replicas (including leader) to acknowledge → protects against leader failure without losing data.

---

####  Zookeeper Role (Kafka 2.x)

- Kafka uses **ZooKeeper** for:
    - Cluster coordination
    - Leader election
    - Metadata management

- Kafka nodes register with ZooKeeper, and ZooKeeper tracks broker health and controls topic-partition assignments.

---

####  KRaft Mode (Kafka 3.x+)

- Kafka now supports **KRaft (Kafka Raft Metadata mode)** to eliminate ZooKeeper.
- Features of KRaft:
    - Built-in consensus mechanism (Raft protocol)
    - Simplified deployment (no ZooKeeper)
    - Better performance and scalability

```
Kafka KRaft Mode
 ├── Controller quorum (Raft)
 └── Brokers
```

---

####  High Availability and Fault Tolerance

Kafka ensures high availability by:
- **Replicating partitions** across multiple brokers
- Using **acks** to control delivery guarantees:
    - `acks=0`: no guarantee
    - `acks=1`: leader acknowledgment
    - `acks=all`: leader + all ISR (in-sync replicas) acknowledge

- **Consumer group rebalance** on failure
- **Retention policies** ensure data durability

---

###  Sample Interview Questions and Answers

---

**Q1. What happens when the leader of a partition goes down?**  
**A:** Kafka triggers a **leader election** process via ZooKeeper (or KRaft), and one of the in-sync followers becomes the new leader.

---

**Q2. How does Kafka ensure fault tolerance?**  
**A:** By **replicating partitions**, using **acks**, and **leader-follower** election. Consumers and producers can resume from the last committed **offset** in case of failure.

---

**Q3. What is the role of ZooKeeper in Kafka?**  
**A:** In Kafka 2.x, ZooKeeper manages broker metadata, leader elections, and configuration. From Kafka 3.x onward, ZooKeeper is replaced by **KRaft** for better performance.

---

**Q4. How does Kafka achieve scalability?**  
**A:** Kafka uses **partitioning** to split topics into multiple parts that can be spread across brokers, allowing parallel processing and high throughput.

---

# Kafka Producer API (Java)

##  Overview
Kafka Producers are used to publish data (records/messages) to Kafka topics. They push data to Kafka brokers based on the topic and optional key, which determines the partition.

---

##  1. Creating Kafka Producers

###  Components:
- `ProducerRecord<K, V>`: Represents a record with topic, key, value, partition, and timestamp.
- `KafkaProducer<K, V>`: Main class to send records.

### 💡 Example:

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);

ProducerRecord<String, String> record = new ProducerRecord<>("my-topic", "key1", "value1");
producer.send(record);
producer.close();
```

---

##  2. Acknowledgment Modes (`acks`)

| acks Value | Description |
|------------|-------------|
| 0          | No acknowledgment; fire and forget |
| 1          | Leader acknowledges receipt |
| all (or -1)| All in-sync replicas acknowledge |

**Set via:**
```java
props.put("acks", "all");
```

---

##  3. Key Serialization & Partitioning

- Kafka uses key's hash to determine partition.
- If no key is provided, Kafka uses round-robin.

```java
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
```

You can also implement a **CustomPartitioner**.

---

##  4. Error Handling and Retries

###  Configurations:

- `retries`: Number of retry attempts (default: 0).
- `retry.backoff.ms`: Wait time between retries.

```java
props.put("retries", 3);
props.put("retry.backoff.ms", 1000);
```

###  Synchronous Send with Exception Handling:

```java
try {
    producer.send(record).get(); // Blocking
} catch (Exception e) {
    e.printStackTrace();
}
```

---

##  5. Idempotent Producer

Ensures **exactly-once** semantics in Kafka 0.11+.

```java
props.put("enable.idempotence", "true");
```

Benefits:
- Prevents duplicate messages on retries
- Automatically sets safe configurations (`acks=all`, `retries > 0`, `max.in.flight.requests.per.connection=5`)

---

##  Summary Table

| Feature           | Key Config / Class                                 |
|-------------------|-----------------------------------------------------|
| Basic Producer    | `KafkaProducer`, `ProducerRecord`                  |
| Acks              | `acks = 0 / 1 / all`                                |
| Retry             | `retries`, `retry.backoff.ms`                      |
| Serialization     | `key.serializer`, `value.serializer`               |
| Idempotence       | `enable.idempotence = true`                        |
| Partitioning      | `CustomPartitioner` (optional)                     |

---

#  Kafka Consumer API (Java)

##  Overview

The **Kafka Consumer API** allows applications to read streams of data from Kafka topics efficiently and with fault tolerance. Consumers are typically part of a **Consumer Group**, which allows Kafka to scale horizontally.

---

##  Creating Kafka Consumers

To create a Kafka consumer in Java:

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "my-consumer-group");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Collections.singletonList("my-topic"));
```

---

##  poll() and subscribe()

- `poll(Duration timeout)`: Fetches data from Kafka.
- `subscribe(List<String>)`: Subscribes the consumer to one or more topics.

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("Consumed: key=%s, value=%s, offset=%d%n",
                          record.key(), record.value(), record.offset());
    }
}
```

---

##  Consumer Groups and Load Balancing

- Kafka consumers in the **same group** share load.
- Each partition is consumed by only **one consumer** in the group.
- Multiple groups = multiple independent reads of data.

---

##  Offset Commit Strategies

###  Automatic Offset Commit
Set `enable.auto.commit=true` (default). Kafka will periodically commit offsets.

**Pros:**
- Easy to use.

**Cons:**
- Risk of **duplicate** processing on crashes.

---

###  Manual Offset Commit

```java
consumer.commitSync(); // or commitAsync()
```

Control **when** offsets are committed. Ensures processing is complete before acknowledging.

---

##  Rebalancing and Partition Assignment Strategies

When consumers join or leave the group, Kafka **rebalances** partitions.

Common strategies:
- `RangeAssignor` (default): continuous chunk of partitions.
- `RoundRobinAssignor`: assigns partitions evenly across consumers.

```java
props.put("partition.assignment.strategy",
          "org.apache.kafka.clients.consumer.RoundRobinAssignor");
```

---

##  Consuming from a Specific Offset or Timestamp

```java
TopicPartition partition = new TopicPartition("my-topic", 0);
consumer.assign(Collections.singletonList(partition));
consumer.seek(partition, 10); // Start from offset 10
```

For timestamp:

```java
Map<TopicPartition, Long> timestamps = new HashMap<>();
timestamps.put(partition, System.currentTimeMillis() - 3600000);
Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestamps);
consumer.seek(partition, offsets.get(partition).offset());
```

---

##  Handling Duplicates and Idempotent Processing

Kafka **does not guarantee exactly-once delivery** on the consumer side. To ensure idempotency:

- Track processed message keys or offsets.
- Use external storage or deduplication logic.
- Combine with transactional producer for true exactly-once.

---

##  Interview Questions & Answers

### Q1: What is a Kafka consumer group?

**A:** A group of consumers that coordinate to consume partitions of a topic without overlap. It enables horizontal scaling of message processing.

---

### Q2: How does Kafka handle load balancing?

**A:** Kafka uses **consumer groups** to assign partitions such that each partition is consumed by only one consumer in a group.

---

### Q3: What are rebalancing events?

**A:** When a consumer joins or leaves, Kafka reassigns partitions among the active consumers. This is called rebalancing.

---

### Q4: When should you use manual offset commit?

**A:** Use manual offset commit when you want to ensure that processing logic is successfully completed before acknowledging the message.

---

### Q5: Can a Kafka consumer read from a specific timestamp?

**A:** Yes, using `offsetsForTimes()` to get the offset for a given timestamp and then `seek()`.

---

# Kafka with Spring Boot

##  Using Spring Kafka (`spring-kafka`)
Spring Kafka provides a simple abstraction for integrating Apache Kafka with Spring applications. The main dependency is:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

---

##  @KafkaListener, KafkaTemplate

### KafkaTemplate
Used to send messages to Kafka topics.

```java
@Autowired
private KafkaTemplate<String, String> kafkaTemplate;

public void sendMessage(String topic, String message) {
    kafkaTemplate.send(topic, message);
}
```

### @KafkaListener
Used to consume messages.

```java
@KafkaListener(topics = "my-topic", groupId = "group_id")
public void listen(String message) {
    System.out.println("Received: " + message);
}
```

---

##  Listener Container Config (`ConcurrentKafkaListenerContainerFactory`)

Used to configure how consumers behave.

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3); // for parallel processing
    return factory;
}
```

---

##  Error Handling and Retry Logic

You can handle errors by customizing the error handler.

```java
factory.setErrorHandler((thrownException, data) -> {
    System.out.println("Error in process with Exception {} and the record is {}" + thrownException + data);
});
```

Spring Kafka also supports retry logic through `RetryTemplate` or Dead Letter Topics.

---

##  Dead Letter Topics (DLTs)

DLTs help handle message processing failures.

```yaml
spring:
  kafka:
    listener:
      ack-mode: manual
      retry:
        enabled: true
      error-handler:
        dead-letter-publish: true
```

Example configuration:
```java
@Bean
public DeadLetterPublishingRecoverer publisher(KafkaTemplate<Object, Object> template) {
    return new DeadLetterPublishingRecoverer(template);
}
```

---

##  Producer and Consumer Configuration

### Producer config:
```java
@Bean
public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
}
```

### Consumer config:
```java
@Bean
public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return props;
}
```

---

##  Follow-Up Interview Questions

1. **How does Spring Kafka handle message retries?**
    - Via `RetryTemplate` or Dead Letter Topics.
2. **What happens if message processing fails in a Kafka listener?**
    - It can be retried or forwarded to a DLT.
3. **How do you configure concurrent consumers in Spring Kafka?**
    - Using `setConcurrency()` in `ConcurrentKafkaListenerContainerFactory`.
4. **Difference between `@KafkaListener` and using KafkaConsumer directly?**
    - `@KafkaListener` is simpler and uses a listener container internally.

---

# Kafka Streams API

Kafka Streams is a client library for building applications and microservices that process and transform data stored in Kafka. It combines the simplicity of writing and deploying standard Java applications with the benefits of Kafka's server-side cluster technology.

---

##  Stream Processing on Kafka Topics

Kafka Streams allows direct processing of records in Kafka topics without requiring separate processing clusters (like Flink or Spark). It can read, process, and write back to Kafka—all inside a single application.

---

##  Stateless vs Stateful Transformations

- **Stateless**: Operations do not depend on previous records.
    - Examples: `map`, `filter`, `flatMap`

- **Stateful**: Require maintaining state or relationships across records.
    - Examples: `groupByKey`, `count`, `aggregate`, `join`, `windowed operations`

---

##  Core Transformations

###  map, filter, flatMap
```java
stream.filter((key, value) -> value.contains("error"))
      .mapValues(value -> value.toUpperCase());
```

###  join
```java
KStream<String, Order> orders = builder.stream("orders");
KTable<String, Customer> customers = builder.table("customers");

KStream<String, EnrichedOrder> enriched = orders.join(
    customers,
    (order, customer) -> new EnrichedOrder(order, customer)
);
```

###  Windowed operations
```java
stream.groupByKey()
      .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
      .count();
```

---

##  KStream, KTable, GlobalKTable

- **KStream**: Represents an unbounded stream of records.
- **KTable**: Represents a changelog stream as a table (latest state per key).
- **GlobalKTable**: Replicates the full table to each application instance.

Use appropriate types depending on the need for stateful joins or lookups.

---

##  Kafka Streams vs Kafka Connect or External Stream Processors

| Feature | Kafka Streams | Kafka Connect | Flink/Spark |
|--------|----------------|----------------|-------------|
| Type | Library (embedded) | Connector framework | External system |
| Use Case | In-app processing | Ingest/export data | Complex/large-scale processing |
| Dependency | Minimal | Runs in Kafka ecosystem | External infra needed |

---

##  Example Use Case

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

StreamsBuilder builder = new StreamsBuilder();
KStream<String, String> stream = builder.stream("input-topic");

stream.filter((key, value) -> value.contains("important"))
      .mapValues(String::toUpperCase)
      .to("output-topic");

KafkaStreams streams = new KafkaStreams(builder.build(), props);
streams.start();
```

---

##  Follow-up Interview Questions

### Q1. What are the differences between KStream and KTable?
**A:** `KStream` is for real-time data (event streams), while `KTable` holds the latest value for each key (like a database view). Use `KStream` for stateless, `KTable` for stateful operations.

---

### Q2. What is a windowed join in Kafka Streams?
**A:** It limits the join to events that occur within a specified time window (e.g., join user logins with events within 5 minutes). Useful for temporal correlation.

---

### Q3. What happens if a Kafka Streams app crashes?
**A:** Kafka Streams uses changelog topics and internal state stores to recover state after crashes, ensuring exactly-once semantics.

---

### Q4. When would you choose Kafka Streams over Apache Flink?
**A:** Kafka Streams is ideal for lightweight, embedded stream processing in Java apps. Choose Flink for complex distributed processing, non-JVM support, or large-scale analytics.

---

#  8. Kafka Connect

Kafka Connect is a framework to stream data into and out of Apache Kafka. It comes with pre-built connectors and also allows the creation of custom ones.

---

##  What is Kafka Connect?

Kafka Connect is part of the Apache Kafka project. It’s designed to simplify the process of integrating Kafka with external systems such as databases, key-value stores, search indexes, and file systems.

- Moves large data sets efficiently
- Reuses connectors instead of writing custom code
- Scales elastically and fault-tolerantly

---

Why Use Kafka Connect?

- Simplifies integration between Kafka and external systems
- Offers reusable components called connectors
- Supports both source connectors (importing data into Kafka) and sink connectors (exporting data from Kafka)
- Distributed and fault-tolerant

---

Kafka Connect Architecture

- Standalone Mode: For development/testing (runs on a single process)
- Distributed Mode: Production-ready, supports scalability and fault tolerance
- Workers: Kafka Connect processes that run connectors
- Tasks: Units of work performed by a connector (can be parallelized)
- Connector Plugin: JAR file containing logic for interacting with an external system

---
##  Connectors

###  Source Connectors
Used to pull data from external systems into Kafka.

**Examples:**
- JDBC Source Connector
- FileStream Source Connector

###  Sink Connectors
Used to push data from Kafka topics to external systems.

**Examples:**
- JDBC Sink Connector
- Elasticsearch Sink Connector
- S3 Sink Connector

---

##  Popular Connectors

| Connector Type | Examples |
|----------------|----------|
| Source         | JDBC, File, MQTT, HTTP, Twitter |
| Sink           | JDBC, S3, Elasticsearch, HDFS   |

---
Example: JDBC Source Connector

```json```
{
"name": "jdbc-source-connector",
"config": {
"connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
"tasks.max": "1",
"connection.url": "jdbc:mysql://localhost:3306/mydb",
"connection.user": "user",
"connection.password": "password",
"topic.prefix": "jdbc-",
"poll.interval.ms": "1000"
}
}
---

Kafka Connect Cluster Configuration

- Bootstrap Servers: Kafka brokers to connect to
- Key Converter / Value Converter:
    - e.g., org.apache.kafka.connect.json.JsonConverter
- Internal Topics:
    - Config storage topic
    - Offset storage topic
    - Status storage topic
- Rest Port: Connects over REST API (default: 8083)

---
##  Custom Connector Creation

To create a custom Kafka connector:

1. Implement `SourceConnector` or `SinkConnector`
2. Implement `SourceTask` or `SinkTask`
3. Package into a JAR
4. Deploy in the Connect cluster

**Basic Structure:**
```java
public class MyCustomSourceConnector extends SourceConnector {
    // implementation logic
}
```

---

##  Connect Cluster Configuration

Kafka Connect can be run in two modes:

### 1. Standalone Mode
- Suitable for development or testing.
- All configuration in local files.

### 2. Distributed Mode
- Production-ready.
- Distributed, scalable, and fault-tolerant.

**Key configuration files:**
- `connect-standalone.properties`
- `connect-distributed.properties`

**REST API support for managing connectors** in distributed mode.

---

##  Summary

Kafka Connect simplifies integration between Kafka and external systems. With built-in scalability, fault tolerance, and the ability to write custom connectors, it’s an essential tool for building real-time data pipelines.

---

##  Interview Questions

1. **What is Kafka Connect and how is it different from Kafka Streams?**
    - Kafka Connect is for moving data in/out of Kafka. Kafka Streams is for processing data within Kafka.

2. **What’s the difference between a source and sink connector?**
    - Source pulls data **into** Kafka, sink pushes data **out** to external systems.

3. **How do you implement a custom Kafka Connector?**
    - Extend `SourceConnector` or `SinkConnector`, implement associated Task classes, and package them as a JAR.

4. **What’s the difference between standalone and distributed mode in Kafka Connect?**
    - Standalone is for dev/testing, distributed is production-grade with REST management.

5. **Can we manage connectors dynamically?**
    - Yes, in distributed mode using Kafka Connect REST API.

---

#  Kafka Security

##  1. SSL Encryption (TLS)

**Purpose**:  
Encrypt data in transit between Kafka clients and brokers.

**How to Enable**:
- Generate SSL certificates for broker and clients.
- Configure Kafka broker:
```properties
ssl.keystore.location=/path/to/kafka.server.keystore.jks  
ssl.keystore.password=secret  
ssl.key.password=secret  
ssl.truststore.location=/path/to/kafka.server.truststore.jks  
ssl.truststore.password=secret  
security.inter.broker.protocol=SSL  
listeners=SSL://localhost:9093  
```

- Configure client:
```properties
security.protocol=SSL  
ssl.truststore.location=/path/to/client.truststore.jks  
ssl.truststore.password=secret  
```

**Interview Qs**:
- *Why is SSL required in Kafka?*  
  To ensure encryption and prevent man-in-the-middle attacks.
- *Can you encrypt only client-broker traffic?*  
  Yes, by selectively enabling SSL on listener ports.

---

##  2. SASL Authentication

**Purpose**:  
Verify client identity (authentication).

**Mechanisms**:
- **PLAIN** (username/password)
- **SCRAM-SHA-256/512** (secure salted hash passwords)
- **GSSAPI (Kerberos)** (enterprise-level)

**Config for Broker** (SCRAM):
```properties
listeners=SASL_SSL://localhost:9094  
security.inter.broker.protocol=SASL_SSL  
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-512  
sasl.enabled.mechanisms=SCRAM-SHA-512  
```

**Client Config**:
```properties
security.protocol=SASL_SSL  
sasl.mechanism=SCRAM-SHA-512  
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required  
username="admin" password="admin-secret";
```

**Interview Qs**:
- *Difference between PLAIN and SCRAM?*  
  SCRAM stores hashed/salted credentials, more secure than PLAIN.
- *How does Kafka store credentials for SCRAM?*  
  In ZooKeeper (Kafka <3.x) or in KRaft metadata (Kafka 3.x+).

---

##  3. ACLs (Access Control Lists)

**Purpose**:  
Authorization — control which users can access which Kafka resources.

**Enable ACLs**:
```properties
authorizer.class.name=kafka.security.authorizer.AclAuthorizer  
super.users=User:admin  
allow.everyone.if.no.acl.found=false
```

**Add ACL Example**:
```sh
kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181  --add --allow-principal User:clientA --operation Read --topic myTopic
```

**Interview Qs**:
- *How do ACLs work in Kafka?*  
  They specify which principals (users) can perform operations on resources like topics, consumer groups.
- *What happens if no ACL is defined?*  
  Controlled by `allow.everyone.if.no.acl.found` (true allows access, false blocks).

---

##  4. Kerberos (Optional, for enterprise)

**Purpose**:  
Enterprise-grade SSO and authentication with GSSAPI.

**Broker Config**:
```properties
security.inter.broker.protocol=SASL_PLAINTEXT  
sasl.mechanism.inter.broker.protocol=GSSAPI  
```

**Client Config**:
```properties
security.protocol=SASL_PLAINTEXT  
sasl.mechanism=GSSAPI  
sasl.kerberos.service.name=kafka  
```

**Key Concepts**:
- Uses **Ticket Granting Ticket (TGT)** and **Keytab files**
- Integrated with LDAP/Active Directory

**Interview Qs**:
- *When is Kerberos preferable over SCRAM?*  
  In large enterprise environments where SSO and centralized identity management are required.
- *What are challenges with Kerberos in Kafka?*  
  Complex setup, requires Kerberos KDC and proper time sync (NTP).

---

##  Summary Table

| Feature       | Purpose                          | Example Mechanism           |
|---------------|----------------------------------|-----------------------------|
| SSL/TLS       | Encryption                       | X.509 certs                 |
| SASL          | Authentication                   | SCRAM, PLAIN, GSSAPI        |
| ACLs          | Authorization                    | kafka-acls.sh               |
| Kerberos      | Enterprise Authentication        | GSSAPI                      |

---


#  10. Kafka Monitoring and Metrics

##  Kafka JMX Metrics

JMX (Java Management Extensions) is the standard way Kafka exposes metrics.
You can access metrics by enabling JMX and using tools like JConsole, Prometheus JMX exporter, etc.

**Common JMX Metrics:**
- `kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec`
- `kafka.network:type=RequestMetrics,name=RequestsPerSec,request=Produce`
- `kafka.consumer:type=ConsumerFetcherManager,name=MaxLag,clientId=*`
- GC metrics from JVM (`java.lang:type=GarbageCollector,name=*`)

##  Tools for Monitoring

### Prometheus + Grafana
- **Prometheus JMX Exporter**: Exposes Kafka JMX metrics as Prometheus metrics.
- **Grafana Dashboards**: Visualize Kafka health, throughput, latency, etc.

**Steps:**
1. Use JMX Exporter as Java Agent in Kafka startup script.
2. Configure Prometheus to scrape metrics.
3. Import prebuilt Grafana dashboards (e.g., from Confluent or Bitnami).

### Confluent Control Center
- GUI for Kafka health, topic throughput, lag, schema usage, alerts.
- Part of Confluent Platform (requires enterprise edition for some features).

##  Consumer Lag Monitoring

Consumer lag = Latest offset - Committed offset

**Tools:**
- `kafka-consumer-groups.sh --describe`
- Burrow (by LinkedIn)
- Prometheus + Grafana dashboards

### Why it's important:
- Large lag means consumers are behind, potentially causing SLA issues.
- May indicate processing issues, throttled consumers, network latency.

## 💀 Dead-letter Queues (DLQs)

Used to handle messages that failed during consumption or processing.

### Key Features:
- Prevent poison-pill messages from crashing consumers.
- DLQ is another Kafka topic.
- Implemented with Spring Kafka (or manually via Kafka APIs).

```java
@KafkaListener(topics = "input-topic", errorHandler = "myErrorHandler")
public void process(String message) {
    // handle message
}

@Bean
public ErrorHandler myErrorHandler(KafkaTemplate<?, ?> template) {
    return new SeekToCurrentErrorHandler(
        new DeadLetterPublishingRecoverer(template), 
        new FixedBackOff(1000L, 2));
}
```

##  Best Practices
- Monitor all critical metrics: lag, throughput, request failures, JVM GC.
- Use alerting systems based on metrics thresholds.
- Centralized logging for Kafka brokers.

---

#  11. Kafka Error Handling & Retries

Error handling in Kafka applications is critical for ensuring robust and reliable data processing. Kafka provides built-in mechanisms, and frameworks like Spring Kafka extend them for more advanced use cases.

---

##  Deserialization Errors

### Problem:
When a consumer receives a message that cannot be deserialized due to malformed data or wrong schema.

### Solutions:
- Use `ErrorHandlingDeserializer` from Spring Kafka.
- Log and route bad messages to a Dead Letter Topic (DLT).

```java
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
    ErrorHandlingDeserializer.class.getName());
props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
    JsonDeserializer.class.getName());
```

---

##  Retry Strategies

### 1. Fixed Backoff
Retries the failed message with a constant delay.

```java
factory.setCommonErrorHandler(new DefaultErrorHandler(
    new FixedBackOff(1000L, 3))); // Retry 3 times with 1 second delay
```

### 2. Exponential Backoff
Backoff time increases exponentially with each retry.

```java
ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
backOff.setInitialInterval(1000L);
backOff.setMultiplier(2);
factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));
```

---

##  Dead Letter Topics (DLTs)

DLTs are special Kafka topics used to store messages that cannot be processed even after multiple retries.

### How it works:
- Consumer retries N times (using `DefaultErrorHandler`).
- After failure, message is redirected to `.DLT` topic.

```java
DefaultErrorHandler errorHandler = new DefaultErrorHandler(
    new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 3));
```

---

##  Poison Pill Messages

A poison pill is a message that consistently causes failures.

### Mitigation:
- Detect and route to DLT after N retries.
- Use message headers (like `x-retry-count`) to track retry attempts.

---

##  Interview Questions

### Q1: How do you handle deserialization errors in Kafka consumers?
**Answer:** Use `ErrorHandlingDeserializer` and configure a `DefaultErrorHandler` to log the error, retry, and optionally publish to a DLT.

### Q2: What's the difference between fixed backoff and exponential backoff?
**Answer:** Fixed backoff retries messages with a constant delay; exponential backoff increases the delay after each retry.

### Q3: What are dead-letter topics and when do you use them?
**Answer:** DLTs store unprocessable messages for later analysis or manual intervention. They’re used after exhausting retry attempts.

### Q4: How to identify a poison pill message?
**Answer:** Poison pills cause repeated failures and can be identified using headers or error tracking tools. Route them to DLTs to prevent retry loops.

---

##  Best Practices
- Always use DLTs in production.
- Configure proper retry intervals to reduce load.
- Use logging and alerting for poison pills and deserialization failures.

---

#  12. Kafka Performance Tuning

Performance tuning in Kafka is essential to maximize throughput, minimize latency, and efficiently utilize system resources. Below are the key parameters and strategies to optimize Kafka producers and consumers.

---

##  Batching and Compression

###  Batching
- **batch.size**:
    - Maximum number of bytes per batch for a single partition.
    - Larger batches improve throughput but may increase latency.

- **linger.ms**:
    - Time to wait before sending a batch even if it’s not full.
    - Helps in batching more records in a single request.

###  Compression
- **compression.type**: `none`, `gzip`, `snappy`, `lz4`, `zstd`
    - Reduces network usage and disk I/O.
    - **snappy** and **lz4** are fast and suitable for high-throughput use cases.

---

##  Producer Tuning Parameters

| Property          | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| `acks`           | Control durability (`0`, `1`, `all`). `all` is safest, `0` is fastest.       |
| `retries`        | Number of retries on transient failures.                                    |
| `max.in.flight.requests.per.connection` | Controls ordering guarantees and parallelism. Set to 1 to ensure ordering. |
| `buffer.memory`  | Total memory available to the producer for buffering.                        |

---

##  Consumer Tuning Parameters

###  Fetch Configuration
- **fetch.min.bytes**:
    - Minimum amount of data the broker should return.
    - Higher value improves throughput.

- **fetch.max.wait.ms**:
    - Max wait time if `fetch.min.bytes` is not met.
    - Useful for batching messages.

###  Parallelism
- **max.poll.records**:
    - Number of records returned in one `poll()` call.
    - Tune for batch processing.

- **concurrency (Spring Kafka)**:
    - Number of concurrent consumers per listener container.
    - Set via `ConcurrentKafkaListenerContainerFactory`.

---

##  Partitioning Strategies

- **Custom Partitioner**:
    - Implement your own logic for routing messages to partitions.
    - Can help evenly distribute load or co-locate related data.

- **Key-based Partitioning**:
    - Default method; ensures order for a given key.
    - Uneven keys can lead to partition skew.

---

##  Best Practices

- Use **snappy** or **lz4** compression for fast throughput.
- Tune **linger.ms** and **batch.size** together for batching.
- Monitor **consumer lag** and adjust `max.poll.records` or concurrency.
- Avoid excessive batching in low-latency use cases.
- Benchmark with your actual workloads.

---

#  13. Kafka High Availability & Scalability

Kafka is designed for high availability (HA) and horizontal scalability. Proper configurations and architecture decisions help ensure data durability and system resilience.

---

##  Partitioning Strategy and Balancing

###  Partitioning Strategy
- Messages are distributed across **partitions** within a topic.
- Key-based partitioning ensures message ordering per key.
- Random or round-robin partitioning provides even distribution.

###  Balancing
- Partitions are spread across brokers.
- Good partition design ensures even load across Kafka brokers.
- Use **Kafka Cruise Control** to auto-balance partitions.

---

##  Replication Factor and ISR (In-Sync Replicas)

###  Replication Factor
- Each partition can have multiple replicas.
- One **leader**, others are **followers**.
- Recommended: 3 (minimum 2 for HA).

###  ISR (In-Sync Replicas)
- Set of replicas that are fully caught up with the leader.
- Kafka only acknowledges writes to ISR members.

---

##  Avoiding Data Loss

| Setting | Description |
|--------|-------------|
| `acks=all` | Ensures data is acknowledged only after all ISR members confirm the write. |
| `min.insync.replicas` | Minimum ISR count required to accept a write. Prevents write if not enough replicas are in sync. |

>  Use `acks=all` **with** `min.insync.replicas >= 2` to ensure durability even during broker failure.

---

##  Multi-Cluster Architecture (MirrorMaker)

###  Kafka MirrorMaker
- Tool for replicating data between Kafka clusters.
- Used for geo-redundancy, migration, and multi-datacenter setups.

###  Use Cases
- **Disaster recovery**: replicate between regions.
- **Data locality**: keep consumers close to their data.
- **Cloud migration**: mirror data from on-prem to cloud.

---

##  Best Practices

- Always enable replication (minimum 2, ideally 3).
- Monitor ISR lag and rebalance under-replicated partitions.
- Use **Kafka Controller** monitoring to detect failovers.
- Test cluster failover scenarios proactively.
- Separate critical topics into separate partitions with dedicated replication if needed.

---

#  14. Schema Registry (with Avro/Protobuf)

Kafka often works with structured data formats like **Avro** or **Protobuf**. To manage schema evolution and enforce data contracts, we use **Schema Registry**.

---

##  What is Schema Registry?

- A central service to store and retrieve schemas (data structure definitions).
- Ensures producers and consumers agree on the data structure.
- Avoids hard-coding schema information into Kafka messages.

---

##  Why Use Schema Registry?

- **Strong typing**: Avro and Protobuf provide strongly typed messages.
- **Compatibility control**: Ensure that data changes don’t break consumers.
- **Efficient serialization**: Avro and Protobuf compress data and reduce payload size.

---

##  Common Schema Formats

| Format    | Description |
|-----------|-------------|
| **Avro**  | Popular in Kafka ecosystem; supports schema evolution. |
| **Protobuf** | Compact, fast, language-neutral format from Google. |

---

##  Schema Evolution and Compatibility Rules

Schema Registry supports different compatibility modes to ensure schema changes don’t break consumers:

| Compatibility Mode | Description |
|--------------------|-------------|
| **BACKWARD**       | New schema can read data written with previous schema. |
| **FORWARD**        | Old schema can read data written with new schema. |
| **FULL**           | Both forward and backward compatible. |
| **NONE**           | No compatibility checks. |

>  Always plan schema evolution carefully in production systems.

---

##  How It Works (with Avro)

1. **Producer** registers the schema with the registry.
2. **Schema Registry** returns a schema ID.
3. **Producer** sends data with the schema ID in message headers.
4. **Consumer** fetches the schema by ID from the registry to deserialize.

---

##  Code Snippet: Kafka Avro Producer Example

```java
Properties props = new Properties();
props.put("key.serializer", StringSerializer.class);
props.put("value.serializer", KafkaAvroSerializer.class);
props.put("schema.registry.url", "http://localhost:8081");

KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);
```

---

##  Tools

- **Confluent Schema Registry** (most widely used)
- **Karapace** (open-source alternative)
- **Apicurio** (Red Hat schema registry)

---

##  Best Practices

- Always version your schemas.
- Avoid schema breaking changes like deleting required fields.
- Use full compatibility mode in production for maximum safety.
- Centralize schema governance for better data contracts.

---

#  15. Real-World Design Patterns in Kafka

##  Event Sourcing with Kafka
- **Concept**: Store state changes (events) rather than current state.
- **Why Kafka?**: Kafka's immutable, append-only log fits perfectly for persisting events over time.
- **How it works**:
    - Each change to the application's state is captured as an event and written to a Kafka topic.
    - Consumers rebuild current state by replaying events.

##  CQRS using Kafka Streams
- **CQRS (Command Query Responsibility Segregation)**: Separate read and write models.
- **Kafka Use Case**:
    - **Command Side**: Events from producers (writes).
    - **Query Side**: Kafka Streams processes events and updates materialized views (read models).
- **Tools**: `KTable`, `KStream`, local state stores.

##  Saga Pattern with Kafka
- **Problem**: Distributed transaction management across services.
- **Kafka Solution (Choreography)**:
    - Services communicate via events.
    - No central orchestrator — each service reacts and emits events.
    - Example: Order Service → emits "OrderPlaced" → Payment Service listens → emits "PaymentConfirmed".

##  Idempotent Consumers
- **Goal**: Prevent duplicate processing.
- **Challenges**: Kafka can redeliver messages (at-least-once by default).
- **Techniques**:
    - Use unique IDs in events.
    - Maintain a processed-event cache or DB record.
    - Ensure message processing is side-effect free or deduplicated.

##  Outbox Pattern
- **Problem**: Ensuring DB and Kafka remain in sync (avoid dual writes).
- **Solution**:
    - Write DB transaction and event into an `outbox` table in the same transaction.
    - A background process (or Debezium CDC) publishes events from the outbox to Kafka.
- **Benefits**: Atomicity and consistency.

##  When to Use These Patterns?
| Pattern         | Use Case |
|----------------|----------|
| Event Sourcing | Auditability, replayability |
| CQRS           | Complex queries, performance isolation |
| Saga           | Long-running workflows across microservices |
| Idempotent Consumer | Preventing duplicate side effects |
| Outbox Pattern | Reliable DB-to-Kafka publishing |

##  Best Practices
- Ensure topic naming reflects business domains.
- Use schema registry for event compatibility.
- Monitor consumer lag and event delivery.
- Choose correct delivery semantics (at-least-once vs exactly-once).

---

#  16. Testing Kafka

##  Embedded Kafka for Unit/Integration Tests
- **Embedded Kafka** from `spring-kafka-test` or other test libraries allows spinning up a Kafka broker in-memory.
- **Use Cases**:
    - Unit testing producers and consumers without external dependencies.
    - Integration testing Kafka configurations.
- **Example (Spring Boot)**:
  ```java
  @SpringBootTest
  @EmbeddedKafka(partitions = 1, topics = { "test-topic" })
  public class KafkaIntegrationTest {
      @Autowired
      private KafkaTemplate<String, String> kafkaTemplate;
      
      @Test
      public void testSendReceive() {
          kafkaTemplate.send("test-topic", "key", "value");
          // assertions here
      }
  }
  ```

##  Kafka TestContainers
- Leverages Docker to spin up real Kafka instances for testing using the `testcontainers` library.
- **Benefits**:
    - Closer to production environment.
    - Supports custom configurations (e.g., SASL, SSL).
- **Use Case**:
  ```java
  KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
  kafka.start();
  ```

##  Simulating Partitions, Lag, and Retries
- **Partition Testing**: Manually assign partitions and produce to simulate ordering and load balancing.
- **Lag Simulation**: Use delay in consumer polling or commit to test lag-handling logic.
- **Retry Simulation**: Throw exceptions in consumer logic and validate retry policies/backoff.

##  Best Practices
- Clean up brokers/topics after test runs.
- Isolate tests using random topic names.
- Assert message delivery, ordering, and headers where relevant.


## Kafka does not guarantee global ordering across partitions. Ordering is only guaranteed within a single partition.

- **Kafka’s Sequential Guarantee**: Kafka guarantees order within a partition, not across partitions.

- That means:
    -	Inside Partition 0 → messages are strictly ordered by offset (0, 1, 2, 3…).
    -	Inside Partition 1 → another independent sequence (0, 1, 2, 3…).
    -	Across partitions → no ordering guarantee.

- Example:
```
Topic: trades
Partitions: 2

Partition 0:

Offset 0 → Trade A
Offset 1 → Trade B
Offset 2 → Trade C

Partition 1:

Offset 0 → Trade D
Offset 1 → Trade E
```
If consumers read from both partitions in parallel, they could see:
```
Trade A → Trade D → Trade B → Trade E → Trade C
```
Order across partitions is not guaranteed.

- **Why Kafka Does This**
- Kafka is designed for horizontal scalability:
-	Partitioning allows parallel reads/writes → improves throughput.
-	But this comes at the cost of global ordering.
-	Maintaining global order across partitions would require a single log → kills scalability.

That’s why Kafka chooses order per partition.

**When Order Matters**
- Kafka lets you control which partition a message goes to:
-	Producers can use a partition key to consistently send related messages to the same partition.
-	This ensures ordering for those messages.

- Example:
```java
producer.send(new ProducerRecord<>("trades", "accountId-123", tradeJson));
```
Here "accountId-123" is the partition key → all trades for this account go to the same partition → ordering is preserved for that account.

**How Consumers Read from Partitions**
- A Kafka consumer group assigns partitions to consumers:
-	Each consumer reads one or more partitions.
-	Inside each partition → order is preserved.
-	Across partitions → processing is parallel → order can vary.

- Example:
```
Consumer group "risk-service"
Consumer 1 → Partition 0
Consumer 2 → Partition 1
```
They read in parallel → no global ordering.

---

1. Summary & Mental Model

Kafka trades immediate consistency for scalability, resilience, and decoupling. Design for failure.

---

2. Partitioning and Ordering

Example trace showing ordering differences across partitions:

```
Trade A → Trade D → Trade B → Trade E → Trade C
```

Order across partitions is not guaranteed.

Why Kafka Does This
- Kafka is designed for horizontal scalability:
  - Partitioning allows parallel reads/writes → improves throughput.
  - But this comes at the cost of global ordering.
  - Maintaining global order across partitions would require a single log → kills scalability.

That’s why Kafka chooses order per partition.

When Order Matters
- Kafka lets you control which partition a message goes to:
  - Producers can use a partition key to consistently send related messages to the same partition.
  - This ensures ordering for those messages.

Example:

```java
producer.send(new ProducerRecord<>("trades", "accountId-123", tradeJson));
```

Here `accountId-123` is the partition key → all trades for this account go to the same partition → ordering is preserved for that account.

How Consumers Read from Partitions
- A Kafka consumer group assigns partitions to consumers:
    - Each consumer reads one or more partitions.
    - Inside each partition → order is preserved.
    - Across partitions → processing is parallel → order can vary.

Example:

```
Consumer group "risk-service"
Consumer 1 → Partition 0
Consumer 2 → Partition 1
```

They read in parallel → no global ordering.

---

3. Production-Grade Issues (Overview)

Now we’ll go one level deeper — why these Kafka problems actually happen internally, and how exactly they’re solved in production-grade systems.

Each issue explained in 4 parts:
1. What happens
2. Why it happens (Kafka internals)
3. What breaks in real systems
4. How it’s solved (with patterns)

This is senior / staff engineer depth.

---

4. Deep Dives: Common Failure Modes and Solutions

1️⃣ DB Updated but Event Not Published

(Inconsistent State)

What happens
- Booking saved in DB ✅
- Kafka publish failed ❌
  → Other services never know booking exists

Why it happens
- Kafka and DB are two different systems:
    - DB transaction commits
    - Kafka producer fails (network / broker / timeout)

There is no atomicity across DB + Kafka.

Even Kafka “transactions” do NOT cover your DB.

What breaks
- Notification not sent
- Doctor calendar not updated
- Analytics incorrect

This is catastrophic in healthcare / payments.

How it’s solved

✅ Outbox Pattern (industry standard)

Instead of:

```
saveBooking();
publishEvent();
```

You do:

```
BEGIN TRANSACTION
saveBooking();
saveOutboxEvent();
COMMIT
```

Then:
- Background publisher reads outbox
- Publishes to Kafka
- Marks event as published

Same DB transaction = guaranteed consistency

— — —

Duplicate Events

(At-Least-Once Delivery)

What happens
- Same event processed twice.

Why it happens
- Kafka delivery model:
    - “At least once”
    - If consumer processes message and crashes before committing offset, Kafka will re-deliver message.

This is intentional — Kafka prefers data safety over convenience.

What breaks
- Double email
- Double analytics count
- Double doctor slot booking

How it’s solved

✅ Idempotent Consumers

Each event has:

```
eventId: "UUID"
```

Consumer:

```
if (alreadyProcessed(eventId)) return;
process();
markProcessed(eventId);
```

Kafka handles delivery. YOU handle business correctness.

— — —

3️⃣ Consumer Lag

(System is alive but unusable)

What happens
- Kafka running
- Messages piling up
- Users experience delays

Why it happens
- Producer speed > Consumer speed.

Reasons:
- Slow DB writes
- External API calls
- Single-threaded consumers

Kafka never slows producers by default.

What breaks
- Notifications delayed by minutes
- Doctors see outdated calendars
- System appears “randomly slow”

How it’s solved
- Increase partitions
- Increase consumer concurrency
- Batch DB writes
- Optimize slow operations

Kafka queues pain silently.

— — —

4️⃣ Ordering Breaks

(Out-of-order events)

What happens
- Events processed in wrong order.

Why it happens
- Kafka ordering is guaranteed only per partition.
- If you send without key:

```
send(topic, event);
```

Kafka assigns random partitions.

What breaks
- Booking cancelled before created
- Doctor slot freed before blocked

How it’s solved

✅ Correct Partition Key

```
send(topic, bookingId, event);
```

All events for same booking go to same partition → same order.

— — —

5️⃣ Hot Partitions

(Uneven load)

What happens
- One consumer overloaded, others idle.

Why it happens
- Bad partition key distribution:
    - Few doctors get most bookings
    - Their partition becomes hotspot

Kafka does no auto-rebalancing of load inside partition.

What breaks
- High latency
- Consumer lag
- Unpredictable performance

How it’s solved
- Better partition key design
- Composite keys
- Increase partitions
- Repartition topic

This only appears at scale.

— — —

6️⃣ Poison Messages

(Consumer stuck forever)

What happens
- One bad message blocks entire partition.

Why it happens
- Kafka retries same message again and again:
    - Invalid JSON
    - DB constraint violation
    - Unexpected schema

Kafka assumes consumer failure is temporary.

What breaks
- Entire partition halted
- Downstream services frozen

How it’s solved

✅ Dead Letter Topic (DLT)

Flow:
- Retry 3 times
- ↓
- Send to DLT
- ↓
- Continue processing next messages

Bad messages are isolated.

— — —

7️⃣ Schema Evolution Failures

(Breaking changes)

What happens
- New producer breaks old consumers.

Why it happens
- Kafka does NOT enforce schema compatibility.
- JSON has:
    - No versioning
    - No type safety

What breaks
- Consumer crashes
- Silent data corruption

How it’s solved

✅ Schema Registry (Avro / Protobuf)

Rules:
- Backward compatible changes only
- Versioned schemas
- Validation at producer time

This is mandatory at scale.

— — —

8️⃣ Rebalance Storms

(Random latency spikes)

What happens
- Consumers pause suddenly.

Why it happens
- Kafka rebalances when:
    - Consumer joins/leaves
    - Pod restarts
    - Network hiccups

During rebalance:
- No messages are consumed

What breaks
- Latency spikes
- SLA violations

How it’s solved
- Static membership
- Tune timeouts
- Reduce consumer restarts

— — —

9️⃣ No Backpressure

(Kafka overwhelms downstream systems)

What happens
- Kafka accepts messages faster than DB can handle.

Why it happens
- Kafka is:
    - Disk-based
    - Extremely fast
    - Designed to buffer

It assumes consumers handle backpressure.

What breaks
- DB connection pool exhaustion
- OOM errors
- Cascading failures

How it’s solved
- Rate limit producers
- Pause consumers
- Queue-aware throttling

Kafka does not protect your DB.

— — —

🔐 10️⃣ Security Gaps

(Anyone reads anything)

What happens
- Any service can read/write any topic.

Why it happens
- Default Kafka:
    - No auth
    - No ACLs

What breaks
- Data leaks
- Compliance violations (HIPAA, PCI)

How it’s solved
- SASL / mTLS
- Topic-level ACLs
- Service identities

— — —

11️⃣ Monitoring Blindness

(Flying blind)

What happens
- Problems detected only by users.

Why it happens
- Kafka doesn’t alert by default.

What breaks
- SLA breaches
- Late incident response

How it’s solved
Monitor:
- Consumer lag
- Producer error rate
- Disk usage
- Under-replicated partitions

— — —

1 Large Message Payloads

(Kafka abused as storage)

What happens
- Brokers slow down.

Why it happens
- Kafka replicates every byte:
    - Memory
    - Network
    - Disk

Large payloads multiply cost.

What breaks
- Broker crashes
- Throughput collapse

How it’s solved
- Store files in S3
- Send references only

— — —

13️⃣ Event Misuse

(Kafka becomes distributed monolith)

What happens
- Events used as commands.

Why it happens
- Developers misuse Kafka for orchestration.

What breaks
- Tight coupling
- Impossible changes

How it’s solved
- Events represent facts, not actions.

— — —

14️⃣ Eventual Consistency Confusion

(Users see inconsistent state)

What happens
- Booking created but notification delayed.

Why it happens
- Kafka is asynchronous.

What breaks
- User trust
- UX expectations

How it’s solved
- Async UI indicators
- Read models
- Product alignment

---

5. What This Means for You

If you implement:
- Outbox
- Idempotency
- DLT
- Partition strategy
- Monitoring

You are thinking like a senior distributed systems engineer.

---

6. Next Steps (choose one)
1. Implement Outbox Pattern step-by-step in Booking Service
2. Implement Idempotent Consumer + DLT
3. Simulate Kafka failure scenarios
4. Design production Kafka architecture diagram

Tell me what to build next, and we’ll do it properly.

---

7. Mock Interview: Kafka (Scenario-Based + Follow-ups)

I’ll act as the interviewer. Pause after each question, think, and answer, then compare with the expected answer + follow-ups.

Question 1: Message Loss in Production

You deployed a Kafka-based system. Suddenly, downstream systems report missing events. How would you debug this?

What a strong answer should include:
- Start from producer → broker → consumer
- Check producer configs:
    - `acks=all`
    - `retries`
    - `idempotence`
- Check broker:
    - ISR shrink?
    - under-replicated partitions
- Check consumer:
    - offset commits
    - crashes before processing

Follow-up 1:
What happens if `acks=1`?
Expected:
- Leader acknowledges without replicas → possible data loss

Follow-up 2:
Why is `acks=all` still not 100% safe?
Expected:
- If `min.insync.replicas` not configured properly
- ISR may be small

— — —

Question 2: Duplicate Processing

Your system is processing duplicate messages. Kafka is “working fine”. What’s wrong?
Expected answer:
- At-least-once delivery → duplicates possible
- Causes:
    - Producer retries
    - Consumer crash before offset commit
- Solution:
    - Idempotent producer
    - Deduplication at consumer (DB unique key, cache)

Follow-up:
How would you design idempotency in a banking system?
Expected:
- Use transaction ID as unique key
- Store processed IDs (DB/Redis)
- Reject duplicates safely

— — —

Question 3: Ordering Issue

You notice that transactions for a single user are processed out of order.
Expected:
- Kafka guarantees ordering only within a partition
- If no key → round-robin → disorder
- Fix:
    - Use `userId` as key

Follow-up:
Can you guarantee ordering across the whole topic?
Expected:
- Only with single partition (but hurts scalability)

— — —

Question 4: Consumer Lag

Your consumer lag keeps increasing. What will you do?
Expected structured approach:
1. Check lag metrics
2. Identify bottleneck:
    - slow processing?
    - insufficient consumers?
3. Fix:
    - increase partitions
    - scale consumers
    - batch processing
    - tune configs

Follow-up:
Can increasing consumers always reduce lag?
Expected:
- No — limited by number of partitions

— — —

Question 5: Reprocessing Data

A bug caused incorrect processing. You need to reprocess all events.
Expected:
- Reset offsets:
    - `auto.offset.reset=earliest` (new group)
    - or Kafka offset reset tool
- Or create new consumer group

Follow-up:
Why not reuse same consumer group?
Expected:
- Offsets already committed

— — —

Question 6: DB + Kafka Consistency

You save data in DB and then publish event to Kafka. What if Kafka fails after DB commit?
Expected:
- Classic dual write problem
- Solution:
    - Transactional Outbox Pattern
    - CDC (e.g., Debezium)

Follow-up:
Why not use Kafka transactions?
Expected:
- Doesn’t cover external DB transactions

— — —

Question 7: Broker Failure

One Kafka broker crashes. What happens?
Expected:
- Leader election happens
- Replica becomes leader
- No data loss if replication factor > 1

Follow-up:
What if replication factor = 1?
Expected:
- Data loss possible

— — —

Question 8: Partition Increase

You increased partitions from 3 → 10 in production. Any side effects?
Expected:
- Key rehashing → ordering breaks
- Data imbalance
- Consumers may need scaling

Follow-up:
Will old messages get redistributed?
Expected:
- No, only new messages

— — —

Question 9: Retry & DLQ Design

How do you handle failed messages?
Expected:
- Retry topic(s)
- Exponential backoff
- Dead Letter Queue (DLQ)

Follow-up:
Why not retry infinitely?
Expected:
- Poison messages → infinite loop

— — —

Question 10: Exactly Once Processing

How do you ensure exactly-once processing?
Expected:
- Idempotent producer
- Kafka transactions
- Idempotent consumer logic
- Outbox pattern for DB sync

Follow-up:
Is Kafka exactly-once truly “exactly once”?
Expected:
- Only within Kafka ecosystem, not across DB/services

— — —

Rapid Fire (Very Common)
Answer quickly:
1. Can two consumers read same message? → Yes, different consumer groups
2. What happens if offset not committed? → Reprocessing
3. Max consumers in group? → Equal to partitions
4. Ordering guarantee? → Within partition only

— — —

How to Practice (Important)
Don’t just read — simulate:
1. Answer each question out loud
2. Keep answers structured (Producer → Broker → Consumer)
3. Always mention:
    - configs
    - trade-offs
    - real-world fix

If you want next level:
- Cross-question grilling
- System design round (design Kafka for banking transactions)
- Spring Boot + Kafka coding scenarios
- Tricky edge cases interviewers love

---

8. Hands-on: Spring Boot + Kafka Examples

Problem 1: Reliable Kafka Producer (No Message Loss)

Scenario
- Build a Spring Boot producer that publishes payment events reliably.

What interviewer is testing
- Producer configs
- Idempotence
- Serialization
- Clean design

Implementation

1. Configuration

```java
@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, PaymentEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Reliability configs
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

2. Producer Service

```java
@Service
public class PaymentProducer {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPayment(PaymentEvent event) {
        kafkaTemplate.send("payments-topic", event.getUserId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    // log + retry strategy
                    System.err.println("Failed to send: " + ex.getMessage());
                } else {
                    System.out.println("Sent to partition: " + result.getRecordMetadata().partition());
                }
            });
    }
}
```

Follow-up Questions
- Why are you using `userId` as key?
    - Ensures ordering per user (same partition)
- Is this enough to guarantee no data loss?
    - No — need proper broker configs (`min.insync.replicas`)
    - Also need retry handling/logging

— — —

Problem 2: Consumer with Manual Offset Commit

Scenario
- Build a consumer that processes orders and avoids data loss.

What interviewer checks
- Offset handling
- Error handling
- Idempotency awareness

Implementation

1. Config

```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(config,
            new StringDeserializer(),
            new JsonDeserializer<>(OrderEvent.class));
    }
}
```

2. Consumer

```java
@KafkaListener(topics = "orders-topic", groupId = "order-group")
public void consume(OrderEvent event, Acknowledgment ack) {
    try {
        process(event); // business logic
        ack.acknowledge(); // commit AFTER processing
    } catch (Exception ex) {
        // log and retry or send to DLQ
        System.err.println("Error processing: " + event);
    }
}
```

Follow-up Questions
- What happens if you call `acknowledge()` before processing?
    - Data loss
- What if processing fails?
    - Message will be reprocessed
    - Need retry/DLQ

— — —

Problem 3: Retry + Dead Letter Queue (DLQ)

Scenario
- Implement retry mechanism for failed messages.

Implementation Approach
- Main topic → Retry topic → DLQ

Spring Boot Way (using Error Handler)

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
    FixedBackOff backOff = new FixedBackOff(2000L, 3); // 3 retries
    return new DefaultErrorHandler(
        new DeadLetterPublishingRecoverer(template),
        backOff
    );
}
```

Follow-up
- What happens after retries are exhausted?
    - Message goes to DLQ

— — —

Problem 4: Idempotent Consumer

Scenario
- Prevent duplicate payment processing.

Implementation Idea

```java
@Service
public class PaymentService {
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    public void process(PaymentEvent event) {
        if (processed.contains(event.getTransactionId())) {
            return; // duplicate
        }
        // process payment
        processed.add(event.getTransactionId());
    }
}
```

Strong Answer Enhancement
- Use DB with unique constraint instead of in-memory set

Follow-up
- Why is in-memory set not safe?
    - Lost on restart
    - Not scalable

— — —

Problem 5: Kafka + Database Consistency (Outbox Pattern)

Scenario
- Save order in DB and publish event safely.

Correct Approach

1. Save to DB (with outbox table)

```java
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    OutboxEvent event = new OutboxEvent(order.getId(), "ORDER_CREATED");
    outboxRepository.save(event);
}
```

2. Separate publisher reads outbox and sends to Kafka

Follow-up
- Why not send Kafka message inside same transaction?
    - Kafka and DB transactions are separate → inconsistency risk

— — —

Problem 6: High Throughput Optimization

Scenario
- Your Kafka producer is slow.

Expected Improvements
- Increase:
    - `batch.size`
    - `linger.ms`
- Enable compression

Follow-up
- Tradeoff of increasing `linger.ms`?
    - Higher latency, better throughput

— — —

Problem 7: Parallel Processing Consumer

Scenario
- One message takes long time to process.

Solution

```java
@KafkaListener(topics = "orders-topic", concurrency = "3")
public void consume(OrderEvent event) {
    CompletableFuture.runAsync(() -> process(event));
}
```

Follow-up
- Risk here?
    - Offset may be committed before async processing completes → data loss

---

9. Interview Traps & Common Gotchas (Detailed)

When you answer these, they check:
- Do you think in failure scenarios?
- Understand trade-offs
- Know real production issues
- Don’t blindly trust Kafka guarantees

If you want to go elite level, next:
- Turn this into a live coding simulation (you answer, I interrupt)
- Give banking-grade system design using Kafka
- Ask tricky Spring Kafka annotation-based pitfalls
- Give debugging logs and ask you to diagnose

— — —

Common Traps Covered (short list)
1. Auto Commit Trap — `enable.auto.commit=true` can cause data loss because commit timing ≠ processing completion.
    - Strong Answer: Disable auto commit; manual commit after processing.

2. `max.poll.interval.ms` Rebalance Trap — long processing can trigger rebalance.
    - Strong Answer: Increase `max.poll.interval.ms` or use async processing + polling thread separation.

3. Consumer Group Rebalance Storm — frequent joins/leaves cause rebalances.
    - Strong Answer: Use cooperative rebalancing (sticky assignor), avoid frequent restarts.

4. Idempotent Producer Misunderstanding — `enable.idempotence=true` prevents duplicates from retries, but not duplicates caused by other factors.
    - Strong Answer: Still need consumer-side idempotency.

5. Exactly-Once Myth — exactly-once works only within Kafka; not across external DBs/APIs.
    - Strong Answer: Combine Kafka transactions + idempotent DB logic.

6. Message Ordering Break After Scaling — increasing partitions can break ordering.
    - Strong Answer: Partition count decided early or use custom partitioner/key strategy.

7. Large Message Failure — multiple limits (`max.request.size`, `message.max.bytes`, `fetch.max.bytes`) must align.
    - Strong Answer: Align configs or avoid large messages (S3 + references).

8. Silent Data Loss (`min.insync.replicas`) — `acks=all` alone insufficient if `min.insync.replicas` is low.
    - Strong Answer: Use replication factor ≥ 3 and `min.insync.replicas=2`.

9. Zombie Consumers — delayed heartbeats cause perceived ownership.
    - Strong Answer: Tune `session.timeout.ms` and `heartbeat.interval.ms`.

10. Offset Commit Wrong Placement — commit after processing to avoid loss.

11. Poison Message Problem — use retry topics, DLQ, skip after threshold.

12. Multiple Consumers but No Scaling — consumers limited by partitions.

13. Log Compaction Confusion — compaction keeps only latest value per key.

14. Producer Throughput vs Latency Tradeoff — tune `linger.ms` and `batch.size`.

15. Consumer Reads Old Messages Unexpectedly — offset expired or new group; understand `auto.offset.reset` and retention.

16. Kafka vs DB Transaction Misalignment — DB rollback after Kafka publish leads to inconsistency; use Outbox or CDC.

17. Backpressure Collapse — apply throttling, pause/resume consumer, scale partitions.

18. Time vs Offset Misunderstanding — use offset lookup by timestamp if you need time-based consumption.

19. Rebalance During Deployment — use static group membership and cooperative rebalancing.

20. Schema Evolution Breaks Consumers — use Schema Registry and backward compatibility.

---

10. Outbox Pattern & CDC with Debezium (In Depth)

This is one of the most important real-world Kafka patterns, especially for systems like banking, payments, orders.

Problem First: Why Outbox Pattern Exists

Scenario
- Create Order API
- Save Order in DB
- Publish Event to Kafka

Naive Code

```
orderRepository.save(order);
kafkaTemplate.send("order-topic", order);
```

The Problem (Dual Write Issue)
- Case 1: DB Success, Kafka Fails → Order saved in DB, Kafka publish fails → Other services never know order exists → Data inconsistency
- Case 2: Kafka Success, DB Fails → Event published, DB rollback → Consumers process non-existent order → Corrupted system

Root Cause
- Kafka and DB are two separate systems → No shared transaction → No atomicity

Solution: Transactional Outbox Pattern

Instead of writing to DB + Kafka, you:

Step 1: Write EVERYTHING to DB (single transaction)
- Save business data (Order)
- Save event in Outbox Table

Architecture
```
[Your Service]
↓
[DB Transaction]
├── orders table
└── outbox table (event stored)
↓
[Outbox Publisher]
↓
[Kafka Topic]
```

Step-by-Step Implementation (Spring Boot)

1. Outbox Table Design

```sql
CREATE TABLE outbox_event (
  id UUID PRIMARY KEY,
  aggregate_id VARCHAR(255),
  event_type VARCHAR(100),
  payload JSONB,
  status VARCHAR(20), -- NEW, SENT
  created_at TIMESTAMP
);
```

2. Save Order + Event (Single Transaction)

```java
@Transactional
public void createOrder(Order order) {
    // Step 1: Save order
    orderRepository.save(order);
    // Step 2: Save event in outbox
    OutboxEvent event = new OutboxEvent(
        UUID.randomUUID(),
        order.getId(),
        "ORDER_CREATED",
        convertToJson(order),
        "NEW",
        LocalDateTime.now()
    );
    outboxRepository.save(event);
}
```

Key Point
- If anything fails → whole transaction rolls back
- No inconsistency

3. Outbox Publisher (Separate Process)

This is a background job / scheduler / Kafka publisher service

```java
@Scheduled(fixedDelay = 5000)
public void publishOutboxEvents() {
    List<OutboxEvent> events = outboxRepository.findByStatus("NEW");
    for (OutboxEvent event : events) {
        try {
            kafkaTemplate.send("order-topic", event.getAggregateId(), event.getPayload());
            event.setStatus("SENT");
            outboxRepository.save(event);
        } catch (Exception ex) {
            // retry later
        }
    }
}
```

4. What This Guarantees
   Guaranteed:
- If DB commit succeeds → event is stored
- Event will eventually be published

Not Guaranteed:
- Exactly-once delivery (still need idempotent consumers)

Critical Interview Insights
1. Why This Works
- DB transaction is atomic
- Kafka publish is eventually consistent

2. What Happens if Publisher Crashes?
- Events remain in DB (status = NEW)
- Will be retried later

3. What About Duplicate Events?
- Possible scenarios:
    - Publisher crashes after sending but before marking SENT
- Solution:
    - Make consumers idempotent

4. Scaling Problem
- Polling DB every few seconds is not efficient at scale.
- Better Approach: Use CDC (Change Data Capture)

Advanced Version: CDC with Debezium

Instead of polling:
- DB → Debezium → Kafka

- Debezium listens to DB logs (binlog/WAL)
- Automatically publishes events to Kafka

Why CDC is Better
- No polling
- Near real-time
- Highly scalable

Comparison
- Polling Outbox: Simple / DB load, latency
- CDC (Debezium): Scalable, real-time / Setup complexity

Common Mistakes (Interview Traps)
- Mistake 1: “I’ll use `@Transactional` and Kafka together”
    - Wrong — Kafka is NOT part of DB transaction
- Mistake 2: “Kafka transactions solve this”
    - No — they don’t include DB
- Mistake 3: “This guarantees exactly-once”
    - No — still need idempotent consumer, unique keys

Banking Domain Example (Money Transfer)
- Debit account (DB)
- Insert into outbox_event
- Debezium → Kafka
- Ledger Service updates balance

Perfect Interview Answer (Short Version)
- Use the Transactional Outbox Pattern. Store the event in an outbox table within the same DB transaction as the business data. A separate publisher (or CDC tool like Debezium) reads from the outbox and publishes to Kafka. This avoids the dual-write problem and ensures eventual consistency. Design consumers to be idempotent to handle duplicates.

---

11. Debezium Setup (High-Level)

1. Run Kafka + Zookeeper (or KRaft)
2. Run Debezium Connector

Example config (Postgres):

```json
{
  "name": "outbox-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "localhost",
    "database.port": "5432",
    "database.user": "user",
    "database.password": "password",
    "database.dbname": "orders_db",
    "database.server.name": "dbserver1",
    "table.include.list": "public.outbox_event",
    "plugin.name": "pgoutput",
    "tombstones.on.delete": "false"
  }
}
```

Step 3: Use Outbox SMT (Critical)

```json
{
  "transforms": "outbox",
  "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
  "transforms.outbox.route.by.field": "event_type"
}
```

---

12. Failure Scenarios, Trade-offs, and When Not to Use CDC

Failures & Behavior
- App crashes after DB commit → Event already in DB → Debezium will still publish
- Debezium crashes → WAL still has logs → On restart → resumes
- Kafka is down → Debezium retries → No data loss
- Duplicate events → Possible due to retries → Use idempotent consumers

Exactly-Once Discussion
- With Debezium: at-least-once delivery
- To make system safe: idempotent consumers, unique event IDs

Performance Benefits
- Compared to polling: Lower DB load, Real-time events, Horizontally scalable

When NOT to Use Debezium
- Small system
- Low throughput
- Simpler polling is enough

Common Interview Traps
- Debezium replaces Kafka producer — Wrong: It replaces event publishing logic, not Kafka.
- No duplicates with CDC — Wrong: Still possible → handle idempotency.
- We don’t need outbox table — Risky: Exposes internal DB schema and is hard to evolve.

Banking Example (Strong Answer)
- Money Transfer:
    - Debit Account (DB)
    - Insert into outbox_event
    - Debezium → Kafka → Ledger Service updates balance

Perfect Interview Answer
- Use Outbox Pattern with CDC via Debezium. Service writes business data and an event into an outbox table in a single transaction. Debezium listens to WAL and publishes these changes to Kafka in near real-time. This removes dual-write problem and avoids polling overhead. Since delivery is at-least-once, design consumers to be idempotent.

---

13. Final Perfect Interview Answer (Short)
- “For strong consistency between DB and Kafka, I use the Outbox Pattern with CDC via Debezium. The service writes business data and an event into an outbox table in a single transaction. Debezium listens to the database WAL and publishes these changes to Kafka in near real-time. This removes the dual-write problem and avoids polling overhead. Since delivery is at-least-once, I design consumers to be idempotent.”

---

14. Appendix: Short Tips & Configs

- Producer:
    - `acks=all`, `retries=Integer.MAX_VALUE`, `enable.idempotence=true`
- Broker:
    - `replication.factor ≥ 3`, `min.insync.replicas=2`
- Consumer:
    - `enable.auto.commit=false`, commit after processing
- Rebalancing:
    - Use cooperative assignor, static members if possible
- Schema:
    - Use Schema Registry (Avro/Protobuf) with compatibility rules
- Backpressure:
    - Implement producer throttling and consumer pause/resume
- Large Payloads:
    - Store large objects externally (S3) and send references
- Outbox vs CDC:
    - Outbox + Debezium is best at scale; polling outbox OK for small systems

---
