✅ 1. Relational Database Basics
* What is a relational database?
* RDBMS vs NoSQL
* Tables, rows, columns, primary key, foreign key, indexes
* ACID properties (Atomicity, Consistency, Isolation, Durability)

⸻

✅ 2. SQL Language Proficiency
* DDL: CREATE, ALTER, DROP
* DML: SELECT, INSERT, UPDATE, DELETE
* DCL: GRANT, REVOKE
* TCL: COMMIT, ROLLBACK, SAVEPOINT
* Joins: INNER, LEFT, RIGHT, FULL OUTER, SELF JOIN
* Subqueries, IN, EXISTS, GROUP BY, HAVING, ORDER BY
* Views, stored procedures, triggers (basic)

⸻

✅ 3. Indexing
* What is an index and why it’s needed
* Types: B-Tree, Hash, Composite Index, Unique Index
* Clustered vs Non-clustered index
* When indexing helps and when it hurts (e.g., during inserts)

⸻

✅ 4. Transactions & Isolation Levels
* What is a transaction and how it’s managed in Java (@Transactional)
* Isolation levels: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
* Common problems: Dirty Read, Non-Repeatable Read, Phantom Read
* Two-phase commit (2PC) for distributed transactions (conceptually)

⸻

✅ 5. Database Normalization
* 1NF, 2NF, 3NF (at minimum)
* Denormalization: when and why
* Avoiding redundancy and improving consistency

⸻

✅ 6. JPA & ORM Integration
* JPA concepts: Entity, Table, Id, GeneratedValue, OneToMany, ManyToOne
* Entity relationships and fetch types (LAZY vs EAGER)
* JPQL vs Native SQL
* Criteria API and Specifications (for dynamic queries)
* N+1 problem and how to fix it (e.g., JOIN FETCH, EntityGraph)

⸻

✅ 7. Spring Data JPA
* Repository abstraction
* Query methods
* @Query annotation
* Pagination and sorting
* DTO projection and result mapping

⸻

✅ 8. Connection Pooling
* HikariCP (default in Spring Boot)
* Connection pool tuning: max pool size, idle timeout, leak detection
* JDBC vs JPA performance consideration

⸻

✅ 9. Database Performance Tuning
* Query optimization using EXPLAIN
* Avoiding full table scans
* Use of batch inserts/updates
* Caching frequently accessed data (Spring Cache, Redis)

⸻

✅ 10. NoSQL (optional but beneficial)
* Types: Document (MongoDB), Key-Value (Redis), Column-family (Cassandra)
* When to use NoSQL vs RDBMS
* Spring Data MongoDB basics: @Document, MongoRepository
* CAP Theorem (Consistency, Availability, Partition Tolerance)

⸻

✅ 11. Schema Migration Tools
* Liquibase or Flyway
* Version-controlled DDL scripts
* Managing DB changes across environments

⸻

✅ 12. Testing with Databases
* Use H2/in-memory DB for unit tests
* @DataJpaTest and test isolation
* TestContainers for integration testing with real DBs
* Database seeding for integration tests

⸻

✅ 13. Database in Production
* Backup and restore strategies
* Monitoring (DB CPU, slow queries)
* Read/Write splitting (replication)
* Connection timeouts and retries
* Secure access and credentials management (e.g., vaults)