# TOP JAVA + MICROSERVICES SCENARIO QUESTIONS (WITH ANSWERS)

I’ve grouped them logically so you can revise fast.

⸻

SECTION 1: API DESIGN & MICROSERVICE BASICS

1. You need to design a “Transfer Money” API. What happens if service crashes after debit but before credit?

Answer:
•	Use distributed transaction handling
•	Prefer Saga Pattern
•	Steps:
•	Debit → Event
•	Credit → Event
•	If credit fails → compensate (refund)

⸻

2. How do you ensure idempotency in payment APIs?

Answer:
•	Use Idempotency Key
•	Store request + response in DB/cache
•	Reject duplicate requests

⸻

3. How do you version APIs?

Answer:
•	URL: /v1/accounts
•	Header-based versioning
•	Avoid breaking changes → backward compatibility

⸻

4. How do you handle partial failures in microservices?

Answer:
•	Circuit Breaker (Resilience4j)
•	Retry with backoff
•	Fallback response

⸻

5. Service A depends on Service B, which is slow. What do you do?

Answer:
•	Timeout + fallback
•	Cache response
•	Bulkhead isolation

⸻

SECTION 2: CONCURRENCY & THREADING

6. Your API latency increased under load. What could be wrong?

Answer:
•	Thread pool exhaustion
•	Blocking calls
•	DB connection pool exhausted

⸻

7. When to use CompletableFuture vs ThreadPool?

Answer:
•	CompletableFuture → async orchestration
•	ThreadPool → task execution control

⸻

8. Why is common ForkJoinPool dangerous in production?

Answer:
•	Shared across app
•	Blocking tasks → starvation

⸻

9. How would you parallelize multiple API calls?

Answer:
•	CompletableFuture.allOf()
•	Combine results after completion

⸻

10. How do you prevent thread starvation?

Answer:
•	Separate thread pools
•	Avoid blocking operations
•	Use virtual threads (Java 21)

⸻

SECTION 3: DATABASE & TRANSACTIONS

11. How do you handle transactions across microservices?

Answer:
•	Avoid 2PC
•	Use Saga Pattern

⸻

12. Why not use distributed transactions (XA)?

Answer:
•	Slow
•	Not scalable
•	Tight coupling

⸻

13. How do you handle DB connection pool exhaustion?

Answer:
•	Increase pool size carefully
•	Optimize queries
•	Use connection timeout

⸻

14. How do you handle duplicate messages in Kafka?

Answer:
•	Idempotent consumers
•	Store processed IDs

⸻

15. How do you ensure consistency?

Answer:
•	Eventual consistency
•	Retry + compensation

⸻

SECTION 4: SYSTEM DESIGN SCENARIOS

16. Design a rate limiter

Answer:
•	Token bucket / Leaky bucket
•	Redis-based counter

⸻

17. How do you handle high traffic spikes?

Answer:
•	Auto-scaling
•	Load balancing
•	Queue buffering (Kafka)

⸻

18. How do you design a notification system?

Answer:
•	Event-driven
•	Kafka → Email/SMS service

⸻

19. How do you handle file uploads in microservices?

Answer:
•	Store in S3
•	Save metadata in DB

⸻

20. How do you design a search system?

Answer:
•	Use Elasticsearch
•	Sync via events

⸻

SECTION 5: RESILIENCE & FAULT TOLERANCE

21. What is Circuit Breaker?

Answer:
•	Stops calls to failing service
•	Prevents cascading failures

⸻

22. Retry vs Circuit Breaker?

Retry	Circuit Breaker
Try again	Stop calling


⸻

23. What is Bulkhead Pattern?

Answer:
•	Isolate resources (thread pools)

⸻

24. What is fallback?

Answer:
•	Default response when service fails

⸻

25. How do you avoid cascading failures?

Answer:
•	Timeouts
•	Circuit breakers
•	Isolation

⸻

SECTION 6: SECURITY

26. How do you secure microservices?

Answer:
•	OAuth2 / JWT
•	API Gateway

⸻

27. What is JWT?

Answer:
•	Stateless authentication token

⸻

28. How do you validate JWT?

Answer:
•	Signature verification
•	Expiry check

⸻

29. How do services trust each other?

Answer:
•	Mutual TLS
•	Service-to-service auth

⸻

30. How do you prevent replay attacks?

Answer:
•	Nonce + timestamp

⸻

SECTION 7: SPRING BOOT SPECIFIC

31. Difference: @RestController vs @Controller

Answer:
•	@RestController → returns JSON
•	@Controller → returns view

⸻

32. @Transactional pitfalls?

Answer:
•	Doesn’t work on private methods
•	Doesn’t work within same class

⸻

33. What is Bean scope?

Answer:
•	Singleton (default), Prototype, Request, Session

⸻

34. How does Spring Boot auto-config work?

Answer:
•	Based on classpath + conditions

⸻

35. What is AOP used for?

Answer:
•	Logging, security, transactions

⸻

SECTION 8: KAFKA & EVENT-DRIVEN

36. Why use Kafka?

Answer:
•	Decoupling
•	Async processing

⸻

37. What is consumer group?

Answer:
•	Parallel consumption

⸻

38. What happens if consumer crashes?

Answer:
•	Rebalance

⸻

39. How do you ensure ordering?

Answer:
•	Partition key

⸻

40. At-least-once vs exactly-once?

Answer:
•	Exactly-once → expensive
•	Use idempotency instead

⸻

SECTION 9: PERFORMANCE & SCALABILITY

41. How do you reduce API latency?

Answer:
•	Caching
•	Parallel calls
•	DB optimization

⸻

42. What is caching strategy?

Answer:
•	Redis / in-memory
•	TTL-based

⸻

43. How do you scale services?

Answer:
•	Horizontal scaling

⸻

44. How do you avoid cache stampede?

Answer:
•	Locking
•	Staggered expiry

⸻

45. What is backpressure?

Answer:
•	Controlling incoming traffic

⸻

SECTION 10: ADVANCED REAL-WORLD SCENARIOS

46. Payment processed twice — what went wrong?

Answer:
•	Missing idempotency

⸻

47. Kafka consumer lag increasing — why?

Answer:
•	Slow processing
•	Fewer consumers

⸻

48. Memory leak in service — how to debug?

Answer:
•	Heap dump
•	Analyze GC

⸻

49. High CPU usage — causes?

Answer:
•	Infinite loops
•	Thread contention

⸻

50. Service returning stale data

Answer:
•	Cache not invalidated

⸻

51–100 (Rapid Fire – Must Know)

I’ll keep them concise but impactful.

⸻

51. Blue-green deployment → zero downtime

52. Canary release → gradual rollout

53. API Gateway → central entry point

54. Service discovery → Eureka

55. Config server → centralized config

56. Load balancing → round-robin / weighted

57. Health checks → actuator

58. Distributed tracing → Zipkin

59. Logging → ELK stack

60. Metrics → Prometheus

61. Timeout → prevents hanging threads

62. Deadlock → cyclic dependency

63. Race condition → concurrent updates

64. Optimistic locking → versioning

65. Pessimistic locking → DB lock

66. CQRS → read/write separation

67. Event sourcing → store events

68. API throttling → limit requests

69. Retry storm → dangerous

70. Bulk insert → batch processing

71. Lazy loading → performance gain

72. N+1 problem → ORM issue

73. Connection timeout → DB safety

74. Thread dump → debugging

75. Heap dump → memory debugging

76. Horizontal vs vertical scaling

77. Stateless services → scalable

78. Sticky sessions → avoid

79. Reverse proxy → nginx

80. DNS load balancing

81. Rate limiting via Redis

82. Graceful shutdown

83. Rolling deployment

84. API pagination

85. Data sharding

86. Read replicas

87. Circuit breaker states

88. Retry backoff

89. Dead letter queue

90. Message ordering

91. Schema evolution

92. Feature flags

93. Chaos testing

94. SLA vs SLO vs SLI

95. Observability

96. Thread safety

97. Immutable objects

98. Connection pooling

99. Async vs sync communication

100. Monolith → microservices migration strategy

⸻

Final Advice (IMPORTANT)

Don’t just memorize — practice answering like:

1. Problem
2. Approach
3. Trade-offs
4. Real-world example


⸻

-------


```markdown
# Top Java + Microservices Scenario Questions (with Answers)

A compact, exam-friendly cheat sheet grouped by topic for fast revision.

---

## Table of Contents

1. API Design & Microservice Basics  
2. Concurrency & Threading  
3. Database & Transactions  
4. System Design Scenarios  
5. Resilience & Fault Tolerance  
6. Security  
7. Spring Boot Specific  
8. Kafka & Event-Driven  
9. Performance & Scalability  
10. Advanced Real-World Scenarios  
11. Rapid Fire (51–100)  
12. Final Advice

---

## SECTION 1: API DESIGN & MICROSERVICE BASICS

### 1. Transfer Money — crash after debit before credit?
- Prefer Saga pattern (avoid 2PC).
- Emit events: Debit → event, Credit → event.
- On credit failure → compensate (refund) or retry with idempotency.

### 2. Ensure idempotency in payment APIs
- Use an idempotency key.
- Persist request/response in DB or cache.
- Reject or return stored response for duplicates.

### 3. Version APIs
- URL: `/v1/accounts` or header-based versioning.
- Keep backward compatibility; avoid breaking changes.

### 4. Handle partial failures
- Circuit breaker (Resilience4j), retry with backoff, and fallback responses.

### 5. Service A depends on slow Service B
- Use timeouts, fallback, caching, and bulkhead isolation.

---

## SECTION 2: CONCURRENCY & THREADING

### 6. API latency increased under load — possible causes
- Thread pool exhaustion, blocking calls, DB connection pool exhausted.

### 7. CompletableFuture vs ThreadPool
- CompletableFuture: async orchestration and composition.
- ThreadPool: explicit task execution control and sizing.

### 8. ForkJoinPool risks
- Common pool is shared; blocking tasks can cause starvation.

### 9. Parallelize multiple API calls
- Use `CompletableFuture.allOf()` and combine results after completion.

### 10. Prevent thread starvation
- Use separate thread pools, avoid blocking operations, consider virtual threads (Java 21).

---

## SECTION 3: DATABASE & TRANSACTIONS

### 11. Transactions across microservices
- Avoid 2PC; use Saga pattern with compensation.

### 12. Why not use distributed transactions (XA)?
- Slow, not scalable, increases coupling.

### 13. DB connection pool exhaustion
- Increase pool size carefully, optimize queries, use connection timeouts.

### 14. Duplicate messages in Kafka
- Idempotent consumers; persist processed message IDs.

### 15. Ensure consistency
- Eventual consistency, retry + compensation.

---

## SECTION 4: SYSTEM DESIGN SCENARIOS

### 16. Design a rate limiter
- Token bucket or leaky bucket; Redis-based counters for distributed limits.

### 17. Handle high traffic spikes
- Auto-scaling, load balancing, queue buffering (e.g., Kafka).

### 18. Notification system
- Event-driven: Kafka → Email/SMS service workers.

### 19. File uploads in microservices
- Store files in S3; save metadata in DB.

### 20. Design a search system
- Use Elasticsearch and keep it synced via events.

---

## SECTION 5: RESILIENCE & FAULT TOLERANCE

### 21. Circuit Breaker
- Stops calls to failing service and prevents cascading failures.

### 22. Retry vs Circuit Breaker
- Retry: try again; Circuit Breaker: stop calling after repeated failures.

### 23. Bulkhead Pattern
- Isolate resources (separate thread pools, connection pools).

### 24. Fallback
- Provide default response when service fails.

### 25. Avoid cascading failures
- Timeouts, circuit breakers, isolation.

---

## SECTION 6: SECURITY

### 26. Secure microservices
- OAuth2 / JWT, API Gateway for centralized auth and rate limiting.

### 27. JWT
- Stateless authentication token.

### 28. Validate JWT
- Verify signature and expiry; validate claims.

### 29. Service trust
- mTLS and service-to-service auth (mutual authentication).

### 30. Prevent replay attacks
- Use nonce + timestamp and track used nonces.

---

## SECTION 7: SPRING BOOT SPECIFIC

### 31. `@RestController` vs `@Controller`
- `@RestController` → JSON responses; `@Controller` → views/templates.

### 32. `@Transactional` pitfalls
- Does not work on private methods or internal calls within the same class.

### 33. Bean scopes
- Singleton (default), Prototype, Request, Session.

### 34. Auto-configuration
- Based on classpath and conditional annotations.

### 35. AOP uses
- Logging, security, transaction management, metrics.

---

## SECTION 8: KAFKA & EVENT-DRIVEN

### 36. Why use Kafka?
- Decoupling, async processing, durability.

### 37. Consumer group
- Enables parallel consumption across consumers.

### 38. Consumer crashes
- Consumer group rebalance; partitions reassigned.

### 39. Ensure ordering
- Use a consistent partition key.

### 40. At-least-once vs exactly-once
- Exactly-once is costly; prefer idempotent consumers and design for at-least-once.

---

## SECTION 9: PERFORMANCE & SCALABILITY

### 41. Reduce API latency
- Caching, parallel calls, DB optimization, connection tuning.

### 42. Caching strategy
- Redis or in-memory caches; TTL-based eviction.

### 43. Scale services
- Horizontal scaling (stateless services) is preferred.

### 44. Avoid cache stampede
- Use locking, request coalescing, and staggered expiry.

### 45. Backpressure
- Control incoming traffic to match processing capacity (queues, rate limiting).

---

## SECTION 10: ADVANCED REAL-WORLD SCENARIOS

### 46. Payment processed twice
- Missing idempotency or duplicate message handling.

### 47. Kafka consumer lag increasing
- Slow processing, insufficient consumers, GC pauses.

### 48. Memory leak debugging
- Generate heap dump, analyze with MAT, inspect long-lived objects and GC logs.

### 49. High CPU usage causes
- Infinite loops, thread contention, expensive synchronous processing.

### 50. Service returning stale data
- Cache not invalidated or event sync failure.

---

## RAPID FIRE (51–100)

- 51: Blue-green deployment — zero downtime  
- 52: Canary release — gradual rollout  
- 53: API Gateway — central entry point  
- 54: Service discovery — Eureka / Consul  
- 55: Config server — centralized configuration  
- 56: Load balancing — round-robin / weighted  
- 57: Health checks — Spring Actuator  
- 58: Distributed tracing — Zipkin / Jaeger  
- 59: Logging — ELK / EFK  
- 60: Metrics — Prometheus / Grafana  
- 61: Timeouts — prevent hanging threads  
- 62: Deadlock — cyclic dependency between locks  
- 63: Race condition — concurrent updates without synchronization  
- 64: Optimistic locking — versioning  
- 65: Pessimistic locking — DB locks  
- 66: CQRS — read/write separation  
- 67: Event sourcing — store domain events  
- 68: API throttling — limit requests per client  
- 69: Retry storm — dangerous; use jitter/backoff  
- 70: Bulk insert — use batching  
- 71: Lazy loading — reduce initial load, beware N+1  
- 72: N+1 problem — ORM fetch strategy issue  
- 73: Connection timeout — protect DB from hanging clients  
- 74: Thread dump — thread-level debugging  
- 75: Heap dump — memory debugging  
- 76: Horizontal vs vertical scaling — prefer horizontal  
- 77: Stateless services — easier to scale and deploy  
- 78: Sticky sessions — avoid for stateless apps  
- 79: Reverse proxy — nginx / envoy  
- 80: DNS load balancing — coarse-grained LB  
- 81: Rate limiting via Redis — distributed counters or token bucket  
- 82: Graceful shutdown — drain connections and complete inflight work  
- 83: Rolling deployment — incremental upgrades  
- 84: API pagination — avoid large payloads  
- 85: Data sharding — scale write throughput  
- 86: Read replicas — scale read traffic  
- 87: Circuit breaker states — CLOSED/OPEN/HALF\_OPEN  
- 88: Retry backoff — exponential + jitter  
- 89: Dead letter queue — failed message handling  
- 90: Message ordering — partitioning strategy  
- 91: Schema evolution — backward/forward compatibility  
- 92: Feature flags — safe rollouts  
- 93: Chaos testing — validate resilience  
- 94: SLA vs SLO vs SLI — contractual vs measurable vs metric  
- 95: Observability — logs, metrics, traces  
- 96: Thread safety — immutable or synchronized state  
- 97: Immutable objects — safer concurrency  
- 98: Connection pooling — reuse DB connections  
- 99: Async vs sync communication — trade-offs in latency/consistency  
- 100: Monolith → microservices migration — incremental extraction by domain

---

## Final Advice (IMPORTANT)
Practice answers using this structure:
1. Problem  
2. Approach  
3. Trade-offs  
4. Real-world example

---

If needed, the document can be converted into mock interview Q&A, diagrams, or company-specific question sets.
```