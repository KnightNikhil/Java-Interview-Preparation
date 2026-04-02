# Database Concepts — Complete Developer Reference

Table of contents
1. [Overview](#overview)
2. [Relational Database Basics](#relational-database-basics)  
3. [SQL Language Proficiency](#sql-language-proficiency)  
4. [Indexing](#indexing)  
5. [Transactions & Isolation Levels (with Java / Spring)](#transactions--isolation-levels)  
6. [Database Normalization & Denormalization](#database-normalization--denormalization)  
7. [JPA & ORM Integration](#jpa--orm-integration)  
8. [Spring Data JPA](#spring-data-jpa)  
9. [Connection Pooling](#connection-pooling)  
10. [Database Performance Tuning](#database-performance-tuning)  
11. [NoSQL Overview (when to use)](#nosql-overview)  
12. [Schema Migration Tools](#schema-migration-tools)  
13. [Testing with Databases](#testing-with-databases)  
14. [Database in Production](#database-in-production)  
15. [Appendices: Common Interview/Debugging Tips and FAQs](#appendices)

---

## Overview

This document consolidates essential database knowledge for developers — from beginners to senior engineers. It covers theory, practical examples, Java/Spring integration patterns, best practices, and common pitfalls. Use it as a reference for development, code reviews, and interview prep.

---

## Relational Database Basics

What is a relational database?
- Stores data in tables (relations): rows = records, columns = attributes.
- Schema defines structure and constraints.
- Relationships via keys (primary/foreign).
- SQL is the standard query language.

Core concepts:
- Table, Row, Column
- Primary Key (PK): uniqueness and non-null.
- Foreign Key (FK): referential integrity to another table's PK.
- Indexes: accelerate lookups.
- Constraints: UNIQUE, NOT NULL, CHECK, FOREIGN KEY.

Example schema
```sql
CREATE TABLE customer (
  customer_id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE
);

CREATE TABLE orders (
  order_id SERIAL PRIMARY KEY,
  customer_id INT NOT NULL REFERENCES customer(customer_id),
  amount DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT now()
);
```

Best practices:
- Choose meaningful primary keys (synthetic vs natural).
- Use FK constraints to enforce relationships unless you intentionally opt out for performance or distribution.
- Normalize until the point of diminishing returns.

Pitfalls:
- Over-normalization can harm read performance.
- Missing FK causes data integrity issues.

---

## SQL Language Proficiency

SQL categories:
- DDL (Data Definition Language): CREATE, ALTER, DROP.
- DML (Data Manipulation Language): SELECT, INSERT, UPDATE, DELETE.
- DCL (Data Control Language): GRANT, REVOKE.
- TCL (Transaction Control): COMMIT, ROLLBACK, SAVEPOINT.

Examples
- DDL
```sql
CREATE TABLE account (
  account_id INT PRIMARY KEY,
  customer_name VARCHAR(50),
  balance DECIMAL(10,2)
);
```

- DML
```sql
INSERT INTO account (account_id, customer_name, balance) VALUES (101, 'Nikhil', 50000);

SELECT customer_name, balance FROM account WHERE balance > 10000;

UPDATE account SET balance = balance + 1000 WHERE account_id = 101;

DELETE FROM account WHERE account_id = 101;
```

- DCL
```sql
GRANT SELECT, INSERT ON account TO app_user;
REVOKE INSERT ON account FROM app_user;
```

- TCL
```sql
BEGIN;
UPDATE account SET balance = balance - 100 WHERE account_id = 1;
UPDATE account SET balance = balance + 100 WHERE account_id = 2;
COMMIT;
```

Joins (quick reference)
- INNER JOIN: matching rows only.
- LEFT JOIN: all left rows, matching right rows or NULL.
- RIGHT JOIN: all right rows, matching left rows or NULL.
- FULL OUTER JOIN: all rows from both sides.
- SELF JOIN: joining a table to itself.
- CROSS JOIN: Cartesian product — use with caution.

Examples
```sql
SELECT c.name, o.order_id, o.amount
FROM customer c
JOIN orders o ON c.customer_id = o.customer_id;

-- Find customers without orders
SELECT c.name
FROM customer c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE o.order_id IS NULL;
```

Subqueries, IN vs EXISTS:
- Use EXISTS for presence checks — often more efficient on large datasets.
- IN is fine for small sets or when values are known.

Aggregation:
- GROUP BY to aggregate, HAVING to filter groups, ORDER BY to sort.

Views, Stored Procedures, Triggers:
- Views: virtual, useful for encapsulation and security.
- Stored procedures: encapsulate logic in DB for performance or consistency.
- Triggers: reactive logic on changes — use sparingly (can hide behavior).

Pitfalls:
- Putting business logic solely in triggers/stored procedures can make app behavior opaque.
- Use indexes to support JOINs and WHERE clauses.

---

## Indexing

Purpose: Speed up data retrieval by reducing the number of disk pages scanned.

What an index stores:
- Indexed column value(s) plus a pointer to the row.

Types:
- B-Tree index: general purpose; supports range, ORDER BY, equality.
- Hash index: equality only (fast), no range queries.
- Composite (multi-column) index: follow left-most prefix rule.
- Unique index: enforces uniqueness.
- Clustered vs Non-clustered:
    - Clustered: table data stored in index order (one per table).
    - Non-clustered: index points to data rows.

Example
```sql
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_orders_customer_date ON orders(customer_id, created_at);
```

Covering index:
- Index contains all columns needed by the query — no table lookup.

When indexing helps:
- Frequent SELECTs, JOIN columns, WHERE filters, ORDER BY / GROUP BY, FK columns.

When indexing hurts:
- High write volume (INSERT/UPDATE/DELETE) — indexes must be maintained.
- Low cardinality columns (gender, boolean) — index not selective.
- Small tables — full table scan is cheap.

Best practices:
- Index primary & foreign keys.
- Use composite indexes according to query patterns; put most selective column first.
- Analyze execution plans (EXPLAIN) to confirm index usage.

Pitfalls:
- Indexing every column increases write cost and storage.
- Ignoring index selectivity and order of columns in composite indexes.

---

## Transactions & Isolation Levels

Transaction = an atomic unit of work. ACID properties:
- Atomicity, Consistency, Isolation, Durability.

Common transaction problems:
- Dirty read, Non-repeatable read, Phantom reads.

Isolation levels (standard):
- READ_UNCOMMITTED: allows dirty reads.
- READ_COMMITTED: prevents dirty reads; allows non-repeatable & phantom.
- REPEATABLE_READ: prevents dirty & non-repeatable reads; may allow phantom depending on DB.
- SERIALIZABLE: highest isolation — prevents phantoms; lowest concurrency.

Java / Spring integration (declarative)
- @Transactional annotation manages transactions declaratively.

Example
```java
// language: java
@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    debit(fromId, amount);
    credit(toId, amount);
}
```

Key points about @Transactional:
- Default propagation: REQUIRED.
- Default rollback: RuntimeException and Error (checked exceptions do not trigger rollback unless configured).
- self-invocation bypasses proxy, so transaction not applied if calling another method in same class directly.
- Private methods are not proxied and will not run in transaction via Spring AOP.

Best practices:
- Keep transactional methods coarse-grained (service layer), not on each DAO call.
- Mark read-only transactions when appropriate: @Transactional(readOnly = true).
- Use rollbackFor to handle checked exceptions that should trigger rollback.

Pitfalls:
- Using SERIALIZABLE everywhere causes contention.
- Relying on default rollback behavior without tests.
- Long-running transactions block resources.

Distributed transactions:
- Two-Phase Commit (2PC) ensures atomic commit across resources but has scaling and availability issues.
- Prefer eventual consistency / Saga pattern in microservices.

---

## Database Normalization & Denormalization

Goals:
- Reduce redundancy, avoid anomalies (insert/update/delete), improve consistency.

Normal forms (common):
- 1NF: atomic columns, no repeating groups.
- 2NF: 1NF + no partial dependency (relevant when composite PKs exist).
- 3NF: 2NF + no transitive dependency.

When to denormalize:
- Read-heavy workloads, reporting, reduce expensive joins.
- Use with caution: denormalization increases redundancy and update complexity.

Examples:
- Normalized: orders, order_items, customers.
- Denormalized for reporting: orders with customer_name and city copied into orders.

Best practices:
- Normalize up to 3NF for OLTP.
- Denormalize selectively for read performance, and keep a clear update strategy.

Pitfalls:
- Blindly denormalizing across domain without automation.
- Not documenting denormalized fields and origin of truth.

---

## JPA & ORM Integration

Core JPA concepts:
- Entity, Table, Id, GeneratedValue, relationships (OneToMany, ManyToOne, OneToOne, ManyToMany).
- Fetch types: LAZY (default for collections), EAGER.
- JPQL vs Native SQL.
- Criteria API and Specifications for dynamic queries.

Example entity
```java
// language: java
@Entity
public class Customer {
  @Id @GeneratedValue
  private Long id;

  private String name;

  @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
  private List<Order> orders;
}
```

N+1 problem:
- Occurs when ORM loads collections lazily per parent row.
  Fixes:
- JOIN FETCH in JPQL.
- Use EntityGraph.
- Batch fetching configuration.

Best practices:
- Prefer DTO projections for API responses (avoid returning entities directly).
- Use explicit fetch strategies tailored to use-case.
- Keep entities focused on persistence; avoid business logic that depends on transactional context.

Pitfalls:
- LazyInitializationException when accessing LAZY associations outside a transaction.
- Overusing EAGER leads to large object graphs fetched unintentionally.

---

## Spring Data JPA

Repository abstraction:
- CrudRepository / JpaRepository / PagingAndSortingRepository.

Query methods:
- Derived queries by method names (findByEmailAndStatus).
- @Query for custom JPQL or native SQL.
- Pagination and sorting support via Pageable, Sort.

Example repository
```java
// language: java
public interface OrderRepository extends JpaRepository<Order, Long> {
  List<Order> findByCustomerId(Long customerId);

  @Query("SELECT o FROM Order o WHERE o.amount > :min")
  List<Order> findHighValueOrders(@Param("min") BigDecimal min);
}
```

DTO projections:
- Interface-based projections or constructor expressions in JPQL to avoid fetching entire entities.

Best practices:
- Use pagination for list endpoints.
- Prefer projections for performance.
- Keep repository methods focused; complex logic belongs in services.

Pitfalls:
- Returning entities from service layer to UI without DTO mapping.
- Large transactional scope for read-only operations when not needed.

---

## Connection Pooling

Why use pooling:
- Reuse physical DB connections, reduce overhead of establishing connections.

Common pool: HikariCP (Spring Boot default)
Important settings:
- maximumPoolSize
- connectionTimeout
- idleTimeout
- leakDetectionThreshold

Example Spring Boot config (application.properties)
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.leak-detection-threshold=2000
```

Best practices:
- Tune pool size based on app thread count and DB capacity.
- Monitor pool metrics and connection wait times.

Pitfalls:
- Too large pool overwhelms DB.
- Too small pool causes request queuing and timeouts.

---

## Database Performance Tuning

Techniques:
- Use EXPLAIN / EXPLAIN ANALYZE to inspect query plans.
- Add indexes for selective predicates and join columns.
- Avoid SELECT *; select only needed columns.
- Use batch inserts/updates when handling many rows (JDBC batch, JPA batch settings).
- Cache frequently accessed data (Spring Cache, Redis) when appropriate.

Examples
- Use EXPLAIN to check plan:
```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 10;
```

Batch insert (JDBC / JPA tuning) and index maintenance:
- Reduce index count during bulk load and rebuild indexes after large batches if feasible.

Best practices:
- Monitor slow query logs.
- Use read replicas for read scaling and offload long-running analytics to separate systems.
- Archive old data to reduce table size.

Pitfalls:
- Premature optimization without metrics.
- Adding indexes without measuring selectivity.

---

## NoSQL Overview

Types:
- Document: MongoDB
- Key-Value: Redis
- Column-family: Cassandra
- Graph: Neo4j

When to choose NoSQL:
- Flexible schema, horizontal scalability, huge write throughput, denormalized models for reads.

Trade-offs:
- Weaker consistency models (often eventual consistency).
- No standard query language; system-specific features.

Basic Spring Data MongoDB example:
```java
// language: java
@Document(collection = "user")
public class User {
  @Id private String id;
  private String name;
}
public interface UserRepository extends MongoRepository<User, String> {}
```

CAP theorem reminder:
- Consistency, Availability, Partition tolerance — choose two under network partitions.

Pitfalls:
- Using NoSQL for transactional workloads where strong consistency is required.

---

## Schema Migration Tools

Tools:
- Flyway, Liquibase.

Why:
- Version-controlled DDL migrations, repeatable and environment-safe deployment.

Example Flyway usage:
- Place SQL migrations in db/migration with names like V1__create_tables.sql.

Best practices:
- Keep migrations small and reversible when possible.
- Use migrations for schema and seed data needed for app startup (but avoid large data migrations during deploy windows).

Pitfalls:
- Applying destructive migrations in production without backups.
- Mixing ORM auto-DDL in production with migration tools — pick one.

---

## Testing with Databases

Strategies:
- Unit tests: mock repositories or use in-memory DB (H2) for simple tests.
- Integration tests: @DataJpaTest for JPA slices.
- TestContainers: run real DB containers for realistic integration tests.
- Seed test data deterministically for reproducible tests.

Example @DataJpaTest
```java
// language: java
@DataJpaTest
public class CustomerRepositoryTest {
  @Autowired CustomerRepository repo;
  @Test void testFindByName() { ... }
}
```

TestContainers example (JUnit 5)
```java
// language: java
@Testcontainers
public class IntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

  @BeforeAll static void setup() {
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
  }
}
```

Best practices:
- Use TestContainers for CI to reproduce environment.
- Isolate DB state between tests (transactions + rollback or rebuild schema).

Pitfalls:
- Flaky tests due to shared DB state.
- Over-reliance on H2 behavior when production uses PostgreSQL/MySQL (differences exist).

---

## Database in Production

Operational concerns:
- Backup and restore strategies: logical (dump) vs physical backups.
- Monitoring: slow queries, buffer/cache hit rate, CPU, I/O.
- Read/write splitting: replicas for reads.
- Connection timeouts and retries: be explicit in application config.
- Secure credentials: use vaults / secrets manager.

High-availability:
- Replication, failover, automated backups, regular disaster recovery drills.

Best practices:
- Configure alerts for slow query thresholds and connection pool exhaustion.
- Run periodic schema and data health checks.
- Rotate credentials and use least-privilege database users.

Pitfalls:
- Not testing restore procedures.
- Exposing admin DB credentials in source or unsafe environment variables.

---

## Appendices: Common Interview / Debugging Tips

Quick recall lines:
- ACID ensures reliable transactions: Atomicity, Consistency, Isolation, Durability.
- Indexes speed reads, slow writes.
- Left-most prefix rule for composite indexes.
- @Transactional defaults: propagation = REQUIRED, rollback on RuntimeException only.
- Use EXPLAIN to understand query plans.

Common troubleshooting checklist:
1. Slow query? Run EXPLAIN ANALYZE.
2. High CPU or IO? Check indices, large scans, missing pagination.
3. Connection leaks? Enable leak detection in HikariCP.
4. N+1 problem? Inspect logs for repeated queries; use JOIN FETCH or DTO projections.
5. Failing tests with H2? Check SQL dialect differences.

Interview tips:
- Explain joins with diagrams and sample small tables.
- When asked about consistency vs availability, reference CAP theorem trade-offs.
- For transactions, mention MVCC vs locking and how different DBs implement isolation.

---
