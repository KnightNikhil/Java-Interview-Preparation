## High‑level & Architecture

1. In a large fintech platform (payments, wallets, loans), where does Spring Data JPA fit in the overall layered architecture, and how would you describe its role to a non‑Java architect? 
2. Your notes say “Spring Data repositories ARE DAOs internally but behave like repositories externally.” In a fintech system with complex domain (Accounts, Transactions, Limits), why would you still prefer the Repository pattern over explicit DAOs? 
3. Suppose product asks you to support multiple data stores in future (Postgres now, maybe Mongo/Redis later). How does Spring Data’s abstraction help or hurt that plan in a fintech setup? 

## JPA, EntityManager, Persistence Context

4. Explain the persistence context and its responsibilities, and why it is critical for correctness in a money transfer flow with multiple entity updates (Account, LedgerEntry, Transaction). 
5. In a high‑throughput fintech service, how does the first‑level cache (persistence context) both help and potentially hurt performance? When would you explicitly clear/detach entities? 
6. Walk me through the entity lifecycle states (transient, managed, detached, removed) using a simple “create payment → update status → refund” scenario. Where do bugs typically arise with detached entities in such flows? 

## JpaRepository internals & proxies

7. How does Spring Data generate implementations for your `JpaRepository` interfaces at runtime? Walk through the role of `RepositoryFactoryBean`, dynamic proxies, and `SimpleJpaRepository`. 
8. In a fintech company, why is it important to know that `JpaRepository` ultimately delegates to `EntityManager` methods like `persist`, `merge`, `find`, `remove`? Give a practical debugging/optimization example. 
9. If you see unexpected extra SQL UPDATEs in logs during a high‑QPS operation, how would you reason about `JpaRepository` + EntityManager + dirty checking to find the cause? 

## Dirty checking & @Transactional

10. Explain dirty checking in detail and how `@Transactional` boundaries impact it. Why is this especially important for idempotent payment APIs in fintech? 
11. Consider this code in a payment settlement service:

```java
@Transactional
public void settle(Long txnId) {
    PaymentTxn txn = repo.findById(txnId).orElseThrow();
    txn.setStatus(SETTLED);
    // no repo.save(...)
}
```  

Would this reliably persist the status change? When could this fail or behave unexpectedly in a real system (e.g., due to propagation, read‑only transactions, or multiple EntityManagers)? 

12. How does dirty checking interact with optimistic locking (`@Version`) in Spring Data JPA, and why is that combination often used in wallet/balance updates? 

## Repositories, query methods, fintech domain

13. When would you choose derived query methods like `findByEmailAndStatus` versus writing `@Query` or a custom repository in a fintech codebase? Give concrete examples (e.g., fraud checks, reporting queries). 
14. You have an `Account` entity and you need a query: “all accounts with balance < threshold, status ACTIVE, lastTxnDate older than 30 days.” Would you model this as a derived query method or `@Query` or a custom repository? Why? 
15. In a card‑transactions table with billions of rows, what are the risks of overusing complex derived query methods? How would you design repository APIs so that DB performance and indexing can be tuned by DBAs? 

## @Modifying, bulk updates, consistency

16. Explain why `@Modifying` queries must be inside a transaction and what can go wrong in a fintech system if they are not. 
17. You run this in production:

```java
@Modifying
@Transactional
@Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.id = :id")
int debit(@Param("id") Long id, @Param("amount") BigDecimal amount);
```  

a) What subtle consistency issues might this cause with the persistence context and cached `Account` entities?  
b) How would you mitigate this (e.g., `clearAutomatically`, reloading, or avoiding bulk updates)? 

18. When would you prefer `EntityManager.createQuery(...).executeUpdate()` instead of `@Modifying`, in a big fintech batch job context? 

## Pagination, sorting, large datasets

19. How do `Pageable` and `Sort` work together in Spring Data JPA? In a statement export API for users (potentially thousands of records), how would you design the repository and REST API to scale? 
20. For a regulatory reporting job that needs to process millions of rows nightly, would you use standard `Page<T>` pagination from Spring Data, or something else? Explain trade‑offs with JDBC streaming, cursor‑based pagination, and memory usage. 

## Design and best practices

21. In a fintech domain, how do you ensure that your repository interfaces do not leak low‑level DB concerns into the service layer? Give examples of good vs bad method signatures. 
22. How would you structure repositories and custom implementations for a “Transaction Search” feature that supports many optional filters (date range, status, merchant, card BIN, amount range) without creating dozens of derived methods? 
23. In a microservices architecture (e.g., Payments, Ledger, Risk), how do you decide which aggregates get their own repositories, and how do you handle cross‑aggregate read scenarios without tightly coupling repositories across bounded contexts? 

## Edge cases and anti‑patterns

24. What are common anti‑patterns you’ve seen (or can anticipate) when using Spring Data JPA in fintech systems? Think about N+1 queries, lazy loading in REST responses, large object graphs, and using entities as DTOs. 
25. Your notes say DAO and Repository can coexist (Repository delegating to DAO for complex queries). Describe one real‑looking fintech scenario where you would explicitly introduce such a DAO below a Spring Data repository. 
26. When would you deliberately avoid Spring Data JPA and instead use plain JDBC/MyBatis in a fintech company’s codebase, even if the rest of the system uses JPA? 

If you want, I can next turn these into a spaced‑repetition sheet or categorize them by difficulty for practice.