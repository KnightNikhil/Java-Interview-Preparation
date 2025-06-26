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

📍 PHASE 2: Integration & Functional Testing (Spring Boot)

🔹 A. Spring Boot Integration Testing
1.	@SpringBootTest vs @WebMvcTest
      <!-- @SpringBootTest: Loads the full application context for testing. -->
      <!-- @WebMvcTest: Loads only the web layer for testing controllers. -->

2.	Use of @MockBean in integration tests
      <!-- Replaces a bean in the application context with a mock. -->

3.	Testing controllers using MockMvc
      <!-- MockMvc is used to test Spring MVC controllers. -->

4.	TestRestTemplate or WebTestClient (for REST APIs)
      <!-- Tools for testing REST APIs in Spring Boot. -->
      <!-- TestRestTemplate: Simplified REST client for integration tests. -->
      <!-- WebTestClient: Reactive client for testing web applications. -->

5.	Use @DataJpaTest to test repository layer
      <!-- Loads only the JPA layer for testing database interactions. -->

➡️ Hands-on:
•	Write integration tests for a REST API (/api/accounts)
<!-- Practical exercise to test REST API endpoints. -->
•	Use H2 DB for testing repositories
<!-- Use an in-memory H2 database for repository tests. -->

⸻

🔹 B. Functional Testing Concepts
1.	End-to-end behavior testing of features
      <!-- Tests the complete flow of a feature from start to finish. -->

2.	Simulate real HTTP requests
      <!-- Sends actual HTTP requests to test application behavior. -->

3.	Validate responses, status codes, and headers
      <!-- Ensures the API returns correct responses, status codes, and headers. -->

4.	Use TestContainers for real DBs (optional)
      <!-- TestContainers allows using real databases in Docker containers for testing. -->

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

📍 PHASE 4: Service Virtualization (WireMock)

🔥 Purpose:

Simulate external services (APIs, third-party systems) for isolated testing.
<!-- WireMock is used to mock external services for testing. -->
*	stubFor(get(urlEqualTo("/path")).willReturn(aResponse().withBody(...)))
     <!-- Defines a stubbed response for a specific HTTP GET request. -->
*	verify(getRequestedFor(urlEqualTo("/path")))
     <!-- Verifies that a specific HTTP GET request was made. -->
*	withStatus(200), withBody("response")
     <!-- Configures the status code and body of the stubbed response. -->
*	Can simulate delays, errors, dynamic responses
     <!-- WireMock can simulate various scenarios like delays, errors, and dynamic responses. -->