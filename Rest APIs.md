# REST APIs: A Comprehensive Guide

### 1. What is REST (Representational State Transfer)? 

REST is an architectural style for building distributed systems and web services using standard HTTP protocols. It promotes stateless communication between client and server and defines clear guidelines for API development.

### 1.1 REST Constraints:
* Statelessness: Each request must contain all required information; server doesn’t store client context.
* Uniform Interface: Standard resource-oriented communication using HTTP verbs and consistent URLs.
* Client-Server Separation: The client (frontend) and server (backend) evolve independently.
* Cacheable: Responses can be cached to improve performance.
* Layered System: Intermediaries like API gateways can exist between client and server.

### 1.2 REST vs SOAP vs GraphQL:

|Feature	|REST	|SOAP	|GraphQL
|----------------|----------------|----------------|----------------|
|Protocol	|HTTP	|XML-based protocol	|HTTP with a flexible query language
|Data Format	|JSON, XML, Plain text	|XML only	|JSON
|Contract	|OpenAPI (optional)	|WSDL (strict XML contract)	|Schema with types
|Use Cases	|Web/Mobile APIs, Microservices	|Enterprise apps, transactions	|Flexible data fetching for clients

SOAP API Overview
* XML-based protocol.
* Uses WSDL for strict service contracts.
* Supports advanced features: transactions, security (WS-Security), reliable messaging.
* Heavyweight, often used in banking, telecom, and enterprise integrations.

* Example SOAP Envelope:

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
    <soapenv:Header/>
    <soapenv:Body>
        <getUser>
            <userId>101</userId>
        </getUser>
    </soapenv:Body>
</soapenv:Envelope>
```
⸻

### 2. HTTP Fundamentals

### 2.1 HTTP Methods:
* GET: Retrieve resources.
* POST: Create new resources.
* PUT: Replace resources.
* PATCH: Partial updates.
* DELETE: Remove resources.
* OPTIONS: Discover supported operations.

### 2.2 Common Status Codes:
* 200 OK: Success.
* 201 Created: Resource created.
* 204 No Content: Success with no body.
* 400 Bad Request: Invalid request.
* 401 Unauthorized: Missing or invalid authentication.
* 403 Forbidden: Insufficient permissions.
* 404 Not Found: Resource doesn’t exist.
* 409 Conflict: Resource conflict.
* 500 Internal Server Error: Server failure.

### 2.3 HTTP Headers:
* Content-Type: Format of request/response.
* Accept: Expected response format.
* Authorization: Credentials for authentication.
* Cache-Control: Caching policies.

### 2.4 Parameters:
* Path Variables: /users/{id} — Identify resources.
* Query Parameters: /users?page=1&size=10 — Filtering, pagination.
* Request Body: Complex data payload, typically JSON.

⸻

### 3. REST API Design Best Practices
* Use nouns and plural forms for resource names: /orders, /customers.
* Version APIs via URI: /api/v1/orders.
* Pagination, Sorting, Filtering via query parameters.
* HATEOAS: Include links to related resources.
* Idempotency:
* GET, PUT, DELETE are idempotent.
* POST is not idempotent unless designed with Idempotency Keys.

⸻

### 4. Spring Boot REST API Development

### 4.1 Key Annotations:

* @RestController
* @RequestMapping("/api")
* @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
* @PathVariable, @RequestParam, @RequestBody

### 4.2 Example with Validation:

```java
public class UserDto {
@NotNull private String name;
@Size(min = 8) private String password;
}
```

### 4.3 Exception Handling:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
}
```


⸻

### 5. API Security Best Practices
* JWT for stateless authentication.
* OAuth2/OpenID Connect for delegated authentication.
* Role-based access control with @PreAuthorize.
* CORS configuration via @CrossOrigin.
* Rate limiting with Bucket4j or API Gateway mechanisms.

⸻

### 6. Global Error Handling

Standard error structure example:
```json
{
    "timestamp": "2024-01-01T12:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed"
}
```

Custom exceptions can be created with @ResponseStatus to automatically map to specific status codes.

⸻

### 7. API Testing Techniques
* MockMvc for unit tests.
* TestRestTemplate or WebTestClient for integration tests.
* WireMock for mocking external APIs.
* Postman/Rest Assured for manual and automated testing.
* Pact for consumer-driven contract testing.

⸻

### 8. Documentation with OpenAPI/Swagger

Auto-generate API documentation using:
* Springdoc-OpenAPI (springdoc-openapi-ui).
* Swagger2 annotations to describe endpoints.

Example:
```java
@Operation(summary = "Get user by ID")
@GetMapping("/users/{id}")
public UserDto getUser(@PathVariable Long id) {
// logic
}
```

⸻

### 9. Performance Optimization Techniques
* Caching with HTTP headers (Cache-Control, ETag).
* Conditional GET requests.
* Asynchronous endpoints with CompletableFuture.
* Throttling with Bucket4j.

⸻

### 10. Real-World API Patterns
 * API Gateway for centralized routing and security.
 * Composite APIs to aggregate responses.
 * Backend-for-Frontend (BFF) per client type.
 * Enforce HTTPS for all communications.

⸻

### 11. Idempotency Explained
 * GET, PUT, DELETE are idempotent — repeating them yields the same server state.
 * POST is not idempotent by default — creates new resources each time.
 * Use Idempotency Keys to safely retry POST requests.

Example of Idempotency Key:
```http
POST /payments
Idempotency-Key: abc-123
```


⸻

### 12. Enforcing Mandatory Headers Globally in Spring Boot

Required Headers:
* Authorization
* CustomerContext
* CorrelationId
* ChannelId
* TestBed

Interceptor Implementation:
``` java
@Component
public class HeaderValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String[] headers = {"Authorization", "CustomerContext", "CorrelationId", "ChannelId", "TestBed"};
        for (String header : headers) {
            if (request.getHeader(header) == null || request.getHeader(header).isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing required header: " + header + "\"}");
                return false;
            }
        }
        return true;
    }
}
```

Interceptor Registration:

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private HeaderValidationInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
```

⸻

Conclusion
* REST APIs are lightweight, scalable, and ideal for modern applications.
* Use Spring Boot’s rich ecosystem for building secure, reliable APIs.
* Enforce mandatory headers using interceptors for consistent request validation.
* Implement testing, documentation, and performance improvements.
* Choose SOAP for scenarios requiring strict contracts and enterprise-grade reliability.

