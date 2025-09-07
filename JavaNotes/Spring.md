✅ Spring Ecosystem (Advanced Topics)

🔹 1. Spring Boot Auto-Configuration Internals
* How @SpringBootApplication works (includes @EnableAutoConfiguration)
* Auto-configuration mechanism using:
* @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty
* spring.factories or META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
* Custom auto-configuration creation
* Excluding auto-configs via exclude or spring.autoconfigure.exclude

⸻

# Spring Boot Auto-Configuration Internals

## 1. How @SpringBootApplication works

### Core Components:
- `@SpringBootApplication` is a meta-annotation that combines:
    - `@Configuration`: Marks the class as a source of bean definitions.
    - `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on classpath settings, other beans, and property settings.
    - `@ComponentScan`: Enables component scanning in the package of the annotated class.

### Interview Tip:
**Q:** What would happen if you remove `@EnableAutoConfiguration`?
**A:** Spring Boot won't configure the application automatically (e.g., DataSource, DispatcherServlet, etc.).

---

## 2. Auto-configuration mechanism

### Annotations:
- `@ConditionalOnClass`: Loads configuration only if a specific class is present in the classpath.
- `@ConditionalOnMissingBean`: Applies auto-config only if a bean is not already defined by the user.
- `@ConditionalOnProperty`: Activates config based on specific property values in `application.properties`/`yml`.

```java
@Bean
@ConditionalOnMissingBean
public MyService myService() {
    return new MyService();
}
```

### Interview Tip:
**Q:** Can auto-configuration override your manually defined beans?
**A:** No. `@ConditionalOnMissingBean` ensures your custom bean takes precedence.

---

## 3. How Spring Boot discovers Auto-configurations

### Mechanism:
- Before Spring 2.7:
    - Uses `META-INF/spring.factories`
    - Lists all auto-configuration classes under `org.springframework.boot.autoconfigure.EnableAutoConfiguration`

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.autoconfig.MyAutoConfig
```

- Since Spring Boot 3.x:
    - Uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```text
com.example.autoconfig.MyAutoConfig
```

### Interview Tip:
**Q:** What replaced `spring.factories` in Spring Boot 3?
**A:** `AutoConfiguration.imports` under `META-INF/spring/`

---

## 4. Creating a Custom Auto-Configuration

### Steps:
1. Create a `@Configuration` class with `@Conditional...` annotations.
2. Define beans conditionally.
3. Register it using:
    - Spring Boot 2: `spring.factories`
    - Spring Boot 3: `AutoConfiguration.imports`

### Example:
```java
@Configuration
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public class FeatureAutoConfiguration {

    @Bean
    public FeatureService featureService() {
        return new FeatureService();
    }
}
```

---

## 5. Excluding Auto-Configurations

### Method 1: Annotation-based exclusion
```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
```

### Method 2: Property-based exclusion
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

### Interview Tip:
**Q:** Why would you exclude auto-configuration?
**A:** If it interferes with custom setup or causes startup failures due to missing dependencies.

---

## Follow-up Interview Questions:

### 1. What happens if two auto-configuration classes try to configure the same bean?
**A:** The first one wins unless `@ConditionalOnMissingBean` is used.

### 2. How does Spring Boot decide the order of auto-configuration?
**A:** Uses `@AutoConfigureBefore` and `@AutoConfigureAfter` annotations to manage the order.

### 3. Can we write auto-configuration for third-party libraries?
**A:** Yes, you can create your own starter module with configuration class and expose it via `spring.factories` or `AutoConfiguration.imports`.

### 4. What is the role of `@ImportAutoConfiguration`?
**A:** It can be used to manually import specific auto-configurations in non-boot projects.

---

---

🔹 2. Spring AOP (Aspect-Oriented Programming)
* Purpose: cross-cutting concerns (logging, security, transactions)
* AOP concepts: JoinPoint, Pointcut, Advice, Aspect, Weaving
* Types of advices: @Before, @After, @Around, @AfterThrowing, @AfterReturning
* Using @EnableAspectJAutoProxy
* Proxy types: JDK dynamic proxies vs CGLIB
* Use cases: method logging, performance metrics, auditing, validation

⸻
# Spring AOP (Aspect-Oriented Programming)

## 🔹 Purpose: Cross-Cutting Concerns

Spring AOP is used to modularize concerns that cut across multiple classes such as:
- Logging
- Security
- Transaction Management
- Performance Metrics
- Auditing
- Validation

---

## 🔹 Core AOP Concepts

| Concept      | Description |
|--------------|-------------|
| **JoinPoint** | A point during execution of a program (e.g., method execution) where an aspect can be applied. |
| **Pointcut** | A predicate that matches JoinPoints (where advice should be applied). |
| **Advice** | Action taken at a particular JoinPoint. Types include `@Before`, `@After`, `@Around`, etc. |
| **Aspect** | A module that encapsulates pointcuts and advices. |
| **Weaving** | Process of linking aspects with other application types to create an advised object. |

---

## 🔹 Types of Advices

- `@Before`: Executes before the JoinPoint.
- `@After`: Executes after the JoinPoint (finally block style).
- `@AfterReturning`: Executes after method returns successfully.
- `@AfterThrowing`: Executes if method throws an exception.
- `@Around`: Surrounds the JoinPoint (most powerful; can control method execution).

---

## 🔹 Enable AOP in Spring

```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
}
```

---

## 🔹 Proxy Mechanism

- **JDK Dynamic Proxies**: Used if the target implements at least one interface.
- **CGLIB Proxies**: Used if the target is a concrete class.

Use `proxyTargetClass=true` to enforce CGLIB proxying.

---

## 🔹 Common Use Cases

- Logging method input/output
- Timing execution for performance monitoring
- Access control checks
- Automatic validation and auditing

---

## 🔹 Example Aspect

```java
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Method Called: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "execution(* com.example.service.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Method returned: " + result);
    }
}
```

---

## 🔹 Interview Follow-up Questions

### Q1: How does Spring choose between JDK and CGLIB proxy?
**A:** If the bean implements an interface, Spring uses JDK dynamic proxies by default. If no interfaces are found or if `proxyTargetClass=true`, it uses CGLIB.

### Q2: Can you apply aspects to private methods?
**A:** No. Spring AOP uses proxies, and it can only intercept public/protected methods that are externally visible.

### Q3: What is the difference between Spring AOP and AspectJ?
**A:** Spring AOP is proxy-based and applies aspects at runtime, limited to method execution join points. AspectJ supports compile-time, load-time, and runtime weaving and has a richer join point model.

### Q4: Is AOP supported in Spring Boot by default?
**A:** Yes. Just add `spring-boot-starter-aop` and enable it using `@EnableAspectJAutoProxy`.

---
---

🔹 3. Event Handling with ApplicationEventPublisher
* Publish events using ApplicationEventPublisher
* Listen with @EventListener or implementing ApplicationListener
* Asynchronous event processing using @Async
* Custom events vs built-in events (ContextRefreshedEvent, ApplicationReadyEvent, etc.)
* Use cases: decoupling, domain-driven events, notification systems

⸻


# Spring Event Handling with ApplicationEventPublisher

## 1. Overview

Spring's event-driven model allows beans to publish and listen to events synchronously or asynchronously, enabling decoupled communication between components.

## 2. Core Concepts

### 🔹 ApplicationEventPublisher

Used to publish custom or built-in events.

```java
@Autowired
private ApplicationEventPublisher publisher;

publisher.publishEvent(new MyCustomEvent(this, data));
```

### 🔹 @EventListener

Used to listen for events in a decoupled way.

```java
@EventListener
public void handleCustomEvent(MyCustomEvent event) {
    // handle event
}
```

### 🔹 Implementing ApplicationListener

Another way to listen to events (pre-@EventListener).

```java
@Component
public class MyListener implements ApplicationListener<MyCustomEvent> {
    @Override
    public void onApplicationEvent(MyCustomEvent event) {
        // handle event
    }
}
```

### 🔹 Asynchronous Event Processing

Mark the listener method with `@Async` and enable async processing with `@EnableAsync`.

```java
@Async
@EventListener
public void handleAsync(MyCustomEvent event) {
    // handle event asynchronously
}
```

## 3. Built-in Spring Events

| Event | Description |
|-------|-------------|
| `ContextRefreshedEvent` | When ApplicationContext is initialized or refreshed |
| `ApplicationReadyEvent` | After application is ready to serve requests |
| `ContextClosedEvent` | When ApplicationContext is closed |
| `ContextStartedEvent` | When ApplicationContext is started |
| `ContextStoppedEvent` | When ApplicationContext is stopped |

## 4. Custom Events

```java
public class MyCustomEvent extends ApplicationEvent {
    private final String message;
    public MyCustomEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
```

## 5. Use Cases

- Decoupled communication between services/components
- Domain-driven design event publishing
- Sending notifications/emails asynchronously
- Caching and auditing

## 6. Follow-up Interview Questions

### Q1: What is the difference between synchronous and asynchronous event handling?
**A:** Synchronous events block the publisher until all listeners are done, while asynchronous events run listeners in separate threads, allowing the publisher to continue execution.

### Q2: When would you use `@EventListener` vs `ApplicationListener`?
**A:** `@EventListener` is more declarative and flexible with support for conditional listening and async processing. `ApplicationListener` is more verbose and tightly coupled.

### Q3: How do you ensure thread-safety in async event listeners?
**A:** Use thread-safe data structures, proper synchronization, or delegate to services that manage thread safety.

### Q4: Can you filter events in `@EventListener`?
**A:** Yes, by using the `condition` attribute, e.g.
```java
@EventListener(condition = "#event.message == 'trigger'")
```

---

**Tip:** Keep listeners lightweight and quick. Offload heavy processing to async workers or messaging systems like Kafka or RabbitMQ when needed.
---

---

🔹 4. Profiles and Environment-Specific Configurations
* Using @Profile("dev"), @Profile("prod") on beans or configs
* Managing different application-{profile}.yml files
* Activating profiles via:
* Spring Boot CLI: --spring.profiles.active=dev
* Environment variables
* JVM args
* Use cases: different DB configs, logging levels, feature toggles

⸻

# Spring Profiles and Environment-Specific Configurations

## Overview

Spring provides a powerful way to manage different configurations for different environments (e.g., development, testing, production) using profiles.

---

## 1. Using `@Profile`

### Definition
You can annotate beans or configuration classes with `@Profile("profileName")` to include them only in specific environments.

```java
@Configuration
@Profile("dev")
public class DevDatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // dev-specific DataSource
    }
}
```

### Multiple Profiles
```java
@Profile({"dev", "test"})
```

---

## 2. Managing Profile-Specific `application.yml` Files

You can create separate property files for each environment:

- `application-dev.yml`
- `application-prod.yml`

### Structure

**application.yml**
```yaml
spring:
  profiles:
    active: dev
```

**application-dev.yml**
```yaml
logging:
  level:
    root: DEBUG
```

**application-prod.yml**
```yaml
logging:
  level:
    root: ERROR
```

---

## 3. Activating Profiles

### 1. Spring Boot CLI

```bash
java -jar myapp.jar --spring.profiles.active=dev
```

### 2. Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
```

### 3. JVM Arguments

```bash
-Dspring.profiles.active=test
```

---

## 4. Use Cases

- Switching between different database configurations (H2 for dev, PostgreSQL for prod)
- Changing logging levels across environments
- Enabling/disabling features conditionally
- Using different API endpoints or third-party integrations

---

## Follow-Up Interview Questions

### Q1: Can you activate multiple profiles simultaneously?
**A:** Yes, by separating them with commas: `--spring.profiles.active=dev,feature-x`.

### Q2: What happens if a bean is defined in two profiles and both are active?
**A:** The application context will fail to start unless you use conditional logic or prioritize bean creation using `@Primary`.

### Q3: What is the default profile in Spring Boot?
**A:** If no profile is explicitly set, the `default` profile is considered active.

---

## Summary

Spring Profiles help you cleanly separate configuration concerns based on runtime environments, improving code manageability and deployment flexibility.

---
---


🔹 5. @Conditional Annotations and Bean Lifecycle

Conditional Annotations
* @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty, @ConditionalOnResource, etc.
* Custom conditions via @Conditional(MyCondition.class)

Bean Lifecycle
* Initialization: @PostConstruct, InitializingBean
* Destruction: @PreDestroy, DisposableBean
* Bean scopes: singleton, prototype, request, session
* Bean lifecycle events (BeanPostProcessor, SmartInitializingSingleton)
* Importance of IoC Container in managing lifecycle


# @Conditional Annotations and Bean Lifecycle

## 🔹 Conditional Annotations

Spring provides conditional annotations to control when a bean should be registered in the context.

### Common Conditional Annotations

- **@ConditionalOnClass**: Loads a bean only if a specific class is present in the classpath.
- **@ConditionalOnMissingBean**: Loads a bean only if a specified bean is not already defined.
- **@ConditionalOnProperty**: Loads a bean based on the presence or value of a property.
- **@ConditionalOnResource**: Loads a bean if a specified resource is available.
- **@Conditional**: Base annotation to define custom conditions using a class implementing `Condition` interface.

### Example

```java
@Configuration
@ConditionalOnClass(name = "com.example.SomeClass")
public class SomeAutoConfig {
    // Beans defined here will load only if SomeClass is present
}
```

### Custom Condition Example

```java
public class MyCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().containsProperty("my.feature.enabled");
    }
}

@Configuration
@Conditional(MyCondition.class)
public class MyFeatureConfig {
    // This config loads only when "my.feature.enabled" property is present
}
```

---

## 🔹 Bean Lifecycle

Spring beans go through a lifecycle from creation to destruction.

### Initialization

- **@PostConstruct**: Method runs after dependency injection is done.
- **InitializingBean.afterPropertiesSet()**: Custom initialization logic.

### Destruction

- **@PreDestroy**: Method runs before bean is destroyed.
- **DisposableBean.destroy()**: Clean-up logic.

### Bean Scopes

- **singleton** (default): Single instance per Spring context.
- **prototype**: New instance every time it's requested.
- **request**: One instance per HTTP request (web only).
- **session**: One instance per HTTP session (web only).

### Bean Lifecycle Hooks

- **BeanPostProcessor**: Modify bean before and after initialization.
- **SmartInitializingSingleton**: Callback after all singletons are initialized.

### Importance of IoC Container

The IoC (Inversion of Control) container is responsible for managing the full lifecycle of beans, including:

- Creation
- Dependency Injection
- Initialization
- Destruction

This allows developers to focus on business logic without managing object lifecycles.

---

## 🔍 Interview Follow-Up Questions

### Q1. What is the difference between @PostConstruct and InitializingBean?
**A:** `@PostConstruct` is annotation-based and preferred for modern development. `InitializingBean` is interface-based and ties your bean to Spring.

### Q2. How can you define a custom condition?
**A:** Implement the `Condition` interface and override the `matches()` method. Use `@Conditional(MyCondition.class)`.

### Q3. What is the use of BeanPostProcessor?
**A:** It allows for modification of new bean instances, like wrapping them with proxies or performing validations.

### Q4. When would you use a prototype scope?
**A:** When you need a new instance every time a bean is requested, like in the case of stateful beans.

---

⸻

🔹 6. Spring Actuator

# Spring Actuator

## 🔹 What is Spring Actuator?
Spring Boot Actuator provides production-ready features to help you monitor and manage your application. It exposes various REST endpoints to give insights into your app’s internals.

---

## 🔹 Key Features
- Health checks
- Metrics (JVM, CPU, memory, custom metrics)
- Audit events
- Application environment details
- Thread dumps and HTTP trace logs

---

## 🔹 Common Actuator Endpoints
| Endpoint           | Description                              |
|--------------------|------------------------------------------|
| `/actuator/health` | Shows application health                 |
| `/actuator/info`   | Displays arbitrary application info      |
| `/actuator/metrics`| Exposes metrics like memory, CPU usage   |
| `/actuator/env`    | Lists environment properties             |
| `/actuator/beans`  | Shows all Spring beans                   |
| `/actuator/loggers`| Shows and modifies log levels dynamically|
| `/actuator/httptrace` | Displays HTTP request traces        |

---

## 🔹 Enabling Actuator
Add the dependency in `pom.xml` or `build.gradle`:

**Maven:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Gradle:**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

---

## 🔹 Configuring Endpoints
Customize exposure in `application.yml` or `application.properties`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: always
```

---

## 🔹 Security for Endpoints
By default, sensitive actuator endpoints require authentication.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
spring:
  security:
    user:
      name: admin
      password: admin123
```

Use Spring Security to control access.

---

## 🔹 Custom Info Endpoint
Define info in `application.yml`:

```yaml
info:
  app:
    name: My App
    description: Spring Boot App with Actuator
    version: 1.0.0
```

---

## 🔹 Integrations
- **Micrometer**: For metrics collection and integration with Prometheus, Graphite, etc.
- **Spring Cloud**: For advanced tracing, circuit breakers, etc.

---

## 🔹 Use Cases
- Monitor application uptime, health, and performance.
- Integrate with alerting/monitoring systems like Prometheus and Grafana.
- View logs and trace issues dynamically.

---

## 🔹 Interview Follow-up Questions

### Q1: How can you restrict access to actuator endpoints?
**A:** Use Spring Security, configure endpoint exposure, and apply roles.

### Q2: How do you create a custom actuator endpoint?
**A:** Use `@Endpoint`, `@ReadOperation`, `@WriteOperation` annotations.

### Q3: How does Actuator relate to Micrometer?
**A:** Micrometer is the metrics collection facade used by Actuator to expose metrics in a vendor-neutral way.

### Q4: Can actuator endpoints be used in production?
**A:** Yes, but they should be secured and carefully exposed.

---

⸻

✅ Spring MVC Core (Web Layer in Spring)

🔹 1. Architecture
* DispatcherServlet
* HandlerMapping → Controller → ViewResolver
* Front Controller pattern

# 🔹 1. Spring MVC Architecture

## ✅ Overview

Spring MVC follows the **Front Controller pattern**, where a single servlet (`DispatcherServlet`) handles all incoming requests and delegates them to appropriate handlers (controllers).

---

## 🔹 Key Components

### 🚀 DispatcherServlet
- Acts as the **front controller**.
- Responsible for receiving HTTP requests and routing them to appropriate components.
- Configured automatically by Spring Boot via `@SpringBootApplication`.

### 🧭 HandlerMapping
- Maps incoming requests to handler methods (controllers).
- Based on annotations like `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
- Examples: `RequestMappingHandlerMapping`, `SimpleUrlHandlerMapping`.

### 👨‍🏫 Controller
- Annotated with `@Controller` or `@RestController`.
- Contains methods that handle HTTP requests and return model data or response bodies.
- Can return a `ModelAndView`, a String view name, or JSON (in case of `@RestController`).

### 🧩 ViewResolver
- Responsible for resolving the logical view names returned by controllers to actual views (like JSP, Thymeleaf, etc.).
- Example: `InternalResourceViewResolver`, `ThymeleafViewResolver`.

---

## 🔄 Request Flow Diagram

```
Client → DispatcherServlet
       → HandlerMapping
       → Controller
       → ViewResolver
       → View
       → Response
```

---

## 📌 Front Controller Pattern

- Centralizes control of request handling.
- Improves maintainability and scalability.
- Allows pre/post-processing via interceptors and filters.

---

## 💡 Example

```java
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello from Spring MVC!");
        return "hello"; // Logical view name
    }
}
```

- DispatcherServlet receives `/hello` request.
- HandlerMapping maps it to `hello()` method.
- Controller returns "hello" view name.
- ViewResolver resolves to `/WEB-INF/views/hello.jsp`.

---

## 🧠 Interview Follow-Up Questions

### ❓ What is the role of DispatcherServlet in Spring MVC?
> It's the front controller that routes requests to appropriate controllers.

### ❓ What is the difference between `@Controller` and `@RestController`?
> `@RestController` is a combination of `@Controller` and `@ResponseBody`, used for REST APIs.

### ❓ Can we have multiple ViewResolvers?
> Yes. Spring supports ordering them using `setOrder()`.

### ❓ How does Spring resolve a view name?
> It uses `ViewResolver` to convert the view name to an actual view resource.

---

🔹 2. Controllers
* @RestController vs @Controller
* @RequestMapping, @GetMapping, @PostMapping, etc.
* @RequestParam, @PathVariable, @RequestBody, @ModelAttribute
* Returning ResponseEntity<> with custom status codes

## 2. Controllers in Spring MVC

### @RestController vs @Controller

- **@Controller**:

    - Returns a **view name** to be resolved by a `ViewResolver` (e.g., JSP).
    - Typically used for web applications that return HTML.
    - Example:
      ```java
      @Controller
      public class HomeController {
          @GetMapping("/home")
          public String home() {
              return "home"; // resolves to home.jsp
          }
      }
      ```

- **@RestController**:

    - Combines `@Controller` and `@ResponseBody`.
    - Returns data directly (usually JSON or XML).
    - Commonly used in REST APIs.
    - Example:
      ```java
      @RestController
      public class ApiController {
          @GetMapping("/api/data")
          public Map<String, String> getData() {
              return Map.of("key", "value");
          }
      }
      ```

### Mapping Annotations

- **@RequestMapping**:

    - Generic annotation for mapping HTTP requests to handler methods.
    - Can specify method, headers, params, etc.
    - Example:
      ```java
      @RequestMapping(value = "/user", method = RequestMethod.GET)
      public String getUser() { return "user"; }
      ```

- **@GetMapping**, **@PostMapping**, **@PutMapping**, **@DeleteMapping**:

    - Shortcut annotations for `@RequestMapping(method = ...)`.
    - Example:
      ```java
      @PostMapping("/submit")
      public String submitData(@RequestBody MyData data) {
          return "saved";
      }
      ```

### Method Parameter Annotations

- **@RequestParam**:

    - Used to extract query parameters from the URL.
    - Example:
      ```java
      @GetMapping("/search")
      public String search(@RequestParam String keyword) {
          return "Searching for: " + keyword;
      }
      ```

- **@PathVariable**:

    - Binds a method parameter to a URI template variable.
    - Example:
      ```java
      @GetMapping("/user/{id}")
      public String getUser(@PathVariable int id) {
          return "User ID: " + id;
      }
      ```

- **@RequestBody**:

    - Binds the body of the request to a Java object.
    - Used for POST/PUT requests.
    - Example:
      ```java
      @PostMapping("/user")
      public String createUser(@RequestBody User user) {
          return "User Created: " + user.getName();
      }
      ```

- **@ModelAttribute**:

    - Used to bind form data or populate common model attributes.
    - Also used for form submissions.
    - Example:
      ```java
      @PostMapping("/register")
      public String register(@ModelAttribute User user) {
          return "registered";
      }
      ```

### Returning ResponseEntity<>

- Used to:

    - Return HTTP status codes.
    - Return custom headers.
    - Provide fine-grained control over response.

- Example:

  ```java
  @GetMapping("/status")
  public ResponseEntity<String> checkStatus() {
      return ResponseEntity.status(HttpStatus.OK)
                           .body("Application is running");
  }

  @PostMapping("/create")
  public ResponseEntity<User> create(@RequestBody User user) {
      return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }
  ```

---

### Interview Follow-up Questions

1. **What is the difference between @PathVariable and @RequestParam?**

    - `@PathVariable` is used to capture dynamic parts from the URL path.
    - `@RequestParam` is used to extract query parameters.

2. **When would you use ResponseEntity instead of returning plain objects?**

    - When you need to set status codes, headers, or content types manually.

3. **Can you use @RestController and @Controller in the same project?**

    - Yes. They serve different purposes and can coexist.

4. **What happens if both @PathVariable and @RequestParam are used in the same method?**

    - It works fine; you can capture both path and query data in a single method.

5. **How do you handle default values with @RequestParam?**

   ```java
   @RequestParam(defaultValue = "guest") String user
   ```

---
🔹 3. Validation
* Bean validation with javax.validation (@Valid, @NotNull, @Size, etc.)
* BindingResult for validation errors
* Global error handling with @ControllerAdvice


### 3. Validation

#### Bean validation with javax.validation

- Use annotations like `@Valid`, `@NotNull`, `@Size`, `@Min`, `@Max`.
- Works with `javax.validation.constraints` and Hibernate Validator.

```java
public class User {
    @NotNull(message = "Username cannot be null")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
```

```java
@PostMapping("/users")
public ResponseEntity<String> createUser(@Valid @RequestBody User user, BindingResult result) {
    if (result.hasErrors()) {
        return ResponseEntity.badRequest().body("Validation failed");
    }
    return ResponseEntity.ok("User created");
}
```

#### BindingResult for Validation Errors

- Always use it **immediately after** `@Valid` or `@Validated`.
- Prevents throwing MethodArgumentNotValidException.

#### Global Error Handling with @ControllerAdvice

- Used to define global exception and error handlers.

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Validation error: " + ex.getMessage());
    }
}
```

---

### Interview Follow-up Questions

1. **What is the difference between @PathVariable and @RequestParam?**

    - `@PathVariable` is used to capture dynamic parts from the URL path.
    - `@RequestParam` is used to extract query parameters.

2. **When would you use ResponseEntity instead of returning plain objects?**

    - When you need to set status codes, headers, or content types manually.

3. **Can you use @RestController and @Controller in the same project?**

    - Yes. They serve different purposes and can coexist.

4. **What happens if both @PathVariable and @RequestParam are used in the same method?**

    - It works fine; you can capture both path and query data in a single method.

5. **How do you handle default values with @RequestParam?**

   ```java
   @RequestParam(defaultValue = "guest") String user
   ```

6. **What is BindingResult and why is it important?**

    - It holds validation errors and allows custom error responses instead of exceptions.

7. **What’s the purpose of @ControllerAdvice?**

    - It provides centralized exception handling across all `@Controller` classes.

---


🔹 4. Content Negotiation
* Returning XML, JSON, or custom format
* produces and consumes attributes
* HttpMessageConverter

### 4. Content Negotiation

#### Returning XML, JSON, or Custom Format

- Spring uses **HttpMessageConverters** to serialize/deserialize request and response bodies.
- By default, Jackson is used for JSON and JAXB for XML.
- Example:

```java
@GetMapping(value = "/product", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public Product getProduct() {
    return new Product("Laptop", 1200);
}
```

#### Produces and Consumes Attributes

- Use `produces` to specify the response content type (e.g., JSON, XML).
- Use `consumes` to specify the expected request content type.

```java
@PostMapping(value = "/product", consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<String> addProduct(@RequestBody Product product) {
    return ResponseEntity.ok("Product added");
}
```

#### HttpMessageConverter

- Converts request/response bodies to and from Java objects.
- You can register custom converters using `WebMvcConfigurer`.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // add custom converters if needed
    }
}
```

---

### Interview Follow-up Questions

1. **What is the difference between @PathVariable and @RequestParam?**

    - `@PathVariable` is used to capture dynamic parts from the URL path.
    - `@RequestParam` is used to extract query parameters.

2. **When would you use ResponseEntity instead of returning plain objects?**

    - When you need to set status codes, headers, or content types manually.

3. **Can you use @RestController and @Controller in the same project?**

    - Yes. They serve different purposes and can coexist.

4. **What happens if both @PathVariable and @RequestParam are used in the same method?**

    - It works fine; you can capture both path and query data in a single method.

5. **How do you handle default values with @RequestParam?**

   ```java
   @RequestParam(defaultValue = "guest") String user
   ```

6. **What is BindingResult and why is it important?**

    - It holds validation errors and allows custom error responses instead of exceptions.

7. **What’s the purpose of @ControllerAdvice?**

    - It provides centralized exception handling across all `@Controller` classes.

8. **How does Spring handle JSON and XML in responses?**

    - Through `HttpMessageConverters`, based on the `Accept` header and `produces` attribute.

9. **What happens if client requests XML but Jackson (for JSON) is only on classpath?**

    - Spring returns 406 Not Acceptable since it can't fulfill the request.

10. **How can you add custom format support like CSV or YAML?**

- Implement and register a custom `HttpMessageConverter`.

---

🔹 5. Exception Handling
* @ExceptionHandler, @ResponseStatus
* Centralized error handling with @ControllerAdvice
* Custom error responses

# 5. Exception Handling in Spring

Exception handling in Spring provides a robust mechanism to manage application errors in a centralized, consistent, and user-friendly way.

---

## 🔹 @ExceptionHandler

- Handles exceptions in specific controller methods.
- Annotated method can return a custom response.
- Can be used at the controller or method level.

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

---

## 🔹 @ResponseStatus

- Used to map an exception class to a specific HTTP status.
- Can be applied directly on a custom exception class.

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

## 🔹 Centralized Error Handling with @ControllerAdvice

- A global error handler that applies to all controllers.
- Ensures uniform exception handling across the application.

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }
}
```

---

## 🔹 Custom Error Responses

- You can return structured error responses using a custom error object.

```java
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private int status;

    // constructor, getters, setters
}
```

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    ErrorResponse response = new ErrorResponse(ex.getMessage(), LocalDateTime.now(), 404);
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
}
```

---

## ✅ Follow-up Interview Questions and Answers

### Q1: What’s the difference between @ExceptionHandler and @ControllerAdvice?
**A:** `@ExceptionHandler` handles exceptions in a specific controller. `@ControllerAdvice` applies globally to all controllers, allowing centralized error handling.

---

### Q2: Can you return different HTTP statuses from an exception handler?
**A:** Yes, by using `ResponseEntity<>` you can return any HTTP status from an exception handler method.

---

### Q3: How does Spring know which exception handler to invoke?
**A:** Spring uses the exception type to map the error to a method annotated with `@ExceptionHandler` that handles that specific type (or its parent).

---

### Q4: What happens if no exception handler is found?
**A:** Spring falls back to the default error handler which returns a generic error page or JSON response, depending on the client.

---

### Q5: Can you combine @ControllerAdvice and @ResponseStatus?
**A:** Yes, you can define a custom exception with `@ResponseStatus`, and still use `@ControllerAdvice` to handle it if more control is needed.

---

🔹 6. Asynchronous MVC
* Returning Callable, DeferredResult, or CompletableFuture from controllers
* Async request processing with thread pools

# 6. Asynchronous MVC in Spring

Spring MVC supports asynchronous request processing to improve scalability by freeing up request-handling threads while the response is being prepared.

---

## 🔹 Async Return Types

### 1. `Callable<T>`
- Allows the controller to return a `Callable` that Spring will execute in a separate thread.
- Frees up the main servlet thread while computation happens.

```java
@GetMapping("/asyncCallable")
public Callable<String> handleAsync() {
    return () -> {
        Thread.sleep(2000);
        return "Callable Response";
    };
}
```

---

### 2. `DeferredResult<T>`
- More flexible than `Callable`. The controller returns a `DeferredResult`, and another thread sets the result later.
- Useful for complex async workflows, webhooks, or polling.

```java
@GetMapping("/deferred")
public DeferredResult<String> handleDeferred() {
    DeferredResult<String> output = new DeferredResult<>();
    Executors.newSingleThreadExecutor().submit(() -> {
        Thread.sleep(3000);
        output.setResult("Deferred Result Response");
    });
    return output;
}
```

---

### 3. `CompletableFuture<T>` (Spring 4.2+)
- Modern, powerful async support using Java 8's `CompletableFuture`.

```java
@GetMapping("/completable")
public CompletableFuture<String> handleCompletable() {
    return CompletableFuture.supplyAsync(() -> {
        Thread.sleep(1500);
        return "CompletableFuture Response";
    });
}
```

---

## 🔹 Thread Pool Configuration

Spring uses a `TaskExecutor` to manage async requests.

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "mvcTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(25);
        executor.initialize();
        return executor;
    }
}
```

To set this for Spring MVC async processing:
```yaml
spring:
  mvc:
    async:
      request-timeout: 5000
```

---

## 🔹 When to Use Async MVC

- Long-running I/O operations (e.g., DB, REST APIs)
- Streaming real-time data
- Polling mechanisms
- Push-based systems (e.g., SSE)

---

## 🔹 Benefits

- Increases throughput by avoiding blocking request threads.
- Frees servlet thread during long-running operations.
- Better resource utilization under heavy load.

---

## 🔹 Limitations

- Not useful for CPU-bound tasks.
- Exception handling and timeouts can be complex.
- Debugging can be harder.

---

## 🧠 Interview Follow-up Questions

### Q1: What is the difference between Callable and DeferredResult?
**A:**  
`Callable` returns a value asynchronously and is executed by Spring in a separate thread.  
`DeferredResult` allows more control — you can decide when and how to set the result, often in response to other external events.

---

### Q2: When would you prefer `CompletableFuture` over `Callable`?
**A:**  
Use `CompletableFuture` for chaining multiple async operations or for more functional-style programming with `thenApply`, `handle`, etc. It also integrates better with reactive programming models.

---

### Q3: What happens if the task does not complete in time?
**A:**  
Spring MVC has a configurable timeout (`spring.mvc.async.request-timeout`). If the task exceeds this, the request is terminated and a timeout response is returned (default HTTP 503).

---

### Q4: How does Spring internally manage threads for async processing?
**A:**  
Spring delegates async execution to a `TaskExecutor` (typically a `ThreadPoolTaskExecutor`). You can configure this in your context or let Spring use the default one.

---

### Q5: Can we use @Async with Spring MVC Controllers?
**A:**  
`@Async` is typically used for fire-and-forget background tasks, not for returning values directly to the client. For controller-level async, `Callable`, `DeferredResult`, and `CompletableFuture` are preferred.

---

## ✅ Summary

| Technique           | When to Use                              | Key Benefit                         |
|---------------------|------------------------------------------|-------------------------------------|
| `Callable`          | Simple async computation                 | Easy to implement                   |
| `DeferredResult`    | External/long workflows, polling         | Full control over async lifecycle   |
| `CompletableFuture` | Chained async operations, functional use | Modern, flexible, non-blocking      |

--- 
🔹 1. Spring WebFlux (Reactive Programming)

Useful for high-throughput or streaming-based systems

* Reactive stack (Mono, Flux)
* WebClient (non-blocking HTTP client)
* Backpressure and event loops
* Compare with Spring MVC (imperative)

⸻

🔹 2. Spring Batch (ETL / job scheduling)

Useful for batch processing, scheduled jobs

* Jobs, Steps, Readers, Writers, Processors
* Job parameters and listeners
* Chunk vs Tasklet
* Restartability and transaction management

⸻

🔹 3. Spring Integration / Spring Messaging (Optional but good to know)

Messaging pipelines inside Spring

* Channel, Message, Transformers, Filters
* Integration with Kafka, RabbitMQ

⸻

🔹 4. Spring Boot CLI (Command Line Interface)

Useful for quick prototyping

* Using Groovy with Spring Boot
* Running scripts without full project setup

⸻

🔹 5. Advanced Spring Boot Features
* Custom EnvironmentPostProcessor and ApplicationContextInitializer
* Custom SpringApplicationRunListener
* Overriding auto-configured beans
* Spring Boot Starters (creating your own)

⸻

🔹 6. Spring Shell (CLI for custom applications)

* If you’re building terminal-based tools

⸻

🔹 7. Spring State Machine

Workflow/stateful applications (approval, payments, etc.)

⸻

🔹 8. Spring HATEOAS (Hypermedia REST APIs)

Building REST APIs with hypermedia links

* Building navigable APIs (HAL format)
* Used in REST maturity level 3

⸻

🔹 9. GraphQL with Spring Boot

An alternative to REST, useful in modern APIs

* Query and mutation support
* Schema stitching
* Integration with Spring Security

⸻

🔹 10. Spring Cache Abstraction

Integrate with Redis, Ehcache, etc.

* @Cacheable, @CachePut, @CacheEvict
* Cache manager configuration
* Custom key generation

⸻

🔹 11. Spring Boot Admin UI

Monitor actuator endpoints with visual dashboard

* Application status, health, metrics, logs