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
