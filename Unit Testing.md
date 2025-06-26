`1: Unit Testing with JUnit & Mockito`

🔹 A. JUnit 5 (Jupiter) Basics
1.	@Test, @BeforeEach, @AfterEach, @BeforeAll, @AfterAll
<br>
      These annotations are used to define test methods and lifecycle hooks in JUnit 5.
      * @Test: Marks a method as a test case. 
      * @BeforeEach: Runs before each test method. 
      * @AfterEach: Runs after each test method. 
      * @BeforeAll: Runs once before all test methods in the class. 
      * @AfterAll: Runs once after all test methods in the class. 

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

2.	Assertions: assertEquals, assertTrue, assertThrows, etc.
    <br> Assertions are used to validate expected outcomes in tests. 
    <br> Examples: 
      *  assertEquals: Checks if two values are equal.
      *  assertTrue: Checks if a condition is true. 
      * assertThrows: Verifies that a specific exception is thrown. 
```java
assertTrue(list.isEmpty());
assertThrows(IllegalArgumentException.class, () -> methodThatThrows());
 ```

3.	Parameterized Tests: @ParameterizedTest
      <br> Allows running the same test with different parameters.
```java
@ParameterizedTest
@ValueSource(ints = {1, 2, 3})
void testWithParameters(int number) {
    assertTrue(number > 0);
}
```

4.	Nesting tests: @Nested
      *  The @Nested annotation in JUnit 5 is primarily for grouping related test cases within a parent test class, which improves readability and organization. It allows you to structure tests hierarchically, making it easier to manage complex test scenarios.
      *  Additionally, each nested class can have its own setup and teardown methods, providing more granular control over test initialization for specific groups of tests.
      *   It does not affect test execution order or test isolation beyond this organizational benefit.


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

5.	Disabling: @Disabled
    <br> Temporarily disables a test or test class.

➡️ Hands-on: Write test classes for a simple Calculator or BankingService
<!-- Practical exercise to apply the above concepts. -->

⸻

🔹 B. Mockito – Mocking Dependencies
1.	@Mock, @Spy, @InjectMocks
      *  @Mock: Creates a mock object.
      <br> Creates a mock instance of a class or interface. This mock object simulates the behavior of real objects, allowing you to define return values and verify interactions without relying on actual implementations.
      ```java
      @Mock
      private PaymentGateway gateway;
      ``` 
      * @Spy: Creates a partial mock, allowing real method calls. 
        <br> Creates a partial mock of a real object. Unlike @Mock, a spy calls real methods unless they are stubbed. This is useful when you want to mock only specific methods but keep the rest of the behavior unchanged.
        ```java
        @Spy
        private List<String> realList = new ArrayList<>();
        ```
        
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
        
      * When you use @Mock List<Integer> mockList; and call mockList.add(2);, nothing is actually added to the list. By default, all methods on a mock return default values (e.g., false for add, null for get, etc.) and have no side effects.
      * So, mockList.add(2) will return false (the default for boolean methods), and the mock list remains empty. No real logic is executed unless you explicitly stub the method.
        ```

      * @InjectMocks
        <br> Automatically injects mocks into the class under test. 
        <br> This annotation is used to create an instance of the class under test and inject all mocks annotated with @Mock or @Spy into it. It simplifies the setup of tests by automatically wiring dependencies.
        
        Without @InjectMocks:
        ```java
        PaymentGateway gateway = Mockito.mock(PaymentGateway.class);
        PaymentService service = new PaymentService();
        service.setGateway(gateway); // manual injection
        ```
        
        With @InjectMocks:
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
        <br> MockitoAnnotations.openMocks(this); 
        <br> MockitoAnnotations.openMocks(this); initializes the fields annotated with @Mock, @Spy, and @InjectMocks in the current test class. It sets up the mocks before each test, so you don't have to manually call Mockito.mock() for each dependency.

        ```java
            @BeforeEach
            void setUp() {
                MockitoAnnotations.openMocks(this);
            }

        ```
2.	when(...).thenReturn(...), verify(...)
   *  when(...).thenReturn(...): Defines mock behavior.
*  verify(...): Verifies interactions with the mock.

    ```java
      when(gateway.charge(anyDouble())).thenReturn(true);
      verify(gateway).charge(100.0); : 
      ```

3.	Mocking exceptions: thenThrow(...)
      <br> Simulates exceptions being thrown by a mock.
      ```java
        when(gateway.charge(anyDouble())).thenThrow(new RuntimeException("Payment failed"));
        ```

4.	Argument matchers: any(), eq(), argThat()
<br> Matchers are used to define flexible argument conditions for mock methods.
   *  any(): Matches any argument of the specified type.
   *  eq(): Matches a specific value.
   *  argThat(): Custom matcher for complex conditions.
   ```java
      when(gateway.charge(anyDouble())).thenReturn(true);
      verify(gateway).charge(eq(100.0));
      verify(gateway).charge(argThat(amount -> amount > 0));
   ```


➡️ Hands-on: Write tests for services depending on DAOs or APIs (e.g., PaymentService → PaymentGateway)
<!-- Practical exercise to mock dependencies and test service logic. -->

⸻

🔹 C. Advanced Unit Testing
1.	Mockito annotations vs manual mocks
   *  Mockito annotations (@Mock, @Spy, @InjectMocks) simplify mock creation and injection. 
   *  Manual mocks require explicit instantiation and setup. 
   *  Using annotations reduces boilerplate code and improves readability. 

2.	Mocking static methods (with mockito-inline)
   *  Java 8+ - Allows mocking static methods using the mockito-inline library.
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
                    // Now Utils.greet("Nikhil") returns "Hi, Nikhil"
                    assertEquals("Hi, User", Utils.greet("User"));
                }
                // Outside the block, Utils.greet behaves normally
            }
        }
      ```
   * PowerMockito is an extension of Mockito that allows you to mock static methods, constructors, final classes, private methods, and more capabilities not available in standard Mockito (before mockito-inline). 
   * It is useful for testing legacy code that is hard to refactor.
    
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

3. Reflection in testing
*  Reflection in testing refers to using Java’s reflection API to access, modify, or invoke private fields, methods, or constructors of classes under test. This is sometimes needed for legacy code or when you cannot change the code to make it more testable.
*  Reflection can be used to access private methods, fileds, constructors, but it is generally discouraged in unit tests. 
*  Instead, focus on testing public methods that use the private methods internally.
*  If you find yourself needing to test private methods, consider refactoring your code to make it more testable.
*  Accessing or setting private fields for setup or assertions.
*  Invoking private methods to test their logic directly. 
*  Creating instances via private constructors.
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
4. Writing unit tests for edge cases and failure scenarios
   <br>   Focus on testing boundary conditions and error handling. 

5.	Coverage analysis (Jacoco plugin)
      <br> Use Jacoco to measure test coverage and identify untested code.

⸻

📍 PHASE 4: Service Virtualization (WireMock)

🔥 Purpose:


Service virtualization is the practice of simulating external systems (like APIs, databases, or third-party services) so you can test your application in isolation, even if those systems are unavailable or incomplete. WireMock is a popular Java tool for this purpose.

<hr>
What is WireMock?
<br> WireMock is a flexible library for stubbing and mocking web services. It allows you to:

1. Simulate HTTP APIs (REST, SOAP, etc.)
2. Define expected requests and stub responses
3. Simulate delays, errors, and dynamic responses
4. Verify that your application made the expected HTTP calls
<hr>
Why use WireMock?

1. Isolate tests: Test your code without relying on real external services.
2. Control scenarios: Simulate edge cases, errors, or slow responses.
3. Repeatability: Ensure tests are deterministic and not affected by external changes.
<hr>
How does WireMock work?

* WireMock runs as a local HTTP server. 
* You configure it to expect certain requests and return predefined responses. 
* Your application is pointed to WireMock instead of the real service during tests.
<hr>

Basic Usage Example
1. Add WireMock dependency (Maven)
    ```java
    <dependency>
        <groupId>com.github.tomakehurst</groupId>
        <artifactId>wiremock-jre8</artifactId>
        <version>2.31.0</version>
        <scope>test</scope>
    ```

2. Start WireMock in your test
```java
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

WireMockServer wireMockServer = new WireMockServer(8080);
wireMockServer.start();
 ```
3. Stub an endpoint
```java
stubFor(get(urlEqualTo("/api/user/1"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"id\":1,\"name\":\"Nikhil\"}")));
```
4. Test your code against WireMock
   * Point your application’s API client to http://localhost:8080/api/user/1. It will receive the stubbed response.
5. Verify requests
```java
verify(getRequestedFor(urlEqualTo("/api/user/1")));
 ```
6. Stop WireMock after tests
```java
wireMockServer.stop();
 ```
<hr></hr>
Advanced Features
Simulate delays:
.withFixedDelay(2000) to test timeouts.
Simulate errors:
.withStatus(500) for server errors.
Dynamic responses:
Use response templating for dynamic data.
<hr></hr>
Typical Use Cases
Testing code that calls payment gateways, third-party APIs, or microservices.
Simulating failures or slow responses.
Running integration tests in CI/CD pipelines without real dependencies.
<hr></hr>

. Do you want to use WireMock as a standalone server or embedded in your tests?


Standalone server:
WireMock can run as a separate process (via JAR or Docker). You configure stubs via REST API or files. This is useful for manual testing, contract testing, or when multiple teams share the same mock server.
Embedded in tests:
Most common for automated tests. You start and stop a WireMockServer instance in your test code (JUnit, etc.), configure stubs programmatically, and run tests against it. This keeps tests isolated and repeatable.
<hr></hr> 2. Are you testing REST APIs, SOAP, or something else?


REST APIs:
WireMock natively supports stubbing and verifying HTTP(S) REST endpoints (GET, POST, PUT, DELETE, etc.).
SOAP:
WireMock can stub SOAP endpoints as well, since SOAP is also HTTP-based. You match on URL and request body (XML), and return XML responses.
Other protocols:
WireMock is limited to HTTP/HTTPS. For non-HTTP protocols, use other tools.
<hr></hr> 3. Do you need to simulate authentication, headers, or query parameters?


Yes, WireMock supports all of these:
Authentication:
You can stub endpoints that require specific headers (e.g., Authorization).
Example:
stubFor(get(urlEqualTo("/api/secure"))
.withHeader("Authorization", equalTo("Bearer token123"))
.willReturn(aResponse().withStatus(200)));
Headers:
Match or assert on any HTTP header.
Query parameters:
Use urlPathEqualTo and withQueryParam to match specific query parameters.
stubFor(get(urlPathEqualTo("/api/user"))
.withQueryParam("id", equalTo("1"))
.willReturn(aResponse().withStatus(200)));
<hr></hr> 4. Should I show a full JUnit test example with WireMock?

Yes, here is a minimal JUnit 5 example:

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

<hr></hr> 5. Are you interested in using WireMock with Spring Boot’s @AutoConfigureWireMock?
@AutoConfigureWireMock is a Spring Boot test annotation that automatically starts WireMock on a random port and injects it into your test context.
It is useful for integration tests with @SpringBootTest or @WebMvcTest.
Example usage:

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

⸻

📍 PHASE 2: Integration & Functional Testing (Spring Boot)

🔹 A. Spring Boot Integration Testing
1. @SpringBootTest vs @WebMvcTest
* @SpringBootTest
  <br> Loads the full application context, including all beans, configurations, and external dependencies. Used for end-to-end or integration tests.


```java
@SpringBootTest
class MyServiceIntegrationTest {
@Test
void contextLoads() {
// Test with full context
}
}
```
* @WebMvcTest
  <br> Loads only the web layer (controllers, filters, etc.), not services or repositories. Used for controller tests.


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
<hr></hr>
2. Use of @MockBean in Integration Tests
<br> Purpose: Replace a bean in the application context with a Mockito mock.
<br> Example:

```java
@WebMvcTest(MyController.class)
class MyControllerTest {
    @MockBean
    private MyService myService; // Replaces real MyService with a mock

    @Autowired
    private MockMvc mockMvc;
}
```
<br> Diff between Mockito and MockBean:

* The main difference between @MockBean (Spring Boot) and @Mock (plain Mockito) is how they integrate with the Spring context:

* @Mock (Mockito):
<br> Creates a mock object for unit tests, but does not register it in the Spring context. You must inject it manually if needed.

* @MockBean (Spring Boot):
<br> Creates a Mockito mock and replaces the real bean in the Spring context, so Spring injects the mock wherever that bean is used.

Example:

Suppose you have a UserService that depends on UserRepository.

Using Mockito @Mock (no Spring context):
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
Using Spring Boot @MockBean (with Spring context):
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

* Use @Mock for plain unit tests (no Spring context).
* Use @MockBean for Spring tests to mock and inject dependencies into the Spring context automatically.
* You should not use @MockBean for plain unit tests because:
  * @MockBean is designed for Spring tests; it replaces beans in the Spring application context. 
  * It requires the Spring Test framework to run. 
  * Plain unit tests do not load the Spring context, so @MockBean will not work and may cause errors. 
  * For plain unit tests, use Mockito’s @Mock annotation, which is lightweight and does not depend on Spring.

<hr></hr>
3. Testing Controllers Using MockMvc
<br> MockMvc simulates HTTP requests to controller endpoints without starting a real server.
<br> Example:

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
<hr></hr>
4. TestRestTemplate or WebTestClient (for REST APIs)
<br> TestRestTemplate: For integration tests with a running server (non-reactive).


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
WebTestClient: For reactive applications or WebFlux.

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
<hr></hr>
5. Use @DataJpaTest to Test Repository Layer
<br> Loads only JPA components (repositories, entities, DataSource).
<br> Uses in-memory DB by default.
<br> Example:

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
<hr></hr>

➡️ Hands-on:
•	Write integration tests for a REST API (/api/accounts)
<!-- Practical exercise to test REST API endpoints. -->
•	Use H2 DB for testing repositories
<!-- Use an in-memory H2 database for repository tests. -->

⸻

🔹 B. Functional Testing Concepts
1. End-to-end behavior testing of features
   Test the complete workflow as a user would interact with the system.
   Example:
   Test user registration, login, and fund transfer in a banking app.

2. Simulate real HTTP requests
   Send actual HTTP requests to your application, just like a real client (browser or API consumer) would.
   Example:
   Use MockMvc, TestRestTemplate, or WebTestClient in Spring Boot tests.

3. Validate responses, status codes, and headers
   Check that the API returns the correct data, HTTP status codes, and headers.
   Example:
   Assert that a POST to /api/register returns 201 Created and the expected JSON body.

4. Use TestContainers for real DBs (optional)
   Run a real database (like PostgreSQL or MySQL) in a Docker container during tests for realistic integration.
   Example:
   Use the TestContainers library to spin up a database container before tests and tear it down after.

* Functional testing and integration testing differ mainly in scope and intent:

  * Integration Testing checks if different components or layers of your application (e.g., service + repository, controller + service) work together as expected. It usually uses in-memory databases or mocks for external systems, and focuses on internal integration, not the full user flow.

  * Functional Testing (end-to-end testing) validates complete business features as a user would experience them. It simulates real HTTP requests, often uses real databases (via TestContainers), and covers the entire workflow (e.g., register → login → transfer funds), including all layers and external integrations.



➡️ Hands-on:
•	Test user flows like “Register → Login → Transfer Funds → Get Balance”
<!-- Practical exercise to test complete user workflows. -->

⸻

📍 PHASE 3: Mutation Testing

🔹 A. Using PIT (Pitest)
1.	Add PIT plugin to pom.xml or build.gradle
      <!-- Adds the PIT mutation testing plugin to the build configuration. -->

2.	Run mutation tests via CLI or IDE
      <!-- Executes mutation tests to evaluate test quality. -->

3.	Interpret mutation score (survived mutants vs killed)
      <!-- Analyzes the mutation score to identify weak test cases. -->

4.	Refactor test cases to improve coverage
      <!-- Enhances test cases to kill more mutants and improve quality. -->

➡️ Hands-on:
•	Run PIT on an existing project
<!-- Practical exercise to apply mutation testing. -->
•	Improve test quality to kill more mutants
<!-- Refactor tests to achieve a higher mutation score. -->
