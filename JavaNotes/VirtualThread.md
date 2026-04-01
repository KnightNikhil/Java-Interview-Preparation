# Virtual Threads (Project Loom, Java 21)

A comprehensive, practical guide to virtual threads: what they are, how they work, where they help, where they hurt, patterns, gotchas, and hands-on exercises.

---

## Table of Contents

- Overview
- Why Virtual Threads Were Introduced
- Virtual Thread Definition
- Platform Thread vs Virtual Thread
- Internal Mechanics
  - Virtual Thread
  - Carrier Thread
  - Blocking and Unmounting
- Blocking Examples
- Creating Virtual Threads
  - Direct API
  - Executor-based (recommended)
- Good Use Cases
- Bad Use Cases
- Synchronization, Pinning, and Deadlocks
- Virtual Threads vs ExecutorService and Async/Reactive
- Real-world Patterns
  - Executor deadlock example
  - Kafka consumers
  - Spring Boot
  - Payment systems
- Performance Tuning & Rules
- Structured Concurrency
- Hands-on Exercises
  - Beginner to Expert
- Key Takeaways
- Next Steps

---

## Overview

Virtual threads (Project Loom, Java 21) are lightweight Java-managed threads scheduled by the JVM rather than the OS. They make blocking operations scalable by unmounting from carrier (OS) threads when parked, enabling massive concurrency for I/O-bound workloads while preserving a synchronous programming model.

---

## Why Virtual Threads Were Introduced

Problems with platform (OS) threads:

- Expensive to create
- Large memory footprint (stacks ~1–2 MB)
- Costly context switches
- Limited scalability (practical limits in thousands)

Real systems (web servers, DB clients, HTTP callers, Kafka consumers) spend most time waiting. Platform threads are blocked during waits, wasting OS resources. Virtual threads solve blocking scalability (not CPU speed).

---

## What Is a Virtual Thread?

A virtual thread is a lightweight Java thread scheduled by the JVM. Key properties:

- Blocking a virtual thread does NOT block an OS thread.
- The JVM parks the virtual thread and reuses the carrier thread for other work.
- Virtual threads are small (storing stack frames and a tiny state, a few KB).

---

## Platform Thread vs Virtual Thread (Mental Model)

| Platform Thread | Virtual Thread |
|---|---|
| OS-managed | JVM-managed |
| Heavy | Extremely lightweight |
| Limited (~thousands) | Millions possible |
| Blocking is expensive | Blocking is cheap |
| 1:1 with OS | Many:1 with OS |

Summary: virtual threads make blocking cheap, not CPU free.

---

## How Virtual Threads Work Internally

Two main components:

1. Virtual Thread (Java object)
   - Holds stack frames and state
   - Small memory footprint

2. Carrier Thread (platform thread)
   - Real OS thread that executes virtual threads

Lifecycle:

- When a virtual thread runs → it is mounted on a carrier thread.
- When it blocks (I/O, sleep, lock) → it is unmounted (parked) and the carrier thread runs other virtual threads.

This unmount/mount mechanism is the core that makes blocking cheap.

---

## What Happens During Blocking

Example:

```java
Thread.sleep(1000);
```

- Platform thread: OS thread blocks for 1s — wasted resource.
- Virtual thread: JVM parks the virtual thread; the carrier thread is reused for other tasks; no OS thread blocked.

This is the fundamental advantage.

---

## Creating Virtual Threads

Java 21 provides direct and executor-based APIs.

Direct API:

```java
java
Thread.ofVirtual().start(() -> {
    System.out.println("Hello from virtual thread: " + Thread.currentThread());
});
```

Executor-based (recommended):

```java
java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

executor.submit(() -> {
    // blocking code is fine
});
```

Use try-with-resources on the executor to ensure shutdown:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> System.out.println("Task 1"));
    executor.submit(() -> System.out.println("Task 2"));
}
```

Each submitted task gets its own virtual thread.

---

## What Virtual Threads Are Good At

- Blocking I/O
- Database calls
- HTTP calls
- Sleep / wait
- High concurrency request handling

Example:

```java
java
executor.submit(() -> {
    callDatabase();
    callExternalAPI();
});
```

This easily scales to hundreds of thousands of concurrent tasks.

---

## What Virtual Threads Are Bad At

- CPU-bound work
- Tight loops
- Heavy computation

Reason: Virtual threads still execute on carrier threads; CPU is finite. For CPU-bound tasks, prefer a fixed-size thread pool.

---

## Synchronization, Pinning, and Deadlocks

- `synchronized` and `ReentrantLock` semantics remain the same.
  - Example `synchronized(lock) { Thread.sleep(1000); }` will pin and block the carrier thread — lock semantics unchanged.
- Pinning: virtual threads cannot be unmounted when:
  - Inside a `synchronized` block
  - Running native code
  - Using some legacy blocking I/O that pins threads
- When pinned:
  - Carrier thread is blocked
  - Scalability is lost

Key: virtual threads fix resource starvation but do not fix logical deadlocks or incorrect locking.

---

## Common Deadlocks and Virtual Threads

1. Executor pool starvation (fixed thread pool example)
  - With platform threads, nested submits that block can deadlock when the pool is small.
  - With virtual threads, each submit creates a new virtual thread; blocking parks the outer virtual thread and lets the inner task run — no starvation deadlock.

2. Logical locking deadlock remains possible:
  - Two tasks locking resources in opposite order still deadlock even with virtual threads.

3. Synchronized + I/O pinning can still create resource contention and effectively starve carrier threads.

---

## Virtual Threads vs ExecutorService

| Feature | Fixed Thread Pool | Virtual Threads |
|---|---:|---:|
| Threads | Limited | Millions |
| Blocking | Bad | Cheap |
| Backpressure | Manual | Required |
| Scheduling | OS | JVM |

Use virtual threads for blocking tasks, but design backpressure explicitly (semaphores, rate limiters, bulkheads).

---

## Virtual Threads vs Reactive Programming

Reactive (e.g., Reactor, WebFlux):

- Non-blocking, event-driven, callback chains
- Harder to debug, steep learning curve
- Built-in backpressure

Virtual Threads:

- Imperative blocking style, linear code
- Easier debugging and reasoning
- Uses JVM scheduling
- Backpressure must be handled by developer

Rule of thumb:
- Virtual threads → business logic, DB, REST
- Reactive → streaming, massive fan-out, event pipelines

---

## Spring Boot + Virtual Threads (Java 21, Spring Boot 3.2+)

Enable virtual threads globally:

```properties
properties
spring.threads.virtual.enabled=true
```

Effect:

- Servlet containers (Tomcat/Jetty) use virtual threads for requests
- One virtual thread per HTTP request
- Blocking controllers scale

Caveats:

- Avoid long `synchronized` blocks with I/O
- Avoid native blocking calls and CPU-heavy loops

---

## Real-world Patterns

### Executor Deadlock → Virtual Threads (Example)

Platform-thread deadlock:

```java
java
ExecutorService executor = Executors.newFixedThreadPool(1);

Future<String> f = executor.submit(() -> {
    Future<String> inner = executor.submit(() -> "Inner");
    return inner.get(); // DEADLOCK with pool size 1
});

System.out.println(f.get());
```

Virtual-thread safe:

```java
java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

Future<String> f = executor.submit(() -> {
    Future<String> inner = executor.submit(() -> "Inner");
    return inner.get(); // SAFE
});

System.out.println(f.get());
executor.shutdown();
```

Why: each submit creates a new virtual thread; blocking parks the outer virtual thread, allowing inner to run.

### Kafka Consumer Pattern

Traditional blocking processing in poll loop causes slowdowns and rebalance risk.

Correct pattern with virtual threads:

```java
java
ExecutorService vExecutor = Executors.newVirtualThreadPerTaskExecutor();

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        vExecutor.submit(() -> process(record));
    }
}
```

Benefits:

- Poll loop stays fast
- Each message processed in its own virtual thread
- Blocking DB/HTTP calls are cheap
- Offsets can be committed safely

### Payment Gateways

Payment flows are typically I/O bound (fraud checks, DB, RPC, notifications). Virtual threads let you keep imperative controllers while scaling massively.

Controller example (safe with virtual threads):

```java
java
@PostMapping("/pay")
public Response pay() {
    fraudCheck();     // blocking
    debitAccount();   // blocking
    creditMerchant(); // blocking
    emitEvent();      // blocking
    return SUCCESS;
}
```

Why prefer virtual threads over reactive here:

- Clear flow, transactions, auditing, and debuggability are often more important than micro-optimizing CPU usage.

---

## Performance Tuning & Rules

Rule 1: Limit CPU-bound work

- Do not run heavy computation on unbounded virtual-thread submission.
- Use a separate fixed thread pool for CPU tasks.

Rule 2: Avoid long synchronized blocks with I/O

- Avoid: `synchronized(lock) { callDatabase(); }` (pins carrier thread).
- Prefer short critical sections and move blocking I/O outside locks.

Rule 3: Use structured concurrency for task orchestration (Java 21+)

```java
java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    scope.fork(this::fraudCheck);
    scope.fork(this::balanceCheck);
    scope.join();
}
```

Benefits: cancellation, error propagation, clearer code than ad-hoc futures.

Rule 4: Backpressure is developer responsibility

- Virtual threads allow millions of concurrent tasks but downstream services cannot handle millions.
- Use semaphores, rate limiters, and bulkheads to protect resources.

---

## Structured Concurrency (Short Example)

```java
java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

    Future<String> user = scope.fork(() -> "User Data");
    Future<String> order = scope.fork(() -> "Order Data");

    scope.join();
    scope.throwIfFailed();

    System.out.println(user.resultNow() + order.resultNow());
}
```

Structured concurrency simplifies managing concurrent subtasks with structured lifetimes and failure handling.

---

## Hands-on Exercises

### Beginner

1. Create a virtual thread:

```java
java
Thread vt = Thread.ofVirtual().start(() -> {
    System.out.println("Running in virtual thread: " + Thread.currentThread());
});
```

2. Virtual thread using executor:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> System.out.println("Task 1"));
    executor.submit(() -> System.out.println("Task 2"));
}
```

3. Compare platform vs virtual:

```java
java
// Platform threads
ExecutorService platform = Executors.newFixedThreadPool(2);

// Virtual threads
ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor();
```

### Intermediate

4. Run 10,000 tasks:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        executor.submit(() -> {
            Thread.sleep(1000);
            return null;
        });
    }
}
```

5. Blocking I/O is cheap:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        Thread.sleep(2000); // cheap in virtual thread
        System.out.println("Done");
    });
}
```

6. Thread-per-request simulation:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 5; i++) {
        int req = i;
        executor.submit(() -> {
            System.out.println("Handling request " + req);
            Thread.sleep(1000);
        });
    }
}
```

### Advanced

7. Structured concurrency example (preview shown earlier)

8. Virtual threads + DB simulation:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        Thread.sleep(2000); // simulate DB
        return "DB Result";
    });
}
```

9. Avoid ThreadLocal misuse:

```java
java
ThreadLocal<String> tl = new ThreadLocal<>();

Thread.ofVirtual().start(() -> {
    tl.set("data");
});
```

Note: virtual threads are short-lived; ThreadLocal usage may be unreliable for lifespan assumptions.

### Expert

10. Massive concurrency test:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 100000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(100);
            return null;
        });
    });
}
```

11. Virtual threads vs CompletableFuture:

```java
java
// Virtual thread (simple)
Thread.ofVirtual().start(() -> callAPI());

// CompletableFuture (complex chaining)
CompletableFuture.supplyAsync(() -> callAPI());
```

12. API aggregation example:

```java
java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

    Future<String> user = executor.submit(() -> "User");
    Future<String> orders = executor.submit(() -> "Orders");

    System.out.println(user.get() + orders.get());
}
```

---

## Key Takeaways

- Virtual threads are lightweight — millions possible.
- Blocking is cheap for virtual threads.
- They simplify async code and reduce the need for complex CompletableFuture chains and reactive plumbing for many use cases.
- They are ideal for I/O-heavy systems, but backpressure, proper locking, and separate CPU pools are still necessary.
- Virtual threads fix resource starvation, not logical concurrency bugs.

---

## Next Steps

- Structured concurrency deep dive
- Virtual threads vs async DB drivers
- Migration strategies from executors to virtual threads
- Production tuning and benchmarking
- Real outage case studies caused by incorrect thread models

---
