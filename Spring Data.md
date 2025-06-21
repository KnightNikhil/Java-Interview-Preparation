✅ 1. Spring Data Overview
* What is Spring Data?
* Spring Data JPA vs Spring Data MongoDB, Cassandra, Elasticsearch, Redis, etc.
* Role in layered architecture (Repository layer abstraction)

⸻

✅ 2. Spring Data JPA Basics
* What is JPA?
* JPA vs Hibernate
* Entity annotations: @Entity, @Table, @Id, @GeneratedValue, etc.
* One-to-One, One-to-Many, Many-to-One, Many-to-Many relationships
* Lazy vs Eager loading

⸻

✅ 3. Repositories
* CrudRepository
* JpaRepository
* PagingAndSortingRepository
* QueryDslPredicateExecutor (optional)

⸻

✅ 4. Query Methods
* Method name conventions for auto-query generation (e.g., findByUsernameAndStatus)
* Custom queries with @Query
* Native SQL queries with nativeQuery = true
* Modifying queries with @Modifying and @Transactional

⸻

✅ 5. Pagination and Sorting
* Pageable, Sort interfaces
* findAll(Pageable pageable)
* Creating pagination-friendly REST APIs

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
* @Transactional at method/class level
* Propagation types (REQUIRED, REQUIRES_NEW, etc.)
* Read-only vs modifying transactions
* Rollbacks and exception handling

⸻

✅ 11. Spring Data Testing
* Unit tests with @DataJpaTest
* Using in-memory DBs (like H2)
* Testing queries and repositories
* Transaction rollback after tests

⸻

✅ 12. Spring Data for NoSQL (Optional)
* MongoDB: @Document, MongoRepository
* Redis: Spring Data Redis with cache support
* Elasticsearch: Entity mapping, indexing
* Use-case based exploration

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