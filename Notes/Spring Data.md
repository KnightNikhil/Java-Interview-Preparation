✅ 1. Spring Data Overview
 

* What is Spring Data?
    Spring Data is a part of the Spring ecosystem that simplifies data access in Java applications. It provides a consistent and easy way to interact with different types of data stores, such as relational databases (JPA/Hibernate, JDBC), NoSQL databases (MongoDB, Cassandra, Redis, Neo4j), and even newer data technologies like Elasticsearch.
    The main goal of Spring Data is to reduce boilerplate code required for data access. For example, instead of writing long DAOs and queries manually, Spring Data allows developers to define repository interfaces, and it automatically generates implementations at runtime.
    Key Features of Spring Data
      1.	Repository Abstraction – Provides CrudRepository, JpaRepository, and other repository interfaces that give CRUD and pagination methods out of the box.
      2.	Derived Query Methods – Queries can be created simply by method naming conventions (e.g., findByName, findByEmailAndStatus).
      3.	Support for Custom Queries – Allows writing queries using JPQL, native SQL, or @Query annotation.
      4.	Paging and Sorting – Built-in support for pagination (Pageable) and sorting (Sort).
      5.	Cross-Store Support – Works with both relational and NoSQL databases seamlessly.
      6.	Integration with Spring Boot – Works with starter dependencies (e.g., spring-boot-starter-data-jpa, spring-boot-starter-data-mongodb) for quick setup.

    Advantages of Spring Data
    •	Less Boilerplate Code → Developers only define interfaces, no need to write DAO implementations.
    •	Consistency → Same programming model across different data stores.
    •	Productivity Boost → With repositories, queries, and paging features readily available.
    •	Flexibility → Supports custom queries and advanced data access scenarios.

⸻


* Role in layered architecture (Repository layer abstraction)
  Spring applications usually follow a layered architecture:
    1.	Controller Layer → Handles HTTP requests/responses (REST APIs).
    2.	Service Layer → Contains business logic and orchestrates between controller and repository.
    3.	Repository Layer (Persistence Layer) → Handles database interactions.

⸻

Where Spring Data Fits In
•	The Repository Layer is abstracted by Spring Data.
•	Instead of writing DAO classes manually with JDBC, Hibernate, or boilerplate CRUD methods, we only define Repository Interfaces, and Spring Data automatically provides implementations at runtime.

⸻

How It Works
1.	Repository Abstraction
•	You define interfaces like JpaRepository, CrudRepository, or MongoRepository.
•	Spring Data generates the implementation dynamically at runtime using proxy classes.
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
}
```
👉 No need to write SELECT * FROM users WHERE last_name = ?. Spring Data derives it.

⸻
2.	Benefits in Layered Architecture

•	Loose Coupling: Service layer depends on an interface, not a concrete DAO implementation.
•	Consistent API: Same save(), findAll(), delete() methods across different databases (JPA, MongoDB, Cassandra, etc.).
•	Query Abstraction: Query methods (findBy…), JPQL, native queries, or Criteria API supported.
•	Custom Methods: Still possible to extend with your own repository methods when needed.
•	Separation of Concerns: Business logic stays in the Service layer; persistence logic stays abstracted in Repository layer.

⸻
3.	Example in Layered Architecture

•	Controller Layer
```java
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
```

•	Service Layer
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                             .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
```

•	Repository Layer
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
}
```
👉 Here, the Repository abstraction removes the need to manually implement database logic, letting you focus on service/business logic.

How does Spring Data create repository implementations at runtime?
•	Spring Data uses dynamic proxies and the RepositoryFactoryBean mechanism.
•	When the application context starts:
1.	Spring scans for repository interfaces (UserRepository).
2.	It creates a proxy class implementing that interface.
3.	The proxy delegates calls like findById() to the appropriate JpaRepository / MongoRepository / etc. methods.
•	Query methods (findByLastName) are parsed using the Query Method Parser → generates JPQL/SQL automatically.

👉 In short: You just define the interface; Spring Data provides the runtime implementation via proxy.

What’s the difference between DAO and Repository patterns?
•	DAO (Data Access Object)
•	Low-level pattern.
•	Encapsulates persistence logic (JDBC, Hibernate code).
•	Often tightly coupled with database technology.
•	Example: UserDAO with methods saveUser(), getUserById().
•	Repository
•	Higher-level abstraction.
•	Operates on domain objects (entities), not database-specific constructs.
•	Offers collection-like APIs (findAll, save, delete).
•	Independent of persistence technology (can switch from JPA to MongoDB).

👉 Spring Data implements the Repository pattern, not just DAO.

Does Spring Data eliminate the DAO layer completely? Why/why not?
•	Not completely:
•	Spring Data reduces the need to write DAOs for common CRUD and query operations.
•	But for complex queries, batch operations, stored procedures, or performance optimizations, you might still need custom DAO/repository code.
•	Essentially, Spring Data abstracts away 80% of boilerplate DAO code, but allows custom DAO-style extensions when needed.

👉 So, it doesn’t eliminate, but rather enhances and simplifies DAO responsibilities.

---


✅ 2. Spring Data JPA Basics
* What is JPA?
  JPA (Java Persistence API) is a specification in Java EE (now Jakarta EE) that defines a standard way to map Java objects (POJOs) to relational database tables and manage data persistence.

👉 It’s not an implementation; it’s just a set of interfaces and rules.
👉 To use JPA, you need a JPA provider (implementation) such as:
•	Hibernate (most popular)
•	EclipseLink
•	OpenJPA

Core Purpose
•	Simplify object-relational mapping (ORM).
•	Eliminate boilerplate JDBC code (managing connections, statements, result sets).
•	Provide a uniform API so you can switch providers (e.g., Hibernate ↔ EclipseLink) with minimal code changes.

Key Features
1.	ORM Mapping
•	Map Java classes → DB tables
•	Map fields → columns using annotations (@Entity, @Table, @Column, @Id, etc.)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
}
```

2.	EntityManager API
•	Central interface for CRUD operations.
```java
@PersistenceContext
private EntityManager em;

public void saveUser(User user) {
    em.persist(user);   // insert
}

public User getUser(Long id) {
    return em.find(User.class, id);  // select
}
```

3.	Querying
•	JPQL (Java Persistence Query Language): object-oriented queries using entity names and fields.

```java
List<User> users = em.createQuery(
    "SELECT u FROM User u WHERE u.email = :email", User.class)
    .setParameter("email", "abc@xyz.com")
    .getResultList();
```
•	Named Queries (@NamedQuery) and Criteria API for dynamic queries.

	4.	Transaction Management
	•	Integrates with JTA or Spring’s transaction manager.
	5.	Caching
	•	First-level (per EntityManager) and optional second-level (provider-specific).

⸻

Why JPA instead of JDBC?
•	JDBC: lots of boilerplate (connections, statements, result sets).
•	JPA: abstracts database interactions, works with objects, and generates SQL under the hood.
•	Benefit: Productivity + maintainability + portability.

⸻

Architecture
1.	JPA (specification) → defines interfaces like EntityManager, EntityTransaction.
2.	JPA Provider (implementation) → Hibernate, EclipseLink, etc.
3.	Database → actual SQL queries executed.

Flow: EntityManager → Provider (Hibernate) → JDBC → Database.

* What’s the role of EntityManager in JPA?
  EntityManager is the primary JPA interface that manages the persistence context and CRUD/query operations for entity instances.

Details
•	Responsibility
    •	Create, read, update, delete entity instances (persist, find, merge, remove).
    •	Manage lifecycle states (transient, managed, detached, removed).
    •	Provide JPQL and Criteria API (createQuery, createNamedQuery, createNativeQuery).
    •	flush() (synchronize persistence context to DB) and clear() (detach all managed entities).
•	Persistence Context
    •	EntityManager holds a first-level cache (persistence context): managed entities are identity-guaranteed within it.
•	Transaction scope
    •	For resource-local, EntityTransaction is used; in container/Spring, EntityManager participates in the current transaction.
•	Obtaining it
    •	In Java EE: @PersistenceContext injection.
    •	In Spring: injected and typically proxied to a container-scoped EntityManager.

Example
```java
@PersistenceContext
private EntityManager em;

public User getUser(Long id) {
    return em.find(User.class, id); // returns managed entity within transaction
}
```

* Difference between persist(), merge(), remove(), and detach()?

These are EntityManager lifecycle operations affecting entity states: persist makes a transient entity managed and scheduled for insert; merge copies detached state into managed entity and returns managed instance; remove schedules entity for deletion; detach removes entity from persistence context (becomes detached).

Details
•	persist(entity)
    •	Takes a transient instance and makes it managed; will INSERT at flush/commit.
    •	Does not return the managed instance (same reference becomes managed).
•	merge(entity)
    •	Takes a detached or transient instance, copies its state into a managed instance and returns that managed instance.
    •	The passed instance remains detached. Use the returned instance to continue.
    •	If no managed instance exists, provider will fetch or create one.
•	remove(entity)
    •	Marks a managed entity for removal → DELETE at flush/commit. Passing a detached instance throws IllegalArgumentException (must merge first or find the entity).
•	detach(entity)
    •	Removes the entity from persistence context → changes are not tracked/saved.
•	Example 
```java
em.getTransaction().begin();
User u = new User("a@x.com");        // transient
em.persist(u);                       // becomes managed
em.getTransaction().commit();        // INSERT

em.detach(u);                        // now detached
u.setName("new");                    // not tracked

User merged = em.merge(u);           // merged returns managed instance
em.remove(merged);                   // mark for delete
```



* JPA vs Hibernate
  JPA is a specification (a set of interfaces and rules); Hibernate is a popular implementation/provider of that specification (with many extra features).

Details
•	JPA = standard/spec
    •	Defines APIs like EntityManager, EntityTransaction, @Entity, JPQL, lifecycle rules, mapping annotations.
    •	You code against these interfaces to be provider-agnostic.
•	Hibernate = implementation (provider)
    •	Implements JPA APIs and also provides proprietary features (HQL, extended Criteria, second-level caching options, @NaturalId, multi-tenancy, etc.).
    •	Historically Hibernate existed before JPA; JPA adopted many patterns from Hibernate.
•	Practical differences
    •	If you use only JPA APIs your code is portable across providers (Hibernate, EclipseLink, OpenJPA).
    •	Using provider extensions ties you to that provider (e.g., Session APIs, @Filter).
•	When to use which
    •	Prefer JPA standard for portability; use Hibernate features when you need specific functionality or performance optimizations.


* Entity annotations: @Entity, @Table, @Id, @GeneratedValue, etc.


* One-to-One, One-to-Many, Many-to-One, Many-to-Many relationships
* Lazy vs Eager loading

⸻

✅ 3. Repositories

* What are the advantages/disadvantages of JPA over plain JDBC?

JPA provides ORM, productivity, caching and portability, while JDBC gives fine-grained control and potentially simpler SQL performance tuning.

Advantages (JPA)
•	Less boilerplate: No manual ResultSet mapping and resource handling.
•	Object mapping: Work with entities and relationships, not rows.
•	Vendor portability: Higher-level API (JPQL) is portable.
•	Caching & identity: L1 (and optional L2) caches, transactional identity.
•	Unit of Work: Persistence context batches DB changes and flushes efficiently.
•	Query abstraction: JPQL, Criteria API, named queries.

Disadvantages / trade-offs
•	Hidden SQL: Generated SQL may be suboptimal for certain queries; need to inspect show_sql and optimize.
•	Complexity/learning curve: Understanding states, proxies, lazy loading, and N+1 issues.
•	Performance pitfalls: Large-batch operations need careful handling (bulk updates, detach/clear).
•	Less control: For specialized DB features you may still need native SQL/JDBC.
•	Debugging ORM issues can be harder than raw SQL mistakes.

When to prefer JDBC
•	Very simple, ultra-high performance batch ETL where you control every statement.
•	When you need the absolute lowest latency or special DB APIs not exposed via JPA.

* CrudRepository
  What is CrudRepository?
  •	CrudRepository<T, ID> is a Spring Data interface that provides generic CRUD operations for an entity.
  •	It’s the base interface for repositories in Spring Data (superinterface of PagingAndSortingRepository and JpaRepository).
  •	Part of Spring Data Commons (not tied only to JPA).
  
  Key Methods
    CrudRepository provides basic CRUD methods without requiring implementation:
```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S entity);          // Insert or update
    Optional<T> findById(ID id);             // Find by ID
    boolean existsById(ID id);               // Check existence
    Iterable<T> findAll();                   // Find all
    Iterable<T> findAllById(Iterable<ID> ids);
    long count();                            // Count entities
    void deleteById(ID id);                  // Delete by ID
    void delete(T entity);                   // Delete entity
    void deleteAll(Iterable<? extends T> entities);
    void deleteAll();                        // Delete all entities
}
```
Return Types: Optional<T>, Iterable<T>, primitive types (like long).

Example Usage

Entity
```java
@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
```

Repository
```java
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    // Custom query methods can be added here
}
```

Service
```java
@Autowired
private CustomerRepository customerRepo;

public void demoCrud() {
    // Create
    Customer c = new Customer();
    c.setName("Nikhil");
    customerRepo.save(c);

    // Read
    Optional<Customer> customer = customerRepo.findById(1L);

    // Update
    c.setName("Updated Nikhil");
    customerRepo.save(c);

    // Delete
    customerRepo.deleteById(1L);
}
```

Relationship to Other Repositories
•	CrudRepository → Basic CRUD.
•	PagingAndSortingRepository → Adds pagination + sorting (findAll(Pageable)).
•	JpaRepository → Adds JPA-specific features (flush(), batch operations, @Query support).

👉 In most Spring Boot apps, developers directly extend JpaRepository since it inherits all features of CrudRepository and PagingAndSortingRepository.

1.	What’s the difference between CrudRepository and JpaRepository?
→ CrudRepository → basic CRUD. JpaRepository → full JPA support (flush, batch delete, pagination, etc.).
2.	Why does save() handle both insert and update?
→ Hibernate detects if entity has an ID:
•	No ID → INSERT.
•	Existing ID → UPDATE.
3.	What’s the return type of findById()? Why Optional?
→ To avoid NullPointerException. Encourages safe handling.
4.	How does deleteById() work internally?
→ Fetches entity reference by ID, then calls EntityManager.remove().
5.	Can you use CrudRepository outside of JPA (e.g., MongoDB, Cassandra)?
→ Yes, Spring Data defines it in commons; it’s extended by MongoRepository, CassandraRepository, etc.
6.	If CrudRepository already exists, why do we need JpaRepository?
→ To get advanced features: pagination, sorting, flush, batch operations, etc.

      
* JpaRepository

JpaRepository is a Spring Data JPA interface that provides CRUD operations, pagination, sorting, and JPA-specific methods.
It sits on top of CrudRepository and PagingAndSortingRepository and adds extra functionality for working with JPA entities.

```
Repository (Marker Interface)
   ↑
CrudRepository<T, ID>
   ↑
PagingAndSortingRepository<T, ID>
   ↑
JpaRepository<T, ID>

```

1.	CRUD methods (from CrudRepository)
  •	save(), findById(), findAll(), delete(), count()
2.	Pagination & Sorting (from PagingAndSortingRepository)
•	findAll(Pageable pageable)
•	findAll(Sort sort)
3.	JPA-specific methods
•	void flush() → Synchronizes persistence context with DB.
•	< S extends T> S saveAndFlush(S entity) → Saves entity & flushes immediately.
•	void deleteInBatch(Iterable<T> entities) → Bulk delete using a single query.
•	void deleteAllInBatch() → Bulk delete all rows.
•	List<T> findAllById(Iterable<ID> ids) → Fetches entities by multiple IDs efficiently.
4.	Supports Derived Query Methods
•	Example:
```java
List<Customer> findByLastName(String lastName);
List<Customer> findByAgeGreaterThan(int age);
```
5.	Supports @Query (JPQL or Native SQL)
```java
@Query("SELECT c FROM Customer c WHERE c.email = ?1")
Customer findByEmail(String email);
```

Example:
```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByName(String name);       // Derived query
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %?1%")
    List<Customer> searchByEmail(String email);  // JPQL query
}
```

Usage:
```java
@Autowired
private CustomerRepository repo;

public void testJpaRepo() {
    // Save
    repo.save(new Customer(null, "John", "john@test.com"));

    // Fetch all
    List<Customer> all = repo.findAll();

    // Pagination
    Page<Customer> page = repo.findAll(PageRequest.of(0, 5));

    // Custom query
    List<Customer> results = repo.searchByEmail("gmail.com");
}
```

Advantages of JpaRepository

✅ Rich set of methods (CRUD + pagination + batch operations).
✅ Less boilerplate (no need for EntityManager in most cases).
✅ Supports both JPQL and native SQL queries.
✅ Plays well with Spring Boot (autoconfiguration).
✅ Integrates with Spring Data ecosystem (Specifications, Projections, QueryDSL, etc.).

* QueryDslPredicateExecutor (optional)

What is it?

QueryDslPredicateExecutor<T> is a Spring Data interface that enables type-safe, dynamic queries using QueryDSL predicates, instead of JPQL or method-name queries.

```java
public interface CustomerRepository 
        extends JpaRepository<Customer, Long>, QuerydslPredicateExecutor<Customer> {
}
```
Why Use It?
•	✅ Avoids long method names like findByNameAndAgeGreaterThanAndStatus
•	✅ Provides type safety at compile-time
•	✅ Ideal for dynamic search filters
•	✅ Less boilerplate compared to JPA Criteria API

⸻

How It Works
1.	QueryDSL generates Q-classes (meta-models) at compile-time.
For entity Customer, you get QCustomer.
2.	Build Predicate objects using Q-classes.
3.	Execute queries with repository methods.

⸻

Common Methods
•	findOne(Predicate predicate)
•	findAll(Predicate predicate)
•	findAll(Predicate predicate, Sort sort)
•	findAll(Predicate predicate, Pageable pageable)
•	count(Predicate predicate)
•	exists(Predicate predicate)

⸻

Example

Entity
```java
@Entity
public class Customer {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int age;
    private String status;
}
```
Repository
```java
public interface CustomerRepository 
       extends JpaRepository<Customer, Long>, QuerydslPredicateExecutor<Customer> {
}
```
Usage
```java
@Autowired
CustomerRepository repo;

public void queryDslExample() {
    QCustomer customer = QCustomer.customer;

    Predicate predicate = customer.age.gt(25).and(customer.status.eq("ACTIVE"));

    Iterable<Customer> results = repo.findAll(predicate);

    Page<Customer> page = repo.findAll(predicate, PageRequest.of(0, 5));
}
```

⸻

Advantages
•	✅ Cleaner & safer than string-based JPQL
•	✅ Supports dynamic query building
•	✅ Works with pagination & sorting
•	✅ Integrates with Spring Data easily

⸻

Limitations
•	❌ Requires QueryDSL annotation processor setup
•	❌ Verbose compared to simple derived queries
•	❌ Higher learning curve

⸻

Follow-up Interview Questions
1.	How does QueryDslPredicateExecutor differ from @Query?
→ @Query is static, QueryDSL is dynamic & type-safe.
2.	How are Q-classes generated?
→ By annotation processor (APT) during build.
3.	Can you use Pageable & Sort with QueryDSL?
→ Yes, findAll(Predicate, Pageable) supports pagination.
4.	QueryDSL vs Criteria API?
→ Both allow dynamic queries, but QueryDSL is fluent & easier to maintain.
5.	Does QueryDSL only work with JPA?
→ No, it supports SQL, MongoDB, Cassandra, Elasticsearch, etc.,
but QueryDslPredicateExecutor is specific to Spring Data JPA.

⸻



⸻

✅ 4. Query Methods
Can we customize a Spring Data Repository with our own queries? How?
Yes, Spring Data allows us to customize repositories when we need more control than the default findAll(), save(), or derived queries.
There are four main ways to do this:

* Derived Query Methods
  •	Spring Data generates queries based on method names.
  •	You just follow naming conventions (findBy, readBy, countBy, existsBy, etc.).
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
    User findByEmailAndStatus(String email, String status);
} 
```
👉 How it works: Spring parses method names and converts them into SQL/JPQL queries at runtime.
👉 Limitations: Works only for straightforward queries; complex joins/aggregations may not fit.

* Using @Query Annotation
  •	Define JPQL or native SQL queries explicitly.
  •	Good for complex queries where derived methods don’t fit.
```java
// JPQL example
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// Native query example
@Query(value = "SELECT * FROM users WHERE status = ?1", nativeQuery = true)
List<User> findUsersByStatus(String status);
```
👉 Advantage: Precise control, supports named parameters.
👉 Use Case: When queries are too complex for derived methods.

* Named Queries
  •	Predefine queries in the entity class using @NamedQuery.
```java
@Entity
@NamedQuery(
    name = "User.findByUsername",
    query = "SELECT u FROM User u WHERE u.username = ?1"
)
public class User { ... }

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username); // Automatically uses NamedQuery
}
```
👉 Best for: Reusable queries tied to entity lifecycle.
👉 Downside: Adds clutter to entity classes.

* Custom Repository Implementations
  If queries are too advanced (e.g., dynamic criteria, stored procedures), you can extend your repository with custom logic.

```java
public interface UserRepositoryCustom {
    List<User> searchByKeyword(String keyword);
}

public class UserRepositoryImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> searchByKeyword(String keyword) {
        String jpql = "SELECT u FROM User u WHERE u.name LIKE :keyword";
        return entityManager.createQuery(jpql, User.class)
                            .setParameter("keyword", "%" + keyword + "%")
                            .getResultList();
    }
}

// Final repository combines both
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {}
```

👉 Advantage: Full flexibility with EntityManager, native queries, Criteria API.
👉 Best for: Complex dynamic queries or integration with stored procedures.

* Modifying queries with @Modifying and @Transactional
1. Why do we need @Modifying?
   •	By default, Spring Data JPA queries (@Query) are select-only.
   •	If you want to execute update, delete, or insert queries, you need to tell Spring Data JPA that this query modifies data.
   •	That’s where @Modifying comes in.

⸻

2. Why do we need @Transactional with @Modifying?
   •	Modifying queries change the database state, so they must be executed inside a transaction.
   •	Without @Transactional, the changes won’t be committed to the database.
   •	If the class or method is already annotated with @Transactional, then @Modifying queries will run within that transaction.

⸻

3. Example

Entity
```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String status;
}
```

Repository
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Update customer status
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.status = :status WHERE c.email = :email")
    int updateCustomerStatus(@Param("email") String email, @Param("status") String status);

    // Delete by status
    @Modifying
    @Transactional
    @Query("DELETE FROM Customer c WHERE c.status = :status")
    int deleteByStatus(@Param("status") String status);
}
```
Usage
```java
@Autowired
private CustomerRepository repo;

public void modifyCustomers() {
    // Update status
    int updated = repo.updateCustomerStatus("john@example.com", "ACTIVE");

    // Delete inactive customers
    int deleted = repo.deleteByStatus("INACTIVE");

    System.out.println("Updated: " + updated + ", Deleted: " + deleted);
}
```
4. Things to Remember
   •	@Modifying must be used for UPDATE or DELETE queries.
   •	@Transactional is required for committing changes.
   •	Return type can be:
   •	int → number of rows affected.
   •	void → if you don’t need a result.
   •	By default, modifying queries clear the persistence context automatically (clearAutomatically = true) → ensures stale entities are not cached.

Example:
```java
@Modifying
@Transactional
@Query(value = "DELETE FROM customer WHERE status = :status", nativeQuery = true)
int deleteByStatusNative(@Param("status") String status);
```
6. Advantages

✅ Cleaner than writing boilerplate EntityManager code.
✅ No need to call entityManager.createQuery("...").executeUpdate().
✅ Integrated with Spring’s transaction management.

⸻


1.	What happens if you use @Modifying without @Transactional?
→ The query will execute but changes won’t be committed → rollback.
2.	What does clearAutomatically = true do in @Modifying?
→ Clears the persistence context to avoid stale entity states.
3.	Can @Modifying queries return entities?
→ No, they can only return int (affected rows) or void.
4.	Difference between using @Modifying vs EntityManager.createQuery(...).executeUpdate()?
→ @Modifying is declarative & integrates with Spring Data, EntityManager is imperative.
5.	Can we use @Modifying for INSERT?
→ Rarely used. Usually for UPDATE & DELETE. Inserts are done via save() or batch inserts.


⸻

✅ 5. Pagination and Sorting
* Pageable, Sort interfaces
1. Sort Interface
  •	Purpose: Defines sorting criteria for queries.
  •	Usage: Pass it to repository methods to sort results.\
```java
List<Customer> findByLastName(String lastName, Sort sort);
```

```java
// Usage
Sort sort = Sort.by("firstName").ascending()
                .and(Sort.by("age").descending());

List<Customer> customers = repo.findByLastName("Sharma", sort);
```
Behind the scenes, this generates SQL with ORDER BY.

2. Pageable Interface
•	Purpose: Encapsulates both pagination (page number, size) and sorting.
•	Implementation: PageRequest is the most common implementation.

```java
Page<Customer> findAll(Pageable pageable);
```

```java
/// Usage
Pageable pageable = PageRequest.of(0, 5, Sort.by("lastName").descending());
Page<Customer> page = repo.findAll(pageable);

List<Customer> customers = page.getContent(); // actual data
long total = page.getTotalElements();         // total records
int pages = page.getTotalPages();             // total pages
```
This executes a paged SQL query using LIMIT and OFFSET.

3. Return Types
   •	List<T> → Just results.
   •	Page<T> → Results + pagination metadata (total pages, total elements, etc.).
   •	Slice<T> → Similar to Page but doesn’t calculate total count (better for performance in large datasets).

4. When to Use
   •	Sort → When you just need ordering.
   •	Pageable → When you need pagination (and possibly sorting).


| Feature  | Sort          | Pageable                                   |
|----------|---------------|--------------------------------------------|
| Purpose  | Order results | Paginate + order results                   |
| SQL      | Adds ORDER BY | Adds LIMIT + OFFSET + ORDER BY             |
| Metadata | None          | Provides total elements, total pages, etc. |
| Impl     | Sort.by(...)  | PageRequest.of(...)                        |

1.	What’s the difference between Page and Slice in Spring Data?
→ Page includes total count query, Slice does not (better for performance).
2.	How does Spring Data translate Pageable into SQL queries?
→ Uses LIMIT and OFFSET (in most DBs).
3.	Can you implement pagination without Pageable?
→ Yes, manually via native queries, but Pageable is cleaner and standard.
4.	What happens if you pass a Sort inside a PageRequest?
→ Sorting is applied in addition to pagination.
5.	What’s the performance cost of using Page?
→ Additional COUNT(*) query is executed to calculate total pages.


* findAll(Pageable pageable)

Provided by Spring Data JPA’s PagingAndSortingRepository and inherited by JpaRepository.
•	Signature:
```java
Page<T> findAll(Pageable pageable);
```

2. Purpose
   •	Retrieves a subset (page) of entities from the database.
   •	Supports pagination + sorting in a single query.

3. Example Usage
```java
@Autowired
private CustomerRepository repo;

public void demo() {
    Pageable pageable = PageRequest.of(0, 5, Sort.by("lastName").descending());
    Page<Customer> page = repo.findAll(pageable);

    List<Customer> customers = page.getContent(); // actual page results
    long totalRecords = page.getTotalElements();  // total rows in DB
    int totalPages = page.getTotalPages();        // total pages
}
```
Generated SQL (in MySQL for example):
```sql
SELECT * 
FROM customer 
ORDER BY last_name DESC 
LIMIT 5 OFFSET 0;

SELECT COUNT(*) 
FROM customer;
```

4. What it Returns
   •	Page<T> object, which includes:
   •	getContent() → The actual list of entities.
   •	getTotalElements() → Total rows in the table.
   •	getTotalPages() → Total number of pages.
   •	getNumber() → Current page number.
   •	getSize() → Page size.
   •	hasNext(), hasPrevious() → Navigation helpers.

⸻

5. When to Use
   •	Paginated results for large datasets (e.g., showing 10 records per page in a UI).
   •	APIs that return pageable results (Page<T> can be easily mapped to JSON).
   •	Avoids loading entire table into memory.

⸻

    1.	What is the difference between findAll(Pageable) and findAll(Sort)?
          •	findAll(Pageable) → Pagination + sorting.
          •	findAll(Sort) → Sorting only (all records).
    2.	What’s the difference between Page and Slice?
          •	Page executes an extra COUNT(*) query for total records.
          •	Slice just fetches limit + 1 records to check if more exist (faster for large datasets).
    3.	How is Pageable implemented internally?
          •	Typically with PageRequest.of(page, size, sort) → converts to LIMIT and OFFSET.
    4.	What happens if you request a page number greater than total pages?
          •	You get an empty list (getContent()), but metadata (getTotalPages()) is still correct.
    5.	How do you handle pagination efficiently for very large datasets?
          •	Instead of OFFSET pagination, use Keyset Pagination (a.k.a. Seek Method).
          •	Example: Use WHERE id > lastFetchedId with LIMIT.


* Creating pagination-friendly REST APIs

Why Pagination in REST APIs?
•	Avoids returning huge datasets in one response.
•	Improves performance & scalability.
•	Client can navigate data in pages (page=0&size=10).
•	Standard practice in microservices / REST APIs.

2. Repository Layer

Use findAll(Pageable pageable) from JpaRepository.
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {}
```

3. Service Layer

Wrap repository call with pagination.
```java
@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
}
```

4. Controller Layer

Expose REST API with Spring Data Pageable.
```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<Customer>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") 
                        ? Sort.by(sortBy).ascending() 
                        : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Customer> customers = customerService.getAllCustomers(pageable);

        return ResponseEntity.ok(customers);
    }
}
```

5. Sample Request

```
GET /api/customers?page=1&size=5&sortBy=lastName&sortDir=asc
```

6. Sample JSON Response

Spring Boot automatically serializes Page<Customer> into JSON:
```json
{
  "content": [
    { "id": 6, "firstName": "Amit", "lastName": "Sharma" },
    { "id": 7, "firstName": "Nikhil", "lastName": "Verma" }
  ],
  "pageable": {
    "pageNumber": 1,
    "pageSize": 5,
    "sort": { "sorted": true, "unsorted": false }
  },
  "totalPages": 10,
  "totalElements": 50,
  "last": false,
  "first": false,
  "numberOfElements": 5,
  "size": 5,
  "number": 1
}
```

7. Best Practices
   •	Always return metadata (total pages, elements, etc.).
   •	Use default values (page=0, size=10).
   •	Validate size (e.g., max 100 records per page).
   •	Consider DTOs instead of exposing entities directly.
   •	For very large datasets → use Keyset Pagination instead of OFFSET.


1.	How does Spring Data JPA implement pagination under the hood?
  → Uses SQL LIMIT + OFFSET, plus a separate COUNT(*) query.
2.	What’s the difference between Page, Slice, and List return types?
•	Page → Data + metadata (extra count query).
•	Slice → Data + “is there next page?” (no count query).
•	List → Just data.
3.	How would you design a pagination API that scales for millions of records?
→ Use Keyset Pagination (Seek Method) instead of OFFSET-based pagination.
4.	How do you handle sorting + pagination together?
→ Use PageRequest.of(page, size, Sort.by("column")).
5.	How would you secure pagination APIs from overloading (DoS attack)?
→ Put a max limit on size param (e.g., 100).

⸻

✅ 6. Specifications (Criteria Queries)
* Dynamic queries using JpaSpecificationExecutor
* Creating reusable Specification objects
* When to use: filtering/searching based on many optional parameters

⸻

✅ 7. Auditing
* Enable auditing: @EnableJpaAuditing
* @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
* Auditing with Spring Security (for user context)

⸻

✅ 8. Projections and DTO Mapping
* Interface-based projections
* Class-based DTO mapping
* JPQL vs Native SQL projections
* Reducing overfetching

⸻

✅ 9. Custom Repository Implementations
* Create custom methods with implementation logic outside of JPA’s built-in features
* Extending default repository and adding custom logic
* When to use: complex SQL, joins, or business rules

⸻

✅ 10. Transaction Management

* How does JPA support transactions?

JPA supports transactions via EntityTransaction (resource-local) or through JTA for container-managed transactions; in Spring you typically use @Transactional which binds the JPA EntityManager to the Spring transaction.

Details
•	Resource-local transactions (EntityTransaction) — used in Java SE or when not using JTA.
Begin/commit/rollback via em.getTransaction().
•	JTA (Java Transaction API) — for XA or container-managed transactions across multiple resources (DB + JMS).
•	Spring integration
Spring’s @Transactional abstracts transaction management (declarative).
Spring configures JpaTransactionManager or JtaTransactionManager depending on environment.
Propagation behaviors (REQUIRED, REQUIRES_NEW, SUPPORTS), isolation levels, rollbackFor settings.
•	Flush & commit
flush() synchronizes persistence context to DB; commit also ends the transaction.
•	Transaction boundaries
Best practice: annotate service layer methods, not repository/DAO, to keep transactional boundaries at the business-operation level.


* @Transactional at method/class level
1. Class-level @Transactional
   •	Declaring @Transactional at the class level means:
   👉 All public methods of that class are transactional by default.
   👉 Each method inherits the class-level transaction settings unless overridden at the method level.
```java 
@Service
@Transactional(readOnly = true)  // default for all methods
public class UserService {
    
    public User getUser(Long id) {   // read-only transaction
        return userRepository.findById(id).orElse(null);
    }
    
    @Transactional(readOnly = false) // overrides class-level setting
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```
Use case:
When most methods in a service should share the same transaction semantics (e.g., readOnly=true for a reporting service).

⸻

2. Method-level @Transactional
   •	Applies only to that specific method.
   •	Overrides class-level configuration if present.
   •	If both class and method have annotations → method wins.
```java
@Service
public class OrderService {
    
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepo.findById(id).orElse(null);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(Order order) {
        orderRepo.save(order);
    }
}
```
Use case:
When different methods require different propagation, isolation, or rollback rules.

⸻

3. Important Points
   •	By default, only public methods are transactional.
   •	Calls within the same class (self-invocation) won’t trigger proxy-based transactions unless you use AspectJ weaving.
   •	Class-level + method-level = flexible: class defines defaults, method fine-tunes.

⸻

4. Best Practices
   •	Put @Transactional at the service layer, not the repository/DAO layer.
   •	Use class-level for defaults (e.g., read-only reporting services).
   •	Override at method-level for exceptions (e.g., write/update operations).
   •	Always consider propagation, isolation, and rollbackFor attributes if relevant.

    1.	What happens if a class has @Transactional(readOnly = true) and a method inside has @Transactional without attributes?
          → The method-level one overrides and will be read/write.
    2.	Does @Transactional work on private or protected methods?
          → No, by default it works only on public methods (proxy-based AOP).
    3.	What’s the difference between class-level vs method-level when it comes to proxying?
          → Both use the same proxy mechanism, but method-level has higher precedence.
    4.	What happens with self-invocation of a transactional method?
          → It bypasses the proxy, so the transaction is not applied. Solution: use AspectJ mode or call from another bean.
    5.	Why is @Transactional better placed at service layer, not repository layer?
          → Because transactions should span business operations, not individual DB calls.

* Propagation types (REQUIRED, REQUIRES_NEW, etc.)
  Propagation defines how a transaction boundary behaves when a method is called inside an existing transaction.
1. REQUIRED (Default)
   •	If a transaction exists → join it.
   •	If none exists → start a new one.
```java
@Transactional(propagation = Propagation.REQUIRED)
public void saveOrder(Order order) {
    orderRepository.save(order);
}
```
Use case: Most business methods. Ensures everything runs in one transaction.

2. REQUIRES_NEW
   •	Always starts a new transaction, suspending the current one (if any).
   •	Commits/rolls back independently.
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void auditLog(String action) {
    auditRepository.save(new Audit(action));
}
```
Use case: Logging, audit trails, notifications → should succeed even if main transaction fails.

3. SUPPORTS
   •	If a transaction exists → join it.
   •	If none exists → run without a transaction.
```java
@Transactional(propagation = Propagation.SUPPORTS)
public List<Order> getOrders() {
    return orderRepository.findAll();
}
```
Use case: Read-only operations that may run inside or outside a transaction.

4. MANDATORY
   •	Must run inside an existing transaction.
   •	If none exists → throws TransactionRequiredException.
```java
@Transactional(propagation = Propagation.MANDATORY)
public void updateInventory() {
    inventoryRepo.decrementStock();
}
```
Use case: Internal methods that must always be called within a transaction.

5. NOT_SUPPORTED
   •	Runs without a transaction.
   •	If a transaction exists → suspend it.
   Use case: Long-running, non-critical tasks (e.g., reports, analytics).

6. NEVER
   •	Must run without a transaction.
   •	If a transaction exists → throws exception.
   Use case: Operations that should never run in a transaction (e.g., schema checks).

7. NESTED 
    •	If a transaction exists → create a nested transaction (via savepoints).
    •	If none exists → behaves like REQUIRED.
   Use case: Partial rollback inside a larger transaction.

| Propagation Type | If Tx Exists         | If No Tx Exists | Example Use Case           |
|------------------|---------------------|-----------------|---------------------------|
| REQUIRED         | Join existing       | Start new       | Business methods          |
| REQUIRES_NEW     | Suspend + new Tx    | Start new       | Logging, audit            |
| SUPPORTS         | Join existing       | Run without     | Optional Tx read          |
| MANDATORY        | Join existing       | Throw error     | Must run in Tx            |
| NOT_SUPPORTED    | Suspend Tx          | Run without     | Reports, analytics        |
| NEVER            | Throw error         | Run without     | Non-Tx methods            |
| NESTED           | Nested Tx           | Start new       | Partial rollback          |

1.	What is the difference between REQUIRES_NEW and NESTED?
→ REQUIRES_NEW suspends the parent transaction; NESTED uses savepoints within the same transaction.
2.	What happens if you call a @Transactional(REQUIRES_NEW) method from another @Transactional method?
→ The parent transaction is suspended, and a completely new transaction begins.
3.	Why is SUPPORTS rarely used in production?
→ Because behavior changes depending on caller context, which can cause unpredictable results.
4.	Can NESTED work with JPA/Hibernate?
→ Not always. It requires JDBC savepoints. Hibernate supports it only with certain DBs/drivers.
5.	If you mark everything as REQUIRES_NEW, what’s the downside?
→ Performance overhead (more commits), and you lose atomicity (everything doesn’t roll back together).


* Read-only vs modifying transactions
  When we use @Transactional, we can specify whether a transaction is read-only or not:
```java
@Transactional(readOnly = true)
public List<Order> getOrders() { ... }

@Transactional
public void saveOrder(Order order) { ... }
```
1. Read-Only Transactions
   •	Definition: Marked with @Transactional(readOnly = true).
   •	Purpose: Optimize performance for queries.

How it Works:
•	Spring passes the readOnly = true hint to the transaction manager (Hibernate/JPA/JDBC).
•	Hibernate/JPA may:
•	Avoid dirty checking (skips detecting changes in entities).
•	Flush mode is set to FlushMode.MANUAL → No auto-flush before queries.
•	Database may:
•	Some DBs can optimize query execution for read-only transactions.
•	But many don’t enforce it strictly (it’s a hint, not a guarantee).

✅ Use Case: Service methods that only fetch data, not modify it.

⸻

2. Modifying (Read-Write) Transactions
   •	Definition: Default behavior of @Transactional.
   •	Purpose: Allow insert, update, delete.

How it Works:
•	Hibernate/JPA:
•	Keeps entities in persistence context.
•	Performs dirty checking at commit to flush changes.
•	DB:
•	Executes DML (INSERT/UPDATE/DELETE).
•	Ensures ACID properties of transactions.

✅ Use Case: Methods that modify state — e.g., saving an order, updating inventory.

⸻

Comparison Table

| Aspect                | Read-Only Tx (`@Transactional(readOnly = true)`) | Modifying Tx (`@Transactional`) |
|-----------------------|--------------------------------------------------|-------------------------------|
| Hibernate Flush       | Disabled (manual only)                           | Auto at commit                |
| Dirty Checking        | Skipped                                          | Enabled                       |
| Performance           | Faster (less overhead)                           | More overhead                 |
| DB Writes             | Not allowed (ideally)                            | Allowed                       |
| Typical Use Case      | Queries                                          | Inserts/Updates/Deletes       |


NOTE: readOnly = true is a performance optimization hint. It does not enforce read-only at the DB level. You can still execute DML statements, but it may lead to inconsistent behavior.

    1.	What happens if you try to perform an update inside a readOnly = true transaction?
      •	Hibernate may throw an exception or silently ignore changes (depends on provider & flush mode).
      2.	Is readOnly = true always enforced at the database level?
      •	No. It’s mostly a hint for the ORM (Hibernate). Some databases ignore it.
      3.	If read-only is faster, why not make everything read-only?
      •	Because then you can’t persist changes — updates won’t be flushed.
      4.	How does Hibernate optimize performance in read-only transactions?
      •	By skipping dirty checking and preventing auto-flush before queries.
      5.	Can you override read-only at runtime?
      •	Yes, but generally discouraged. You should clearly separate read and write service methods.


* Rollbacks and exception handling

1. Default Rollback Behavior
   •	By default, Spring rolls back a transaction only on unchecked exceptions:
   •	RuntimeException (e.g., NullPointerException, IllegalArgumentException)
   •	Error (e.g., OutOfMemoryError)
   •	Checked exceptions (e.g., IOException, SQLException) → transaction is not rolled back unless explicitly told.
```java
@Transactional
public void placeOrder() {
    throw new RuntimeException("rollback!"); // triggers rollback
}

@Transactional
public void placeOrder() throws IOException {
    throw new IOException("won't rollback by default!");
}
```

2. Controlling Rollback Behavior

Spring lets you customize rollback rules using @Transactional attributes:

Rollback for Checked Exceptions
```java
@Transactional(rollbackFor = IOException.class)
public void processFile() throws IOException {
    throw new IOException("Now rollback will happen!");
}
```

Prevent Rollback for Specific Exceptions
```java
@Transactional(noRollbackFor = CustomBusinessException.class)
public void businessOperation() {
    throw new CustomBusinessException("Transaction still commits!");
}
```

3. Exception Handling and Transactions
   •	If you catch an exception inside the method → Spring doesn’t see it → transaction won’t roll back.
```java
@Transactional
public void processOrder() {
    try {
        // some DB updates
        throw new RuntimeException("Error");
    } catch (Exception e) {
        // handled internally → transaction commits!
    }
}
```
Best practice:
•	Don’t swallow exceptions inside transactional methods.
•	Re-throw or let them propagate so Spring can handle rollback.

4. Programmatic Rollback

Sometimes you want manual control:
```java
@Autowired
private PlatformTransactionManager txManager;

@Transactional
public void processPayment() {
    try {
        // risky code
    } catch (Exception e) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
}
```

5. Nested Rollbacks
   •	If you use Propagation.REQUIRES_NEW, inner transactions can roll back independently of outer ones.
   •	If you use Propagation.REQUIRED, rollback cascades to the entire transaction.

| Exception Type | Default Behavior | Can Override?                          |
|-----------------|------------------|----------------------------------------|
| RuntimeException | Rollback         | Yes                                    |
| Error | Rollback         | Yes                                    |
| Checked Exception | No Rollback      | Yes (with rollbackFor)                 |
| Caught Exception | No Rollback      | Must rethrow or mark rollback manually |            


1.	Why does Spring only roll back on runtime exceptions by default?
      → Because checked exceptions are often recoverable (business logic), while runtime exceptions indicate programming errors.
      2.	How do you force rollback on checked exceptions?
      → Use @Transactional(rollbackFor = Exception.class)
      3.	What happens if you catch an exception inside a transactional method?
      → No rollback unless you rethrow or call setRollbackOnly().
      4.	What’s the difference between rollbackFor and noRollbackFor?
      → rollbackFor enforces rollback, noRollbackFor prevents rollback.
      5.	How does rollback work with nested transactions (REQUIRES_NEW)?
      → Inner transaction rollback does not affect the outer one (independent).




⸻

✅ 11. Spring Data Testing
* Unit tests with @DataJpaTest
* Using in-memory DBs (like H2)
* Testing queries and repositories
* Transaction rollback after tests

⸻

✅ 12. Spring Data for NoSQL 

Spring Data JPA vs Spring Data MongoDB, Cassandra, Elasticsearch, Redis, etc.
Spring Data provides a consistent programming model for different data stores, but the underlying implementation and use cases differ depending on the database type.

1. Spring Data JPA (Relational Databases)
   •	Works with relational databases (MySQL, PostgreSQL, Oracle, SQL Server, etc.) via JPA (Java Persistence API) and Hibernate.
   •	Entities are mapped to tables using @Entity, @Table, etc.
   •	Supports transactions, joins, complex queries, and relationships (@OneToMany, @ManyToOne).
   •	Queries can be JPQL, native SQL, or derived query methods.

👉 Best for: Applications requiring ACID transactions, relational modeling, strong consistency.

⸻

2. Spring Data MongoDB (NoSQL – Document Store)
   •	Works with MongoDB, a document-oriented NoSQL database.
   •	Entities are stored as JSON documents instead of rows in a table.
   •	Uses annotations like @Document, @Field instead of JPA annotations.
   •	No joins (data is usually embedded or denormalized).
   •	Supports rich queries, aggregation framework, geospatial queries.

👉 Best for: Schema-less, high-volume, scalable applications.

⸻

3. Spring Data Cassandra (NoSQL – Column Store)
   •	Works with Apache Cassandra, a column-family NoSQL DB designed for high availability and scalability.
   •	Data modeled using tables but with a column-family approach (not strict relational).
   •	Uses annotations like @Table, @PrimaryKey, @Column.
   •	Best suited for time-series data, event logging, IoT, high write throughput use cases.

👉 Best for: Highly available, horizontally scalable apps (e.g., Netflix, IoT platforms).

⸻

4. Spring Data Elasticsearch (Search Engine)
   •	Works with Elasticsearch, a distributed full-text search and analytics engine.
   •	Entities are indexed as documents for search.
   •	Provides powerful full-text search, fuzzy search, autocomplete, analytics, and real-time search queries.
   •	Uses @Document(indexName="...") annotation.

👉 Best for: Applications needing advanced search functionality (e-commerce, logs, recommendation engines).

⸻

5. Spring Data Redis (Key-Value Store, In-Memory)
   •	Works with Redis, an in-memory key-value store.
   •	Data stored as keys and different structures (string, hash, list, set, sorted set).
   •	Extremely fast (used for caching, session management, leaderboards, message queues).
   •	Provides pub/sub messaging support and distributed caching features.

👉 Best for: Caching, session storage, real-time analytics, distributed applications.

⸻


| Feature         | JPA (Relational)         | MongoDB (Document)         | Cassandra (Column)         | Elasticsearch (Search)         | Redis (Key-Value)         |
|-----------------|-------------------------|----------------------------|----------------------------|-------------------------------|---------------------------|
| Data Model      | Tables & Rows           | JSON Documents             | Column Families            | Indexed Documents             | Key-Value Pairs           |
| Schema          | Fixed (DDL)             | Schema-less                | Semi-Structured            | Schema-less                   | Schema-less               |
| Query Language  | JPQL / SQL              | Mongo Query API            | CQL (Cassandra Query Language) | Elasticsearch Query DSL   | Redis Commands            |
| Best Use Case   | Transactions, joins     | Flexible schema, large volumes | High write throughput, HA | Full-text search, analytics   | Caching, sessions         |
| Scalability     | Vertical + some horizontal | Horizontal               | Horizontal                 | Horizontal                    | Horizontal                |
| Consistency     | Strong (ACID)           | Eventual                   | Tunable (AP system)        | Eventual                      | Eventual (but fast)       |




⸻

✅ 13. Advanced Concepts
* Entity lifecycle callbacks: @PrePersist, @PostUpdate, etc.
* Soft deletes (logical deletes)
* Envers for auditing history (optional)
* Caching frequently accessed data with @Cacheable

⸻

✅ 14. Best Practices
* Avoid N+1 queries (use @EntityGraph or JOIN FETCH)
* Use DTOs for responses, not entities directly
* Use pagination to avoid loading large result sets
* Keep queries readable and maintainable

