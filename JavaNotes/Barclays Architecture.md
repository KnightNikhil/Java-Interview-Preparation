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


#BARCLAYS ARCHITECTURE

# Barclays Online Banking - Detailed Technical Documentation of Decoupled Mini-App Architecture

---

## 1. Architectural Approach

Barclays Online Banking follows a **Decoupled Mini-App Architecture**, which is distinct from both standard monolithic and full microservices patterns. It blends modular development, scalable deployment, and enterprise-grade security to support high-traffic online banking systems.

### Key Characteristics:

* **32 independent mini-apps**, each addressing different feature areas (Payments, Transfers, Accounts, etc.).
* All mini-apps leverage a **common framework**, enforcing uniform:

    * Security policies
    * Logging, monitoring
    * Utility functions
    * Response structures
* Each mini-app:

    * Has its own frontend (Angular, React, Vue, etc.)
    * Has its own backend (Java, Spring Boot, Tomcat)
    * Is deployed independently or collectively on **AWS EC2** instances.
* **Nginx reverse proxies** are bundled with each mini-app to serve UI and route backend requests.
* Global session continuity is achieved through centralized **Redis**.

---

## 2. Deployment Structure and Flow

### 2.1 Mini-App Composition:

* **Frontend:**

    * Built using different JS frameworks based on feature requirements.
    * Served via **Nginx** embedded within the deployment package.
* **Backend:**

    * Spring Boot applications running on Tomcat servers.
    * Handles business logic, API endpoints, and downstream integrations.
* **Bundling:**

    * Frontend static files + Nginx reverse proxy + backend service packaged together.
    * Deployed as a single unit per mini-app to AWS EC2.

### 2.2 CI/CD Pipelines:

* Common **Jenkins pipelines** manage builds and deployments.
* Pipeline steps include:

    * Code checkout (frontend + backend)
    * Build frontend artifacts
    * Package backend applications (JAR/WAR)
    * Bundle with Nginx configuration
    * Deploy to AWS EC2
* Supports:

    * Independent mini-app deployments for feature releases.
    * Bundled deployments for coordinated platform-wide updates.

---

## 3. Global Session Management with Redis

### 3.1 Why Redis for Sessions:

* **Stateless EC2 instances** necessitate centralized session storage.
* Redis provides:

    * Fast, in-memory session lookups
    * Scalability across instances
    * High availability with clustering and replication

### 3.2 Session Flow:

1. **User Login:**

    * Credentials verified via enterprise Identity Provider (IdP) or IAA system.
    * Session object stored in Redis with session ID, user roles, metadata.
    * Session token issued to the browser (secure cookie, HttpOnly, Secure flags).

2. **Navigating Across Mini-Apps:**

    * Session token sent with each request.
    * Nginx reverse proxies the request to the correct mini-app.
    * Mini-app backend validates session by:

        * Fetching session details from Redis.
        * Checking token integrity, expiry, and user roles.

---

## 4. Security During Mini-App Navigation

### 4.1 Seamless Security Enforcement:

* Though mini-apps are technically independent, they share:

    * Common security filters from the shared framework.
    * Unified session verification via Redis.
* Navigation between mini-apps is transparent to the user.
* Backend enforces security policies individually, ensuring consistent access control.

### 4.2 Role-Based Access and Restrictions:

* User roles stored in Redis session.
* Each mini-app applies:

    * Feature-specific access checks.
    * Fine-grained permissions based on user roles.

---

## 5. TIAA Token for Downstream API Security

### 5.1 Introduction to TIAA Token:

* **TIAA = Trusted Identity and Access Authorization** token.
* Internal, encrypted token used for backend-to-backend communication.
* Issued and validated by the enterprise-wide **IAA (Identity Access & Authentication)** system.

### 5.2 Why Not Standard Spring Security JWT:

| Aspect                | Spring Security JWT                | TIAA Token                                  |
| --------------------- | ---------------------------------- | ------------------------------------------- |
| Visibility            | Base64-encoded, decodable          | Opaque, enterprise-encrypted                |
| Usage Scope           | Public API tokens                  | Internal-only, backend-to-backend calls     |
| Revocation Control    | Hard to revoke instantly           | Central real-time revocation via IAA        |
| Claims & Metadata     | Basic user claims                  | Rich, enterprise-controlled claims          |
| Compliance & Auditing | Minimal, depends on implementation | Enforced audit trails and security controls |

TIAA tokens offer:

* Enhanced encryption and tamper protection.
* Real-time revocation capabilities.
* Secure identity propagation across backend services.
* Compliance with financial regulatory standards.

### 5.3 Complete TIAA Token Flow:

1. **Initial Authentication:**

    * User authenticates via a mini-app.
    * Backend interacts with IAA to validate credentials.
    * Session established in Redis.

2. **Backend-to-Backend Communication:**

    * Mini-app backend needs to call a downstream API (e.g., Transfers).
    * Requests TIAA token from IAA using session context.
    * IAA issues TIAA token with encrypted user identity and roles.
    * Token attached to downstream API request header.

3. **Downstream Service Processing:**

    * Validates TIAA token via IAA.
    * Applies role-based access control.
    * Executes authorized operations.

---

## 6. Security Considerations and Controls

* **Session Tokens:**

    * Stored in HttpOnly, Secure cookies.
    * Prevent XSS and session hijacking.

* **Redis Security:**

    * Role-based access control to Redis.
    * Clustering for high availability.
    * Encryption in transit between services and Redis.

* **TIAA Token Handling:**

    * Short-lived, time-bound tokens.
    * Scope-limited for specific API operations.
    * Immediate revocation possible via IAA on logout or anomalies.

* **Network Security:**

    * All communication over HTTPS.
    * AWS security groups control traffic between instances.
    * Optionally integrated with API Gateway or WAF for external exposure.

---

## 7. Scalability and Operational Benefits

| Benefit                | Description                                                  |
| ---------------------- | ------------------------------------------------------------ |
| Independent Scaling    | Mini-apps scale individually based on feature demand.        |
| Backend Decoupling     | Backend services isolated, reducing system-wide failures.    |
| Deployment Flexibility | Teams can release features independently.                    |
| Session Continuity     | Redis ensures seamless user experience across mini-apps.     |
| Security Uniformity    | Shared framework and TIAA token enforce consistent policies. |

---

## 8. Q\&A: Anticipated Follow-Up Interview Questions

### 8.1 How is this different from a pure microservices architecture?

* Pure microservices often have independent databases and tech stacks.
* Barclays mini-apps are independently deployable but share common frameworks and session management.
* Reduces distributed system complexity while retaining scalability.

### 8.2 Why bundle frontend and backend together?

* Ensures version compatibility between UI and backend APIs.
* Simplifies deployment processes.
* Reduces integration bugs and accelerates feature rollout.

### 8.3 How does Redis help with session continuity?

* Central in-memory store accessible to all mini-apps.
* Session token lookup ensures consistent user identity across independent backends.

### 8.4 What happens if Redis goes down?

* Redis is configured with clustering and replication.
* High availability ensures minimal downtime.
* Failures result in session invalidation, prompting user re-login.

### 8.5 How are downstream API calls secured?

* TIAA token obtained from IAA for each backend-to-backend call.
* Downstream services validate tokens, ensuring secure identity propagation.

### 8.6 Why not use Spring Security JWT for downstream calls?

* Plain JWT lacks real-time revocation.
* TIAA token offers centralized control, encryption, and compliance features.

### 8.7 Can mini-apps be scaled independently?

* Yes, deployed on separate EC2 instances.
* Autoscaling adjusts resources based on feature-specific traffic patterns.

### 8.8 How is user identity preserved when switching between mini-apps?

* Session token sent with each request.
* Nginx reverse proxies to target mini-app backend.
* Backend verifies session state via Redis before processing.

### 8.9 How does TIAA token enhance compliance and audit?

* Token issuance and usage are logged by IAA.
* Token revocation and expiration are centrally managed.
* Supports regulatory audits and traceability of user actions.

---

## 9. Advanced Technical Deep Dives

### 9.1 Token Revocation and Blacklisting

* IAA maintains token state and can revoke tokens instantly.
* Supports real-time user session termination across all mini-apps.
* Redis entries for sessions are cleared upon logout or suspicious activity.

### 9.2 Zero Trust Network Principles

* Internal services authenticate even within the VPC.
* TIAA token validates each request, ensuring no implicit trust.
* Service-to-service calls require strict identity verification.

### 9.3 Future Improvements (Possible Enhancements)

* Migration to container-based deployments (e.g., ECS, Kubernetes) for more efficient scaling.
* Integration of service mesh for better observability and traffic management.
* Enhanced token introspection endpoints for richer API-level authorization decisions.

---

## 10. Architectural Diagrams (Text Representation)

**10.1 Mini-App Deployment Flow:**

```
[Frontend Code] + [Backend Code] → Jenkins Pipeline → Bundle with Nginx + Tomcat → Deploy to EC2
```

**10.2 User Navigation Across Mini-Apps:**

```
User → Nginx (Mini-App 1) → Backend (Mini-App 1) → Redis (Session Validation)
User Navigates → Nginx (Mini-App 2) → Backend (Mini-App 2) → Redis (Session Validation)
```

**10.3 Backend-to-Backend Call with TIAA Token:**

```
Mini-App Backend → Request TIAA Token from IAA
Mini-App Backend → Downstream API with TIAA Token
Downstream API → Validate Token via IAA → Process Request
```

---

## 11. Summary for Technical Stakeholders

Barclays Online Banking employs a hybrid mini-app architecture where 32 decoupled frontend-backend units operate with shared session management, standardized security enforcement, and backend-to-backend trust via TIAA tokens. The platform balances modular scalability with centralized governance to meet banking-grade security and compliance requirements.

---

## 12. Interview-Ready Summary Statement

"In Barclays' Online Banking platform, we utilize a backend-driven, decoupled mini-app model where frontend and backend for each feature area are packaged together and deployed on AWS EC2 with Nginx and Tomcat. User sessions are centrally managed in Redis for seamless navigation, while backend-to-backend security is enforced using enterprise-issued TIAA tokens via the IAA system. This design ensures modular scalability, robust security, and regulatory compliance, without the full operational complexity of microservices."

---
# Barclays Online Banking - TIAA Token Encryption Lifecycle (Detailed Documentation)

---

## 1️⃣ What is a TIAA Token?

TIAA (Trusted Identity and Access Authorization) Token is an **internal, encrypted, opaque token** used for secure backend-to-backend communication within Barclays. It is designed to provide:

* Tamper-proof identity propagation
* Real-time revocation capability
* Compliance with banking-grade security and regulatory requirements
* Secure propagation of user identity, session, and roles between trusted services

Unlike public-facing JWTs, TIAA tokens are fully opaque to clients and encrypted end-to-end using Barclays' internal Key Management System (KMS).

---

## 2️⃣ Complete TIAA Token Encryption Lifecycle

### 2.1 Token Generation

When a backend service (Mini-App Backend) requires a TIAA token to call another internal service:

**Process Flow:**

1. **Session Context Extraction:**

    * Retrieve details from Redis:

        * User ID
        * Session ID
        * User roles & permissions
        * Additional metadata (e.g., device fingerprint)

2. **Token Request to IAA:**

    * Backend sends a secure request to the IAA (Identity Access & Authentication) system with session context.

3. **Payload Creation by IAA:**

    * Constructs token payload containing:

        * `sub` → User identifier
        * `sid` → Session ID
        * `roles` → User roles
        * `iat` → Issued timestamp
        * `exp` → Expiry timestamp
        * `trace` → Correlation or request trace ID
        * Optionally: device ID, tenant, geo-location

4. **Encryption:**

    * Entire payload encrypted using:

        * AES-256 or Barclays-approved symmetric algorithm
        * Keys managed by Barclays' enterprise KMS
    * Encrypted output is opaque, binary-safe token (non-readable)

5. **Token Issuance:**

    * IAA returns the encrypted token to the requesting backend.

---

### 2.2 Token Example (Conceptual)

```
TIAAToken ::= ENCRYPT({
  "sub": "user12345",
  "sid": "session-abc-789",
  "roles": "CUSTOMER,VIEWER",
  "iat": 1723803200,
  "exp": 1723806800,
  "trace": "correlation-uuid"
})
```

**Post Encryption:**

```
gHFt932/1djW...U98z0fjiw==
```

Opaque and cannot be decoded or manipulated without IAA's decryption.

---

### 2.3 Token Validation During API Calls

When the Mini-App Backend calls a downstream internal API:

1. Token added to HTTP headers:

```
Authorization: TIAA gHFt932/1djW...U98z0fjiw==
```

2. **Downstream API Process:**

    * Receives token
    * Forwards token to IAA for validation
    * IAA decrypts token using KMS
    * Validates:

        * Token integrity (HMAC, encryption checks)
        * Expiry timestamp (`exp`)
        * Active session ID in Redis
        * User roles and permissions
        * Optional: IP/device fingerprint matching

3. If valid → API processes request

4. If invalid/expired → Responds with `401 Unauthorized`

---

## 3️⃣ TIAA Token Lifecycle Summary

| Phase           | Action                                                    |
| --------------- | --------------------------------------------------------- |
| Generation      | Issued by IAA on-demand to trusted backend services       |
| Validity Period | Short-lived (configurable, e.g., 5-15 minutes)            |
| Revocation      | Immediate via IAA (on logout, suspicious activity, fraud) |
| Renewal         | Backend re-requests token after expiry or revocation      |

---

## 4️⃣ Encryption & Key Management

* **Encryption Algorithm:** AES-256 or Barclays-approved symmetric standard
* **KMS Integration:**

    * Keys managed centrally by Barclays' Key Management System
    * Regular automated key rotation
    * Restricted key access (limited to IAA system)
* **Tamper Protection:**

    * Integrity ensured via HMAC or MAC schemes
    * Altered tokens immediately rejected

---

## 5️⃣ Comparison: TIAA Token vs Standard JWT

| Feature            | Standard JWT                            | TIAA Token (Barclays)                   |
| ------------------ | --------------------------------------- | --------------------------------------- |
| Readability        | Base64-encoded, decodable               | Fully opaque, encrypted blob            |
| Intended Audience  | Public/Client visible                   | Internal only, backend-to-backend       |
| Revocation Support | Complex, not instant                    | Real-time, centralized via IAA          |
| Encryption Level   | Optional (signed, not always encrypted) | Encrypted at rest and in transit        |
| Compliance         | Varies by implementation                | Meets strict banking security standards |

---

## 6️⃣ Security Considerations and Best Practices

✅ Tokens never exposed to browser or frontend layers
✅ Short-lived tokens reduce replay attack window
✅ Session state cross-verified with Redis
✅ All token issuance and usage logged for audit trails
✅ Encrypted tokens mitigate tampering and eavesdropping
✅ Layered security enforced alongside HTTPS and network firewalls

---

## 7️⃣ Typical Call Flow Illustration

```
[Mini-App Backend] → Request TIAA Token → [IAA System]
[Mini-App Backend] → Calls Downstream API with TIAA Token
[Downstream API] → Validates Token with IAA → Processes Request or Rejects
```

---

## 8️⃣ Integration with Redis and Session State

* Redis holds active session data: session ID, user roles, expiry
* IAA validates session existence during token verification
* Logout or session timeout triggers token revocation
* Ensures tight coupling between session state and token validity

---

## 9️⃣ Benefits of TIAA Token Architecture

* Enterprise-grade encryption protects sensitive identity data
* Opaque tokens eliminate risks of client-side tampering
* Real-time revocation enhances security posture
* Supports Zero Trust principles for internal communication
* Fully auditable for compliance with banking regulations

---

## 10️⃣ Conclusion

TIAA tokens are a secure, internal-only mechanism for backend identity propagation within Barclays' decoupled mini-app architecture. Designed for scalability, security, and regulatory compliance, TIAA tokens enhance backend interactions by:

* Providing encrypted, non-manipulable identity tokens
* Allowing real-time revocation and session synchronization
* Supporting secure, role-aware API communication across services

---

End of Documentation
