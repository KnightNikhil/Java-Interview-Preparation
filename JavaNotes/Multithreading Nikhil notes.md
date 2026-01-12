- In mulithreading we basically add tasks into threads which can be run simultaneouly.
- You cannot directly “run a method in multithreading”.
- You must wrap the method call inside a task (Runnable / Callable) and execute that task on a thread.
- and thats why we need to create a class which extens the thread class and hold a run method in which we write the code we want to execute i.e. our actual task.
- if we already have a method, which I want to execute in multithreading, than I need to wrap it in `Thread emailing = Thread as new Thread(() -> sendEmail())`
- there another way to do so, implement runnable interface 
```java
Runnable emailing = new Runnable(
        public void run(){
            sendEmail();
        })
new Thread(emailing);
```
The idea is, we basically need to get our operation inside the run method. Either we can o it while creating class, or while assigning the task to threads.
- We can also use 
```java
ExecutorService executor = Executors.newFixedThreadPool(2);
Runnable task = () -> sendEmail());
executor.submit(task);
```

**Mental Model--**
1. Threads do not run methods.
2. Threads run tasks.
3. Tasks call methods.
4. A task is an execution recipe. (Place order, make payment, send confirmation -- need to be done synchronously for ordering, transactional consistency, or context propagation)
5. It can contain 1 method or 100 methods. (These methods will be synchronous in a task, based on the sequence they are called in run() )
6. Thread just follows the recipe. (which implies that one task will be executed in one thread, the methods in that tasks will behave synchronously)
