`ROADMAP`

📍 PHASE 1: Unit Testing with JUnit & Mockito

🔹 A. JUnit 5 (Jupiter) Basics
1.	@Test, @BeforeEach, @AfterEach
2.	Assertions: assertEquals, assertTrue, assertThrows, etc.
3.	Parameterized Tests: @ParameterizedTest
4.	Nesting tests: @Nested
5.	Disabling: @Disabled

➡️ Hands-on: Write test classes for a simple Calculator or BankingService

⸻

🔹 B. Mockito – Mocking Dependencies
1.	@Mock, @InjectMocks, @Spy
2.	when(...).thenReturn(...), verify(...)
3.	Mocking exceptions: thenThrow(...)
4.	Argument matchers: any(), eq(), argThat()

➡️ Hands-on: Write tests for services depending on DAOs or APIs (e.g., PaymentService → PaymentGateway)

⸻

🔹 C. Advanced Unit Testing
1.	Mockito annotations vs manual mocks
2.	Mocking static methods (with mockito-inline)
3.	Writing unit tests for edge cases and failure scenarios
4.	Coverage analysis (Jacoco plugin)

⸻

📍 PHASE 2: Integration & Functional Testing (Spring Boot)

🔹 A. Spring Boot Integration Testing
1.	@SpringBootTest vs @WebMvcTest
2.	Use of @MockBean in integration tests
3.	Testing controllers using MockMvc
4.	TestRestTemplate or WebTestClient (for REST APIs)
5.	Use @DataJpaTest to test repository layer

➡️ Hands-on:
•	Write integration tests for a REST API (/api/accounts)
•	Use H2 DB for testing repositories

⸻

🔹 B. Functional Testing Concepts
1.	End-to-end behavior testing of features
2.	Simulate real HTTP requests
3.	Validate responses, status codes, and headers
4.	Use TestContainers for real DBs (optional)

➡️ Hands-on:
•	Test user flows like “Register → Login → Transfer Funds → Get Balance”

⸻

📍 PHASE 3: Mutation Testing

🔹 A. Using PIT (Pitest)
1.	Add PIT plugin to pom.xml or build.gradle
2.	Run mutation tests via CLI or IDE
3.	Interpret mutation score (survived mutants vs killed)
4.	Refactor test cases to improve coverage

➡️ Hands-on:
•	Run PIT on an existing project
•	Improve test quality to kill more mutants