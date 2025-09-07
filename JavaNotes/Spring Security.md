✅ 1. Spring Security Core Concepts
* What is Spring Security and how the filter chain works
* Authentication vs Authorization
* SecurityContext and SecurityContextHolder
* Principal and GrantedAuthority

⸻

✅ 2. Authentication
* In-memory authentication
* JDBC authentication (using database users)
* Custom user details service (UserDetailsService)
* Password encoding (e.g., BCryptPasswordEncoder)
* AuthenticationManager and AuthenticationProvider
* Programmatic vs declarative authentication

⸻

✅ 3. Authorization
* Role-based access control (RBAC)
* @PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed
* Method-level security (enable via @EnableGlobalMethodSecurity)
* Expression-based access control

⸻

✅ 4. Security Filters and Chain
* Understanding the Spring Security filter chain
* Role of filters like UsernamePasswordAuthenticationFilter, BasicAuthenticationFilter, etc.
* Custom filter creation
* Ordering and precedence of filters

⸻

✅ 5. Form-Based and Basic Authentication
* Login page, logout mechanism
* HTTP Basic authentication (with REST)
* CSRF protection (disabling for APIs)

⸻

✅ 6. JWT Authentication
* Stateless authentication using JWTs
* Creating and verifying JWTs
* Storing JWT in headers (Bearer tokens)
* Filter to intercept and validate token
* Expiry, signature, and refresh token handling

⸻

✅ 7. OAuth2 & OpenID Connect
* Spring Security OAuth2 client and resource server
* Integrating with providers like Google, GitHub, Okta, Keycloak
* spring-security-oauth2-client and spring-security-oauth2-resource-server
* Scopes and userinfo endpoints

⸻

✅ 8. Security with API Gateway
* Global security filter for authentication/authorization
* Forwarding JWT tokens downstream
* Custom GatewayFilter for request validation

⸻

✅ 9. CORS and CSRF
* Difference between CORS and CSRF
* CORS configuration in Spring Security
* CSRF protection for browser clients
* Disabling CSRF for REST APIs

⸻

✅ 10. Session Management and Stateless Security
* Statefulness vs stateless authentication
* Session creation policy
* Handling session fixation and concurrent sessions
* Logout in stateless systems

⸻

✅ 11. Custom Authentication & Authorization
* Custom login authentication filter
* Custom AccessDecisionVoter and AccessDecisionManager
* Custom token-based or header-based authentication

⸻

✅ 12. Security Context Propagation
* In async scenarios (@Async, CompletableFuture)
* Using DelegatingSecurityContextRunnable or SecurityContextAwareExecutor

⸻

✅ 13. Spring Boot 3.x & Spring Security 6+ Changes
* Lambda-based security configuration (SecurityFilterChain beans)
* Removal of WebSecurityConfigurerAdapter
* Declarative security using DSL-style config

⸻

✅ 14. Testing Secured Applications
* @WithMockUser, @WithUserDetails
* MockMvc + Spring Security
* Testing with JWT authentication filters

⸻

✅ 15. Best Practices
* Always hash passwords (e.g., BCrypt)
* Avoid storing tokens in local/session storage on frontend
* Secure all endpoints (deny-all-by-default strategy)
* Don’t expose internal endpoints (actuator, DB)
* Use HTTPS and set secure headers (CSP, HSTS, X-Frame-Options)
