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
