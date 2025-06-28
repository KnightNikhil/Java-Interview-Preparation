✅ 1. Overall Architecture Style
* Is it Monolithic, Microservices, or Modular Monolith?
* Why this approach was chosen?
* Pros/cons observed in real usage

⸻

✅ 2. Service Decomposition
* What are the main services/modules?
* e.g., UserService, PaymentService, InventoryService
* How responsibilities are separated?
* Are services stateless or stateful?

⸻

✅ 3. Tech Stack

Clearly state:
* Backend: Java version, Spring Boot, JPA, etc.
* Frontend (if applicable): Angular, React, etc.
* Database: MySQL, PostgreSQL, MongoDB, Redis
* Messaging: Kafka, RabbitMQ, etc.
* DevOps: Docker, Kubernetes, Jenkins, GitLab CI
* Observability: ELK, Prometheus, Grafana, Zipkin

⸻

✅ 4. Inter-Service Communication
* REST or Feign?
* Synchronous vs Asynchronous patterns
* Circuit breaker or retry logic in place? (Resilience4j?)
* Use of Kafka or queues for communication/events

⸻

✅ 5. Authentication & Authorization
* JWT, OAuth2, or session-based auth?
* Is it centralized (API Gateway or Auth service) or local?
* Role-based or fine-grained access control?

⸻

✅ 6. Data Management
* Database per service or shared DB?
* How are transactions handled? (ACID vs eventual consistency)
* Any caching layer? (Redis, Caffeine)
* Use of read replicas, sharding, or indexes?

⸻

✅ 7. CI/CD Pipeline
* How is code built, tested, and deployed?
* Branching strategy (GitFlow, trunk-based)?
* Tools: Jenkins, GitHub Actions, GitLab CI/CD, ArgoCD, etc.

⸻

✅ 8. Configuration Management
* Spring profiles
* Centralized config (Spring Cloud Config?)
* Secret and credentials handling (Vault, AWS Secrets Manager?)

⸻

✅ 9. Observability and Monitoring
* Health checks (Actuator?)
* Logging (Logback, Log4j2, ELK stack?)
* Tracing (Sleuth, Zipkin, Jaeger?)
* Metrics (Micrometer, Prometheus?)

⸻

✅ 10. Security Practices
* How are APIs protected?
* Input validation, rate limiting, CSRF/XSS prevention
* HTTPS/TLS enforced?
* Static/dynamic code analysis?

⸻

✅ 11. Scalability & Performance
* How does the system scale? (Horizontally or vertically)
* Load balancing strategy
* Caching (data, page, method level)
* Stress/load testing strategy or tools (e.g., JMeter)

⸻

✅ 12. Notable Design Decisions
* Why certain frameworks were chosen (Spring Boot vs Quarkus? Kafka vs RabbitMQ?)
* Trade-offs made (monolith to microservices, SQL vs NoSQL)
* Known limitations or areas for improvement
* Design patterns used (e.g., Saga, API Gateway, CQRS, Circuit Breaker)

⸻

📋 Optional But Good to Know
* Any usage of feature toggles or canary deployments?
* Rollback strategy if deployment fails
* How you test and deploy microservices independently
* Approach to schema evolution/migration (Liquibase/Flyway?)