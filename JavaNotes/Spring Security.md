# Spring Security — Complete Reference

Table of Contents
1. [Introduction](#1-introduction)
2. [Core Concepts](#2-core-concepts)
3. [Authentication](#3-authentication)
4. [Authorization](#4-authorization)
5. [Security Filters & Filter Chain](#5-security-filters--filter-chain)
6. [Form-Based and HTTP Basic Authentication](#6-form-based-and-http-basic-authentication)
7. [JWT Authentication (Practical)](#7-jwt-authentication-practical)
8. [Refresh Tokens (Design & Best Practices)](#8-refresh-tokens-design--best-practices)
9. [OAuth2 & OpenID Connect (Overview)](#9-oauth2--openid-connect-overview)
10. [CORS and CSRF](#10-cors-and-csrf)
11. [Session Management & Stateless Architectures](#11-session-management--stateless-architectures)
12. [Security with API Gateway & Microservices](#12-security-with-api-gateway--microservices)
13. [Custom Authentication & Authorization](#13-custom-authentication--authorization)
14. [Thread Safety, Concurrency & Token Rotation](#14-thread-safety-concurrency--token-rotation)
15. [Testing Secured Applications](#15-testing-secured-applications)
16. [Best Practices](#16-best-practices)
17. [Common Pitfalls to Avoid](#17-common-pitfalls-to-avoid)
18. [Useful Patterns & Reference Implementations](#18-useful-patterns--reference-implementations)
19. [Appendix: Quick Cheatsheet](#19-appendix-quick-cheatsheet)

---

## 1. Introduction
Spring Security is a powerful and highly customizable authentication and access-control framework for Java applications. It integrates with Spring applications and supports a wide range of security scenarios: session-based authentication, stateless JWTs, OAuth2, method-level security, and custom auth flows.

This document targets developers from beginners to seniors and provides practical examples, configuration snippets, best practices, and common pitfalls.

---

## 2. Core Concepts
- Authentication vs Authorization
  - Authentication: proving identity (login).
  - Authorization: deciding whether an authenticated identity can access a resource (roles/permissions).
- Principal and GrantedAuthority
  - Principal: the authenticated user object (often `UserDetails`).
  - GrantedAuthority: a granted permission or role (e.g., `ROLE_USER`).
- SecurityContext & SecurityContextHolder
  - `SecurityContext` holds the current `Authentication`.
  - `SecurityContextHolder` exposes the `SecurityContext` (thread-local by default).
- Filters & Filter Chain
  - Spring Security is implemented as a chain of servlet filters that intercept requests and populate `SecurityContext`.

---

## 3. Authentication

### 3.1 Common Authentication Sources
- In-memory (good for demos)
- JDBC-based (users stored in DB)
- LDAP / Active Directory
- OAuth2 / OpenID Connect
- Custom `UserDetailsService` or `ReactiveUserDetailsService`

### 3.2 Password Encoding
Always store hashed passwords. Use `BCryptPasswordEncoder` or `Argon2PasswordEncoder`.

Example:
```java
// java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 3.3 Spring Boot 3 / Spring Security 6 Configuration (recommended)
Use `SecurityFilterChain` beans instead of `WebSecurityConfigurerAdapter`.

Example: Stateless API using JWT (basic skeleton)
```java
// java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/public/**").permitAll()
          .requestMatchers("/admin/**").hasRole("ADMIN")
          .anyRequest().authenticated())
      .httpBasic(Customizer.withDefaults());
    return http.build();
}
```

### 3.4 AuthenticationManager & AuthenticationProvider
- `AuthenticationManager` delegates to one or more `AuthenticationProvider`.
- Custom `AuthenticationProvider` can implement bespoke verification (2FA, hardware token).

---

## 4. Authorization

### 4.1 URL-based vs Method-based
- URL-based: `HttpSecurity` rules.
- Method-based: annotations like `@PreAuthorize`, `@PostAuthorize`, `@Secured`, `@RolesAllowed`.

Enable method security:
```java
// java
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig { }
```

Example:
```java
// java
@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
public Appointment getAppointment(Long id) { ... }
```

### 4.2 Expression-based Access Control
Use SpEL expressions in `@PreAuthorize` and `HttpSecurity` (`hasRole`, `hasAuthority`, `principal`, `authentication`, `permitAll`).

---

## 5. Security Filters & Filter Chain

- Key filters: `UsernamePasswordAuthenticationFilter`, `BasicAuthenticationFilter`, `OncePerRequestFilter` for custom filters, `JwtAuthenticationFilter` (custom).
- Order matters: authentication filters should run before protected resource handling.
- Create custom filters by extending `OncePerRequestFilter`.

Example JWT filter skeleton:
```java
// java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Authentication auth = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
```

Register filter:
```java
// java
http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
```

---

## 6. Form-Based and HTTP Basic Authentication

- Form-based: default for web applications, supports login page, remember-me, CSRF protection.
- HTTP Basic: simple header-based auth for REST or internal services (use HTTPS).

Example form login:
```java
// java
http
  .formLogin(form -> form
    .loginPage("/login")
    .permitAll())
  .logout(logout -> logout
    .logoutUrl("/logout")
    .invalidateHttpSession(true));
```

---

## 7. JWT Authentication (Practical)

### 7.1 When to use JWT
- Stateless APIs, microservices with distributed verification.
- Short-lived JWTs for authorization; never store sensitive long-term secrets in JWT payload.

### 7.2 Typical JWT Setup
- Access token: short-lived (minutes), sent on every request (Authorization: Bearer).
- Refresh token: long-lived, stored server-side, used to issue new access tokens (see section 8).

### 7.3 JWT Provider Responsibilities
- Generate signed JWTs (HMAC or RSA).
- Verify signature and expiry.
- Convert claims to `Authentication`/`UserDetails`.

Example JWT provider with HMAC:
```java
// java
public class JwtTokenProvider {
    private final SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

    public String generateToken(UserDetails user, Duration ttl) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(ttl)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        String username = claims.getSubject();
        List<GrantedAuthority> authorities = ... // convert claims
        UserDetails user = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }
}
```

### 7.4 Storing JWTs on the Client
- Avoid storing in localStorage (XSS risk). Prefer HttpOnly secure cookies if web application.
- For SPAs, use same-site, secure, HttpOnly cookies or short-lived access tokens + refresh token in secure storage.

---

## 8. Refresh Tokens (Design & Best Practices)
- Purpose: allow short-lived access tokens with seamless UX.
- Refresh tokens must be stateful (store hash in DB or Redis), rotatable, revocable.
- Never use refresh tokens to call APIs.
- Hash refresh tokens before storing (same rationale as password hashing).

Typical refresh token DB table:
- id (UUID), user_id, token_hash, issued_at, expires_at, revoked, device_id, ip_address

Refresh flow:
1. Client sends refresh token to `/auth/refresh`.
2. Server hashes token and looks it up.
3. Validate: exists, not expired, not revoked, device matches.
4. Rotate: revoke old token and issue new refresh token + new access token.
5. Return both to client.

Concurrency: use DB constraints or distributed locking to prevent double usage.

---

## 9. OAuth2 & OpenID Connect (Overview)

### 9.1 Roles
- Authorization Server: issues tokens (access + refresh).
- Resource Server: accepts access tokens and serves protected resources.
- Client: application that requests tokens (confidential vs public).

Spring libraries:
- `spring-security-oauth2-client` — clients (login with Google/GitHub).
- `spring-security-oauth2-resource-server` — validate incoming JWTs or opaque tokens.

Example resource server config:
```java
// java
http
  .oauth2ResourceServer(oauth -> oauth
      .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
```

Use provider metadata for OIDC and rely on JWKs for JWT verification.

---

## 10. CORS and CSRF

### 10.1 CORS
- Cross-Origin Resource Sharing: browser mechanism controlling cross-origin HTTP requests.
- Configure allowed origins, methods, headers, and credentials in Spring Security or via `CorsConfigurationSource`.

Example:
```java
// java
http.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowCredentials(true);
    config.setAllowedHeaders(List.of("*"));
    return config;
}));
```

### 10.2 CSRF
- Prevents cross-site request forgery for stateful web applications.
- Spring Security enables CSRF protection for non-GET requests by default.
- For stateless REST APIs using JWTs, disable CSRF:
```java
// java
http.csrf(csrf -> csrf.disable());
```
- For form-based apps, keep CSRF enabled and transmit tokens in forms or via custom headers.

---

## 11. Session Management & Stateless Security

### 11.1 Session Creation Policies
- `SessionCreationPolicy.STATELESS` — no `HttpSession` created; use for APIs.
- `IF_REQUIRED` or `ALWAYS` — session-based auth.

Example:
```java
// java
http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```

### 11.2 Session Fixation & Concurrent Sessions
- Use Spring Security features to prevent session fixation (`migrateSession`) and limit concurrent sessions (`ConcurrentSessionControlAuthenticationStrategy` or session management configuration).

---

## 12. Security with API Gateway & Microservices
- Gateway validates access tokens (JWT) and forwards user info downstream or passes `Authorization` header.
- Keep gateways stateless: do not perform DB calls for every request.
- Use short-lived access tokens; use refresh flow via centralized AuthService.
- Prefer verification via signature (JWK) rather than introspection for JWTs to avoid DB hits.

---

## 13. Custom Authentication & Authorization

### 13.1 Custom Filters
- Implement `OncePerRequestFilter` or extend `AbstractAuthenticationProcessingFilter`.
- Validate headers, perform token lookup, create `Authentication` and populate `SecurityContext`.

### 13.2 Custom AuthenticationProvider
- Implement `authenticate()` and `supports()` for bespoke credential types.

### 13.3 Custom AccessDecisionVoter / AccessDecisionManager
- Fine-grained access decisions across multiple voters.

### 13.4 Auditing & Device Control
- Store refresh token metadata (device id, IP).
- Allow per-device logout / token revocation.

---

## 14. Thread Safety, Concurrency & Token Rotation
- Always store token state atomically: revoke+insert in a transaction.
- Use DB unique constraints to prevent reuse of the same refresh token.
- Optimistic locking or CAS if using Redis.

---

## 15. Testing Secured Applications

### 15.1 Unit & Integration
- `@WithMockUser` for method-level tests.
- `@WithUserDetails` to load real user data.
- `MockMvc` with Spring Security setup:
```java
// java
mockMvc.perform(get("/secure")
    .with(user("user").roles("USER")))
    .andExpect(status().isOk());
```

### 15.2 Testing JWT Filters
- Mock `JwtTokenProvider` or provide test tokens signed with test key.
- For refresh flows, test rotation and concurrency (simulate two refresh requests).

### 15.3 Test Utilities
- Use `SecurityMockMvcRequestPostProcessors` for token injection.
- Use `@SpringBootTest` with test profile for DB-backed refresh tokens (H2 / Testcontainers).

---

## 16. Best Practices
- Hash all passwords (`BCrypt`, `Argon2`).
- Use short-lived access tokens and rotate refresh tokens.
- Store refresh token hashes, not raw tokens.
- Use HttpOnly, Secure, SameSite cookies for web refresh tokens.
- Deny-by-default: protect endpoints and explicitly permit public ones.
- Validate tokens early in filter chain to fail fast.
- Use HTTPS everywhere.
- Enable CSP, HSTS, X-Frame-Options, X-Content-Type-Options headers.
- Log security events (login success/failures, token revocations) but avoid logging raw tokens.
- Centralize authentication in an AuthService for microservices.
- Use provider metadata & JWKs for OAuth2/OIDC.

---

## 17. Common Pitfalls to Avoid
- Storing raw refresh tokens in DB (instead store hash).
- Making access tokens too long-lived.
- Disabling CSRF blindly on stateful apps.
- Storing JWTs in localStorage for web apps (XSS risk).
- Verifying JWTs by relying on claims without checking signature/expiry.
- Performing DB lookups on every request for access token validation (defeats stateless advantage).
- Inadequate handling of concurrent refresh requests (allowing replay).
- Exposing internal endpoints (management/actuator) without protection.

---

## 18. Useful Patterns & Reference Implementations

### 18.1 Two-Token Model (Recommended)
- Access token (JWT) — short-lived, stateless.
- Refresh token — server-side, rotatable, revocable.

### 18.2 Refresh Token Rotation (sample flow)
1. Client sends `refreshToken`.
2. Server validates and issues new `accessToken` and new `refreshToken`.
3. Server revokes old refresh token (mark revoked).
4. Store hash(new refresh token).

### 18.3 Token Store Options
- Relational DB: durability, auditing, device tracking.
- Redis: low latency, TTL eviction.
- Use hashing to protect tokens in storage.

---

## 19. Appendix: Quick Cheatsheet

- Disable CSRF for stateless APIs:
```java
// java
http.csrf(csrf -> csrf.disable());
```
- Make security stateless:
```java
// java
http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
```
- Add custom filter:
```java
// java
http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
```
- Create `SecurityFilterChain`:
```java
// java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { ... }
```

---