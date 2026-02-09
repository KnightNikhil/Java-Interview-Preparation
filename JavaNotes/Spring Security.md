# Spring Security Notes

---

## 1. Spring Security Core Concepts
- What is Spring Security and how the filter chain works
- Authentication vs Authorization
- SecurityContext and SecurityContextHolder
- Principal and GrantedAuthority

---

## 2. Authentication
- In-memory authentication
- JDBC authentication (using database users)
- Custom user details service (UserDetailsService)
- Password encoding (e.g., BCryptPasswordEncoder)
- AuthenticationManager and AuthenticationProvider
- Programmatic vs declarative authentication

---

## 3. Authorization
- Role-based access control (RBAC)
- \@PreAuthorize, \@PostAuthorize, \@Secured, \@RolesAllowed
- Method-level security (enable via \@EnableGlobalMethodSecurity)
- Expression-based access control

---

## 4. Security Filters and Chain
- Understanding the Spring Security filter chain
- Role of filters like UsernamePasswordAuthenticationFilter, BasicAuthenticationFilter, etc.
- Custom filter creation
- Ordering and precedence of filters

---

## 5. Form-Based and Basic Authentication
- Login page, logout mechanism
- HTTP Basic authentication (with REST)
- CSRF protection (disabling for APIs)

---

## 6. JWT Authentication
- Stateless authentication using JWTs
- Creating and verifying JWTs
- Storing JWT in headers (Bearer tokens)
- Filter to intercept and validate token
- Expiry, signature, and refresh token handling

---

## 7. OAuth2 & OpenID Connect
- Spring Security OAuth2 client and resource server
- Integrating with providers like Google, GitHub, Okta, Keycloak
- `spring-security-oauth2-client` and `spring-security-oauth2-resource-server`
- Scopes and userinfo endpoints

---

## 8. Security with API Gateway
- Global security filter for authentication/authorization
- Forwarding JWT tokens downstream
- Custom GatewayFilter for request validation

---

## 9. CORS and CSRF
- Difference between CORS and CSRF
- CORS configuration in Spring Security
- CSRF protection for browser clients
- Disabling CSRF for REST APIs

---

## 10. Session Management and Stateless Security
- Statefulness vs stateless authentication
- Session creation policy
- Handling session fixation and concurrent sessions
- Logout in stateless systems

---

## 11. Custom Authentication & Authorization
- Custom login authentication filter
- Custom AccessDecisionVoter and AccessDecisionManager
- Custom token-based or header-based authentication

---

## 12. Security Context Propagation
- In async scenarios (\@Async, CompletableFuture)
- Using DelegatingSecurityContextRunnable or SecurityContextAwareExecutor

---

## 13. Spring Boot 3.x & Spring Security 6+ Changes
- Lambda-based security configuration (SecurityFilterChain beans)
- Removal of WebSecurityConfigurerAdapter
- Declarative security using DSL-style config

---

## 14. Testing Secured Applications
- \@WithMockUser, \@WithUserDetails
- MockMvc + Spring Security
- Testing with JWT authentication filters

---

## 15. Best Practices
- Always hash passwords (e.g., BCrypt)
- Avoid storing tokens in local/session storage on frontend
- Secure all endpoints (deny-all-by-default strategy)
- Don’t expose internal endpoints (actuator, DB)
- Use HTTPS and set secure headers (CSP, HSTS, X-Frame-Options)


---

### 1. The Core Problem JWT Tries to Solve

JWT access tokens exist to answer one simple question:

“Is this request coming from an authenticated user, and what are they allowed to do?”

JWTs are:
- Stateless
- Self-contained
- Fast to validate (no DB hit)

So far so good.

---

### 2. The Fundamental Problem With JWT Access Tokens

Option A: Long-lived Access Token (Bad)

Access token valid for 7 days

What happens if:
- Token is stolen?
- Laptop compromised?
- Token leaked via logs?

❌ Attacker has full access for 7 days  
❌ No way to revoke it  
❌ Logout is meaningless

---

Option B: Short-lived Access Token (Better, but incomplete)

Access token valid for 10–15 minutes

Pros:
- Damage window is small
- Better security

But now a new problem appears:

❌ User gets logged out every 10–15 minutes  
❌ Terrible user experience

So we now have a trade-off:

Security vs User Experience
- Long token = Bad security
- Short token = Bad UX

---

### 3. This Is Where Refresh Token Comes In

Refresh token exists ONLY to solve this trade-off.

It allows you to keep access tokens short-lived without forcing the user to log in again

---

### 4. What a Refresh Token Actually Is (Important)

A refresh token is:
- Long-lived
- Stored server-side
- Used only to get a new access token
- Never used to call business APIs

It is NOT another JWT for APIs.

---

### 5. Two-Token Model (Correct Mental Model)

| Token | Purpose | Sent With Every Request? |
|---|---:|:---:|
| Access Token | Authorization | Yes |
| Refresh Token | Re-authentication | No |

---

### 6. The Actual Flow (Step-by-Step)

Login

User logs in  
↓  
Server issues:
- Access Token (15 min)
- Refresh Token (7 days)

---

Normal API Call

Client → API Gateway  
Authorization: Bearer ACCESS_TOKEN

No DB call. Fast. Stateless.

---

Access Token Expires

API returns 401 Unauthorized

Client does not ask user to login.

Instead:

Client → AuthService /refresh  
Body: refreshToken

---

Refresh Token Validation

AuthService:
1. Checks refresh token exists in DB
2. Checks it is not expired
3. Checks it is not revoked

If valid:

Issue new access token

---

Result

User continues seamlessly.  
No login screen.  
No session expiry frustration.

---

### 7. Why Refresh Token Must Be Stateful

This is critical.

Access tokens:
- Stateless
- Cannot be revoked

Refresh tokens:
- Stored in DB
- Can be revoked
- Can be rotated
- Can be audited

This gives you:
- Logout support
- Compromised token recovery
- Device-level control

---

### 8. What Happens Without Refresh Token (Real Scenario)

Banking Example (relevant to you)

Imagine:
- Access token valid for 24 hours
- Token leaks

Attacker:
- Transfers money
- Views statements
- Changes profile

For 24 hours, bank is helpless.

Now with refresh token:
- Access token lasts 10 minutes
- Bank revokes refresh token
- Attacker is dead in 10 minutes

That is why banks require refresh tokens.

---

### 9. Logout Is Impossible Without Refresh Tokens

Without refresh token:
- Logout = client deletes token
- Token still valid on server

With refresh token:
- Logout = server deletes refresh token
- User can never get new access token

This is real logout.

---

### 10. Why Not Just Store JWT in DB?

People ask this a lot.

If you store access tokens in DB:
- You lose stateless benefit
- Every request hits DB
- Scalability drops
- Microservices coupling increases

Refresh token avoids this.

---

### 11. In Your MediConnect Microservices

Without refresh token:
- Access token must be long-lived
- Any token leak compromises patient data
- HIPAA-style compliance impossible

With refresh token:
- Access tokens are short
- AuthService controls sessions
- Gateway and services stay stateless
- Proper healthcare-grade security

---

### 12. Interview-Grade Explanation (Memorize This)

“Refresh tokens exist to decouple security from user experience.  
They allow short-lived access tokens for security while maintaining seamless user sessions.  
Access tokens remain stateless and fast, while refresh tokens are stateful and revocable.”

If you say this in an interview, the panel knows you understand it.

---

### 13. When You Don’t Need Refresh Tokens

Being honest is senior-level.

You don’t need refresh tokens when:
- Internal service-to-service auth
- One-time tokens
- CLI tools
- Very low-risk apps

But for:
- Healthcare
- Banking
- Consumer apps
- Long sessions

They are mandatory.

---

### 14. One Line Summary

Access token = permission  
Refresh token = continuity

---

If you want next, I can:
- Explain refresh token rotation
- Explain why refresh token must never be JWT
- Show Postman flow
- Explain OAuth 2.0 vs custom JWT flow

Just say the word.

----

Perfect. Now we go inside the engine.

I’ll explain refresh tokens internally, exactly what happens at runtime, data structures, thread flow, Spring Security hooks, and why each step exists.

No hand-waving. This is senior-level internal understanding.

---

## 1. What a Refresh Token Actually Is

A refresh token is NOT:
- a JWT (often it isn’t)
- proof of identity
- used on every request

A refresh token is:

A long-lived secret that allows issuing new access tokens

It represents:
- user
- device/session
- permission to continue authentication

---

## 2. Internal Components Involved

Let’s name the moving parts.

Server Side
- AuthController
- AuthenticationManager
- UserDetailsService
- RefreshTokenService
- TokenStore (DB / Redis)
- JwtTokenProvider

Client Side
- Access Token (short)
- Refresh Token (long)

---

## 3. Data Model (Very Important)

Refresh Token Table (Typical)

`refresh_token`  
--------------  
- id (UUID)  
- user_id  
- token_hash  
- issued_at  
- expires_at  
- revoked (boolean)  
- device_id  
- ip_address

⚠️ Never store raw token, only hash.

---

## 4. Login Flow (Step-by-Step)

Step 1: Credential Authentication

POST /auth/login  
username + password

Internally:
1. AuthenticationManager.authenticate()
2. Calls loadUserByUsername() → DB
3. Password verified

---

Step 2: Token Generation

String accessToken = jwtProvider.generate(user);  
String refreshToken = randomSecureToken();

Internally:
- Access token = signed JWT
- Refresh token = cryptographically random (256-bit)

---

Step 3: Persist Refresh Token

refreshTokenRepository.save(  
hash(refreshToken),  
userId,  
expiry  
);

Why?
- Enables revocation
- Enables logout
- Enables device tracking

---

Step 4: Return Tokens

Response:
{
accessToken,
refreshToken
}

Client stores:
- Access token → memory
- Refresh token → HttpOnly cookie / secure storage

---

## 5. Normal API Call (No Refresh Token)

GET /appointments  
Authorization: Bearer access-token

Internally:
1. JwtAuthenticationFilter
2. Validate signature
3. Check expiry
4. Build Authentication object
5. Set SecurityContext

🚀 No DB call

---

## 6. Access Token Expires (Critical Moment)

JWT validation fails:

ExpiredJwtException

Spring Security:
- Rejects request with 401

Client now triggers refresh flow.

---

## 7. Refresh Flow (Internals)

POST /auth/refresh  
refresh_token

Step-by-Step Internals

Step 1: Token Lookup

hashed = hash(refreshToken);  
token = refreshTokenRepository.findByHash(hashed);

DB / Redis call happens only here.

---

Step 2: Validations

- Exists?  
- Expired?  
- Revoked?  
- Belongs to user?  
- Same device?

Any failure → force re-login.

---

Step 3: Rotation (Security Critical)

revoke(oldToken);  
newToken = generateNewRefreshToken();  
save(newToken);

Why rotation?
- Prevent replay attacks
- Stolen refresh token becomes useless

---

Step 4: Issue New Access Token

String newAccessToken = jwtProvider.generate(user);

Return:
- accessToken (new)
- refreshToken (new)

---

## 8. Logout Internals

POST /auth/logout  
refresh_token

Internally:
refreshTokenRepository.revoke(token);

No JWT invalidation needed.

Access token:
- Dies naturally

---

## 9. Why NOT JWT for Refresh Token?

Because JWT:
- Cannot be revoked
- Can be replayed
- Encourages long expiry

Refresh tokens must be controllable.

---

## 10. Thread Safety & Concurrency

Problem
- Two refresh requests at same time.

Solution
- DB unique constraint
- Transactional revoke + insert
- Optimistic locking

Result:
- One succeeds
- Other fails

---

## 11. Why Hash Refresh Tokens?

If DB leaks:
- Raw tokens = account takeover

Hashing:
- Same model as passwords
- Zero trust storage

---

## 12. Spring Security Integration Points

Layer | Role
---|---
Filter | JWT validation
Controller | Refresh endpoint
Service | Token lifecycle
Repository | Persistence
SecurityConfig | Stateless policy

---

## 13. Why This Design Is “Internally Correct”

Problem | Solved
---|---
DB on every request | No
Logout | Yes
Compromise handling | Yes
Multi-device | Yes
Scalability | Yes

---

## 14. Internal State Diagram (Mental)

[LOGIN]  
↓  
[ACCESS OK] → [EXPIRED]  
↓  
[REFRESH]  
↓  
[ROTATED]

---

## 15. Interview-Level Explanation (Use This)

“Internally, refresh tokens are persisted secrets tied to a user and device. On refresh, the server validates the token from storage, rotates it to prevent replay, and issues a new stateless access token. This design keeps request processing stateless while retaining centralized control over session lifecycle.”

---

## 16. One Line to Remember Forever

Access token = computation  
Refresh token = coordination

---

If you want next, I can:
- Implement this line-by-line in your repo
- Show Redis-based implementation
- Explain OAuth2 refresh vs custom
- Simulate token theft scenarios

Just tell me what to do next.

---

This is a fundamental systems concept, not just Spring Security.  
I’ll explain it cleanly, with intuition, with Spring Security mapping, and with real-world analogies.

No jargon dumping.

---

## 1. One-Line Definitions (First Principles)

**Stateless**

The server does not remember anything about the client between requests.

**Stateful**

The server remembers something about the client between requests.

That’s it. Everything else is consequence.

---

## 2. Visual Mental Model

**Stateless**

Request 1 → Server (no memory)  
Request 2 → Server (no memory)  
Request 3 → Server (no memory)

Each request must be self-sufficient.

**Stateful**

Request 1 → Server (stores state)  
Request 2 → Server (uses stored state)  
Request 3 → Server (uses stored state)

The server depends on past interactions.

---

## 3. Real-World Analogy (Best Way to Lock It In)

**Stateless = ATM Card**
- You insert card + PIN every time
- ATM doesn’t remember you
- Each transaction stands alone

**Stateful = Hotel Reception**
- You check in
- They remember your room, stay, preferences
- Next interaction depends on earlier one

---

## 4. HTTP Itself Is Stateless

This is crucial.

HTTP does not remember:
- Who you are
- What you did before
- Whether you are logged in

That’s why:
- Cookies
- Sessions
- Tokens

exist.

---

## 5. Stateful Authentication (Session-Based)

How it works

Login  
↓  
Server stores session in memory / DB  
↓  
Client sends session ID cookie  
↓  
Server looks up session on every request

Characteristics

Aspect | Stateful
---|---
Server memory | Required
DB / Cache | Required
Horizontal scaling | Hard
Logout | Easy
Revocation | Easy

Spring Security (Session):
- HttpSession
- SecurityContext stored in session

---

## 6. Stateless Authentication (JWT)

How it works

Login  
↓  
Server issues JWT  
↓  
Client sends JWT every request  
↓  
Server verifies JWT (no memory)

Characteristics

Aspect | Stateless
---|---
Server memory | Not required
DB lookup | Not required
Scaling | Very easy
Logout | Hard
Revocation | Not possible

JWT is proof, not a session.

---

## 7. Why JWT Is Stateless (Key Insight)

JWT contains:
- User ID
- Roles
- Expiry
- Signature

So server can say:

“I can trust this token without remembering you”

This is why:
- No DB
- No session
- No cache

---

## 8. Why Refresh Token Is Stateful

Now the important contrast.

Refresh tokens:
- Stored in DB / Redis
- Tied to user + device
- Can be revoked

So:

Refresh token = state

---

## 9. Hybrid Model (Production-Grade)

Modern systems combine both.

Component | Nature
---|---
Access Token (JWT) | Stateless
Refresh Token | Stateful

This gives:
- Performance
- Scalability
- Security
- Control

This is what you are building in MediConnect.

---

## 10. Spring Security Mapping (Very Important)

**Stateless**

sessionManagement()  
.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

Means:
- Spring will NOT create HttpSession
- SecurityContext exists only per request

**Stateful**

sessionManagement()  
.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

Means:
- Session created
- SecurityContext stored server-side

---

## 11. Why Microservices Prefer Stateless

Because:

Problem | Stateful | Stateless
---|---:|---:
Load balancing | Sticky sessions | Any node
Auto-scaling | Hard | Easy
Failover | Painful | Seamless
Latency | DB/Cache hit | Pure CPU

This is why API Gateways must be stateless.

---

## 12. Common Confusion (Clear This)

“Stateless means server has no data”

❌ Wrong

Stateless means:
- Server does not remember client-specific session data

Your DB still exists.  
Your Kafka still exists.  
Your business data still exists.

---

## 13. Interview-Grade Explanation

Say this:

“Stateless means the server does not store client session information between requests. Each request contains all information needed for authentication. Stateful means the server maintains session context across requests, requiring server-side storage and lookup.”

This is perfect.

---

## 14. One Line to Remember Forever

Stateless = proof  
Stateful = memory

---

## 15. Mapping Back to Your Question

Question | Answer
---|---
Why no DB call for JWT? | Stateless
Why refresh token needs DB? | Stateful
Why short-lived access token? | No revocation
Why logout uses refresh token? | Server memory

---

If you want next, I can:
- Show request lifecycle diagram
- Compare JWT vs OAuth sessions
- Explain why Kafka consumers are stateful
- Show why Redis is often used for refresh tokens

Just tell me.