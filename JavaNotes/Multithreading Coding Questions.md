**Multithreading Hands-on Exercises**

**Beginner**
1.	Create and run a Thread by extending Thread class
```java
public class Cooking extends Thread{
    public String dish;
    public Cooking(String dish){
        this.dish = dish;
    }
    public run(){
        System.out.println("Cooking"+dish);
    }
}

Thread t1 = new Cooking("Pasta");
t.start();
```

2.	Create and run a Thread by implementing Runnable

```java
Runnable t1 = new Runnable("Pasta") {
    @Override
    public void run() {
        System.out.println("Cooking" + dish);
    }
};

Thread t = new Thread(t1);
t.start();
```

3.	Use .join() to wait for a thread to finish
```java
public static class Cooking extends Thread{
        private String dish;
        private int time;

        public Cooking(String dish, int time){
            this.dish = dish;
            this.time = time;
        }

        public void run(){
            System.out.println("Cooking "+ dish);
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Cooking done for "+ dish);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Cooking cookingPasta = new Cooking("Pasta", 5000);
        cookingPasta.start();

        Cooking cookingRice = new Cooking("Rice", 2000);
        cookingRice.start();

        cookingPasta.join();
        cookingRice.join();

        System.out.println("Start plating");

    }
```
- Had I not used .join, the plating would have started before the dish was made.

4.	Print thread’s current name 
```java
cookingRice.setName("Rice Thread");
System.out.println("Rice thread - "+ cookingPasta.getName());
```
5.	Start multiple threads that print numbers in parallel
```java
 public static void main(String[] args) {
    Thread t1 = new Thread(() -> printNum());
    Thread t2 = new Thread(() -> printNum());

    t1.start();
    t2.start();
}

private static void printNum() {
    for(int i=0; i<10; i++)
        System.out.println(i + " "+ Thread.currentThread().getName());
}
```
```
0 Thread-1
1 Thread-1
2 Thread-1
0 Thread-0
3 Thread-1
1 Thread-0
4 Thread-1
2 Thread-0
5 Thread-1
3 Thread-0
6 Thread-1
4 Thread-0
7 Thread-1
5 Thread-0
8 Thread-1
6 Thread-0
9 Thread-1
7 Thread-0
8 Thread-0
9 Thread-0
```
6.	Synchronize a method to protect a shared variable
```java
public static class Counter {
    private int count =0;

    public synchronized void increment() {
        for(int i=0; i<10; i++){
            count++;
            System.out.println("Count: " + getCount() + " Thread: " + Thread.currentThread().getName());
        }
    }
    public int getCount() {
        return count;
    }
}

public static void main(String[] args) throws InterruptedException {
    Counter counter = new Counter();
    System.out.println(counter.getCount());

    Thread t1 = new Thread(counter::increment);
    Thread t2 = new Thread(counter::increment);

    t1.setName("Counter 1 object");
    t2.setName("Counter 2 object");

    t1.start();
    t2.start();

}
```
```
0
Count: 1 Thread: Counter 1 object
Count: 2 Thread: Counter 1 object
Count: 3 Thread: Counter 1 object
Count: 4 Thread: Counter 1 object
Count: 5 Thread: Counter 1 object
Count: 6 Thread: Counter 1 object
Count: 7 Thread: Counter 1 object
Count: 8 Thread: Counter 1 object
Count: 9 Thread: Counter 1 object
Count: 10 Thread: Counter 1 object
Count: 11 Thread: Counter 2 object
Count: 12 Thread: Counter 2 object
Count: 13 Thread: Counter 2 object
Count: 14 Thread: Counter 2 object
Count: 15 Thread: Counter 2 object
Count: 16 Thread: Counter 2 object
Count: 17 Thread: Counter 2 object
Count: 18 Thread: Counter 2 object
Count: 19 Thread: Counter 2 object
Count: 20 Thread: Counter 2 object
```

-- `synchronized` keyword will make the thread 2 wait until first one complete its task.

-- What if I have used 2 object of the same class?
-- The counter will increase to 10 for each object
```java
public static void main(String[] args) throws InterruptedException {
        Counter counter1 = new Counter();
        Counter counter2 = new Counter();
        System.out.println(counter1.getCount());
        System.out.println(counter2.getCount());

        Thread t1 = new Thread(counter1::increment);
        t1.setName("Counter 1 object");
        Thread t2 = new Thread(counter2::increment);
        t2.setName("Counter 2 object");


        t1.start();
        t2.start();

    }
```
```
0
0
Count: 1 Thread: Counter 1 object
Count: 2 Thread: Counter 1 object
Count: 3 Thread: Counter 1 object
Count: 4 Thread: Counter 1 object
Count: 5 Thread: Counter 1 object
Count: 6 Thread: Counter 1 object
Count: 7 Thread: Counter 1 object
Count: 8 Thread: Counter 1 object
Count: 9 Thread: Counter 1 object
Count: 10 Thread: Counter 1 object
Count: 1 Thread: Counter 2 object
Count: 2 Thread: Counter 2 object
Count: 3 Thread: Counter 2 object
Count: 4 Thread: Counter 2 object
Count: 5 Thread: Counter 2 object
Count: 6 Thread: Counter 2 object
Count: 7 Thread: Counter 2 object
Count: 8 Thread: Counter 2 object
Count: 9 Thread: Counter 2 object
Count: 10 Thread: Counter 2 object
```

7.	Demo race condition using two unsynchronized threads
```java
      public static class Counter {
      private int count =0;

      public void increment() {
      count++;
      System.out.println("Count: " + getCount() + " Thread: " + Thread.currentThread().getName());
      }
      public int getCount() {
      return count;
      }
      }

    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        System.out.println(counter.getCount());

        Thread t1 = new Thread(() -> {
            for(int i=0; i<100000; i++)
                counter.increment();
        });
        Thread t2 = new Thread(() -> {
            for(int i=0; i<100000; i++)
                counter.increment();
        });

        t1.setName("Counter 1 object");
        t2.setName("Counter 2 object");

        t1.start();
        t2.start();

    }
```
```text
output is not 200000 it is less than expected, since there will be conditions when 2 threads must have read the same value off counter before it was updated previously
```
8.	Print numbers 1-10 from two threads (even/odd) alternately
```java

```
9.	Create a thread with sleep delay and observe order of output

10.	

**Intermediate**
11.	Create a Producer-Consumer using wait()/notify()
```java
public static class Helper {

        Queue<String> messages = new LinkedList<>();
        int MAX_CAPACITY = 5;

        public synchronized void produceMessage() {
            try{
                /* this condition should be checked in "while" and not "if",
                so that when the thread reacquires the lock, it will recheck the condition
                because a thread can reacquire the lock even without th notify() being called on that thread by JVM
                and if satisfied than need to wait again */
                while (messages.size() == MAX_CAPACITY){
                    wait();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try{
                Thread.sleep(2000); // JUST TO READ BEHAVIOR IN REALTIME
                System.out.println("Message produced");
                messages.add("Message");
                notifyAll();
                /* used to notify all the threads to wake up
                 i.e. reacquire the lock and check the condition again,
                 bcoz notify() might not wake up the thread working on producer,
                 it might wake up another consumer thread, which is of no use to us
                 */

            }catch (Exception e){
                throw new RuntimeException(e);
            }

        }

        public synchronized void consumeMessage() {
            try{
                /* this condition should be checked in "while" and not "if",
                so that when the thread reacquires the lock, it will recheck the condition
                because a thread can reacquire the lock even without th notify() being called on that thread by JVM called spurious wakeups */
                while (messages.isEmpty()){
                    wait();
                    /* if thread need to wait, it will go to sleep, release the lock on method, so some other thread can try to execute the method
                    once notify(), it will reacquire the lock, and start executing from the next line of code itself,
                    */
                    System.out.println("producer wait over");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try{
                Thread.sleep(2000); // JUST TO READ BEHAVIOR IN REALTIME
                System.out.println("Message consumed");
                messages.remove();
                notify();
            }catch (Exception e){
                throw new RuntimeException(e);
            }

        }
    }



    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();

        Thread t1 = new Thread(() -> {
            while (true) // TO PRODUCE ENDLESS MESSAGES
                helper.produceMessage();

        });

        Thread t2 = new Thread(() ->{
            while (true) // TO CONSUME ENDLESS MESSAGES
                helper.consumeMessage();}
        );

        t2.start();
        System.out.println("t2 started");
        Thread.sleep(3000); //JUST TO MAKE SURE THAT CONSUMER RUNS BEFORE PRODUCER (FOR UNDERSTANDING PURPOSE ONLY)
        t1.start();
        System.out.println("t1 started");
    }
```
12.	Synchronize blocks with synchronized(this) and synchronized(obj)
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

---
13.	Use ExecutorService to submit and manage tasks
```java
public class ExecutorBasic {

    public static void main(String[] args) {

        // 1. Create executor with fixed number of threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // 2. Submit tasks
        for (int i = 1; i <= 6; i++) {
            int taskId = i;

            executor.submit(() -> {
                System.out.println(
                        "Task " + taskId + " running on " + Thread.currentThread().getName()
                );
                try {
                    Thread.sleep(2000); // simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 3. Shutdown executor
        executor.shutdown();
    }
}
```
Timeline-
```text
Time 0s:
  Threads: T1, T2, T3
  Tasks running: 1, 2, 3
  Tasks waiting: 4, 5, 6

Time 2s:
  Threads finish 1, 2, 3
  Threads pick next tasks: 4, 5, 6

Time 4s:
  All tasks complete
```

14.	Implement Callable with Future to retrieve result from a thread
```java
ExecutorService executor = Executors.newSingleThreadExecutor();

        // OPTION 1 :::
        Callable<Integer> task = () -> {
            System.out.println(Thread.currentThread().getName() + " computing...");
            Thread.sleep(2000);
            return 42;
        };

        Future<Integer> future = executor.submit(task);

        // OPTION 2 ::: ExecutorService.submit() is overloaded : <T> Future<T> submit(Callable<T> task)
        // “Callable can be submitted inline as a lambda to ExecutorService.
        // Java infers it based on return type, and submit returns a Future that represents the eventual result.”
        Future<Integer> future =
                executor.submit(() -> {
                    Thread.sleep(1000);
                    return 42;
                });
        
        System.out.println("Main thread doing other work...");

        Integer result = future.get(); // blocks
        System.out.println("Result = " + result);

        executor.shutdown();
```
-If the Lambda expression is returning something, it will treat it as Callable else Runnable  


15.	Use Semaphore to limit number of concurrent threads

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
16.	
17.	
18.	Schedule tasks for future execution using ScheduledExecutorService
```java
ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        // payment fails, so added a scheduled thread after 5 seconds to try again
        scheduledExecutorService.schedule(() -> System.out.println("retrying payment"), 5, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
```
```
T0  → Payment failed
T0+5s → Retry payment task executes
```

19.	Implement Deadlock with two or more threads


20.	Implement ReadWriteLock for concurrent read and exclusive write
```java
public static class Helper {

        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock readLock = lock.readLock();
        Lock writeLock = lock.writeLock();


        public void getBalance() {
            readLock.lock();
            //performOperations
            readLock.unlock();
        }

        public void deposit() {
            writeLock.lock();
            //perform operation
            writeLock.unlock();
        }
    }
```

**Advanced**
21.	Use CountDownLatch to coordinate multiple threads
```java
public static class Helper {

        public void method1(CountDownLatch countDownLatch)  {
            try{
                Thread.sleep(1000);
                System.out.println("t1 done");
                countDownLatch.countDown();
                System.out.println("post countdown, t1");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void method2(CountDownLatch countDownLatch)  {
            try{
                Thread.sleep(2000);
                System.out.println("t2 done");
                countDownLatch.countDown();
                System.out.println("post countdown, t2");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
    
    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();

        CountDownLatch countDownLatch = new CountDownLatch(2);
        // size can not be more than the number of time countDownLatch.countDown() is called, else will wait forever as the count need to be zero before calling await()

        Thread t1 = new Thread(() -> helper.method1(countDownLatch));
        Thread t2 = new Thread(() -> helper.method2(countDownLatch));

        t1.start();
        t2.start();

        countDownLatch.await();

        System.out.println("Wait complete");
    }
```
```
t1 done
post countdown, t1
t2 done
post countdown, t2
Wait complete
```

22.	Use CyclicBarrier to make one thread wait for others
```java
public static class Helper {

        public void method1(CyclicBarrier cyclicBarrier)  {
            try{
                Thread.sleep(1000);
                System.out.println("t1 done");
                cyclicBarrier.await();
                System.out.println("post barrier wait, t1");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        public void method2(CyclicBarrier cyclicBarrier)  {
            try{
                Thread.sleep(2000);
                System.out.println("t2 done");
                cyclicBarrier.await();
                System.out.println("post barrier wait, t2");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Helper helper = new Helper();

        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        Thread t1 = new Thread(() -> helper.method1(cyclicBarrier));
        Thread t2 = new Thread(() -> helper.method2(cyclicBarrier));

        t1.start();
        t2.start();

        System.out.println("Wait complete");
    }
```
```
Wait complete
t1 done
t2 done
post barrier wait, t2
post barrier wait, t1
```
23.	Implement a shared resource protected with Atomic variables
```java
AtomicInteger counter = new AtomicInteger(0);

public void increment(){
    counter.incrementAndGet();
    System.out.println(counter);
}
```
24.	

25.	Create a thread-safe singleton 
```java
public class Singleton {

    // MUST be volatile (critical)
    private static volatile Singleton instance;

    // private constructor prevents external instantiation
    private Singleton() {
    }

    public static Singleton getInstance() {

        // First check (no locking)
        if (instance == null) {

            synchronized (Singleton.class) {

                // Second check (with lock)
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }

        return instance;
    }
}
```
#### Better alternatives (if allowed):
1. Enum Singleton (BEST) - Enum Singleton (BEST)
```java
public enum Singleton {
    INSTANCE;

    public void doSomething() {
        System.out.println("Doing work");
    }
}
```
When the JVM loads this:
```java
enum Singleton {
    INSTANCE
}
```
It internally converts it to something like:
```java
final class Singleton extends Enum<Singleton> {

    public static final Singleton INSTANCE;

    static {
        INSTANCE = new Singleton("INSTANCE", 0);
    }

    private Singleton(String name, int ordinal) {
        super(name, ordinal);
    }
}
```

2. Initialization-on-Demand Holder (cleanest)
```java
public class Singleton {

    private Singleton() {}

    private static class Holder {
        private static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

26.	Implement custom thread-safe data structure
27.	Using ThreadLocal to store thread-specific data
28.	Demo priority inversion and how to solve it
29.	Profile and debug a multithreaded Java application
30.	Implement Multithreaded Map-Reduce using ExecutorService