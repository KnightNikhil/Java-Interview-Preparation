# COMPREHENSIVE SYSTEM DESIGN PLAYLIST NOTES
## Engineering Digest - Complete 34-Video Series

***

## **VIDEO 1: WHAT IS SYSTEM DESIGN PROCESS IN SOFTWARE ENGINEERING (2:21)**

### Definition
System Design is the process of designing different elements of a system. It involves defining the internal design details for building an application.

### Key Concepts
- **Architecture**: Internal design details for building an application
- **Elements to Design**: Architecture, modules, data storage, communication between modules
- **Process**: Defining how different components of the system interact

### Two Types of System Design
1. **HLD (High-Level Design)**: Architecture, modules, communication patterns, database choices
2. **LLD (Low-Level Design)**: Business logic, actual implementation, code structure, detailed algorithms

### Important Points
- System design comes after requirements analysis
- Need to define architecture before starting implementation
- Must specify which technology for each component
- Should outline communication protocols between modules

***

## **VIDEO 2: MONOLITHIC ARCHITECTURE (8:50)**

### Definition
All components (Frontend, Backend, Database) are combined and deployed as a single unit.

### Structure
- **Three Components**: Frontend (UI), Backend (Business Logic), Database (Data Storage)
- All integrated in single codebase
- Single deployment unit
- Single technology stack for entire application

### Advantages
1. **Easier Integration Testing**: All components together, so testing is simpler
2. **Simpler to Understand**: No distributed system complexity
3. **Minimal Network Calls**: All code is in one place, so no inter-service network calls
4. **No Complex Management**: Easier to manage and deploy
5. **Better for Small Teams**: Good for startups and small projects

### Disadvantages
1. **Single Point of Failure**: If one component fails, entire system goes down
2. **Must Redeploy Entire System**: Any change to one module requires full redeploy
3. **Technology Limitation**: Cannot change technology for one component without affecting others
4. **Scaling Challenges**: Must scale entire application (vertical scaling only)
5. **Large Codebase**: Becomes difficult to maintain as it grows
6. **Resource Dependency**: All modules share same resources, leading to contention

### When to Use
- Small applications
- Simple use cases
- Early-stage startups
- Read-only websites (less complex than applications)

### When NOT to Use
- Large-scale applications
- Applications requiring different tech stacks
- High-traffic systems

***

## **VIDEO 3: DIFFERENCE BETWEEN MONOLITHIC AND MICROSERVICES ARCHITECTURE (5:17)**

### Monolithic System (Recap)
- All modules in single deployed unit
- Single point of failure
- Limited scalability
- Simpler for small projects

### Distributed/Microservices System

**What It Is**:
- Multiple individual systems/services
- Connected via network
- Each service is independently deployable
- Separate databases for different services (can share if needed)

**Key Difference from Monolithic**:
Instead of one large system, break into multiple smaller systems deployed separately.

### Advantages of Distributed Systems
1. **Scalability**: Can add more machines horizontally to improve capabilities
    - Example: If Service A is bottleneck, add more instances of Service A

2. **No Single Point of Failure**:
    - If one service goes down, others continue
    - System can continue functioning even with partial failures

3. **Fault Tolerance/Replication**:
    - Can replicate services across multiple machines
    - Data duplication ensures data isn't lost if one machine fails
    - Example: If one database machine fails, data still available on replicated machine

4. **Better Availability**: Because of fault tolerance and no single point of failure

5. **Technology Independence**: Each service can use different technology

### Disadvantages of Distributed Systems
1. **Complexity**: Much harder to design and manage
2. **Network Management**:
    - Inter-service communication over network
    - Need load balancing
    - Need proper network configuration
3. **Debugging Difficulty**: Harder to trace issues across services
4. **Testing Complexity**: Integration testing becomes complex
5. **Security Challenges**: More nodes to secure, more attack surfaces
6. **Consistency Issues**: Ensuring data consistency across services

***

## **VIDEO 4: WHAT IS LATENCY IN NETWORKING (5:20)**

### Definition
**Latency** = Time taken for a request to go from client to server and back (round-trip time)

### Formula
Latency = Network Delay (T1) + Processing Delay (T2) + Computation Delay (T3)

### Breaking It Down
- **Network Delay**: Time for data to travel from client to server
- **Processing/Computation Delay**: Time server takes to process request and prepare response
- **Return Delay**: Time for response to travel back to client

### Latency in Different Architectures

**Monolithic**:
- Lower network latency
- No inter-service network calls (within same deployed unit)
- Only initial client-to-server call and server-to-client response
- Process-to-process calls (within memory) - no network overhead

**Distributed/Microservices**:
- Higher network latency
- Initial request-response network cost
- Additional network calls between services
- **In-between network calls add up**: Each service-to-service communication adds latency

### How to Reduce Latency

**1. Caching**
- Store frequently accessed data closer to source
- Definition: Process of storing information for a set period of time on a computer
- Example: If a function returns same result for same input, cache the result
- Benefit: Avoid repeating expensive computation
- Location: Can implement on server itself
- Use Case: Dynamic data that changes frequently

**2. CDN (Content Delivery Network)**
- Store static content geographically closer to users
- Example: Store website images/videos in servers near user's location
- Benefit: Reduce distance data travels, thus reduce latency
- Use Case: Static data like images, videos, CSS, JS files

**Difference Between Caching and CDN**:
- **Caching**: Server-side optimization, applied on server handling request
- **CDN**: Geographic distribution, places servers closer to users

**3. Hardware Upgrade**
- Improve server capabilities
- Better hardware = faster processing

### Key Takeaway
Distributed systems have inherently higher latency due to network calls between services, but can be mitigated through caching, CDN, and optimized infrastructure.

***

## **VIDEO 5: WHAT IS THROUGHPUT (3:19)**

### Definition
**Throughput** = Volume of work or information flowing through a system per unit of time

### Measurement
- Measured in **BPS (Bits Per Second)**
- Also: Number of requests per second
- Number of transactions per second
- Amount of data transmitted per second

### Example
If you produce 100 videos in 100 days, throughput = 1 video/day

### Throughput in Different Architectures

**Monolithic**:
- Lower throughput
- Limited resources on single machine
- Cannot distribute work

**Distributed Systems**:
- Higher throughput
- Can divide work among multiple machines
- Can add more resources horizontally
- Use load balancer to distribute load

### Reasons for Low Throughput (Bottlenecks)

**1. Latency**:
- High latency reduces throughput
- Request takes long time to complete = fewer requests processed per second

**2. Protocol Overhead**:
- Communication overhead between services
- Handshakes and back-and-forth communication add latency
- Example: TCP handshake has overhead

**3. Congestion**:
- Too many requests arriving at same time
- System cannot process all fast enough
- Creates bottleneck

### How to Improve Throughput

**1. Use Caching**
- Avoid recomputation
- Faster response = more requests processed

**2. Use CDN**
- Faster content delivery
- Reduce latency = increase throughput

**3. Implement Distributed Architecture**
- Instead of monolithic
- Divide load among multiple machines
- Use load balancer to distribute requests

**4. Use Load Balancer**
- Distributes incoming requests among multiple servers
- Example: Round-robin distribution

**5. Upgrade Hardware**
- Better servers can handle more load
- Increase processing capability

### Key Relationship
- **Lower Latency** → **Higher Throughput**
- **Higher Latency** → **Lower Throughput**

***

## **VIDEO 6: WHAT IS AVAILABILITY (5:35)**

### Definition
**Availability** = Measure of how long a system is operational and accessible to users

### Example
- When CBSE announces results, their website crashes due to high traffic
- System becomes unavailable
- User experience suffers
- Goal: Keep system available all the time

### Availability in Different Architectures

**Monolithic**:
- **Low availability**
- Single point of failure
- If main server goes down → entire system is down
- No fallback mechanism

**Distributed Systems**:
- **High availability**
- No single point of failure
- Can handle server failures gracefully
- System continues functioning with partial failures

### Fault Tolerance
**Definition**: Ability of system to continue functioning even when components fail

**Relationship**: Fault Tolerance ∝ Availability
- Better fault tolerance = Higher availability
- Higher fault tolerance = System remains available longer

### Achieving High Availability

**1. Replication**
- Copy data/services across multiple machines
- If one machine fails, others continue
- Example: Database replication across 3 servers

**2. Redundancy**
- Have backup/duplicate components
- When primary fails, switch to backup
- Example: Backup servers ready to take over

### Replication vs Redundancy

**Replication** (Active-Active):
- Multiple copies of same service running simultaneously
- All copies are active
- All process requests
- Example: 3 instances of database running, all taking writes
- Used for: Applications, services that can handle simultaneous copies

**Redundancy** (Active-Passive):
- Primary system is active, backup system is passive (standby)
- Backup only takes over when primary fails
- Example: Primary database active, secondary database in standby waiting
- Used for: Databases, where you need single source of truth
- Benefit: Simpler to manage consistency

### Key Concept
To increase availability in distributed systems:
- Replicate services across multiple nodes
- Replicate databases (but carefully for consistency)
- Use fault-tolerant architecture
- Design for graceful degradation

***

## **VIDEO 7: WHAT IS CONSISTENCY IN SYSTEM DESIGN (6:22)**

### Definition
**Consistency** = All nodes/databases have the same data at the same time

### Real-World Example
- You deposit money in bank account
- All branches should show updated balance immediately
- Inconsistency: One branch shows old balance, another shows new balance

### Consistency in Distributed Systems

When you have multiple databases replicated across different nodes, keeping them consistent becomes critical.

### Types of Consistency

**1. Strong Consistency (Immediate Consistency)**
- **Definition**: Data is immediately consistent across all nodes
- Changes propagate instantly to all replicas
- All reads return latest written value
- **Approach**: Synchronous replication
    - Write happens on all nodes before returning to client
    - Slower but guaranteed consistency
- **Use Case**: Banking systems, critical operations where accuracy is paramount
- **Trade-off**: Higher latency (slower operations)

**2. Eventual Consistency**
- **Definition**: Data becomes consistent across nodes over time (not immediately)
- Write succeeds on primary node
- Replicas get updated asynchronously
- **Approach**: Asynchronous replication
    - Write happens on primary, returns immediately
    - Replicas update in background
- **Advantage**: Fast writes, high throughput
- **Disadvantage**: Brief inconsistency window (reads might get stale data)
- **Use Case**:
    - Social media (likes, comments can have brief delay)
    - Caching systems
    - Analytics systems
    - Systems where eventual correctness is acceptable

### CAP Theorem Connection
This video introduces concept that different systems prioritize different properties (consistency, availability, partition tolerance) differently based on use case.

### When to Use Which

**Strong Consistency**:
- Financial transactions
- Critical operations
- Where immediate accuracy is essential
- Acceptable latency trade-off

**Eventual Consistency**:
- Social media updates
- User preferences
- Analytics
- Where speed is more important than instant accuracy
- Can tolerate temporary inconsistency

***

## **VIDEO 8: WHAT IS CAP THEOREM (10:12)**

### Definition
**CAP Theorem**: In a distributed system, you can guarantee at most **two out of three** of these properties:
- **C**onsistency
- **A**vailability
- **P**artition Tolerance

### Three Properties Explained

**1. Consistency (C)**
- All nodes have same data at same time
- Every read returns most recent write
- Strong consistency
- All replicas in sync

**2. Availability (A)**
- System is always available and responsive
- Every request receives response (success or failure)
- No downtime
- Can handle failures

**3. Partition Tolerance (P)**
- **What is a Network Partition**: Communication breaks down between nodes
- System continues functioning even when network partition occurs
- Some nodes cannot communicate with other nodes
- System doesn't completely fail despite communication breakdown
- **Note**: In distributed systems, partitions **will happen**, so you must handle them

### The Theorem
In presence of network partition (which **WILL happen** in distributed systems), you must choose between:

**Option 1: CP (Consistency + Partition Tolerance)**
- Sacrifice Availability
- When partition happens, system becomes unavailable rather than serving stale data
- Ensures consistency even if unavailable
- Example: Traditional SQL databases

**Option 2: AP (Availability + Partition Tolerance)**
- Sacrifice Consistency
- System stays available and responsive
- Serves potentially stale data during partition
- Consistency achieved eventually
- Example: NoSQL databases, DynamoDB, Cassandra

**Option 3: CA (Consistency + Availability)**
- Sacrifice Partition Tolerance
- Only works in non-distributed systems (single node)
- Not viable for true distributed systems
- Partitions will happen, so this is theoretical

### Practical Implications

**During Normal Operations** (No Partition):
- Can have all three properties

**During Network Partition**:
- MUST choose between Consistency and Availability
- Cannot have both

### Common Database Choices

**CP Systems** (Consistent, Partition Tolerant):
- MongoDB
- PostgreSQL
- Redis
- Become unavailable during partitions but maintain consistency

**AP Systems** (Available, Partition Tolerant):
- Cassandra
- DynamoDB
- Couch DB
- Remain available but data might be inconsistent temporarily

### Design Decision
System designers must decide:
- Do I need immediate consistency? → Choose CP
- Do I need continuous availability? → Choose AP

### Key Insight
You **cannot** have all three in a distributed system. Network partitions are inevitable, so you pick your trade-off.

***

## **VIDEO 9: WHAT IS LAMPORT LOGICAL CLOCK (3:08)**

### Problem It Solves
In distributed systems, you need ordering of events. But physical clocks on different servers can be out of sync.

### Definition
**Lamport Logical Clock**: A mechanism to order events in a distributed system without relying on physical clocks.

### How It Works
- Each process/node maintains a counter (logical clock)
- Counter increments:
    - By 1 whenever local event happens
    - To max(own counter, received message counter) + 1 when receiving message from another process
- Creates partial ordering of events
- Helps determine causality

### Why It Matters
- Ensures events can be ordered
- Helps with consistency maintenance
- Enables replication and synchronization in distributed systems

### Example
- Process A sends message with timestamp T1
- Process B receives it
- Process B updates its clock to T1+1
- Maintains logical ordering of events

***

## **VIDEO 10: DIFFERENCE BETWEEN HORIZONTAL AND VERTICAL SCALING (4:19)**

### Two Types of Scaling

**1. Vertical Scaling (Scale Up)**
- Add more resources to single machine
- Increase CPU, RAM, Storage of existing server
- Makes existing server more powerful
- Example: Upgrade server from 8GB to 32GB RAM

**Advantages**:
- Simple to implement
- No code changes needed


# COMPREHENSIVE SYSTEM DESIGN PLAYLIST NOTES (34 VIDEOS)
## Engineering Digest - Complete Detailed Notes

***

## **VIDEO 1: WHAT IS SYSTEM DESIGN PROCESS (2:21)**

**Definition**: System design is the process of designing different elements of a system - defining the internal design details for building an application.

**Key Elements**:
- Architecture of application
- Modules/components
- Data storage approach
- Communication between modules

**Two Types**:
1. **HLD (High-Level Design)**: Architecture, modules, communication patterns, database selection
2. **LLD (Low-Level Design)**: Business logic, actual code, algorithms, implementation details

***

## **VIDEO 2: MONOLITHIC ARCHITECTURE (8:50)**

**Definition**: All components (Frontend, Backend, Database) combined into single deployed unit.

**Structure**: Single codebase, single deployment, single technology stack

**Advantages**:
- Simpler integration testing
- Easier to understand
- Minimal network calls
- Simpler management

**Disadvantages**:
- Single point of failure
- Must redeploy entire system for any change
- Cannot change technology for individual components
- Limited scalability (vertical only)
- Technology lock-in

***

## **VIDEO 3: MONOLITHIC VS MICROSERVICES ARCHITECTURE (5:17)**

**Monolithic**:
- All modules in single unit
- Single point of failure
- Must scale entire application

**Distributed/Microservices**:
- Multiple independent services
- Each service separate deployment
- No single point of failure
- Fault tolerance through replication

**Advantages of Distributed**:
- Scalability (horizontal scaling possible)
- No single point of failure
- Fault tolerance via replication
- Technology independence
- Better availability

**Disadvantages of Distributed**:
- Complexity
- Network management needed
- Debugging harder
- Integration testing complex
- Security challenges
- Data consistency issues

***

## **VIDEO 4: WHAT IS LATENCY IN NETWORKING (5:20)**

**Definition**: Round-trip time for request to travel from client to server and back

**Formula**: Latency = Network Delay + Processing Delay + Computation Delay

**In Different Architectures**:
- **Monolithic**: Lower latency (no inter-service calls, process-to-process communication)
- **Distributed**: Higher latency (multiple inter-service network calls)

**How to Reduce Latency**:
1. **Caching**: Store frequently accessed data locally, avoid recomputation
2. **CDN**: Geographically distribute static content closer to users
3. **Hardware Upgrade**: Better servers process faster

***

## **VIDEO 5: WHAT IS THROUGHPUT (3:19)**

**Definition**: Volume of work/information flowing through system per unit time

**Measurement**: BPS (Bits Per Second), requests/second, transactions/second

**In Different Architectures**:
- **Monolithic**: Lower throughput (limited resources)
- **Distributed**: Higher throughput (horizontal scaling, load balancing)

**Bottlenecks**:
1. **Latency**: High latency reduces throughput
2. **Protocol Overhead**: Communication handshakes add delay
3. **Congestion**: Too many requests, system can't process fast enough

**How to Improve**:
- Caching
- CDN
- Distributed architecture
- Load balancer
- Hardware upgrade

**Key Relationship**: Lower Latency → Higher Throughput

***

## **VIDEO 6: WHAT IS AVAILABILITY (5:35)**

**Definition**: How long system is operational and accessible to users

**In Different Architectures**:
- **Monolithic**: Low availability (single point of failure)
- **Distributed**: High availability (no single point of failure, fault tolerance)

**Fault Tolerance**: Ability to continue functioning when components fail

**Achieving High Availability**:
1. **Replication**: Copy data/services across multiple machines
2. **Redundancy**: Have backup components ready

***

## **VIDEO 7: CONSISTENCY IN SYSTEM DESIGN (6:22)**

**Definition**: All nodes have same data at same time

**Types**:
1. **Strong Consistency**: Data immediately consistent across all nodes
    - Synchronous replication
    - Slower writes, guaranteed accuracy
    - Use: Banking, critical operations

2. **Eventual Consistency**: Data becomes consistent over time
    - Asynchronous replication
    - Fast writes, brief inconsistency window
    - Use: Social media, caching, analytics

***

## **VIDEO 8: CAP THEOREM (10:12)**

**The Theorem**: In distributed systems, can guarantee **at most 2 of 3** properties:
- **Consistency (C)**: All nodes same data
- **Availability (A)**: System always responsive
- **Partition Tolerance (P)**: System functions despite network partitions

**During Network Partition, Choose**:
- **CP**: Consistent but unavailable (SQL databases)
- **AP**: Available but possibly inconsistent (NoSQL databases)

**Key Insight**: Partitions WILL happen, so choose your trade-off

***

## **VIDEO 9: LAMPORT LOGICAL CLOCK (3:08)**

**Purpose**: Order events in distributed system without relying on physical clocks

**How**: Each node maintains counter that increments on local events and receives max of counters on messages

**Benefit**: Enables event ordering and helps with consistency/replication

***

## **VIDEO 10: HORIZONTAL VS VERTICAL SCALING (4:19)**

**Vertical Scaling (Scale Up)**:
- Add resources to single machine (CPU, RAM, Storage)
- Simple but has limits
- Single point of failure remains

**Horizontal Scaling (Scale Out)**:
- Add more machines
- Distributes load
- No inherent limit
- More complex

***

## **VIDEO 11: REDUNDANCY VS REPLICATION (7:56)**

**Redundancy**: Simple duplication of nodes

**Types**:
1. **Active**: All nodes simultaneously active, accepting requests
2. **Passive**: One primary active, others standby

**Replication**: Redundancy + Synchronization

**Key Difference**:
- **Redundancy**: Just copying
- **Replication**: Copying + keeping in sync

**Types of Replication**:
1. **Active Replication**: All replicas handle reads/writes
2. **Passive (Master-Slave)**:
    - Master handles all reads/writes
    - Slaves are backup
    - **Synchronous**: Slaves updated immediately
    - **Asynchronous**: Slaves updated later

***

## **VIDEO 12: LOAD BALANCER (10:18)**

**Definition**: Distributes incoming requests across multiple servers

**How It Works**:
1. User sends request to VIP (Virtual IP)
2. Load balancer intercepts
3. Decides which server handles it
4. Forwards request
5. Returns response to user

**Roles**:
- Equal load distribution
- Health checks (removes unhealthy servers)
- High availability
- Horizontal scalability
- Redundancy possible (active-passive LB)

**Algorithms**:

**Static**:
1. **Round Robin**: Rotation through servers
2. **Weighted Round Robin**: Proportional to capacity
3. **IP Hash**: Consistent routing based on client IP
4. **Source IP Hash**: Consistent based on source+destination

**Dynamic**:
1. **Least Response Time**: Route to fastest server
2. **Least Connections**: Route to least busy server

***

## **VIDEO 13: CACHING (7:36)**

**Definition**: Storing data temporarily for quick retrieval

**Why It Works**:
- Reduces database load
- Faster response (RAM > Database)
- Fewer API calls
- Fewer code executions

**How**:
1. First request → Database → Cache
2. Subsequent requests → Cache (fast)
3. When data changes → Invalidate cache

**Types**:
1. **In-Memory/Local**: Single server cache
2. **Distributed**: Shared across servers (Redis, Memcached)

**Caching At Different Layers**:
- Database query results
- Application responses
- Static content (CDN)

**When to Use**:
- Read-heavy applications
- Static content
- Expensive computations
- Frequently accessed data

***

## **VIDEO 14: CACHE EVICTION TECHNIQUES (3:57)**

**Why Needed**: Cache has limited size, must manage what stays/goes

**Strategies**:

1. **LRU (Least Recently Used)**: Remove data not used longest
2. **MRU (Most Recently Used)**: Remove most recently used (rare, specific use cases)
3. **LFU (Least Frequently Used)**: Remove least accessed data
4. **FIFO (First In First Out)**: Remove oldest data
5. **LIFO (Last In First Out)**: Remove newest data
6. **Random Replacement**: Random eviction

**When to Use Each**:
- **LRU**: Most common, good general strategy
- **LFU**: When access frequency matters (popular vs unpopular)
- **FIFO/LIFO**: Simple scenarios
- **Random**: When no pattern exists

***

## **VIDEO 15: FILE-BASED STORAGE SYSTEM (1:58)**

**Definition**: Data stored in files on file system

**Characteristics**:
- Simple retrieval
- No complex queries
- Basic operations
- Foundation for databases

***

## **VIDEO 16: CAN RDBMS SCALE HORIZONTALLY? (7:00)**

**Challenge**: Scaling SQL databases horizontally is difficult

**Why**:
- ACID properties require single source of truth
- Distributed transactions complex
- Consistency hard to maintain across nodes
- Vertical scaling easier

**Solutions**:
- Replication (read replicas)
- Sharding (distribute data)
- Read replicas for read scaling

***

## **VIDEO 17: TYPES OF NOSQL DATABASES (6:45)**

**Categories**:

1. **Key-Value Stores**: Redis, Memcached
    - Fast access
    - Simple structure

2. **Document Stores**: MongoDB, CouchDB
    - Flexible schema
    - JSON-like documents

3. **Column-Family**: Cassandra, HBase
    - Optimized for reads
    - Wide tables

4. **Graph Databases**: Neo4j
    - Relationship queries
    - Complex associations

**Advantages**:
- Horizontal scalability
- Flexible schema
- High availability
- Performance optimization

***

## **VIDEO 18: POLYGLOT PERSISTENCE (1:59)**

**Definition**: Using different database technologies for different purposes

**Rationale**:
- Different use cases need different databases
- Optimize each system for its purpose

**Example**:
- SQL for transactional data
- NoSQL for flexible data
- Graph DB for relationships
- Cache for frequent reads

***

## **VIDEO 19: DENORMALIZATION IN RDBMS (4:43)**

**Definition**: Intentionally duplicating data to improve query performance

**Trade-off**:
- **Pro**: Faster reads, fewer joins
- **Con**: Redundancy, update complexity, storage

**When to Use**:
- Read-heavy applications
- When join performance critical
- Analytics systems

***

## DATABASE INDEXING

**Purpose**: Speed up data retrieval from databases
Create index on frequently queried columns : If I have a column of salary, want to use WHERE on salary column, instead of going to  all the rows with O(n) we prefer to use use create separate salary column where the salary will be stored in sorted manner, to now it will be O(log n) to iterate, this memory space also consist of pointer that points to the memory space of row that salary is associated with, this is called indexing.

**How It Works**:
- Create index on frequently queried columns
- Allows faster lookups (B-tree, hash tables)
- Trade-off: Faster reads, slower writes

**Types**:
- Primary key index
- Secondary indexes
- Composite indexes
- Full-text indexes

**Benefits**:
- Dramatically faster reads/queries on indexed columns
- Essential for large datasets

**Disadvantages**
- Should not be used for write intensive data - as first we have to lookup, then add row, and then sort the index as well. 
- Slower writes and extra storage as the indexed column's data is stored in both the table and the index.  



***

## SYNCHRONOUS COMMUNICATION 

**Definition**: Sender waits for receiver response before continuing to maintain HIGH CONSISTENCY.

**Characteristics**:
- Blocking operation
- Immediate response expected
- Simple to implement
- Tightly coupled services

  
**Disadvantages:** 
- Tight coupling
- blocking behavior  
- cascading latency  

**Examples:** REST API call to payment gateway.

**Use Case**: Stock Market, REST APIs, HTTP requests

***

## ASYNCHRONOUS COMMUNICATION

**Definition**: Sender continues without waiting for response where Computation take a lot of time, it can continue with other tasks.

**Characteristics**:
- Non-blocking
- Response comes later
- Loosely coupled
- More complex

**Implementations**:
- Message queues (RabbitMQ)
- Pub/sub (Kafka, Redis)
- Event streaming

**Advantages**:
- Decoupling
- Better scalability
- Resilience
- Avoid Cascading failure 
  - A cascading failure is a chain reaction where the failure of one part of a system triggers the failure of other, dependent parts, eventually leading to a widespread outage. 
  - Think of it like a series of dominoes falling.
  
  **How It Happens in Synchronous Systems**
  - In a synchronous, tightly-coupled architecture, services often make blocking calls to each other.
- 1. Service A needs data from Service B, so it sends a request and waits.
- 2. Service B becomes slow or fails entirely.
- 3. Service A is now stuck, holding onto resources (like connection threads) while it waits for a response that will never come.
- 4. As more requests come into Service A, all its available threads get blocked waiting for Service B.
- 5. Service A runs out of resources and becomes unresponsive. It fails.
- 6. Now, if Service C depends on Service A, it will also start to fail.The initial problem in Service B has cascaded, taking down Service A and then Service C.
  
    **How Asynchronous Communication Prevents This**
  - Asynchronous communication, typically using a message queue (like RabbitMQ or Kafka), decouples the services.
- 1. Service A needs something done by Service B. Instead of calling it directly, it publishes a message to a queue and immediately moves on. It doesn't wait. 
- 2. Service B is down or slow.
- 3. The message simply sits in the queue. The queue acts as a buffer or a shock absorber.
- 4. Service A is completely unaffected. It doesn't know or care that Service B is down. It continues to function normally, accepting requests and adding new messages to the queue.
- 5. When Service B recovers, it can start processing the backlog of messages from the queue at its own pace.The failure is isolated. 
  - The message queue breaks the chain of dependencies, preventing the failure of one service from cascading to others. This makes the entire system more resilient and robust.
 
***

## **VIDEO 23: MESSAGE-BASED COMMUNICATION (2:15)**

**Definition**: Services communicate via messages
- client send the message in form of request adn move on,
- it does not wait for the response
- async communication

**Benefits**:
- Asynchronous by nature
- Decoupling
- Scalability
- Resilience to failures

**Examples**: Kafka, RabbitMQ, AWS SQS

p2p model - point to point communication - one point to another - eg. email
pub sub model - publish subscribe model - newsletter - one producer but multiple consumer

NOTE -
Sending an email to 5 people in CC is conceptually a one-to-many broadcast, which makes it behave like a Publish-Subscribe pattern. 
However, its underlying implementation is a series of Point-to-Point connections.
Let's break that down:
1.Why it's like Publish-Subscribe (The Conceptual Pattern):
•Publisher: You, the sender, create one message.•
Subscribers: The 5 recipients.
•Act: You "publish" the message once, and the email system ensures all "subscribers" get a copy. 
The core idea of "one-to-many" is fulfilled.
2.Why it's NOT a classic Pub/Sub System:
•In a true Pub/Sub system (like Kafka or a newsletter), the publisher is decoupled from the subscribers. The publisher sends a message to a "topic," and has no idea who or how many subscribers will receive it.
•In the email CC case, you (the publisher) are tightly coupled to the subscribers because you explicitly define the entire recipient list.
3.How it's actually implemented (Point-to-Point):•When you hit "send," your email server (the SMTP server) doesn't just broadcast the email into the void.
•It looks at your recipient list (To and CC).
•It then establishes a separate, Point-to-Point connection to the mail server of each recipient and delivers a copy of the email. It essentially says, "Here is a message for recipient1@domainA.com," closes the connection, and then opens a new one: "Here is a message for recipient2@domainB.com."

***

## WEB SERVER  

**Definition**: Server that handles HTTP requests and serves content

**Characteristics**:
- Receives requests
- Processes/serves responses
- Can serve static/dynamic content

**Examples**: Nginx, Apache, Node.js

***

## COMMUNICATION PROTOCOLS 

**Purpose**: Define how systems communicate

**Common Protocols**:
- **HTTP/HTTPS**: Web communication
- **TCP/IP**: Network layer
- **WebSocket**: Real-time bidirectional
- **gRPC**: High-performance RPC

***

## **VIDEO 26: REST API, SOA, MICROSERVICES, TIER ARCHITECTURE (11:13)**

**REST API**: Architectural style using HTTP methods (GET, POST, PUT, DELETE)

**SOA (Service-Oriented Architecture)**: Services accessible via network interfaces

**Microservices**: Fine-grained, independently deployable services

**Tier Architecture**:
- Presentation tier
- Application tier
- Data tier

***

## **VIDEO 27: AUTHENTICATION VS AUTHORIZATION (2:03)**

**Authentication**: Verifying user identity (who are you?)

**Authorization**: Verifying permissions (what can you do?)

***

## **VIDEO 28: BASIC AUTHENTICATION (1:05)**

**Method**: Username and password encoded in request

**Security**: Low (credentials in every request)

***

## **VIDEO 29: TOKEN-BASED AUTHENTICATION (1:16)**

**Method**: Exchange credentials for token, use token for requests

**Benefits**: Stateless, can expire tokens, more secure

***

## **VIDEO 30: OAUTH AUTHENTICATION (1:59)**

**Purpose**: Delegated authorization without sharing credentials

**Flow**: Redirect to provider, get access token

**Use**: Third-party integrations (Google, GitHub login)

***

## **VIDEO 31: FORWARD PROXY (3:21)**

**Definition**: Proxy on client side, acts on behalf of client

**Use Cases**:
- Privacy (hide client IP)
- Caching
- Content filtering

***

## **VIDEO 32: REVERSE PROXY (4:16)**

**Definition**: Proxy on server side, acts on behalf of servers

**Use Cases**:
- Load balancing
- SSL termination
- Caching
- Compression

**Examples**: Nginx, HAProxy

***

## **VIDEO 33: URL SHORTENER SYSTEM DESIGN**

**Requirements**:
- Convert long URL to short
- Redirect short to long
- High availability
- Low latency

**Key Decisions**:
- Hash algorithm
- Database design
- Caching strategy
- Redirect mechanism

***

## **VIDEO 34: DROPBOX/GOOGLE DRIVE SYSTEM DESIGN (11:49)**

**Requirements**:
- File upload/download
- Sync across devices
- Sharing
- Version control
- Availability & consistency

**Key Components**:
- Object storage (S3-like)
- Metadata database
- Sync service
- Notification system
- Caching layer

**Architecture Considerations**:
- Replication for availability
- Eventual consistency for sync
- CDN for downloads
- Message queues for async operations

***

## **SUMMARY OF KEY CONCEPTS**

### Scalability
- Vertical: Add resources to single machine
- Horizontal: Add more machines
- Load balancing distributes load

### Availability & Fault Tolerance
- Replication: Copies of data/services
- Redundancy: Backup components
- No single point of failure design

### Consistency
- Strong: Immediately consistent
- Eventual: Consistent over time
- CAP theorem guides trade-offs

### Performance
- Caching: Fast retrieval
- CDN: Geographic distribution
- Indexing: Database


------

\#\#\# System Design Process  
\- Why necessary: Provides structured approach to decompose requirements into architecture and implementation decisions; reduces risk and rework.  
\- Advantages: Aligns stakeholders, clarifies components, guides tech selection and scalability planning.  
\- Disadvantages: Can be time-consuming; over-design possible for small projects.  
\- Unique: Bridges requirements to concrete architecture and LLD.  
\- Examples: Designing an e\-commerce checkout flow, choosing database + cache + API design.

\#\#\# Monolithic Architecture  
\- Why necessary: Simple initial delivery model for small apps; minimizes infra complexity.  
\- Advantages: Easier testing, deployment, debugging; single codebase.  
\- Disadvantages: Hard to scale per component, single point of failure, large codebase complexity.  
\- Unique: Single deployable unit with tight coupling.  
\- Examples: Early startup backend serving web + API + DB in one app.

\#\#\# Distributed / Microservices Architecture  
\- Why necessary: Enables independent scaling, tech heterogeneity and team autonomy for large systems.  
\- Advantages: Horizontal scaling, fault isolation, technology independence.  
\- Disadvantages: Operational complexity, network latency, harder debugging and consistency.  
\- Unique: Fine\-grained independently deployable services communicating over network.  
\- Examples: Separate order, payment, recommendation services in an online store.

\#\#\# Latency  
\- Why necessary: Critical user experience and throughput determinant; drives design choices.  
\- Advantages (when optimized): Faster responses, improved UX, higher throughput.  
\- Disadvantages (if high): Poor UX, reduced throughput, timeout/failure risk.  
\- Unique: Measured as RTT = network + processing + computation delay.  
\- Examples: CDN reduces latency for static assets; RPC chaining increases latency.

\#\#\# Throughput  
\- Why necessary: Measures system capacity and helps size infrastructure.  
\- Advantages (high): Handles more requests, supports growth.  
\- Disadvantages (if low): Bottlenecks, poor scalability.  
\- Unique: Related but distinct from latency; throughput = work/time.  
\- Examples: Requests per second for an API; BPS for network links.

\#\#\# Availability  
\- Why necessary: Ensures users can access service when needed; business critical.  
\- Advantages: Better user trust, revenue continuity.  
\- Disadvantages: High availability often increases cost and complexity.  
\- Unique: Achieved via redundancy/replication and failover strategies.  
\- Examples: Multi\-AZ deployments, active/passive DB failover.

\#\#\# Fault Tolerance  
\- Why necessary: Allows graceful operation during partial failures.  
\- Advantages: Improves availability and resilience.  
\- Disadvantages: Added complexity in state management and testing.  
\- Unique: Design for graceful degradation instead of full failure.  
\- Examples: Circuit breakers, retries with backoff, degraded feature mode.

\#\#\# Consistency (Strong vs Eventual)  
\- Why necessary: Determines correctness of reads/writes across replicas.  
\- Advantages (Strong): Immediate correctness for critical ops.  
\- Advantages (Eventual): Higher availability and throughput.  
\- Disadvantages (Strong): Higher latency, worse availability under partition.  
\- Disadvantages (Eventual): Temporary stale reads, harder reasoning about state.  
\- Unique: Tradeoff between correctness and performance/availability.  
\- Examples: Banking uses strong consistency; social feeds often use eventual.

\#\#\# CAP Theorem  
\- Why necessary: Guides trade\-offs in distributed DB/system design under partitions.  
\- Advantages: Clarifies which two properties to prioritize given real network partitions.  
\- Disadvantages: Limits guarantees; forces design compromises.  
\- Unique: Formalizes impossibility of C, A and P simultaneously under partition.  
\- Examples: Cassandra (AP), Traditional SQL in some configs (CP).

\#\#\# Lamport Logical Clock  
\- Why necessary: Orders distributed events without synchronized physical clocks.  
\- Advantages: Simple causality ordering, useful for concurrency control.  
\- Disadvantages: Produces partial order only; cannot capture real time.  
\- Unique: Uses logical counters and max+1 update rule on message receive.  
\- Examples: Event timestamping for distributed logs, conflict resolution hints.

\#\#\# Vertical Scaling  
\- Why necessary: Quick way to increase capacity by beefing up a single machine.  
\- Advantages: Simpler to implement, no code changes.  
\- Disadvantages: Upper hardware limits, single point of failure.  
\- Unique: Scale up, not out.  
\- Examples: Upgrading server RAM/CPU for a DB instance.

\#\#\# Horizontal Scaling  
\- Why necessary: Enables near\-unbounded capacity via adding nodes.  
\- Advantages: Fault isolation, elastic scaling, no single machine limit.  
\- Disadvantages: Requires distributed coordination, load balancing, and data partitioning.  
\- Unique: Scale out across machines.  
\- Examples: Auto\-scaling web server fleet behind a load balancer.

\#\#\# Redundancy vs Replication  
\- Why necessary: Provide backups and synchronized copies for availability and durability.  
\- Advantages (Redundancy): Simple failover.  
\- Advantages (Replication): Active load handling and higher availability.  
\- Disadvantages: Replication requires synchronization and consistency management.  
\- Unique: Replication implies keeping copies in sync; redundancy may be passive copy.  
\- Examples: Active\-active DB cluster (replication), primary/standby (redundancy).

\#\#\# Load Balancer  
\- Why necessary: Distributes traffic and hides backend topology.  
\- Advantages: Improved availability, horizontal scaling, health checks.  
\- Disadvantages: Can be a single point of failure if not redundant; adds routing complexity.  
\- Unique: Supports many algorithms (round robin, least connections, IP hash).  
\- Examples: Nginx, HAProxy, cloud LB services.

\#\#\# Caching  
\- Why necessary: Reduces load and latency by serving frequent data from faster storage.  
\- Advantages: Faster responses, lower DB load, improved throughput.  
\- Disadvantages: Staleness, invalidation complexity, memory cost.  
\- Unique: Multi\-layer placement (local, distributed, CDN).  
\- Examples: Redis for session/cache, in\-process memoization.

\#\#\# CDN (Content Delivery Network)  
\- Why necessary: Reduces latency by serving static content from geo\-distributed edges.  
\- Advantages: Faster delivery to global users, offloads origin.  
\- Disadvantages: Cache invalidation complexity; not suitable for highly dynamic content.  
\- Unique: Geo\-proximity optimization.  
\- Examples: Cloudflare, Akamai, AWS CloudFront.

\#\#\# Cache Eviction Policies (LRU, LFU, FIFO, etc.)  
\- Why necessary: Cache capacity is limited; need rules to keep most valuable items.  
\- Advantages: Tailored eviction improves hit rate for different access patterns.  
\- Disadvantages: Complexity to implement and tune; wrong policy hurts performance.  
\- Unique: Policies optimize for recency vs frequency vs simplicity.  
\- Examples: LRU for general use, LFU for long‑lived popularity.

\#\#\# File\-Based Storage  
\- Why necessary: Simple, efficient for unstructured or large binary data.  
\- Advantages: Easy storage and retrieval, no DB overhead.  
\- Disadvantages: Harder to query metadata; backup/version complexity.  
\- Unique: Foundation for object stores.  
\- Examples: Storing logs, images on filesystem or S3.

\#\#\# RDBMS Horizontal Scaling Challenges  
\- Why necessary to consider: ACID and single\-source constraints complicate sharding.  
\- Advantages (when doable): Read replicas improve reads; sharding spreads writes.  
\- Disadvantages: Distributed transactions, cross\-shard joins, complex rebalancing.  
\- Unique: Strong consistency and relational features impose limits.  
\- Examples: Read replicas for reporting, sharding by customer id.

\#\#\# Types of NoSQL Databases  
\- Why necessary: Different data models solve different scaling and flexibility needs.  
\- Advantages: Horizontal scalability, flexible schemas, high availability.  
\- Disadvantages: Weaker consistency models, lack of relational features.  
\- Unique: Key\-value, document, column\-family, graph each optimize distinct workloads.  
\- Examples: Redis (key\-value), MongoDB (document), Cassandra (column), Neo4j (graph).

\#\#\# Polygot Persistence  
\- Why necessary: Use best storage per workload rather than one DB fits all.  
\- Advantages: Optimized performance and features per use case.  
\- Disadvantages: Increased operational complexity, data integration overhead.  
\- Unique: Intentional multi\-DB architecture.  
\- Examples: SQL for transactions, Elasticsearch for search, S3 for objects.

\#\#\# Denormalization  
\- Why necessary: Speed up reads by duplicating data to avoid expensive joins.  
\- Advantages: Faster query performance for read\-heavy systems.  
\- Disadvantages: Update complexity, data redundancy and possible inconsistency.  
\- Unique: Trade\-off read speed for write/maintenance cost.  
\- Examples: Precomputed user profile denormalized into posts for faster feed rendering.

\#\#\# Database Indexing  
\- Why necessary: Accelerates lookups on large datasets.  
\- Advantages: Much faster reads/queries on indexed columns.  
\- Disadvantages: Slower writes and extra storage.  
\- Unique: Different index types (B\-tree, hash, full\-text) fit different queries.  
\- Examples: Index on email column for user lookup; composite index for multi\-column filters.

\#\#\# Synchronous Communication  
\- Why necessary: Simple, predictable interactions where immediate response required.  
\- Advantages: Simpler semantics, immediate error handling.  
\- Disadvantages: Tight coupling, blocking behavior and cascading latency.  
\- Unique: Blocking request/response model (e.g., HTTP).  
\- Examples: REST API call to payment gateway.

\#\#\# Asynchronous Communication  
\- Why necessary: Decouples services and improves resilience and throughput.  
\- Advantages: Non\-blocking, better scalability, resilience to transient failures.  
\- Disadvantages: Increased complexity, eventual consistency, harder debugging.  
\- Unique: Loose coupling via messages/events.  
\- Examples: Kafka event stream, RabbitMQ queues.

\#\#\# Message\-Based Communication  
\- Why necessary: Reliable decoupled interactions and background processing.  
\- Advantages: Durable delivery, retry semantics, buffering.  
\- Disadvantages: Operational overhead and eventual consistency.  
\- Unique: Supports pub/sub patterns and async workflows.  
\- Examples: Order events via Kafka, background job queue.

\#\#\# Web Server  
\- Why necessary: Entry point for HTTP requests; serves static and dynamic content.  
\- Advantages: Handles routing, TLS termination, basic load balancing and static serving.  
\- Disadvantages: Can become bottleneck without scaling.  
\- Unique: Orchestrates request handling to application processes.  
\- Examples: Nginx, Apache, Node.js server.

\#\#\# Communication Protocols (HTTP, TCP, WebSocket, gRPC)  
\- Why necessary: Define reliable semantics for data exchange depending on use case.  
\- Advantages: Each protocol optimized for different patterns (stateless HTTP, streaming gRPC).  
\- Disadvantages: Choosing wrong protocol can hurt performance or complexity.  
\- Unique: Protocols offer tradeoffs (binary vs text, streaming vs request/response).  
\- Examples: gRPC for internal microservice RPC, WebSocket for real\-time updates.

\#\#\# REST API, SOA, Microservices, Tiered Architecture  
\- Why necessary: Architectural styles organize services and responsibilities.  
\- Advantages: Clear separation of concerns, reusability, modularity.  
\- Disadvantages: Overhead of service boundaries and contracts, potential latency.  
\- Unique: Varying granularity from coarse SOA to fine microservices; tiers separate presentation, logic, data.  
\- Examples: RESTful catalog API, 3\-tier web apps.

\#\#\# Authentication vs Authorization  
\- Why necessary: Security fundamentals: identify users and enforce permissions.  
\- Advantages: Protects resources and enforces access policies.  
\- Disadvantages: Complexity in identity management, token lifecycle.  
\- Unique: Authentication = who; Authorization = what.  
\- Examples: OAuth for delegation (authn), RBAC for permissions (authz).

\#\#\# Basic Authentication  
\- Why necessary: Simple identity mechanism for quickly protected endpoints.  
\- Advantages: Easy to implement.  
\- Disadvantages: Credentials sent each request (security risk) unless over TLS.  
\- Unique: Credential\-per\-request model.  
\- Examples: Basic auth for internal tools over HTTPS.

\#\#\# Token\-Based Authentication  
\- Why necessary: Stateless authentication for scalable APIs.  
\- Advantages: Scalability, revocable/expiring tokens, no session storage.  
\- Disadvantages: Token theft risk, token revocation complexity.  
\- Unique: Decouples auth state from server via signed tokens (e.g., JWT).  
\- Examples: JWT access/refresh token flows.

\#\#\# OAuth  
\- Why necessary: Delegated authorization without sharing credentials with third parties.  
\- Advantages: Secure delegated access, standardized flows.  
\- Disadvantages: Complex flows to implement correctly.  
\- Unique: Authorization delegation standard for third\-party access.  
\- Examples: "Login with Google" OAuth flows.

\#\#\# Forward Proxy  
\- Why necessary: Intermediary for client requests for caching, filtering or privacy.  
\- Advantages: Client anonymity, caching, policy enforcement.  
\- Disadvantages: Additional latency and configuration.  
\- Unique: Sits on client side.  
\- Examples: Corporate proxy, local caching proxy.

\#\#\# Reverse Proxy  
\- Why necessary: Fronts backend servers for load balancing, TLS termination and caching.  
\- Advantages: Simplifies backend, improves performance and security.  
\- Disadvantages: Another component to manage; potential single point if not redundant.  
\- Unique: Sits in front of servers and hides topology.  
\- Examples: Nginx or HAProxy as ingress for microservices.

\#\#\# URL Shortener Design  
\- Why necessary: Simplify sharing of long URLs and tracking.  
\- Advantages: Small, fast redirects; analytics opportunities.  
\- Disadvantages: Collision handling, misuse/abuse, scaling redirects.  
\- Unique: Compact mapping of long to short with id or hash and TTLs.  
\- Examples: bit\.ly, tinyurl; use base62 encoding and cache redirects.

\#\#\# Dropbox / Google Drive Design  
\- Why necessary: Provide durable, synced file storage across devices.  
\- Advantages: User convenience, versioning, cross\-device sync.  
\- Disadvantages: Complex consistency, conflict resolution, storage costs.  
\- Unique: Combines object storage, metadata DB, sync clients and deduplication.  
\- Examples: S3 for object store, metadata DB for file trees, message queues for sync events.

\#\#\# Summary (Key trade\-offs)  
\- Why necessary: System design is about tradeoffs between latency, throughput, availability, consistency and complexity.  
\- Unique: No one\-size\-fits\-all — choices depend on requirements and constraints.