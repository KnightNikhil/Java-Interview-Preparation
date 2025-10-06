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
- Conditional annotations in Spring Boot help us **create beans or configurations only if certain  conditions are met**.
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
Spring Boot uses a special mechanism based on:
```
@EnableAutoConfiguration + SpringFactoriesLoader + META-INF/spring.factories
```

| Step | 	  Mechanism	                    | Description                                                 | 
|------|----------------------------------|-------------------------------------------------------------|
| 1	   | @SpringBootApplication	          | Includes @EnableAutoConfiguration                           | 
| 2	   | @EnableAutoConfiguration	        | Imports AutoConfigurationImportSelector                     | 
| 3	   | AutoConfigurationImportSelector	 | Uses SpringFactoriesLoader                                  | 
| 4	   | SpringFactoriesLoader	           | Reads META-INF/spring.factories for all auto-config classes | 
| 5	   | Conditional Beans	               | Loads only those configurations matching the environment    | 
| 6	   | Debug Report	                    | Shows which auto-configs were applied/skipped               | 


**Step 1: The @SpringBootApplication annotation**

When you start your app, you write:
```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

Now, @SpringBootApplication is actually a meta-annotation that includes:
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootConfiguration
@EnableAutoConfiguration  // this is key
@ComponentScan
public @interface SpringBootApplication {}
```
So this triggers @EnableAutoConfiguration.

⸻

**Step 2: Inside @EnableAutoConfiguration**
```java
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration { ... }
```
Here the magic happens 
- It imports a class called AutoConfigurationImportSelector into the Spring context.

⸻

**Step 3: AutoConfigurationImportSelector**

- This class implements the logic that loads all auto-config classes.

- Inside it, you’ll see:
```java
List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
EnableAutoConfiguration.class, classLoader);
```
- This uses the SpringFactoriesLoader utility to load all the class names listed in a specific file:

`META-INF/spring.factories`

⸻

**Step 4: The META-INF/spring.factories file**

- Every Spring Boot starter JAR (like spring-boot-autoconfigure) has this file.

- If you open the file (e.g., in spring-boot-autoconfigure.jar), you’ll see something like:
```
# Auto Configuration Imports
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,\
org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
...
```
These are fully-qualified class names of auto-configuration classes.

⸻

**Step 5: Loading and Applying Configurations**

- Each of these classes is annotated with @Configuration and many @Conditional annotations, for example:
```java
@Configuration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration { ... }
```
So Boot:
1.	Scans the list of auto-configurations.
2.	Checks each condition (@ConditionalOnClass, @ConditionalOnMissingBean, etc.).
3.	Only applies those configurations that match the current classpath and context.

⸻

**Step 6: Final Auto-Configuration Report**

- At runtime, you can see which auto-configurations were applied or skipped:
- Run your app with:

`--debug`

- You’ll see a report in the logs:
```
============================
AUTO-CONFIGURATION REPORT
============================

Positive matches:
-----------------
WebMvcAutoConfiguration matched:
- @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'org.springframework.web.servlet.DispatcherServlet'

Negative matches:
-----------------
JpaRepositoriesAutoConfiguration did not match:
- @ConditionalOnClass classes not found: javax.persistence.EntityManager
```


### Interview Tip:
**Q:** What replaced `spring.factories` in Spring Boot 3?
**A:** `AutoConfiguration.imports` under `META-INF/spring/`

---

## 4. Creating a Custom Auto-Configuration

### Steps
1.	Created a library (starter)
2.	Defined a service (HelloService)
3.	Wrote an auto-config class (HelloAutoConfiguration)
4.	Registered it under META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
5.	Added conditions (@ConditionalOnProperty, etc.)
6.	Used it in a Spring Boot app — no need for manual config!

**Step-by-Step: Creating a Custom Auto-Configuration**

**Step 1: Create a new Maven/Gradle module (a library)**

- You can name it:
`my-spring-boot-starter`

This module will contain:
- 	The HelloService bean
- 	The auto-configuration class
- 	The metadata file (AutoConfiguration.imports)

⸻

**Step 2: Create the service class**
```java
package com.example.hello;

public class HelloService {
private final String message;

    public HelloService(String message) {
        this.message = message;
    }

    public void sayHello() {
        System.out.println("Hello, " + message + "!");
    }
}
```


⸻

**Step 3: Create the Auto-Configuration class**
```java
package com.example.hello.autoconfig;

import com.example.hello.HelloService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "hello.enabled", havingValue = "true", matchIfMissing = true)
public class HelloAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HelloService helloService() {
        return new HelloService("Spring Boot Auto-Configuration");
    }
}
```
Explanation:
- 	@Configuration: marks it as a configuration class.
- 	@ConditionalOnProperty: only loads this if hello.enabled=true in application properties.
- 	@ConditionalOnMissingBean: allows the user to override your bean if they define their own HelloService.

⸻

**Step 4: Register Auto-Configuration in AutoConfiguration.imports**

- Create the file:
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- And add the fully qualified name of your config class:
`com.example.hello.autoconfig.HelloAutoConfiguration`

That’s it. This is how Spring Boot discovers your auto-config.

⸻

🧩 Step 5: Package and Publish

You can:
- 	Build it:
`mvn clean install`
- 	Or publish it to your internal Maven repository.

⸻

🧩 Step 6: Use it in another Spring Boot project

In a separate project (like your actual app), just include the dependency:
```
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```
Then run:
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(DemoApplication.class, args);
        var helloService = ctx.getBean(HelloService.class);
        helloService.sayHello();
    }
}
```
You’ll see:

`Hello, Spring Boot Auto-Configuration!`


⸻

**Step 7: Make It Configurable**

If you want to make it configurable via application.properties:

Create a properties class:
```java
package com.example.hello.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hello")
public class HelloProperties {
private String message = "Spring Boot Auto-Configuration";

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
```
Modify the AutoConfig:

```java
@Configuration
@EnableConfigurationProperties(HelloProperties.class)
@ConditionalOnProperty(name = "hello.enabled", havingValue = "true", matchIfMissing = true)
public class HelloAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HelloService helloService(HelloProperties props) {
        return new HelloService(props.getMessage());
    }
}
```
Now you can control it using:

```
hello.enabled=true
hello.message=World from Spring Boot!
```
Output:
```text
Hello, World from Spring Boot!
```

---

## 5. Excluding Auto-Configurations


**Why exclude auto-configurations?**

- Spring Boot’s auto-configuration mechanism is powerful — it automatically configures beans based on the classpath and environment.
- However, sometimes this can lead to:
  - 	Unwanted configurations being loaded (e.g., DataSource auto-config when you don’t want a DB).
  - 	Conflicts with your custom configuration.
  - 	Performance issues (loading extra beans you don’t need).
That’s where excluding auto-configurations comes in.

**Example: Why It’s Needed**

- Let’s say you have a project that uses MongoDB but not a relational DB.
- Spring Boot sees spring-boot-starter-data-jpa and tries to auto-configure a DataSource.

**Result:**
```
Failed to configure a DataSource: 'url' attribute is not specified
```
**Fix:**
```java
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class MyApp { ... }
```

⸻

**Bonus Tip — Finding What to Exclude**

If you’re not sure which auto-configs are loaded, you can run:
```
java -jar myapp.jar --debug
```
This prints an Auto-Configuration Report (the “CONDITIONS EVALUATION REPORT”) showing:
- 	Which auto-configurations were applied -
- 	Which were excluded or not matched ❌

⸻

### Ways to Exclude Auto-Configuration

| Method	                                | Where Used                 | Example                                                               |
|----------------------------------------|----------------------------|-----------------------------------------------------------------------|
| @SpringBootApplication(exclude = …)	   | In main app class          | 	 @SpringBootApplication(exclude = DataSourceAutoConfiguration.class) |
| @EnableAutoConfiguration(exclude = …)	 | Config classes             | 	Same as above                                                        |
| spring.autoconfigure.exclude           | In application.properties	 | Config-based exclusion                                                |
| SpringApplicationBuilder               | Programmatic config	       | Useful for custom setups                                              |

⸻

**1. Using @SpringBootApplication(exclude = ...)**

This is the most common and recommended way.

Example:
```java
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```
Here, Spring Boot will not auto-configure a DataSource, even if spring-boot-starter-jdbc is on the classpath.

⸻

**2. Using @EnableAutoConfiguration(exclude = ...)**
- You can also use it directly (since @SpringBootApplication itself includes @EnableAutoConfiguration).
```java
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@EnableAutoConfiguration(exclude = { HibernateJpaAutoConfiguration.class })
public class MyConfig {
}
```
This is functionally identical to using the exclude attribute in @SpringBootApplication.

⸻

**3. Using application properties**

You can also exclude auto-configurations declaratively in application.properties or application.yml.
application.properties
```properties
spring.autoconfigure.exclude=\
org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```
application.yml
```yaml
spring:
    autoconfigure:
      exclude:
        - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
        - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```
This is very useful when you can’t modify the code (e.g., in shared modules).

⸻

**4. Using SpringApplicationBuilder**

In programmatic setups or special cases:
```java
new SpringApplicationBuilder(MyApp.class)
    .web(WebApplicationType.NONE)
    .properties("spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
    .run(args);
```

⸻

How It Works Internally
- 	During startup, Spring Boot uses AutoConfigurationImportSelector to load auto-configurations listed in:

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- 	The exclusions you define (in any of the above ways) are checked before configurations are imported.
- 	If a class is listed in exclude, it’s removed from the list of configurations to be imported.


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

# Spring Event Handling with ApplicationEventPublisher

## 1. Overview

Spring's event-driven model allows beans to publish and listen to events synchronously or asynchronously, enabling decoupled communication between components.

## 2. Core Concepts

### ApplicationEventPublisher

Used to publish custom or built-in events.

```java
@Autowired
private ApplicationEventPublisher publisher;

publisher.publishEvent(new MyCustomEvent(this, data));
```

### @EventListener

Used to listen for events in a decoupled way.

```java
@EventListener
public void handleCustomEvent(MyCustomEvent event) {
    // handle event
}
```

### Implementing ApplicationListener

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

### Asynchronous Event Processing

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


# @Conditional Annotations

## Conditional Annotations
- Spring provides conditional annotations to control when a bean should be registered in the context.

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

# Spring Actuator

## What is Spring Actuator?
Spring Boot Actuator provides production-ready features to help you monitor and manage your application. It exposes various REST endpoints to give insights into your app’s internals.

---

## Key Features
- Health checks
- Metrics (JVM, CPU, memory, custom metrics)
- Audit events
- Application environment details
- Thread dumps and HTTP trace logs

---

## Common Actuator Endpoints
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

## Enabling Actuator
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

## Configuring Endpoints
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

## Security for Endpoints
By default, not all actuator endpoints are exposed. We can control which ones are available over the web. It's like choosing what parts of your diary are okay to share.
1. Use **Spring Security** to control access.
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
2. **Use HTTPS instead of HTTP.**
3. **Actuator Role**
- Create a specific role, like ACTUATOR_ADMIN, and assign it to users who should have access. This is like giving a key to only trusted people.


---

## Custom Info Endpoint
Define info in `application.yml`:

```yaml
info:
  app:
    name: My App
    description: Spring Boot App with Actuator
    version: 1.0.0
```

---

## Integrations
- **Micrometer**: For metrics collection and integration with Prometheus, Graphite, etc.
- **Spring Cloud**: For advanced tracing, circuit breakers, etc.

---

## Use Cases
- Monitor application uptime, health, and performance.
- Integrate with alerting/monitoring systems like Prometheus and Grafana.
- View logs and trace issues dynamically.

---

## Interview Follow-up Questions

### Q1: How can you restrict access to actuator endpoints?
**A:** Use Spring Security, configure endpoint exposure, and apply roles.

### Q2: How do you create a custom actuator endpoint?
**A:** Use `@Endpoint`, `@ReadOperation`, `@WriteOperation` annotations.

### Q3: How does Actuator relate to Micrometer?
**A:** Micrometer is the metrics collection facade used by Actuator to expose metrics in a vendor-neutral way.

### Q4: Can actuator endpoints be used in production?
**A:** Yes, but they should be secured and carefully exposed.

---


# Spring MVC Core (Web Layer in Spring)

## 1. Spring MVC Architecture

Spring MVC follows the **Front Controller pattern**, where a single servlet (`DispatcherServlet`) handles all incoming requests and delegates them to appropriate handlers (controllers).


## Key Components

### DispatcherServlet
- Acts as the **front controller**.
- Responsible for receiving HTTP requests and routing them to appropriate components.
- Configured automatically by Spring Boot via `@SpringBootApplication`.

### HandlerMapping
- Maps incoming requests to handler methods (controllers).
- Based on annotations like `@RequestMapping`, `@GetMapping`, `@PostMapping`, etc.
- Examples: `RequestMappingHandlerMapping`, `SimpleUrlHandlerMapping`.

### Controller
- Annotated with `@Controller` or `@RestController`.
- Contains methods that handle HTTP requests and return model data or response bodies.
- Can return a `ModelAndView`, a String view name, or JSON (in case of `@RestController`).

### ViewResolver
- Responsible for resolving the logical view names returned by controllers to actual views (like JSP, Thymeleaf, etc.).
- Example: `InternalResourceViewResolver`, `ThymeleafViewResolver`.


## Request Flow Diagram
```
Client → DispatcherServlet
       → HandlerMapping
       → Controller
       → ViewResolver
       → View
       → Response
```

---

## Front Controller Pattern

- Centralizes control of request handling.
- Improves maintainability and scalability.
- Allows pre/post-processing via interceptors and filters.


## Example

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

## Interview Follow-Up Questions

### Q. What is the role of DispatcherServlet in Spring MVC?
> It's the front controller that routes requests to appropriate controllers.

### Q. What is the difference between `@Controller` and `@RestController`?
> `@RestController` is a combination of `@Controller` and `@ResponseBody`, used for REST APIs.

### Q. Can we have multiple ViewResolvers?
> Yes. Spring supports ordering them using `setOrder()`.

### Q. How does Spring resolve a view name?
> It uses `ViewResolver` to convert the view name to an actual view resource.

---


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

6. **What is BindingResult and why is it important?**

  - It holds validation errors and allows custom error responses instead of exceptions.

7. **What’s the purpose of @ControllerAdvice?**

  - It provides centralized exception handling across all `@Controller` classes.

---

# Validation

**1. Big picture (what & why)**

Bean Validation (JSR 303/349/380 → now Jakarta Validation) is the standard API for declarative constraint-based validation in Java.
-	Purpose: validate POJOs (beans), method parameters/returns, container elements.
-	Reference implementation: Hibernate Validator (most apps use it; Spring Boot brings it in by default).
-	Two modes:
  -	Declarative via annotations (@NotNull, @Size, etc.)
  -	Programmatic via Validator API

Note: namespace changed: older projects use javax.validation.*; recent Jakarta EE / Spring Boot 3+ uses jakarta.validation.*. Concepts are the same.

⸻

**2. Core annotations (common, know these)**
-	@NotNull — value must not be null.
-	@NotEmpty — for CharSequence/Collection/Map/Array: size > 0 (not null & not empty).
-	@NotBlank — for Strings: not null, trimmed length > 0 (rejects whitespace).
-	@Size(min=..., max=...) — for String/Collection/Array length.
-	@Min, @Max — numeric bounds (long-based).
-	@Positive, @PositiveOrZero, @Negative, @NegativeOrZero.
-	@Email — email format (note: not full RFC validation).
-	@Pattern(regexp=...) — regex match.
-	@Past, @Future — dates/times.
-	@Valid — cascade validation into nested object/collection elements.
-	@AssertTrue, @AssertFalse — boolean checks.
-	@Null — value must be null.

Container/Type-use constraints (Java 8+): List<@NotNull String> to validate container elements.

⸻

**3. Validation API essentials (programmatic)**
```java
import javax.validation.*;
import java.util.Set;

ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
Validator validator = factory.getValidator();

MyBean bean = new MyBean(...);
Set<ConstraintViolation<MyBean>> violations = validator.validate(bean);
for (ConstraintViolation<MyBean> v : violations) {
    System.out.println(v.getPropertyPath() + " " + v.getMessage());
}
```

ConstraintViolation gives: getMessage(), getPropertyPath(), getInvalidValue().

⸻

**4. Spring Boot integration (most important for interviews)**

1. Validating request body in controllers

```java
@PostMapping("/users")
public ResponseEntity<?> create(@Valid @RequestBody UserDto dto, BindingResult br) {
if (br.hasErrors()) { ... } // or let exception handler handle it
// proceed
}
```
-	@Valid triggers bean validation on the request body.
-	Spring handles errors as MethodArgumentNotValidException (for @RequestBody) or BindException (for form binding).

2. Validating path/params / method params
-	Use @Validated on the controller/class or configuration to enable method-level validation for simple types:
```java
@Validated
@RestController
public class C {
@GetMapping("/items/{id}")
public Item get(@PathVariable @Min(1) Long id) { ... }
}
```
-	For service-layer method validation (method param/return), enable MethodValidationPostProcessor (Spring auto-configures it if you include dependency), annotate service with @Validated.

⸻

**5. Cascading validation**
```java
public class Order {
  @Valid
  private Customer customer;
  @Valid
  private List<@NotNull Item> items;
}
```
@Valid on a field causes the validator to validate nested object(s).

⸻

**6. Custom constraint (class + validator)**

Annotation
```java
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
  String message() default "Passwords don't match";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
```
Validator
```java
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserDto> {
@Override
public boolean isValid(UserDto dto, ConstraintValidatorContext ctx) {
    if (dto == null) return true; // keep null-check for @NotNull elsewhere
    return Objects.equals(dto.getPassword(), dto.getConfirmPassword());
}
}
```
Apply as class-level: @PasswordMatches on UserDto.

⸻

**7. Cross-field/class-level validation**

Used when a constraint depends on multiple fields (password match, startDate < endDate). Use type-level annotation + validator (see above).

⸻

**8. Constraint composition & payload & groups**
-	Composition: create annotations that combine other constraints (meta-annotations).
-	Payload: carry metadata for clients (rarely used).
-	Groups: support different validation sequences (e.g., @Validated(Create.class) vs @Validated(Update.class)).
```java
public interface Create {}
public interface Update {}

@NotNull(groups = Create.class)
private Long id;
```
Use group sequences to enforce order.

⸻

**9. Method validation (JSR-349/380)**
-	Bean Validation supports method parameter and return value validation via @Validated and AOP proxying.
-	Typical usage in services:
```java
@Validated
@Service
public class MyService {
    public void create(@NotNull @Size(min=3) String name) { ... }
}
```
If violated, a ConstraintViolationException is thrown.

⸻

**10. Message interpolation & i18n**
-	Default messages come from annotation message attribute, e.g. @Size(message = "must be between {min} and {max}").
-	To internationalize: create ValidationMessages.properties (and localized variants) in classpath; use message keys in annotations: @NotNull(message = "{user.name.notnull}").
-	Messages support placeholders like {validatedValue}, {min}, etc.

⸻

**11. Exception handling in Spring (best practice)**
-	@ControllerAdvice to handle:
-	MethodArgumentNotValidException (body validation) → extract BindingResult.getFieldErrors() to return field-specific messages.
-	ConstraintViolationException (method param/service layer) → map ConstraintViolation to field/param messages.
Example error DTO:
```JSON
{
"timestamp":"...",
"status":400,
"errors":[ {"field":"email","message":"must be a well-formed email address"} ]
}
``` 

⸻

**12. Fail-fast vs default**

Hibernate Validator supports fail-fast mode:

hibernate.validator.fail_fast=true

Fail-fast makes validation stop at first violation (faster but less info). Default behavior collects all violations.

⸻

**13. Testing validation**
-	Unit-test validators with Validator programmatic API.
-	Controller tests: use MockMvc and send invalid DTOs; assert error payload and status.
-	Test custom constraints in isolation.

⸻

**14. Common pitfalls & clarifications (interview traps)**
-	@NotNull vs @NotEmpty vs @NotBlank — know differences.
-	@Valid vs @Validated:
-	@Valid (javax/ jakarta) is Bean Validation annotation used to trigger cascade on nested objects (works on method params in some contexts).
-	@Validated (Spring) enables validation groups and method-level validation via AOP.
-	Primitive types can’t be null. Use wrapper types if nullability matters.
-	@Email is not RFC 100% spec — may accept invalid addresses; for full validation use stricter logic.
-	volatile/concurrency & validation? unrelated — don’t mix up.
-	Message interpolation placeholders and using ValidationMessages.properties.
-	@Valid is needed to validate elements in collections nested inside a bean; also use container element constraints for List<@NotNull Foo>.

⸻

15. Quick code cheatsheet

DTO with annotations
```java
public class UserDto {
@NotNull
private Long id;

    @NotBlank
    private String name;

    @Email
    private String email;

    @Size(min=8)
    private String password;
    // getters/setters
}


```

Controller
```java
@PostMapping("/users")
public ResponseEntity<Void> create(@Valid @RequestBody UserDto dto) { ... }
```

Programmatic
```java
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
Set<ConstraintViolation<UserDto>> v = validator.validate(dto);
```

⸻

**Interview-style Qs & short model answers**
1.	Q: Difference @NotNull, @NotEmpty, @NotBlank?
A: @NotNull forbids null; @NotEmpty forbids null or empty (for String/Collection); @NotBlank forbids null/empty/whitespace (for String).
2.	Q: How to validate nested objects?
A: Use @Valid on the nested field or container element constraints for collections.
3.	Q: How to validate method parameters in service layer?
A: Use Bean Validation method validation with @Validated on the bean and ensure MethodValidationPostProcessor is enabled (Spring Boot auto-configs it). Violations throw ConstraintViolationException.
4.	Q: How to create a custom validator?
A: Create @interface annotated with @Constraint(validatedBy=...), then implement ConstraintValidator<A,T>.
5.	Q: How to localize messages?
A: Put message keys in ValidationMessages.properties and use {key} in message attribute.
6.	Q: What is fail-fast?
A: Stop validation at the first constraint violation (configured via Hibernate Validator property).
7.	Q: When to use groups?
A: When you need different validation rules for different operations (e.g., create vs update). Use @Validated(Group.class).

⸻

**Best practices (what to say in interviews)**
-	Prefer declarative bean validation for DTOs and simple rules.
-	Use @Valid to cascade and @Validated for groups/method validation.
-	Keep validation logic simple; for complex business rules, implement in service layer (and throw meaningful exceptions).
-	Return structured error responses (field -> message).
-	Avoid putting heavy logic inside custom validators (they should be fast).
-	Test custom validators thoroughly and include i18n.

---

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


1. **How does Spring handle JSON and XML in responses?**

    - Through `HttpMessageConverters`, based on the `Accept` header and `produces` attribute.

2. **What happens if client requests XML but Jackson (for JSON) is only on classpath?**

    - Spring returns 406 Not Acceptable since it can't fulfill the request.
   
3. **How can you add custom format support like CSV or YAML?**

- Implement and register a custom `HttpMessageConverter`.

---

# 5. Exception Handling in Spring

Exception handling in Spring provides a robust mechanism to manage application errors in a centralized, consistent, and user-friendly way.

---

# @ExceptionHandler


## What is @ExceptionHandler?

@ExceptionHandler is an annotation used in Spring MVC / Spring Boot to handle exceptions thrown during request processing inside a controller.

- In simple words: It allows you to catch specific exceptions (like NullPointerException, UserNotFoundException, etc.) and define custom responses instead of returning the default Spring error page or stack trace.

⸻

**Basic Example**
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        if (id == 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return new User(id, "John");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>("Error: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
```
**What happens here:**
1.	You hit /users/0.
2.	IllegalArgumentException is thrown.
3.	Instead of an ugly 500 error page, Spring looks inside the same controller.
4.	It finds a method annotated with @ExceptionHandler(IllegalArgumentException.class).
5.	It calls that method and returns your custom ResponseEntity.

⸻

**How Spring Handles It Internally**

When an exception is thrown in a controller method:
1.	The DispatcherServlet catches it.
2.	It checks if the same controller has a method annotated with @ExceptionHandler for that exception (or a superclass).
3.	If found → that method is executed.
4.	If not found → Spring checks global handlers defined via @ControllerAdvice.
5.	If still not found → Default error handling applies (Spring Boot’s BasicErrorController).

⸻

**Multiple Exception Handlers in One Controller**

You can handle multiple exception types easily.
```java
@RestController
public class DemoController {

    @GetMapping("/demo")
    public String demo() {
        throw new NullPointerException("Something went null!");
    }

    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleCommonExceptions(Exception ex) {
        return new ResponseEntity<>("Handled: " + ex.getClass().getSimpleName(), HttpStatus.BAD_REQUEST);
    }
}
```

⸻

**Handling Exception Hierarchies**

Spring always picks the most specific handler first.

Example:
```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<String> handleRuntime(RuntimeException ex) { ... }

@ExceptionHandler(NullPointerException.class)
public ResponseEntity<String> handleNPE(NullPointerException ex) { ... }
```
If a NullPointerException is thrown, Spring will call handleNPE() (since it’s more specific), not handleRuntime().

⸻

**Returning JSON Responses (Best Practice for REST APIs)**

Instead of raw strings, it’s best to return structured error responses:
```java
@RestController
public class ProductController {

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable int id) {
        if (id <= 0) throw new ProductNotFoundException("Invalid product ID");
        return new Product(id, "Laptop");
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Product Not Found");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```
Output:
```json
{
"timestamp": "2025-10-05T21:34:10",
"status": 404,
"error": "Product Not Found",
"message": "Invalid product ID"
}
```

⸻

Key Rules

| Rule   | 	Explanation                                                                             |
|--------|------------------------------------------------------------------------------------------|
| 1.	    | @ExceptionHandler methods can be in the same controller or in a global @ControllerAdvice |
| 2.	    | You can handle multiple exception classes in one method using {}                         |
| 3.     | The return type can be ResponseEntity, ModelAndView, or any object (for REST → JSON)     |
| 4.	    | You can access HttpServletRequest, WebRequest, or HttpServletResponse as method params   |
| 5.	    | Spring picks the closest matching exception type automatically                           |


⸻

Example: Centralized vs Local Handling

- **Local:**

Inside a single controller, handles only exceptions from that controller.

- Global:

Using @ControllerAdvice, you can centralize all @ExceptionHandlers.
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        return new ResponseEntity<>("Global: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
Now any controller in the project throwing an exception will be handled here.

⸻

**Common Interview Questions & Answers**

1. What is @ExceptionHandler?	Used to handle specific exceptions thrown during controller execution.
2. What is the return type of @ExceptionHandler?	Can be any type; ResponseEntity is most common in REST APIs.
3. Can one method handle multiple exceptions?	Yes, by passing an array of classes — e.g. @ExceptionHandler({A.class, B.class}).
4. What’s difference between @ExceptionHandler and @ControllerAdvice?	@ExceptionHandler = local to one controller. @ControllerAdvice = global across all controllers.
5. What if no matching handler is found?	Spring falls back to default error handling (BasicErrorController).
6. Can you access the request info in handler?	Yes, add HttpServletRequest request or WebRequest as parameters.
7. Order of precedence?	Local @ExceptionHandler in controller → Global @ControllerAdvice → Default Spring handler.


⸻

Example with Multiple Exception Types (Real-World)
```java
@RestController
@RequestMapping("/api")
public class EmployeeController {

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        if (id == 0)
            throw new IllegalArgumentException("ID cannot be zero");
        if (id < 0)
            throw new EmployeeNotFoundException("Employee not found for ID " + id);
        return new Employee(id, "Nikhil");
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EmployeeNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
```
- Clean separation of logic
- Meaningful HTTP responses
- No duplication in controllers



---

## @ResponseStatus

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

## Centralized Error Handling with @ControllerAdvice

**What is @ControllerAdvice?**

- @ControllerAdvice is a specialized Spring annotation used to handle cross-cutting concerns across all controllers in your application — most commonly exception handling, data binding, and model attributes.

- Think of it like a “global interceptor” for controllers —
- Instead of writing duplicate exception handling logic in every controller, you define it once in a class annotated with @ControllerAdvice.

⸻

**Key Roles of @ControllerAdvice**

| Responsibility	            | Description                                                                    | 
|----------------------------|--------------------------------------------------------------------------------|
| Global Exception Handling	 | Catch and handle exceptions thrown by any controller in one centralized place. | 
| Global Data Binding	       | Customize how data is bound to objects globally.                               | 
| Global Model Attributes	   | Add common data (like appVersion, userInfo) to all responses automatically.    | 


⸻

**Basic Structure**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>("Something went wrong: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
How It Works:
-	Any exception thrown in any controller method will be intercepted here.
-	Spring Boot will check if there is a method with a matching @ExceptionHandler for that exception type.
-	If found, it executes that method and returns the response.

⸻

**1. Exception Handling with @ControllerAdvice**

Example:
```java
@RestControllerAdvice // Shortcut for @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Handle custom exceptions
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("Unexpected error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
Notes:
-	@RestControllerAdvice is equivalent to @ControllerAdvice + @ResponseBody.
-	It ensures all methods return JSON instead of a view.
-	You can define multiple handlers for different exception classes.

⸻

**2. Scope Control (Target Specific Controllers)**

By default, @ControllerAdvice applies to all controllers, but you can narrow its scope using one of these filters:

```java 
@ControllerAdvice(basePackages = "com.example.api.controller")
```

- Applies only to controllers inside that package.
```java
@ControllerAdvice(assignableTypes = {UserController.class, OrderController.class})
```
- Applies only to specific controllers.
```java
@ControllerAdvice(annotations = RestController.class)
```
- Applies only to controllers annotated with @RestController.

⸻

3. Adding Common Data (@ModelAttribute)

You can add attributes that should be available to all controllers.
```java
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("appVersion")
    public String getAppVersion() {
        return "v1.0.3";
    }
}
```
Every controller now automatically receives a model attribute appVersion.

⸻

4. Global Data Binding (@InitBinder)

You can customize how form/request data is converted to Java objects globally.
```java
@ControllerAdvice
public class GlobalBindingAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Trims whitespace from all String inputs
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
```
This ensures input sanitization for all controllers automatically.

⸻

Difference Between @ControllerAdvice and @ExceptionHandler (Inside Controller)

| Aspect	   | @ExceptionHandler in Controller	             | @ControllerAdvice                                  |
|-----------|----------------------------------------------|----------------------------------------------------|
| Scope	    | Handles exceptions for one controller only	  | Handles exceptions globally across all controllers |
| Usage	    | Localized error handling	                    | Centralized error handling                         |
| Best for	 | Small projects or specific controller logic	 | Large applications needing consistency             |


⸻

**Interview Tip**

Q: How does Spring know which method to call in @ControllerAdvice?
- Spring maintains a hierarchy: it looks for a matching @ExceptionHandler method for the thrown exception (or its superclass). If multiple match, it picks the most specific one.

⸻

**Real-World Example**

Imagine a REST API with endpoints for patients:
```java
@RestController
@RequestMapping("/patients")
public class PatientController {

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }
}
```
Now, if getPatientById throws PatientNotFoundException, you don’t want every controller to handle it.

So you define:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<String> handlePatientNotFound(PatientNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception ex) {
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
- Cleaner
- Reusable
- Consistent error responses across controllers

⸻

- Summary Table

| Feature	                   | Annotation	                                 | Description                           |
|----------------------------|---------------------------------------------|---------------------------------------|
| Global exception handling	 | @ExceptionHandler	                          | Handles exceptions globally           |
| Add common model data	     | @ModelAttribute	                            | Adds shared attributes                |
| Customize data binding	    | @InitBinder	                                | Modifies data conversion rules        |
| REST-style response	       | @RestControllerAdvice	                      | Returns JSON responses automatically  |
| Scoped advice	             | basePackages, assignableTypes, annotations	 | Limits advice to specific controllers |



---

## Custom Error Responses

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

## Follow-up Interview Questions and Answers

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

6. Asynchronous MVC
* Returning Callable, DeferredResult, or CompletableFuture from controllers
* Async request processing with thread pools

# 6. Asynchronous MVC in Spring

Spring MVC supports asynchronous request processing to improve scalability by freeing up request-handling threads while the response is being prepared.

---

## Async Return Types

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

## Thread Pool Configuration

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

## When to Use Async MVC

- Long-running I/O operations (e.g., DB, REST APIs)
- Streaming real-time data
- Polling mechanisms
- Push-based systems (e.g., SSE)

---

## Benefits

- Increases throughput by avoiding blocking request threads.
- Frees servlet thread during long-running operations.
- Better resource utilization under heavy load.

---

## Limitations

- Not useful for CPU-bound tasks.
- Exception handling and timeouts can be complex.
- Debugging can be harder.

---

## Interview Follow-up Questions

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

## Summary

| Technique           | When to Use                              | Key Benefit                         |
|---------------------|------------------------------------------|-------------------------------------|
| `Callable`          | Simple async computation                 | Easy to implement                   |
| `DeferredResult`    | External/long workflows, polling         | Full control over async lifecycle   |
| `CompletableFuture` | Chained async operations, functional use | Modern, flexible, non-blocking      |

--- 
1. Spring WebFlux (Reactive Programming)

Useful for high-throughput or streaming-based systems

* Reactive stack (Mono, Flux)
* WebClient (non-blocking HTTP client)
* Backpressure and event loops
* Compare with Spring MVC (imperative)

⸻

2. Spring Batch (ETL / job scheduling)

Useful for batch processing, scheduled jobs

* Jobs, Steps, Readers, Writers, Processors
* Job parameters and listeners
* Chunk vs Tasklet
* Restartability and transaction management

⸻

3. Spring Integration / Spring Messaging (Optional but good to know)

Messaging pipelines inside Spring

* Channel, Message, Transformers, Filters
* Integration with Kafka, RabbitMQ

⸻

4. Spring Boot CLI (Command Line Interface)

Useful for quick prototyping

* Using Groovy with Spring Boot
* Running scripts without full project setup

⸻

5. Advanced Spring Boot Features
* Custom EnvironmentPostProcessor and ApplicationContextInitializer
* Custom SpringApplicationRunListener
* Overriding auto-configured beans
* Spring Boot Starters (creating your own)

⸻

6. Spring Shell (CLI for custom applications)

* If you’re building terminal-based tools

⸻

7. Spring State Machine

Workflow/stateful applications (approval, payments, etc.)

⸻

8. Spring HATEOAS (Hypermedia REST APIs)

Building REST APIs with hypermedia links

* Building navigable APIs (HAL format)
* Used in REST maturity level 3

⸻

11. Spring Boot Admin UI

Monitor actuator endpoints with visual dashboard

* Application status, health, metrics, logs