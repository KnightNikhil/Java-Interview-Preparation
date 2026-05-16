# Unit Testing, Integration, and Service Virtualization in Java

---

## 1. Unit Testing with JUnit & Mockito

### A. JUnit 5 (Jupiter) Basics

#### 1. Test Lifecycle Annotations

- `@Test`: Marks a method as a test case.
- `@BeforeEach`: Runs before each test method.
- `@AfterEach`: Runs after each test method.
- `@BeforeAll`: Runs once before all test methods in the class.
- `@AfterAll`: Runs once after all test methods in the class.

```java
@BeforeEach
void setUp() {
    // Runs before each test
}

@Test
void testAddition() {
    assertEquals(4, 2 + 2);
}
```

#### 2. Assertions

- `assertEquals`: Checks if two values are equal.
- `assertTrue`: Checks if a condition is true.
- `assertThrows`: Verifies that a specific exception is thrown.

```java
assertTrue(list.isEmpty());
assertThrows(IllegalArgumentException.class, () -> methodThatThrows());
```

#### 3. Parameterized Tests

- `@ParameterizedTest`: Allows running the same test with different parameters.

```java
@ParameterizedTest
@ValueSource(ints = {1, 2, 3})
void testWithParameters(int number) {
    assertTrue(number > 0);
}
```

#### 4. Nesting Tests

- `@Nested`: Groups related test cases within a parent test class for better organization. Each nested class can have its own setup/teardown methods.

```java
class CalculatorTest {

    @Nested
    class AdditionTests {
        @Test
        void addsTwoPositiveNumbers() {
            Assertions.assertEquals(5, 2 + 3);
        }
    }

    @Nested
    class SubtractionTests {
        @Test
        void subtractsTwoNumbers() {
            Assertions.assertEquals(1, 3 - 2);
        }
    }
}
```

#### 5. Disabling Tests

- `@Disabled`: Temporarily disables a test or test class.

---

**Hands-on:**  
Write test classes for a simple Calculator or BankingService.

---

### B. Mockito – Mocking Dependencies

#### 1. Mockito Annotations

- `@Mock`: Creates a mock object.
- `@Spy`: Creates a partial mock, allowing real method calls unless stubbed.
- `@InjectMocks`: Automatically injects mocks into the class under test.

```java
@Mock
private PaymentGateway gateway;
```

```java
@Spy
private List<String> realList = new ArrayList<>();
```

**Mock vs Spy Example:**

```java
// Using @Mock: All methods return default values unless stubbed
@Mock
List<String> mockList;

@Test
void testMock() {
    when(mockList.size()).thenReturn(10);
    assertEquals(10, mockList.size()); // Returns 10 (stubbed)
    assertNull(mockList.get(0));       // Returns null (default)
}

// Using @Spy: Real methods are called unless stubbed
@Spy
List<String> spyList = new ArrayList<>();

@Test
void testSpy() {
    spyList.add("A");
    assertEquals(1, spyList.size());   // Calls real method, returns 1
    when(spyList.size()).thenReturn(5);
    assertEquals(5, spyList.size());   // Returns 5 (stubbed)
}
```

- On a mock, methods return default values and have no side effects unless stubbed.
- On a spy, real methods are called unless stubbed.

**Injecting Mocks:**

Without `@InjectMocks`:

```java
PaymentGateway gateway = Mockito.mock(PaymentGateway.class);
PaymentService service = new PaymentService();
service.setGateway(gateway); // manual injection
```

With `@InjectMocks`:

```java
class PaymentServiceTest {
    @Mock
    PaymentGateway gateway;

    @InjectMocks
    PaymentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPayment() {
        // gateway is a mock injected into service
        // Write test logic here
    }
}
```

- `MockitoAnnotations.openMocks(this);` initializes fields annotated with `@Mock`, `@Spy`, and `@InjectMocks`.

#### 2. Stubbing and Verifying

- STUBBING - `when(...).thenReturn(...)`: Defines mock behavior.
- `verify(...)`: Verifies interactions with the mock.

```java
when(gateway.charge(anyDouble())).thenReturn(true);
verify(gateway).charge(100.0);
```

#### 3. Mocking Exceptions

- `thenThrow(...)`: Simulates exceptions being thrown by a mock.

```java
when(gateway.charge(anyDouble())).thenThrow(new RuntimeException("Payment failed"));
```

#### 4. Argument Matchers

- `any()`: Matches any argument of the specified type.
- `eq()`: Matches a specific value.
- `argThat()`: Custom matcher for complex conditions.

```java
when(gateway.charge(anyDouble())).thenReturn(true);
verify(gateway).charge(eq(100.0));
verify(gateway).charge(argThat(amount -> amount > 0));
```

---

**Hands-on:**  
Write tests for services depending on DAOs or APIs (e.g., PaymentService → PaymentGateway).

---

### C. Advanced Unit Testing

#### 1. Mockito Annotations vs Manual Mocks

- Mockito annotations (`@Mock`, `@Spy`, `@InjectMocks`) simplify mock creation and injection.
- Manual mocks require explicit instantiation and setup.
- Using annotations reduces boilerplate and improves readability.

#### 2. Mocking Static Methods

- With `mockito-inline` (Java 8+):

```java
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;

class Utils {
    static String greet(String name) {
        return "Hello, " + name;
    }
}

class UtilsTest {
    @Test
    void testStaticMethodMocking() {
        try (MockedStatic<Utils> mocked = mockStatic(Utils.class)) {
            mocked.when(() -> Utils.greet("Nikhil")).thenReturn("Hi, User");
            assertEquals("Hi, User", Utils.greet("User"));
        }
        // Outside the block, Utils.greet behaves normally
    }
}
```

- With PowerMockito (for legacy code):

```java
import static org.powermock.api.mockito.PowerMockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class UtilsTest {
    @Test
    public void testStaticMethod() {
        mockStatic(Utils.class);
        when(Utils.greet("Nikhil")).thenReturn("Hi, User");
        assertEquals("Hi, User", Utils.greet("Nikhil"));
    }
}
```

#### 3. Reflection in Testing

- Use Java’s reflection API to access, modify, or invoke private fields, methods, or constructors (generally discouraged; prefer testing via public API).

```java
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyClass {
    private int secret = 42;
}

class MyClassTest {
    @Test
    void testPrivateField() throws Exception {
        MyClass obj = new MyClass();
        Field field = MyClass.class.getDeclaredField("secret");
        field.setAccessible(true);
        int value = (int) field.get(obj);
        assertEquals(42, value);
    }
}
```

#### 4. Edge Cases and Failure Scenarios

- Focus on testing boundary conditions and error handling.

#### 5. Coverage Analysis

- Use Jacoco plugin to measure test coverage and identify untested code.

---

## 2. Service Virtualization (WireMock)

### Purpose

Service virtualization simulates external systems (APIs, databases, third-party services) so you can test your application in isolation. WireMock is a popular Java tool for this.

### What is WireMock?

WireMock is a flexible library for stubbing and mocking web services. It allows you to:

1. Simulate HTTP APIs (REST, SOAP, etc.)
2. Define expected requests and stub responses
3. Simulate delays, errors, and dynamic responses
4. Verify that your application made the expected HTTP calls

### Why use WireMock?

1. Isolate tests from real external services.
2. Control scenarios (simulate edge cases, errors, slow responses).
3. Ensure tests are deterministic and repeatable.

### How does WireMock work?

- WireMock runs as a local HTTP server.
- You configure it to expect certain requests and return predefined responses.
- Your application is pointed to WireMock instead of the real service during tests.

### Basic Usage Example

#### 1. Add WireMock dependency (Maven)

```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.31.0</version>
    <scope>test</scope>
</dependency>
```

#### 2. Start WireMock in your test

```java
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

WireMockServer wireMockServer = new WireMockServer(8080);
wireMockServer.start();
```

#### 3. Stub an endpoint

```java
stubFor(get(urlEqualTo("/api/user/1"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"id\":1,\"name\":\"Nikhil\"}")));
```

#### 4. Test your code against WireMock

- Point your application’s API client to `http://localhost:8080/api/user/1` to receive the stubbed response.

#### 5. Verify requests

```java
verify(getRequestedFor(urlEqualTo("/api/user/1")));
```

#### 6. Stop WireMock after tests

```java
wireMockServer.stop();
```

### Advanced Features

- **Simulate delays:** `.withFixedDelay(2000)` to test timeouts.
- **Simulate errors:** `.withStatus(500)` for server errors.
- **Dynamic responses:** Use response templating for dynamic data.

### Typical Use Cases

- Testing code that calls payment gateways, third-party APIs, or microservices.
- Simulating failures or slow responses.
- Running integration tests in CI/CD pipelines without real dependencies.

### Standalone vs Embedded

- **Standalone server:** Run as a separate process (JAR or Docker). Configure stubs via REST API or files.
- **Embedded in tests:** Start/stop a `WireMockServer` instance in your test code (JUnit, etc.). Most common for automated tests.

### REST, SOAP, and More

- **REST APIs:** Native support for HTTP(S) REST endpoints.
- **SOAP:** Stub SOAP endpoints (match on URL and request body, return XML).
- **Other protocols:** WireMock is limited to HTTP/HTTPS.

### Simulating Authentication, Headers, Query Parameters

- **Authentication:** Stub endpoints requiring specific headers (e.g., Authorization).

```java
stubFor(get(urlEqualTo("/api/secure"))
    .withHeader("Authorization", equalTo("Bearer token123"))
    .willReturn(aResponse().withStatus(200)));
```

- **Headers:** Match or assert on any HTTP header.
- **Query parameters:**

```java
stubFor(get(urlPathEqualTo("/api/user"))
    .withQueryParam("id", equalTo("1"))
    .willReturn(aResponse().withStatus(200)));
```

### Full JUnit Test Example with WireMock

```java
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class UserServiceTest {
    static WireMockServer wireMockServer;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
        stubFor(get(urlEqualTo("/api/user/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"id\":1,\"name\":\"Nikhil\"}")));
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void testGetUser() {
        // Call your API client here, e.g., userService.getUser(1)
        // Assert the response matches the stubbed data
        verify(getRequestedFor(urlEqualTo("/api/user/1")));
    }
}
```

### Using WireMock with Spring Boot’s `@AutoConfigureWireMock`

- `@AutoConfigureWireMock` starts WireMock on a random port and injects it into your test context.

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0) // random port
class MyApiIntegrationTest {
    @Test
    void testExternalApiCall() {
        stubFor(get(urlEqualTo("/external/api"))
            .willReturn(aResponse().withStatus(200).withBody("OK")));
        // Test your service that calls /external/api
    }
}
```

---

## 3. Integration & Functional Testing (Spring Boot)

### A. Spring Boot Integration Testing

#### 1. `@SpringBootTest` vs `@WebMvcTest`

- `@SpringBootTest`: Loads the full application context (all beans, configs, dependencies). Used for end-to-end or integration tests.

```java
@SpringBootTest
class MyServiceIntegrationTest {
    @Test
    void contextLoads() {
        // Test with full context
    }
}
```

- `@WebMvcTest`: Loads only the web layer (controllers, filters, etc.), not services or repositories. Used for controller tests.

```java
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    void testGetAllAccounts() throws Exception {
        Account acc = new Account();
        acc.setId(1L);
        acc.setOwner("Nikhil");
        acc.setBalance(1000.0);

        given(accountRepository.findAll()).willReturn(Arrays.asList(acc));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owner").value("Nikhil"));
    }

    @Test
    void testGetAccountById() throws Exception {
        Account acc = new Account();
        acc.setId(1L);
        acc.setOwner("Nikhil");
        acc.setBalance(1000.0);

        given(accountRepository.findById(1L)).willReturn(Optional.of(acc));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Nikhil"));
    }
}
```

#### 2. Use of `@MockBean` in Integration Tests

- `@MockBean`: Replaces a bean in the application context with a Mockito mock.

```java
@WebMvcTest(MyController.class)
class MyControllerTest {
    @MockBean
    private MyService myService; // Replaces real MyService with a mock

    @Autowired
    private MockMvc mockMvc;
}
```
**NOTE:** : 
1. If no @MockBean for Service, then Test fails with UnsatisfiedDependencyException
2. @WebMvcTest is meant for focused, isolated controller tests, not full integration.
3. Use @SpringBootTest for full integration tests with all beans.

4. **Difference:**

- `@Mock` (Mockito): Creates a mock for unit tests, not registered in Spring context.
- `@MockBean` (Spring Boot): Creates a mock and replaces the real bean in the Spring context.

**Example:**

Mockito `@Mock` (no Spring context):

```java
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Test
    void testFindUser() {
        MockitoAnnotations.openMocks(this);
        UserService service = new UserService(userRepository); // manual injection
        // define mock behavior and test
    }
}
```

Spring Boot `@MockBean` (with Spring context):

```java
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRepository userRepository; // automatically injected into UserController

    // test methods using mockMvc
}
```

- Use `@Mock` for plain unit tests (no Spring context).
- Use `@MockBean` for Spring tests to mock and inject dependencies into the Spring context.

#### 3. Testing Controllers Using MockMvc

- `MockMvc` simulates HTTP requests to controller endpoints without starting a real server.

```java
@WebMvcTest(MyController.class)
class MyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/hello"))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello World"));
    }
}
```

#### 4. TestRestTemplate or WebTestClient (for REST APIs)

- `TestRestTemplate`: For integration tests with a running server (non-reactive).

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyApiTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGet() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/hello", String.class);
        assertEquals("Hello World", response.getBody());
    }
}
```

- `WebTestClient`: For reactive applications or WebFlux.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyReactiveApiTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGet() {
        webTestClient.get().uri("/api/hello")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello World");
    }
}
```

#### 5. Use `@DataJpaTest` to Test Repository Layer

- Loads only JPA components (repositories, entities, DataSource).
- Uses in-memory DB by default.

```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        User user = new User("Nikhil");
        userRepository.save(user);
        Optional<User> found = userRepository.findByName("Nikhil");
        assertTrue(found.isPresent());
    }
}
```

---

**Hands-on:**
- Write integration tests for a REST API (`/api/accounts`)
- Use H2 DB for testing repositories

---

### B. Functional Testing Concepts

1. **End-to-end behavior testing:** Test the complete workflow as a user would interact with the system (e.g., registration, login, fund transfer).
2. **Simulate real HTTP requests:** Use `MockMvc`, `TestRestTemplate`, or `WebTestClient` in Spring Boot tests.
3. **Validate responses, status codes, and headers:** Check that the API returns the correct data, HTTP status codes, and headers.
4. **Use TestContainers for real DBs (optional):** Run a real database (like PostgreSQL or MySQL) in a Docker container during tests for realistic integration.

**Difference:**

- **Integration Testing:** Checks if different components/layers work together (often with in-memory DBs or mocks).
- **Functional Testing:** Validates complete business features as a user would experience them (often with real DBs and all layers).

---

**Hands-on:**
- Test user flows like “Register → Login → Transfer Funds → Get Balance”

---

## 4. Mutation Testing

### A. Using PIT (Pitest)

1. Add PIT plugin to `pom.xml` or `build.gradle`.
2. Run mutation tests via CLI or IDE.
3. Interpret mutation score (survived mutants vs killed).
4. Refactor test cases to improve coverage.

---

**Hands-on:**
- Run PIT on an existing project
- Improve test quality to kill more mutants

---

### Junit 4 vs 5

| Aspect	                 | JUnit 4	                                                                      | JUnit 5 |
|-------------------------|-------------------------------------------------------------------------------|-------------------------------|
| Architecture	           | Single, monolithic junit-4.x.jar.	                                            | Modular:  1. JUnit Platform (launching framework)  2. JUnit Jupiter (new programming & extension model)  3. JUnit Vintage (to run JUnit 3/4 tests).| 
| Annotations	            | - @Test  - @Before  - @After  - @BeforeClass  - @AfterClass  - @Ignore	       | - @Test  - @BeforeEach  - @AfterEach  - @BeforeAll  - @AfterAll  - @Disabled  - New: @DisplayName, @Nested, @Tag, @ParameterizedTest| 
| Test Class Visibility	  | Test classes & methods must be public.	                                       | Can be package-private (no modifier). Public not required.| 
| Assertions	             | From org.junit.Assert (e.g. assertEquals).	                                   | From org.junit.jupiter.api.Assertions.  Also supports lambdas & better messages: assertThrows(Exception.class, () -> {...});| 
| Assumptions	            | Assume.assumeTrue().	org.junit.jupiter.api.Assumptions with lambdas.          |
| Parameterized Tests	    | @RunWith(Parameterized.class) → verbose & awkward.	                           | Native @ParameterizedTest with flexible sources (@ValueSource, @CsvSource, @MethodSource, etc.).| 
| Extensions	             | @RunWith (only one per class).	                                               | New extension model (@ExtendWith), allows multiple extensions (e.g. Spring, Mockito). Much more powerful.| 
| Test Suites	            | @RunWith(Suite.class) & @SuiteClasses.	                                       | No special suite class. Use build tool (Maven/Gradle) or tags (@Tag) to group tests.| 
| Static Imports	         | Assertions often used with static import (import static org.junit.Assert.*).	 | Same idea, but from Assertions API.| 
| Execution	              | Only via JUnit Runner.	                                                       | More flexible: JUnit Platform supports running tests from IDEs, build tools, or custom launchers.| 
| Dynamic Tests	          | Not supported.	                                                               | Supported: @TestFactory lets you generate tests at runtime.| 
| Backward Compatibility	 | Only JUnit 4 tests.	                                                          | JUnit Vintage lets you run old JUnit 3/4 tests on JUnit 5 platform.| 


**Example Differences**

JUnit 4
```java
import org.junit.*;

public class CalculatorTest {
@BeforeClass
public static void initAll() { ... }

    @Before
    public void init() { ... }

    @Test
    public void testAddition() {
        Assert.assertEquals(4, 2 + 2);
    }

    @After
    public void tearDown() { ... }

    @AfterClass
    public static void tearDownAll() { ... }
}

```
JUnit 5

```java
import org.junit.jupiter.api.*;

class CalculatorTest {   // No need for public
@BeforeAll
static void initAll() { ... }

    @BeforeEach
    void init() { ... }

    @Test
    void testAddition() {
        Assertions.assertEquals(4, 2 + 2);
    }

    @AfterEach
    void tearDown() { ... }

    @AfterAll
    static void tearDownAll() { ... }
}

```

**Key Takeaway**
- **JUnit 4** → Simple but rigid (everything public, single runner, limited extensions).
- **JUnit 5 →** Modern, modular, more expressive (package-private allowed, parameterized tests built-in, extension model, dynamic tests).


### Java Access Modifiers 

In Java, you can mark classes/methods with different visibility levels:
	-	**public** → visible everywhere.
	-	**protected** → visible in the same package + subclasses.
	-	**private** → visible only inside the same class.
	-	**no modifier (default) → called package-private** → visible only within the same package.

**Example:**
```java
class MyClass {   // no modifier → package-private
    void myMethod() {   // no modifier → package-private
        System.out.println("Hello");
    }
}
```

Here, MyClass and myMethod() are accessible only inside the same package.


**JUnit 4 vs JUnit 5 wrt access modifiers**

JUnit 4
-	Required public classes and public test methods.
-	Example:

```java
public class MyTests {
    @Test
    public void testSomething() { ... }
}
```
-	If class/methods weren’t public, tests wouldn’t run because JUnit 4’s reflection rules were stricter.


JUnit 5 (Jupiter)
-	Relaxed rules to allow package-private classes and methods.
-	Example:
```java
class MyTests {   // package-private class
    @Test
    void testAddition() {   // package-private method
        assertEquals(4, 2 + 2);
    }
}
```

✅ This works in JUnit 5.
❌ This fails in JUnit 4 (test won’t even be discovered).

**Why did JUnit 5 allow package-private?**

Two main reasons:
1.	Cleaner test code – You don’t need to expose test classes/methods publicly since they’re only meant for internal testing.
2.	Reflection in JUnit 5 uses the java.lang.reflect API with relaxed accessibility rules → it can call methods even if they’re package-private.

**What about private?**
-	If you make a test class or test method private, even JUnit 5 cannot access it → tests won’t run.
-	Example:
```java
private class MyTests {  // ❌ Won’t run
    @Test
    private void testSomething() {  // ❌ Won’t run
        ...
    }
}
```

**Summary**
-	JUnit 4 → Only public classes & methods are allowed.
-	JUnit 5 → public or package-private (default modifier) classes & methods work.
-	private → Never works (JUnit can’t see them).


## Spring Data JPA Testing with @DataJpaTest
Spring Data testing with `@DataJpaTest` focuses on validating JPA repository logic and custom queries using in-memory databases like H2, with transactional isolation. Mastering these techniques is vital for interviews and real-world development[1][2][3][4][5].

### Unit Testing Repositories with @DataJpaTest

- `@DataJpaTest` is a Spring Boot annotation that creates a minimal application context, loading only JPA components and repositories. It disables full auto-configuration, focusing tests solely on the persistence layer.
- Tests annotated this way use an embedded database by default and do not load web, service, or other beans, making them lightweight and fast.
- Usage:
```java
@DataJpaTest
public class MyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // Optional for direct DB operations

    @Autowired
    private MyRepository repository;

    @Test
    public void should_store_an_entity() {
        MyEntity entity = new MyEntity("data");
        repository.save(entity);
        // assert repository behavior or DB state
    }
}
```
- The `TestEntityManager` helps in setting up or cleaning initial data for assertions[1][5].

### Using In-Memory Databases (H2)

- By default, Spring Boot uses H2 (or HSQL/Derby) for tests if the dependency is present. This allows tests to run without connecting to an external database[2][6].
- You configure H2 usage in `src/test/resources/application.properties`:
```
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=sa
```
- In-memory databases make tests repeatable, quick, and easy to clean up. However, be aware of edge cases (SQL dialect differences between test and production DBs)[2][6][7].

### Testing Queries and Repositories

- `@DataJpaTest` is ideal for asserting correct behavior of repository methods: CRUD, custom finders, or JPQL/native queries.
- Typical assertions include:
	- Saving entities and reading them back.
	- Finding by ID, querying with conditions (e.g., `findByNameContaining()`).
	- Verifying results of custom JPQL or native queries.
- Example:
```java
@Test
public void shouldFindByStatus() {
    MyEntity e1 = new MyEntity("active");
    repository.save(e1);
    List<MyEntity> results = repository.findByStatus("active");
    assertEquals(1, results.size());
}
```
- You can use SQL seed scripts with `@Sql("classpath:seed-data.sql")` to preload test data before each test[1][4].

### Transaction Rollback after Tests

- By default, Spring test machinery executes each test method in a transaction that is automatically rolled back after the method finishes, ensuring database isolation and preventing cross-test contamination[8][9][10][11].
- This means every change made by a test (`@DataJpaTest`, `@Transactional`) is undone after the test completes.
- You do not need manual clean-up code—Spring guarantees a “clean” DB for each test method.
- Example behavior:
	- Insert or modify data in test.
	- Assert behavior.
	- Once test finishes, transaction rolls back, leaving no trace for subsequent tests.

### Key Points

| Concept                         | Core Purpose/Benefit                                                      | Implementation/Note                                 |
|----------------------------------|---------------------------------------------------------------------------|-----------------------------------------------------|
| `@DataJpaTest`                   | Isolated JPA/repository testing, boots fast                               | Loads only JPA layer, uses embedded DB, disables web|
| In-memory DBs (H2)               | Quick, repeatable tests; clean environment every run                      | Used by default, configure in test properties       |
| Query/Repo tests                 | Validates correct behavior for CRUD, custom queries                       | Use standard and custom methods, assert results     |
| Rollback after test              | Ensures isolation, prevents side-effects across tests                     | Automatic via Spring; no manual clean-up needed     |

----

JUnit 5 @ExtendWith and Testcontainers — Interview Preparation Documentation

⸻

Table of Contents

1. Introduction
2. JUnit 4 vs JUnit 5
3. What is @ExtendWith
4. Why JUnit 5 Introduced Extension Model
5. Internal Working of Extensions
6. Common JUnit 5 Extensions
7. SpringExtension
8. MockitoExtension
9. Multiple Extensions in JUnit 5
10. Lifecycle Callbacks in Extensions
11. Custom Extensions
12. Parameter Injection
13. Interview Questions on @ExtendWith
14. What is Testcontainers
15. Why Testcontainers Were Introduced
16. Problems With Traditional Integration Testing
17. H2 vs Real Database Testing
18. How Testcontainers Work Internally
19. @Testcontainers
20. @Container
21. Static vs Non-Static Containers
22. Spring Boot + Testcontainers Integration
23. @DynamicPropertySource
24. PostgreSQL Testcontainer Example
25. Redis Testcontainer Example
26. Kafka Testcontainer Example
27. Testcontainers Lifecycle
28. Local Development With Testcontainers
29. Testcontainers in CI/CD
30. Benefits of Testcontainers
31. Drawbacks of Testcontainers
32. Best Practices
33. Integration Testing Strategy
34. Real-World Use Cases
35. Advanced Concepts
36. Common Interview Questions
37. Senior-Level Talking Points
38. Final Interview Answers

⸻

1. Introduction

Modern enterprise applications require:

* reliable integration testing
* production-like testing environments
* modular testing architecture
* realistic infrastructure validation

JUnit 5 and Testcontainers together solve many limitations of traditional testing approaches.

These concepts are extremely important for:

* Spring Boot applications
* microservices
* distributed systems
* backend engineering interviews

⸻

2. JUnit 4 vs JUnit 5

JUnit 4 Problem

JUnit 4 used:

@RunWith(...)

Example:

@RunWith(SpringRunner.class)

The major limitation:

* only ONE runner allowed per test class

This created problems when multiple frameworks needed integration simultaneously.

Example:

* Spring support
* Mockito support
* custom logging
* retry handling

Only one could be used cleanly.

⸻

3. What is @ExtendWith

JUnit 5 replaced:

@RunWith

with:

@ExtendWith

This introduced:

Extension Model

The extension model allows:

* multiple extensions together
* modular testing architecture
* lifecycle interception
* dependency injection
* advanced customization

⸻

4. Why JUnit 5 Introduced Extension Model

The extension model was introduced to solve:

* rigidity of single-runner architecture
* lack of composability
* poor extensibility
* framework integration limitations

JUnit 5 follows:

composition over inheritance

Instead of:

one giant runner

JUnit 5 allows:

multiple modular extensions

⸻

5. Internal Working of Extensions

Extensions hook into:

test lifecycle callbacks

Examples:

* before test execution
* after test execution
* exception handling
* parameter injection
* dependency resolution

JUnit internally invokes extension callbacks during test execution phases.

⸻

6. Common JUnit 5 Extensions

Extension	Purpose
SpringExtension	Spring TestContext support
MockitoExtension	Mockito mock initialization
TempDir	Temporary directory management
TestInfo	Metadata injection
TestReporter	Reporting support

⸻

7. SpringExtension

Example:

@ExtendWith(SpringExtension.class)

Responsibilities:

* load Spring context
* dependency injection
* transaction management
* bean lifecycle handling
* integration test support

Important:

@SpringBootTest

already internally uses:

SpringExtension

⸻

8. MockitoExtension

Example:

@ExtendWith(MockitoExtension.class)

Responsibilities:

* initialize mocks
* inject mocks
* manage Mockito lifecycle

Example:

@Mock
private AccountRepository repository;
@InjectMocks
private AccountService service;

Without MockitoExtension, these annotations will not initialize automatically.

⸻

9. Multiple Extensions in JUnit 5

One of the biggest advantages.

Example:

@ExtendWith({
SpringExtension.class,
MockitoExtension.class
})

This allows:

* Spring support
* Mockito support
* custom extensions

to work together.

This was difficult in JUnit 4.

⸻

10. Lifecycle Callbacks in Extensions

Extensions implement callback interfaces.

Examples:

Interface	Purpose
BeforeEachCallback	before each test
AfterEachCallback	after each test
BeforeAllCallback	before all tests
ParameterResolver	parameter injection
TestExecutionExceptionHandler	exception handling

⸻

11. Custom Extensions

JUnit 5 allows creating custom extensions.

Use cases:

* logging
* retry logic
* timing measurement
* security setup
* tenant context injection

Example:

public class TimingExtension
implements BeforeTestExecutionCallback,
AfterTestExecutionCallback {
}

⸻

12. Parameter Injection

JUnit 5 supports parameter injection through extensions.

Example:

@Test
void test(TestInfo info) {
System.out.println(info.getDisplayName());
}

This is powered by:

extension model

⸻

13. Interview Questions on @ExtendWith

⸻

Q1. Why was @ExtendWith introduced?

Answer

JUnit 5 introduced @ExtendWith to replace the restrictive single-runner architecture of JUnit 4 with a modular extension model supporting multiple extensions and richer lifecycle integration.

⸻

Q2. Can multiple extensions work together?

Answer

Yes. This is one of the biggest advantages of JUnit 5.

Example:

@ExtendWith({
SpringExtension.class,
MockitoExtension.class
})

⸻

Q3. Does Spring Boot use extensions internally?

Answer

Yes.

@SpringBootTest internally uses:

SpringExtension

⸻

14. What is Testcontainers

Testcontainers is a Java library that:

starts real Docker containers during tests

Examples:

* PostgreSQL
* Redis
* Kafka
* MongoDB

Integration tests interact with:

real infrastructure

instead of mocks or in-memory substitutes.

⸻

15. Why Testcontainers Were Introduced

Before Testcontainers:

* developers manually installed DBs
* shared environments caused conflicts
* CI/CD setup was inconsistent
* “works on my machine” problems common

Testcontainers solved this by:

* automating infrastructure startup
* isolating test environments
* improving reproducibility

⸻

16. Problems With Traditional Integration Testing

Traditional approaches:

* shared local DB
* manually started Kafka
* manually configured Redis

Problems:

* flaky tests
* environment inconsistencies
* port conflicts
* configuration drift

⸻

17. H2 vs Real Database Testing

Many teams used:

H2 in-memory DB

Problem:
H2 behaves differently from PostgreSQL/MySQL.

Differences:

* SQL dialect
* transaction behavior
* indexing
* locking
* JSON support

This caused production bugs escaping tests.

Testcontainers solve this by using:

real production-like DBs

⸻

18. How Testcontainers Work Internally

Flow:

JUnit starts
↓
Testcontainers starts Docker container
↓
Spring connects to container
↓
Tests execute
↓
Container destroyed automatically

⸻

19. @Testcontainers

Example:

@Testcontainers
class AccountIT {
}

This tells JUnit:

* manage container lifecycle
* integrate containers with test execution

⸻

20. @Container

Example:

@Container
static PostgreSQLContainer<?> postgres

Marks:

managed container instance

JUnit automatically:

* starts container
* stops container
* monitors lifecycle

⸻

21. Static vs Non-Static Containers

Static Container

@Container
static PostgreSQLContainer<?> postgres

Behavior:

* starts once per test class
* faster execution

⸻

Non-Static Container

@Container
PostgreSQLContainer<?> postgres

Behavior:

* starts per test method
* slower
* better isolation

⸻

22. Spring Boot + Testcontainers Integration

Example:

@SpringBootTest
@Testcontainers
class AccountIT {
}

This creates:

full production-like integration test environment

⸻

23. @DynamicPropertySource

Used to inject dynamic container properties into Spring.

Example:

@DynamicPropertySource
static void properties(DynamicPropertyRegistry registry) {
registry.add(
"spring.datasource.url",
postgres::getJdbcUrl
);
}

Important because:

* container ports are dynamic
* Spring must discover runtime connection details

⸻

24. PostgreSQL Testcontainer Example

@Testcontainers
@SpringBootTest
class AccountIT {
@Container
static PostgreSQLContainer<?> postgres =
new PostgreSQLContainer<>("postgres:15")
.withDatabaseName("test-db")
.withUsername("test")
.withPassword("test");
}

⸻

25. Redis Testcontainer Example

@Container
static GenericContainer<?> redis =
new GenericContainer<>("redis:7")
.withExposedPorts(6379);

⸻

26. Kafka Testcontainer Example

@Container
static KafkaContainer kafka =
new KafkaContainer(
DockerImageName.parse("confluentinc/cp-kafka:latest")
);

⸻

27. Testcontainers Lifecycle

Before tests:
container starts
During tests:
application uses real infra
After tests:
container destroyed automatically

⸻

28. Local Development With Testcontainers

Testcontainers are commonly run locally.

Requirement:

Docker

Developers simply run:

mvn test

or

gradle test

Containers start automatically.

⸻

29. Testcontainers in CI/CD

Huge advantage:
same tests run:

* locally
* CI/CD
* build pipelines

This reduces:

environment drift

⸻

30. Benefits of Testcontainers

Benefit	Description
production-like	realistic infra
isolated	no shared state
reproducible	stable CI
automatic cleanup	no leftovers
portable	works everywhere

⸻

31. Drawbacks of Testcontainers

Drawback	Explanation
Docker required	runtime dependency
slower startup	containers boot
resource usage	CPU/memory heavy
Kafka containers heavy	large RAM usage

⸻

32. Best Practices

Use Static Containers

Improves speed.

⸻

Combine With Unit Tests

Do NOT replace all tests with Testcontainers.

Recommended:

* unit tests for business logic
* integration tests for infra validation

⸻

Avoid Shared External Infra

Use isolated containers.

⸻

Use Realistic Images

Prefer:

postgres:15

instead of H2.

⸻

33. Integration Testing Strategy

Recommended testing pyramid:

Layer	Purpose
unit tests	business logic
integration tests	infra interaction
E2E tests	full system validation

⸻

34. Real-World Use Cases

Database Persistence Testing

Validate:

* transactions
* schema mappings
* SQL queries

⸻

Redis Cache Testing

Validate:

* cache population
* TTL
* cache hit/miss

⸻

Kafka Integration Testing

Validate:

* producers
* consumers
* serialization
* async processing

⸻

35. Advanced Concepts

Reusable Containers

Example:

.withReuse(true)

Improves local execution speed.

⸻

Parallel Test Execution

Possible but requires careful resource management.

⸻

Awaitility With Async Tests

Useful for Kafka/eventual consistency testing.

⸻

36. Common Interview Questions

⸻

Q1. Why use Testcontainers instead of H2?

Answer

H2 behavior differs from production databases like PostgreSQL in transactions, locking, indexing, and SQL dialects. Testcontainers provide production-like realism.

⸻

Q2. Do Testcontainers require Docker?

Answer

Yes.

Docker runtime is required.

⸻

Q3. Can Testcontainers run locally?

Answer

Yes. They are commonly used by developers locally.

⸻

Q4. Why use static containers?

Answer

Static containers start once per test class, improving test performance.

⸻

Q5. Are Testcontainers slow?

Answer

Slower than mocks but far more realistic for infrastructure integration testing.

⸻

37. Senior-Level Talking Points

Excellent statements for interviews:

⸻

“JUnit 5 replaced the restrictive single-runner architecture with a modular extension model supporting composable lifecycle integrations.”

⸻

“Testcontainers provide production-like infrastructure validation by dynamically starting real Dockerized services during test execution.”

⸻

“One major advantage of Testcontainers is eliminating environment inconsistency between local development and CI/CD pipelines.”

⸻

“Integration testing should validate not only business logic but also infrastructure behavior, serialization, caching, messaging, and transaction management.”

⸻

38. Final Interview Answers

⸻

@ExtendWith

“JUnit 5 introduced the @ExtendWith extension model to replace the restrictive single-runner architecture of JUnit 4. Extensions provide modular lifecycle integration for frameworks such as Spring and Mockito and allow multiple extensions to participate together in the test lifecycle.”

⸻

Testcontainers

“Testcontainers allow us to execute integration tests against real Dockerized infrastructure such as PostgreSQL, Redis, and Kafka. This provides production-like testing environments locally and in CI/CD pipelines, improving reliability and eliminating environment inconsistencies.”


