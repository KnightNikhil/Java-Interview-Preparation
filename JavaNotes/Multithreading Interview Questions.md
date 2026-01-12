# Multithreading Interview Questions

## Q. What are the states in the lifecycle of a Thread?
A java thread can be in any of following thread states during it\'s life cycle i.e. New, Runnable, Blocked, Waiting, Timed Waiting or Terminated. These are also called life cycle events of a thread in java.

* New
* Runnable
* Running
* Non-Runnable (Blocked)
* Terminated

**1. New**: The thread is in new state if you create an instance of Thread class but before the invocation of start() method.  
**2. Runnable**: The thread is in runnable state after invocation of start() method, but the thread scheduler has not selected it to be the running thread.  
**3. Running**: The thread is in running state if the thread scheduler has selected it.  
**4. Non-Runnable (Blocked)**: This is the state when the thread is still alive, but is currently not eligible to run.  
**5. Terminated**: A thread is in terminated or dead state when its run() method exits.

----

## Q. What are the different ways of implementing thread?
There are two ways to create a thread:
* extends Thread class
* implement Runnable interface

**1. Extends Thread class**  
- Create a thread by a new class that extends Thread class and create an instance of that class. 
- The extending class must override run() method which is the entry point of new thread.
```java
public class MyThread extends Thread {
    @Override 
    public void run() {
      System.out.println("Thread started running..");
    }
    public static void main( String args[] ) {
       MyThread mt = new  MyThread();
       mt.start();
    }
}
```
Output
```
Thread started running..
```
**2. Implementing the Runnable Interface**  
After implementing runnable interface, the class needs to implement the run() method, which is `public void run()`.

* run() method introduces a concurrent thread into your program. This thread will end when run() returns.
* You must specify the code for your thread inside run() method.
* run() method can call other methods, can use other classes and declare variables just like any other normal method.
```java
class MyThread implements Runnable {
    @Override
    public void run() {
        System.out.println("Thread started running..");
    }
    public static void main(String args[]) {
        MyThread mt = new MyThread();
        Thread t = new Thread(mt);
        t.start();
    }
}
```
**NOTES:**
1. Internally Thread class implements Runnable interface only.
2. Runnable is a functional interface.
```java
public static void main(String args[]) {
    Runnable task = () -> System.out.println("Runnable running"); // implementation of runnable, no need of MyThread anymore
    Thread t = new Thread(task);
    t.start();
}
```

**Difference between Runnable vs Thread**

* Implementing Runnable is the preferred way to do it. Here, you’re not really specializing or modifying the thread's behavior. You’re just giving the thread something (task) to run. That means composition is the better way to go.
* Java only supports single inheritance, so you can only extend one class.
* Instantiating an interface gives a cleaner separation between your code and the implementation of threads.
* Implementing Runnable makes your class more flexible. If you extend Thread then the action you’re doing is always going to be in a thread. However, if you implement Runnable it doesn’t have to be. You can run it in a thread, or pass it to some kind of executor service, or just pass it around as a task within a single threaded application.

| Features	              | implements Runnable	    | extends Thread |
|------------------------|-------------------------|----------------|
| Inheritance option	    | extends any java class  | No             |
| Reusability	           | Yes	                    | No             |
| Object Oriented Design | Good,allows composition | Bad            |
| Loosely Coupled	       | Yes 	                   | No             |
| Function Overhead	     | No	                     | Yes            |

## Journey of thread.start() to the overridden run() method

1. Thread object is created
```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Inside run: " + Thread.currentThread().getName());
    }
}

public class Test {
    public static void main(String[] args) {
        MyThread t = new MyThread();
        t.start(); // 🔴 journey begins
    }
}
```
- At this point, t is just a Java object in the NEW state.
- No OS-level thread is created yet.


2. start() is called
    - Thread.start() is a native method (actually written in C/C++ in the JVM).
    - It does two things:
    1.	Requests the JVM + OS thread scheduler to create a new native thread mapped to this Java Thread object.
    2.	Marks the Thread as RUNNABLE (ready to run, not yet running).
        - After start() returns, a new thread of execution exists, independent of the main thread.

Note: If you call run() directly, no new thread is created — it just executes like a normal method call in the same thread.


3. OS allocates a new native thread
    - The JVM delegates to the operating system to create a lightweight process (LWP / kernel thread).
    - The OS provides stack, registers, and execution context for the new thread.
    - The Java Thread object and the OS-native thread are now linked.


4. JVM calls the run() method
    - Once the OS schedules the new thread, the JVM invokes an internal thread entry point.
    - This entry point eventually calls your thread’s overridden run() method.
    - If you extended Thread, it calls this.run().
    - If you passed a Runnable to the Thread constructor, it calls runnable.run().

Pseudo-code inside JVM (simplified):
```java
private void threadEntry() {
    if (target != null) {
        target.run(); // Runnable target
    } else {
        this.run();   // Thread subclass override
    }
}
```

5. **Execution of run()**
    - Now the code inside your overridden run() executes in the new thread’s call stack.
    - The main thread and this new thread run concurrently.


6. **Thread termination**
    - When run() completes:
    - The thread moves to TERMINATED state.
    - The OS reclaims resources (stack, registers, etc.).
    - The Java Thread object still exists in memory, but it can’t be restarted (calling start() again throws IllegalThreadStateException).


Visual Flow
```
t.start()
    │
    ▼
[JVM native method]
    │
    ▼
[OS creates native thread]
    │
    ▼
[JVM thread entry point]
    │
    ▼
→ overridden run() method executes
    │
    ▼
[Thread completes, TERMINATED]
```


**Key Notes (Interview Hotspots)**
- start() → creates a new native thread + schedules execution.
- run() → just a method; if called directly, runs on current thread.
- A thread object can call start() only once.
- JVM → interacts with OS thread scheduler (priority is a hint, not guarantee).
- Context switch between threads is OS-level, JVM has no direct control.


- **Q. What will happen if we don’t override Thread class run() method?**
  - If we don't override Thread class run() method in our defined thread then Thread class run() method will be executed and we will not get any output because Thread class run() is with an empty implementation.
  - It is highly recommended to override run() method because it improves the performance of the system. If we override run() method in the user-defined thread then in run() method we will define a job and Our created thread is responsible to execute run() method.


- **Q. What is difference between start() and run() method of thread class?**
- When program calls `start()` method a **new Thread** is created and code inside `run()` method is executed in new Thread while if you call `run()` method directly **no new Thread is created** and code inside `run()` will execute on current Thread.
- `start()` Can't be invoked more than one time otherwise throws `java.lang.IllegalStateException`  but `run()` can be invoked multiple times

----


## Q. What is the difference between Process and Thread?
Both processes and threads are independent sequences of execution. The typical difference is that **threads run in a shared memory space**, while **processes run in separate memory spaces**.
- A thread is a subset of the process.

`Process vs Thread Analogy`

**Process = A Restaurant**
- A process is like a restaurant building.
- It has its own resources:
- Kitchen (CPU, memory space)
- Storage (disk)
- Utilities (network, files, handles)
- Each restaurant is independent — McDonald’s and Starbucks don’t share kitchens or storerooms.

**In computing:**
- **A process has its own memory space and resources.**
- **Different processes don’t share memory directly — they talk via IPC (like two restaurants calling each other).**



**Thread = Chef working inside the restaurant**
- A thread is like a chef inside the restaurant.
- All chefs (threads) in the same restaurant (process) share:
- The same kitchen (heap memory, files, variables).
- The same storeroom (resources).
- But each chef has their own tools & workspace:
- Personal chopping board (thread’s own stack).
- Personal notepad (registers, program counter).

**In computing:**
- **Threads share process memory/resources.**
- **Each thread has its own stack & registers.**

**Example Scenario**
- Single-threaded process:
One chef working alone in the kitchen. If he takes a break (blocked I/O), no cooking happens.
- Multi-threaded process:
Multiple chefs in the same kitchen. They can:
  - Work on different dishes at the same time (parallel tasks).
  - But must coordinate (synchronization) to avoid collisions:
  - Two chefs grabbing the same pan (race condition).
  - Both chopping on the same board (data corruption).

**Comparison**

| Aspect	           | Process (Restaurant)	                           | Thread (Chef)                                   | 
|-------------------|-------------------------------------------------|-------------------------------------------------|
| Memory	           | Own address space	                              | Shares address space with peers                 | 
| Communication 	   | Hard (like two restaurants calling each other)	 | Easy (chefs shout to each other in the kitchen) | 
| Failure           | Impact	                                         | If restaurant burns down, all chefs stop	       | If one chef cuts finger, others keep cooking| 
| Resource          | creation	                                       | Expensive (open new restaurant)	                | Cheap (hire another chef)| 




Would you like me to also give you a banking-domain analogy (since you prefer that) for process vs thread, so you can explain it in interviews using your domain?

----

## Q. What is difference between user Thread and daemon Thread?
- **Daemon threads are low priority threads which always run in background** and **user threads are high priority threads which always run in foreground.** 
- User Thread or Non-Daemon are designed to do specific or complex task where as daemon threads are used to perform supporting tasks.

**Difference Between Daemon Threads And User Threads In Java**

| Aspect      | User Thread                                                                                              | Daemon Thread                                                                                                           |
|-------------|----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| Created By  | User                                                                                                     | usually JVM (User can also create)                                                                                      | 
| Usage       | Specific tasks                                                                                           | background tasks like garbage collection                                                                                |
| JVM Exit    | wait for user threads to finish their tasks                                                              | JVM will not wait for daemon threads to finish their tasks. It will exit as soon as all user threads finish their tasks |
| Priority    | High priority threads. designed mainly to execute some important task in an application.                 | less priority threads. They are designed to serve the user threads.                                                     |
| Termination | JVM will not force the user threads to terminate. It will wait for user threads to terminate themselves. | JVM will force the daemon threads to terminate if all the user threads have finished their task.                        |


**create Daemon Thread**
```java
/**
 * Java Program to demonstrate difference beween a daemon and a user thread .
 * 
 */
public class DaemonThreadDemo {

    public static void main(String[] args) throws InterruptedException {

        // main thread is a non-daemon thread
        String name = Thread.currentThread().getName();
        boolean isDaemon = Thread.currentThread().isDaemon();

        System.out.println("name: " + name + ", isDaemon: " + isDaemon);

        // Any new thread spawned from main is also non-daemon or user thread
        // as seen below:
        Runnable task = new Task();
        Thread t1 = new Thread(task, "T1");
        System.out.println("Thread spawned from main thread");
        System.out.println("name: " + t1.getName() + ", isDaemon: " + t1.isDaemon());

        // though you can make a daemon thread by calling setDaemon()
        // before starting it as shown below:
        t1.setDaemon(true);
        t1.start();

        // let's wait for T1 to finish
        t1.join();
    }

    private static class Task implements Runnable {

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("Thread made daemon by calling setDaemon() method");
            System.out.println("name: " + t.getName() + ", isDaemon: " + t.isDaemon());

            // Any new thread created from daemon thread is also daemon
            Thread t2 = new Thread("T2");
            System.out.println("Thread spawned from a daemon thread");
            System.out.println("name: " + t2.getName() + ", isDaemon: " + t2.isDaemon());
        }
    }
}
```
Output
```
name: main, isDaemon: false
Thread spawned from main thread
name: T1, isDaemon: false
Thread made daemon by calling setDaemon() method
name: T1, isDaemon: true
Thread spawned from a daemon thread
name: T2, isDaemon: true
```

---

## wait() / notify() / notifyAll()
- Threads can signal each other using object monitors.

Note - wait, notify and notifyAll are applied on object and not thread

**Example:**
- We have a producer thread that puts items into a shared “bucket” and a consumer thread that takes items out.
- Both threads coordinate using wait() and notify().
```java
class SharedBucket {
   private int item;
   private boolean available = false;

   // Producer puts item in the bucket
   public synchronized void produce(int value) {
      while (available) {
         try {
            wait(); // 🚨 Bucket is full → wait until consumer consumes
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
      item = value;
      available = true;
      System.out.println("Produced: " + value);

      notify(); // 👉 Wake up a waiting consumer
   }

   // Consumer takes item from the bucket
   public synchronized int consume() {
      while (!available) {
         try {
            wait(); // 🚨 Bucket is empty → wait until producer produces
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
      available = false;
      System.out.println("Consumed: " + item);

      notify(); // 👉 Wake up a waiting producer
      return item;
   }
}

public class WaitNotifyDemo {
   public static void main(String[] args) {
      Bucket bucket = new Bucket();

      // Producer Thread
      Thread producer = new Thread(() -> {
         for (int i = 1; i <= 5; i++) {
            bucket.produce(i);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
         }
      });

      // Consumer Thread
      Thread consumer = new Thread(() -> {
         for (int i = 1; i <= 5; i++) {
            bucket.consume();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
         }
      });

      producer.start();
      consumer.start();
   }
}
```
**Behind the scene**
Step 1: First Producer Run
- Producer enters produce().
- Since available = false, it produces an item.
- Sets available = true.
- Calls notify() → wakes up any waiting consumer.

Step 2: Consumer’s Turn
- Consumer enters consume().
- Since available = true, it consumes the item.
- Sets available = false.
- Calls notify() → wakes up any waiting producer.

Step 3: Synchronization via wait()
- If consumer arrives before producer has produced anything, it goes to wait() (releases lock and waits in the monitor’s waiting room).
- Producer will later call notify(), waking up the consumer.
- Similarly, if producer tries to produce when the bucket is still full, it goes to wait() until consumer notifies.

**Difference Between wait(), notify(), and notifyAll()**
- `wait()` → Thread pauses and releases the lock, waiting for someone to wake it.
- `notify()` → Wakes up one waiting thread on the same monitor.
- `notifyAll()` → Wakes up all waiting threads on the same monitor (they compete for the lock).


**Q. How does the jvm understood that when consumer notify() is called, which thread .wait() to be released?**

- When consumer calls notify(), the JVM doesn’t know it must wake a producer.
- It wakes one thread at random from the bucket waiting set.
- If it happens to be another consumer → it re-checks condition (while (!available)) → condition fails → goes back to waiting.
- Eventually, the correct type (producer) wakes up.

**Behind the scene**

1. What actually happens when wait() is called?
    - When a thread calls obj.wait():
        1.	It releases the monitor lock on obj.
        2.	It goes into the waiting set of obj (think of it as a queue of sleeping threads for that object).

👉 Key point:
Each object in Java has one waiting set, not separate waiting sets for producers vs. consumers.


2. What happens when notify() is called?
    - When a thread calls obj.notify():
    - The JVM chooses ONE thread randomly (no guaranteed order) from obj’s waiting set.
    - That thread is moved from the waiting set to the entry set (threads competing for the lock).
    - It still needs to reacquire the lock before continuing.

👉 So, if both producer and consumer are waiting on the same object monitor, you don’t control which one gets chosen with notify().


3. What happens when notifyAll() is called?
    - All waiting threads in the monitor’s waiting set are awakened and moved to the entry set.
    - They then compete for the lock. Only one wins, the rest keep waiting.

This guarantees no thread is left behind accidentally, but can lead to “thundering herd” (all wake up unnecessarily).


4. How does JVM “know” it’s consumer vs. producer?

👉 Trick answer: It doesn’t know.
The JVM only knows:
- “Thread X is waiting on bucket object.”
- “Thread Y called bucket.notify().”

That’s why in the producer-consumer pattern:
- We carefully design the condition checks (while (!available) wait(); and while (available) wait();).
- Even if the wrong type of thread is awakened, it checks the condition again and goes back to waiting.

This is called “spurious wakeup” handling — always use while, not if.


5. Example with Multiple Threads

Imagine:
- 2 Producers (P1, P2)
- 2 Consumers (C1, C2)

If bucket is empty and both consumers (C1, C2) are waiting:
- Producer calls notify().
- JVM may wake up either C1 or C2, chosen randomly.
- The awakened consumer checks the condition → if valid, consumes → notifies producers.

If instead, all four are waiting and someone calls notify(), any one may wake up (no guarantee it will be the “right” type).


**Summary Rules**
1.	notify() wakes up one random thread from the waiting set.
2.	notifyAll() wakes up all waiting threads, they compete for the lock.
3.	JVM does not differentiate between producer/consumer — the logic is in your condition checks (while (...) wait();).
4.	Always use while, never if, when waiting — this avoids issues if the “wrong” thread is awakened.

**Q. Why wait(), notify() and notifyAll() must be called from inside of the synchronized block or method.?**
- `wait()` forces the thread to release its lock. This means that it must own the lock of an object before calling the `wait()` method of that (same) object. Hence the thread must be in one of the object's synchronized methods or synchronized block before calling wait().
- When a thread invokes an object's `notify()` or `notifyAll()` method, one (an arbitrary thread) or all of the threads in its waiting queue are removed from the waiting queue to the entry queue. They then actively contend for the object's lock, and the one that gets the lock goes on to execute.

---

## Q. What is the difference between wait() and sleep() method?
**1.  Class  belongs**:  The wait() method belongs to `java.lang.Object` class, thus can be called on any Object. The sleep() method belongs to `java.lang.Thread` class, thus can be called on Threads.

**2. Context**:  The wait() method can only be called from Synchronized context i.e. using synchronized block or synchronized method. The sleep() method can be called from any context.

**3. Locking**:  The wait() method releases the lock on an object and gives others chance to execute. The sleep() method does not releases the lock of an object for specified time or until interrupt.

**4. Wake up condition**:  A waiting thread can be awake by notify() or notifyAll() method. A sleeping can be awaked by interrupt or time expires.

**5. Execution**:  Each object has each wait() method for inter-communication between threads. The sleep() method is static method belonging to Thread class. There is a common mistake to write t.sleep(1000) because sleep() is a class method and will pause the current running thread not t.
```java
synchronized(LOCK) {
    Thread.sleep(1000); // LOCK is held
}


synchronized(LOCK) {
    LOCK.wait(); // LOCK is not held
}
```

---

## Q. What does join() method?
`java.lang.Thread` class provides the join() method which allows one thread to wait until another thread completes its execution.
```java
public class ThreadJoinExample {

    public static void main(String[] args) {
        Thread t1 = new Thread(new MyRunnable(), "t1");
        Thread t2 = new Thread(new MyRunnable(), "t2");
        Thread t3 = new Thread(new MyRunnable(), "t3");
        
        t1.start();
        
        //start second thread after waiting for 2 seconds or if it's dead
        try {
            t1.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        t2.start();
        
        //start third thread only when first thread is dead
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        t3.start();
        
        //let all threads finish execution before finishing main thread
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("All threads are dead, exiting main thread");
    }

}

class MyRunnable implements Runnable {

    @Override
    public void run() {
        System.out.println("Thread started: "+Thread.currentThread().getName());
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread ended: "+Thread.currentThread().getName());
    }
}
```
Output
```
Thread started: t1
Thread started: t2
Thread ended: t1
Thread started: t3
Thread ended: t2
Thread ended: t3
All threads are dead, exiting main thread
```

---

## Q. Tell me about join() and wait() methods?

**join() Method**
-	Defined in: java.lang.Thread class.
-	Purpose: To make one thread wait for another thread to finish.

Example:
```java
public class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("Worker: " + i);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        });

        worker.start();

        System.out.println("Main waiting for worker...");
        worker.join(); // 👈 Main thread waits until worker finishes
        System.out.println("Worker finished, Main resumes!");
    }
}
```

Key points about join():
-	Always called on a Thread instance (e.g., t.join()).
-	Causes the calling thread to pause until the target thread finishes.
-	Has overloads with timeout → join(2000) waits max 2 seconds.
-	Does not need to be inside synchronized.


**wait() Method**
-	Defined in: java.lang.Object class.
-	Purpose: Used for inter-thread communication inside synchronized blocks.

Key points about wait():
-	Always called on an object’s monitor (not a thread).
-	Must be inside a synchronized block/method.
-	Causes the thread to:
1.	Release the lock.
2.	Enter the object’s waiting set.
3.	Stay there until another thread calls notify()/notifyAll().


---

## Q. What is difference between Yield and Sleep method in Java?

**Yield -** It hints the JVM to give other threads a chance to run and execute. (it hints, not strict rule)

**1. Currently executing thread state**: sleep()  method causes the currently executing thread to sleep for the number of milliseconds specified in the argument. yield() method temporarily pauses the currently executing thread to give a chance to the remaining waiting threads of the same priority to execute.
If there is no waiting thread or all the waiting threads of low priority then the current thread will continue its execution.

**2. Interrupted Exception**: Sleep method throws the Interrupted exception if another thread interrupts the sleeping thread.  yield method does not throw Interrupted Exception.

**3. Give up monitors**:  Thread.sleep() method does not cause cause currently executing thread to give up any monitors while yield() method give up the monitors.


---


## Q. What is race-condition?
**What is a Race Condition?**

A race condition happens when:
-	Two or more threads access a shared resource (variable, object, file, DB, etc.).
-	At least one of them modifies it.
-	And the final outcome depends on the interleaving (order) of thread execution.

Since the JVM thread scheduler decides which thread runs when, execution order is non-deterministic → leading to inconsistent or incorrect results.

⸻

**Why does it happen?**

Because thread operations that look atomic in Java are not really atomic at the CPU/JVM level.

For example:
`count++;`
- This looks like a single operation, but under the hood it is:
    1.	Read value of count from memory into CPU register.
    2.	Increment it.
    3.	Write updated value back to memory.

If two threads interleave between steps, the update can be lost.

```java
class Counter {
private int count = 0;

    public void increment() {
        count++; // not atomic
    }

    public int getCount() {
        return count;
    }
}

public class RaceConditionExample {
public static void main(String[] args) throws InterruptedException {
Counter counter = new Counter();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Final count: " + counter.getCount());
    }
}
```
Expected Output = 2000
Actual Output = 1900, 1978, 1995, etc. (non-deterministic)
Because both threads race to update count.

⸻


**Timeline of a Race**

Imagine count = 10, two threads increment:
1.	Thread A reads count (10).
2.	Thread B reads count (10).
3.	Thread A increments (11) and writes back.
4.	Thread B increments (still from old 10 → 11) and writes back.

Final result = 11 (should have been 12).

⸻

**Types of Race Conditions**
- **1.	Read-Modify-Write Race**
  Example: count++, balance = balance - amount.
- **2.	Check-Then-Act Race**
```java
if (balance >= amount) {
    balance -= amount;
}
```
Between checking and subtracting, another thread may modify balance → overdraft.
- **3. Initialization Race**
  Two threads creating a singleton object at the same time.

⸻

**How to Prevent Race Conditions?**
**1.	Synchronized Blocks/Methods**
```java
public synchronized void increment() {
    count++;
}
```

**2.	Locks (ReentrantLock)**
```
lock.lock();
try { count++; } finally { lock.unlock(); }
```

**3.	Atomic Variables (AtomicInteger)**
```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
```

**4.	Volatile?**
⚠️ volatile does not prevent race conditions, it only ensures visibility.
Example:
Initial: count = 10 (volatile)
1.	Thread A reads → sees 10.
2.	Thread B reads → sees 10 (yes, volatile ensures both read from main memory, so both got 10).
3.	Thread A increments → calculates 11 and writes it back (main memory = 11).
4.	Thread B increments → but note: it already loaded 10 into its register in step 2. It increments that to 11 and writes it back.

Final result = 11, not 12.

This is the classic lost update problem — visibility is correct, but atomicity is broken.

For atomicity, you still need synchronization or atomics.

⸻

**Real-world Example**
-	Banking System: Two ATMs withdrawing from the same account balance → balance mismatch.
-	Ticket Booking: Two users book the last seat at the same time → overselling.
-	E-commerce Cart: Two users apply discount codes simultaneously → double discounts.


---

## Synchronized keyword

**What is synchronized?**
- In Java, synchronized is a keyword used to control access to critical sections of code by multiple threads.
- It ensures that only one thread at a time can execute the synchronized block/method for a given object/monitor.

So, it prevents race conditions by providing mutual exclusion.

⸻

**How does it work internally?**

When a thread enters a synchronized block/method:
1.	It acquires the monitor lock of the object (or class if it’s a static method).
2.	While the thread holds the lock, no other thread can enter any synchronized block/method guarded by the same lock.
3.	Once the thread exits the block (normally or due to exception), it releases the lock automatically.
4.	Other threads waiting for the same lock can then compete to acquire it.

Each Java object has an intrinsic lock (monitor lock) associated with it.

⸻

**Types of Synchronization**

**1. Synchronized Instance Methods**

Lock is on the current object (this).
```java
class Counter {
private int count = 0;

    public synchronized void increment() {
        count++; // only one thread at a time
    }

    public int getCount() {
        return count;
    }
}
```
-	Here, two threads calling increment() on the same object won’t run it simultaneously.
-	But if they call it on different objects, each has its own lock, so both threads can run.

⸻

**2. Synchronized Static Methods**

- **Normal Synchronization Recap**

When you declare a instance synchronized method:
```java
public synchronized void instanceMethod() {
// critical section
}
```
- The lock is acquired on the current object’s monitor (this).
  -	So, if two threads are calling instanceMethod() on the same object, they must wait (mutual exclusion).
  -	But if they call it on different objects, both can run at the same time.


- **Static Synchronization**

Now, if you declare a static synchronized method:
```java
public static synchronized void staticMethod() {
// critical section
}
```
- The lock is acquired on the Class object monitor (i.e., the .class literal).

- Every loaded class in JVM has a unique Class object stored in the Method Area.
- That object acts like a “class-level lock.”


- **Key Difference**
  -	Instance synchronized method → lock on object’s monitor (this).
  -	Static synchronized method → lock on Class object’s monitor (MyClass.class).

- So:
  -	If two threads call a static synchronized method, only one can run at a time (because they share the same Class lock).
  -	If one thread calls a static synchronized method and another calls an instance synchronized method → they don’t block each other (different locks: Class lock vs Object lock).

⸻

**Example**
```java
class Printer {
// static synchronized method
public static synchronized void printStatic(String msg) {
System.out.println("Start: " + msg);
try { Thread.sleep(1000); } catch (InterruptedException e) {}
System.out.println("End: " + msg);
}

    // instance synchronized method
    public synchronized void printInstance(String msg) {
        System.out.println("Start Instance: " + msg);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("End Instance: " + msg);
    }
}

public class StaticSyncDemo {
public static void main(String[] args) {
Printer p1 = new Printer();
Printer p2 = new Printer();

        // Two threads calling static synchronized method
        new Thread(() -> Printer.printStatic("A")).start();
        new Thread(() -> Printer.printStatic("B")).start();

        // Two threads calling instance synchronized method on same object
        new Thread(() -> p1.printInstance("X")).start();
        new Thread(() -> p1.printInstance("Y")).start();

        // Two threads calling instance synchronized method on different objects
        new Thread(() -> p1.printInstance("M")).start();
        new Thread(() -> p2.printInstance("N")).start();
    }
}
```
- Behavior:
  1.	Static sync methods (printStatic) → only one thread runs at a time, because they lock on Printer.class.
  2.	Instance sync methods (printInstance) on same object → one thread at a time (lock on p1).
  3.	Instance sync methods on different objects → run in parallel (different locks: p1 vs p2).
  4.	Static vs Instance sync methods → don’t block each other (different locks).


⸻

**3. Synchronized Blocks**
- Allows finer control — lock on a specific object, not necessarily this.
```java
class Counter {
private int count = 0;
private final Object lock = new Object();

    public void increment() {
        synchronized (lock) {
            count++;
        }
    }
}
```
-	Best practice because you avoid locking the entire method.

**Limitations of synchronized**
1.	Blocking — if one thread holds a lock, others must wait (can cause bottlenecks).
2.	No fairness — JVM doesn’t guarantee which waiting thread gets the lock first.
3.	Deadlock risk if multiple locks are acquired in inconsistent order.
4.	Coarse-grained — locks entire object/method unless carefully scoped.

⸻

**When to Use**
-	Use synchronized for simple mutual exclusion.
-	For advanced control (fairness, timeout, read/write), use ReentrantLock from java.util.concurrent.locks.

⸻

**So in short:**
-	synchronized is about locking a monitor.
-	It ensures atomic execution of critical sections.
-	Use it carefully to avoid deadlocks and contention.

---

## Locks 

### Need of Locks

**Why synchronized Exists**

- synchronized was the original concurrency mechanism in Java (since JDK 1.0).
- It gives you:
  -	Mutual exclusion → only one thread in the block/method at a time.
  -	Visibility → variables updated inside synchronized block are visible to other threads after lock release.
  -	Reentrancy → same thread can acquire the lock multiple times.
- So, yes — synchronized is enough for many simple concurrency needs.

**But Then, Why Locks?**

- By JDK 5, it became clear that synchronized is too limited for advanced multithreading.
- That’s where java.util.concurrent.locks was introduced.

**Here’s why Locks were needed:**
- **1. More Control (Flexibility)**

- With synchronized, you can’t do things like:
  -	Try acquiring a lock and if unavailable, do something else.
  -	Acquire a lock with a timeout.
  -	Interrupt a thread waiting for a lock.

With ReentrantLock, you can:
```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
        try {
        // critical section
        } finally {
        lock.unlock();
    }
} else {
// do something else if lock not available
}
```
- This is not possible with synchronized.

- **2. Fairness Policy**
-	synchronized is non-fair — the JVM chooses the next thread randomly.
-	ReentrantLock can be fair (first-come-first-serve).
```java
ReentrantLock lock = new ReentrantLock(true); // fair lock
```

- **3. Multiple Condition Variables**
  -	With synchronized, you only have one monitor queue (wait(), notify(), notifyAll()).
  -	With ReentrantLock, you can create multiple Condition objects to manage different wait-sets.

Example: One condition for “buffer full” and another for “buffer empty” in Producer-Consumer.


- **4. Performance (under contention)**
   -	In older JVMs (pre-Java 6), synchronized was slow, because it used heavyweight OS mutexes.
   -	ReentrantLock was faster because it was implemented at the Java level.
   -	Today (Java 8+), JVM has optimized synchronized (biased locking, lightweight locking, etc.), so performance gap is smaller — but ReentrantLock still shines under high contention or advanced use cases.


- **5. Read-Write Scenarios**
    - synchronized allows only exclusive locks (one thread at a time). 
    - If you have a read-heavy system, this wastes performance. 
    - Example: 100 threads just reading a cache → why block each other? 
    - Solution: **ReadWriteLock** (multiple readers allowed, one writer at a time).


- **6. Optimistic Locking (StampedLock)**
    - synchronized and ReentrantLock are pessimistic (they always block). 
    - In read-heavy workloads, this causes bottlenecks.
    - StampedLock (Java 8) allows optimistic reading:
      -	Threads read without locking.
      -	Later, they check if data changed during the read. 
    - Much faster for concurrent reads.

**When to Use What?**

Use synchronized when:
-	Simpler critical sections.
-	No special locking requirements.
-	You want cleaner, less error-prone code.

Use ReentrantLock (or others) when:
-	You need tryLock() or tryLock(timeout).
-	You want fair ordering.
-	You need multiple condition variables.
-	You need read-write separation (ReadWriteLock).
-	You need optimistic concurrency (StampedLock).

In short:
synchronized is like a basic lock (easy to use, but limited).
Lock framework gives advanced locks (powerful, flexible, but more complex).

---

## Lock

### What is a Lock?
- A Lock is a mechanism that ensures mutual exclusion in multithreading:
    -	Only one thread can access a shared resource (critical section) at a time.
    -	Other threads trying to access the resource must wait until the lock is released.


### Types of Locks in Java
**1. Intrinsic Locks (Monitor Locks)**
-	Provided by the synchronized keyword.
-	Tied to an object (this) or class (Class object for static).
-	Features:
  -	Mutual exclusion.
  -	Reentrant (a thread can reacquire the same lock).
  -	No flexibility (no try-lock, no timed-lock, no fairness policies).

**2. Explicit Locks (java.util.concurrent.locks)**

- Introduced in Java 5 (java.util.concurrent.locks package).
- More advances features, we can control when to lock, how to lock and unlock, giving more control over when and how an object can be accessed.
- Main interface: Lock.

### Lock Interface

**What is the Lock Interface?**
-	Lock is part of java.util.concurrent.locks package.
-	It provides a more flexible and powerful mechanism than synchronized for controlling access to shared resources.
-	Unlike synchronized (which is block-structured and automatically released when the method/block exits), Lock requires explicit acquire and release (lock() and unlock()).

**Methods of the Lock Interface**

Here are the important ones:

**1.	void lock()**
-	Acquires the lock unconditionally.
-	If another thread already holds the lock, the current thread will wait until it becomes available.

**2.	void unlock()**
-	Releases the lock.
-	Unlike synchronized, you must always release the lock explicitly (ideally inside finally).
```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Counter {
    private int count = 0;
    private Lock lock = new ReentrantLock();

    public void increment() {
        lock.lock(); // acquire lock
        try {
            count++;
        } finally {
            lock.unlock(); // always release in finally
        }
    }

    public int getCount() {
        return count;
    }
}

public class LockExample {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("Final Count: " + counter.getCount());
    }
}
```

**3.	boolean tryLock()**
-	Tries to acquire the lock immediately without waiting.
-	Returns:
-	true → if lock acquired
-	false → if lock not available

**4.	boolean tryLock(long time, TimeUnit unit)**
-	Tries to acquire the lock but waits for a given time before giving up.
-	Prevents indefinite waiting.
-   If the lock still not available after time period, returns false, code not executed.
```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockTimeoutExample {
    private final Lock lock = new ReentrantLock();

    public void doWork(String threadName) {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) { // wait up to 1 sec
                try {
                    System.out.println(threadName + " acquired the lock.");
                    Thread.sleep(2000); // simulate work
                } finally {
                    lock.unlock();
                    System.out.println(threadName + " released the lock.");
                }
            } else {
                System.out.println(threadName + " could not acquire the lock within 1 second.");
            }
        } catch (InterruptedException e) {
            System.out.println(threadName + " was interrupted while waiting for the lock.");
        }
    }

    public static void main(String[] args) {
        TryLockTimeoutExample example = new TryLockTimeoutExample();

        Thread t1 = new Thread(() -> example.doWork("Thread-1"));
        Thread t2 = new Thread(() -> example.doWork("Thread-2"));

        t1.start();
        t2.start();
    }
}
```
```text
Thread-1 acquired the lock.
Thread-2 could not acquire the lock within 1 second.
Thread-1 released the lock.
```

**5.	void lockInterruptibly()**
-	Similar to lock(), but allows the thread to be interrupted while waiting for the lock.
-	Useful when you don’t want threads to wait forever.
-   A thread tries to acquire the lock but can be interrupted while waiting.
-	If you just use lock(), the thread blocks forever until the lock is available.
-	With lockInterruptibly(), if another thread interrupts it, it throws an InterruptedException and stops waiting.

**What is interruption?**
```java
public class InterruptionExample {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Worker thread interrupted. Exiting...");
                    break;
                }
                System.out.println("Working...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted during sleep. Cleaning up...");
                    break; // exit loop
                }
            }
        });

        worker.start();
        Thread.sleep(2000); // Let worker run a bit
        worker.interrupt(); // Ask worker to stop
    }
}
```
```text
Working...
Working...
Working...
Interrupted during sleep. Cleaning up...
```

**6.	Condition newCondition()**
-	Provides a Condition object (similar to wait(), notify(), notifyAll() in synchronized).
-	Allows more advanced waiting/notification mechanisms.

---

## Volatile Keyword

**The Problem Without volatile**
- Each thread creates its own copy of the variables in its cached memory(in CPU registers or thread-local memory).
- When one thread updates a variable, other threads can not see the update because they are still reading the old cached value.
- This is due to the Java Memory Model (JMM) and hardware optimizations.

Example (without volatile):
```java
class SharedData {
boolean running = true;

    void stop() {
        running = false; // request thread to stop
    }
}

public class VolatileDemo {
public static void main(String[] args) {
SharedData data = new SharedData();

        Thread t = new Thread(() -> {
            while (data.running) {
                // do work
            }
            System.out.println("Stopped");
        });

        t.start();

        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        data.stop(); // request stop
    }
}
```
Issue:
The worker thread may never stop because it keeps reading running from its CPU cache, not from main memory.

**What volatile Does**

When you declare a variable as volatile:
```java
volatile boolean running = true;
```
It tells the JVM:
1.	Visibility guarantee → Any write to a volatile variable is immediately written to main memory, and any read always comes from main memory.
- This means all threads see the most recent value.
2.	Happens-before guarantee → A write to a volatile variable happens-before any subsequent read of that variable.
- This provides a lightweight synchronization mechanism.

Now the worker thread will see the update and stop correctly.

**What volatile Does NOT Do**
- It does not provide atomicity.

Example:
```java
volatile int counter = 0;

counter++;  // Not atomic (read → increment → write)
```
- Even though counter is volatile, multiple threads can still overwrite each other’s updates.
- To fix this, use Atomic classes (AtomicInteger, etc.) or synchronization.
- It does not replace locks for compound actions.
  For example:
```java
if (flag) {
// do something
}
```
This check+action sequence is not atomic, even with volatile.

⸻
**When to Use volatile**

- Good use cases:
    - Flags (volatile boolean running)
    - State indicators (volatile boolean isConnected)
    - Double-checked locking for Singleton (with extra care)

- Not good for:
    - Counters (counter++)
    - Complex state updates
    - Multiple related variables that must be updated consistently


---

## Q. How do you stop a thread in java?
- A thread is automatically destroyed when the run() method has completed. 
- But it might be required to kill/stop a thread before it has completed its life cycle. 
- Modern ways to suspend/stop a thread are by using a boolean flag and `Thread.interrupt()` method.
```java
class MyThread extends Thread
{
    //Initially setting the flag as true
    private volatile boolean flag = true;
     
    //This method will set flag as false
    public void stopRunning() {
        flag = false;
    }
     
    @Override
    public void run() {
         
        //This will make thread continue to run until flag becomes false
        while (flag) {
            System.out.println("I am running....");
        }
        System.out.println("Stopped Running....");
    }
}
 
public class MainClass 
{   
    public static void main(String[] args) {

        MyThread thread = new MyThread();
        thread.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        //call stopRunning() method whenever you want to stop a thread
        thread.stopRunning();
    }   
}
```
Output
```
I am running….
I am running….
I am running….
I am running….
I am running….
I am running….
I am running….
I am running….
Stopped Running….
```

---

## Q. Why and How does thread communicate with each other?

Threads in the same process share the same memory (heap).
- That’s powerful → they can directly read/write shared variables.
- But it’s dangerous ❌ → without coordination, you get race conditions, inconsistency, or even deadlocks.

So the JVM + Java concurrency APIs give us structured ways to communicate safely.


### Ways Threads Communicate in Java

1. **Shared Memory (synchronized access)**
   - The simplest way: threads share variables.
   - To avoid corruption, they use synchronization.

```java
class Shared {
    private int data = 0;

    public synchronized void setData(int value) {
        this.data = value;
    }

    public synchronized int getData() {
        return data;
    }
}
```

- Here, one thread can setData, another can getData.
- synchronized ensures mutual exclusion and happens-before relationship (visibility).

--

2. **wait(), notify()**

--

3. **High-level Concurrency Utilities (java.util.concurrent)**

- Modern Java gives better tools than raw wait/notify:

- BlockingQueue
```java
BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

// Producer
new Thread(() -> {
    try { queue.put(1); } catch (InterruptedException e) {}
    }).start();

// Consumer
new Thread(() -> {
    try { System.out.println(queue.take()); } catch (InterruptedException e) {}
    }).start();
```
 - Internally uses locks/conditions but much safer & cleaner.
 - Other utilities: Exchanger, Semaphore, CountDownLatch, CyclicBarrier.

--

4. Volatile variables

⸻

5. **CompletableFuture / Message Passing**
   - Instead of shared memory, threads can also communicate by passing results.
   - Example: using CompletableFuture to signal result readiness.
```java
CompletableFuture<String> future = new CompletableFuture<>();

new Thread(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
    future.complete("Hello from thread!");
    }).start();

System.out.println(future.join()); // waits and gets message
```


**Key Interview Takeaways**
- Shared memory → synchronized blocks, volatile, atomic variables.
- Coordination → wait/notify, locks, conditions, concurrent utilities.
- Modern Java → use BlockingQueue, CompletableFuture, etc. instead of low-level wait/notify.
- Threads don’t “send messages” directly → they either:
  - Share variables (safely), or
  - Use higher-level concurrency constructs for coordination.

---

## Q. What do you understand about Thread Priority?
- Every thread in Java has a priority that helps the thread scheduler to determine the order in which threads scheduled. 
- The threads with higher priority will usually run before and more frequently than lower priority threads. 
- By default, all the threads had the same priority, i.e., they regarded as being equally distinguished by the scheduler, when a thread created it inherits its priority from the thread that created it.

Default priority of a thread is 5 (NORM_PRIORITY). The value of MIN_PRIORITY is 1 and the value of MAX_PRIORITY is 10.

* public static int MIN_PRIORITY
* public static int NORM_PRIORITY
* public static int MAX_PRIORITY

```java
class TestMultiPriority1 extends Thread {  
  
    public void run() {  
        System.out.println("Running thread name is:" + Thread.currentThread().getName());  
        System.out.println("Running thread priority is:" + Thread.currentThread().getPriority());  
    }  

    public static void main(String args[]) {  
        TestMultiPriority1 m1 = new TestMultiPriority1();  
        TestMultiPriority1 m2 = new TestMultiPriority1();  
        m1.setPriority(Thread.MIN_PRIORITY);  
        m2.setPriority(Thread.MAX_PRIORITY);  
        m1.start();  
        m2.start();  
    }  
}     
```
Output
```
Running thread name is: Thread-0
Running thread priority is: 10
Running thread name is: Thread-1
Running thread priority is: 1    
```

---

## Q. What is Thread Scheduler and Time Slicing?

**Thread Scheduler**
   - 	The Thread Scheduler is the JVM component (actually, relies on the OS) that decides which thread should run when multiple threads are ready (in Runnable state).
   - 	It uses the underlying operating system’s scheduling algorithm (Java does not implement its own).

Key points:
   - 	If two threads are ready, the scheduler decides who gets CPU.
   - 	You can suggest priorities with thread.setPriority(), but it’s just a hint — actual behavior depends on the OS + JVM implementation. 

Example:
```java
Thread t1 = new Thread(() -> System.out.println("Thread-1"));
Thread t2 = new Thread(() -> System.out.println("Thread-2"));

t1.setPriority(Thread.MIN_PRIORITY); // 1
t2.setPriority(Thread.MAX_PRIORITY); // 10
```
Scheduler may prefer t2, but not guaranteed.

⸻

**Time Slicing (a.k.a Round-Robin Scheduling)**
   - 	Time slicing is when the CPU gives each runnable thread a fixed time slot (time slice) to run.
   - 	If the thread doesn’t finish in that time, it goes back to the ready queue, and another thread gets CPU.
   - 	After a cycle, the thread gets another turn.

Analogy:
- 	Imagine 5 kids sharing 1 toy.
- 	Scheduler (the teacher) says:
- 	Each kid gets the toy for 10 seconds (time slice).
- 	After 10 seconds, next kid gets it.
- 	If one kid finishes early (thread completes), others get longer turns.

⸻

**Relation Between Scheduler & Time Slicing**
   - 	Scheduler decides who runs.
   - 	Time slicing decides for how long.

Different OS/JVM combos may use:
- 	Preemptive scheduling (higher-priority threads can interrupt lower ones).
- 	Time slicing / round-robin scheduling (all threads get equal share).

Java does not guarantee time slicing → depends on JVM + OS.

⸻

**Why It Matters in Java**
   - 	Explains why thread execution order is non-deterministic.
   - 	Helps understand why Thread.sleep(), yield(), and join() affect execution.
   - 	Important in performance tuning: CPU-bound vs I/O-bound threads behave differently under scheduling.

⸻

**Quick Recap**
   - 	Thread Scheduler: Decides which thread runs.
   - 	Time slicing: Ensures each thread gets CPU for a small quantum, then rotates.
   - 	Both are OS-dependent; Java just exposes hooks (priority, yield, sleep).


---

## Q. What is context-switching in multi-threading?
-  Context switching is the process where the CPU saves the state of one thread and restores the state of another, so multiple threads can share a single CPU core.
-  Since a CPU core can execute only one thread at a time, context switching creates the illusion of parallel execution in multi-threaded programs.

**Analogy**

Imagine two kids (threads) sharing one pen (CPU):
- 	When Kid A is writing, Kid B waits.
- 	When the teacher says “Switch!”, Kid A bookmarks the page (saves context).
- 	Kid B opens his notebook to the last page he was writing (restores context).
- 	They keep switching — it looks like both are writing at once, but really it’s just fast switching.


**What is “Context”?**
The context of a thread is basically everything the CPU needs to resume execution later:
- 	Program counter (which instruction to run next)
- 	CPU registers
- 	Stack pointer (method calls, local variables)
- 	Thread state (Ready, Running, Waiting, etc.)

**Steps in Context Switching**
   1.	Thread A is running.
   2.	Scheduler decides to run Thread B instead (maybe because Thread A’s time slice expired).
   3.	CPU saves Thread A’s context into its Thread Control Block (TCB).
   4.	CPU loads Thread B’s context from its TCB.
   5.	Execution resumes with Thread B.
- Later, when Thread A gets CPU again, its saved context is restored.


**Context Switching in Multithreading**
   - 	Caused by time slicing, I/O blocking, or higher-priority thread preemption.
   - 	Essential for concurrency but expensive because saving/loading context takes CPU cycles.
   - 	Too much context switching = thrashing (performance drops).

**Impact in Java**
   - 	In Java, context switching is managed by the OS thread scheduler, not the JVM directly.
   - 	JVM just maps Java threads to OS-level threads (native threads).
   - 	The frequency of context switches depends on:
     - 	Thread priorities
     - 	OS scheduling policy (round-robin, preemptive, etc.)
     - 	Number of CPU cores

**Quick Recap**
   - 	Context switching = saving one thread’s state & restoring another’s.
   - 	Needed because CPUs run one thread/core at a time.
   - 	Costly (performance-wise).
   - 	Managed by OS, invisible to programmer, but affects program performance.

- Q. If asked “Is context switching free?” → 
- Answer: No, it’s expensive because saving/restoring registers, program counter, and memory state takes time. Too many threads cause overhead.

---

## Q. What is Deadlock? How to analyze and avoid deadlock situation?
## Deadlock

### 1. What is a deadlock?
A deadlock occurs when two or more threads are permanently blocked because each thread holds a lock that another thread needs. When deadlock happens:
- No thread can proceed.
- The JVM does not resolve it automatically.
- The application appears to hang.

---

### 2. Minimal deadlock example (2 threads, 2 locks)
```java
public class DeadlockDemo {
    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("Thread-1 acquired LOCK_A");
                sleep(100);
                synchronized (LOCK_B) {
                    System.out.println("Thread-1 acquired LOCK_B");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("Thread-2 acquired LOCK_B");
                sleep(100);
                synchronized (LOCK_A) {
                    System.out.println("Thread-2 acquired LOCK_A");
                }
            }
        });

        t1.start();
        t2.start();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

**Expected output**
```
Thread-1 acquired LOCK_A
Thread-2 acquired LOCK_B
```
After this the program typically freezes due to deadlock.

---

### 3. What happened? (timeline)
| Step | Thread-1 | Thread-2 |
|------|----------|----------|
| 1 | Acquires `LOCK_A` | — |
| 2 | Sleeps | Acquires `LOCK_B` |
| 3 | Tries `LOCK_B` → blocked | Tries `LOCK_A` → blocked |

Final state:
- Thread-1 holds `LOCK_A`, waiting for `LOCK_B`.
- Thread-2 holds `LOCK_B`, waiting for `LOCK_A`.

---

### 4. Necessary conditions for deadlock
All four must hold:
1. **Mutual exclusion** — locks are exclusive.
2. **Hold and wait** — thread holds one lock and waits for another.
3. **No preemption** — locks cannot be forcibly taken away.
4. **Circular wait** — a cycle of threads each waiting for the next.

Breaking any one of these prevents deadlock.

---

### 5. Deadlock with more threads (3-way)
```java
Object A = new Object();
Object B = new Object();
Object C = new Object();

Thread t1 = new Thread(() -> {
    synchronized (A) {
        sleep(100);
        synchronized (B) {}
    }
});

Thread t2 = new Thread(() -> {
    synchronized (B) {
        sleep(100);
        synchronized (C) {}
    }
});

Thread t3 = new Thread(() -> {
    synchronized (C) {
        sleep(100);
        synchronized (A) {}
    }
});

t1.start();
t2.start();
t3.start();
```
Circular dependency: `A → B → C → A`.

---

### 6. Why the JVM does not resolve deadlocks
- The JVM does not know business logic.
- It cannot safely break locks without corrupting data.
- It favors correctness over liveness.
  Deadlock detection tools exist, but automatic resolution is the developer's responsibility.

---

### 7. How to detect deadlocks

- CLI: `jstack <process-id>` — output includes `Found one Java-level deadlock`.
- Programmatic:
```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

ThreadMXBean bean = ManagementFactory.getThreadMXBean();
long[] deadlockedThreads = bean.findDeadlockedThreads();
```

---

### 8. How to prevent deadlocks

1. **Enforce lock ordering** (strongest rule)  
   Always acquire locks in a consistent global order:
   ```java
   synchronized (LOCK_A) {
       synchronized (LOCK_B) {
           // safe
       }
   }
   ```
   Never allow one thread to lock A→B while another locks B→A.

### Deadlock with ReentrantLock
```java
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDeadlock {

    static ReentrantLock lockA = new ReentrantLock();
    static ReentrantLock lockB = new ReentrantLock();

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            lockA.lock();
            System.out.println("T1 acquired lockA");

            sleep(100);

            lockB.lock(); // DEADLOCK HERE
            System.out.println("T1 acquired lockB");

            lockB.unlock();
            lockA.unlock();
        });

        Thread t2 = new Thread(() -> {
            lockB.lock();
            System.out.println("T2 acquired lockB");

            sleep(100);

            lockA.lock(); // DEADLOCK HERE
            System.out.println("T2 acquired lockA");

            lockA.unlock();
            lockB.unlock();
        });

        t1.start();
        t2.start();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

2. **Use `ReentrantLock.tryLock()`** to avoid circular wait:
```java
if (lockA.tryLock()) {
    try {
        if (lockB.tryLock()) {
            try {
                // critical section
            } finally { lockB.unlock(); }
        }
    } finally { lockA.unlock(); }
}
```

3. **Minimize lock scope**  
   Lock only the minimal critical section and release locks as soon as possible.

---

### 9. ExecutorService and deadlocks
`ExecutorService` manages threads and queues tasks but does not enforce lock ordering. Deadlocks can still occur inside thread pools.
```markdown
Deadlock in an ExecutorService / thread pool is a very common and very dangerous real-world problem—especially in payment systems, Kafka consumers, async APIs, and microservices.

I’ll explain this in layers, so the mental model locks in permanently.

⸻

1️⃣ What is a deadlock (in simple words)?

A deadlock happens when:

Threads are waiting for each other forever, and none can make progress.

In executor pools, this often happens when:
	•	Threads submit tasks
	•	Those tasks wait for other tasks
	•	But no free threads exist to run the waiting tasks

⸻

2️⃣ The classic ExecutorService deadlock (MOST IMPORTANT)

❌ Code that looks correct but deadlocks

ExecutorService executor = Executors.newFixedThreadPool(1);

Future<String> future = executor.submit(() -> {
    // Task A
    System.out.println("Task A started");

    Future<String> inner = executor.submit(() -> {
        // Task B
        System.out.println("Task B started");
        return "Result B";
    });

    // DEADLOCK HERE
    return inner.get();
});

System.out.println(future.get());
executor.shutdown();


⸻

3️⃣ Step-by-step: WHY this deadlocks

Thread pool size = 1

Step	What happens
1	Pool has 1 thread
2	Thread executes Task A
3	Task A submits Task B
4	Task B is queued
5	Task A calls inner.get()
6	Task A blocks waiting for Task B
7	Task B cannot run (no free thread)
8	🔥 DEADLOCK

💥 Thread waits for itself indirectly

⸻

4️⃣ Why this happens ONLY in executor pools

Because:
	•	Executor threads are limited
	•	Blocking inside tasks is dangerous
	•	Tasks depend on other tasks in the same pool

This does not happen with:
	•	New Thread per task (inefficient but safe)
	•	Non-blocking async chains

⸻

5️⃣ Deadlock with multiple threads (still dangerous)

ExecutorService executor = Executors.newFixedThreadPool(2);

Callable<String> taskA = () -> {
    Future<String> f = executor.submit(taskB);
    return f.get();
};

Callable<String> taskB = () -> {
    Future<String> f = executor.submit(taskA);
    return f.get();
};

executor.submit(taskA);
executor.submit(taskB);

Pool size	2
Task A	waits for B
Task B	waits for A
Result	💀 DEADLOCK


⸻

6️⃣ Deadlock pattern name (INTERVIEW GOLD)

This is called:

Thread Pool Starvation Deadlock

Because:
	•	Threads exist
	•	Tasks exist
	•	But threads are starved by blocking dependencies

⸻

7️⃣ Real-world example (Payment / Banking)

// Payment Service
executor.submit(() -> {
    validatePayment(); // async
    reserveFunds();    // async
    sendNotification();// async
});

If:
	•	Each method submits tasks
	•	Each waits using Future.get()
	•	Same executor is used

➡️ Deadlock under high traffic

⸻

8️⃣ How to FIX executor deadlocks (IMPORTANT)

✅ Rule #1: Never block inside executor tasks

❌ Bad:

future.get();

✅ Good:
	•	Chain async work
	•	Use separate executors
	•	Use non-blocking constructs

⸻

✅ Solution 1: Increase pool size (TEMP FIX)

Executors.newFixedThreadPool(10);

⚠️ This reduces probability, not correctness.

⸻

✅ Solution 2: Separate executors (BEST PRACTICE)

ExecutorService cpuPool = Executors.newFixedThreadPool(4);
ExecutorService ioPool = Executors.newCachedThreadPool();

	•	CPU tasks → cpuPool
	•	IO tasks → ioPool

⸻

✅ Solution 3: Use CompletableFuture (BEST)

CompletableFuture
    .supplyAsync(this::taskA, executor)
    .thenCompose(result -> CompletableFuture.supplyAsync(this::taskB, executor))
    .thenAccept(System.out::println);

✔ No blocking
✔ No deadlock
✔ Scales cleanly

⸻

9️⃣ How Kafka avoids this

Kafka consumers:
	•	Never block waiting for other tasks
	•	Use polling + async processing
	•	Commit offsets independently

➡️ No cyclic waiting

⸻

🔟 Interview-ready explanation

Deadlock in executor pools occurs when tasks block waiting for other tasks in the same limited thread pool, causing thread starvation. The solution is to avoid blocking calls like Future.get inside tasks and use asynchronous chaining or separate executors.

⸻

Final mental model (remember this)

Executor threads must never wait for executor threads.

⸻

If you want next:
	•	Show deadlock vs livelock in executors
	•	Rewrite deadlock code using CompletableFuture
	•	Show how Spring @Async can cause this silently

Just say 👍
```
---

### 10. Real-world example
Bank transfers:
- Thread-1: transfer A → B (locks Account A, then B)
- Thread-2: transfer B → A (locks Account B, then A)  
  This pattern commonly causes production outages.

---

### 11. Summary (one-liner)
Deadlock occurs when multiple threads acquire locks in inconsistent order, creating a circular wait that the JVM cannot resolve.

### 12. Key takeaways
- Deadlocks are design issues, not syntax bugs.
- The JVM will not save you.
- Enforce lock ordering.
- `ReentrantLock` provides additional control.
- Thread pools do not eliminate deadlocks.

---


## Q. What is Thread Pool? How can we create Thread Pool in Java?
A thread pool reuses previously created threads to execute current tasks and offers a solution to the problem of thread cycle overhead and resource thrashing. Since the thread is already existing when the request arrives, the delay introduced by thread creation is eliminated, making the application more responsive.

Java provides the Executor framework which is centered around the Executor interface, its sub-interface –**ExecutorService** and the class-**ThreadPoolExecutor**, which implements both of these interfaces. By using the executor, one only has to implement the Runnable objects and send them to the executor to execute.

To use thread pools, we first create a object of ExecutorService and pass a set of tasks to it. ThreadPoolExecutor class allows to set the core and maximum pool size.The runnables that are run by a particular thread are executed sequentially.
```java
// Java program to illustrate  
// ThreadPool 
import java.text.SimpleDateFormat;  
import java.util.Date; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
  
// Task class to be executed (Step 1) 
class Task implements Runnable    
{ 
    private String name; 
      
    public Task(String s) { 
        name = s; 
    } 
      
    // Prints task name and sleeps for 1s 
    // This Whole process is repeated 5 times 
    public void run() { 
        try { 
            for (int i = 0; i<=5; i++) { 
                if (i == 0) { 
                    Date d = new Date(); 
                    SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss"); 
                    System.out.println("Initialization Time for"
                            + " task name - "+ name +" = " +ft.format(d));    
                    //prints the initialization time for every task  
                } 
                else { 
                    Date d = new Date(); 
                    SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss"); 
                    System.out.println("Executing Time for task name - "+ 
                            name +" = " +ft.format(d));    
                    // prints the execution time for every task  
                } 
                Thread.sleep(1000); 
            } 
            System.out.println(name+" complete"); 
        } 
        catch(InterruptedException e) { 
            e.printStackTrace(); 
        } 
    } 
} 
public class Test 
{ 
     // Maximum number of threads in thread pool 
    static final int MAX_T = 3;              
  
    public static void main(String[] args) { 
        // creates five tasks 
        Runnable r1 = new Task("task 1"); 
        Runnable r2 = new Task("task 2"); 
        Runnable r3 = new Task("task 3"); 
        Runnable r4 = new Task("task 4"); 
        Runnable r5 = new Task("task 5");       
          
        // creates a thread pool with MAX_T no. of  
        // threads as the fixed pool size(Step 2) 
        ExecutorService pool = Executors.newFixedThreadPool(MAX_T);   
         
        // passes the Task objects to the pool to execute (Step 3) 
        pool.execute(r1); 
        pool.execute(r2); 
        pool.execute(r3); 
        pool.execute(r4); 
        pool.execute(r5);  
          
        // pool shutdown ( Step 4) 
        pool.shutdown();     
    } 
} 
```
Output
```
Initialization Time for task name - task 2 = 02:32:56
Initialization Time for task name - task 1 = 02:32:56
Initialization Time for task name - task 3 = 02:32:56
Executing Time for task name - task 1 = 02:32:57
Executing Time for task name - task 2 = 02:32:57
Executing Time for task name - task 3 = 02:32:57
Executing Time for task name - task 1 = 02:32:58
Executing Time for task name - task 2 = 02:32:58
Executing Time for task name - task 3 = 02:32:58
Executing Time for task name - task 1 = 02:32:59
Executing Time for task name - task 2 = 02:32:59
Executing Time for task name - task 3 = 02:32:59
Executing Time for task name - task 1 = 02:33:00
Executing Time for task name - task 3 = 02:33:00
Executing Time for task name - task 2 = 02:33:00
Executing Time for task name - task 2 = 02:33:01
Executing Time for task name - task 1 = 02:33:01
Executing Time for task name - task 3 = 02:33:01
task 2 complete
task 1 complete
task 3 complete
Initialization Time for task name - task 5 = 02:33:02
Initialization Time for task name - task 4 = 02:33:02
Executing Time for task name - task 4 = 02:33:03
Executing Time for task name - task 5 = 02:33:03
Executing Time for task name - task 5 = 02:33:04
Executing Time for task name - task 4 = 02:33:04
Executing Time for task name - task 4 = 02:33:05
Executing Time for task name - task 5 = 02:33:05
Executing Time for task name - task 5 = 02:33:06
Executing Time for task name - task 4 = 02:33:06
Executing Time for task name - task 5 = 02:33:07
Executing Time for task name - task 4 = 02:33:07
task 5 complete
task 4 complete
```
**Risks in using Thread Pools**

* Deadlock
* Thread Leakage
* Resource Thrashing



---

## Q. How is the safety of a thread achieved?
* Immutable objects are by default thread-safe because there state can not be modified once created. Since String is immutable in Java, its inherently thread-safe.
* Read only or final variables in Java are also thread-safe in Java.
* Locking is one way of achieving thread-safety in Java.
* Static variables if not synchronized properly becomes major cause of thread-safety issues.
* Example of thread-safe class in Java: Vector, Hashtable, ConcurrentHashMap, String etc.
* Atomic operations in Java are thread-safe e.g. reading a 32 bit int from memory because its an atomic operation it can't interleave with other thread.
* local variables are also thread-safe because each thread has there own copy and using local variables is good way to writing thread-safe code in Java.
* In order to avoid thread-safety issue minimize sharing of objects between multiple thread.
* Volatile keyword in Java can also be used to instruct thread not to cache variables and read from main memory and can also instruct JVM not to reorder or optimize code from threading perspective.

---

## Q. Why we use Vector class?
Vector implements a dynamic array that means it can grow or shrink as required. Like an array, it contains components that can be accessed using an integer index. They are very similar to ArrayList but Vector is **synchronised** and have some legacy method which collection framework does not contain. It extends AbstractList and implements List interfaces.
```java
import java.util.*; 
class Vector_demo { 

    public static void main(String[] arg) { 

        // create default vector 
        Vector v = new Vector(); 
        v.add(10); 
        v.add(20); 
        v.add("Numbers");   
        System.out.println("Vector is " + v); 
    } 
}
```

---


## Q. What is Thread Group? Why it\'s advised not to use it?
ThreadGroup creates a group of threads. It offers a convenient way to manage groups of threads as a unit. This is particularly valuable in situation in which you want to suspend and resume a number of related threads.

* The thread group form a tree in which every thread group except the initial thread group has a parent.
* A thread is allowed to access information about its own thread group but not to access information about its thread group\'s parent thread group or any other thread group.
```java
// Java code illustrating Thread Group 
import java.lang.*; 
class NewThread extends Thread  
{ 
    NewThread(String threadname, ThreadGroup tgob) { 
        super(tgob, threadname); 
        start(); 
    } 
    public void run() { 
  
        for (int i = 0; i < 1000; i++) { 
            try { 
                Thread.sleep(10); 
            } 
            catch (InterruptedException ex) { 
                System.out.println("Exception encounterted"); 
            } 
        } 
    } 
}  
public class ThreadGroupExample  
{ 
    public static void main(String arg[]) { 
        // creating the thread group 
        ThreadGroup tg = new ThreadGroup("Parent Thread Group"); 
  
        NewThread t1 = new NewThread("One", tg); 
        System.out.println("Starting One"); 
        NewThread t2 = new NewThread("Two", tg); 
        System.out.println("Starting two"); 
  
        // checking the number of active thread 
        System.out.println("Number of active thread: "
                           + tg.activeCount()); 
    } 
} 
```

--- 


## Q. What is ThreadLocal?
The Java ThreadLocal class enables you to create variables that can only be read and written by the same thread. Thus, even if two threads are executing the same code, and the code has a reference to the same ThreadLocal variable, the two threads cannot see each other's ThreadLocal variables. Thus, the Java ThreadLocal class provides a simple way to make code thread safe that would not otherwise be so.

**Creating a ThreadLocal**
```java
private ThreadLocal threadLocal = new ThreadLocal();
```
**Set ThreadLocal Value**
```java
threadLocal.set("A thread local value");
```
**Get ThreadLocal Value**
```java
String threadLocalValue = (String) threadLocal.get();
```
**Remove ThreadLocal Value**
```java
threadLocal.remove();
```
Example
```java
// Java code illustrating get() and set() method 
public class ThreadLocalExample { 
  
    public static void main(String[] args) { 
  
        ThreadLocal<Number> tlObj = new ThreadLocal<Number>(); 
  
        // setting the value 
        tlObj.set(100);  
        System.out.println("value = " + tlObj.get()); 
    } 
} 
```

---

## Q. What is Java Thread Dump, How can we get Java Thread dump of a Program?
A Java thread dump is a way of finding out what every thread in the JVM is doing at a particular point in time. This is especially useful if your Java application sometimes seems to hang when running under load, as an analysis of the dump will show where the threads are stuck.

You can generate a thread dump under Unix/Linux by running `kill -QUIT <pid>`, and under Windows by hitting `Ctl + Break`.
Thread dump is the list of all the threads, every entry shows information about thread which includes following in the order of appearance.

**Java Thread Dump Example**
```
2019-12-26 22:28:39
Full thread dump Java HotSpot(TM) 64-Bit Server VM (23.5-b02 mixed mode):

"Attach Listener" daemon prio=5 tid=0x00007fb7d8000000 nid=0x4207 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Timer-0" daemon prio=5 tid=0x00007fb7d4867000 nid=0x5503 waiting on condition [0x00000001604d9000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(Native Method)
	at com.journaldev.threads.MyTimerTask.completeTask(MyTimerTask.java:19)
	at com.journaldev.threads.MyTimerTask.run(MyTimerTask.java:12)
	at java.util.TimerThread.mainLoop(Timer.java:555)
	at java.util.TimerThread.run(Timer.java:505)

"Service Thread" daemon prio=5 tid=0x00007fb7d482c000 nid=0x5303 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread1" daemon prio=5 tid=0x00007fb7d482b800 nid=0x5203 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread0" daemon prio=5 tid=0x00007fb7d4829800 nid=0x5103 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Signal Dispatcher" daemon prio=5 tid=0x00007fb7d4828800 nid=0x5003 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Finalizer" daemon prio=5 tid=0x00007fb7d4812000 nid=0x3f03 in Object.wait() [0x000000015fd26000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x0000000140a25798> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:135)
	- locked <0x0000000140a25798> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:151)
	at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:177)

"Reference Handler" daemon prio=5 tid=0x00007fb7d4811800 nid=0x3e03 in Object.wait() [0x000000015fc23000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on <0x0000000140a25320> (a java.lang.ref.Reference$Lock)
	at java.lang.Object.wait(Object.java:503)
	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:133)
	- locked <0x0000000140a25320> (a java.lang.ref.Reference$Lock)

"main" prio=5 tid=0x00007fb7d5000800 nid=0x1703 waiting on condition [0x0000000106116000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(Native Method)
	at com.journaldev.threads.MyTimerTask.main(MyTimerTask.java:33)

"VM Thread" prio=5 tid=0x00007fb7d480f000 nid=0x3d03 runnable 
"GC task thread#0 (ParallelGC)" prio=5 tid=0x00007fb7d500d800 nid=0x3503 runnable 
"GC task thread#1 (ParallelGC)" prio=5 tid=0x00007fb7d500e000 nid=0x3603 runnable 
"GC task thread#2 (ParallelGC)" prio=5 tid=0x00007fb7d5800000 nid=0x3703 runnable 
"GC task thread#3 (ParallelGC)" prio=5 tid=0x00007fb7d5801000 nid=0x3803 runnable 
"GC task thread#4 (ParallelGC)" prio=5 tid=0x00007fb7d5801800 nid=0x3903 runnable 
"GC task thread#5 (ParallelGC)" prio=5 tid=0x00007fb7d5802000 nid=0x3a03 runnable 
"GC task thread#6 (ParallelGC)" prio=5 tid=0x00007fb7d5802800 nid=0x3b03 runnable 
"GC task thread#7 (ParallelGC)" prio=5 tid=0x00007fb7d5803800 nid=0x3c03 runnable 
"VM Periodic Task Thread" prio=5 tid=0x00007fb7d481e800 nid=0x5403 waiting on condition 

JNI global references: 116
```
* **Thread Name**: Name of the Thread
* **Thread Priority**: Priority of the thread
* **Thread ID**: Represents the unique ID of the Thread
* **Thread Status**: Provides the current thread state, for example RUNNABLE, WAITING, BLOCKED. While analyzing deadlock look for the blocked threads and resources on which they are trying to acquire lock.
* **Thread callstack**: Provides the vital stack information for the thread. This is the place where we can see the locks obtained by Thread and if it\'s waiting for any lock.

**Tools**

* jstack
* JVisualVM
* JMC
* ThreadMXBean
* APM Tool – App Dynamics
* JCMD
* VisualVM Profiler

---




## Q. What is Lock interface in Java Concurrency API? What is the Difference between ReentrantLock and Synchronized?
A `java.util.concurrent.locks.Lock` is a thread synchronization mechanism just like synchronized blocks. A Lock is, however, more flexible and more sophisticated than a synchronized block. Since Lock is an interface, you need to use one of its implementations to use a Lock in your applications. `ReentrantLock` is one such implementation of Lock interface.
```java
Lock lock = new ReentrantLock();
 
lock.lock();
 
//critical section
lock.unlock();
```

**Difference between Lock Interface and synchronized keyword**

* ReentrantLock is a class in java.util.concurrent.locks.
- You explicitly acquire the lock with lock().
- You explicitly release it with unlock().
- Supports features that synchronized doesn’t:
- Try locking (tryLock())
- Timeout-based locking
- Fairness
- Lock across multiple methods

Rule: The same thread that acquires the lock must release it, usually in a finally block.
* Having a timeout trying to get access to a `synchronized` block is not possible. Using `Lock.tryLock(long timeout, TimeUnit timeUnit)`, it is possible.
* The `synchronized` block must be fully contained within a single method. A Lock can have it\'s calls to `lock()` and `unlock()` in separate methods.

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyLockExample implements Runnable {

	private Resource resource;
	private Lock lock;
	
	public ConcurrencyLockExample(Resource r) {
		this.resource = r;
		this.lock = new ReentrantLock();
	}
	
	@Override
	public void run() {
		try {
			if(lock.tryLock(10, TimeUnit.SECONDS)) {
			  resource.doSomething();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			//release lock
			lock.unlock();
		}
		resource.doLogging();
	}
}
```
## Q. What is the difference between the Runnable and Callable interface?
Runnable and Callable interface both are used in the multithreading environment. Runnable is the core interface provided for representing multi-threaded tasks and Callable is an improved version of Runnable that was added in Java 1.5.

**Difference between Callable and Runnable in Java**

**1. Checked Exception**: Callable's call() method can throw checked exception while  Runnable run() method can  not throw checked exception.

**2. Return value**: Return type of Runnable run() method is void , so it can not return any value. while Callable can return the Future object, which represents the life cycle of a task and provides methods to check if the task has been completed or canceled.

**3. Implementation**: Callable needs to implement call() method while Runnable needs to implement run() method.

**4. Execution**: Limitation of Callable interface lies in java is that one can not pass it to Thread as one pass the Runnable instance. There is no constructor defined in the Thread class which accepts a Callable interface.

Example: Callable Interface
```java
// Java program to illustrate Callable and FutureTask 
// for random number generation 
import java.util.Random; 
import java.util.concurrent.Callable; 
import java.util.concurrent.FutureTask; 
  
class CallableExample implements Callable 
{ 
  
  public Object call() throws Exception { 
    Random generator = new Random(); 
    Integer randomNumber = generator.nextInt(5); 
    Thread.sleep(randomNumber * 1000); 
    return randomNumber; 
  } 
} 
  
public class CallableFutureTest 
{ 
  public static void main(String[] args) throws Exception { 
  
    // FutureTask is a concrete class that 
    // implements both Runnable and Future 
    FutureTask[] randomNumberTasks = new FutureTask[5]; 
  
    for (int i = 0; i < 5; i++) { 
      Callable callable = new CallableExample(); 
  
      // Create the FutureTask with Callable 
      randomNumberTasks[i] = new FutureTask(callable); 
  
      // As it implements Runnable, create Thread 
      // with FutureTask 
      Thread t = new Thread(randomNumberTasks[i]); 
      t.start(); 
    } 
  
    for (int i = 0; i < 5; i++) { 
      // As it implements Future, we can call get() 
      System.out.println(randomNumberTasks[i].get()); 
    } 
  } 
}
```
Output
```
4
2
3
3
0
```
Example: Runnable Interface
```java
// Java program to illustrate Runnable 
// for random number generation 
import java.util.Random; 
import java.util.concurrent.Callable; 
import java.util.concurrent.FutureTask; 
  
class RunnableExample implements Runnable 
{ 
    // Shared object to store result 
    private Object result = null; 
  
    public void run() { 
        Random generator = new Random(); 
        Integer randomNumber = generator.nextInt(5); 
  
        // As run cannot throw any Exception 
        try { 
            Thread.sleep(randomNumber * 1000); 
        } catch (InterruptedException e) { 
            e.printStackTrace(); 
        } 
  
        // Store the return value in result when done 
        result = randomNumber; 
  
        // Wake up threads blocked on the get() method 
        synchronized(this) { 
            notifyAll(); 
        } 
    } 
  
    public synchronized Object get() throws InterruptedException  { 
        while (result == null) 
            wait(); 
  
        return result; 
    } 
} 

public class RunnableTest 
{ 
    public static void main(String[] args) throws Exception { 
        RunnableExample[] randomNumberTasks = new RunnableExample[5]; 
  
        for (int i = 0; i < 5; i++) { 
            randomNumberTasks[i] = new RunnableExample(); 
            Thread t = new Thread(randomNumberTasks[i]); 
            t.start(); 
        } 
  
        for (int i = 0; i < 5; i++) 
            System.out.println(randomNumberTasks[i].get()); 
    } 
} 
```
Output
```
0
4
3
1
4
```

---

## Q. What is the Thread\'s interrupt flag? How does it relate to the InterruptedException?
If any thread is in sleeping or waiting state (i.e. sleep() or wait() is invoked), calling the interrupt() method on the thread, breaks out the sleeping or waiting state throwing InterruptedException. If the thread is not in the sleeping or waiting state, calling the interrupt() method performs normal behaviour and doesn't interrupt the thread but sets the interrupt flag to true.

Example: **Interrupting a thread that stops working**
```java
// Java Program to illustrate the concept of interrupt() method 
// while a thread stops working 
class ThreadsInterruptExample extends Thread { 

    public void run() { 
        try { 
            Thread.sleep(1000); 
            System.out.println("Task"); 
        } catch (InterruptedException e) { 
            throw new RuntimeException("Thread interrupted"); 
        } 
    } 
    public static void main(String args[]) { 
        ThreadsInterruptExample t1 = new ThreadsInterruptExample(); 
        t1.start(); 
        try { 
            t1.interrupt(); 
        } 
        catch (Exception e) { 
            System.out.println("Exception handled"); 
        } 
    } 
} 
```
Output
```
Exception in thread "Thread-0" java.lang.RuntimeException: Thread interrupted
```
## Q. What is Java Memory Model (JMM)? Describe its purpose and basic ideas.
The Java memory model used internally in the JVM divides memory between thread stacks and the heap. Each thread running in the Java virtual machine has its own thread stack. The thread stack contains information about what methods the thread has called to reach the current point of execution.

The thread stack also contains all local variables for each method being executed (all methods on the call stack). A thread can only access it's own thread stack. Local variables created by a thread are invisible to all other threads than the thread who created it. Even if two threads are executing the exact same code, the two threads will still create the local variables of that code in each their own thread stack. Thus, each thread has its own version of each local variable.

All local variables of primitive types ( boolean, byte, short, char, int, long, float, double) are fully stored on the thread stack and are thus not visible to other threads. One thread may pass a copy of a pritimive variable to another thread, but it cannot share the primitive local variable itself.

The heap contains all objects created in your Java application, regardless of what thread created the object. This includes the object versions of the primitive types (e.g. Byte, Integer, Long etc.). It does not matter if an object was created and assigned to a local variable, or created as a member variable of another object, the object is still stored on the heap.


---

## Q. Describe the conditions of livelock and starvation?
**Livelock** occurs when two or more processes continually repeat the same interaction in response to changes in the other processes without doing any useful work. These processes are not in the waiting state, and they are running concurrently. This is different from a deadlock because in a deadlock all processes are in the waiting state.
```java
var l1 = .... // lock object like semaphore or mutex etc 
var l2 = .... // lock object like semaphore or mutex etc 
      
    // Thread1 
    Thread.Start( ()=> { 
              
    while (true) { 
          
        if (!l1.Lock(1000)) { 
            continue; 
        } 
        if (!l2.Lock(1000)) { 
            continue; 
        } 
          
        // do some work 
    }); 
  
    // Thread2 
    Thread.Start( ()=> { 
            
      while (true) { 
            
        if (!l2.Lock(1000)) { 
            continue; 
        }   
        if (!l1.Lock(1000)) { 
            continue; 
        }   
        // do some work 
    }); 
```

**starvation** describes a situation where a greedy thread holds a resource for a long time so other threads are blocked forever. The blocked threads are waiting to acquire the resource but they never get a chance. Thus they starve to death.

Starvation can occur due to the following reasons:

* Threads are blocked infinitely because a thread takes long time to execute some synchronized code (e.g. heavy I/O operations or infinite loop).

* A thread doesn’t get CPU\'s time for execution because it has low priority as compared to other threads which have higher priority.

* Threads are waiting on a resource forever but they remain waiting forever because other threads are constantly notified instead of the hungry ones.


---

## Q. How do I share a variable between 2 Java threads?
We should declare such variables as static and volatile.

**Volatile variables** are shared across multiple threads. This means that individual threads won’t cache its copy in the thread local. But every object would have its own copy of the variable so threads may cache value locally.

We know that static fields are shared across all the objects of the class, and it belongs to the class and not the individual objects. But, for static and non-volatile variable also, threads may cache the variable locally.

## Q. What are the main components of concurrency API?
The concurrency API are designed and optimized specifically for synchronized multithreaded access. They are grouped under the `java.util.concurrent` package.

**Main Components**

* Executor
* ExecutorService
* ScheduledExecutorService
* Future
* CountDownLatch
* CyclicBarrier
* Semaphore
* ThreadFactory
* BlockingQueue
* DelayQueue
* Locks
* Phaser



## CountDownLatch
## What problem does `CountDownLatch` solve?

> “One or more threads must wait until a set of other threads finishes some work.”

This is a one-way dependency:  
- Workers → finish  
- Waiting thread → proceeds

Unlike `CyclicBarrier`, workers do **NOT** wait for each other.

---

## Basic `CountDownLatch` example

**Scenario**  
Main thread waits for 3 worker threads

```java
import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {

    private static final int WORKERS = 3;
    private static CountDownLatch latch = new CountDownLatch(WORKERS);

    public static void main(String[] args) throws InterruptedException {

        for (int i = 1; i <= WORKERS; i++) {
            int id = i;
            new Thread(() -> workerTask(id)).start();
        }

        System.out.println("Main thread waiting...");
        latch.await();   // WAIT POINT
        System.out.println("All workers finished. Main continues.");
    }

    private static void workerTask(int id) {
        try {
            System.out.println("Worker " + id + " started");
            Thread.sleep(1000 * id);
            System.out.println("Worker " + id + " finished");
        } catch (InterruptedException ignored) {
        } finally {
            latch.countDown(); // SIGNAL COMPLETION
        }
    }
}
```

**Output (order may vary)**

```text
Main thread waiting...
Worker 1 started
Worker 2 started
Worker 3 started
Worker 1 finished
Worker 2 finished
Worker 3 finished
All workers finished. Main continues.
```

---

## Key rules (MEMORIZE THESE)

- `await()` blocks
  - The waiting thread sleeps until count reaches zero, if th count never reaches to 0, waiting thread will be sleeping only, no execution

- `countDown()` never blocks
  - Worker threads: just decrement the count, never wait. Which means, it will decrement the count and move forward with code execution written in it. 
  - It does not block the execution of self, only the waiting thread's execution is blocked.

- Count only goes DOWN
  - Once it reaches zero:
      - It stays zero
      - Cannot be reset  
        This is why it is not reusable.

---

## Multiple waiting threads (IMPORTANT)

More than one thread can wait:

```java
latch.await();
```

All of them unblock when count reaches zero.

---

## Real-world example: Application startup

```java
CountDownLatch startupLatch = new CountDownLatch(3);

// DB init
new Thread(() -> {
    initDB();
    startupLatch.countDown();
}).start();

// Cache init
new Thread(() -> {
    initCache();
    startupLatch.countDown();
}).start();

// Kafka init
new Thread(() -> {
    initKafka();
    startupLatch.countDown();
}).start();

startupLatch.await();
System.out.println("Application fully started");
```

---

## Common mistakes

- ❌ Using it for producer-consumer
- ❌ Expecting workers to block
- ❌ Forgetting `countDown()` in `finally`
- ❌ Expecting reuse

---

## Internally (simple view)

- Uses a counter
- `await()` → thread parked
- `countDown()` → atomic decrement
- When count reaches zero → unpark all waiters

---

## Interview one-liner

`CountDownLatch` allows one or more threads to wait until a fixed number of operations performed by other threads completes.

---

---

## `CyclicBarrier` — purpose and mental model

## What problem does `CyclicBarrier` solve?

> “Multiple threads must all reach a point before any of them can proceed.”

This is very common in: 
- Parallel computation
- Batch processing
- Multi-stage workflows
- Simulations
- Microservice fan-out → fan-in patterns

---

## Mental model (IMPORTANT)

Think of a checkpoint:
- Threads run independently
- They all stop at a barrier
- When all required threads arrive, the barrier opens
- All threads proceed together

And unlike `CountDownLatch`, this checkpoint can be reused → hence *cyclic*.

---

## Simple analogy

**🚌 Bus analogy**
- A bus leaves only when all passengers arrive
- If 5 passengers are expected:
    - Passenger 1 waits
    - Passenger 2 waits
    - …
    - Passenger 5 arrives → bus departs
- Same bus can be reused for the next trip

---

## Basic `CyclicBarrier` example

**Scenario**  
3 threads do some work → wait → continue together

```java
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

    private static final int THREAD_COUNT = 3;
    private static CyclicBarrier barrier =
            new CyclicBarrier(THREAD_COUNT, () ->
                    System.out.println("All threads reached the barrier. Proceeding...\n"));

    public static void main(String[] args) {

        for (int i = 1; i <= THREAD_COUNT; i++) {
            int id = i;
            new Thread(() -> task(id)).start();
        }
    }

    private static void task(int id) {
        try {
            System.out.println("Thread " + id + " doing initial work");
            Thread.sleep(1000 * id); // simulate different speeds

            System.out.println("Thread " + id + " waiting at barrier");
            barrier.await(); // WAIT POINT

            System.out.println("Thread " + id + " resumed after barrier");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**Output (order may vary)**

```text
Thread 1 doing initial work
Thread 2 doing initial work
Thread 3 doing initial work
Thread 1 waiting at barrier
Thread 2 waiting at barrier
Thread 3 waiting at barrier
All threads reached the barrier. Proceeding...

Thread 3 resumed after barrier
Thread 1 resumed after barrier
Thread 2 resumed after barrier
```

---

## Key things to notice

1️⃣ Threads stop at `await()` — they block until all parties arrive.

2️⃣ Barrier action (optional)

```java
new CyclicBarrier(3, () -> System.out.println("Barrier opened"));
```

- Runs once
- Runs by last arriving thread
- Runs before threads are released

3️⃣ Barrier is reusable
- After all threads pass:
    - Barrier resets automatically
    - Can be used again

---

## `CyclicBarrier` vs `CountDownLatch` (IMPORTANT)

| Feature                               | CyclicBarrier | CountDownLatch |
|---------------------------------------|:-------------:|:--------------:|
| Reusable                              | Yes           | No             |
| Threads wait for each other           | Yes           | No             |
| Common use                            | Phases        | One-time events|
| Reset automatically                   | Yes           | No             |

---

## Real-world example: Parallel payment checks

Imagine a payment gateway doing parallel validations:
- Fraud check
- Balance check
- Limit check

All must finish before proceeding.

```java
CyclicBarrier barrier = new CyclicBarrier(3, () ->
    System.out.println("All validations passed. Proceeding with payment")
);

Runnable fraudCheck = () -> performCheck("Fraud", barrier);
Runnable balanceCheck = () -> performCheck("Balance", barrier);
Runnable limitCheck = () -> performCheck("Limit", barrier);

new Thread(fraudCheck).start();
new Thread(balanceCheck).start();
new Thread(limitCheck).start();

private static void performCheck(String name, CyclicBarrier barrier) {
    try {
        System.out.println(name + " check started");
        Thread.sleep(1000);
        System.out.println(name + " check done");
        barrier.await();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

Only when all checks complete, the payment proceeds.

---

## Failure behavior (IMPORTANT)

- If one thread fails or times out:
    - Barrier is broken
    - All waiting threads get `BrokenBarrierException`

This is by design.

---

## When NOT to use `CyclicBarrier`

- ❌ Producer-consumer → use `BlockingQueue`
- ❌ Limiting concurrency → use `Semaphore`
- ❌ One-time coordination → use `CountDownLatch`

---

## Interview-ready summary

`CyclicBarrier` lets multiple threads wait for each other at a common point and proceed together once all have arrived. It is reusable and ideal for phased parallel workflows.

---

## CountDownLatch vs CyclicBarrier — Deep Explanation

1️⃣ **Core difference (ONE LINE)**
- `CountDownLatch` waits for events to finish.
- `CyclicBarrier` waits for threads to meet.

Keep this sentence in mind — everything flows from it.

2️⃣ **Mental model (MOST IMPORTANT)**

- **🧱 CountDownLatch — “Finish Line”**
    - Some threads do work
    - They signal completion
    - One or more threads wait until all work is done
    - Workers never wait.

- **🚧 CyclicBarrier — “Checkpoint”**
    - All participating threads: work independently, stop at a common point, resume together
    - Everyone waits for everyone.

3️⃣ **Visual Timeline**

**CountDownLatch**

```
Worker-1 ──────✔
Worker-2 ─────────✔
Worker-3 ────✔
↓
Main Thread ──────────────▶ CONTINUE
```

**CyclicBarrier**

```
Thread-1 ────┐
Thread-2 ─────┼── WAIT ──▶ ALL CONTINUE
Thread-3 ─────┘
```

4️⃣ **Who waits?**

| Concept          | CountDownLatch | CyclicBarrier |
|------------------|:--------------:|:-------------:|
| Worker threads   | ❌ Never       | ✅ Yes        |
| Waiting threads  | ✅ Yes         | ✅ Yes        |
| Mutual waiting   | ❌ No          | ✅ Yes        |

5️⃣ **Reusability (CRITICAL)**

- **CountDownLatch**
  ```java
  CountDownLatch latch = new CountDownLatch(3);
  ```
    - Count only goes down
    - Cannot be reset
    - One-time use only

- **CyclicBarrier**
  ```java
  CyclicBarrier barrier = new CyclicBarrier(3);
  ```
    - Resets automatically
    - Can be reused across phases
    - Designed for loops / stages

6️⃣ **Code comparison (SIDE BY SIDE)**

**CountDownLatch — wait for workers**
```java
CountDownLatch latch = new CountDownLatch(3);

new Thread(() -> {
    doWork();
    latch.countDown();
}).start();

latch.await(); // waiting thread blocks
```
- ✔ Worker finishes → signals
- ✔ Waiting thread resumes when count = 0

**CyclicBarrier — wait for each other**
```java
CyclicBarrier barrier = new CyclicBarrier(3);

new Thread(() -> {
    doWork();
    barrier.await();
}).start();
```
- ✔ Thread stops
- ✔ Waits until all threads arrive
- ✔ All resume together

7️⃣ **Barrier action (ONLY CyclicBarrier)**
```java
new CyclicBarrier(3, () -> {
    System.out.println("All threads reached checkpoint");
});
```
- Runs once per cycle
- Executed by last arriving thread
- Useful for phase transitions

❌ `CountDownLatch` has no such feature

8️⃣ **Failure behavior (IMPORTANT)**

- **CountDownLatch**
    - If one worker never calls `countDown()` → waiting thread blocks forever
    - No automatic failure propagation.

- **CyclicBarrier**
    - If one thread times out or fails → barrier becomes broken → all waiting threads throw `BrokenBarrierException`
    - Safer for coordinated workflows

9️⃣ **Internals (Simplified)**

| Aspect           | CountDownLatch     | CyclicBarrier               |
|------------------|:------------------:|:---------------------------:|
| Backed by        | AQS counter        | ReentrantLock + Condition   |
| Coordination     | Event-based        | Phase-based                 |
| Reset            | ❌                 | ✅                          |
| Thread awareness | Low                | High                        |

🔟 **Real-world usage**

- Use `CountDownLatch` when:
    - App startup
    - Waiting for parallel API calls
    - Testing async logic
    - Fan-out → join

- Use `CyclicBarrier` when:
    - Parallel computation
    - Simulation steps
    - Batch processing
    - Multi-phase pipelines

1️⃣1️⃣ **What NOT to use them for**

- ❌ Producer–Consumer → use `BlockingQueue`
- ❌ Limiting concurrency → use `Semaphore`
- ❌ Async pipelines → use `CompletableFuture`

1️⃣2️⃣ **Interview traps (VERY COMMON)**

- ❌ “They are similar”
- ❌ “Both block threads” (true but shallow)
- ❌ “Latch is reusable”
- ❌ “Barrier blocks only one thread”

1️⃣3️⃣ **Interview-ready answer**

`CountDownLatch` is used when one or more threads must wait for a fixed number of operations to complete, while `CyclicBarrier` is used when a fixed number of threads must wait for each other to reach a common execution point before continuing together. The latch is one-time use; the barrier is reusable.

1️⃣4️⃣ **Final comparison table**

| Feature             | CountDownLatch | CyclicBarrier |
|---------------------|:--------------:|:-------------:|
| Wait style          | One-direction  | Mutual        |
| Worker waits        | ❌             | ✅            |
| Resettable          | ❌             | ✅            |
| Barrier action      | ❌             | ✅            |
| Failure propagation | ❌             | ✅            |
| Typical use         | Completion     | Coordination  |

---





## Q. What is difference between CyclicBarrier and CountDownLatch in Java?
Both CyclicBarrier and CountDownLatch are used to implement a scenario where one Thread waits for one or more Thread to complete their job before starts processing. The differences are:

**1. Definition**:  CountDownLatch  is a synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes.

CyclicBarrier is a synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point.

**2. Reusable**: A CountDownLatch is initialized with given count. count reaches zero by calling of countDown() method. The count can not be reset.

CyclicBarrier is used to reset count. The barrier is called cyclic because it can be reused after the waiting threads are released (or count  become zero).

## Q. What is Semaphore in Java concurrency?
A Semaphore is a thread synchronization construct that can be used either to send signals between threads to avoid missed signals, or to guard a critical section like you would with a lock. Java 5 comes with semaphore implementations in the java.util.concurrent package.

**1. Simple Semaphore**:
```java
public class Semaphore {
  private boolean signal = false;

  public synchronized void take() {
    this.signal = true;
    this.notify();
  }

  public synchronized void release() throws InterruptedException{
    while(!this.signal) wait();
    this.signal = false;
  }
}
```
The take() method sends a signal which is stored internally in the Semaphore. The release() method waits for a signal. When received the signal flag is cleared again, and the release() method exited.

**2. Counting Semaphore**:
```java
public class CountingSemaphore {
  private int signals = 0;

  public synchronized void take() {
    this.signals++;
    this.notify();
  }

  public synchronized void release() throws InterruptedException{
    while(this.signals == 0) wait();
    this.signals--;
  }
}
```
**3. Bounded Semaphore**
```java
public class BoundedSemaphore {
  private int signals = 0;
  private int bound   = 0;

  public BoundedSemaphore(int upperBound){
    this.bound = upperBound;
  }

  public synchronized void take() throws InterruptedException{
    while(this.signals == bound) wait();
    this.signals++;
    this.notify();
  }

  public synchronized void release() throws InterruptedException{
    while(this.signals == 0) wait();
    this.signals--;
    this.notify();
  }
}
```
**4. Using Semaphores as Locks**
```java
BoundedSemaphore semaphore = new BoundedSemaphore(1);

...

semaphore.take();

try{
  //critical section
} finally {
  semaphore.release();
}
```

### Semaphore to limit number of concurrent threads

**What problem does Semaphore solve?**
- Limit how many threads can enter a critical section at the same time
- Not one thread (that’s synchronized / Lock), but N threads.

**Basic idea**
```java
Semaphore semaphore = new Semaphore(3);
```
-	3 → number of permits
-	Each thread must:
-	acquire() before entering
-	release() after leaving

```java
import java.util.concurrent.Semaphore;

public class SemaphoreDemo {

    static Semaphore semaphore = new Semaphore(3);

    public static void main(String[] args) {

        for (int i = 1; i <= 10; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + id + " waiting...");
                    semaphore.acquire();

                    System.out.println("Thread " + id + " entered");
                    Thread.sleep(2000); // simulate work

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Thread " + id + " leaving");
                    semaphore.release();
                }
            }).start();
        }
    }
}
```

**What happens internally?**
-	First 3 threads → pass immediately
-	Remaining 7 threads → BLOCK
-	When one finishes → release() → next thread proceeds

**Output (conceptually)**
```
Thread 1 entered
Thread 2 entered
Thread 3 entered
Thread 4 waiting
Thread 5 waiting
...
Thread 1 leaving
Thread 4 entered
```
✔ Controlled concurrency
✔ No thread explosion
✔ No busy waiting

**Real-world web application usage**

Example: Payment Gateway limit
```java
Semaphore paymentLimit = new Semaphore(10);

public void processPayment() {
    try {
        paymentLimit.acquire();
        // call external payment API
    } finally {
        paymentLimit.release();
    }
}
```

Why?
-	Payment provider allows only 10 concurrent calls
-	Semaphore enforces that globally


**Fair vs Non-fair semaphore**
```java
Semaphore fairSemaphore = new Semaphore(3, true);
```
-	true → FIFO order
-	false (default) → better throughput, possible starvation

**tryAcquire() – no blocking**
```java
if (semaphore.tryAcquire()) {
    try {
        // do work
    } finally {
        semaphore.release();
    }
} else {
        // reject request / return 429
}
```

Used heavily in:
-	Rate limiting
-	Load shedding
-	Circuit breakers

⸻

**Common mistakes (VERY IMPORTANT)**

❌ Forgetting release() → deadlock

❌ Acquiring inside loop without release

❌ Using Semaphore where ExecutorService is enough

❌ Using Semaphore instead of DB-level locking

**When SHOULD you use Semaphore?**

- Limit DB connections
- Limit external API calls
- Throttle expensive computation
- Protect scarce resources

**When NOT to use Semaphore?**

❌ Ordering guarantees
❌ Mutual exclusion (use Lock)
❌ Per-request task execution (use ExecutorService)
---

## Q. What is Callable and Future in Java concurrency?
Future and FutureTask in Java allows to write asynchronous code. A Future interface provides methods **to check if the computation is complete, to wait for its completion and to retrieve the results of the computation**. The result is retrieved using Future\'s get() method when the computation has completed, and it blocks until it is completed. We need a callable object to create a future task and then we can use Java Thread Pool Executor to process these asynchronously.

```java
import java.util.concurrent.Callable; 
import java.util.concurrent.ExecutionException; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.Future; 
import java.util.logging.Level; 
import java.util.logging.Logger; 
/** 
* Java program to show how to use Future in Java. Future allows to write 
* asynchronous code in Java, where Future promises result to be available in 
* future  
**/ 
public class FutureExample { 

    private static final ExecutorService threadpool = Executors.newFixedThreadPool(3); 

    public static void main(String args[]) throws InterruptedException, ExecutionException { 
        
        FactorialCalculator task = new FactorialCalculator(10); 
        System.out.println("Submitting Task ..."); 
        
        Future future = threadpool.submit(task); 
        
        System.out.println("Task is submitted"); 
        
        while (!future.isDone()) { 
            System.out.println("Task is not completed yet...."); 
            Thread.sleep(1); //sleep for 1 millisecond before checking again 
        } 
        System.out.println("Task is completed, let's check result"); 
        long factorial = future.get(); 
        System.out.println("Factorial of 1000000 is : " + factorial); 
        
        threadpool.shutdown(); 
    } 
    private static class FactorialCalculator implements Callable { 
        
        private final int number; 
        
        public FactorialCalculator(int number) { 
            this.number = number; 
        } 
        @Override public Long call() { 
            long output = 0; 
            try { 
                output = factorial(number); 
            } catch (InterruptedException ex) { 
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex); 
            } 
            return output; 
        } 
        private long factorial(int number) throws InterruptedException { 
            if (number < 0) { 
                throw new IllegalArgumentException("Number must be greater than zero"); 
            } 
            long result = 1; 
            while (number > 0) { 
                Thread.sleep(1); // adding delay for example 
                result = result * number; 
                number--; 
            } 
            return result; 
        } 
    } 
} 
```
Output
```
Submitting Task ... 
Task is submitted Task is not completed yet.... 
Task is not completed yet.... 
Task is not completed yet.... 
Task is completed, let's check result 
Factorial of 1000000 is : 3628800
```

---

## Q. What is blocking method in Java?
Blocking methods in Java are those methods which block the executing thread until their operation finished. Example of blocking method is `InputStream read()` method which blocks until all data from InputStream has been read completely.

```java
public class BlcokingCallTest {

    public static void main(String args[]) throws FileNotFoundException, IOException  {
      System.out.println("Calling blocking method in Java");
      int input = System.in.read();
      System.out.println("Blocking method is finished");
    }  
}
```
**Examples of blocking methods in Java:**

* **InputStream.read()** which blocks until input data is available, an exception is thrown or end of Stream is detected.
* **ServerSocket.accept()** which listens for incoming socket connection in Java and blocks until a connection is made.
* **InvokeAndWait()** wait until code is executed from Event Dispatcher thread.

## Q. What is atomic variable in Java?
Atomic variables allow us to perform atomic operations on the variables. The most commonly used atomic variable classes in Java are `AtomicInteger`, `AtomicLong`, `AtomicBoolean`, and `AtomicReference`. These classes represent an int, long, boolean and object reference respectively which can be atomically updated. The main methods exposed by these classes are:

*  `get()`: gets the value from the memory, so that changes made by other threads are visible; equivalent to reading a volatile variable
*  `set()`: writes the value to memory, so that the change is visible to other threads; equivalent to writing a volatile variable
*  `lazySet()`: eventually writes the value to memory, may be reordered with subsequent relevant memory operations. One use case is nullifying references, for the sake of garbage collection, which is never going to be accessed again. In this case, better performance is achieved by delaying the null volatile write
*  `compareAndSet()`: same as described in section 3, returns true when it succeeds, else false
*  `weakCompareAndSet()`: same as described in section 3, but weaker in the sense, that it does not create happens-before  orderings. This means that it may not necessarily see updates made to other variables

```java
public class SafeCounterWithoutLock {

    private final AtomicInteger counter = new AtomicInteger(0);
     
    public int getValue() {
        return counter.get();
    }
    public void increment() {
        while(true) {
            int existingValue = getValue();
            int newValue = existingValue + 1;
            if(counter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }
}
```

---

## Q. What is Executors Framework?
The Executor framework helps to **decouple a command submission from command execution**. In the java.util.concurrent package there are three interfaces:

**1. Executor** — Used to submit a new task.

**2. ExecutorService** — A subinterface of Executor that adds methods to manage lifecycle of threads used to run the submitted tasks and methods to produce a Future to get a result from an asynchronous computation.

**3. ScheduledExecutorService** — A subinterface of ExecutorService, to execute commands periodically or after a given delay.

Example: **Java ExecutorService**
```java
ExecutorService executorService = Executors.newFixedThreadPool(10);

executorService.execute(new Runnable() {
    public void run() {
        System.out.println("Asynchronous task");
    }
});

executorService.shutdown();
```
First an ExecutorService is created using the Executors newFixedThreadPool() factory method. This creates a thread pool with 10 threads executing tasks.

Second, an anonymous implementation of the Runnable interface is passed to the execute() method. This causes the Runnable to be executed by one of the threads in the ExecutorService.


---

## Q. What are the available implementations of ExecutorService in the standard library?
The ExecutorService interface has three standard implementations:

* **ThreadPoolExecutor**: for executing tasks using a pool of threads. Once a thread is finished executing the task, it goes back into the pool. If all threads in the pool are busy, then the task has to wait for its turn.
* **ScheduledThreadPoolExecutor**: allows to schedule task execution instead of running it immediately when a thread is available. It can also schedule tasks with fixed rate or fixed delay.
* **ForkJoinPool**: is a special ExecutorService for dealing with recursive algorithms tasks. If you use a regular ThreadPoolExecutor for a recursive algorithm, you will quickly find all your threads are busy waiting for the lower levels of recursion to finish. The ForkJoinPool implements the so-called work-stealing algorithm that allows it to use available threads more efficiently.

## Q. What kind of thread is the Garbage collector thread?
* Daemon thread
## Q. How can we pause the execution of a Thread for specific time?
* **Thread.sleep(...)**: causes the currently executing thread to sleep (cease execution) for the specified number of milliseconds plus the specified number of nanoseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors;
* **Thread.yield()**: causes the currently executing thread object to temporarily pause and allow other threads to execute;
* **Monitors**: you call wait on the object you want to lock and you release the lock by calling notify on the same object.

## Q. What is difference between Executor.submit() and Executer.execute() method?
**execute()**:

* Takes Runnable object as parameter.
* Returns void.
* Useful when the calling thread needs the output from the task executed. Using Future object, you can get result, check whether the task is completed without failure or can request cancelling the task before its completion.
* Example: Delegating a request (for which no response required) to another service, sending an email.

**submit()**:

* Takes Runnable or Callable object as parameter.
* Returns Future object.
* Useful when the calling thread needs the output from the task executed. Using Future object, you can get result, check whether the task is completed without failure or can request cancelling the task before its completion.
* Example: Parallel stream search in Java 8.


---

## Q. What is Phaser in Java concurrency?
The Phaser allows us to build logic in which **threads need to wait on the barrier before going to the next step of execution**. Phaser is similar to other synchronization barrier utils like CountDownLatch and CyclicBarrier.

**CountDownLatch vs CyclicBarrier vs Phaser**

**1. CountDownLatch**:

* Created with a fixed number of threads
* Cannot be reset
* Allows threads to wait(CountDownLatch#await()) or continue with its execution(CountDownLatch#countDown()).

**2. CyclicBarrier**:

* Can be reset.
* Does not a provide a method for the threads to advance. The threads have to wait till all the threads arrive.
* Created with fixed number of threads.

**3. Phaser**:

* Number of threads need not be known at Phaser creation time. They can be added dynamically.
* Can be reset and hence is, reusable.
* Allows threads to wait(Phaser#arriveAndAwaitAdvance()) or continue with its execution(Phaser#arrive()).
* Supports multiple Phases(hence the name phaser).

**PhaserExample.java**
```java
import java.util.concurrent.Phaser;
 
public class PhaserExample
{
    public static void main(String[] args) throws InterruptedException
    {
          Phaser phaser = new Phaser();
          phaser.register();//register self... phaser waiting for 1 party (thread)
          int phasecount = phaser.getPhase();
          
          System.out.println("Phasecount is "+phasecount);
          new PhaserExample().testPhaser(phaser,2000);//phaser waiting for 2 parties
          new PhaserExample().testPhaser(phaser,4000);//phaser waiting for 3 parties
          new PhaserExample().testPhaser(phaser,6000);//phaser waiting for 4 parties
          //now that all threads are initiated, we will de-register main thread 
          //so that the barrier condition of 3 thread arrival is meet.
          phaser.arriveAndDeregister();
                  Thread.sleep(10000);
                  phasecount = phaser.getPhase();
          System.out.println("Phasecount is "+phasecount);
 
    }
 
    private void testPhaser(final Phaser phaser,final int sleepTime) {
        phaser.register();
        new Thread() {
            @Override
            public void run() {
                        try {
                            System.out.println(Thread.currentThread().getName()+" arrived");
                            phaser.arriveAndAwaitAdvance();//threads register arrival to the phaser.
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    System.out.println(Thread.currentThread().getName()+" after passing barrier");
                }
            }.start();
    }
}
```
Output
```
Phasecount is 0
Thread-0 arrived
Thread-2 arrived
Thread-1 arrived
Thread-0 after passing barrier
Thread-1 after passing barrier
Thread-2 after passing barrier
Phasecount is 1
```

---

## Q. How to stop a Thread in Java?
A thread is automatically destroyed when the run() method has completed. But it might be required to kill/stop a thread before it has completed its life cycle. Modern ways to suspend/stop a thread are by using a **boolean flag** and **Thread.interrupt()** method.

Example: Stop a thread Using a boolean variable
```java
/**
* Java program to illustrate 
* stopping a thread using boolean flag 
*
**/
class MyThread extends Thread {

    // Initially setting the flag as true 
    private volatile boolean flag = true;
     
    // This method will set flag as false
    public void stopRunning() {
        flag = false;
    }
     
    @Override
    public void run() {
                 
        // This will make thread continue to run until flag becomes false 
        while (flag) {
            System.out.println("I am running....");
        }
        System.out.println("Stopped Running....");
    }
}
 
public class MainClass {

    public static void main(String[] args) {

        MyThread thread = new MyThread();
        thread.start();
         
        try {
            Thread.sleep(100);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
         
        // call stopRunning() method whenever you want to stop a thread
        thread.stopRunning();
    }   
}
```
Output:
```java
I am running….
I am running….
I am running….
I am running….
I am running….
Stopped Running….
```

Example: Stop a thread Using interrupt() Method
```java
/**
* Java program to illustrate 
* stopping a thread using interrupt() method 
*
**/
class MyThread extends Thread {

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            System.out.println("I am running....");
        }
        System.out.println("Stopped Running.....");
    }
}
 
public class MainClass {

    public static void main(String[] args) {

        MyThread thread = new MyThread(); 
        thread.start();
         
        try {
            Thread.sleep(100);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        // interrupting the thread         
        thread.interrupt();
    }   
}
```
Output:
```java
I am running….
I am running….
I am running….
I am running….
I am running….
Stopped Running….
```


---



## Q. How to implement thread-safe code without using the synchronized keyword?
* **Atomic updates**: A technique in which you call atomic instructions like compare and set provided by the CPU
* **java.util.concurrent.locks.ReentrantLock**: A lock implementation that provides more flexibility than synchronized blocks
* **java.util.concurrent.locks.ReentrantReadWriteLock**: A lock implementation in which reads do not block reads
* **java.util.concurrent.locks.StampedLock** a nonreeantrant Read-Write lock with the possibility of optimistically reading values.
* **java.lang.ThreadLocal**: No need for synchronization if the mutable state is confined to a single thread. This can be done by using local variables or `java.lang.ThreadLocal`.



#### Q. What is difference between ArrayBlockingQueue & LinkedBlockingQueue in Java Concurrency?
#### Q. What is PriorityBlockingQueue in Java Concurrency?
#### Q. What is DelayQueue in Java Concurrency?
#### Q. What is SynchronousQueue in Java?
#### Q. What is Exchanger in Java concurrency?
#### Q. What is Busy Spinning? Why will you use Busy Spinning as wait strategy?
#### Q. What is Multithreading in java?
-	Semaphore vs ThreadPool
-	How Tomcat + Semaphore work together
-	Semaphore vs RateLimiter
-	Implement API rate limiting using Semaphore


---

## Q. ReadWriteLock

**What is ReadWriteLock?**
- A `ReadWriteLock` maintains a pair of associated locks, one for read-only operations and one for writing.
- The **read lock** may be held simultaneously by multiple reader threads, so long as there are no writers.
- The **write lock** is exclusive.

**Why do we need it?**
- In many applications, reads are much more frequent than writes.
- Using a standard `ReentrantLock` or `synchronized` block for both reads and writes is inefficient because it blocks readers from reading concurrently.
- `ReadWriteLock` improves performance by allowing multiple readers to access the resource at the same time.

**Key Rules:**
1. **Multiple Readers**: If no thread holds the write lock, multiple threads can acquire the read lock.
2. **Single Writer**: Only one thread can hold the write lock at a time.
3. **Writer Priority**: If a thread holds the write lock, no other thread can acquire the read lock or the write lock.

**Implementation:**
- `ReentrantReadWriteLock` is the standard implementation of `ReadWriteLock`.

**Example:**
```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class SharedData {
    private int data = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void write(int value) {
        lock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " writing: " + value);
            data = value;
            Thread.sleep(1000); // Simulate write operation
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void read() {
        lock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " reading: " + data);
            Thread.sleep(500); // Simulate read operation
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
    }
}

public class ReadWriteLockDemo {
    public static void main(String[] args) {
        SharedData sharedData = new SharedData();

        // Create writer thread
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                sharedData.write(i);
            }
        }, "Writer");

        // Create reader threads
        Runnable readerTask = () -> {
            for (int i = 0; i < 3; i++) {
                sharedData.read();
            }
        };

        Thread reader1 = new Thread(readerTask, "Reader-1");
        Thread reader2 = new Thread(readerTask, "Reader-2");

        writer.start();
        reader1.start();
        reader2.start();
    }
}
```

**Custom Implementation of ReadWriteLock**
```java
public class SimpleReadWriteLock {
    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;

    public synchronized void lockRead() throws InterruptedException {
        while (writers > 0 || writeRequests > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        writeRequests++;
        while (readers > 0 || writers > 0) {
            wait();
        }
        writeRequests--;
        writers++;
    }

    public synchronized void unlockWrite() {
        writers--;
        notifyAll();
    }
}
```
**Explanation of Custom Implementation:**
1. **Readers Count**: Tracks the number of active readers.
2. **Writers Count**: Tracks the number of active writers (0 or 1).
3. **Write Requests**: Tracks the number of threads waiting to write. This is crucial to prevent **writer starvation**. If we didn't track requests, a constant stream of readers could keep `readers > 0` forever, preventing a writer from ever acquiring the lock.
4. **lockRead()**: Waits if there are any writers or write requests. This gives priority to writers.
5. **lockWrite()**: Waits if there are any readers or writers.

**Difference between ReentrantReadWriteLock and Synchronized**

| Feature | ReentrantReadWriteLock | Synchronized |
| :--- | :--- | :--- |
| **Concurrency** | Allows multiple concurrent readers. | Allows only one thread (read or write) at a time. |
| **Performance** | Better for read-heavy workloads. | Better for write-heavy or simple workloads. |
| **Fairness** | Supports fair and non-fair ordering. | Non-fair only. |
| **Locking** | Explicit lock/unlock. | Implicit block-based locking. |
| **Condition Support** | Supports multiple Condition objects. | Supports only one wait-set (wait/notify). |

---


## Synchronize blocks with synchronized(this) and synchronized(obj)
```java
class BankAccount {
    private double balance = 0.0;
    private List<String> statement = new ArrayList<>();

    // separate lock for statement operations
    private final Object statementLock = new Object();  // THIS IS BASICALLY USED AS TOKEN, NOT AS OBJECT, SO THAT WE CAN ACQUIRE DIFFRENT LOCKS ON DIFF OPERATIONS
    // Think of it as a key that controls access to some critical section

    // synchronized block on 'this' for balance operations
    public void deposit(double amount) {
        synchronized(this) {
            balance += amount;
            statement.add("Deposited: " + amount);
            System.out.println(Thread.currentThread().getName() + " deposited " + amount + ", balance=" + balance);
            try { Thread.sleep(1000); } catch(Exception e) {}
        }
    }

    public void withdraw(double amount) {
        synchronized(this) {
            if(balance >= amount) {
                balance -= amount;
                statement.add("Withdrew: " + amount);
                System.out.println(Thread.currentThread().getName() + " withdrew " + amount + ", balance=" + balance);
            } else {
                System.out.println(Thread.currentThread().getName() + " insufficient funds!");
            }
            try { Thread.sleep(1000); } catch(Exception e) {}
        }
    }

    // synchronized block on statementLock for printing, independent of balance lock
    public void printStatement() {
        synchronized(statementLock) {
            System.out.println(Thread.currentThread().getName() + " printing statement:");
            for(String s : statement) {
                System.out.println("  " + s);
            }
            try { Thread.sleep(500); } catch(Exception e) {}
        }
    }
}

public class BankDemo {
    public static void main(String[] args) {
        BankAccount account = new BankAccount();

        // Multiple threads performing deposits and withdrawals
        Thread t1 = new Thread(() -> account.deposit(100), "T1");
        Thread t2 = new Thread(() -> account.withdraw(50), "T2");
        Thread t3 = new Thread(() -> account.deposit(200), "T3");
        Thread t4 = new Thread(() -> account.printStatement(), "T4");
        Thread t5 = new Thread(() -> account.printStatement(), "T5");

        // Start threads
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
    }
}
```
```
1.	deposit() and withdraw() are synchronized(this) → they cannot run concurrently on the same account, protecting balance from race conditions.
2.	printStatement() uses a different lock (statementLock) → multiple print operations or print vs deposit/withdraw can run concurrently without waiting.

	•	synchronized(this) → locks the account itself (like locking the safe for deposits/withdrawals)
	•	synchronized(statementLock) → locks the statement book (like someone reading the ledger)
	•	Two people can read the ledger at the same time, but nobody can change the balance while another is changing it
```
1.	synchronized on a method is exactly like wrapping the entire method in synchronized(this).
2.	Lock object: this (the current instance)
3.	Any other thread trying to call any other synchronized method on the same object will be blocked until this method finishes

When to use a synchronized block instead of a synchronized method?
1.	Partial locking: Only part of the method needs locking, e.g., update balance but not logging
2.	Different locks: You want to synchronize on a different object (synchronized(obj)) instead of this

----
Multiple object locks
```java
class BankAccount {
    private double balance;
    private List<String> history = new ArrayList<>();

    private final Object balanceLock = new Object();
    private final Object historyLock = new Object();

    public void deposit(double amount) {
        synchronized (balanceLock) {
            balance += amount;
        }
        synchronized (historyLock) {
            history.add("Deposit " + amount);
        }
    }

    public double getBalance() {
        synchronized (balanceLock) {
            return balance;
        }
    }

    public List<String> getStatement() {
        synchronized (historyLock) {
            return new ArrayList<>(history);
        }
    }
}
```
Intution -
- For the same BankAccount object, if:
    - Thread A is executing deposit() inside synchronized(balanceLock)
    - Thread B calls getBalance() which also uses synchronized(balanceLock)
    - Thread B will block until Thread A exits the balanceLock block.
    - during this time, Thread C can getStatement because historyLock is free.

- This is where multiple ReentrantLock can be connected with synchronized object lock.
```java
class BankAccount {

    private double balance;
    private List<String> history = new ArrayList<>();

    private final ReentrantLock balanceLock = new ReentrantLock();
    private final ReentrantLock historyLock = new ReentrantLock();

    public void deposit(double amount) {
        balanceLock.lock();
        try {
            balance += amount;
        } finally {
            balanceLock.unlock();
        }

        historyLock.lock();
        try {
            history.add("Deposit " + amount);
        } finally {
            historyLock.unlock();
        }
    }

    public double getBalance() {
        balanceLock.lock();
        try {
            return balance;
        } finally {
            balanceLock.unlock();
        }
    }

    public List<String> getStatement() {
        historyLock.lock();
        try {
            return new ArrayList<>(history);
        } finally {
            historyLock.unlock();
        }
    }
}
```


## Livelock
```markdown
Excellent topic — livelock is subtle and much harder than deadlock.
I’ll explain it in three layers: intuition → code → prevention.

⸻

1️⃣ What is a Livelock? (clear definition)

Livelock happens when:

	•	Threads are NOT blocked
	•	Threads keep running
	•	But they keep reacting to each other
	•	And no progress is made

💡 Threads are polite, not stuck.

⸻

Deadlock vs Livelock (one-liner)

Deadlock	Livelock
Threads stuck	Threads busy
No CPU usage	High CPU usage
Easy to detect	Hard to detect
BLOCKED state	RUNNABLE state


⸻

2️⃣ Real-world analogy (important)

Two people in a hallway
	•	Person A moves left → Person B moves right
	•	Both notice → both switch sides
	•	Repeat forever

They are active, but never pass.

That is livelock.

⸻

3️⃣ Code: Livelock using ReentrantLock (classic)

⚠️ Bad code (livelock)

import java.util.concurrent.locks.ReentrantLock;

class LivelockDemo {

    static ReentrantLock lockA = new ReentrantLock();
    static ReentrantLock lockB = new ReentrantLock();

    static class Worker implements Runnable {
        private final String name;
        private final ReentrantLock first;
        private final ReentrantLock second;

        Worker(String name, ReentrantLock first, ReentrantLock second) {
            this.name = name;
            this.first = first;
            this.second = second;
        }

        public void run() {
            while (true) {
                try {
                    if (first.tryLock()) {
                        System.out.println(name + " acquired first lock");

                        if (second.tryLock()) {
                            System.out.println(name + " acquired second lock");
                            break; // work done
                        } else {
                            System.out.println(name + " released first lock to be polite");
                            first.unlock();
                        }
                    }
                    Thread.sleep(50); // avoid tight loop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Worker("Thread-1", lockA, lockB)).start();
        new Thread(new Worker("Thread-2", lockB, lockA)).start();
    }
}


⸻

What happens here?
	1.	Thread-1 gets lockA
	2.	Thread-2 gets lockB
	3.	Both fail to get second lock
	4.	Both release first lock
	5.	Both retry
	6.	Repeat forever

💥 No blocking
💥 No progress
💥 CPU burning

⸻

4️⃣ How to DETECT livelock

Symptoms
	•	High CPU
	•	No progress
	•	Threads in RUNNABLE
	•	Logs repeating same pattern

JVM tools
	•	jstack → threads not blocked
	•	Thread dumps show spinning

⸻

5️⃣ How to PREVENT livelock (real fixes)

⸻

✅ Fix 1: Global lock ordering (best)

lockA.lock();
try {
    lockB.lock();
    try {
        // work
    } finally {
        lockB.unlock();
    }
} finally {
    lockA.unlock();
}

✔ No livelock
✔ No deadlock
✔ Simple

⸻

✅ Fix 2: Backoff strategy (important)

if (first.tryLock()) {
    try {
        if (second.tryLock()) {
            break;
        }
    } finally {
        first.unlock();
    }
}

Thread.sleep(random(50, 150)); // randomness breaks symmetry

Used in:
	•	Databases
	•	Distributed locks
	•	Kafka clients

⸻

✅ Fix 3: Timeout + give up

if (first.tryLock(500, TimeUnit.MILLISECONDS)) {
    try {
        if (second.tryLock(500, TimeUnit.MILLISECONDS)) {
            break;
        }
    } finally {
        first.unlock();
    }
}

✔ Avoids infinite retries

⸻

✅ Fix 4: Reduce shared locks (best architectural fix)
	•	Use immutable data
	•	Message passing (Kafka)
	•	Actor model
	•	Partition data

This is what high-scale systems do

⸻

6️⃣ Why synchronized rarely causes livelock

synchronized:
	•	No tryLock
	•	No polite release
	•	Threads block

So:
	•	Deadlock possible
	•	Livelock rare

Livelock is a ReentrantLock problem, not a synchronized one.

⸻

7️⃣ Why payment gateways & Kafka avoid this

They:
	•	Avoid mutual locks
	•	Use queues
	•	Use CAS
	•	Use partition ownership

Because:

“Threads talking to each other cause chaos.
Threads working independently scale.”

⸻

8️⃣ One-sentence takeaway (remember this)

Deadlock = nobody moves
Livelock = everyone moves but nobody progresses

⸻

If you want next:
	•	Livelock using synchronized (rare but possible)
	•	Kafka’s cooperative rebalancing (livelock prevention)
	•	How databases avoid livelock
	•	ExecutorService + livelock scenario

Just tell me.
```
```markdown
Excellent example. This is classic real-world concurrency code, and understanding it deeply means you truly understand locks.

I’ll explain this step-by-step, thread-by-thread, and line-by-line, then connect it to deadlock, livelock, fairness, and banking reality.

⸻

1️⃣ What problem is this code solving?

You have two shared BankAccount objects:

BankAccount from;
BankAccount to;

Multiple threads (users) may try to transfer money at the same time, including:
	•	User A: transfer from A → B
	•	User B: transfer from B → A (same time!)

Without locking:
	•	Balances can corrupt
	•	Money can be lost or duplicated

So we need mutual exclusion on both accounts.

⸻

2️⃣ Why EACH BankAccount has its own ReentrantLock

class BankAccount {
    final ReentrantLock lock = new ReentrantLock();
    double balance;
}

Each account owns its own lock.

✔ Lock protects only that account’s balance
✔ Fine-grained locking (better concurrency)
✔ Same idea as DB row-level lock

⸻

3️⃣ Why transfer() needs TWO locks

from.balance -= amount;
to.balance += amount;

This must be:
	•	Atomic
	•	Consistent
	•	Isolated

So:
	•	from must not change while debiting
	•	to must not change while crediting

➡️ Both locks are required

⸻

4️⃣ The core idea: tryLock + retry loop

while (true) {

This loop exists because:
	•	tryLock() does not wait
	•	It either succeeds or fails immediately

So if we fail, we retry politely.

⸻

5️⃣ Step-by-step execution (IMPORTANT)

Scenario:

Two threads:

Thread	Transfer
T1	A → B
T2	B → A


⸻

Step 1: Thread T1 runs

if (from.lock.tryLock()) {

T1:
	•	Locks account A successfully

⸻

Step 2: Before T1 locks B…

Thread T2 starts:

if (from.lock.tryLock()) {

T2:
	•	Locks account B successfully

Now situation:

Account	Locked by
A	T1
B	T2

⚠️ This is where deadlock would happen with lock().

⸻

Step 3: Second lock attempt

T1 tries:

if (to.lock.tryLock()) { // tries B

Fails → B is locked by T2

T2 tries:

if (to.lock.tryLock()) { // tries A

Fails → A is locked by T1

⸻

Step 4: Deadlock AVOIDED

Because of tryLock():

finally {
    from.lock.unlock();
}

✔ Both threads release their first lock
✔ No thread blocks forever
✔ No deadlock

⸻

Step 5: Polite retry

Thread.sleep(50);

This avoids:
	•	CPU spinning
	•	Livelock starvation

Threads back off briefly, then retry.

⸻

Step 6: One thread wins

Eventually:
	•	T1 locks A
	•	T1 locks B
	•	Transfer completes
	•	Locks released

System.out.println("Transfer successful");
break;

Loop exits safely.

⸻

6️⃣ Why finally blocks are CRITICAL

finally {
    to.lock.unlock();
}

and

finally {
    from.lock.unlock();
}

Even if:
	•	Exception occurs
	•	Thread is interrupted

➡️ Locks are always released

Without finally:
	•	Permanent lock leak
	•	System freeze

⸻

7️⃣ Why NOT use lock() here?

If you wrote:

from.lock.lock();
to.lock.lock();

You would get:

DEADLOCK

T1 holds A → waits for B
T2 holds B → waits for A

Nobody releases.

⸻

8️⃣ Is this deadlock-free?

✔ Yes
But…

⸻

9️⃣ Is this livelock-free?

⚠️ Not guaranteed

Both threads could:
	•	Repeatedly acquire first lock
	•	Release
	•	Retry
	•	Repeat forever

This is livelock.

You partially mitigate it with:

Thread.sleep(50);

But production systems often add:
	•	Random backoff
	•	Lock ordering
	•	Timeouts

⸻

🔟 How banks REALLY fix this

Option 1: Lock ordering

Always lock smaller account ID first.

Option 2: Single transaction lock

Database transaction isolation

Option 3: Distributed lock

Redis / Zookeeper

⸻

1️⃣1️⃣ Why this is REAL-WORLD CORRECT CODE

✔ Fine-grained locking
✔ Deadlock avoidance
✔ Safe retry
✔ Shared mutable state handled correctly
✔ Mirrors DB row-locks

⸻

1️⃣2️⃣ One-line summary (memorize this)

This code avoids deadlock by using non-blocking tryLock(), releasing partial locks, and retrying until both account locks are acquired atomically.

⸻

If you want next:
	•	Convert this to synchronized
	•	Add lock ordering
	•	Demonstrate livelock
	•	Compare with database transactions

Just tell me.
```

```markdown
## how to make it livelock-free

Excellent question. This is the exact next step after understanding deadlock.

You already avoided deadlock.
Now we’ll make the code livelock-free.

I’ll explain:
1.	Why livelock happens in your code
2.	3 proven strategies to fix it
3.	Banking-grade solution (recommended)
4.	Final livelock-free code
5.	Mental model to remember forever

⸻

1️⃣ Why livelock happens in your current code

Your current logic:

while (true) {
if (from.lock.tryLock()) {
try {
if (to.lock.tryLock()) {
try {
transfer();
break;
} finally {
to.lock.unlock();
}
}
} finally {
from.lock.unlock();
}
}
Thread.sleep(50);
}

Livelock scenario (very important)

Thread	Step
T1	locks A
T2	locks B
T1	fails to lock B → releases A
T2	fails to lock A → releases B
BOTH	sleep
BOTH	retry at same time
🔁	repeat forever

They are active, polite, cooperative… but no progress.

That is livelock.

⸻

2️⃣ Strategy #1 (BEST): Lock Ordering (Banking-grade)

Core idea

Always acquire locks in the same global order

For bank accounts:
•	Order by accountId
•	Smaller ID first, larger ID second

Why this works

All threads:
•	Lock accounts in same order
•	No circular dependency possible
•	No deadlock
•	No livelock

⸻

3️⃣ Livelock-free Transfer (RECOMMENDED)

BankAccount with ID

class BankAccount {
final int id;
final ReentrantLock lock = new ReentrantLock();
double balance;

    BankAccount(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }
}


⸻

Livelock-free transfer using lock ordering

class TransferService {

    static void transfer(BankAccount a, BankAccount b, double amount) {

        BankAccount first = a.id < b.id ? a : b;
        BankAccount second = a.id < b.id ? b : a;

        first.lock.lock();
        try {
            second.lock.lock();
            try {
                a.balance -= amount;
                b.balance += amount;
                System.out.println("Transfer successful");
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }
}

✔ Why this is PERFECT
•	No retry loop
•	No sleep
•	No livelock
•	No deadlock
•	Maximum throughput

This is how real banks do it internally.

⸻

4️⃣ Strategy #2: Random Backoff (Good but not perfect)

If ordering is impossible:

Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));

✔ Reduces livelock
❌ Still probabilistic
❌ Not banking-grade

⸻

5️⃣ Strategy #3: tryLock with timeout (Better fallback)

if (from.lock.tryLock(100, TimeUnit.MILLISECONDS)) {
try {
if (to.lock.tryLock(100, TimeUnit.MILLISECONDS)) {
try {
transfer();
return;
} finally {
to.lock.unlock();
}
}
} finally {
from.lock.unlock();
}
}

✔ Bounded waiting
✔ Avoids infinite retry
❌ Slight overhead

⸻

6️⃣ Which strategy should YOU use?

Scenario	Best Choice
Banking / payments	Lock ordering
Legacy systems	tryLock with timeout
Experiments	Random backoff


⸻

7️⃣ Key difference (VERY IMPORTANT)

Problem	Solution
Deadlock	tryLock / ordering
Livelock	ordering or backoff
Starvation	fair locks


⸻

8️⃣ One-line rule to remember forever

Deadlock is caused by waiting; livelock is caused by being too polite.
Lock ordering fixes both.

⸻

If you want next:
•	Visual thread timeline
•	Convert this to synchronized
•	Compare with database transaction isolation
•	Show livelock in real execution

Just say the word.

```