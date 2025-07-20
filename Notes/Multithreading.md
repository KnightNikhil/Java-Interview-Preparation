## ✅ 1. Java Thread Basics

### ✅ Creating Threads

#### A. **Using `Thread` class**

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }
}

public class Main {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        t1.start();  // calls run() in a new thread
    }
}
```

#### B. **Using `Runnable` interface**

```java
Runnable task = () -> System.out.println("Runnable thread: " + Thread.currentThread().getName());
Thread thread = new Thread(task);
thread.start();
```

#### C. **Using `Callable` and `Future`**

```java
Callable<String> task = () -> {
    Thread.sleep(1000);
    return "Result from Callable";
};

ExecutorService executor = Executors.newSingleThreadExecutor();
Future<String> future = executor.submit(task);
System.out.println(future.get());  // blocks until result is ready
executor.shutdown();
```

#### Differences:

* `Runnable` → no result
* `Callable` → returns result and can throw checked exceptions
* `Future` → used to retrieve the result of `Callable`

---

### ✅ Thread Lifecycle States

| State              | Meaning                                            |
| ------------------ | -------------------------------------------------- |
| **NEW**            | Thread object created but not started              |
| **RUNNABLE**       | Eligible to run but not necessarily running        |
| **BLOCKED**        | Waiting to acquire monitor lock                    |
| **WAITING**        | Waiting indefinitely (e.g. `wait()`)               |
| **TIMED\_WAITING** | Waiting for a time (e.g. `sleep`, `join`, `await`) |
| **TERMINATED**     | Completed execution or aborted                     |

**Diagram Tip:** Know transitions:

* `start()` ⇒ `NEW` → `RUNNABLE`
* `wait()` ⇒ `WAITING`
* `sleep()` ⇒ `TIMED_WAITING`

---

### ✅ Thread Priorities

```java
Thread t = new Thread(() -> {});
t.setPriority(Thread.MAX_PRIORITY); // 10
```

* Ranges: 1 (MIN) to 10 (MAX), default = 5
* JVM-dependent. Often ignored in practice.

---

### ✅ Thread Scheduling

* **Preemptive**: OS decides which thread to run
* **Cooperative**: Thread must yield control (`Thread.yield()`)
* Java typically uses **preemptive**, but **no guarantees on order**.

---

### ✅ Interview Follow-up Questions

1. **Q:** Difference between `Runnable` and `Callable`?
   **A:** `Runnable` doesn't return a result or throw checked exceptions, `Callable` does both.

2. **Q:** What are different thread states in Java?
   **A:** NEW, RUNNABLE, BLOCKED, WAITING, TIMED\_WAITING, TERMINATED.

3. **Q:** How to retrieve result from a thread?
   **A:** Use `Callable` + `Future` + `ExecutorService`.

4. **Q:** What is the lifecycle of a Java thread?
   **A:** Starts in NEW, moves to RUNNABLE after `start()`, then transitions through various WAITING states, and finally TERMINATED.

---

## ✅ 2. Synchronization & Locks

### ✅ `synchronized` keyword

#### A. **Method-level**

```java
public synchronized void increment() {
    count++;
}
```

#### B. **Block-level**

```java
public void increment() {
    synchronized (this) {
        count++;
    }
}
```

* Locks on the object (`this`) or specified lock object.

---

### ✅ Intrinsic Locks (Monitor Locks)

* Every object has an intrinsic lock.
* `synchronized` acquires and releases this monitor lock.
* Only one thread can hold the lock at a time.

---

### ✅ Deadlocks

#### A. **Cause**

1. Multiple threads hold locks and wait on each other
2. No timeout or release

#### B. **Example**

```java
class A { synchronized void method(B b) {
    synchronized(b) {
        System.out.println("Deadlock example");
    }
}}

class B {}
```

#### C. **Prevention**

* Always acquire locks in the same order
* Use try-lock with timeout
* Detect using thread dump (e.g., `jstack`)

---

### ✅ Thread-safe Collections

| Collection             | Thread-safe | Mechanism       |
| ---------------------- | ----------- | --------------- |
| `Vector`               | ✅           | synchronized    |
| `Hashtable`            | ✅           | synchronized    |
| `ConcurrentHashMap`    | ✅           | Segment locking |
| `CopyOnWriteArrayList` | ✅           | Snapshot copies |

---

### ✅ `ReentrantLock`

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

* Explicit locking/unlocking
* Can check if the lock is available with `tryLock()`
* Reentrant = same thread can reacquire the lock

---

### ✅ `ReentrantReadWriteLock`

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
rwLock.readLock().lock();
// or
rwLock.writeLock().lock();
```

* Multiple readers allowed
* Writers get exclusive access

---

### ✅ `tryLock()` and Fairness

```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    // acquired lock
} else {
    // timeout, couldn't acquire
}
```

* `tryLock()` avoids deadlock
* `new ReentrantLock(true)` → fair lock (FIFO), otherwise unfair by default (better performance)

---

### ✅ Interview Follow-up Questions

1. **Q:** Difference between synchronized and ReentrantLock?
   **A:** ReentrantLock gives more control: tryLock, timed lock, fairness, multiple condition variables.

2. **Q:** How can you prevent deadlocks?
   **A:** Lock ordering, timeout with tryLock, avoiding nested locks.

3. **Q:** Is `ConcurrentHashMap` fully thread-safe?
   **A:** Yes, it allows concurrent reads and segment-based writes (JDK 7) or CAS-based (JDK 8+).

4. **Q:** Why prefer ReentrantReadWriteLock over synchronized?
   **A:** For read-heavy applications; improves performance by allowing concurrent reads.
