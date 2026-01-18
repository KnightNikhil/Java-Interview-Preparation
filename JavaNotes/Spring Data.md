# Spring Data JPA Interview Prep Guide

## 1. Spring Data Overview

### What is Spring Data?
Spring Data is a part of the Spring ecosystem that simplifies data access in Java applications. It provides a consistent, easy way to interact with relational (JPA/Hibernate, JDBC), NoSQL (MongoDB, Cassandra, Redis), and newer stores like Elasticsearch.

- **Goal:** Reduce boilerplate for data access via automatic repository generation.
- Example: Define repository interfaces and implementations are auto-generated at runtime.

#### Key Features
- **Repository Abstraction:** `CrudRepository`, `JpaRepository`, etc. for out-of-the-box CRUD/pagination.
- **Derived Query Methods:** Queries can be created simply by method naming conventions, e.g., `findByEmailAndStatus`.
- **Custom Queries:** Allows writing queries using JPQL, native SQL, or `@Query`.
- **Paging and Sorting:** Built-in `Pageable` and `Sort`.
- **Cross-Store Support:** Works with both Relational & NoSQL databases seamlessly.
- **Spring Boot Integration:** Works with starter dependencies (e.g., spring-boot-starter-data-jpa, spring-boot-starter-data-mongodb) for quick setup.

#### Advantages
- **Less boilerplate code** : Developers only define interfaces, no need to write DAO implementations.
- **Consistency** : Same programming model across different data stores.
- **Productivity Boost** : With repositories, queries, and paging features readily available.
- **Flexibility** : Supports custom queries and advanced data access scenarios.

***

### Role in Layered Architecture
Spring applications typically use:

- **Controller Layer:** Handles HTTP requests (REST APIs).
- **Service Layer:** Contains business logic, orchestrates between controller and repository.
- **Repository Layer (Persistence):** Handles database interactions.

**Spring Data abstracts the Repository Layer**, no manual DAO implementations needed.

Instead of writing DAO classes manually with JDBC, Hibernate, or boilerplate CRUD methods, we only define Repository Interfaces, and Spring Data automatically provides implementations at runtime.

***

### How It Works

#### Repository Abstraction Example
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
}
```
*No manual SQL needed; `findByLastName` is auto-implemented!*

#### Benefits in Layered Architecture
- Loose coupling; service layer depends on interfaces.
- Consistent API (`save`, `findAll`, etc.) for diverse data stores.
- Query abstraction supports method names, JPQL, native, criteria.
- Custom methods possible.
- Separation of concerns: business logic in Service; persistence in Repository.

#### Layered Example
**Controller**
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

**Service**
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

**Repository**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
}
```

***

### DAO Pattern

A **DAO (Data Access Object)** is a design pattern used to separate the data persistence logic from the business logic in an application. The DAO acts as an interface or object that provides a standardized way to access a database or other storage system, allowing the rest of the code to interact with data without knowing the underlying details of how that data is stored or retrieved.

**The Core Problem DAO Solves**
- Imagine you are building a Java application.

- You have **business logic** like:
  - Transfer money
  - Create user
  - Fetch orders
  - Validate accounts

- And you also need to:
  - Talk to a database
  - Write SQL
  - Handle connections
  - Map rows to objects

**The BIG question:** Should business logic know HOW data is stored?
**Answer: NO**

This is where DAO pattern comes in.

- In simple words:
  - Business code says **WHAT** it wants
  - DAO code knows **HOW** to get it from DB


Without DAO (Tightly Coupled – BAD)
```java
class AccountService {

    public Account getAccount(Long id) {
        Connection con = DriverManager.getConnection(...);
        PreparedStatement ps =
            con.prepareStatement("SELECT * FROM account WHERE id=?");
        ...
    }
}
```
With DAO Pattern (Clean Separation – GOOD)
```java
class AccountService {
    private AccountDao accountDao;

    public Account getAccount(Long id) {
        return accountDao.findById(id);
    }
}
```
DAO handles DB logic:
```java
class AccountDao {
    public Account findById(Long id) {
        // SQL / JPA / Hibernate code
    }
}
```

### DAO Pattern Structure

**Three Main Layers**

```
Controller
   ↓
Service (Business Logic)
   ↓
DAO (Data Access)
   ↓
Database
```

Each layer has **one responsibility**.

DAO Components Explained

### Entity (Domain Object)

Represents a table row.

```java
class Account {
    Long id;
    double balance;
}
```

---

### DAO Interface

Defines **WHAT operations are allowed**.

```java
public interface AccountDao {
    Account findById(Long id);
    void save(Account account);
    void delete(Long id);
}
```

**Why interface?**
- Loose coupling
- Easy testing
- Easy implementation change

---

### DAO Implementation

Defines **HOW operations are done**.

```java
public class AccountDaoJdbcImpl implements AccountDao {

    public Account findById(Long id) {
        // JDBC code
    }
}
```

Later you can switch to:
- AccountDaoJpaImpl
- AccountDaoHibernateImpl
- AccountDaoMongoImpl

Without touching business logic.

### DAO Pattern Advantages

1. Separation of concerns
2. Clean architecture
3. Easy testing
4. Easy DB migration
5. Centralized data access
6. Reusable logic

### DAO Pattern Disadvantages

1. Extra layer (boilerplate)
2. Overkill for small apps
3. Duplicate CRUD code (before Spring Data)

### DAO Pattern in Spring Boot (Real World)

Spring Data JPA eliminates boilerplate:

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

Behind the scenes:
**This IS DAO pattern, auto-generated.**

### Does Spring Data Replace the DAO Layer?
- **Not fully:** Automates common CRUD/queries (removes ~80% boilerplate).
- **Complex scenarios:** Custom DAO/repo code may still be needed for batch ops, stored procedures, etc.

### DAO vs Repository Pattern — Explained From First Principles

#### 1. Why This Comparison Even Exists

Both **DAO** and **Repository** exist to solve **one core problem**:

> *Business logic should NOT care about how data is stored or fetched.*

But they approach this problem from **different abstraction levels** and **different mindsets**.

To understand the difference properly, we must first understand **what problem each one was designed to solve**.

#### 2. DAO Pattern — The Database-Centric View

**What DAO Really Is**

**DAO (Data Access Object)** is a pattern that focuses on:

> “How do I safely and cleanly talk to the database?”

DAO is **data-source oriented**, not domain-oriented.

It hides:
- SQL
- JDBC / Hibernate / JPA
- Connection handling
- Query execution

from the business layer.

**Mental Model of DAO**

Think of DAO as:

> “A helper whose only job is to fetch/store data from a database.”

Business layer says:
```
Give me User with id 10
```

DAO thinks:
```
Which table?
Which query?
Which joins?
Which DB?
```

**DAO Structure**

Typical DAO setup:

```
Service Layer
     ↓
DAO Interface
     ↓
DAO Implementation
     ↓
Database
```

Example:

```java
public interface UserDao {
    User findById(Long id);
    void save(User user);
}
```

```java
public class UserDaoJpaImpl implements UserDao {
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
}
```

**Key Characteristics of DAO**

- Low-level abstraction
- Database-focused
- Often mirrors tables
- CRUD-heavy
- One DAO per table (commonly)

---

#### Repository Pattern — The Domain-Centric View

**What Repository Really Is**

**Repository** comes from **Domain-Driven Design (DDD)**.

Its goal is NOT database access.

Its goal is:

> “Represent a collection of domain objects.”

Repository pretends that:
- Database does not exist
- Storage is just a collection

**Mental Model of Repository**

Think of Repository as:

> “An in-memory collection that happens to be backed by a database.”

You don’t ask:
```
Run query X
```

You ask:
```
Give me all active users
Save this order
```

**Repository Structure**

```
Service Layer
     ↓
Repository
     ↓
Persistence Mechanism (hidden)
```

Example:

```java
public interface UserRepository {
    User findByEmail(String email);
    List<User> findActiveUsers();
}
```

Notice:
- No SQL
- No DB terminology
- Pure domain language

**Key Characteristics of Repository**

- High-level abstraction
- Domain-focused
- Collection-like behavior
- Business-friendly methods
- Aggregates-aware (DDD)

**Core Difference in One Sentence**

> **DAO talks in terms of tables and queries.**  
> **Repository talks in terms of domain and behavior.**

#### DAO vs Repository — Side-by-Side Comparison

| Aspect | DAO | Repository |
|-----|----|----|
| Origin | Traditional layered architecture | Domain-Driven Design |
| Focus | Database | Domain |
| Abstraction level | Low | High |
| Talks about | Tables, rows, queries | Aggregates, entities |
| Interface language | Technical | Business-oriented |
| Who uses it | Service layer | Domain layer |
| Knowledge of DB | Yes | Hidden |
| Common in | Legacy Java, JDBC | Spring Data JPA |


#### Example: Same Use Case, Different Approach

**Requirement:**
> Fetch all active users created in last 30 days


**DAO Style**

```java
List<User> findActiveUsersLast30Days();
```

Implementation:
- SQL with WHERE, JOIN, DATE filters
- Query logic dominates

DAO thinks:
```
How do I query this data?
```


**Repository Style**

```java
List<User> findRecentlyActiveUsers();
```

Repository thinks:
```
What does the domain mean by "recently active"?
```

Implementation detail is hidden.

**Why Repository Is Considered an Evolution of DAO**

DAO solved:
- SQL leakage
- Tight DB coupling

But still had problems:
- Too many CRUD methods
- Database-centric naming
- Business logic leaked into services

Repository improves by:
- Speaking domain language
- Grouping behavior around aggregates
- Reducing service complexity

---

#### Spring Data JPA — Where Confusion Starts

**Important Truth:**
**Spring Data Repositories ARE DAOs internally.**

But externally:
- They behave like repositories
- They expose domain language

Example:

```java
public interface UserRepository
        extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

Internally:
- Query generation
- EntityManager usage
- DAO mechanics

Externally:
- Clean domain abstraction

---

**Can DAO and Repository Coexist?** YES — in complex systems

Common real-world setup:

```
Controller
   ↓
Service
   ↓
Repository (Domain logic)
   ↓
DAO (Complex queries, native SQL)
   ↓
Database
```

Repository uses DAO internally for:
- Reporting
- Analytics
- Native queries
- Performance-critical operations


**When to Use DAO Pattern**

Use DAO when:
- You use JDBC or MyBatis
- You need fine-grained SQL control
- Application is data-centric
- Legacy systems

**When to Use Repository Pattern**

Use Repository when:
- Using JPA / Hibernate
- Domain logic is complex
- You follow DDD
- Spring Boot applications
- Long-term maintainability matters

---

**Interview-Grade Summary (Memorize This)**

> **DAO abstracts database access.**  
> **Repository abstracts domain collections.**
>
> **Repository is a higher-level abstraction built on top of DAO concepts.**

---

**One-Line Killer Explanation (Senior-Level)**

> “DAO answers *how data is fetched*.  
> Repository answers *what data the domain needs*.”
"""

***

## 2. Spring Data JPA Basics

### What is JPA?
Java Persistence API (JPA) standardizes how Java objects (POJOs) are mapped to database tables and persisted.

- JPA is a specification, not an implementation.
- Common providers: **Hibernate**, **EclipseLink**, **OpenJPA**.

#### Core Purpose
- Simplifies object-relational mapping (ORM).
- Eliminates JDBC boilerplate.
- Enables code portability across providers.

#### Key Features

1. **ORM Mapping**
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

2. **EntityManager API**
    ```java
    @PersistenceContext
    private EntityManager em;

    public void saveUser(User user) {
        em.persist(user);
    }
    public User getUser(Long id) {
        return em.find(User.class, id);
    }
    ```

3. **Querying: JPQL**
    ```java
    List<User> users = em.createQuery(
        "SELECT u FROM User u WHERE u.email = :email", User.class)
        .setParameter("email", "abc@xyz.com")
        .getResultList();
    ```

4. **Transactions:** Integrates with JTA/Spring TransactionManager.
5. **Caching:** First-level (per EntityManager) and optional second-level (provider-specific).

#### JPA vs JDBC
- JDBC: Handles connections/statements manually.
- JPA: Works with objects, auto-generates SQL.
- Benefit: Productivity and maintainability.

#### Architecture
```
EntityManager → Provider (e.g., Hibernate) → JDBC → Database
```
1.	JPA (specification) → defines interfaces like EntityManager, EntityTransaction.
2.	JPA Provider (implementation) → Hibernate, EclipseLink, etc.
3.	Database → actual SQL queries executed.

***

## Internal Working of JPARepository in Spring Data JPA

### 1. Breakdown of JPARepository Internal Mechanisms

- **JPARepository** is an interface provided by Spring Data JPA that extends repository interfaces such as `CrudRepository` and `PagingAndSortingRepository`.
- It provides built-in methods for standard CRUD operations, pagination, sorting, and JPA-specific capabilities like flushing or batch deletions.
- However, **JPARepository itself is just an interface**, so the actual logic for these methods does not exist inside it.
- SimpleJpaRepository<T, ID> implements JpaRepositoryImplementation<T, ID>, class hold the implementation of all the methods defined in JpaRepository, so if our repository does not have any custom methods then SimpleJpaRepository will be used to provide the implementation of all the methods defined in JpaRepository.

***

### 2. Dynamic Implementation Generation Using Proxies

- At **runtime**, Spring Data automatically creates a **proxy implementation** of the repository interface.
- This dynamic proxy is generated by Spring's infrastructure via `RepositoryFactoryBean`.
- The proxy implements all repository methods by intercepting calls and delegating to appropriate data access logic.
- This means developers only need to declare the interface; Spring handles generating the concrete implementation under the hood.
- This mechanism avoids boilerplate DAO implementation code, improves reuse, and simplifies data access.

***

### 3. Use of `EntityManager` Behind the Scenes

- The dynamically generated repository proxy internally uses the **JPA `EntityManager`** to perform all persistence operations.
- The `EntityManager` is the JPA interface responsible for interacting with the persistence context and database.
- Repository methods like `save()`, `findById()`, `deleteById()`, etc., translate into EntityManager calls such as:
    - `persist()`, `merge()`
    - `find()`
    - `remove()`
- For query methods, the proxy builds JPQL or SQL queries and executes them through `EntityManager`.

***

### 4. Responsibilities of `EntityManager`

- The `EntityManager` manages the **entity lifecycle** and tracks entity states within the persistence context.
- It executes JPQL, Criteria API, or native SQL queries.
- Maintains a cache of managed entities, ensuring efficiency and consistency.
- Acts as the core interface for all direct interaction with the database.
- Manages transaction boundaries and synchronizes changes using `flush()`.

***

### 5. Proxy Intercepts Repository Method Calls and Translates into Persistence Operations

- When a repository method is invoked, the Spring-generated proxy intercepts the method call.
- It analyzes the method signature and name:
    - For standard CRUD operations, it maps method calls directly to EntityManager API calls.
    - For derived query methods (e.g., `findByLastNameAndStatus`), the proxy parses method names and generates JPQL queries dynamically.
    - For methods annotated with `@Query`, it executes the specified JPQL or native SQL queries.
- The proxy then performs the necessary SQL execution, manages transaction and session context implicitly, and returns mapped entity results.
- This abstraction hides the complexity of query creation, transaction management, and entity state handling from the developer.

***

### Summary Flow
```
 -> Application code calls JPARepository method
 -> Spring Data proxy intercepts the call
 -> Proxy analyzes method type (CRUD, derived, custom query)
 -> Proxy calls corresponding EntityManager functions
 -> EntityManager executes queries/operations in persistence context
 -> Results returned to application transparently
```

***


### EntityManager: Core Interface
- EntityManager is the primary JPA interface that manages the persistence context and CRUD/query operations for entity instances.

<img src="assets/entity-lifecycle.png" alt="JPA EntityManager" width="600"/>

<img src="assets/entity-manager-lifecycle.png" alt="JPA EntityManager" width="600"/>

#### What is Persistence Context?

- **Persistence Context** is a set or environment within the JPA runtime that **manages a collection of entity instances** (objects representing rows in the database) currently being tracked by an `EntityManager`.
- It acts as a **first-level cache** where persisted entities are stored during a transaction or a session.
- Each entity instance in the persistence context corresponds uniquely to a specific database row; thus, for each entity identity, there is exactly one managed instance.
- The **persistence context** is a core concept in JPA that acts as the first-level cache and entity state manager. It provides identity, lifecycle, and change tracking for entities within a transaction, enabling efficient and consistent data access.

***

#### Responsibilities of Persistence Context

- **Entity Lifecycle Management:**  
  It manages entities through various states:
    - **Transient:** New entities not yet associated with persistence context.
    - **Managed:** Entities currently tracked and synchronized with the database.
    - **Detached:** Entities no longer tracked (persistence context closed or cleared).
    - **Removed:** Entities scheduled for deletion.
- **Change Tracking (Dirty Checking):**  
  Persistence context detects any changes to managed entities and ensures updates get synchronized to the database at the appropriate time (flush/commit).
- **Identity Guarantee:**  
  It guarantees that repeated fetches of the same entity within one persistence context return the **same Java object instance**, maintaining consistency.
- **Transaction Scope:**  
  The persistence context is often scoped to the lifecycle of a transaction, ensuring consistency across operations within that transaction.
- **Query Execution Cache:**  
  It can cache query results and reduce redundant database access during its lifecycle.

***

#### How Persistence Context Works with EntityManager

- `EntityManager` represents and manages the persistence context.
- When you perform operations like `persist()`, `find()`, `merge()`, or `remove()`, you are interacting with entities in the persistence context.
- Transactions control when the persistence context is flushed to the database, writing all accumulated changes.

***

#### Benefits of Persistence Context

- **Performance:** Reduces unnecessary database hits by caching entities and changes during a transaction.

    Example:
    ```java
    User user = repository.findById(1L); // First DB hit
    User sameUser = repository.findById(1L); // No DB hit, returns cached instance
    ```
    Is @Transactional annotation is related to the caching mechanism in JPA/Hibernate?
    * Without a transaction (i.e., no `@Transactional` boundary), the persistence context lifetime becomes shorter and less predictable, reducing effective caching benefits.
    * Thus, `@Transactional` helps enable and manage the lifecycle of the persistence context, making caching reliable and predictable throughout the method execution.

- **Consistency:** Ensures entity references remain consistent within transactions.
- **Simplification:** Developers work with objects, not raw database state; JPA and persistence context handle synchronization automatically.

***

#### Example

- When you query an entity by ID using EntityManager’s `find()`, JPA first checks the persistence context:
    - If the entity instance already exists in the context, it returns it directly without querying the database.
    - If not present, it fetches from the database, places it into the context, and returns it.

***


## EntityManager and Hibernate Entity Lifecycle

### Role of EntityManager

- **EntityManager** manages the **persistence context** (the first-level cache).
- It provides CRUD operations:
    - `persist()`: Inserts new entities into the database.
    - `find()`: Retrieves entities by primary key.
    - `merge()`: Updates an existing entity or merges detached entity state.
    - `remove()`: Deletes entities.
- EntityManager supports executing JPQL (Java Persistence Query Language), Criteria API, and native SQL queries.
- It is responsible for transaction handling and caching entity states.

***

### Hibernate Entity Lifecycle States

An entity instance navigates through these lifecycle states within the persistence context managed by EntityManager:

1. **Transient (New) State**
    - When you create a new Java object using `new`, but **it is not associated** with any persistence context or database record.
    - The entity is not yet persisted.
    - Changes to this object are not tracked or saved.
    - Example:
      ```java
      Employee e = new Employee(); // Transient state
      ```

2. **Managed (Persistent) State**
    - When the entity is **attached to the persistence context**.
    - The EntityManager **tracks all changes** made to this entity and synchronizes them with the database during flush or commit.
    - The entity corresponds to a row in the database.
    - Entities enter this state by calling:
        - `entityManager.persist(entity)`
        - Loading an entity via `entityManager.find()` or JPQL queries
        - Calling `merge()` on a detached entity
    - Example:
      ```java
      entityManager.persist(e); // e is now managed
      ```

3. **Detached State**
    - When the persistence context is closed or cleared, the entity becomes **detached**.
    - The entity still corresponds to a database row but **is no longer tracked** by the EntityManager.
    - Changes made to detached entities are not automatically synchronized with the database.
    - Detached entities can be reattached to a new persistence context with `merge()`.
    - Example:
      ```java
      entityManager.close(); // All managed entities become detached
      ```

4. **Removed State**
    - Entities scheduled for deletion are in the **removed** state.
    - The deletion occurs in the database when the persistence context is flushed or committed.
    - EntityManager marks the entity for removal via the `remove()` method.
    - Example:
      ```java
      entityManager.remove(e); // e is marked removed
      ```

***

### Entity State Transitions

- Transient → Managed: via `persist()` or by loading/finding entity.
- Managed → Detached: when persistence context closes or `detach()` is called.
- Detached → Managed: via `merge()`.
- Managed → Removed: via `remove()`.

***

### Practical Explanation

- While managed, any changes to the entity's fields are automatically detected (dirty checking) and written to the DB on commit.
- Detaching the entity stops this tracking.
- Removing marks the entity for deletion.
- Persistence context ensures identity and consistency of entities during transactions.

***

### Summary

| State       | Description                                   | JPA Example                          |
|-------------|-----------------------------------------------|------------------------------------|
| Transient   | New object, not tracked, not persisted       | `new Entity()`                     |
| Managed     | Tracked by persistence context, auto-sync   | `entityManager.persist(entity)`   |
| Detached    | Exists in DB but no longer tracked           | After `entityManager.close()`      |
| Removed     | Scheduled for deletion                        | `entityManager.remove(entity)`    |


***

Here is a detailed explanation of the **Dirty Checking mechanism with @Transactional annotation** in JPA/Hibernate based on the video section (56:46 - 60:00) and supported by authoritative sources:

***

## Dirty Checking with @Transactional Annotation

### What is Dirty Checking?

- **Dirty Checking** is a mechanism in JPA/Hibernate that automatically detects changes made to managed entity objects during a transaction.
- When an entity is loaded into the persistence context (managed state), Hibernate takes a snapshot of the entity’s initial state.
- As the entity’s properties are modified in Java code, Hibernate tracks those changes.
- At **flush** or **transaction commit** time, Hibernate compares the current state against the original snapshot.
- If differences (dirty attributes) are found, Hibernate **automatically issues SQL `UPDATE` statements** to persist those changes to the database.

### How does @Transactional work with Dirty Checking?

- Methods annotated with `@Transactional` define the boundary of a database transaction.
- During a transactional method execution, all entity modifications within the persistence context are tracked.
- When the method completes successfully, the transaction commits, triggering `flush()` on the persistence context.
- The flush process performs dirty checking and synchronizes all entity changes with the database.
- Developers **do not need to explicitly call `save()` or `update()`** after changing entity attributes in a transactional context.

### Practical Example

```java
@Transactional
public void updateUserName(Long id, String newName) {
    User user = entityManager.find(User.class, id); // user becomes managed entity
    user.setName(newName); // change tracked automatically by persistence context
    //    userRepository.save() 
    // No need to call entityManager.merge or userRepository.save explicitly
}
// On transaction commit, Hibernate detects change and runs SQL UPDATE
```

- The setter call changes the entity state.
- Hibernate's dirty checking automatically detects this during transaction commit.
- Updates the database without explicit save calls.

### Benefits

- **Reduces boilerplate code:** No need for manual update calls.
- **Consistency:** Ensures all changes within a transaction are saved atomically.
- **Simplifies developer work:** Just update entities, no explicit persistence calls needed.
- **Transactional safety:** Part of the transactional context, changes roll back on failure.

***

### Summary

| Feature                     | Description                                                   |
|-----------------------------|---------------------------------------------------------------|
| Dirty Checking Definition   | Automatic detection of changes in managed entities            |
| Trigger                     | Happens on `flush()` or `commit()`                             |
| Role of `@Transactional`    | Defines transaction boundary that ensures dirty checking runs |
| Developer Action            | Modify entity properties as normal; no explicit save needed   |
| Outcome                    | Hibernate generates and executes SQL `UPDATE` statements      |

***


## 3. Repositories

### JPA vs JDBC

| Aspect           | JPA                                  | JDBC                  |
|------------------|--------------------------------------|-----------------------|
| Boilerplate      | Minimal                              | Substantial           |
| Mapping          | Entity/relationship                  | Rows/columns          |
| Query Type       | JPQL, Criteria, derived              | SQL only              |
| Caching          | Built-in L1/L2                       | None                  |
| Portability      | Higher-level API, cross-provider     | Manual, DB-specific   |
| Control          | Less fine-tuned                      | Full control          |
| Debugging        | ORM issues/SQL inspections           | Raw SQL errors        |

***

### CrudRepository

- Provides generic CRUD operations for entities.
- Methods include `save`, `findById`, `findAll`, `delete`, etc.
- Not JPA-specific (used for MongoDB, Cassandra).

**Sample Interface**
```java
public interface CrudRepository<T, ID> {
    <S extends T> S save(S entity);
    Optional<T> findById(ID id);
    Iterable<T> findAll();
    void deleteById(ID id);
}
```

**Usage**
```java
public interface CustomerRepository extends CrudRepository<Customer, Long> {}
@Autowired
private CustomerRepository repo;
repo.save(new Customer());
repo.findById(1L);
repo.deleteById(1L);
```

- Extended by `JpaRepository` (with JPQL, paging, batch ops).

***

### JpaRepository

- Full JPA support: CRUD, paging, sorting, batch operations.
- Inherits `CrudRepository` and `PagingAndSortingRepository`.

**Hierarchy**
```
Repository (marker)
   ↑
CrudRepository
   ↑
PagingAndSortingRepository
   ↑
JpaRepository
```

**Advanced Features**
- JPA methods: 
  - `flush`: Synchronizes persistence context with DB.
  - `saveAndFlush` : Saves and flushes immediately.
    - Flushing means that all pending changes in the persistence context (including the saved entity) are synchronized to the database right away.
    - This ensures that the changes are actually written in the database at the time of the method call, not deferred until the transaction commits.
  - `deleteInBatch` : Deletes entities in batch.
  - `getOne` : Lazy loads entity reference.
  - `deleteAllInBatch` : Batch delete all entities.

- Derived queries: `findByLastName`
- `@Query` for custom JPQL/native SQL.

**Example**
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByName(String name); // Derived query
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %?1%")
    List<Customer> searchByEmail(String email); // JPQL query
}
```

***

## 4. Query Methods

Spring Data supports custom repository queries via:

### 1. Derived Query Methods
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLastName(String lastName);
    User findByEmailAndStatus(String email, String status);
}
```
- Spring Data generates queries based on method names.
- You just follow naming conventions (findBy, readBy, countBy, existsBy, etc.).
- How it works: Spring parses method names and converts them into SQL/JPQL queries at runtime.
- Limitations: Works only for straightforward queries; complex joins/aggregations may not fit.

***

### 2. @Query Annotation
```java
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

@Query(value = "SELECT * FROM users WHERE status = ?1", nativeQuery = true)
List<User> findUsersByStatus(String status);
```
- Define JPQL or native SQL queries explicitly.
- Good for complex queries where derived methods don’t fit.
- Advantage: Precise control, supports named parameters.
- Use Case: When queries are too complex for derived methods.
***

### 3. Named Queries
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
- Predefine queries in the entity class using @NamedQuery. reusable in repositories.
- Best for: Reusable queries tied to entity lifecycle.
- Downside: Adds clutter to entity classes.

***

### 4. Custom Repository Implementations
For advanced queries or batch operations.
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
- If queries are too advanced (e.g., dynamic criteria, stored procedures), you can extend your repository with custom logic.
- Advantage: Full flexibility with EntityManager, native queries, Criteria API.
- Best for: Complex dynamic queries or integration with stored procedures.

***

### Modifying Queries (@Modifying, @Transactional)
By default, Spring Data JPA queries (@Query) are select-only, If you want to execute update, delete, or insert(Rarely used. Usually for UPDATE & DELETE. Inserts are done via save() or batch inserts.) queries, you need to tell Spring Data JPA that this query modifies data. That’s where @Modifying comes in.

```java
@Modifying
@Transactional
@Query("UPDATE Customer c SET c.status = :status WHERE c.email = :email")
int updateCustomerStatus(@Param("email") String email, @Param("status") String status);
```

**Why do we need @Transactional with @Modifying?**
- Modifying queries change the database state, so they must be executed inside a transaction.
- Without @Transactional, the changes won’t be committed to the database, rollback happens.
- If the class or method is already annotated with @Transactional, then @Modifying queries will run within that transaction.

**Note:** 
- By default, modifying queries clear the persistence context automatically (clearAutomatically = true) → ensures stale entities are not cached.
- Return type can be:
  - int → number of rows affected.
  - void → if you don’t need a result.
- @Modifying queries can not return entities.
- Difference between using @Modifying vs EntityManager.createQuery(...).executeUpdate()? 
  - @Modifying  Integrated with Spring’s transaction management, Spring manages entity manager flushing, clearing, and transactions behind the scenes.
  - @Modifying  Cleaner than writing boilerplate EntityManager code.

***

## 5. Pagination & Sorting Interfaces

### Sort
- **Purpose**: Defines sorting criteria for queries.
- **Usage**: Pass it to repository methods to sort results.
- Behind the scenes, this generates SQL with ORDER BY.
```java
Sort sort = Sort.by("firstName").ascending().and(Sort.by("age").descending());
repo.findByLastName("Sharma", sort);
```

### Pageable
- **Purpose**: Encapsulates both pagination (page number, size) and sorting.
- **Implementation**: PageRequest is the most common implementation.
```java
Pageable pageable = PageRequest.of(0, 5, Sort.by("lastName").descending());
Page<Customer> page = repo.findAll(pageable);

List<Customer> customers = page.getContent(); // actual data
long total = page.getTotalElements();         // total rows in DB
int pages = page.getTotalPages();             // total pages
```

Generated SQL:
```sql
SELECT * FROM customer 
ORDER BY last_name DESC 
LIMIT 5 OFFSET 0;

SELECT COUNT(*) FROM customer;
```

**Return Types**:
- `List<T>` : Just Data.
- `Page<T>`: Data + pagination metadata (total pages, total elements, etc.).
- `Slice<T>`:  Data + “is there next page?” (no count query).
- Similar to Page but doesn’t calculate total count (better for performance in large datasets). Additional COUNT(*) query is not executed to calculate total pages.

| Feature   | Sort             | Pageable                       |
|-----------|------------------|-------------------------------|
| Purpose   | Ordering         | Paginate + order              |
| SQL       | ORDER BY         | LIMIT + OFFSET + ORDER BY     |
| Metadata  | None             | Page info, total records      |
| Impl      | Sort.by(...)     | PageRequest.of(...)           |


```java
Page<T> findAll(Pageable pageable);
```
Returns a Page object containing results and pagination metadata.
- getContent() → The actual list of entities.
- getTotalElements() → Total rows in the table.
- getTotalPages() → Total number of pages.
- getNumber() → Current page number.
- getSize() → Page size.
- hasNext(), hasPrevious() → Navigation helpers.

### When to Use
- Paginated results for large datasets (e.g., showing 10 records per page in a UI).
- APIs that return pageable results (Page<T> can be easily mapped to JSON).
- Avoids loading entire table into memory.


Note: 
- If you request a page number greater than total pages, you get an empty list (getContent()), but metadata (getTotalPages()) is still correct.
- Page numbers are zero-based (first page is 0).

***

### Pagination-Friendly REST APIs

- Avoids returning huge datasets in one response.
- Improves performance & scalability.
- Client can navigate data in pages (page=0&size=10).
- Standard practice in microservices / REST APIs.

Controller example:
```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @GetMapping
    public ResponseEntity<Page<Customer>> getCustomers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "lastName") String sortBy,
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

Sample request:
```
GET /api/customers?page=1&size=5&sortBy=lastName&sortDir=asc
```

JSON response example:
```json
{
  "content": [{ "id": 6, "firstName": "Amit", "lastName": "Sharma" }],
  "pageable": { "pageNumber": 1, "pageSize": 5, "sort": { "sorted": true } },
  "totalPages": 10,
  "totalElements": 50,
  "last": false,
  "first": false,
  "numberOfElements": 5,
  "size": 5,
  "number": 1
}
```
#### Best Practices
- Always return metadata (total pages, elements, etc.).
- Use default values (page=0, size=10).
- Validate size (e.g., max 100 records per page).
- Consider DTOs instead of exposing entities directly.
- For very large datasets → use Keyset Pagination instead of OFFSET.
- Put a max limit on size param (e.g., 100) to secure pagination APIs from overloading (DoS attack).

***

## 6. Transaction Management

### How does JPA support transactions?
In Spring you typically use @Transactional which binds the JPA EntityManager to the Spring transaction through:
- **Resource-local / EntityTransaction:**  used in Java SE or when not using JTA, begin/commit/rollback via em.getTransaction().
- **JTA:** for XA or container-managed transactions across multiple resources (DB + JMS).

### Spring Integration
- Use `@Transactional` for declarative transaction management.
- Spring configures `JpaTransactionManager` or `JtaTransactionManager`.

- Place transactional boundaries at **Service Layer** (not Repository/DAO).

***

### @Transactional: Class vs Method Level

#### Class Level
```java
@Service
@Transactional(readOnly = true)
public class UserService {
    
    public User getUser(Long id) { ... }
    
    @Transactional(readOnly = false)
    public User createUser(User user) { ... }
}
```
- All public methods of that class are transactional by default.
- Each method inherits the class-level transaction settings unless overridden at the method level.

#### Method Level
```java
@Service
public class OrderService {
    
    @Transactional(readOnly = true)
    public Order getOrder(Long id) { ... }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createOrder(Order order) { ... }
}
```
- Per-method configuration for specific semantics.
- Applies only to that specific method.

#### Notes
- Only public methods are transactional by default.
- Method-level annotation overrides class-level.
- Self-invocation does not trigger transactions.
- Always consider propagation, isolation, and rollbackFor attributes if relevant.
- @Transactional does not work on private or protected methods - by default it works only on public methods (proxy-based AOP).
- Both class-level & method-level use the same proxy mechanism.
- @Transactional better placed at service layer, not repository layer because transactions should span business operations, not individual DB calls.
- Self-invocation of a transactional method, It bypasses the proxy, so the transaction is not applied. Solution: use AspectJ mode or call from another bean.

**How Proxies Work**

When you annotate a method/class with @Transactional, Spring does this under the hood:
- Instead of injecting your actual bean (say UserService), Spring injects a proxy bean.
- This proxy intercepts method calls and decides:
- Should it start a transaction?
- Should it commit/rollback afterward?
- The proxy delegates the actual business logic call to your target bean.

#### Best Practices
- Annotate service layer, not repository/DAO.
- Use class-level for defaults, override as needed.

### Propagation types (REQUIRED, REQUIRES_NEW, etc.)
Propagation defines how a transaction boundary behaves when a method is called inside an existing transaction.

**1. REQUIRED (Default)**
- If a transaction exists → join it.
- If none exists → start a new one.
```java
@Transactional(propagation = Propagation.REQUIRED)
public void placeOrder(Order order) {
    checkInventory(order);     // runs in same transaction
    processPayment(order);     // runs in same transaction
    saveOrder(order);          // runs in same transaction
}
```
Each called method will automatically join the active transaction initiated by `placeOrder`.

Use case: Most business methods. Ensures everything runs in one transaction.

```java
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final BookingRepository bookingRepository;
    
    @Override
    @Transactional  // propagation = REQUIRED (default)
    public void bookDoctorAppointment(AppointmentDetailsDto appointmentDetailsDto) {
        // Runs in SAME transaction as caller
        BookingInventory booking = bookingRepository.findByDoctorIdAndDateAndTime(
            appointmentDetailsDto.getDoctorId(),
            appointmentDetailsDto.getDate(),
            appointmentDetailsDto.getTime()
        );
        
        if (booking.getBookingStatus().equals(BookingStatus.AVAILABLE)) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPatientId(appointmentDetailsDto.getPatientId());
            bookingRepository.save(booking);
        }
    }
}
```

**Key Points:**
- All operations share one transaction
- One rollback affects everyone
- If something fails at the end, earlier saves are wasted


**2. REQUIRES_NEW**
- Always starts a new transaction, suspending the current one (if any).
- Commits/rolls back independently.
- Ensures operations in the method are transactionally isolated from the parent transaction, providing robust handling for scenarios where partial data persistence is required.ß
```java
@Transactional(propagation = Propagation.REQUIRED)
public void createOrder(Order order) {
    orderRepository.save(order); // main transaction
    logAuditTrail(order.getItem());
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logAuditTrail(OrderItem item) {
    // TX2 starts - INDEPENDENT AUDIT TRANSACTION
    // If this fails, it WON'T rollback the consultation save (TX1)
    auditLogService.logAction(
            AuditLog.builder()
                    .action("ORDER_CREATED")
                    .recordId(item.getId())
                    .timestamp(LocalDateTime.now())
                    .build()
    );
}

```
If `processProduct` throws an exception (e.g., insufficient stock), only the product update is rolled back. The main order can go on or handle the failure gracefully.

Use case: Logging, audit trails, notifications → should succeed even if main transaction fails.

**Diagram:**
```
createOrder() [TX1]
    ↓
    orderRepository.save() → TX1
    ↓
    logAuditTrail() → TX2 (new, independent)
        ↓ (Success/Failure isolated)
    ↓
    Return result
```


**3. SUPPORTS**
- If a transaction exists → join it.
- If none exists → run without a transaction, just like a regular method.
- 
```java
@Transactional(propagation = Propagation.SUPPORTS)
public List<Order> getOrders() {
    return orderRepository.findAll();
}
```
Suppose the controller calls `getOrders()` as part of business logic wrapped in a transaction, or calls it directly for display purposes (no transaction).
- Called from a transactional method (e.g., when placing an order): Joins transaction—potentially participating in locks or isolation, etc.
- Called standalone (e.g., simply displaying orders): Runs without transaction—no transaction overhead, just querying data.

This means the method’s behavior adapts to how it’s called:
- If invoked from within a transaction, it runs inside that transaction.
- Otherwise, it runs outside any transaction.

Use case: Read-only operations that may run inside or outside a transaction.

4. MANDATORY
- Must run inside an existing transaction.
- If none exists → throws TransactionRequiredException.
- This propagation does not create its own transaction; it only allows the method to run if a transaction is already active
```java
@Transactional(propagation = Propagation.REQUIRED)
public void placeOrder(Order order) {
    orderRepository.save(order);
    updateInventory(order);
}

@Transactional(propagation = Propagation.MANDATORY)
public void updateInventory(Order order) {
    inventoryRepo.decrementStock(order.getItemId());
}
```
If someone calls `updateInventory()` directly from a non-transactional context, Spring immediately throws an exception, ensuring inventory updates can’t occur without proper transaction management.

Use case: Internal methods that must always be called within a transaction.

**5. NOT_SUPPORTED**
- It executes without any transactional context.
- If there is an active transaction, Spring suspends (pauses) that transaction for the duration of the method. After the method completes, the suspended transaction is resumed.
- Basically, if a transaction exists, it is temporarily aborts the method execution, runs the method without a transaction, and then resumes the original transaction afterward.
```java
@Transactional
public void processOrder() {
    // Active transaction starts here
    System.out.println("Processing order...");
    generateReport();  // Runs with Propagation.NOT_SUPPORTED, suspends transaction
    saveOrder(); // take 10 seconds
    System.out.println("Order processed.");
    // After report generation, transaction resumes here
}
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void generateReport() {
    // runs outside any active transaction, suspends if exists
    reportService.generate(); // take 100 seconds
}

``` 
```text
|--- transaction starts ---|--- transaction suspended for report (100s) ---|--- transaction resumes + saveOrder (10s) ---|
```
Main Reasons and Benefits for Suspending a Transaction
1. Minimize Locking and Concurrency Issues
   - Long-running transactions hold database locks that can block other operations, leading to poor concurrency and potential deadlocks.
   - Suspending the transaction releases those locks temporarily while running non-transactional code, improving database scalability and responsiveness.
2. Avoid Holding Database Connections Too Long
   - Transactions typically hold onto database connections until committed or rolled back.
   - Suspending a transaction frees up the connection, allowing better utilization of the connection pool during lengthy non-transactional processing.
3. Isolate Non-Transactional Work
   - Certain tasks (e.g., reports, logging, notifications) do not need to participate in the transaction’s atomicity or rollback semantics.
   - Running these outside transactions isolates their execution, ensuring failures in non-critical areas don’t affect the main business transaction.
4. Clear Transaction Boundaries
   - Being explicit about suspension helps avoid unintentional transaction propagation to methods that shouldn’t be transactional.
   - It improves design clarity and resource management.

- Use case: Long-running, non-critical tasks (e.g., reports, analytics).

**6. NEVER - Never Run in Transaction**
- Must run without a transaction.
- If a transaction exists → throws exception.
- If there is an active transaction when the method is called, Spring throws an exception, preventing the method from executing within that transaction.

```java
@Service
@RequiredArgsConstructor
public class ReportService {
    private final PatientConsultationRepository consultationRepository;
    
    @Transactional
    public void generateMonthlyReport(YearMonth month) {
        // TX1 starts
        List<Report> reports = consultationRepository.findByMonth(month);
        exportReports(reports);  // Fails if tries to use TX
    }
    
    @Transactional(propagation = Propagation.NEVER)
    private void exportReports(List<Report> reports) {
        // MUST NOT have active transaction
        // If called from generateMonthlyReport() → throws exception
    }
}
```
**This is useful for:**
1. Writing to external systems (file, API)
2. Long-running operations
3. Processes that shouldn't rollback.
4. 
Use case: Operations that should never run in a transaction (e.g., schema checks).

**7. NESTED**
- If a transaction exists → create a nested transaction (via savepoints).
- If none exists → behaves like REQUIRED.
- A savepoint marks a spot in the current transaction you can roll back to, rather than rolling back the entire transaction

```java
@Transactional(propagation = Propagation.REQUIRED)
public void processBatch() {
    updateAccountA(); // if fails, whole TX rolls back
    processSubTask(); // NESTED: partial rollback possible
    updateAccountB(); // continues even if processSubTask rolled back
}

@Transactional(propagation = Propagation.NESTED)
public void processSubTask() {
    try {
        // risky operation, could fail
    } catch (Exception e) {
        // triggers rollback to SAVEPOINT, but processBatch can continue
        throw e;
    }
}

```
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
- REQUIRES_NEW suspends the parent transaction; NESTED uses savepoints within the same transaction.
2.	What happens if you call a @Transactional(REQUIRES_NEW) method from another @Transactional method?
- The parent transaction is suspended, and a completely new transaction begins.
3.	Why is SUPPORTS rarely used in production?
- Because behavior changes depending on caller context, which can cause unpredictable results.
4.	Can NESTED work with JPA/Hibernate?
- Not always. It requires JDBC savepoints. Hibernate supports it only with certain DBs/drivers.
5.	If you mark everything as REQUIRES_NEW, what’s the downside?
- Performance overhead (more commits), and you lose atomicity (everything doesn’t roll back together).


### Read-only vs modifying transactions
  When we use @Transactional, we can specify whether a transaction is read-only or not:
```java
@Transactional(readOnly = true)
public List<Order> getOrders() { ... }

@Transactional
public void saveOrder(Order order) { ... }
```
1. Read-Only Transactions
   - Definition: Marked with @Transactional(readOnly = true).
   - Purpose: Optimize performance for queries.

How it Works:
- Spring passes the readOnly = true hint to the transaction manager (Hibernate/JPA/JDBC).
- Hibernate/JPA may:
- Avoid dirty checking (skips detecting changes in entities).
- Flush mode is set to FlushMode.MANUAL → No auto-flush before queries.
- Database may:
- Some DBs can optimize query execution for read-only transactions.
- But many don’t enforce it strictly (it’s a hint, not a guarantee).

Use Case: Service methods that only fetch data, not modify it.

2. Modifying (Read-Write) Transactions
   - Definition: Default behavior of @Transactional.
   - Purpose: Allow insert, update, delete.

How it Works:
- Hibernate/JPA:
- Keeps entities in persistence context.
- Performs dirty checking at commit to flush changes.
- DB:
- Executes DML (INSERT/UPDATE/DELETE).
- Ensures ACID properties of transactions.

Use Case: Methods that modify state — e.g., saving an order, updating inventory.

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
- Hibernate may throw an exception or silently ignore changes (depends on provider & flush mode).
2.	Is readOnly = true always enforced at the database level?
- No. It’s mostly a hint for the ORM (Hibernate). Some databases ignore it.
3.	If read-only is faster, why not make everything read-only?
- Because then you can’t persist changes — updates won’t be flushed.
4.	How does Hibernate optimize performance in read-only transactions?
- By skipping dirty checking and preventing auto-flush before queries.
5.	Can you override read-only at runtime?
- Yes, but generally discouraged. You should clearly separate read and write service methods.


### Rollbacks and exception handling

1. Default Rollback Behavior
   - By default, Spring rolls back a transaction only on unchecked exceptions:
   - RuntimeException (e.g., NullPointerException, IllegalArgumentException)
   - Error (e.g., OutOfMemoryError)
   - Checked exceptions (e.g., IOException, SQLException) → transaction is not rolled back unless explicitly told.
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
   - If you catch an exception inside the method → Spring doesn’t see it → transaction won’t roll back.
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
- Don’t swallow exceptions inside transactional methods.
- Re-throw or let them propagate so Spring can handle rollback.

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
- This marks the current transaction as rollback-only.
- The transaction will be rolled back when the method completes, even though an exception was caught and not rethrown.
- This enables you to prevent committing a transaction while handling exceptions gracefully, without propagating errors.

5. Nested Rollbacks
   - If you use Propagation.REQUIRES_NEW, inner transactions can roll back independently of outer ones.
   - If you use Propagation.REQUIRED, rollback cascades to the entire transaction.

| Exception Type     | Default Behavior | Can Override?                          |
|--------------------|------------------|----------------------------------------|
| RuntimeException   | Rollback         | Yes                                    |
| Error              | Rollback         | Yes                                    |
| Checked Exception  | No Rollback      | Yes (with rollbackFor)                 |
| Caught Exception   | No Rollback      | Must rethrow or mark rollback manually |            



1.	Why does Spring only roll back on runtime exceptions by default?
- Because checked exceptions are often recoverable (business logic), while runtime exceptions indicate programming errors.
2.	How do you force rollback on checked exceptions?
- Use @Transactional(rollbackFor = Exception.class)
3.	What happens if you catch an exception inside a transactional method?
- No rollback unless you rethrow or call setRollbackOnly().
4.	What’s the difference between rollbackFor and noRollbackFor?
- rollbackFor enforces rollback, noRollbackFor prevents rollback.
5.	How does rollback work with nested transactions (REQUIRES_NEW)?
- Inner transaction rollback does not affect the outer one (independent).


***

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



### QueryDslPredicateExecutor

- QueryDslPredicateExecutor<T> is a Spring Data interface that enables type-safe, dynamic queries using QueryDSL predicates, instead of JPQL or method-name queries.

- Why Use It?
    - Avoids long method names like findByNameAndAgeGreaterThanAndStatus
    - Provides type safety at compile-time
    - Ideal for dynamic search filters
    - Less boilerplate compared to JPA Criteria API

- How It Works
1.	QueryDSL generates Q-classes (meta-models) at compile-time.
      - For entity Customer, you get QCustomer.
2.	Build Predicate objects using Q-classes.
3.	Execute queries with repository methods.

**Example**

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
- Advantages
    - Cleaner & safer than string-based JPQL
    - Supports dynamic query building
    - Works with pagination & sorting
    - Integrates with Spring Data easily

- Limitations
    - Requires QueryDSL annotation processor setup
    - Verbose compared to simple derived queries
    - Higher learning curve



**Follow-up Interview Questions**
1.	How does QueryDslPredicateExecutor differ from @Query?
    - @Query is static, QueryDSL is dynamic & type-safe.
2.	How are Q-classes generated?
    - By annotation processor (APT) during build.
3.	Can you use Pageable & Sort with QueryDSL?
    - Yes, findAll(Predicate, Pageable) supports pagination.
4.	QueryDSL vs Criteria API?
    - Both allow dynamic queries, but QueryDSL is fluent & easier to maintain.
5.	Does QueryDSL only work with JPA?
    - No, it supports SQL, MongoDB, Cassandra, Elasticsearch, etc.,
      but QueryDslPredicateExecutor is specific to Spring Data JPA.


### Spring Data for NoSQL 

Spring Data JPA vs Spring Data MongoDB, Cassandra, Elasticsearch, Redis, etc.
Spring Data provides a consistent programming model for different data stores, but the underlying implementation and use cases differ depending on the database type.

**Spring Data JPA (Relational Databases)**
   -	Works with relational databases (MySQL, PostgreSQL, Oracle, SQL Server, etc.) via JPA (Java Persistence API) and Hibernate.
   -	Entities are mapped to tables using @Entity, @Table, etc.
   -	Supports transactions, joins, complex queries, and relationships (@OneToMany, @ManyToOne).
   -	Queries can be JPQL, native SQL, or derived query methods.

Best for: Applications requiring ACID transactions, relational modeling, strong consistency.

#### Spring Data MongoDB (NoSQL – Document Store)**

**What is Spring Data MongoDB (high level)**
-	Spring Data MongoDB is the Spring project that makes it easy to work with MongoDB (a document-oriented NoSQL DB) from Spring apps.
-	Data is stored as BSON documents (binary JSON). Spring Data maps those documents to your Java POJOs annotated with @Document, @Field, etc.
-	Two main styles:
-	Repository abstraction (MongoRepository, ReactiveMongoRepository) for quick CRUD and derived queries.
-	Template API (MongoTemplate, ReactiveMongoTemplate) for imperative, lower-level control (queries, updates, aggregation, bulk ops).

**Core concepts & annotations (what to remember in interviews)**
-	@Document(collection = "name") — marks a class as a Mongo document.
-	@Id — document id (commonly stored as ObjectId unless you use String).
-	@Field("dbName") — map field name to a different DB key.
-	@Indexed, @CompoundIndex, @TextIndexed — define indexes.
-	@DBRef — database reference to another document (behaves like a pointer; use sparingly).
-	@Transient — not persisted.
-	@Version — optimistic locking / version field support.
-	Collection vs Document — a collection is like a table; a document is a row but hierarchical.

Entity + Repository examples

Entity:
```java
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed
    private String email;

    private String name;
    // constructors, getters/setters
}
```

Repository (derived query & custom):

```java
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByName(String name);
    // custom JSON query
    @org.springframework.data.mongodb.repository.Query("{ 'email' : ?0 }")
    User findByEmailAddress(String email);
}
```
Usage (simple):
```java
List<User> users = userRepository.findByName("Nikhil");
```


Using MongoTemplate (imperative ops, aggregation, updates)
-	MongoTemplate gives more control: Query, Update, FindAndModify, Aggregation, BulkOperations.

Example: update one field:
```java
Query q = Query.query(Criteria.where("email").is(email));
Update u = new Update().set("name", "New Name");
mongoTemplate.updateFirst(q, u, User.class);

Aggregation example (count by country):

Aggregation agg = Aggregation.newAggregation(
Aggregation.group("country").count().as("count"),
Aggregation.project("count").and("_id").as("country"),
Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"))
);
AggregationResults<Document> results = mongoTemplate.aggregate(agg, "users", Document.class);
```
Bulk writes:
```java
BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);
bulk.insert(List.of(user1, user2));
bulk.updateOne(Query.query(Criteria.where("id").is("...")),
new Update().set("name", "x"));
bulk.execute();
```

⸻

Aggregation framework
-	Powerful, pipeline-based: $match, $group, $project, $sort, $lookup, $unwind, $limit, etc.
-	Use Aggregation builder in Spring Data for type-safety.
-	$lookup provides a join-like capability (on server-side) — useful but can be slow for large collections.

⸻

Transactions
-	Supported (multi-document transactions) since MongoDB 4.0 for replica sets, 4.2+ for sharded clusters.
-	In Spring you use MongoTransactionManager and @Transactional.
-	Limitations: transactions add overhead; keep transactions short; require proper replica set / sharded cluster configuration.

Example config + usage:
```java
@Bean
MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
}

@Service
public class MyService {
    @Transactional
    public void transfer(...) {
        // multiple repository operations — will be in a single transaction
    }
}
```


Reactive support
-	ReactiveMongoRepository + ReactiveMongoTemplate using reactive driver and Reactor types (Mono, Flux).
-	Use for non-blocking, high-concurrency I/O-bound workloads.
-	Important: reactive code requires entire stack to be non-blocking (drivers, web stack, etc.).

Example:
```java
public interface ReactiveUserRepo extends ReactiveMongoRepository<User, String> { }
    Flux<User> users = reactiveUserRepo.findAll();
```

⸻

Indexes and performance
-	Index types: single-field, compound, multikey (arrays), hashed, text, geospatial (2d/2dsphere), TTL.
-	Use explain() to check query plans. Avoid collection scans on large datasets.
-	Design indexes based on query patterns (not on every field).
-	TTL indexes for automatic expiration (session caches, ephemeral data).
-	For write-heavy workloads, choose indexes sparingly — each index is additional write cost.

⸻

Schema design: embedding vs referencing (key interview topic)
-	Embedding (sub-documents inside parent):
-	Pros: fast reads, atomic updates to single document, no joins.
-	Cons: document growth, array size limits, harder to update duplicated data.
-	Use when child data is always accessed with parent and size bounded.
-	Referencing (store id(s) of other documents):
-	Pros: normalization, avoids duplication.
-	Cons: requires extra queries or $lookup (server-side join).
-	Use when child data is large or shared frequently across many parents.
-	Denormalization is common — duplicate selected fields for faster reads and fewer joins.

Important constraints:
-	Document size limit (16 MB) — avoid huge embedded arrays.
-	Consider update patterns (frequency, atomicity) when choosing embedding vs referencing.

⸻

Concurrency & consistency
-	MongoDB guarantees atomicity at the single-document level by default.
-	For multi-document atomicity, use transactions (costly).
-	Spring Data supports optimistic locking via @Version (recommended when concurrent updates to same document are likely).

⸻

Write concerns & read preferences
-	WriteConcern (ack level): ACKNOWLEDGED, W1, MAJORITY, UNACKNOWLEDGED, etc. Tune per correctness vs latency needs.
-	ReadPreference: primary, primaryPreferred, secondary, etc. Choose for load distribution vs staleness.

⸻

Useful extras
-	GridFS — store files larger than 16 MB (or as file-storage).
-	Change Streams — listen to real-time DB changes (CDC-style behavior) — great for event-driven systems.
-	Schema validation — MongoDB supports JSON Schema validations at collection level.
-	Monitoring & Ops — use mongotop, mongostat, Atlas monitoring, and server logs to diagnose perf.

⸻

Spring Boot config (application.yml)
```yaml
spring:
data:
mongodb:
uri: mongodb://user:pass@host:27017/dbname
database: dbname
# additional options: replicaSet, ssl, connectTimeoutMS etc.
```
Spring Boot autoconfigures MongoClient, MongoTemplate, and repositories if you include spring-boot-starter-data-mongodb.

⸻

Best practices & common pitfalls
-	Define indexes for hot query paths; measure with explain.
-	Avoid @DBRef for large/complex relationships — it triggers extra round trips.
-	Avoid very large embedded arrays; prefer referencing or bucketing.
-	Use projections to reduce network payload.
-	Use BulkOperations for high-throughput writes.
-	Prefer toCollection(LinkedList::new)? (irrelevant here) — focus on right collection shapes in app.
-	For performance-critical measurement use real data and load testing (not microbenchmarks).
-	Use optimistic locking (@Version) or transactions where concurrency matters.
-	Don’t assume schema-less means no schema — plan your data model and migrations.

⸻

Short, concrete code examples you can explain quickly

Derived query:
```java
List<User> findByEmailContainingIgnoreCase(String part);

@Query JSON:

@Query("{ 'age' : { $gt: ?0 } }")
List<User> findOlderThan(int age);

Aggregation (group by + sum):

Aggregation agg = Aggregation.newAggregation(
Aggregation.match(Criteria.where("status").is("PAID")),
Aggregation.group("customerId").sum("amount").as("totalPaid"),
Aggregation.project("totalPaid").and("customerId").previousOperation()
);
```
Optimistic locking:
```java
@Version
private Long version;
```

⸻

Quick comparison with JPA/Relational DB (common interview angle)
-	No tables/joins by default — documents are hierarchical.
-	Joins are possible (e.g., $lookup) but less idiomatic.
-	Better for rapidly changing, denormalized, or hierarchical data.
-	Transactions were historically limited — now available but heavier.
-	Schema-less → flexible but you still should enforce data integrity at app or DB validation level.

⸻

**Follow-up Interview Questions**

Basic / Conceptual
1.	What is the difference between a document and a collection?
2.	Where is @Document stored and how does Spring map it?
3.	Explain embedding vs referencing. When would you choose one over the other?
4.	What is an ObjectId and what are its components?
5.	How does MongoDB guarantee atomicity?

Repository & API
6. What is MongoRepository and how does it differ from MongoTemplate?
7. How do derived query methods work? How does Spring Data parse them?
8. When would you use @Query instead of derived methods?

Performance & Indexing
9. Explain different index types and when to use each.
10. How do you find a slow query and optimize it? (explain explain() output)
11. What are TTL indexes and how are they used?

Transactions & Concurrency
12. How are transactions supported in MongoDB and what are the requirements?
13. What is @Version and how does optimistic locking work in Spring Data MongoDB?

Advanced / Design
14. How would you design a “comments” feature that supports millions of comments per post? (embedding vs referencing vs bucketing)
15. Explain sharding and how you choose a shard key. What problems happen with a poor shard key?
16. How to implement pagination efficiently for large collections? (skip/limit vs range-based pagination)

Reactive & Ops
17. When to use reactive repositories? What are traps with reactive programming?
18. What are change streams and how would you use them?
19. How do WriteConcern and ReadPreference affect consistency and latency?

Practical / Code
20. Write a repository method to return top 10 users by total order value (hint: aggregation).
21. Show how to perform an atomic find-and-modify (increment a counter and return new document).
22. How to do bulk upsert for a large dataset?

Edge /gotcha
23. Why avoid @DBRef in high throughput systems?
24. What happens if a document grows beyond 16 MB? How to avoid it?
25. How does Spring handle mapping of Java LocalDateTime to MongoDB date?

⸻

### Spring Data Cassandra (NoSQL - Wide Column Store)
**What is Spring Data Cassandra (high level)**
-	Spring Data Cassandra is the Spring project that simplifies working with Apache Cassandra (a distributed wide-column NoSQL DB) from Spring applications.
-	Data is stored in tables with rows and dynamic columns. Spring Data maps tables to Java POJOs annotated with @Table, @PrimaryKey, @Column, etc.
-	Two main styles:
-	Repository abstraction (CassandraRepository, ReactiveCassandraRepository) for quick CRUD and derived queries.
  -	Template API (CassandraTemplate, ReactiveCassandraTemplate) for imperative, lower-level control (queries, updates, batch ops).
  - Reactive support with Project Reactor (Mono, Flux) for non-blocking I/O.

Best for: Highly available, horizontally scalable apps (e.g., Netflix, IoT platforms).

**Core concepts & annotations (what to remember in interviews)**
-	@Table(value = "name") — marks a class as a Cassandra table.
-	@PrimaryKey — composite primary key (partition key + clustering columns).
-	@PrimaryKeyColumn — define partition and clustering columns with ordering.
-	@Column("dbName") — map field name to a different DB column.
-	@Indexed — define secondary indexes (use sparingly).
-	@Transient — not persisted.
-	@Version — optimistic locking / lightweight transactions (LWT) support.
-	Cassandra uses CQL (Cassandra Query Language) similar to SQL but with differences (no joins, limited transactions).

### Spring Data Elasticsearch (Search Engine)
**What is Spring Data Elasticsearch (high level)**
-	Spring Data Elasticsearch is the Spring project that makes it easy to work with Elasticsearch (a distributed search and analytics engine) from Spring applications.
-	Data is stored as JSON documents in indexes. Spring Data maps those documents to Java POJOs annotated with @Document, @Field, etc.
-    Two main styles:
-	Repository abstraction (ElasticsearchRepository, ReactiveElasticsearchRepository) for quick CRUD and derived queries.
-	Template API (ElasticsearchRestTemplate, ReactiveElasticsearchTemplate) for imperative, lower-level control (queries, updates, bulk ops).
-	Reactive support with Project Reactor (Mono, Flux) for non-blocking I/O.

Best for: Full-text search, analytics, log aggregation (e.g., ELK stack).
**Core concepts & annotations (what to remember in interviews)**
-	@Document(indexName = "name") — marks a class as an Elasticsearch document.
-	@Id — document id.
-	@Field(type = FieldType.Text/Keyword/Date/Number, analyzer = "standard") — map field with type and indexing options.
-	@Transient — not persisted.
-	Elasticsearch uses REST API and JSON DSL for queries (no SQL).

### Spring Data Redis (Key-Value Store, In-Memory)
**What is Spring Data Redis (high level)**
-	Spring Data Redis is the Spring project that simplifies working with Redis (an in-memory key-value store) from Spring applications.
-	Data is stored as key-value pairs, hashes, lists, sets, sorted sets, etc. Spring Data provides abstractions to map Java objects to Redis data structures.
- Two main styles:
- Repository abstraction (RedisRepository) for simple CRUD on key-value pairs.
- Template API (RedisTemplate, ReactiveRedisTemplate) for imperative, lower-level control (operations on various Redis data types).
- Reactive support with Project Reactor (Mono, Flux) for non-blocking I/O.

Best for: Caching, session storage, real-time analytics, pub/sub messaging.

**Core concepts & annotations (what to remember in interviews)**
-	No specific annotations like JPA; use RedisTemplate to perform operations.
-	Supports various Redis data structures: String, Hash, List, Set, Sorted Set.
  -	Supports transactions (MULTI/EXEC) and Lua scripting.


Comparison Table

| Feature         | JPA (Relational)         | MongoDB (Document)         | Cassandra (Column)         | Elasticsearch (Search)         | Redis (Key-Value)         |
|-----------------|-------------------------|----------------------------|----------------------------|-------------------------------|---------------------------|
| Data Model      | Tables & Rows           | JSON Documents             | Column Families            | Indexed Documents             | Key-Value Pairs           |
| Schema          | Fixed (DDL)             | Schema-less                | Semi-Structured            | Schema-less                   | Schema-less               |
| Query Language  | JPQL / SQL              | Mongo Query API            | CQL (Cassandra Query Language) | Elasticsearch Query DSL   | Redis Commands            |
| Best Use Case   | Transactions, joins     | Flexible schema, large volumes | High write throughput, HA | Full-text search, analytics   | Caching, sessions         |
| Scalability     | Vertical + some horizontal | Horizontal               | Horizontal                 | Horizontal                    | Horizontal                |
| Consistency     | Strong (ACID)           | Eventual                   | Tunable (AP system)        | Eventual                      | Eventual (but fast)       |


⸻

## Specifications (Criteria Queries) in Spring Data JPA

**What is a Specification?**
   -	A Specification is a way to create dynamic queries in Spring Data JPA.
   -	Instead of writing custom repository methods for every filter (e.g., findByNameAndStatusAndDateBetween), you can build queries at runtime using Specifications.
   -	It is based on the JPA Criteria API, but much cleaner and reusable.



2. How to use?
Extend your repository with JpaSpecificationExecutor<T>.
```java
public interface EmployeeRepository
extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
}
```
Create Specification objects:
```java
public class EmployeeSpecifications {

    public static Specification<Employee> hasDepartment(String department) {
        return (root, query, cb) -> cb.equal(root.get("department"), department);
    }

    public static Specification<Employee> hasSalaryGreaterThan(Double salary) {
        return (root, query, cb) -> cb.greaterThan(root.get("salary"), salary);
    }
}
```
Use them in your service:
```java
List<Employee> results = employeeRepository.findAll(
    Specification.where(EmployeeSpecifications.hasDepartment("IT"))
    .and(EmployeeSpecifications.hasSalaryGreaterThan(50000.0))
);
```

⸻

**Creating Reusable Specifications**
   -	Specifications are composable (can chain with .and(), .or(), .not()).
   -	Example:
```java
Specification<Employee> spec = Specification
    .where(EmployeeSpecifications.hasDepartment("Finance"))
    .or(EmployeeSpecifications.hasSalaryGreaterThan(70000.0));
```
This makes them reusable and ideal when you have many optional search filters.

⸻

**When to use Specifications**
   -	When you have complex filtering/searching logic with many optional params.
   -	Example: A user search API with parameters:
   -	Name (optional)
   -	Age (optional)
   -	Department (optional)
   -	Salary range (optional)

Instead of writing 10+ methods in Repository, you can dynamically combine Specifications.

⸻

**Advantages**

- Clean and dynamic query building.
- Reusable and composable query conditions.
- Reduces repository method explosion.
- Works well with REST APIs that support filtering.

⸻

**Limitations**

- Can get verbose for very complex queries.
- Sometimes better handled with QueryDSL or custom queries.
- Performance issues if not optimized (too many OR conditions).

⸻

Example – Search API with Specifications
```java
public class EmployeeSpecifications {

    public static Specification<Employee> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<Employee> hasAge(Integer age) {
        return (root, query, cb) -> cb.equal(root.get("age"), age);
    }

    public static Specification<Employee> hasDepartment(String dept) {
        return (root, query, cb) -> cb.equal(root.get("department"), dept);
    }
}

public List<Employee> searchEmployees(String name, Integer age, String dept) {
Specification<Employee> spec = Specification.where(null);

    if (name != null) spec = spec.and(EmployeeSpecifications.hasName(name));
    if (age != null) spec = spec.and(EmployeeSpecifications.hasAge(age));
    if (dept != null) spec = spec.and(EmployeeSpecifications.hasDepartment(dept));

    return employeeRepository.findAll(spec);
}
```
Now you can call /search?name=John&dept=IT without writing extra repository methods.

⸻

🤔 **Follow-up Interview Questions**

Basics
1.	What is a Specification in Spring Data JPA?
2.	How is it different from writing a custom @Query method?
3.	Why do we extend JpaSpecificationExecutor in a repository?

Usage
4.	How do you combine multiple Specifications? (.and(), .or())
5.	Can you explain how Specifications are converted internally into Criteria API?
6.	How would you implement a search filter with 10 optional parameters?

Advanced
7.	How do you handle joins in Specifications (e.g., filtering based on related entity fields)?
8.	What are the performance implications of using Specifications?
9.	How do Specifications compare with QueryDSL or Criteria API?
10.	In what scenarios would you not recommend using Specifications?

⸻

Perfect 👍 Let’s dive into Spring Data JPA Auditing so you have a strong interview-ready explanation.

⸻

## Spring Data JPA Auditing

**What is Auditing?**
   -	Auditing means automatically tracking and maintaining metadata about your entities:
   -	When they were created
   -	When they were last modified
   -	Who created them
   -	Who last modified them

This helps in data integrity, traceability, and compliance without writing boilerplate code.

⸻

**How to Enable Auditing**
   -	Add @EnableJpaAuditing in your Spring Boot application class:
```java
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class MyApp {}
```
The auditorAwareRef points to a bean that provides the current user.

⸻

**Auditing Annotations**
   -	@CreatedDate → Automatically sets the entity’s creation timestamp.
   -	@LastModifiedDate → Automatically updates when the entity is modified.
   -	@CreatedBy → Automatically sets the user who created the entity.
   -	@LastModifiedBy → Automatically updates the user who modified it.

⸻

Example Entity with Auditing
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    // getters, setters
}
```
⸻

5. AuditorAware Implementation
   -	To fill @CreatedBy and @LastModifiedBy, Spring needs to know the current user.
   -	We implement AuditorAware<T>:
```java
@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
        public Optional<String> getCurrentAuditor() {
        // Example with Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }
        return Optional.of(authentication.getName());
    }
}
```

⸻

6. Auditing with Spring Security
If your app uses Spring Security, you can fetch the logged-in user from the SecurityContextHolder.

Example:
  -	@CreatedBy = "john.doe"
  -	@LastModifiedBy = "jane.smith"

This ensures changes are traceable to actual users.

⸻

7. Advantages

- No manual setting of created/modified timestamps.
- Automatic and consistent metadata.
- Works well with Spring Security for user context.
- Helps in debugging, compliance (e.g., banking, healthcare).

⸻

8. Limitations

- Needs AuditingEntityListener on each entity.
- Requires correct AuditorAware setup (otherwise createdBy remains null).
- Doesn’t track field-level changes (use Envers for versioning).

⸻

**Follow-up Interview Questions**

Basics
1.	What is JPA auditing, and why do we need it?
2.	How do @CreatedDate and @LastModifiedDate work internally?
3.	What is the role of @EnableJpaAuditing?

Advanced Usage
4.	How do you populate @CreatedBy and @LastModifiedBy?
5.	How does Spring fetch the current user in auditing?
6.	Can auditing work without Spring Security? (yes, you can return a default user/system name).
7.	What happens if an entity is updated without loading it from DB (e.g., via native query)? Will auditing fields update?

Deep Dive
8.	How would you handle auditing across multiple services in a microservices architecture?
9.	How does auditing compare to Hibernate Envers (which keeps version history)?
10.	Can you customize the date format or auditor type (e.g., UUID instead of username)?

⸻

Great topic 👌 — Projections and DTO Mapping in Spring Data JPA are often asked in interviews because they directly touch performance, scalability, and best practices. Let’s break it down in detail.

⸻

## Projections and DTO Mapping in Spring Data JPA

**Why Projections?**
   -	By default, Spring Data JPA fetches entire entities.
   -	This often causes overfetching (fetching more data than needed).
   -	Projections solve this by selecting only the required fields, improving performance and memory efficiency.

⸻

**Types of Projections**

a) Interface-based Projections
-	Create an interface with getter methods matching entity fields (or aliases in query).
-	Spring will map query results automatically.

Entity:
```java
@Entity
public class Employee {
    @Id
    private Long id;
    private String name;
    private String department;
    private Double salary;
}
```
Projection Interface:
```java
public interface EmployeeNameDept {
    String getName();
    String getDepartment();
}
```
Repository:
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
List<EmployeeNameDept> findByDepartment(String dept);
}
```
Returns only name and department without fetching salary.

⸻

b) Class-based DTO Projections
-	Use a DTO class with a constructor.
-	Explicitly tell Spring Data JPA how to map results.
-	Useful for custom transformation logic.

DTO:
```java
public class EmployeeDTO {
private final String name;
private final Double salary;

    public EmployeeDTO(String name, Double salary) {
        this.name = name;
        this.salary = salary;
    }
}
```
Repository with JPQL:
```java
@Query("SELECT new com.example.EmployeeDTO(e.name, e.salary) FROM Employee e WHERE e.department = :dept")
List<EmployeeDTO> findEmployeeDTOByDepartment(@Param("dept") String dept);
```

⸻

c) JPQL vs Native SQL Projections
-	JPQL Projections
-	Type-safe.
-	Works with DTO constructors (new DTO(...)).
-	Example:
```java
@Query("SELECT new com.example.EmployeeDTO(e.name, e.salary) FROM Employee e")
List<EmployeeDTO> getEmployees();
```

-	Native SQL Projections
-	Used when JPQL is not enough (complex queries, DB-specific functions).
-	Must map results manually or with interface projections.

Example (Interface-based Native SQL):
```java
@Query(value = "SELECT e.name as name, e.salary as salary FROM employees e WHERE e.department = :dept",
nativeQuery = true)
List<EmployeeNameSalary> findEmployeeDetails(@Param("dept") String dept);
```
Where:
```java
public interface EmployeeNameSalary {
    String getName();
    Double getSalary();
}
```

⸻

3. Reducing Overfetching
Problem:
-   If an entity has 15 fields, but you only need 2 → fetching the full entity wastes memory & increases query cost.
Solution:
-   Use projections to fetch only required fields.
Example:
   -	findById(Long id) → fetches entire Employee entity.
   -	findNameAndDepartmentById(Long id) → fetches only 2 columns.

⸻

4. When to Use What?
-	Interface Projections: Quick, easy, less boilerplate.
-	Class-based DTO Projections: When you need transformations, computed fields, or immutability.
-	JPQL Projections: Standard use case (portable across DBs).
-	Native SQL Projections: For complex queries or DB-specific optimizations.

⸻

**Follow-up Interview Questions**

Basics
1.	What is a projection in Spring Data JPA, and why do we need it?
2.	How do interface-based projections work under the hood?
3.	What’s the difference between findAll() returning entities vs projections?

Usage
4.	How do you implement class-based DTO mapping in Spring Data JPA?
5.	What happens if your projection field names don’t match entity/alias names?
6.	Can you combine projections with pagination (Pageable)?

Advanced
7.	What are the trade-offs between JPQL projections and Native SQL projections?
8.	How do projections help in reducing N+1 select problems?
9.	Can you project fields from related entities (JOINs)? Example: Employee’s department name.
10.	How do projections interact with lazy-loaded associations?

⸻

Great one 🚀 — Custom Repository Implementations is a very common interview area in Spring Data JPA. It checks if you know what to do when built-in repository methods and derived queries aren’t enough. Let’s go step by step.

⸻

## Custom Repository Implementations in Spring Data JPA

⸻

**What is a Custom Repository Implementation?**
   -	Spring Data JPA gives you derived query methods (findByName, findByEmailAndStatus, etc.) and @Query (JPQL/Native).
   -	But sometimes, you need custom logic that:
   -	Is too complex for derived queries or JPQL.
   -	Requires multiple joins or native SQL.
   -	Needs business logic before/after database interaction.

In such cases, you create a custom repository implementation.

⸻

**How to Create a Custom Repository**

Step 1: Define a Custom Repository Interface
```java
public interface EmployeeRepositoryCustom {
    List<Employee> findEmployeesWithHighSalaryAndDepartment(String dept, double minSalary);
}
```

Step 2: Implement the Custom Repository
-	Naming convention: Impl must match the base repository name (EmployeeRepository + Impl).
```java
@Repository
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Employee> findEmployeesWithHighSalaryAndDepartment(String dept, double minSalary) {
        String jpql = "SELECT e FROM Employee e WHERE e.department = :dept AND e.salary > :minSalary";
        return entityManager.createQuery(jpql, Employee.class)
                            .setParameter("dept", dept)
                            .setParameter("minSalary", minSalary)
                            .getResultList();
    }
}
```
Step 3: Extend the Custom Interface in Your Repository
```java
public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustom {
}
```
Now you can call:
```java
List<Employee> list = employeeRepository.findEmployeesWithHighSalaryAndDepartment("IT", 70000.0);
```

⸻

**When to Use Custom Repositories**

- When queries are too complex for Spring Data method names.
- When you need native SQL or advanced Criteria API.
- When you want to mix business rules with persistence logic.
- When queries involve multiple joins or subqueries.
- When you need dynamic queries (alternative to Specifications/QueryDSL).

⸻

Example – Dynamic Query with Criteria API

Instead of hardcoding JPQL:
```java
@Override
public List<Employee> searchEmployees(String dept, Double minSalary, String nameLike) {
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
Root<Employee> root = query.from(Employee.class);

    List<Predicate> predicates = new ArrayList<>();

    if (dept != null) {
        predicates.add(cb.equal(root.get("department"), dept));
    }
    if (minSalary != null) {
        predicates.add(cb.greaterThan(root.get("salary"), minSalary));
    }
    if (nameLike != null) {
        predicates.add(cb.like(root.get("name"), "%" + nameLike + "%"));
    }

    query.where(cb.and(predicates.toArray(new Predicate[0])));
    return entityManager.createQuery(query).getResultList();
}
```

⸻

**Advantages**
- Full control over queries (JPQL, Criteria API, Native SQL).
- Can add non-query logic inside repository methods.
- Works seamlessly with Spring Data repository model.

⸻

**Disadvantages**
- More boilerplate compared to simple derived queries.
- You lose some of the auto-generated query features.
- Easy to misuse (mixing too much business logic inside repositories).

⸻

**Follow-up Interview Questions**

Basics
1.	Why would you use a custom repository instead of derived queries?
2.	How do you name the custom implementation class so that Spring picks it up?
3.	What’s the role of EntityManager in custom repositories?

Usage
4.	How do you implement a custom repository method that runs native SQL?
5.	Can you use Criteria API or Specifications inside a custom repository?
6.	How do you combine custom repository methods with default Spring Data methods?

Advanced
7.	What are the alternatives to custom repositories (QueryDSL, Specifications)?
8.	How do you handle pagination in custom repository methods?
9.	Can you use Spring Transactions inside custom repositories?
10.	What are best practices for deciding whether to put logic in repository or service layer?

⸻

## Advanced Spring Data JPA Concepts

⸻

**Entity Lifecycle Callbacks**
-	JPA provides entity lifecycle events so you can hook into persistence operations.
-	An entity can define callback methods annotated with lifecycle annotations.

**Common Annotations**
-	@PrePersist → before entity is inserted.
-	@PostPersist → after entity is inserted.
-	@PreUpdate → before entity update.
-	@PostUpdate → after entity update.
-	@PreRemove → before entity removal.
-	@PostRemove → after entity removal.
-	@PostLoad → after entity is loaded from DB.

Example
```java
@Entity
public class Employee {
@Id
@GeneratedValue
private Long id;

    private String name;
    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        createdAt = LocalDateTime.now();
        System.out.println("Before persisting Employee");
    }

    @PostUpdate
    public void onPostUpdate() {
        System.out.println("After updating Employee");
    }
}
```
Use case: automatically set timestamps, log changes, enforce constraints.

⸻

**Soft Deletes (Logical Deletes)**
   -	Instead of physically deleting records (DELETE FROM table), we mark them as deleted with a flag.
   -	Prevents data loss and helps with auditing/compliance.

Example
```java
@Entity
@SQLDelete(sql = "UPDATE employee SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {
@Id
@GeneratedValue
private Long id;

    private String name;

    private boolean deleted = false;
}
```
-	@SQLDelete → overrides delete operation with an UPDATE.
-	@Where → ensures only non-deleted records are fetched.

Now calling employeeRepository.delete(employee) won’t remove the row, just set deleted=true.

⸻

**Hibernate Envers (Auditing History) (optional but valuable)**
-	Envers provides entity versioning → tracks every insert/update/delete.
-	Stores history in separate audit tables.

Setup
-	Add dependency:
```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-envers</artifactId>
</dependency>
```


-	Annotate entity:

```java
@Entity
@Audited
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Double salary;
}

```

-	Hibernate creates an audit table (employee_AUD).
-	You can query historical versions using AuditReader.

Useful in banking, healthcare, finance where audit trails are mandatory.

⸻

### @Cacheable
**Caching Frequently Accessed Data with @Cacheable**
-	Caching avoids hitting the DB repeatedly for frequently accessed queries.
-	Spring provides caching abstraction (@EnableCaching, @Cacheable, @CacheEvict).

Example
```java
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    @Cacheable("employees")
    public Employee getEmployeeById(Long id) {
        System.out.println("Fetching from DB...");
        return repository.findById(id).orElse(null);
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void clearCache() {
        System.out.println("Cache cleared!");
    }
}
```
-	First call → queries DB, stores in cache.
-	Subsequent calls with same ID → served from cache.
-	Cache providers: EhCache, Hazelcast, Redis, Caffeine.

Improves performance and reduces DB load.

⸻

**Follow-up Interview Questions**

Entity Lifecycle Callbacks
1.	What’s the difference between @PrePersist and @PostPersist?
2.	Can lifecycle callbacks be placed in a separate listener class?
3.	How would you use @PostLoad in real projects?

Soft Deletes
4.	How do you implement soft deletes in JPA?
5.	What are the advantages/disadvantages of soft deletes?
6.	How would you handle unique constraints with soft deletes (e.g., deleted record still has unique email)?

Envers
7.	What is Hibernate Envers, and how does it differ from JPA auditing?
8.	How does Envers store versions internally?
9.	How do you query for a past version of an entity?

Caching
10.	What is the difference between first-level cache, second-level cache, and Spring’s @Cacheable?
11.	How does Spring manage cache invalidation (@CacheEvict)?
12.	How would you decide which queries to cache in a microservices system?



⸻

## Best Practices in JPA/Hibernate

1. Avoid N+1 Queries
   -	Problem: When fetching a list of entities, accessing lazy-loaded associations inside a loop triggers one query per record.
   -	Solution:
   -	Use JOIN FETCH in JPQL/HQL.
   -	Use @EntityGraph to eagerly fetch required associations.
```java
@EntityGraph(attributePaths = {"orders", "address"})
List<Customer> findAll();
```



⸻

2. Use DTOs for Responses, Not Entities Directly
   -	Prevents exposing internal entity structure to the API layer.
   -	Helps fetch only required fields instead of entire entity graphs.
   -	Improves performance by reducing data transferred.
```java
@Query("SELECT new com.example.dto.CustomerDTO(c.id, c.name) FROM Customer c")
List<CustomerDTO> findAllCustomerNames();
```

⸻

3. Use Pagination to Avoid Loading Large Result Sets
   -	Avoid fetching thousands of rows into memory.
   -	Use Pageable in Spring Data JPA.
```java
Page<Customer> findAll(Pageable pageable);
```
-	Database executes efficient LIMIT and OFFSET queries.

⸻

4. Keep Queries Readable and Maintainable
   -	Use named queries or query methods for complex queries.
   -	Favor JPQL over native SQL unless absolutely necessary.
   -	Use Criteria API if query needs to be dynamic and type-safe.
```java
@NamedQuery(
name = "Customer.findByName",
query = "SELECT c FROM Customer c WHERE c.name = :name"
)
```

⸻

## Common Pitfalls

1. LazyInitializationException
   -	Cause: Accessing a lazy-loaded collection outside of a transactional context.
   -	Fix:
   -	Use JOIN FETCH or @EntityGraph.
   -	Keep transactions open where data is needed (e.g., in service layer, not controller).

⸻

2. Incorrect Cascade Usage
   -	CascadeType.ALL blindly applied can delete/update unintended entities.
   -	Fix: Use cascade selectively (PERSIST, MERGE, REMOVE) only when logically required.

⸻

3. Misuse of FetchType.EAGER
   -	Problem: EAGER fetching loads unnecessary data, hurting performance.
   -	Fix: Default to LAZY and fetch explicitly only when needed.

⸻

4. Overusing Native Queries
   -	Bypasses JPA optimizations and portability.
   -	Fix: Use JPQL/Criteria unless specific DB features are needed.

⸻

5. Not Handling Transactions Properly
   -	Database operations without @Transactional may cause partial updates or inconsistent states.
   -	Fix: Always define transaction boundaries in the service layer, not the repository or controller.


⸻

## Optimistic vs Pessimistic Locking in JPA

⸻

1. Optimistic Locking

Concept
-	Assumes that conflicts are rare.
-	Multiple transactions can read the same entity simultaneously.
-	Before committing, JPA checks if the entity was modified by someone else (using a @Version field).
-	If yes → throws OptimisticLockException.

⸻

How to Enable

Add a @Version field in your entity:
```java
@Entity
public class Account {
@Id
private Long id;

    private double balance;

    @Version
    private Long version; // Auto-incremented on every update
}
```
⸻

Example Flow
1.	Transaction A reads Account(id=1, balance=100, version=1).
2.	Transaction B reads the same row.
3.	Transaction A updates balance → new version = 2.
4.	Transaction B tries to update balance, but JPA sees version mismatch (expected 1, found 2) → OptimisticLockException.

⸻

When to Use
-	High read and low write workloads.
-	Situations where conflicts are rare but possible.
-	Example: Banking portal where reads are frequent, writes are occasional.

⸻

⸻

2. Pessimistic Locking

Concept
-	Assumes conflicts are likely.
-	Locks the entity in the database at the time of read so that no other transaction can modify it until the lock is released.
-	Uses DB-level row locks.

⸻

How to Enable

Use @Lock with JPA repository or entity manager.

Example with Repository:
```java
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Account findAccountForUpdate(@Param("id") Long id);
}
```

Example with EntityManager:
```java
Account account = em.find(Account.class, 1L, LockModeType.PESSIMISTIC_WRITE);
```

⸻

Lock Types
-	PESSIMISTIC_READ: Shared lock → prevents updates but allows reads.
-	PESSIMISTIC_WRITE: Exclusive lock → prevents both reads and writes by others.
-	PESSIMISTIC_FORCE_INCREMENT: Like WRITE, but also increments version.

⸻

When to Use
-	High write contention scenarios.
-	Example: Stock trading, inventory management (only one user should decrement stock at a time).

⸻

⸻

Comparison

| Aspect                | Optimistic Locking         | Pessimistic Locking         |
|-----------------------|---------------------------|-----------------------------|
| Assumption            | Conflicts are rare        | Conflicts are frequent      |
| Locking               | No DB lock, uses @Version | DB-level row lock           |
| Performance           | Higher concurrency        | Slower, reduces concurrency |
| Failure Mode          | OptimisticLockException   | Other transactions blocked  |
| Best for              | Read-heavy apps           | Write-heavy apps            |



⸻

⚡️ Example Interview Follow-Up Questions
1.	What happens if two transactions update the same row in Optimistic Locking?
2.	How do you resolve OptimisticLockException in production? (→ Retry mechanism)
3.	Difference between LockModeType.OPTIMISTIC and LockModeType.OPTIMISTIC_FORCE_INCREMENT?
4.	What are the trade-offs between optimistic and pessimistic locking in a high-traffic system?
5.	Can you combine optimistic locking with retries?
6.	How do database deadlocks occur in pessimistic locking?


⸻

Example: Bank Account Transfer

We’ll simulate two concurrent transactions trying to update the same Account entity.

⸻

1. Entity Setup with Optimistic Locking
```java
import jakarta.persistence.*;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double balance;

    @Version // Enables optimistic locking
    private Long version;

    // getters and setters
}
```

⸻

2. Optimistic Locking Simulation
```java
@Transactional
public void optimisticTransfer(Long accountId, double amount) {
// Tx A reads account
Account account = em.find(Account.class, accountId);

    // Simulate some delay
    sleep(2000);

    // Update balance
    account.setBalance(account.getBalance() - amount);

    // On commit → JPA checks @Version
    // If another Tx modified this entity in between, throws OptimisticLockException
}
```
What happens?
-	Tx A reads account (balance=100, version=1).
-	Tx B also reads (balance=100, version=1).
-	Tx A updates → balance=90, version=2.
-	Tx B tries to commit → sees version mismatch → OptimisticLockException.

Solution: Wrap in a retry loop.

⸻

3. Pessimistic Locking Simulation
```java
@Transactional
public void pessimisticTransfer(Long accountId, double amount) {
// Locks the row at DB level
Account account = em.find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);

    // Simulate some delay
    sleep(2000);

    // Update balance
    account.setBalance(account.getBalance() - amount);

    // On commit → no conflict (others were blocked until lock released)
}
```
What happens?
-	Tx A acquires DB lock on row.
-	Tx B tries to read with PESSIMISTIC_WRITE → must wait until Tx A commits/rolls back.
-	Ensures no conflict but reduces concurrency (possible deadlocks).

⸻

4. Retry Mechanism for Optimistic Locking
```java
public void transferWithRetry(Long accountId, double amount) {
    int retries = 3;
    while (retries > 0) {
        try {
            optimisticTransfer(accountId, amount);
            break; // success
        } catch (OptimisticLockException e) {
            retries--;
            if (retries == 0) throw e;
            System.out.println("Retrying transaction...");
        }
    }
}
```

⸻

 Key Talking Points for Interview
-	Optimistic locking → fast, but must handle OptimisticLockException.
-	Pessimistic locking → safe, but can cause blocking and deadlocks.
-	Real-world → Use optimistic by default, pessimistic only when contention is high.

⸻

**Optimistic Locking Timeline**

| Time      | Tx A                        | Tx B                        |
|-----------|-----------------------------|-----------------------------|
| t0        | Reads Account (v=1, bal=100)| Reads Account (v=1, bal=100)|
| t1        | (processing...)             | (processing...)             |
| t2        | Updates balance to 90       |                             |
| t3        | Commits (version=2)         |                             |
| t4        |                             | Updates balance to 80       |
| t5        |                             | Commit fails (version=1, but DB has v=2) → **OptimisticLockException** |

---

**Pessimistic Locking Timeline**

| Time      | Tx A                        | Tx B                        |
|-----------|-----------------------------|-----------------------------|
| t0        | Locks Account (PESSIMISTIC) | Tries to lock, waits        |
| t1        | Reads Account (bal=100)     | (waiting)                   |
| t2        | Updates balance to 90       | (waiting)                   |
| t3        | Commits, releases lock      |                             |
| t4        |                             | Acquires lock, reads bal=90 |
| t5        |                             | Updates balance, commits    |