✅ 1. Java Thread Basics
* Creating threads: Thread, Runnable, Callable, Future
* Thread lifecycle: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
* Thread priorities and scheduling (cooperative vs preemptive)

⸻

✅ 2. Synchronization & Locks
* synchronized keyword (method-level and block-level)
* Intrinsic locks (monitor locks)
* Deadlocks: causes, detection, and prevention
* Thread-safe classes: Vector, Hashtable, etc.
* ReentrantLock and ReentrantReadWriteLock
* Try-lock and lock fairness

⸻

✅ 3. Java Memory Model (JMM)
* Happens-before relationship
* Visibility and atomicity
* Volatile keyword: when and how to use
* False sharing and memory consistency

⸻

✅ 4. Thread Communication
* wait(), notify(), notifyAll() (object monitor methods)
* Producer-consumer problem
* Spurious wakeups and while loop usage
* CountDownLatch, CyclicBarrier, Semaphore, Exchanger

⸻

✅ 5. Executor Framework
* Executor, ExecutorService, ScheduledExecutorService
* ThreadPoolExecutor: corePoolSize, maxPoolSize, queue, rejection policy
* Executors factory methods: newFixedThreadPool, newSingleThreadExecutor, newCachedThreadPool, etc.
* Shutdown vs shutdownNow

⸻

✅ 6. Callable, Future, and FutureTask
* Submitting tasks and retrieving results
* Timeouts and cancellation
* Limitations of Future (no chaining, hard to combine)

⸻

✅ 7. CompletableFuture (Java 8+)
* Asynchronous computation and chaining: thenApply, thenCompose, thenCombine
* handle, exceptionally, whenComplete for error handling
* Running in custom thread pools
* Combining multiple async tasks
* Non-blocking alternatives to Future

⸻

✅ 8. ForkJoin Framework
* ForkJoinPool, RecursiveTask, RecursiveAction
* Work stealing and parallelism
* Difference between ForkJoin and ThreadPoolExecutor

⸻

✅ 9. Parallel Streams
* How parallelism works internally
* When to use and when to avoid
* Thread-safety and shared resources
* Comparison with CompletableFuture and executor services

⸻

✅ 10. ThreadLocal
* Storing per-thread state (like user info, transactions)
* Cleaning up to prevent memory leaks (especially in thread pools)

⸻

✅ 11. Best Practices & Patterns
* Immutable objects in concurrent code
* Use thread-safe collections: ConcurrentHashMap, CopyOnWriteArrayList
* Use atomic classes from java.util.concurrent.atomic package
* Avoid shared mutable state
* Prefer higher-level concurrency utilities

⸻

✅ 12. Common Issues
* Race conditions
* Deadlocks
* Livelocks
* Starvation
* Thread leakage

⸻

✅ 13. Testing & Debugging Concurrent Code
* Thread dumps and stack traces
* Using tools: VisualVM, JConsole, JFR
* Testing multithreaded code deterministically
* Using Awaitility for async test verification