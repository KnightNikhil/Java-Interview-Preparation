# REST APIs: A Comprehensive Guide

### 1. What is REST (Representational State Transfer)? 

REST is an architectural style for building distributed systems and web services using standard HTTP protocols. It promotes stateless communication between client and server and defines clear guidelines for API development.

### 1.1 REST Constraints:
* **Statelessness**: Each request must contain all required information; server doesn’t store client context.
* **Uniform Interface**: Standard resource-oriented communication using HTTP verbs and consistent URLs.
* **Client-Server Separation**: The client (frontend) and server (backend) evolve independently.
* **Cacheable**: Responses can be cached to improve performance.
* **Layered System**: Intermediaries like API gateways can exist between client and server.

### 1.2 REST vs SOAP vs GraphQL:

|Feature	|REST	|SOAP	|GraphQL
|----------------|----------------|----------------|----------------|
|Protocol	|HTTP	|XML-based protocol	|HTTP with a flexible query language
|Data Format	|JSON, XML, Plain text	|XML only	|JSON
|Contract	|OpenAPI (optional)	|WSDL (strict XML contract)	|Schema with types
|Use Cases	|Web/Mobile APIs, Microservices	|Enterprise apps, transactions	|Flexible data fetching for clients

### 1.2 SOAP API Overview
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
* **GET**: Retrieve resources.
* **POST**: Create new resources.
* **PUT**: Replace resources.
* **PATCH**: Partial updates.
* **DELETE**: Remove resources.
* **OPTIONS**: Discover supported operations.

### 2.2 Common Status Codes:
* **200** OK: Success.
* **201** Created: Resource created.
* **204** No Content: Success with no body.
* **400** Bad Request: Invalid request.
* **401** Unauthorized: Missing or invalid authentication.
* **403** Forbidden: Insufficient permissions.
* **404** Not Found: Resource doesn’t exist.
* **409** Conflict: Resource conflict.
* **500** Internal Server Error: Server failure.

### 2.3 HTTP Headers:
* **Content-Type:** Format of request/response.
* **Accept**: Expected response format.
* **Authorization**: Credentials for authentication.
* **Cache-Control**: Caching policies.

### 2.4 Parameters:
* **Path Variables**: /users/{id} — Identify resources.
* **Query Parameters**: /users?page=1&size=10 — Filtering, pagination.
* **Request Body**: Complex data payload, typically JSON.

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

# GraphQL Implementation in Java (Spring Boot)

### 1. What is GraphQL?

GraphQL is a query language and runtime for APIs developed by Facebook. It enables client-driven data fetching, reducing over-fetching or under-fetching of data, and works through a single flexible API endpoint.

Key Features:
* Clients specify exactly what data they need
* Strongly typed schema defines the structure of the API
* Single endpoint replaces multiple REST endpoints

⸻

### 2. REST vs GraphQL Comparison

Feature	REST	GraphQL
Endpoints	Multiple (/users, /orders)	Single (/graphql) endpoint
Data Fetching	Fixed structure	Clients control the response shape
Over/Under Fetching	Possible	Eliminated
Versioning	Requires explicit versions	No versioning; schema evolves
Request Format	HTTP Methods (GET, POST, etc.)	Usually POST with JSON payload


⸻

### 3. GraphQL Core Concepts
* Schema: Defines data types, queries, mutations, and subscriptions
* Query: Read operations
* Mutation: Write operations (Create, Update, Delete)
* Subscription: Real-time updates using WebSockets
* Resolvers: Methods that fetch or modify data based on schema fields

⸻

### 4. Setting Up GraphQL with Spring Boot

Dependencies (Gradle)

```groovy
implementation 'com.graphql-java-kickstart:graphql-spring-boot-starter:12.0.0'
implementation 'com.graphql-java-kickstart:graphiql-spring-boot-starter:12.0.0' // Optional playground
```
Dependencies (Maven)

```xml
<dependency>
  <groupId>com.graphql-java-kickstart</groupId>
  <artifactId>graphql-spring-boot-starter</artifactId>
  <version>12.0.0</version>
</dependency>
<dependency>
  <groupId>com.graphql-java-kickstart</groupId>
  <artifactId>graphiql-spring-boot-starter</artifactId>
  <version>12.0.0</version>
</dependency>
```

⸻

### 5. Project Structure Example
```java
src/main/resources/graphql/
└── schema.graphqls

src/main/java/com/example/graphql/
├── model/
├── service/
├── resolver/
└── Application.java
```


⸻

### 6. Defining GraphQL Schema

schema.graphqls
```graphql
type Query {
    getUser(id: ID!): User
    getAllUsers: [User]
}

type Mutation {
  createUser(name: String!, email: String!): User
}

type User {
    id: ID!
    name: String!
    email: String!
}
```

⸻

### 7. Java Model Class

```java
public class User {
private String id;
private String name;
private String email;

    // Constructors, Getters, Setters
}
```


⸻

### 8. Service Layer Implementation

```java
@Service
public class UserService {

    private final Map<String, User> userStore = new HashMap<>();

    public User getUser(String id) {
        return userStore.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }

    public User createUser(String name, String email) {
        String id = UUID.randomUUID().toString();
        User user = new User(id, name, email);
        userStore.put(id, user);
        return user;
    }
}
```


⸻

### 9. Resolver Implementation
```java
@Component
public class UserResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

    @Autowired
    private UserService userService;

    public User getUser(String id) {
        return userService.getUser(id);
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public User createUser(String name, String email) {
        return userService.createUser(name, email);
    }
}
```


⸻

### 10. Example GraphQL Operations

Fetch User by ID
```json
query {
    getUser(id: "some-id") {
        id
        name
        email
    }
}
```

Create New User
```json
mutation {
    createUser(name: "John Doe", email: "john@example.com") {
        id
        name
        email
    }
}
```

Fetch All Users

```json
query {
    getAllUsers {
        id
        name
        email
    }
}
```


⸻

### 11. GraphiQL Playground (Optional)

Access UI for interactive testing:

http://localhost:8080/graphiql


⸻

### 12. Benefits of GraphQL

* Avoids over-fetching/under-fetching
* Strongly typed schemas with validation
* Clients control data structure
* No versioning headaches
* Reduces network calls with nested queries

⸻

### 13. Drawbacks of GraphQL

* More complex server-side implementation
* Not ideal for simple CRUD-only APIs
* Additional learning curve
* Potential over-complexity for small projects

⸻

### 14. Advanced GraphQL Features
* DataLoader for batch fetching, avoiding N+1 query problem
* Subscriptions for real-time updates via WebSockets
* Field Resolvers for nested and computed fields
* GraphQL Federation for combining multiple GraphQL services

⸻

### 15. Conclusion

GraphQL is ideal for modern, flexible APIs where clients dictate required data and efficiency is crucial. Spring Boot provides seamless integration with strong type safety, clear schema definitions, and resolver-based architecture.

⸻

Next Steps:
* Secure GraphQL APIs with authentication/authorization
* Explore pagination and filtering
* Optimize with DataLoader and caching
* Add real-time features with Subscriptions