✅ 1. REST Basics
* What is REST (Representational State Transfer)?
* REST constraints: Statelessness, Uniform interface, Client-server, Cacheable, Layered system
* REST vs SOAP
* REST vs GraphQL

⸻

✅ 2. HTTP Fundamentals
* HTTP Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
* Status codes: 200, 201, 204, 400, 401, 403, 404, 409, 500
* Headers: Content-Type, Accept, Authorization, Cache-Control
* Query parameters vs path variables vs request body

⸻

✅ 3. REST API Design Best Practices
* Resource naming conventions (nouns, plural)
* Versioning: URI-based (/v1/resource) vs header-based
* Pagination, sorting, and filtering
* HATEOAS (Hypermedia as the Engine of Application State)
* Idempotency of methods (GET, PUT, DELETE)

⸻

✅ 4. Building REST APIs in Spring Boot
* @RestController, @RequestMapping, @GetMapping, etc.
* @PathVariable, @RequestParam, @RequestBody, @ResponseStatus
* Returning proper status codes and response entities
* Exception handling with @ControllerAdvice
* Validation using @Valid, @NotNull, @Size, etc.
* DTO vs Entity separation (Model Mapping)

⸻

✅ 5. API Security
* Stateless JWT authentication
* OAuth2 and OpenID Connect
* Role-based access control
* Secure headers and CORS (@CrossOrigin)
* API rate limiting

⸻

✅ 6. Error Handling
* Standard error response format
* Custom exceptions and global exception handlers
* HTTP status mapping to exceptions

⸻

✅ 7. Testing REST APIs
* Unit testing controllers using MockMvc
* Integration testing with TestRestTemplate or WebTestClient
* API mocking using WireMock
* API testing using Postman / Rest Assured
* Contract testing (e.g., with Pact)

⸻

✅ 8. Documentation
* Swagger / OpenAPI using springdoc-openapi or Swagger2
* Annotations for documenting endpoints and models
* API discovery and client generation

⸻

✅ 9. Performance and Optimization
* Caching (HTTP caching headers, Spring Cache)
* ETags and conditional GETs
* Asynchronous endpoints
* Throttling and rate limiting

⸻

✅ 10. Versioning and Backward Compatibility
* URI versioning, parameter versioning, custom headers
* Supporting multiple versions in production

⸻

✅ 11. Real-World Patterns
* API Gateway pattern
* Aggregation of APIs (composite services)
* Backend-for-frontend (BFF)
* REST over HTTPS (always)

⸻

✅ 12. Common Interview Concepts
* Difference between PUT and PATCH
* Statelessness in REST
* Idempotency and safety of HTTP methods
* RESTful error handling
* REST vs RPC

⸻

➕ Optional (Advanced)
* Rate limiter implementation
* Response compression (gzip)
* API monitoring (e.g., with Prometheus/Grafana)
* Async API responses with DeferredResult, CompletableFuture, or WebFlux