## Table of Contents

1. [@Cacheable](#@cacheable)
2. [Scheduled Tasks / Cron Jobs in Java](#scheduled-tasks--cron-jobs-in-java)

## @Cacheable
The `@Cacheable` annotation in Spring is used to **automatically cache the result of a method** so that future requests with the same parameters return the cached value instead of executing the method again. This feature is designed for improving application performance, especially for expensive or frequently called methods[1][3][5][7].

### How @Cacheable Works

- When a method annotated with `@Cacheable` is called, Spring checks if the result for the given input is already in the cache.
- If the cache contains the value, Spring returns it directly and **skips method execution**.
- If the value is not cached, the method executes, and the return value is stored in the specified cache for future use.
- Example:
```java
@Cacheable("books")
public Book findBookByIsbn(String isbn) {
    // Simulate slow DB operation
}
```
- The next call with the same `isbn` will read from the cache "books" and not re-execute the method.

### Key Features and Parameters

- **cacheNames/value**: Name(s) of the cache to use (`@Cacheable("myCache")`).
- **key**: Customizes how the cache key is generated, using Spring EL (`@Cacheable(value="books", key="#isbn")`).
- **condition**: Cache the result only if the condition is true (`@Cacheable(value="users", condition="#age>18")`).
- **unless**: Prevent caching if this condition becomes true (`@Cacheable(value="orders", unless="#result.total < 100")`).
- **sync**: Controls synchronization for cache lookups to avoid simultaneous slow method executions.

### Advanced Usage and Best Practices

- Can be applied at method or class level.
- Supports multiple caches (`@Cacheable({"books", "isbns"})`). Spring updates all caches accordingly.
- Can be combined with other cache annotations like `@CachePut` (always run and update cache) and `@CacheEvict` (remove items from cache).
- Works best with idempotent and side-effect free methods.
- Beans must be managed by Spring (do not instantiate with `new`, as Spring AOP proxying is required).

### Interview Key Points

| Aspect                  | Description                                                            |
|-------------------------|------------------------------------------------------------------------|
| Purpose                 | Avoid redundant computation; use cache for repeated calls.              |
| Usage                   | Simple: `@Cacheable("cacheName")`; result stored by method params.      |
| Key customization       | Use SpEL for cache key expressions.                                    |
| Conditional caching     | Use `condition`, `unless` for more control.                            |
| Cache invalidation      | Use with `@CacheEvict` to clear or update cache when data changes.      |
| Limitations             | Only works on Spring-managed beans, not on new object instances.        |

In summary, **`@Cacheable` is a declarative, annotation-driven caching mechanism** in Spring for optimizing application performance by caching expensive or commonly requested operation results[1][3][5][7].The `@Cacheable` annotation in Spring is used for declarative, annotation-based caching—meaning it allows methods to have their results automatically stored in a cache so later calls with the same parameters return the cached result without executing the method again[1][3][5][7].

### How @Cacheable Works

- When a method annotated with `@Cacheable` is invoked, Spring checks the cache for the result associated with the given input (the cache key, usually derived from method parameters).
- If the value is present, it is returned from the cache, and the method is **not** executed.
- If it is not present, the method is executed, and the return value is stored in the specified cache for future use.
- Example:
  ```java
  @Cacheable("books")
  public Book findBookByIsbn(String isbn) { 
      // slow db call here
  }
  ```
- Every subsequent call with the same ISBN will return the cached Book from the "books" cache, skipping method execution.

### Key Features

- **cacheNames/value**: Specifies the name(s) of the cache(s) to use.
- **key**: SpEL (Spring Expression Language) to customize the cache key (`@Cacheable(value="books", key="#isbn")`).
- **condition/unless**: Control when results should be cached or not (great for conditional or negative caching).
- **sync**: To synchronize cache loading, avoiding parallel computation for the same key.
- Caching works transparently for Spring-managed beans only—if instantiated using `new`, caching will not work as there's no proxy interception.

### Best Practices

- Use on read-heavy, expensive operations.
- Combine with `@CacheEvict` (to remove stale entris) and `@CachePut` (to update cache without skipping method execution) as needed.
- For advanced needs, combine multiple annotations using `@Caching`, or create composed meta-annotations.

### Interview Table

| Feature     | Description                                                                      |
|-------------|----------------------------------------------------------------------------------|
| Purpose     | Avoids expensive method calls by reusing cached results                           |
| Usage       | Annotate methods; specify cache name(s) and optionally key/condition/unless       |
| Limitations | Only for Spring-managed beans; won't work with `new` instantiation                |
| Custom Key  | Use SpEL for cache key computation; e.g., key = "#isbn"                           |
| Conditional | Cache/skip based on condition and unless SpEL expressions                         |


----

## Scheduled Tasks / Cron Jobs in Java
A **cron job in Java** is a scheduled task that executes code at regular, specific intervals (e.g., daily, hourly). In interview settings, you should focus on how cron jobs can be implemented using core Java and with frameworks like Spring, as well as how cron expressions control scheduling[1][2][4][6][9].

### 1. Cron Job Concepts

- Inspired by Unix “cron”, a utility that schedules shell/script execution.
- In Java, cron jobs are used for automated work like report generation, periodic data cleanup, backups, or notifications.
- Cron jobs provide timing control using “cron expressions”: formatted strings indicating minutes, hours, days, months, weekdays, etc. Example: `"0 0 12 * * ?"` for noon daily[3][9].

### 2. Implementing Cron Jobs

#### A. Using Quartz Scheduler (Enterprise Standard)
- Quartz is a popular Java library for scheduling jobs using cron expressions.
- Define a Job class by implementing `org.quartz.Job`:
  ```java
  public class MyJob implements Job {
      @Override
      public void execute(JobExecutionContext context) {
          // business logic here
      }
  }
  ```
- Set up a trigger with a cron expression:
  ```java
  CronTrigger trigger = new CronTrigger("cronTrigger", "group1", "0 0/1 * * * ?");
  ```
- Associate the job and trigger with a scheduler:
  ```java
  Scheduler scheduler = new StdSchedulerFactory().getScheduler();
  scheduler.scheduleJob(jobDetail, trigger);
  scheduler.start();
  ```
- Quartz handles the execution, persistence, and repeat scheduling[4][2][7].

#### B. Using Spring @Scheduled (Modern, Framework-based)
- Spring Boot/Framework enables easy cron scheduling via annotations.
- Annotate the main class with `@EnableScheduling`:
  ```java
  @SpringBootApplication
  @EnableScheduling
  public class DemoApp { ... }
  ```
- Annotate a method with `@Scheduled`:
  ```java
  @Scheduled(cron = "0 0 12 * * ?")
  public void scheduledTask() {
      // Executes daily at 12 noon
  }
  ```
- Spring manages execution and timing, no manual scheduler code needed.
- Can use fixed delays, intervals, or cron expressions for flexibility[1][6][9].

### 3. Running Java Apps as System Cron Jobs

- Outside of Java frameworks, the OS “cron” tool can schedule a JAR or class file:
  ```
  * * * * * java -cp /path/app.jar com.example.App
  ```
- This is set in the crontab of Unix/Linux systems and runs the Java code in intervals specified by the five fields (minute, hour, day, month, weekday)[1].

### 4. Interview Key Points

| Aspect                 | Description                                                                               |
|------------------------|-------------------------------------------------------------------------------------------|
| Cron Expression        | String format for schedule control, e.g., `"0 0 * * * ?"` for every midnight              |
| Quartz                 | Enterprise job scheduler, flexible cron/job management, persistent                       |
| Spring @Scheduled      | Annotation-driven, simple integration, no external scheduler required                     |
| System Cron            | Uses OS-level cron to trigger Java programs, not controlled in Java source                |
| Use Cases              | Reports, reminders, batch jobs, archival, periodic data sync                              |
| Pitfalls/Considerations| Timezones, error handling, job overlap, persistence if scheduling in distributed systems  |

A cron job in Java is most commonly implemented using Spring’s `@Scheduled` (for simplicity) and Quartz Scheduler (for enterprise control), with execution managed by cron expressions that specify exactly when the code should run[1][2][4][6][7][9].

---

# Spring AOP (Aspect-Oriented Programming)

## Purpose: Cross-Cutting Concerns

Aspect-Oriented Programming (AOP) is a programming approach that helps in separating concerns in your program, especially those that cut across multiple parts of an application such as:
- Logging
- Security
- Transaction Management
- Performance Metrics
- Auditing
- Validation

---

## Core AOP Concepts

| Concept        | Purpose                                                                        | Description                                                                                                                                                       |
|----------------|--------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Aspect**     | The module / class where we define the advices                                 | A module that encapsulates pointcuts and advices.                                                                                                                 |
| **Advice**     | What action to be called ?                                                     | Action taken at a particular JoinPoint. Types include `@Before`, `@After`, `@Around`, etc.                                                                        |
| **Pointcut**   | Where do you want to call the advice?                                          | A predicate that matches JoinPoints (where advice should be applied). @PointCut can also be used                                                                  |
| **JoinPoint**  | When that execution is done? (gives the details for the execution)             | A point during execution of a program (e.g., method execution) where an aspect can be applied.                                                                    |
| **Weaving**    | Process of applying aspects (advice/pointcuts/introductions) to target classes | Weaving connects your aspect code to join points in the application so the advice runs at those points by modifying their bytecode or wrapping them with proxies. |

---

## Types of Advices
- Advice will tell us when to execute the method, this will include the pointcut (where to execute the method) as parameter.
- `@Before`: Executes before the JoinPoint.
- `@After`: Executes after the JoinPoint (finally block style).
- `@AfterReturning`: Executes after method returns successfully.
- `@AfterThrowing`: Executes if method throws an exception.
- `@Around`: @Before + @After - Advice that runs before and after a method, with control over whether the method executes.

```java
package com.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    // Common pointcut expression
    @Pointcut("execution(* com.example.service.BankService.transferMoney(..))")
    public void transferOperation() {}

    // 1️⃣ @Before - Executes BEFORE the method runs
    @Before("transferOperation()")
    public void beforeTransfer(JoinPoint joinPoint) {
        System.out.println("🔹 [Before] Starting method: " + joinPoint.getSignature().getName());
    }

    // 2️⃣ @After - Executes AFTER method completes (regardless of success or exception)
    @After("transferOperation()")
    public void afterTransfer(JoinPoint joinPoint) {
        System.out.println("🔹 [After] Finished method: " + joinPoint.getSignature().getName());
    }

    // 3️⃣ @AfterReturning - Runs only if method completes successfully
    @AfterReturning(pointcut = "transferOperation()", returning = "result")
    public void afterReturningTransfer(JoinPoint joinPoint, Object result) {
        System.out.println("✅ [AfterReturning] Method returned: " + result);
    }

    // 4️⃣ @AfterThrowing - Runs only if method throws an exception
    @AfterThrowing(pointcut = "transferOperation()", throwing = "ex")
    public void afterThrowingTransfer(JoinPoint joinPoint, Exception ex) {
        System.out.println("❌ [AfterThrowing] Exception occurred: " + ex.getMessage());
    }

    // 5️⃣ @Around - Combines all: can run code before & after AND even control execution
    @Around("transferOperation()")
    public Object aroundTransfer(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("🌀 [Around] Before execution: " + pjp.getSignature().getName());
        Object result;
        try {
            // Proceed to actual method execution
            result = pjp.proceed();

            // After the method execution
            System.out.println("🌀 [Around] After successful execution");
        } catch (Exception ex) {
            System.out.println("🌀 [Around] Exception caught: " + ex.getMessage());
            throw ex;
        }
        System.out.println("🌀 [Around] After finally");
      // must return the method’s actual return value
        return result;
    }
}
```

**What is ProceedingJoinPoint?**
-	It represents the method being intercepted (the “join point”).
-	You can:
-	Get the method name → joinPoint.getSignature()
-	Get arguments → joinPoint.getArgs()
-	Execute the actual method → joinPoint.proceed()
- If you don’t call proceed(), the target method will never execute. So you can literally skip or short-circuit logic.

| Annotation      | When it Runs                                         | Purpose                                       | Key Notes                              |
|-----------------|------------------------------------------------------|-----------------------------------------------|----------------------------------------| 
| @Before         | Before method execution                              | Setup, logging, pre-validation                | Can access method args using JoinPoint |
| @After          | After method (success or failure)                    | Cleanup, resource closing                     | Runs like a finally block              |
| @AfterReturning | Only after successful return                         | Post-processing or logging return values      | Has returning parameter                |
| @AfterThrowing  | Only if exception occurs                             | Error logging or rollback                     | Has throwing parameter                 |
| @Around         | Before + After (and can prevent or modify execution) | Most powerful; can modify arguments or return | Must call pjp.proceed() manually       | 

---

## Enable AOP in Spring
1. Add Dependencies
2. Enable AspectJ Auto Proxy - In your main Spring Boot application (or any config class):
```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
}
```
3. Create an Aspect Class

---

## Proxy Mechanism
- Spring implements AOP using proxy objects instead of directly modifying class bytecode (like AspectJ).

**- Why Does Spring Use Proxies?**
- Spring implements AOP using proxy objects instead of directly modifying class bytecode (like AspectJ).

**- Benefits:**
-	No need for custom class loaders or compilers.
-	Works seamlessly with existing Spring beans.
-	Lightweight and easy to use.
-	Dynamic — proxies created at runtime.

**- Limitations:**
-	Can only intercept public methods.
-	Works only when you call the method via the proxy (not via this reference, i.e., self-invocation problem).

**- Types of Proxies in Spring**
- Spring uses two mechanisms to create proxy objects depending on the target bean type.
- **JDK Dynamic Proxies**: Used if the target implements at least one interface.
- **CGLIB Proxies**:
  - If there’s no interface, Spring uses CGLIB (Code Generation Library).
  - CGLIB creates a subclass dynamically at runtime:
  - So when Spring returns your bean, it’s actually an instance of MyService$$EnhancerByCGLIB.

- You can force which proxying strategy Spring uses: Use `proxyTargetClass=true` to enforce CGLIB proxying.

---

## Interview Follow-up Questions

### Q1: How does Spring choose between JDK and CGLIB proxy?
**A:** If the bean implements an interface, Spring uses JDK dynamic proxies by default. If no interfaces are found or if `proxyTargetClass=true`, it uses CGLIB.

### Q2: Can you apply aspects to private methods?
**A:** No. Spring AOP uses proxies, and it can only intercept public/protected methods that are externally visible.

### Q3: What is the difference between Spring AOP and AspectJ?
**A:** Spring AOP is proxy-based and applies aspects at runtime, limited to method execution join points. AspectJ supports compile-time, load-time, and runtime weaving and has a richer join point model.

### Q4: Is AOP supported in Spring Boot by default?
**A:** Yes. Just add `spring-boot-starter-aop` and enable it using `@EnableAspectJAutoProxy`.

---

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  JWTFilter defined in file [/Users/nikhilladdha/Desktop/Projects/MediConnect/AuthService/target/classes/com/mediconnect/service/auth/security/JWTFilter.class]
↑     ↓
|  userServiceImpl (field private org.springframework.security.authentication.AuthenticationManager com.mediconnect.service.auth.service.UserServiceImpl.authenticationManager)
↑     ↓
|  securityConfig defined in file [/Users/nikhilladdha/Desktop/Projects/MediConnect/AuthService/target/classes/com/mediconnect/service/auth/security/SecurityConfig.class]
└─────┘


Why Constructor Injection Fails with CyclesWhen Spring creates a bean using constructor injection, it must have all the dependencies ready before it can instantiate the object.1.To create JWTFilter, Spring needs UserServiceImpl.2.To create UserServiceImpl, Spring needs SecurityConfig (likely for the PasswordEncoder).3.To create SecurityConfig, Spring needs JWTFilter (to add it to the filter chain).This creates a deadlock:•Spring says: "I can't create JWTFilter until I have UserServiceImpl."•"I can't create UserServiceImpl until I have SecurityConfig."•"I can't create SecurityConfig until I have JWTFilter."Since none of them can be created first, the application fails to start.Why Field/Setter Injection (or @Lazy) WorksWith field injection (e.g., @Autowired on a field) or setter injection, Spring can:1.Instantiate JWTFilter (with null dependencies initially).2.Instantiate UserServiceImpl.3.Instantiate SecurityConfig.4.Then, once the objects exist, it goes back and injects the dependencies into the fields/setters.Using @Lazy on a constructor parameter tells Spring: "Don't give me the real object right now. Give me a proxy (a placeholder)."•Spring creates JWTFilter with a proxy for UserServiceImpl.•The real UserServiceImpl is only created/looked up when you actually call a method on that proxy.•This breaks the initialization cycle because JWTFilter can be successfully created without waiting for the fully initialized UserServiceImpl.How to Fix ItYou need to break the cycle. The most common fix in Spring Security configurations is to use @Lazy on one of the dependencies in the constructor.I will apply @Lazy to the UserServiceImpl dependency in JWTFilter (or wherever it makes the most sense) to resolve this.First, I need to see the code for JWTFilter and SecurityConfig.