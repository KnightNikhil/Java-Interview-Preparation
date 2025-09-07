✅ Spring Boot Topics You’ve Covered

🔹 1. Spring Boot Fundamentals
* @SpringBootApplication annotation
* Embedded Tomcat
* Starter dependencies
* Application configuration using application.yml / .properties
* Profiles (@Profile, spring.profiles.active)
* Externalized configuration

⸻

🔹 2. Auto-Configuration (Deep Dive)
* @EnableAutoConfiguration
* Internals: spring.factories, META-INF, conditional annotations
* @ConditionalOnClass, @ConditionalOnMissingBean, @ConditionalOnProperty
* Creating custom auto-configuration

⸻

🔹 3. Spring Boot DevTools
* Hot reloading
* Dev-only beans
* Conditional development tools

# Spring Boot DevTools

Spring Boot DevTools is a set of tools aimed at improving the developer experience by providing features like hot reloading, automatic restarts, and conditional configurations specifically for development environments.

---

## 🔹 1. Hot Reloading

### 🔸 What is it?

Hot reloading allows developers to see changes in the application without restarting the server manually.

### 🔸 How it works:

- Spring Boot DevTools automatically monitors classpath resources.
- When a change is detected (e.g., in `.java`, `.properties`, or `.html` files), the application restarts automatically.
- It uses two classloaders to speed up restart:
    - **Base Classloader**: loads third-party jars.
    - **Restart Classloader**: loads your own classes and resources.

### 🔸 Tools integration:

- Works well with IDEs like IntelliJ IDEA, Eclipse, and Spring Tool Suite.
- Can be enhanced with LiveReload browser extensions for real-time frontend refresh.

### 🔸 Limitation:

- Doesn’t support hot reloading for static files in some IDEs unless properly configured.

---

## 🔹 2. Dev-Only Beans

### 🔸 Purpose:

You may want certain beans to only load in the development environment (like database seeders, debugging tools, etc).

### 🔸 How to create Dev-only beans:

```java
@Configuration
@Profile("dev")
public class DevOnlyConfig {
    @Bean
    public CommandLineRunner devDataSeeder() {
        return args -> {
            System.out.println("Seeding data only in dev profile...");
        };
    }
}
```

---

## 🔹 3. Conditional Development Tools

### 🔸 DevTools-specific conditions:

Spring Boot provides conditional annotations such as `@ConditionalOnProperty`, `@ConditionalOnClass`, and more. These can be used to conditionally enable certain configurations or beans only in the development context.

### 🔸 Examples:

```java
@Configuration
@ConditionalOnProperty(name = "spring.devtools.restart.enabled", havingValue = "true")
public class RestartConfig {
    // Configuration here only enabled when devtools restart is enabled
}
```

---

## 🔹 How to Enable DevTools

### Maven:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Gradle:

```groovy
dependencies {
    developmentOnly "org.springframework.boot:spring-boot-devtools"
}
```

---

## 🔹 Interview Follow-up Questions and Answers

### Q1: What is the purpose of DevTools in Spring Boot?

**A:** DevTools helps enhance the developer experience by enabling automatic restarts, hot reloading, and profile-based bean loading to reduce development turnaround time.

### Q2: How does hot reloading work in DevTools?

**A:** It uses classpath monitoring and restarts the application when code changes are detected using separate classloaders for third-party and project-specific code.

### Q3: How do you configure Dev-only beans?

**A:** Use the `@Profile("dev")` annotation on the configuration class or bean method.

### Q4: Can DevTools be used in production?

**A:** No. DevTools is meant for local development only and is disabled in production by default.

---

## 🔹 Use Cases

- Automatically reload changes during development
- Enable debug-only tools or seeders
- Skip certain beans/configs in production

---
⸻

🔹 4. Actuator
* Enabling and exposing endpoints
* Health, metrics, info, env, mappings, loggers, etc.
* Securing actuator endpoints
* Custom health indicators
* Integration with Prometheus/ELK/Zipkin

# Spring Boot Actuator

Spring Boot Actuator provides production-ready features that help you monitor and manage your application.

---

## ✅ Enabling and Exposing Endpoints

Spring Boot Actuator endpoints are enabled by adding the dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Then configure which endpoints to expose in `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics,env,loggers,mappings
```

---

## ✅ Key Actuator Endpoints

- `/actuator/health`: Shows application health (customizable)
- `/actuator/info`: Application info from `application.properties`
- `/actuator/metrics`: Application and system metrics
- `/actuator/env`: Environment properties
- `/actuator/mappings`: All request mappings
- `/actuator/loggers`: Log levels configuration at runtime

---

## ✅ Securing Actuator Endpoints

Secure endpoints using Spring Security:

```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
spring.security.user.name=admin
spring.security.user.password=secret
```

Restrict access to certain roles via Java Config.

---

## ✅ Custom Health Indicators

Create custom health checks by implementing the `HealthIndicator` interface:

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        boolean healthy = checkMySystemHealth();
        if (healthy) {
            return Health.up().withDetail("status", "All good").build();
        }
        return Health.down().withDetail("status", "Something wrong").build();
    }
}
```

---

## ✅ Integration with Monitoring Systems

Spring Boot Actuator integrates with:

- **Prometheus**: via `micrometer-registry-prometheus`
- **Zipkin**: for distributed tracing via Spring Cloud Sleuth
- **ELK Stack**: Forward logs via Logstash for centralized logging

---

## 🎯 Follow-up Interview Questions

### Q1. How do you expose only specific actuator endpoints?
**A:** Using `management.endpoints.web.exposure.include` or `exclude` properties.

### Q2. How do you implement a custom health indicator?
**A:** Implement the `HealthIndicator` interface and override `health()`.

### Q3. How can you change log level of a class at runtime?
**A:** Use `/actuator/loggers/{logger.name}` endpoint with a POST request.

### Q4. What's the role of Micrometer in Spring Boot?
**A:** It acts as a metrics facade supporting multiple monitoring systems like Prometheus, Datadog, etc.

---

✅ Actuator improves observability, and with proper security and integrations, it becomes essential for microservices in production.

⸻

🔹 5. Spring Boot Testing
* @SpringBootTest, @DataJpaTest, @WebMvcTest
* Test slicing
* Test configuration
* Integration tests and H2
* Testcontainers awareness

⸻

🔹 6. Spring Boot with Spring Ecosystem

You’ve integrated Spring Boot with:
* ✅ Spring MVC
* ✅ Spring Data JPA
* ✅ Spring Security
* ✅ Spring AOP
* ✅ Spring Events
* ✅ Spring Cache (covered conceptually)
* ✅ Spring Cloud (Config Server, Eureka, Gateway, Feign, Resilience4j)

⸻

🔹 7. Spring Boot Configuration Tricks
* Configuration properties using @ConfigurationProperties
* Property validation
* Profiles per environment
* EnvironmentPostProcessor
* Dynamic property refresh with Spring Cloud Config


# Spring Boot Configuration Tricks

This guide covers advanced techniques and features for managing configuration in Spring Boot applications.

---

## ✅ Configuration Properties with `@ConfigurationProperties`

- Use `@ConfigurationProperties` to bind external configuration properties to Java objects.
- Works well for grouping related properties.
- Example:

```java
@ConfigurationProperties(prefix = "app.datasource")
public class DataSourceConfig {
    private String url;
    private String username;
    private String password;
    // getters and setters
}
```

- Register with `@EnableConfigurationProperties(DataSourceConfig.class)` or mark class with `@Component`.

### Follow-up Interview Questions:
1. What’s the advantage of using `@ConfigurationProperties` over `@Value`?
2. How can you ensure immutability in configuration classes?

---

## ✅ Property Validation

- Spring Boot supports JSR-303 Bean Validation on configuration properties.
- Add validation annotations to the fields:

```java
@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfig {
    @NotEmpty
    private String name;
    @Min(1024)
    private int port;
}
```

### Follow-up Interview Questions:
1. How do you handle configuration validation failures at startup?
2. Can you add custom validators for configuration properties?

---

## ✅ Profiles per Environment

- Spring Boot allows defining profiles such as `dev`, `test`, `prod`.
- Create `application-{profile}.yml` or `.properties` files.

### Ways to activate profiles:

1. **Via application arguments:**  
   `--spring.profiles.active=prod`

2. **Via environment variable:**  
   `SPRING_PROFILES_ACTIVE=prod`

3. **Via JVM argument:**  
   `-Dspring.profiles.active=prod`

### Follow-up Interview Questions:
1. How do you handle overlapping properties between default and profile-specific files?
2. What happens if the profile-specific file is missing?

---

## ✅ EnvironmentPostProcessor

- A hook to customize the environment before the application starts.
- Implement `EnvironmentPostProcessor` interface and register via `META-INF/spring.factories`.

```java
public class CustomEnvPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        // manipulate env or load custom properties
    }
}
```

### Follow-up Interview Questions:
1. What’s the difference between `ApplicationContextInitializer` and `EnvironmentPostProcessor`?
2. Where can you use this in real-world applications?

---

## ✅ Dynamic Property Refresh (Spring Cloud Config)

- Use Spring Cloud Config for centralized external configuration.
- Enable property refresh with actuator and `@RefreshScope`:

```java
@RefreshScope
@ConfigurationProperties("app.feature")
public class FeatureProperties {
    private boolean enabled;
}
```

- Trigger refresh via actuator endpoint:
  `POST /actuator/refresh`

### Follow-up Interview Questions:
1. How does Spring Cloud Config work with Git-based config repos?
2. What are the limitations of `@RefreshScope`?
3. Can all bean properties be refreshed at runtime?

---

## Summary

| Feature                     | Use Case                               |
|----------------------------|----------------------------------------|
| @ConfigurationProperties   | Type-safe config binding               |
| Property validation        | Catch misconfigs early                 |
| Profiles                   | Env-specific config                    |
| EnvironmentPostProcessor   | Pre-process env before app starts      |
| Spring Cloud Config        | Centralized and refreshable configs    |
⸻

🔹 8. Spring Boot Logging
* Logback config
* Log level per profile
* Structured logging
* Log correlation with Zipkin or Sleuth

# Spring Boot Logging

Spring Boot uses **Logback** as the default logging framework. It also supports other logging frameworks like Log4j2 and Java Util Logging. Logging is essential for monitoring application behavior, debugging, and tracing issues in production.

---

## 🔹 Logback Configuration

- Logback is configured using `logback.xml` or `logback-spring.xml`.
- Spring Boot provides default configuration if none is defined.
- Example logback-spring.xml:

```xml
<configuration>
  <include resource="org/springframework/boot/logging/logback/base.xml"/>

  <logger name="com.myapp" level="DEBUG"/>

  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

> Use `logback-spring.xml` instead of `logback.xml` if you want to use Spring's `@Value`, `springProfile`, etc.

---

## 🔹 Log Level per Profile

You can configure log levels differently for each profile by creating multiple `application-{profile}.yml` or `logback-spring.xml` files.

**application-dev.yml**
```yaml
logging:
  level:
    com.myapp: DEBUG
```

**application-prod.yml**
```yaml
logging:
  level:
    com.myapp: ERROR
```

This ensures verbose logging in development and minimal logging in production.

---

## 🔹 Structured Logging

Structured logging helps parse and analyze logs better (e.g., in JSON).

**Add dependency (for Logstash encoder):**
```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
</dependency>
```

**logback-spring.xml**
```xml
<appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

---

## 🔹 Log Correlation with Zipkin or Sleuth

**Spring Cloud Sleuth** enables distributed tracing and automatically adds:

- `traceId` and `spanId` to logs
- Correlation of logs across microservices

**Add dependency:**
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

**Log output:**
```
[traceId=5af5e1e2, spanId=12fd] - Executing order
```

Combine with **Zipkin** to visualize trace graphs.

---

## ✅ Use Cases

- Enable DEBUG level for dev, INFO for prod
- Trace requests using Sleuth
- Aggregate logs in ELK stack or Splunk
- Store logs in JSON format for analytics

---

## 💬 Interview Follow-Up Questions

### Q1: How does Spring Boot choose the default logging framework?

**A:** Spring Boot auto-configures **Logback** as the default if no other logging implementation is found. You can override it by adding dependencies like Log4j2.

---

### Q2: What's the difference between `logback.xml` and `logback-spring.xml`?

**A:** `logback-spring.xml` allows the use of Spring-specific features like `springProfile`, `@Value`, and is loaded by Spring Boot during the application bootstrap phase.

---

### Q3: How do you change logging level for a specific package?

**A:** You can set it in `application.yml`:
```yaml
logging:
  level:
    com.myapp.service: DEBUG
```
Or via `logback-spring.xml`:
```xml
<logger name="com.myapp.service" level="DEBUG"/>
```

---

### Q4: What is structured logging and why is it important?

**A:** Structured logging outputs logs in a machine-readable format (e.g., JSON), making it easier to parse and search logs in centralized systems like ELK or CloudWatch.

---

### Q5: How does Sleuth add tracing information to logs?

**A:** Sleuth creates a `TraceContext` per request and injects trace IDs into logs via MDC (Mapped Diagnostic Context). It also propagates trace headers in HTTP requests.

---

## 🔚 Summary

| Feature                  | Description                                 |
|--------------------------|---------------------------------------------|
| Logback                  | Default logging backend                     |
| Profile-specific config  | Define log levels per environment           |
| Structured logging       | Logs in JSON using Logstash encoder         |
| Sleuth                   | Adds traceId/spanId for distributed tracing |
| Centralized logging      | Used with ELK, Splunk, Zipkin, etc.         |
