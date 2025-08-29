**Multithreading Hands-on Exercises**

**Beginner**
1.	Create and run a Thread by extending Thread class
2.	Create and run a Thread by implementing Runnable
3.	Use .join() to wait for a thread to finish
4.	Print thread’s current name and id
5.	Start multiple threads that print numbers in parallel
6.	Synchronize a method to protect a shared variable
7.	Demo race condition using two unsynchronized threads
8.	Safely increment a shared counter using synchronized keyword
9.	Print numbers 1-10 from two threads (even/odd) alternately
10.	Create a thread with sleep delay and observe order of output

**Intermediate**
11.	Create a Producer-Consumer using wait()/notify()
12.	Synchronize blocks with synchronized(this) and synchronized(obj)
13.	Use ExecutorService to submit and manage tasks
14.	Implement Callable with Future to retrieve result from a thread
15.	Use Semaphore to limit number of concurrent threads
16.	Use ReentrantLock for mutual exclusion
17.	Create a thread pool with fixed size using Executors
18.	Schedule tasks for future execution using ScheduledExecutorService
19.	Implement Deadlock with two or more threads
20.	Implement ReadWriteLock for concurrent read and exclusive write

**Advanced**
21.	Use CyclicBarrier to coordinate multiple threads
22.	Use CountDownLatch to make one thread wait for others
23.	Implement a shared resource protected with Atomic variables
24.	Detect and prevent livelock scenario
25.	Create a thread-safe singleton with Double-Checked Locking
26.	Implement custom thread-safe data structure
27.	Using ThreadLocal to store thread-specific data
28.	Demo priority inversion and how to solve it
29.	Profile and debug a multithreaded Java application
30.	Implement Multithreaded Map-Reduce using ExecutorService