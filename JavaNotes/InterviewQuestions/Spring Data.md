## Table of Contents

| Section | Questions | Topic |
|---|---|---|
| 1 | 1–10 | Repositories & Core Abstractions |
| 2 | 11–20 | Entity Mapping Fundamentals |
| 3 | 21–30 | Relationships & Associations |
| 4 | 31–40 | Fetching Strategies, Lazy Loading & the N+1 Problem |
| 5 | 41–50 | JPQL, Native Queries, Projections & @Modifying |
| 6 | 51–60 | Persistence Context & Entity Lifecycle |
| 7 | 61–70 | Transactions in Depth |
| 8 | 71–78 | Locking & Concurrency |
| 9 | 79–90 | Performance, Batching & Caching |
| 10 | 91–100 | Specifications, Auditing, Inheritance, Soft Deletes & Testing |

---

# Section 1 — Repositories & Core Abstractions (Q1–Q10)

---

### Q1. Your teammate extends `CrudRepository`, but you always extend `JpaRepository`. In a code review he asks why. What's the actual difference, and when would you deliberately choose the smaller interface?

**Answer:**

The hierarchy is: `Repository` (marker) → `CrudRepository` → `PagingAndSortingRepository` → `JpaRepository`.

- `CrudRepository` gives basic CRUD: `save`, `findById`, `findAll`, `delete`, `count`, `existsById`. Its `findAll()` returns `Iterable<T>`.
- `PagingAndSortingRepository` adds `findAll(Pageable)` and `findAll(Sort)`.
- `JpaRepository` is JPA-specific: returns `List<T>` instead of `Iterable`, and adds `flush()`, `saveAndFlush()`, `saveAllAndFlush()`, `deleteAllInBatch()`, `deleteAllByIdInBatch()`, and `getReferenceById()`.

You'd deliberately choose a **smaller** interface for encapsulation: if you extend `Repository` directly, you expose *only* the methods you declare — for example a read-only repository:

```java
public interface CustomerReadRepository extends Repository<Customer, Long> {
    Optional<Customer> findById(Long id);
    List<Customer> findByStatus(Status status);
    // no save(), no delete() — physically impossible to misuse
}
```

This is the recommended way to enforce "this aggregate is never deleted through this service."

**Follow-up trap:** *"Is `JpaRepository` portable across stores like MongoDB?"* — No. `CrudRepository` is store-agnostic; `JpaRepository` ties you to JPA. If your module might switch stores, code against the smaller abstraction.

---

### Q2. You write `findByLastnameAndFirstname(String lastname, String firstname)` and it "just works" with no `@Query`. In the interview you're asked: how does Spring turn that method name into SQL, and what happens if a property doesn't exist?

**Answer:**

This is a **derived query**. At application startup, Spring Data parses the method name using a state machine:

1. It strips a known prefix: `find…By`, `read…By`, `query…By`, `count…By`, `exists…By`, `delete…By`.
2. The part after `By` is split into **property expressions** joined by `And`/`Or`, each optionally followed by an operator (`Containing`, `GreaterThan`, `Between`, `In`, `IsNull`, `IgnoreCase`, `StartingWith`, …) and terminated by ordering (`OrderByLastnameDesc`).
3. Each property expression is resolved against the entity metamodel: `Lastname` → property `lastname`. For nested paths it tries the longest match first: `findByAddressZipCode` resolves as `address.zipCode` — if the entity had a property `addressZip`, you can disambiguate with an underscore: `findByAddress_ZipCode`.
4. The parsed tree becomes a JPA Criteria query (a `PartTree` query), not string SQL.

If a property doesn't exist, the application **fails at startup** with `PropertyReferenceException: No property 'xyz' found for type…`. This is a feature: broken queries can't reach production silently — unlike a typo in a native SQL string.

**Follow-up trap:** *"When do you stop using derived queries?"* — When the method name gets longer than the query would be (3+ conditions), or when you need joins/aggregations/projections — switch to `@Query`. Readability is the criterion.

---

### Q3. There's no `@Repository` annotation on your repository interfaces, and no implementation class anywhere — yet you can `@Autowired` them. The interviewer asks: who implements the interface, and when?

**Answer:**

At startup, `@EnableJpaRepositories` (auto-configured by Spring Boot) triggers a classpath scan for interfaces extending `Repository`. For each one, a `JpaRepositoryFactory` creates a **JDK dynamic proxy** and registers it as a Spring bean. The proxy routes calls in this order:

1. **Custom implementation fragment** — if the method is declared in a fragment interface you implemented yourself (see Q9), call that.
2. **Declared query** — if the method has `@Query`, or a matching named query (`Customer.findByEmail`) exists, execute it.
3. **Derived query** — otherwise parse the method name into a `PartTree` query.
4. **Base methods** (`save`, `findById`…) are delegated to a shared default implementation, `SimpleJpaRepository`, which is just a thin wrapper around the JPA `EntityManager` (`em.persist`, `em.merge`, `em.find`, Criteria API for `findAll`).

You don't need `@Repository` on the interface — the proxy is already a bean, and `PersistenceExceptionTranslationPostProcessor` translation (converting `PersistenceException` into Spring's `DataAccessException` hierarchy) is applied to it automatically.

**Follow-up trap:** *"Why is exception translation useful?"* — Your service layer catches `DataIntegrityViolationException` or `OptimisticLockingFailureException` regardless of whether the store is Hibernate, EclipseLink, or JDBC — vendor-neutral, unchecked, and consistent.

---

### Q4. In a service you call `userRepository.save(user)` and then immediately query the same table with a native query — the row isn't there. But after the method returns, the row exists. Explain, and explain the difference between `save()` and `saveAndFlush()`.

**Answer:**

`save()` does **not** write to the database immediately. It puts the entity into the persistence context (`em.persist` for new entities, `em.merge` for existing ones). The actual `INSERT` happens at **flush time** — typically just before transaction commit, or automatically before a JPQL/HQL query that Hibernate detects might be affected by pending changes (`FlushModeType.AUTO`).

The catch: auto-flush-before-query only applies reliably to JPQL queries against affected tables. A **native query** may not trigger a flush (behavior depends on flush mode and provider synchronization of native queries), so your native `SELECT` runs against the database where the `INSERT` hasn't happened yet.

`saveAndFlush()` forces `em.flush()` immediately after saving — the SQL `INSERT` is sent to the database *within the current transaction* right away. Note carefully:

- **Flush ≠ commit.** Other transactions still can't see the row (isolation); it just synchronizes the persistence context with the DB *inside* your transaction.
- Use `saveAndFlush()` when a subsequent native query, a trigger, or a DB-generated value needs the row to physically exist.
- Overusing it defeats JDBC batching and dirty-checking optimizations.

```java
userRepository.save(user);          // queued in persistence context
userRepository.saveAndFlush(user);  // INSERT sent now, still uncommitted
```

**Follow-up trap:** *"When does flush happen automatically?"* — Before commit, before a query whose result might be affected by pending changes (AUTO flush mode), and when you call `flush()` explicitly.

---

### Q5. A colleague uses `findById(id).get()` everywhere and it works fine in dev, then throws `NoSuchElementException` in production. Separately, another colleague uses `getReferenceById(id)` and gets a weird `LazyInitializationException`. Explain both APIs and when each is correct.

**Answer:**

- `findById(id)` → executes a `SELECT` immediately, returns `Optional<T>`. Empty if the row doesn't exist. Calling `.get()` blindly is the bug — handle absence explicitly:

```java
User user = userRepository.findById(id)
    .orElseThrow(() -> new UserNotFoundException(id));
```

- `getReferenceById(id)` (formerly `getById`/`getOne`) → executes **no SQL at all**. It returns a lazy **proxy** holding only the id (`em.getReference`). The database is hit only when you access a non-id property. Consequences:
    - If the row doesn't exist, you get `EntityNotFoundException` **later**, at first property access — not at call time.
    - If you access a property after the persistence context is closed, you get `LazyInitializationException` — that's the second colleague's bug.

The **correct** use of `getReferenceById` is setting foreign keys without a pointless `SELECT`:

```java
// BAD: fires SELECT on author just to set a FK
Post post = new Post();
post.setAuthor(authorRepository.findById(authorId).orElseThrow());

// GOOD: no SELECT; INSERT uses the id directly
post.setAuthor(authorRepository.getReferenceById(authorId));
postRepository.save(post);
```

**Rule:** need the data → `findById`; need only a reference/FK → `getReferenceById`.

**Follow-up trap:** *"Why did `.get()` 'work in dev'?"* — Dev data always had the row. Absence-handling bugs are data-dependent, which is why `Optional` forces you to decide.

---

### Q6. You need to delete all expired tokens. You write `deleteByExpiryDateBefore(Instant now)` and are shocked to see in the SQL log: one `SELECT` plus N individual `DELETE` statements. Why, and what's the efficient alternative?

**Answer:**

Derived `deleteBy…` methods are implemented as **load-then-delete-one-by-one**: Spring Data first runs the query to fetch matching entities, then calls `em.remove()` per entity. This is deliberate — it guarantees that:

- JPA **lifecycle callbacks** (`@PreRemove`) fire for each entity,
- **cascade** rules (`CascadeType.REMOVE`, `orphanRemoval`) are honored,
- the persistence context stays consistent.

For thousands of rows this is disastrous. The efficient alternative is a **bulk JPQL delete**:

```java
@Modifying
@Query("delete from Token t where t.expiryDate < :now")
int deleteExpired(@Param("now") Instant now);
```

This issues a single `DELETE … WHERE …` statement. The trade-offs you must state in an interview:

- **No cascades, no callbacks** — bulk operations bypass entity lifecycle entirely; child rows must be handled by DB-level `ON DELETE CASCADE` or a prior bulk delete.
- **Persistence context is not updated** — already-loaded Token entities remain in the context as ghosts; use `@Modifying(clearAutomatically = true)` if the same transaction continues to read.
- Also relevant: `deleteAllInBatch()` / `deleteAllByIdInBatch()` on `JpaRepository` issue single bulk statements, while `deleteAll()` deletes one by one.

**Follow-up trap:** *"Also, derived delete queries require a transaction"* — yes: `deleteBy…` methods must run inside `@Transactional` (Spring Data doesn't make them transactional by default like it does for its `SimpleJpaRepository` methods).

---

### Q7. Your repository method returns `List<User>` but the team debates: should it be `Optional<User>`, `Stream<User>`, `Page<User>`, or `Slice<User>`? Walk through the return-type options Spring Data supports and when each is the right call.

**Answer:**

Spring Data adapts query execution to the declared return type:

- `User` — expects at most one result; **throws `IncorrectResultSizeDataAccessException`** if more than one row matches; returns `null` if none. Prefer `Optional<User>` to make emptiness explicit.
- `Optional<User>` — same single-result semantics, empty instead of null. Still throws if 2+ results.
- `List<User>` — zero or more results; never null (empty list).
- `Page<User>` — takes a `Pageable`; runs the content query **plus a `COUNT` query** for total elements. Expensive on big tables.
- `Slice<User>` — takes a `Pageable`; fetches `size + 1` rows to know only *whether a next page exists*. **No count query** — right choice for infinite scroll.
- `Stream<User>` — lazily streams results; **must** be used inside a transaction and inside try-with-resources so the underlying cursor/connection is released:

```java
@Transactional(readOnly = true)
public void export() {
    try (Stream<User> s = userRepository.streamAllByActiveTrue()) {
        s.forEach(exporter::write);
    }
}
```

- `long` / `boolean` for `countBy…` / `existsBy…` — an `existsBy` generates an optimized query (`select 1 … limit 1`-style) instead of counting; never use `count > 0` when you just need existence.
- Also supported: `CompletableFuture<T>` with `@Async`, and `Streamable<T>`.

**Follow-up trap:** *"Why is `Page` expensive?"* — the extra `COUNT(*)` can cost as much as the data query on large joined tables; see Q86 for count-query optimization.

---

### Q8. The interviewer asks: "You call `repository.save(entity)` — how does Spring Data decide whether to INSERT or UPDATE?" Then: "Why did my entity with a manually-assigned String ID always SELECT before INSERT, and how do I stop it?"

**Answer:**

`SimpleJpaRepository.save()` is:

```java
if (entityInformation.isNew(entity)) em.persist(entity);
else return em.merge(entity);
```

**Is-new detection strategy (in order):**

1. If the entity implements `Persistable<ID>`, it calls your `isNew()`.
2. Else if there's a `@Version` field of a wrapper type, entity is new when version is `null`.
3. Else: new when the `@Id` is `null` (or 0 for primitives).

The trap: with a **manually assigned ID** (say a String natural key), the id is never null, so `save()` always calls `em.merge()`. Merge on an entity that isn't in the persistence context triggers a **`SELECT` first** (to load current state or discover the row doesn't exist), then INSERT/UPDATE. That's your extra SELECT — and at scale it doubles round-trips.

**Fix:** implement `Persistable`:

```java
@Entity
public class Country implements Persistable<String> {
    @Id private String isoCode;

    @Transient
    private boolean isNew = true;

    @Override public String getId() { return isoCode; }
    @Override public boolean isNew() { return isNew; }

    @PostLoad @PostPersist
    void markNotNew() { this.isNew = false; }
}
```

Now `save()` calls `persist()` directly for fresh instances — no exploratory SELECT.

**Follow-up trap:** *"What happens if you `persist` an entity whose id already exists in the DB?"* — a constraint violation at flush (`DataIntegrityViolationException`), because persist assumes new.

---

### Q9. You need a repository method with a complex dynamic query built via the Criteria API — something derived queries and `@Query` can't express. How do you add custom behavior to a Spring Data repository without losing the generated CRUD methods?

**Answer:**

Use a **custom repository fragment**:

```java
// 1. Fragment interface
public interface UserRepositoryCustom {
    List<User> searchByFilter(UserFilter filter);
}

// 2. Implementation — name MUST be <FragmentInterface>Impl by default
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> searchByFilter(UserFilter f) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> q = cb.createQuery(User.class);
        Root<User> root = q.from(User.class);
        List<Predicate> preds = new ArrayList<>();
        if (f.name() != null)
            preds.add(cb.like(cb.lower(root.get("name")), "%" + f.name().toLowerCase() + "%"));
        if (f.minAge() != null)
            preds.add(cb.ge(root.get("age"), f.minAge()));
        q.where(preds.toArray(Predicate[]::new));
        return em.createQuery(q).getResultList();
    }
}

// 3. Main repository composes the fragment
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom { }
```

The proxy checks fragments **before** anything else, so a fragment method even overrides a base method of the same signature (you can, e.g., override `findById` to add caching). Multiple fragments can be composed; the `Impl` suffix is configurable via `@EnableJpaRepositories(repositoryImplementationPostfix = …)`.

Mention the alternatives and when they beat raw Criteria: **Specifications** (Q91) for composable predicates, **Querydsl** for type-safety, or replacing the *entire* base class via `@EnableJpaRepositories(repositoryBaseClass = …)` when all repositories need a new base method.

**Follow-up trap:** *"Why did your fragment impl not get picked up?"* — wrong name (must match `Impl` postfix convention) or it's not in a scanned package.

---

### Q10. During startup your app fails with `Could not create query for public abstract … findByUsernme`. Another day it fails with two repositories matching one entity in different modules. What do these startup failures tell you about how Spring Data validates repositories, and how do you keep a shared base interface from being instantiated?

**Answer:**

**Part 1 — query validation at bootstrap.** Every declared method on every repository is resolved at startup: derived names are parsed against the entity metamodel and `@Query` JPQL strings are compiled by the provider (`Query` named-parameter checks included). A typo (`findByUsernme`) fails fast with `QueryCreationException` / `PropertyReferenceException`. This is a strong argument for JPQL over native SQL: **native query strings are not validated against the schema at startup** (only prepared at first execution), so typos there surface at runtime. Startup validation is also why a plain `@SpringBootTest` context load already smoke-tests every repository query.

**Part 2 — `@NoRepositoryBean`.** When you build a shared base interface with common methods:

```java
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    List<T> findAllByTenantId(String tenantId);
}

public interface OrderRepository extends BaseRepository<Order, Long> { }
```

Without `@NoRepositoryBean`, Spring Data would try to create a proxy for `BaseRepository` itself and fail (no concrete domain type). The annotation says "this is an intermediate interface — don't instantiate." All of Spring Data's own intermediate interfaces (`CrudRepository`, `JpaRepository`…) carry it.

Also worth naming: with multiple Spring Data modules on the classpath (JPA + Redis, say), use `@EnableJpaRepositories(basePackages = …)` per module so each store only picks up its own interfaces — otherwise you get "Spring Data … does not support …" warnings or the wrong module claims a repository.

**Follow-up trap:** *"How would you validate native queries at startup?"* — you can't out of the box; mitigate with repository tests (`@DataJpaTest` + real DB via Testcontainers, Q98).

---

# Section 2 — Entity Mapping Fundamentals (Q11–Q20)

---

### Q11. A new joiner writes an `@Entity` as a Java `record` and it fails. Then they add Lombok `@Data` to an entity class and you reject the PR. What are the actual requirements for an entity class, and why is `@Data` dangerous on entities?

**Answer:**

JPA entity requirements: a **no-arg constructor** (at least protected — Hibernate instantiates via reflection/bytecode), the class must **not be final** (Hibernate subclasses it for lazy proxies), it must have an `@Id`, and fields shouldn't be final. A Java `record` fails on all counts: no no-arg constructor, final class, final fields — records are fine for **DTOs/projections**, never for entities.

Why `@Data` is rejected:

- It generates `equals`/`hashCode` over **all fields** — breaks entity identity semantics (see Q59): hashCode changes when fields change, corrupting `HashSet` membership; comparing lazy fields can trigger unwanted loading.
- It generates `toString()` over all fields — printing an entity **triggers lazy loading** of every association (or throws `LazyInitializationException` in a log statement outside a transaction).
- `@EqualsAndHashCode`/`@ToString` on bidirectional associations cause **infinite recursion** (`StackOverflowError`) because each side calls the other.

Acceptable Lombok on entities: `@Getter @Setter @NoArgsConstructor`, plus carefully hand-written `equals`/`hashCode`/`toString` excluding associations.

**Follow-up trap:** *"Why must the class not be final?"* — lazy loading works by Hibernate generating a runtime **proxy subclass**; a final class (or final getters with bytecode enhancement off) can't be proxied, silently degrading lazy to eager in some configurations.

---

### Q12. You switch an entity's ID from `GenerationType.IDENTITY` to `GenerationType.SEQUENCE` and batch-insert performance improves 10x. Explain every generation strategy, and why IDENTITY kills batching.

**Answer:**

- **IDENTITY** — DB auto-increment column. The id is generated **by the database during the INSERT**. Hibernate must execute each `INSERT` immediately at `persist()` to learn the id (the persistence context needs the id to manage the entity). Consequence: **JDBC batching of inserts is impossible** — every persist is its own round-trip.
- **SEQUENCE** — Hibernate calls `next val` on a DB sequence *before* insert, so it knows ids up front and can delay + batch the INSERTs at flush. With the **pooled/pooled-lo optimizer** and `allocationSize = 50`, one sequence call fetches a block of 50 ids → 1 extra round-trip per 50 inserts, and batching works:

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
@SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 50)
private Long id;
```

(The DB sequence must be created with `INCREMENT BY 50` to match, or you get duplicate-key chaos.)

- **TABLE** — simulates a sequence with a separate table + row locks. Portable but slow (extra transactional round-trips, contention). Avoid.
- **AUTO** — provider picks based on dialect: for MySQL historically IDENTITY; modern Hibernate may pick TABLE/SEQUENCE depending on version — **be explicit** rather than trusting AUTO.
- **UUID** — `@GeneratedValue(strategy = GenerationType.UUID)` (JPA 3.1) or assign in code. Great for distributed generation and exposing non-guessable ids; watch index locality — random UUIDv4 fragments B-tree indexes (prefer time-ordered UUIDv7, or store binary).

MySQL has no sequences → you're stuck with IDENTITY there (and no insert batching via Hibernate), which is a real architectural talking point.

**Follow-up trap:** *"Why exactly does Hibernate need the id at persist time?"* — the persistence context is a `Map<EntityKey, Entity>`; the key includes the id. Without an id it can't register the managed instance (Hibernate works around this for IDENTITY only by insert-early).

---

### Q13. Your `Money`-like fields (`amount`, `currency`) are duplicated across five entities. Also, one entity needs *two* addresses (billing, shipping). How do you model this without inheritance or extra tables?

**Answer:**

Use **`@Embeddable` / `@Embedded`** — value objects whose columns are inlined into the owner's table:

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String zipCode;
}

@Entity
public class Order {
    @Id @GeneratedValue Long id;

    @Embedded
    private Address billingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street",  column = @Column(name = "ship_street")),
        @AttributeOverride(name = "city",    column = @Column(name = "ship_city")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "ship_zip"))
    })
    private Address shippingAddress;
}
```

Key points to hit:

- An embeddable has **no identity and no table of its own** — it lives and dies with the owner. It models a DDD *value object*.
- Two embeddeds of the same type collide on column names → resolve with `@AttributeOverrides`.
- Embeddables can be nested, can contain associations (`@ManyToOne` inside an embeddable is legal), and can be used as collection elements via `@ElementCollection` (Q20) or as composite ids via `@EmbeddedId` (Q17).
- Hibernate 6 adds `@EmbeddableInstantiator` so embeddables can be immutable/record-like.

**Follow-up trap:** *"Embeddable vs `@OneToOne` to a separate table?"* — Embedded = same table, no join, no identity, always loaded with owner. Separate entity = own lifecycle/identity, joinable, lazily loadable. Choose by whether the concept has independent identity.

---

### Q14. A production incident: someone reordered the values of `enum OrderStatus { NEW, PAID, SHIPPED }` and historical rows now mean the wrong status. What went wrong, and how should enums (and other awkward types like JSON) be mapped?

**Answer:**

The enum was mapped with **`EnumType.ORDINAL`** (the JPA default!) — stored as 0, 1, 2 by declaration position. Reordering or inserting a value re-labels all existing rows. Rules:

- Always use `@Enumerated(EnumType.STRING)` — stable against reordering (renaming still needs a data migration, and it costs more storage; add a check constraint or use a DB enum type for integrity).
- Bullet-proof alternative: persist a stable code via a JPA **`AttributeConverter`**:

```java
@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<OrderStatus, String> {
    public String convertToDatabaseColumn(OrderStatus s) { return s == null ? null : s.getCode(); }
    public OrderStatus convertToEntityAttribute(String code) { return OrderStatus.fromCode(code); }
}
```

`AttributeConverter` is the general answer for any custom scalar mapping: encrypting a column, `boolean` ↔ `"Y"/"N"`, comma-separated lists, etc. `autoApply = true` applies it to every attribute of that type. (Converters can't be applied to `@Id`, `@Version`, or association fields.)

For **JSON columns**, modern Hibernate 6 has native support: `@JdbcTypeCode(SqlTypes.JSON)` on a POJO/`Map` attribute maps to `jsonb`/`json`. Pre-Hibernate-6, the standard answer was the hypersistence-utils `JsonType`.

**Follow-up trap:** *"Why is ORDINAL faster and does it matter?"* — Smaller storage/index size, but the integrity risk dwarfs the micro-optimization; STRING (or a converter with a short code) is the professional default.

---

### Q15. The interviewer asks: "`@Column(nullable = false)` vs Bean Validation `@NotNull` — same thing?" And: "What do `insertable = false, updatable = false` do, and when have you actually needed them?"

**Answer:**

Not the same thing:

- `@Column(nullable = false)` is **DDL metadata** — it only matters if Hibernate generates the schema (adds `NOT NULL`). At runtime, Hibernate itself won't reject a null (you get the DB constraint violation at flush, as `DataIntegrityViolationException`).
- `@NotNull` (Bean Validation) is validated **in the application** at `pre-persist`/`pre-update` (throws `ConstraintViolationException` before SQL), *and* Hibernate also uses it for DDL. Best practice: use Bean Validation for behavior, keep DDL managed by Flyway/Liquibase anyway (never `ddl-auto=update` in prod — Q100 territory).

`insertable = false, updatable = false` mark a mapping **read-only** for writes. The classic real-world need: mapping the same column twice — an association plus its raw FK value:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
private Customer customer;

@Column(name = "customer_id", insertable = false, updatable = false)
private Long customerId;   // read-only shadow of the FK
```

Without the read-only flags, two writable mappings of one column is a startup error. Other uses: columns populated by DB defaults/triggers (pair with `@Generated`/`@CreationTimestamp` semantics), and `@ManyToOne` mappings over columns that are part of a composite key.

**Follow-up trap:** *"updatable=false on a normal column?"* — makes it immutable after insert (e.g., `createdAt`), Hibernate silently excludes it from UPDATE statements even if you change the field.

---

### Q16. You rename an entity field `orderLineItems` and suddenly the generated SQL references a column `order_line_items` that doesn't exist in the legacy table (`ORDERLINEITEMS`). Explain how naming resolution works in Spring Boot + Hibernate and your options.

**Answer:**

Two layers decide the final SQL name:

1. **Implicit naming strategy** — when you don't specify a name, JPA/Hibernate derives a logical name from the field (`orderLineItems` → logical `orderLineItems`).
2. **Physical naming strategy** — maps logical names to physical DB names. **Spring Boot's default** is `CamelCaseToUnderscoresNamingStrategy`: camelCase → snake_case, lower-cased (`orderLineItems` → `order_line_items`). That's why the SQL didn't match the legacy `ORDERLINEITEMS` column.

Options:

- Explicit per-column: `@Column(name = "ORDERLINEITEMS")` — explicit names still pass through the *physical* strategy in Boot's default (which lowercases/underscores!), a classic gotcha; quoting or changing strategy is needed for exact case.
- Globally switch the strategy for legacy schemas:

```properties
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

(the JPA-compliant "use exactly what's written" strategy), or write a custom `PhysicalNamingStrategy` implementing a corporate convention (e.g., `TBL_` prefixes).

- Quoted identifiers for reserved words/case sensitivity: `@Column(name = "\"user\"")` or `spring.jpa.properties.hibernate.globally_quoted_identifiers=true`.

**Follow-up trap:** *"Table names too?"* — same mechanism: `@Table(name = "...")`, and remember `@Table` also carries `schema`, `uniqueConstraints` and `indexes` (DDL-generation metadata).

---

### Q17. A legacy table has a composite primary key `(order_id, product_id)`. Compare the two JPA ways to map it, and explain what invariants the id class must satisfy.

**Answer:**

**Option A — `@EmbeddedId`** (usually preferred):

```java
@Embeddable
public class OrderLineId implements Serializable {
    private Long orderId;
    private Long productId;
    // no-arg ctor, equals & hashCode over both fields — MANDATORY
}

@Entity
public class OrderLine {
    @EmbeddedId
    private OrderLineId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")                     // FK is part of the PK
    @JoinColumn(name = "order_id")
    private Order order;
}
```

**Option B — `@IdClass`**: the entity declares each key field with `@Id` directly, and `@IdClass(OrderLineId.class)` names a mirror class with identically-named fields. Queries reference `ol.orderId` directly (flat), and `findById` still takes the id-class instance.

Invariants for both id classes: `Serializable`, public no-arg constructor, and **correct `equals`/`hashCode`** — the persistence context keys entities by (entity type, id), so broken equality corrupts identity management.

Differences worth stating: `@EmbeddedId` keeps the key a single value object (`line.getId().getOrderId()`), is DRY, plays well with `@MapsId`; `@IdClass` duplicates fields but reads more naturally in JPQL and is closer to legacy code styles. `@MapsId` is the piece most candidates miss — it lets a `@ManyToOne` **share** columns with the PK instead of mapping the column twice.

Modern design note: prefer surrogate keys for new tables; composite keys are mostly a legacy-integration skill — saying this shows judgment.

**Follow-up trap:** *"How do you `findById` with a composite key?"* — `repository.findById(new OrderLineId(1L, 2L))` — the id type parameter of the repository is the id class.

---

### Q18. Your `created_at` column must be set once on insert, `updated_at` on every change — and a `row_version` column is maintained by a DB trigger. How do you map DB-generated and framework-generated values correctly?

**Answer:**

Three distinct mechanisms — know which generates where:

1. **Hibernate-side generation:** `@CreationTimestamp` and `@UpdateTimestamp` — Hibernate fills the value in Java at insert/update. Simple, no extra SQL.
2. **Spring Data auditing:** `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` + `@EntityListeners(AuditingEntityListener.class)` + `@EnableJpaAuditing` — same effect but framework-level, and adds **who** via `AuditorAware` (pulls the user from Spring Security). Preferred in Spring projects; typically extracted into a `@MappedSuperclass` base entity (Q95).
3. **Database-side generation:** for the trigger-maintained `row_version`, map it read-only and tell Hibernate to re-read after writes:

```java
@Generated(event = { EventType.INSERT, EventType.UPDATE })  // Hibernate 6
@Column(insertable = false, updatable = false)
private Long rowVersion;
```

Hibernate then issues a re-`SELECT` (or uses `RETURNING` on capable DBs) after insert/update to refresh the value. Similarly `@ColumnDefault` + `@DynamicInsert` lets DB defaults apply by omitting the column from the INSERT.

Use `Instant` (or `OffsetDateTime`) for timestamps — `java.util.Date`/`Calendar` with `@Temporal` is the legacy answer; `java.time` types need no annotation. Store UTC; set `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` to avoid JDBC timezone drift.

**Follow-up trap:** *"Is a trigger-maintained version column usable for optimistic locking?"* — Not with `@Version` (Hibernate must control increments); DB-generated versions need custom locking or Hibernate's `@Source`-style/`@Generated` patterns — usually you let Hibernate own `@Version` instead (Q71).

---

### Q19. An entity has 40 columns, two of which are a 2 MB `byte[]` document and a huge CLOB description. Listing screens are slow because every query drags the blobs along. What are your options for mapping large objects and loading them lazily?

**Answer:**

Map them as `@Lob` (`byte[]`/`String` → BLOB/CLOB). The catch: **`@Basic(fetch = FetchType.LAZY)` on a plain column is only a *hint*** — plain-vanilla Hibernate ignores it unless **bytecode enhancement** is enabled (the enhancement plugin instruments getters; then lazy basic attributes and `@LazyGroup` work). Options, in the order you should present them:

1. **Don't map them on the hot entity at all** — the cleanest fix. Use DTO projections for list screens (`select new …` / interface projections, Q47) that simply don't select the LOB columns.
2. **Split the table vertically in the model:** put LOBs in a secondary entity sharing the PK, loaded on demand:

```java
@Entity
public class Document {
    @Id Long id;                       // shared PK
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId @JoinColumn(name = "id")
    private Post post;
    @Lob byte[] content;
}
```

(One-to-one lazy works reliably from the *owning* side — Q29.)

3. **Bytecode enhancement + `@Basic(LAZY)`** — works, but adds build complexity and subtle behavior changes; justify before adopting.
4. `@SecondaryTable` maps one entity over two tables — but Hibernate joins the secondary table on load unless combined with enhancement, so it rarely solves the perf problem alone.

Also mention: streaming very large files should arguably bypass JPA entirely (JDBC streams / object storage with a path column) — knowing when *not* to use JPA scores points.

**Follow-up trap:** *"Why is `FetchType.LAZY` guaranteed for associations but not basics?"* — associations lazy-load via **proxies** (separate object), basics live inside the entity instance itself — only bytecode instrumentation can intercept field access.

---

### Q20. You map a user's phone numbers as `@ElementCollection List<String>`. Later, updating one phone number in a list of 10 deletes all 10 rows and re-inserts them. Explain `@ElementCollection` semantics, the bag-vs-set-vs-ordered distinction, and when to promote to a real entity.

**Answer:**

`@ElementCollection` maps a collection of **basic types or embeddables** into a separate table (`@CollectionTable`) fully owned by the parent — no repository, no independent lifecycle, always cascaded, and (by spec) LAZY by default.

The delete-all-reinsert behavior happens because with a plain `List` and no order column, Hibernate treats it as a **bag** — it has no way to identify individual rows (no PK on the element side), so *any* modification is implemented as `DELETE WHERE user_id = ?` + re-INSERT everything. Fixes/mitigations:

- Use a `Set` of unique values — Hibernate can then add/remove individual rows (for embeddable elements, correct `equals`/`hashCode` is essential — deletes match by **all columns**).
- Use `@OrderColumn` to make it a true indexed list — updates become positional (still shuffles rows on middle-insert).
- Accept it for tiny collections — the semantics (value collection, wholesale replacement) are often exactly right for e.g. tags.

Promote to a **real `@OneToMany` entity** when you need: an id on each element, individual updates at scale, queries against elements alone, extra columns/lifecycle, or paging over the collection. Rule of thumb worth quoting: `@ElementCollection` for small, wholly-owned value lists; entities for anything with identity.

**Follow-up trap:** *"Can you query the collection table directly?"* — In JPQL yes, via `join u.phoneNumbers p` — but you can't load elements without the owner or map them to a repository; that's the promotion trigger.

---

# Section 3 — Relationships & Associations (Q21–Q30)

---

### Q21. Design question: `Order` has many `OrderItem`s. A junior maps it with only `@OneToMany @JoinColumn` (unidirectional). You recommend bidirectional with `mappedBy`. The interviewer asks you to defend that with the actual SQL each mapping generates.

**Answer:**

**Unidirectional `@OneToMany` without `@JoinColumn`** (worst): Hibernate creates a **join table** `order_order_item` — an extra table nobody asked for, with extra inserts/deletes on every collection change.

**Unidirectional `@OneToMany` with `@JoinColumn`** (better, still flawed): FK lives on `order_item`, but the *parent* owns the association, so Hibernate persists children first with `INSERT` (FK null or via extra statement) and then issues **additional `UPDATE order_item SET order_id = ?`** statements — one per child. Removal similarly updates FK to null before delete (and requires nullable FK).

**Bidirectional** (recommended for large child sets):

```java
@Entity
public class Order {
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {   // helper keeps both sides in sync
        items.add(item);
        item.setOrder(this);
    }
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}

@Entity
public class OrderItem {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
```

The `@ManyToOne` side **owns** the FK, so each child is persisted with one clean `INSERT` including `order_id`. No extra UPDATEs, no join table, `NOT NULL` FK possible.

Also state the modern counterpoint: often the best mapping is **only `@ManyToOne` on the child, no collection on the parent at all** — fetch children with `orderItemRepository.findByOrderId(id)` (pageable!). A mapped collection you never navigate is pure liability.

**Follow-up trap:** *"Why initialize the collection field inline (`new ArrayList<>()`)?"* — avoids NPEs on new instances and lets Hibernate replace it with its own `PersistentBag` on load without null-handling.

---

### Q22. Classic: "In a bidirectional association, what is the *owning side*, what does `mappedBy` mean, and what happens if you set only the non-owning side and save?"

**Answer:**

- The **owning side** is the side whose state Hibernate reads when writing the FK (or join-table rows) to the database. It's the side **without** `mappedBy` — for `@OneToMany`/`@ManyToOne` pairs it must be the `@ManyToOne` (FK holder); for `@ManyToMany` you pick one.
- `mappedBy = "order"` on the inverse side says: "I am a mirror; the association is already mapped by the `order` field on the other entity — don't create a second FK/join table for me."

**If you set only the inverse side** (`order.getItems().add(item)` without `item.setOrder(order)`) and save: **the FK is not written** — Hibernate consults only the owning side. You get an `order_item` row with `order_id = NULL` (or a constraint violation if non-null). This is one of the most common real-world JPA bugs, and the reason helper methods (`addItem`) that set both sides are considered mandatory hygiene.

The in-memory inconsistency also bites reads within the same transaction: the collection you didn't sync won't contain the item even after flush, until a reload.

**Follow-up trap:** *"Does `cascade` on the inverse side work?"* — Yes — cascading is orthogonal to ownership; `cascade = ALL` on the `@OneToMany` (inverse) side is the normal pattern: cascade follows object graph traversal, `mappedBy` only governs column writing.

---

### Q23. `@JoinColumn` vs `@JoinTable`: the interviewer shows you a `@ManyToOne` mapped with `@JoinTable` and asks whether that's even legal, and when you'd prefer a join table for a to-one association.

**Answer:**

Legal, yes. `@JoinColumn` puts a FK column on the owner's table; `@JoinTable` externalizes the link into a third table — and JPA allows `@JoinTable` on **any** association type, not just `@ManyToMany`:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinTable(name = "employee_desk",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "desk_id"))
private Desk desk;
```

When a join table for to-one makes sense: the association is **optional and sparse** (most rows would have a NULL FK — a join table avoids the null column and can enforce at-most-one with a unique constraint), or you can't alter the legacy owner table, or you want the link's lifecycle separate for audit reasons.

Defaults worth knowing: with no explicit names, join-table naming is `owner_owned` and FK columns `owner_id` / `owned_id` — always name them explicitly in real projects (schema review, migrations). For `@ManyToMany`, the join table is mandatory (two FKs, composite PK/unique over both).

Also mention `referencedColumnName` — a `@JoinColumn` can reference a non-PK unique column of the target, needed for legacy schemas joining on natural keys.

**Follow-up trap:** *"Downside of join tables?"* — every navigation costs an extra join; and ORM-generated join-table changes are delete+insert-heavy compared to a single FK column update.

---

### Q24. Your `@ManyToMany(cascade = CascadeType.ALL)` between `Student` and `Course` deleted a Course that other students were enrolled in. Also, removing one course from a student regenerates the whole join table for him. Diagnose both, and explain when to abandon `@ManyToMany` entirely.

**Answer:**

**Bug 1 — cascade on many-to-many:** `CascadeType.ALL` (specifically REMOVE) is **nonsensical for `@ManyToMany`**: entities on the other side are *shared*. Deleting a Student cascaded delete to Course rows still referenced by other students (constraint violation or data loss). Rule: on `@ManyToMany` never use REMOVE (PERSIST/MERGE at most). Deleting a student should only delete **join rows** — which happens naturally when the owning-side collection entries go away with the owner.

**Bug 2 — collection semantics:** with `List` + bag semantics, removing one element makes Hibernate delete *all* join rows for that student and re-insert the remainder. Use `Set`, or accept regeneration for tiny collections. (Interestingly, adding to a bag is cheap; removal is where bags degrade.)

**When to abandon `@ManyToMany`:** the moment the relationship itself has data — `enrolledAt`, `grade` — you must promote the join table to an entity:

```java
@Entity
public class Enrollment {
    @EmbeddedId EnrollmentId id;
    @ManyToOne(fetch = LAZY) @MapsId("studentId") Student student;
    @ManyToOne(fetch = LAZY) @MapsId("courseId")  Course course;
    Instant enrolledAt;
    String grade;
}
```

Two `@ManyToOne`s + composite key (`@MapsId`). Now you can page enrollments, query them directly, and add columns — none of which `@ManyToMany` supports. Many teams ban `@ManyToMany` outright for this reason; join tables rarely stay attribute-free.

**Follow-up trap:** *"Default fetch types per association?"* — `@ManyToOne`/`@OneToOne` default **EAGER**; `@OneToMany`/`@ManyToMany` default **LAZY** (spec-defined). You must override the to-one defaults to LAZY explicitly — recite this cold.

---

### Q25. Explain each `CascadeType` with a concrete scenario where it's right — and one where `CascadeType.REMOVE` caused a production catastrophe. Where should cascades live in a well-designed aggregate?

**Answer:**

Cascades propagate persistence-context **operations** along associations:

- **PERSIST** — saving a new `Order` also persists its new `OrderItem`s. Right wherever children are created with the parent.
- **MERGE** — merging a detached parent graph merges children (typical in "edit form → save" flows).
- **REMOVE** — deleting parent deletes children. Only for **exclusive ownership** (Order → OrderItems). Catastrophe scenario: someone puts REMOVE (or ALL) on a shared reference like `Order → Customer` or on `@ManyToMany`; deleting one order deletes the customer (and possibly cascades further). Also: cascade REMOVE loads and deletes children **one by one** — deleting a parent with 100k children = 100k+ DELETE statements; prefer bulk deletes or DB `ON DELETE CASCADE` for volume.
- **REFRESH / DETACH** — propagate `em.refresh`/`em.detach` through the graph; rare, mostly long-running desktop-style contexts.
- **ALL** — shorthand for all five; only correct where the child cannot exist without the parent.

Design guidance (DDD framing scores well): cascades belong **within an aggregate boundary**, parent → strictly-owned children only — never across aggregates (Order→Customer), never upward (child→parent: a `cascade` on `@ManyToOne` like `OrderItem → Order` with REMOVE means deleting one item deletes the whole order!).

**Follow-up trap:** *"Is cascade a DB feature?"* — No: JPA cascades are in-memory operation propagation generating individual SQL; DB `ON DELETE CASCADE` is a constraint action. They can coexist but must be planned together (Hibernate `@OnDelete(action = CASCADE)` pushes it to DDL).

---

### Q26. `orphanRemoval = true` vs `CascadeType.REMOVE`: the interviewer gives you this: "I removed an item from `order.getItems()` and it was deleted from the DB. My colleague swears removing from a collection can't delete rows. Who's right, and what exactly is the difference?"

**Answer:**

Both can be right — it depends on `orphanRemoval`:

- **`CascadeType.REMOVE`** fires only when the **parent is removed**: `em.remove(order)` → children deleted. Merely detaching a child from the collection does nothing to the child row (FK just gets nulled if the relationship update is mapped, or nothing on the inverse side).
- **`orphanRemoval = true`** adds a stronger rule: a child **dereferenced** from its parent (removed from the collection, or the collection replaced) becomes an *orphan* and is **deleted**. So with orphanRemoval, `order.getItems().remove(item)` → `DELETE from order_item`.

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items;
```

Semantics: orphanRemoval expresses **exclusive, non-shareable ownership** — a child has no meaning outside this parent (Order/OrderItem, Post/Comment... on `@OneToMany` and `@OneToOne` only). REMOVE cascade expresses "when I die, my children die." Typically you want both (ALL + orphanRemoval).

Two classic follow-ups to preempt: (1) replacing the whole collection (`order.setItems(newList)`) with orphanRemoval deletes everything not re-referenced — and Hibernate hates the collection *instance* being swapped (`A collection with cascade="all-delete-orphan" was no longer referenced…` — mutate the existing collection with `clear()`/`addAll()`, never reassign); (2) moving a child between parents with orphanRemoval is unreliable (may delete rather than re-parent) — remove+flush or redesign.

**Follow-up trap:** *"orphanRemoval without cascade REMOVE?"* — legal but odd; orphan deletion works on dereference, but parent deletion wouldn't cascade — usually a smell.

---

### Q27. You call `parent.getChildren().size()` just to check emptiness and see a massive SELECT loading 5,000 children. What are your options to get counts/pages of a child collection without loading it — and what does `@LazyCollection(EXTRA)` / `@Size` do here?

**Answer:**

A lazy collection is **all-or-nothing** by default: *any* access — `size()`, `isEmpty()`, `contains()` — triggers full initialization (one SELECT for all 5,000 rows). Options, best first:

1. **Query instead of navigate** — the professional default:

```java
long count = itemRepository.countByOrderId(orderId);
boolean any = itemRepository.existsByOrderId(orderId);
Page<OrderItem> page = itemRepository.findByOrderId(orderId, pageable);
```

Collections on entities are for object-graph *writing* convenience (cascade/orphan tracking); reading at scale belongs to repository queries with paging.

2. **Hibernate "extra lazy"** (`@LazyCollection(LazyCollectionOption.EXTRA)`, or property-level in Hibernate 6 where the annotation is deprecated): `size()` issues `SELECT COUNT(*)`, indexed access fetches one row — without initializing the whole collection. Sounds ideal, but it silently encourages N+1-style chatty access patterns; treat it as a niche tool.
3. **Map a formula-derived count**: Hibernate `@Formula("(select count(*) from order_item i where i.order_id = id)")` on a read-only field — computed per load; fine for small screens, hidden cost per row otherwise.

Never `@OneToMany(fetch = EAGER)` to "fix" this — that pessimizes every load of the parent everywhere.

**Follow-up trap:** *"Can you paginate a mapped collection itself?"* — Not through the entity graph; pagination requires a query (`Pageable` on a repository method). This is a core reason to avoid navigating huge collections.

---

### Q28. An interviewer sketches `Employee` with a `manager` (self-reference) and asks you to map an arbitrary-depth org tree, then asks how you'd fetch a whole subtree efficiently.

**Answer:**

Mapping is a plain self-referencing association — same table, `@ManyToOne` to itself:

```java
@Entity
public class Employee {
    @Id @GeneratedValue Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager")
    private List<Employee> reports = new ArrayList<>();
}
```

Root nodes have `manager_id NULL`. The interesting part is **fetching a subtree**, because naive navigation (`getReports()` recursively) causes **one query per node per level** — N+1 compounded by depth.

Options to present:

1. **Recursive CTE via native query** — the correct scalable answer on PostgreSQL/modern MySQL/Oracle:

```java
@Query(value = """
    WITH RECURSIVE subtree AS (
      SELECT * FROM employee WHERE id = :rootId
      UNION ALL
      SELECT e.* FROM employee e JOIN subtree s ON e.manager_id = s.id)
    SELECT * FROM subtree""", nativeQuery = true)
List<Employee> findSubtree(@Param("rootId") Long rootId);
```

One round-trip for the whole subtree; reassemble parent/child links in memory if needed.

2. **Fetch-all-and-build** — for small bounded trees (org charts, category trees): load all rows in one query, build the tree in a `Map<Long, Employee>` pass. Simple and often best.
3. **Materialized path / closure table** — schema-level designs when subtree queries dominate (path `LIKE 'root.a.%'`, or an ancestor-descendant link table). Mentioning these shows you know when the *model*, not the ORM, is the fix.

JPQL has no recursion — say that explicitly; depth-generic traversal forces native SQL or schema design.

**Follow-up trap:** *"Cascade REMOVE on `reports`?"* — deleting a manager would recursively delete the whole subtree, one row at a time. Usually wrong; re-parent or restrict instead.

---

### Q29. You set `@OneToOne(fetch = FetchType.LAZY, mappedBy = "user")` for `User → UserProfile`, but the profile still loads eagerly with every user. Why is lazy one-to-one broken on the inverse side, and what are the working patterns?

**Answer:**

Lazy loading works by putting a **proxy** in the field. For a proxy, Hibernate must know *at parent-load time* whether the association is null or not (a proxy can't later "become null"). On the **owning side** the FK column is right there in the parent's row — null FK → null field, else proxy. But on the **inverse side** (`mappedBy`), the FK lives in the *other* table — the only way to know if a profile exists is to **query it**. So Hibernate resolves the association immediately: your `LAZY` is silently ignored and each User load fires an extra `SELECT` on user_profile (classic hidden N+1 on list screens).

Working patterns:

1. **Shared primary key with `@MapsId`** (best): child's PK **is** the FK.

```java
@Entity
public class UserProfile {
    @Id Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;
}
```

Navigate only from profile→user (owning, truly lazy). From user→profile, don't map it — load via `profileRepository.findById(userId)` (same PK!). No extra column, no extra query on user loads.

2. Keep the mapping only on the owning side (child holds FK + unique constraint), and query the child by FK when needed.
3. **Bytecode enhancement** (`hibernate.enhancer.lazyInitialization`) can make inverse one-to-one lazy — the "it depends on build setup" answer; don't rely on it by default.
4. Accept EAGER but always join-fetch it in list queries.

**Follow-up trap:** *"Why doesn't `optional = false` fix it?"* — `@OneToOne(mappedBy=…, optional = false)` promises Hibernate a value always exists so a proxy is safe — it *can* enable lazy, but if the promise is ever false you get broken behavior; it's a fragile contract, `@MapsId` is the robust design.

---

### Q30. Serialization horror: your controller returns an `Order` entity; Jackson throws `InfiniteRecursion (StackOverflowError)` — or, after "fixing" lazy loading, users see huge payloads with the entire object graph. Untangle the bidirectional-serialization problem and give the professional solution.

**Answer:**

Cause: bidirectional associations form cycles (`Order → items → order → items…`). Jackson happily follows getters forever → `StackOverflowError`. Band-aids people try, and their costs:

- `@JsonIgnore` on one side — works, but couples persistence model to API and permanently hides the field.
- `@JsonManagedReference` / `@JsonBackReference` — breaks the cycle (back side omitted), still couples layers.
- `@JsonIdentityInfo` — serializes repeated objects as ids; produces payloads clients rarely expect.
- On top: serializing entities makes Jackson touch **lazy proxies** → `LazyInitializationException` outside a transaction, or with OSIV enabled (Q40), accidental query storms while rendering JSON.

**Professional answer: never serialize entities.** Map to DTOs at the service boundary:

```java
public record OrderDto(Long id, String status, List<OrderItemDto> items) {
    static OrderDto from(Order o) {
        return new OrderDto(o.getId(), o.getStatus().name(),
            o.getItems().stream().map(OrderItemDto::from).toList());
    }
}
```

DTOs are cycle-free by construction, decouple API shape from schema (columns can change without breaking clients), let you exclude sensitive fields, and force you to *decide* what's loaded (pairs with fetch strategies — you join-fetch exactly what the DTO needs, Q34). MapStruct is the standard tooling answer for mapping at scale.

**Follow-up trap:** *"Isn't DTO mapping boilerplate?"* — yes, and it's the cheapest boilerplate you'll ever buy: it eliminates whole bug classes (recursion, lazy-init at render, over-exposure) and is the precondition for fixing fetch performance properly.

---

# Section 4 — Fetching Strategies, Lazy Loading & the N+1 Problem (Q31–Q40)

---

### Q31. A dashboard endpoint got slow after a "harmless" change. SQL logs show 1 query for 50 orders, then 50 more queries for each order's customer. Name the problem, explain precisely why it happens, and list every fix you know ranked by preference.

**Answer:**

The **N+1 select problem**: one query fetches N parent rows, then accessing a lazy association on each row triggers N additional queries (here `@ManyToOne Customer` accessed per order — note N+1 hits EAGER to-ones loaded via *query* results too, since JPQL doesn't join-fetch EAGER associations automatically; it fetches them with secondary selects).

Why: lazy associations are proxies initialized on first access; nothing batches those initializations by default. Each `order.getCustomer().getName()` = one `SELECT … WHERE id = ?`.

Fixes, ranked:

1. **`JOIN FETCH` in JPQL** — one query, association populated:

```java
@Query("select o from Order o join fetch o.customer where o.status = :status")
List<Order> findWithCustomer(@Param("status") Status status);
```

2. **`@EntityGraph`** — declarative, works with derived queries and `Pageable` (Q33):

```java
@EntityGraph(attributePaths = {"customer"})
List<Order> findByStatus(Status status);
```

3. **DTO projection** — select exactly the columns the screen needs; no entities, no lazy anything (often the true best answer for read endpoints).
4. **`@BatchSize` / `hibernate.default_batch_fetch_size`** — doesn't remove the extra queries but collapses N into N/size using `IN (…)`; excellent *global safety net* (Q36).
5. Hibernate 6 alternatives: `@Fetch(FetchMode.SUBSELECT)` for collections — loads all collections for all loaded parents in one subselect query.

And the meta-answer: **detect** it in tests — assert statement counts (e.g., Hibernate statistics or a datasource proxy library) so N+1 regressions fail CI instead of prod.

**Follow-up trap:** *"Would `FetchType.EAGER` fix it?"* — No; EAGER on a to-one still N+1s through JPQL queries (secondary selects), and it pessimizes every other load path. EAGER is a global setting for a per-query problem — that phrasing lands well.

---

### Q32. The interviewer asks: "`join fetch` vs plain `join` in JPQL — your query `select o from Order o join o.items i where i.sku = :sku` still lazy-loads items later. Why? And why does `select distinct` show up in so many join-fetch queries?"

**Answer:**

A plain **`join`** exists only for *filtering/navigation in the WHERE clause* — it does not populate the association. Your query joins order_item to filter orders by sku, but the `items` collection on returned orders remains an uninitialized lazy proxy; touching it later = more queries. **`join fetch`** additionally selects the joined columns and initializes the association in the result graph.

The `distinct` story: fetching a collection multiplies parent rows in the JDBC result set (an order with 5 items appears 5 times), so Hibernate would hand you duplicate `Order` references. `select distinct o … join fetch o.items` de-duplicates the *entities*. Historically `DISTINCT` also went into the SQL (useless work — the DB de-duplicates rows that are already distinct by item columns, and it can force a sort); the classic fix was the query hint `hibernate.query.passDistinctThrough = false`. **Hibernate 6 fixed this**: entity de-duplication is automatic for join-fetch results and DISTINCT is no longer passed down needlessly — knowing the version difference is a strong signal.

Also state the constraints of join-fetching collections: it breaks DB-side pagination (Q33) and you can't fetch two bag collections in one query (Q35).

**Follow-up trap:** *"Fetch join with `on` conditions?"* — JPA forbids restricting a fetch join with extra `on` clauses (the fetched association must be complete — a partially-loaded collection would be a lie in the persistence context); Hibernate technically allows some of it, but filtered fetch joins are a design smell: use a DTO query.

---

### Q33. You add `Pageable` to a `join fetch` query on a collection and the log shows `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`. What is happening, why is it dangerous, and what are correct pagination strategies with collections?

**Answer:**

DB-side pagination (`LIMIT/OFFSET`) operates on **rows**, but a collection join fetch multiplies rows per parent — `LIMIT 20` might cut through the middle of one order's items and return 7 orders instead of 20, with truncated collections. Hibernate refuses to produce wrong results, so it **fetches the ENTIRE result set and paginates in memory**. On a big table that's an OOM/latency bomb hiding behind one warning line.

Correct strategies:

1. **Two-query pattern** (standard): page over **ids** first, then fetch full graphs for those ids:

```java
Page<Long> idPage = orderRepo.findIdsByStatus(status, pageable);        // paginated, no join
List<Order> orders = orderRepo.findWithItemsByIdIn(idPage.getContent()); // join fetch, no paging
// reorder to match idPage order, wrap back into PageImpl
```

2. **Paginate the parent without the collection**, then let `@BatchSize` load the collections for the page's 20 parents in ~1 extra `IN` query — often simplest and plenty fast.
3. **Flip the query**: page over the *child* side (`select i from OrderItem i join fetch i.order`) when the screen is really about items — to-one fetch joins don't multiply rows, pagination stays in SQL.
4. **DTO projection with explicit paging semantics** — page parents, then a second grouped query for children DTOs.

Never ship the in-memory version; treat the HHH90003004 warning as a build-breaker (you can even set `hibernate.query.fail_on_pagination_over_collection_fetch=true` — turning the warning into an exception; that's a memorable pro tip).

**Follow-up trap:** *"Does `@EntityGraph` + `Pageable` have the same problem?"* — Yes for collection attributes: same row multiplication, same in-memory pagination. The mechanism (fetch join) is the same under the hood.

---

### Q34. Explain `@EntityGraph` properly: fetch vs load semantics, defining graphs on the entity vs `attributePaths`, subgraphs for nested associations — and when you'd choose it over `join fetch`.

**Answer:**

An entity graph is a **runtime fetch plan** — it overrides static `FetchType` per query.

- **`fetch` semantics** (`jakarta.persistence.fetchgraph`): attributes **in** the graph are EAGER; everything else is treated LAZY — even statically-EAGER fields (in principle; Hibernate historically still eagers some basics — the spec-vs-Hibernate nuance is a bonus point).
- **`load` semantics** (`jakarta.persistence.loadgraph`, Spring Data's default `EntityGraphType.LOAD`): graph attributes are EAGER **in addition to** whatever is statically EAGER.

Usage in Spring Data:

```java
// ad-hoc
@EntityGraph(attributePaths = {"customer", "items.product"})   // nested via dot-path
Page<Order> findByStatus(Status status, Pageable pageable);

// named graph defined on the entity
@NamedEntityGraph(name = "Order.full",
    attributeNodes = {
        @NamedAttributeNode("customer"),
        @NamedAttributeNode(value = "items", subgraph = "items-sub")},
    subgraphs = @NamedSubgraph(name = "items-sub",
        attributeNodes = @NamedAttributeNode("product")))
@Entity public class Order { … }

@EntityGraph("Order.full")
Optional<Order> findWithGraphById(Long id);
```

**When `@EntityGraph` over `join fetch`:** it composes with **derived queries** (no need to hand-write JPQL), works on `findById`-style lookups, and the same query method can be exposed with different graphs (multiple methods or dynamic `EntityGraph` via the `EntityManager`/hints) — one query, several fetch shapes. `join fetch` wins when you also need the join for filtering, or want full explicit control in one JPQL string. Both share the collection-pagination limitation (Q33) and the multiple-bags limitation (Q35) since both compile to fetch joins.

**Follow-up trap:** *"Can an entity graph make an EAGER field lazy?"* — With fetchgraph semantics, spec-wise yes (omitted = lazy); in practice Hibernate's conformance has caveats — the reliable direction is lazy→eager. Design entities LAZY-everywhere and use graphs to opt *in*.

---

### Q35. You add a second `join fetch` to a query and get `MultipleBagFetchException: cannot simultaneously fetch multiple bags`. Explain the exception, why Sets "fix" it, why that fix is dubious, and the right patterns.

**Answer:**

A **bag** = `java.util.List` mapped without `@OrderColumn` — unordered, duplicates allowed, no index. Fetch-joining **two bag collections at once** produces a cartesian product (parent × items × tags); Hibernate cannot tell original duplicates from join-induced ones in two unindexed lists, so it throws `MultipleBagFetchException` at query creation.

Why switching both to `Set` "works": with sets, Hibernate can de-duplicate deterministically (per-row identity), so the query runs. Why it's dubious: **the cartesian product still happens in SQL** — 100 items × 100 tags = 10,000 rows per order transferred and de-duplicated in memory. You silenced the exception, kept the performance problem. (Same criticism applies to `@OrderColumn`-ing the lists.)

Right patterns:

1. **One collection fetch per query** — split into multiple queries in the same transaction/persistence context; Hibernate stitches results onto the same managed instances:

```java
List<Order> orders = repo.findWithItems(ids);   // join fetch o.items
repo.findWithTags(ids);                          // join fetch o.tags — same entities get tags filled
```

2. **`@BatchSize`/subselect** for the second (and third…) collection — fetch join the dominant one, batch-load the rest.
3. **DTO queries** per collection, merged in the service.

Multiplicative row growth only comes from *sibling* collection fetches; chains (order → items → product) are fine because to-one fetches don't multiply.

**Follow-up trap:** *"Why doesn't this affect two `@ManyToOne` fetch joins?"* — to-one joins add columns, not rows; no product, no ambiguity.

---

### Q36. Your team sets `spring.jpa.properties.hibernate.default_batch_fetch_size=100` globally and most N+1 hotspots melt away. Explain exactly what batch fetching does, its SQL shape, `@BatchSize` vs the global setting vs `@Fetch(SUBSELECT)`, and the residual costs.

**Answer:**

Batch fetching changes **how lazy initialization happens**: when one proxy/collection is touched, Hibernate looks at the persistence context for up to `batch_size` *other* uninitialized proxies of the same association and loads them together:

```sql
-- instead of 50 ×: select * from customer where id = ?
select * from customer where id in (?, ?, ?, … 50 ids …)
```

So 1+N becomes 1 + ceil(N/size). It applies to both to-one proxies and collections (`… where order_id in (…)`).

- **Global** `hibernate.default_batch_fetch_size` — applies everywhere; the widely-recommended safety net (values 16–100 typical). Modern Hibernate pads/aligns IN-lists to reduce statement-cache pollution.
- **`@BatchSize(size = …)`** — per-entity or per-collection override.
- **`@Fetch(FetchMode.SUBSELECT)`** (collections only) — instead of IN(ids), re-runs the *original query* as a subselect to load the collections of **all** parents fetched by that query in one go: `where order_id in (select id from order where <original criteria>)`. Great when you'll touch the collection for every loaded parent; risky if the original query was huge or non-repeatable.

Residual costs to name: still extra round-trips (1 per batch); loads possibly-unneeded siblings (speculative); doesn't help the *first* query's shape (no joins, so you can't filter parents by child columns with it); and IN-list size interacts with DB limits (Oracle 1000) — Hibernate splits as needed. Batch fetching is a **mitigation of lazy access patterns**, not a substitute for designing the right fetch plan on hot paths (join fetch/DTO there).

**Follow-up trap:** *"Why is batch fetching considered the best default?"* — It converts pathological N+1 into bounded queries *without* anyone having to remember to fix each endpoint — resilience over heroics.

---

### Q37. Classic: `LazyInitializationException: could not initialize proxy – no Session`. Show three different real situations that produce it, and the correct fix for each (and the wrong fixes people reach for).

**Answer:**

The exception means: you touched an uninitialized proxy/collection after the persistence context (Session) that created it was closed.

**Situation 1 — access after the transaction, in the web layer:** controller returns an entity; Jackson serializes lazy fields after `@Transactional` service method ended (OSIV off). *Fix:* map to DTOs inside the transaction, fetching what the DTO needs (join fetch/EntityGraph). *Wrong fix:* turning OSIV on (hides the design flaw, Q40) or making fields EAGER (global pessimization).

**Situation 2 — detached entity reused later:** you cached an entity in a `@Cacheable` service method / HTTP session, then touched a lazy field in a later request. *Fix:* cache DTOs, or re-attach & re-read via repository in the new transaction. *Wrong fix:* `Hibernate.initialize()` sprinkled at cache-put time without thinking about payload size.

**Situation 3 — async/scheduled thread:** entity loaded in the request thread, handed to `@Async` worker; Session is thread-bound and gone. *Fix:* pass **ids** (or DTOs) across thread boundaries; the worker loads its own state in its own transaction. This is a hard rule: entities don't cross thread/transaction boundaries.

Legitimate targeted tools: `Hibernate.initialize(entity.getItems())` inside the transaction when you know downstream needs it; `@Transactional` extended to the full unit of work; fetch-joining in the original query. The theme to articulate: **LIE is a symptom of unclear transaction/fetch boundaries** — the fix is deciding *what* each use case loads, not keeping sessions open longer.

**Follow-up trap:** *"Why does `toString()`/debugger sometimes trigger it?"* — anything touching proxy state initializes it; even logging an entity can. Another reason to exclude associations from `toString`.

---

### Q38. The interviewer asks you to explain what a Hibernate proxy actually is: how `order.getCustomer()` can return a non-null object for a broken FK, why `instanceof`/`getClass()` checks misbehave, and the `getId()` subtlety.

**Answer:**

For a lazy `@ManyToOne`, Hibernate puts a runtime-generated **subclass instance** (ByteBuddy) in the field, holding only the id. First access of a non-id property triggers the SELECT and delegates to the loaded target.

Consequences that show real experience:

- **Broken FK:** proxy creation never checks existence — `getCustomer()` returns a proxy for id=42 even if row 42 was deleted (no FK constraint). You get `EntityNotFoundException` later at first property access. (`@NotFound(action = NotFoundAction.IGNORE)` makes Hibernate check-and-null instead — at the cost of an immediate eager select.)
- **`getClass()` lies:** it returns `Customer$HibernateProxy$…`. So `getClass() == other.getClass()` in `equals()` breaks proxy-vs-entity comparisons — the standard fix compares with `instanceof` or Hibernate's effective-class helpers (Hibernate 6: `Hibernate.getClass(obj)`) (Q59). `instanceof Customer` is true for the proxy (it *is* a subclass) — but `instanceof` against a **subclass in an inheritance hierarchy** fails: a proxy created for the base type `Payment` is never an `instanceof CardPayment` even if the row is one; polymorphic lazy to-ones are genuinely problematic.
- **`getId()` subtlety:** calling the id getter does **not** initialize the proxy (the id is known) — with standard property access. With field-access-style mapping quirks or non-getter access it can. That's why `order.getCustomer().getId()` is a free way to read the FK without a query.
- Two objects, one row: within one persistence context Hibernate guarantees *reference* uniqueness — but a proxy created first (via `getReferenceById`) stays a proxy even after `find` loads the real data; you may hold a proxy while another variable holds… the same proxy, actually. Equality by `==` across contexts, however, is never safe.

**Follow-up trap:** *"How to unwrap a proxy?"* — `Hibernate.unproxy(entity)` or via `LazyInitializer.getImplementation()`; needed before casting to subtypes.

---

### Q39. Someone "solved" a lazy problem by annotating the association `FetchType.EAGER`. Make the case against EAGER as a mapping-level strategy, including what EAGER does in `findById` vs JPQL queries — then name the rare cases where EAGER is defensible.

**Answer:**

The case against:

1. **EAGER is forever, for everyone.** Mapping-level fetch type applies to every load path in the application. One screen needed the customer; now the batch job, the health check, and every other query pay for it. You can't opt out per query (fetchgraph semantics aside, unreliably) — but you *can* opt IN to eager per query from a LAZY mapping. Asymmetry ⇒ LAZY everywhere is the only future-proof default.
2. **EAGER ≠ join, necessarily.** `em.find`/`findById` typically implements EAGER to-ones as an outer join — fine. But **JPQL queries do not honor EAGER via join**: they run your query as written, then fire **secondary selects** for each EAGER association not fetched → N+1 that you didn't write and can't see in the JPQL. EAGER *causes* N+1 rather than fixing it.
3. **EAGER chains explode:** Order→Customer→Company→Country… each EAGER hop drags more; collections EAGER-fetched multiply rows or add per-row selects.
4. It hides modeling decisions: with LAZY, each use case *states* its fetch plan (join fetch/graph/DTO) — reviewable, testable.

Defensible EAGER: tiny, always-needed, to-one reference data (e.g., an `@Embedded`-like lookup that's practically part of the row), especially when the entity is itself rarely queried through JPQL; or entities in a second-level-cache-heavy read model where "eager" hits cache. Even then, most seniors default LAZY + explicit fetch.

Remember to recite the defaults: to-one EAGER, to-many LAZY per spec — so the practical rule is "**write `fetch = FetchType.LAZY` on every `@ManyToOne`/`@OneToOne` you map**."

**Follow-up trap:** *"Can you make LAZY fail fast instead of surprise-query?"* — Yes: `hibernate.enable_lazy_load_no_trans` should stay **false** (its `true` mode silently opens temp sessions per access — an anti-feature), and you can catch stray initializations in tests via statement-count assertions.

---

### Q40. Spring Boot keeps Open Session in View **enabled by default**. Your architect wants it off; a teammate says "but then our controllers break." Explain what OSIV does, its real costs, and how to run cleanly with it disabled.

**Answer:**

**What it does:** `OpenEntityManagerInViewInterceptor` binds an `EntityManager` to the request thread for the **entire web request** — created before the controller, closed after view/JSON rendering. Transactions still begin/end at `@Transactional` boundaries, but the persistence context outlives them, so lazy loading during JSON serialization "just works" (each initialization runs in its own short DB interaction, auto-commit-ish, on the same EM).

**Real costs:**

- **Connection/session held across the whole request** — including remote calls, template rendering, slow clients. Under load this starves the connection pool (Hibernate does release/reacquire connections around transactions with default modes, but the session and its late lazy-loads still grab connections at render time — after your service "finished").
- **Queries fired from the view layer** — lazy loads triggered by serialization run *outside* any service transaction: no service-level control, silent N+1 storms in controllers/serializers that no one profiled, reads possibly inconsistent with the earlier transaction.
- It **hides fetch-design bugs**: code that would throw `LazyInitializationException` (a loud, early signal) instead degrades silently.

**Running with it off** (`spring.jpa.open-in-view=false` — Boot even logs a warning nudging you to decide):

1. Services return **DTOs**, fully assembled inside `@Transactional` boundaries.
2. Each use case declares its fetch plan (join fetch / `@EntityGraph` / projection).
3. Anything that still throws LIE is a *found bug* — fix the fetch plan, don't reopen the session.

Fair counterpoint to show balance: for small CRUD apps OSIV is a pragmatic convenience — the criticism is about scale, load, and discipline, not that it never works.

**Follow-up trap:** *"Does OSIV mean one transaction per request?"* — No. One *persistence context* per request; transactions are still demarcated by `@Transactional`. Entities stay managed between transactions within the request — which also means changes can flush unexpectedly if a second transaction starts. That nuance separates seniors from juniors.

---

# Section 5 — JPQL, Native Queries, Projections & @Modifying (Q41–Q50)

---

### Q41. Rapid-fire foundations: JPQL vs SQL — what does JPQL actually query, what can't it do, and how do positional vs named parameters and `@Param` fit in Spring Data?

**Answer:**

JPQL queries the **entity model**, not tables: `select o from Order o join o.customer c where c.vip = true` — names are entity/property names (case-sensitive), joins follow *mapped associations* (no ad-hoc join conditions pre-JPA-2.1; since 2.1 `join … on` adds extra conditions, and Hibernate allows joining unrelated entities with `on`). The provider translates JPQL to dialect-specific SQL, giving portability and startup validation (Q10).

JPQL can't (portably) do: recursive CTEs, window functions, DB-specific functions/hints, `INSERT … VALUES` (only `insert … select` in HQL), vendor JSON operators, `LIMIT` inside the string (use `setMaxResults`/`Pageable`/`Top…` keywords). Hibernate 6's HQL closes some gaps (CTEs, window functions in recent versions) — but then you're writing HQL, not portable JPQL; say so.

Parameters:

```java
@Query("select u from User u where u.email = :email and u.status = :status")
Optional<User> find(@Param("email") String email, @Param("status") Status status);
```

- Named params (`:email`) — order-independent, readable; `@Param` needed unless compiled with `-parameters` (Spring Boot does this by default now — worth knowing).
- Positional (`?1`) — terse, brittle on refactor; avoid.
- **Never concatenate values into query strings** — that's SQL/JPQL injection; parameters are also required for statement-cache reuse.

**Follow-up trap:** *"`select o` vs `select o.customer.name`?"* — the latter is a scalar/projection query returning `String`s, and the implicit path join `o.customer` becomes an **inner** join — silently dropping orders with null customer; use explicit `left join` when nullable paths are involved. That inner-join-by-implicit-path gotcha is a favorite.

---

### Q42. You need `WHERE status IN (:statuses)` with a list, optional filters ("if name is provided, filter by it"), and case-insensitive matching. Show idiomatic solutions in Spring Data — and where the "one clever JPQL" approach breaks down.

**Answer:**

**IN with a collection** — pass a `Collection` parameter, JPQL expands it:

```java
List<Order> findByStatusIn(Collection<Status> statuses);
// or @Query("… where o.status in :statuses")
```

Empty-list caution: `IN ()` is invalid SQL on several DBs — Hibernate 6 handles empty lists (generates a false predicate `1=0`), but guard in service code anyway for portability. Also: huge IN-lists blow DB limits (Oracle: 1000) and pollute statement caches — Hibernate's `in_clause_parameter_padding=true` mitigates cache churn.

**Case-insensitive** — derived: `findByNameContainingIgnoreCase(String part)`; JPQL: `where lower(u.name) like lower(concat('%', :part, '%'))`. Index note: `lower(name)` predicates need a functional index or citext-style column to avoid full scans — mentioning the index shows seniority. Beware `%term%` — leading wildcard defeats B-tree indexes regardless.

**Optional filters** — the "clever" JPQL:

```sql
where (:name is null or u.name like :name)
  and (:status is null or u.status = :status)
```

Works for 2–3 filters but breaks down: DB can't plan well (predicates always present), null-typing issues on some drivers (PostgreSQL `bytea` errors on untyped nulls), and it grows unreadably. The scalable answers are **Specifications** (Q91) or Criteria/Querydsl — build only the predicates that apply, so SQL contains only real conditions.

**Follow-up trap:** *"How do you escape user input used with LIKE?"* — `%`/`_` in user input act as wildcards; escape them (`escape '\'` clause) or you have a subtle search bug/DoS vector even with bind parameters.

---

### Q43. A bulk price update: `@Query("update Product p set p.price = p.price * 1.1 where p.category = :cat")`. It throws `QueryExecutionRequestException` until you add an annotation, and after it runs, an already-loaded Product in the same service still shows the old price. Explain everything happening here.

**Answer:**

**Why the exception:** DML-style JPQL (`update`/`delete`) must run via `executeUpdate()`, not `getResultList()`. In Spring Data you must mark the method `@Modifying` so the infrastructure calls the right execution path; it then returns `int`/`void` (rows affected). A transaction is required — `@Modifying` queries don't self-wrap; the service (or the repository method) needs `@Transactional`.

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Transactional
@Query("update Product p set p.price = p.price * 1.1 where p.category = :cat")
int raisePrices(@Param("cat") Category cat);
```

**Why the stale entity:** bulk JPQL bypasses the persistence context entirely — it compiles straight to SQL. Entities already in the first-level cache are untouched: your loaded `Product` still has the old price, and a subsequent `findById` **returns that stale managed instance** (first-level cache hit — no SQL!). Hence:

- `clearAutomatically = true` — clears the persistence context after the update so later reads reload from DB. Danger: it also discards **unflushed changes to other entities** — pending dirty state is lost; that's what `flushAutomatically = true` is for (flush pending changes *before* the bulk op).
- Also bypassed: `@Version` increments (unless you write `update versioned Product …` in HQL), lifecycle callbacks (`@PreUpdate`), cascades, `@LastModifiedDate` auditing, second-level cache entries (Hibernate does invalidate affected 2LC regions coarsely).

**When bulk beats per-entity:** thousands of rows, uniform change — 1 statement vs N updates + N dirty checks. When per-entity wins: per-row logic, callbacks/auditing must fire, optimistic locking must be honored.

**Follow-up trap:** *"Rows affected is 0 in a test but data looks right?"* — probably the update ran in a different transaction than the assert read, or flush ordering; also `@Modifying` + `deleteBy…` derived methods differ (Q6: load-then-delete vs single statement).

---

### Q44. When do you drop to a native query? Show one with pagination and explain the `countQuery` attribute, result mapping options (entity vs interface projection vs `SqlResultSetMapping`), and the startup-validation caveat.

**Answer:**

Drop to native when the SQL needs what JPQL/HQL can't express (portably): recursive CTEs (Q28), window functions (`row_number() over (partition by …)`), DB-specific ops (Postgres `jsonb`, full-text search, `ON CONFLICT` upserts), optimizer hints, or querying tables with no entity mapping.

```java
@Query(value = """
        select * from orders o
        where o.created_at > :since
        order by o.created_at desc""",
       countQuery = "select count(*) from orders o where o.created_at > :since",
       nativeQuery = true)
Page<Order> findRecent(@Param("since") Instant since, Pageable pageable);
```

Points to hit:

- **Pagination:** Spring Data appends dialect-specific LIMIT/OFFSET and needs a **`countQuery`** for `Page` (it can't reliably derive a count from arbitrary SQL). `Sort` on native queries is applied by naive string append — whitelist/sanitize any client-supplied sort (Q45's injection point).
- **Result mapping:** (a) map to an **entity** if you select all its columns (managed instance — careful: it participates in dirty checking); (b) **interface projection** — getters matched to result column *aliases* (`select o.id as id, c.name as customerName …`) — the pragmatic favorite; (c) JPA **`@SqlResultSetMapping`** + `@ConstructorResult` for class DTOs — powerful, verbose; (d) `Object[]`/`Tuple` — last resort.
- **Caveats:** no startup validation (Q10) — typos surface at first execution, so cover natives with `@DataJpaTest` against a real dialect (Testcontainers, Q98 — H2 may not even parse your Postgres SQL); no automatic flush guarantees before native reads (Q4); binds you to the dialect — isolate natives and document why each exists.

**Follow-up trap:** *"Can a native query return a lazy-loading entity graph?"* — it returns managed entities, whose lazy associations behave normally afterwards; but you can't `join fetch` in native SQL — combine with `@BatchSize` or follow-up queries for associations.

---

### Q45. The frontend sends `?sort=name,desc` and you pass it straight into a `PageRequest`. Security asks: "can sorting be an injection vector?" Also explain `Sort.by`, `JpaSort.unsafe`, `TypedSort`, and sorting by an aggregated/computed value.

**Answer:**

For **derived and JPQL queries**, `Sort` properties are validated against the entity metamodel — an unknown property throws `PropertyReferenceException`, so plain property sorts are injection-safe (still DoS-relevant: sorting an unindexed column on a huge table — whitelist sortable fields anyway).

The dangerous corners:

- **`JpaSort.unsafe("length(name)")`** — deliberately bypasses validation to allow function expressions; anything client-controlled must never reach it (string lands in the ORDER BY).
- **Native queries + `Sort`** — Spring Data appends the sort string with minimal handling; treat any user-supplied sort on a native query as an injection risk. Map allowed sort keys → hardcoded column expressions:

```java
Map<String, String> allowed = Map.of("name", "u.name", "created", "u.created_at");
```

Idioms to name:

```java
Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("name").ignoreCase());
Sort.TypedSort<User> u = Sort.sort(User.class);
Sort typed = u.by(User::getLastname).ascending();      // refactor-safe, typo-proof
```

**Sorting by computed/aggregated values:** for `@Query` with aliases, sort by the alias (`order by cnt` after `select count(i) as cnt`) — Spring Data can reference aliases from `Sort` for JPQL; alternatively bake `order by` into the query and expose separate methods, or use `JpaSort.unsafe` with server-controlled expressions only. Also mention `@OrderBy("position asc")` on collection mappings for default in-graph ordering.

**Follow-up trap:** *"`Pageable.unpaged()`?"* — a real `Pageable` that disables paging; useful for reusing paged repository APIs in exports; combine with streaming for big data.

---

### Q46. Interface-based projections: you return `interface UserSummary { String getName(); AddressView getAddress(); }` from a repository. Explain closed vs open projections, how nesting affects the SQL, `@Value` SpEL projections, and the performance trap hidden in open/nested projections.

**Answer:**

- **Closed projection** — interface getters correspond exactly to entity properties. Spring Data sees only known properties are needed and **restricts the SELECT** to those columns:

```java
interface UserSummary { String getName(); String getEmail(); }
List<UserSummary> findByActiveTrue();   // select u.name, u.email from user u …
```

Backed by a lightweight map-based proxy per row.

- **Open projection** — a getter with `@Value("#{target.firstname + ' ' + target.lastname}")` SpEL. Because Spring can't know which properties SpEL touches, it must **load the entire entity** and evaluate per row — you lose the column-restriction benefit entirely (per-row SpEL is also slow). Default methods on the interface (Java 8+) computing from other getters keep the projection closed — prefer them.
- **Nested projections** (`AddressView getAddress()`): supported, but for a nested *interface* projection of an association, Spring Data generally **fetches the full root entity (and association) and projects in memory** — the SQL optimization is gone; only flat, closed projections reliably narrow columns. That's the hidden trap: a "projection" that's secretly a full entity load + N+1 on the association.

For truly minimal SQL over joins, use a **flattened closed projection** (`String getAddressCity()` — resolves the path) or a class/constructor projection (Q47). Projections also compose with dynamic typing:

```java
<T> List<T> findByStatus(Status s, Class<T> type);   // dynamic projections
```

— one method serving entity, summary, or detail views.

**Follow-up trap:** *"Are projection proxies entities? Can you save them?"* — No; they're read-only views, not managed, no dirty checking. Round-tripping edits requires loading the entity.

---

### Q47. Class-based (constructor) projections: show the `select new` idiom and a records-based variant, compare with interface projections, and explain why DTO projections are the end-game for read performance.

**Answer:**

```java
public record OrderSummary(Long id, String customerName, BigDecimal total) {}

@Query("""
   select new com.acme.dto.OrderSummary(o.id, c.name, sum(i.price * i.quantity))
   from Order o join o.customer c join o.items i
   group by o.id, c.name""")
List<OrderSummary> summarize();
```

`select new` invokes the constructor per row — works with records (canonical constructor). Spring Data can also infer constructor projections **without** `select new` for derived queries when the DTO's constructor parameter names match entity properties (and Hibernate 6.3+/Spring Data allow dropping the FQCN in `select new`). Sorting/paging compose normally.

**Vs interface projections:** class-based = real immutable objects (no proxy indirection), can carry aggregations/expressions naturally, serialize cleanly, are debuggable; interface-based = less boilerplate, support dynamic projections easily. Both beat entities for reads.

**Why DTO projections are the read-path end-game:**

1. **Narrow SQL** — only needed columns; no LOBs, no 40-column drags (Q19).
2. **No persistence-context overhead** — no dirty-checking snapshots (big memory + flush-time CPU saving on large result sets), no accidental updates.
3. **No lazy landmines** — nothing to trip `LazyInitializationException` or N+1 later; the fetch plan is the query.
4. Aggregations join naturally (sum/count in the same row).

Limits: DTOs are flat per query — building nested DTO trees (order + list of item DTOs) takes two queries stitched in memory, or Hibernate 6-specific instantiation features/Blaze-Persistence Entity Views (name-dropping Blaze here is a strong senior signal).

**Follow-up trap:** *"So when do you still fetch entities?"* — When you intend to **modify** them (dirty checking is the point), need lifecycle/locking/caching semantics, or traverse further. Reads → DTO, writes → entities is the clean doctrine.

---

### Q48. `@NamedQuery` vs `@Query` vs named native queries — where does each live, when is each preferred, and how does Spring Data resolve which one backs a repository method?

**Answer:**

- **`@NamedQuery(name = "User.findByEmail", query = "…")`** — declared on the entity, compiled/validated once at startup, referenced by name. Spring Data auto-links a repository method `findByEmail` to the named query `User.findByEmail` (entity simple name + `.` + method name) if it exists.
- **`@Query`** — declared at the repository method. Same startup validation for JPQL. Wins on cohesion: the query lives with its access API rather than cluttering the entity. This is the **de-facto standard** in Spring projects.
- **`@NamedNativeQuery`** — named native SQL on the entity, pairs with `@SqlResultSetMapping` for DTO mapping; the classic (verbose) JPA-standard way to organize native queries.

**Resolution order for a repository method** (worth reciting): (1) `@Query` on the method wins; (2) else a matching named query; (3) else derived-query parsing from the method name. (Configurable via `@EnableJpaRepositories(queryLookupStrategy = CREATE | USE_DECLARED_QUERY | CREATE_IF_NOT_FOUND)` — default `CREATE_IF_NOT_FOUND`.)

When named queries still earn their place: sharing one query across multiple repositories/DAOs, JPA-vanilla codebases (no Spring Data), and pre-compiled named natives with result-set mappings. Otherwise `@Query` for locality. One more advantage of named queries historically cited — parse-once caching — is moot: Hibernate caches all compiled query plans (keyed by string) regardless (mention the **query plan cache** and its sizing when apps have many distinct query strings).

**Follow-up trap:** *"Property-file queries?"* — `META-INF/jpa-named-queries.properties` lets you externalize named queries — occasionally useful for ops-tunable SQL, mostly a legacy trick; knowing it exists is enough.

---

### Q49. SpEL in `@Query`: show `#{#entityName}` for generic base repositories and a use case injecting method arguments — plus the security posture on SpEL with user input.

**Answer:**

Spring Data evaluates SpEL inside `@Query` strings before handing JPQL to the provider:

- **`#{#entityName}`** — resolves to the JPQL entity name of the repository's domain type. This unlocks **generic base repositories**:

```java
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> extends JpaRepository<T, ID> {
    @Query("select e from #{#entityName} e where e.deleted = false")
    List<T> findAllActive();
}
```

Every concrete repository (`OrderRepository extends SoftDeleteRepository<Order, Long>`) inherits a correctly-typed query — impossible with plain JPQL, which needs a literal entity name.

- **Argument access** — `?#{#customer.id}`, or `:#{#filter.name}` referencing bean-style properties of a parameter; handy for passing a filter object instead of exploding parameters. Also available: `?#{principal}` / `:#{authentication.name}` **with the Spring Security Data extension** — row-level security in queries:

```java
@Query("select d from Document d where d.owner = ?#{authentication.name}")
List<Document> findMine();
```

**Security posture:** SpEL here evaluates *server-side context* (parameters, principal) — the *results* are bound as query parameters, so this isn't string concatenation into SQL. The rule remains: never build the SpEL/JPQL string itself from user input; expressions are fixed at compile time, user data flows only through parameters.

**Follow-up trap:** *"Other place `#entityName` helps?"* — with entities whose JPQL name ≠ class simple name (`@Entity(name = "Ord")`), hardcoded strings drift; SpEL stays correct.

---

### Q50. A method must return the top 5 newest users, another must check "is there at least one admin?", a third must fetch a random-ish first match. Cover the `First`/`Top`/`Distinct`/`exists` keyword family and their SQL, plus `findAll(Example.of(...))` (Query by Example) as the underrated option.

**Answer:**

**Limiting keywords** — `findFirst5ByOrderByCreatedAtDesc()`, `findTopByOrderByScoreDesc()` (top-1), optionally with `Pageable`/`Sort` interplay. SQL: dialect LIMIT/FETCH FIRST — done in DB, not memory. `findFirstBy…` without ordering is nondeterministic — always pair with `OrderBy` (interviewers love asking "first by *what*?"). Spring Data also has a `Limit` parameter type (`findByStatus(Status s, Limit.of(5))`) — newer, dynamic.

**`existsBy…`** — `existsByRoleAndActiveTrue(Role.ADMIN)` → optimized existence SQL (select a constant with limit 1), cheaper than `countBy… > 0` (which scans/aggregates). Same idea available via `count` only when you actually need the number.

**`Distinct`** — `findDistinctByItemsCategory(…)`: adds SQL DISTINCT; needed when a join over a collection multiplies root rows. Caveat: DISTINCT over all entity columns can force sorts; consider `exists` subquery style via Specifications for pure "has any child matching" semantics.

**Query by Example (QBE):**

```java
User probe = new User();
probe.setStatus(Status.ACTIVE);
probe.setCity("Pune");
ExampleMatcher m = ExampleMatcher.matching()
    .withIgnoreNullValues()
    .withMatcher("name", match -> match.contains().ignoreCase());
List<User> users = userRepository.findAll(Example.of(probe, m));
```

(requires `QueryByExampleExecutor`, which `JpaRepository` extends). Strengths: zero query code for dynamic filter forms. Limits (recite them): only exact/starts/contains-style string matching, **no ranges** (`>`/`between`), no OR-groups across different properties beyond matcher config, ignores associations beyond to-one equality — beyond that, Specifications (Q91).

**Follow-up trap:** *"`findTop5` combined with a `Pageable`?"* — the keyword caps the page size (effective limit = min of both); mostly avoid mixing to keep semantics obvious.

---

# Section 6 — Persistence Context & Entity Lifecycle (Q51–Q60)

---

### Q51. Whiteboard question: draw the entity lifecycle — transient, managed, detached, removed — and name every operation that moves an entity between states. Then: "which state does `repository.save()` actually handle?"

**Answer:**

Four states:

- **Transient (new):** `new User()` — no id association with a persistence context, unknown to the DB.
- **Managed (persistent):** tracked by the current persistence context; **any field change is automatically written at flush** (dirty checking) — no save call needed.
- **Detached:** was managed; its context closed (transaction ended, `em.detach`, `em.clear`, serialization crossing boundary). Changes are no longer tracked.
- **Removed:** scheduled for deletion; DELETE runs at flush.

Transitions: `persist()` transient→managed; `find()/query` DB→managed; `merge()` detached-*state*→(copied onto a) managed instance; `remove()` managed→removed; `detach()/clear()/close()` managed→detached; `refresh()` re-reads DB state into a managed entity, discarding in-memory changes.

`repository.save()` handles **both** transient and detached via the is-new check (Q8): `persist` for new, `merge` for existing. The subtle interview point: **for a managed entity, calling `save()` is unnecessary** — dirty checking already persists changes:

```java
@Transactional
public void rename(Long id, String name) {
    User u = repo.findById(id).orElseThrow();
    u.setName(name);          // that's it — UPDATE at commit
    // repo.save(u);          // redundant (merge of an already-managed entity is a no-op-ish)
}
```

Saying "in `@Transactional` service methods I usually don't call save for updates" — and explaining why — is one of the highest-signal statements in a JPA interview.

**Follow-up trap:** *"When is the redundant `save()` actually harmful?"* — mostly harmless for managed entities, but it can trigger `merge` semantics on detached ones accidentally (extra SELECT), and it misleads readers about where writes happen.

---

### Q52. Explain dirty checking mechanics: how does Hibernate know what changed, when does it check, what does it cost with 10,000 loaded entities, and what do `@DynamicUpdate` and read-only hints change?

**Answer:**

On load, Hibernate stores a **snapshot** of each entity's loaded state in the persistence context. At **flush**, it walks every managed entity, compares current field values against the snapshot (field-by-field), and schedules UPDATEs for differences. (With bytecode enhancement's dirty tracking, entities self-report changed fields — no full comparison; that's the optimization path.)

Costs at scale: 10,000 managed entities × dozens of fields = expensive flush cycles — and flush may run **before every query** (AUTO flush), so a loop that loads + queries repeatedly re-scans everything. Memory doubles-ish (entity + snapshot). Mitigations:

- **`@Transactional(readOnly = true)`** — Spring propagates a read-only hint: Hibernate sets `FlushMode.MANUAL` and **skips snapshot storage** (session-level read-only), slashing memory and flush cost for read paths. This is the concrete answer to "what does readOnly actually do?" (plus driver/DB-level read-only optimizations, and it can route to replicas with routing datasources).
- **DTO projections** — nothing managed at all (Q47).
- **`em.clear()` / detach processed batches** in long loops (Q57).
- Query-level: `hibernate.readOnly` query hint per query.

**`@DynamicUpdate`**: by default Hibernate UPDATEs **all columns** (static SQL reused for every update of that entity — statement-cache friendly); `@DynamicUpdate` generates per-execution SQL containing only changed columns. Use when: wide tables where updating all columns is costly, or column-level triggers/conditional replication; cost: no reusable prepared statement, per-update SQL generation. Similarly `@DynamicInsert` omits nulls on insert (lets DB defaults apply).

**Follow-up trap:** *"Does dirty checking detect changes made via field access vs getters?"* — it compares **state**, however you mutated it (reflection included). What it can't see: changes to objects it doesn't consider part of state (e.g., mutating a shared mutable `Date` — one more reason for immutable `java.time`).

---

### Q53. `persist()` vs `merge()` — the interviewer gives this bug: a controller deserializes a `User` from JSON (id set), the service calls `em.persist(user)` → `detached entity passed to persist`. Another path calls `merge` and silently **loses concurrent changes**. Explain both semantics precisely and the safe update pattern.

**Answer:**

- **`persist(e)`** — "make this *transient* instance managed and INSERT it." If `e` has an id that the context/provider believes corresponds to an existing row (e.g., detached with a generated id), Hibernate throws `PersistentObjectException / detached entity passed to persist` (surfacing via Spring as `InvalidDataAccessApiUsageException`). persist returns void and manages *the same instance*.
- **`merge(e)`** — "copy the state of `e` onto a managed instance with the same id (loading it if needed), and return **the managed copy**." The argument remains detached! Classic bug: `merge(user); user.setX(...)` — mutation on the detached original is lost; you must use the returned instance.

The **silent-overwrite hazard:** merge copies **every** field from the JSON-born object onto the row — fields the client didn't intend to change (or was never shown) get overwritten with stale or null values, clobbering concurrent updates and server-managed columns. JSON→entity→merge is an anti-pattern.

**Safe update pattern** — load-then-modify inside a transaction, mapping only intended fields:

```java
@Transactional
public UserDto update(Long id, UpdateUserRequest req) {
    User u = repo.findById(id).orElseThrow(NotFound::new);
    u.setName(req.name());              // explicit, field-by-field (or MapStruct @MappingTarget)
    u.setEmail(req.email());
    return UserDto.from(u);             // dirty checking flushes at commit
}
```

Combine with `@Version` optimistic locking (client echoes the version; mismatch → 409) for true lost-update protection (Q71–72).

**Follow-up trap:** *"What does `save()` do for the JSON-born object?"* — it routes to `merge` (id non-null) → the same clobbering hazard, plus an extra SELECT. `repository.save()` is not a "safe upsert."

---

### Q54. First-level cache: within one transaction you call `findById(1L)` three times and see only one SQL SELECT. But `findByEmail("x@y.com")` for the same row still hits the DB every time. Explain what the persistence context does and doesn't cache — and the identity guarantee.

**Answer:**

The persistence context **is** the first-level cache: a per-EntityManager (per-transaction, in Spring's default model) map of `(entityType, id) → managed instance`. Rules:

- **Lookups by id** (`em.find`/`findById`/proxy resolution) check the map first → repeated `findById(1L)` = one SELECT. It also guarantees **repeatable-read-of-objects at the entity level** regardless of DB isolation: once loaded, you get the same instance with its loaded state even if the DB row changed (a subtlety: application-level repeatable read, distinct from DB isolation).
- **Queries are not cached** (that's the *query cache*, a 2LC feature — Q84): `findByEmail` always executes SQL. *But* — and this is the senior nuance — when the SQL returns row id=1, Hibernate checks the context and **returns the already-managed instance**, discarding the fresh column values (unless refresh). So queries hit the DB yet still honor **identity**: within one persistence context, one row ⇒ one Java object (`==` holds). This identity guarantee is what makes dirty checking and graph consistency coherent.
- Scope: flushes don't clear it; `clear()`/`detach()`/close do. It's not shared across transactions/threads — no invalidation concerns, unlike 2LC.

Practical implications: chatty by-id access in a transaction is cheap; the context grows with every loaded row (memory in batch jobs — Q57); and stale-after-bulk-update effects (Q43) come precisely from this cache returning managed instances instead of re-reading rows.

**Follow-up trap:** *"Does `findById` hit the second-level cache too?"* — order: PC (L1) → L2 (if entity is cached) → DB. And `getReferenceById` may hit neither (proxy without SELECT).

---

### Q55. Flush is not commit: enumerate exactly when Hibernate flushes, what flush *order* Hibernate uses for queued actions, and a real bug caused by each of (a) AUTO flush before queries, (b) flush ordering with unique constraints.

**Answer:**

**When flush happens** (FlushMode.AUTO, the default): (1) before transaction **commit**; (2) **before executing a JPQL/HQL/Criteria query** whose queried tables overlap pending changes (Hibernate 6 checks affected tables; older behavior was more eager); (3) on explicit `em.flush()`. Native queries: Hibernate can't parse arbitrary SQL to know affected tables — by default it flushes conservatively before native queries in JPA-compliant mode, but synchronization can require hints (`addSynchronizedEntityClass`) — the safe mental model is "don't rely on auto-flush before native SQL" (Q4). `COMMIT` flush mode defers everything to commit; `MANUAL` only flushes when told (used by readOnly).

**Flush action order** — Hibernate executes queued actions in a **fixed order regardless of your call order**: all INSERTs (in entity-type dependency order), then UPDATEs, then collection deletions/updates/insertions... and DELETEs at the end. Two classic bugs:

**(a) AUTO-flush surprise:** in a loop you `persist` new rows then run a JPQL count for progress — each query triggers a flush of everything pending → performance collapse; or conversely with MANUAL/COMMIT mode, your query **doesn't see** rows you just persisted (read-your-own-writes broken). Fix: understand the mode; batch without interleaved queries.

**(b) Ordering vs unique constraint:** "rename A→temp, B→A" or delete-then-reinsert a row with the same unique key in one transaction:

```java
repo.delete(oldTag);          // queued DELETE …
repo.save(new Tag("java"));   // queued INSERT — executed BEFORE the delete!
```

INSERT runs before DELETE → unique constraint violation, mystifying if you don't know the ordering. Fix: `repo.flush()` (or `saveAndFlush`) between the operations to force your sequencing.

**Follow-up trap:** *"Is data visible to other transactions after flush?"* — No: flushed ≠ committed; other transactions see it only after commit (isolation permitting). Flush just pushes SQL down your own connection.

---

### Q56. `em.refresh()`, `em.detach()`, `em.clear()`, and `getReference` vs `find` — give one production-grade use case for each of refresh/detach/clear, and the risks.

**Answer:**

- **`refresh(entity)`** — discard in-memory state, re-SELECT from DB (cascadable via `CascadeType.REFRESH`). Use case: after a DB trigger/default/function modified the row on insert/update (when not using `@Generated` mappings, Q18) — e.g., re-reading a DB-computed `search_vector` or audit columns; or reloading current state after catching an optimistic-lock failure before retry. Risk: silently throws away user changes; extra SELECT; refresh of a deleted row throws `EntityNotFoundException`.
- **`detach(entity)`** — evict one instance from the context. Use case: you're about to make **experimental mutations** you don't want auto-flushed (e.g., building a "preview" of changes), or evicting one huge entity (LOB) after use in a long-lived context. Risk: subsequent lazy access on it → `LazyInitializationException`; later `save` becomes a merge with clobbering semantics (Q53).
- **`clear()`** — evict everything (pairs with `flush()` in batch loops — Q57). Risk: all previously-loaded references become detached — any held reference elsewhere in the call stack is now a landmine; unflushed changes are **lost** (hence flush first).
- **`getReference` vs `find`** — covered in Q5, but at the EM level: `getReference` = proxy, no SQL, `EntityNotFoundException` deferred; `find` = SQL now (or L1/L2 hit), null if absent. Production-grade use: FK wiring and `em.remove(em.getReference(User.class, id))` — delete without loading.

Spring wrinkle worth stating: repositories don't expose these — you inject `@PersistenceContext EntityManager` (or add fragment methods, Q9) when you need lifecycle control; needing them *often* signals the design should move to DTOs/bulk ops.

**Follow-up trap:** *"Does `clear()` roll anything back?"* — No; already-flushed SQL stays flushed (and commits later). Clear only forgets *in-memory* state — people conflate it with rollback.

---

### Q57. Batch job: insert 1 million rows via `repository.save()` in a loop. It gets slower and slower and finally OOMs. Give the complete production checklist for JPA batch inserts — including the settings almost everyone forgets.

**Answer:**

Why it degrades: every saved entity stays **managed**; the persistence context grows to a million entries; dirty checking scans all of them at each flush; memory climbs until OOM.

**Checklist:**

1. **Enable JDBC batching** (off by default!):

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
# postgres: reWriteBatchedInserts=true on the JDBC URL for true multi-row inserts
```

`order_inserts/updates` group statements by table so batches don't break on interleaved entity types.

2. **Use SEQUENCE ids, not IDENTITY** — IDENTITY disables insert batching entirely (Q12). `allocationSize` aligned with batch size.
3. **Flush+clear in chunks** matching the batch size:

```java
for (int i = 0; i < items.size(); i++) {
    em.persist(items.get(i));
    if (i % 50 == 49) { em.flush(); em.clear(); }
}
```

(or `saveAll` per chunk with a fresh transaction per chunk — `saveAll` alone does NOT flush/clear.)

4. **Chunked transactions** — one mega-transaction holds locks/undo for the whole run and can't checkpoint; commit per chunk (Spring Batch exists for exactly this — name it).
5. Disable what you don't need per run: second-level cache for the batch, expensive listeners/auditing if acceptable.
6. Verify with logs/datasource-proxy that batching *actually* happens (`show_sql` doesn't show batching; use `datasource-proxy`/`p6spy` or Hibernate statistics `getEntityInsertCount` vs statement count) — the forgotten step; many "batched" jobs never batched.
7. Know when to leave JPA: for pure firehose loads, `JdbcTemplate.batchUpdate` or DB-native COPY beats ORM by an order of magnitude — saying this is senior judgment, not defeat.

**Follow-up trap:** *"Why does `saveAll` beat `save` in a loop, even without the above?"* — marginal: fewer transactional proxy invocations (one transaction), but no flush/clear/batching magic — it's not the fix people think it is.

---

### Q58. Read-only transactions and read-only entities: the interviewer pushes past "`readOnly=true` is a hint" — what does it concretely change in Spring + Hibernate + the driver, and what are `@Immutable` entities for?

**Answer:**

`@Transactional(readOnly = true)` concretely:

1. **Hibernate session flush mode → MANUAL**: no auto-flush before queries, no flush at commit — writes simply never happen (silently ignored, not errored — know that!).
2. **No dirty-checking snapshots**: Spring marks the Session read-only → loaded entities skip snapshot storage → lower memory, cheaper (skipped) flush cycles. This is the big one for read-heavy endpoints returning large graphs.
3. **JDBC `Connection.setReadOnly(true)`**: driver/DB-level optimizations (Postgres: read-only transaction state; MySQL replication drivers: can route to replicas). Also usable by routing datasources (`AbstractRoutingDataSource`/`LazyConnectionDataSourceProxy`) to split read/write traffic — a common architecture question chained onto this.
4. Spring may also skip some transaction synchronization overhead.

What it does **not** do: it's not a security boundary (native/JDBC writes through other channels aren't blocked by Hibernate's MANUAL mode; DB-level enforcement depends on the driver), and it doesn't change isolation.

**`@Immutable`** (Hibernate) on an entity/collection: Hibernate treats instances as never-changing — no snapshots, no dirty checks, updates *ignored/errored*, cache-friendly. Use for reference data (countries, currencies) and **entities mapped over views** (`@Immutable @Subselect` — mapping a SQL view/subquery as a read-only entity is a neat pattern to mention for reporting screens).

**Follow-up trap:** *"readOnly on a method that calls a write method?"* — with REQUIRED propagation the inner method joins the same read-only transaction → writes are silently dropped or fail depending on path; readOnly is fixed at transaction start and doesn't magically upgrade. This trips people in production.

---

### Q59. The `equals`/`hashCode` question — every JPA interview's favorite: why is `@Id`-based equality broken for new entities, why is Lombok's default broken, and give a correct implementation with justification (HashSet-before-persist scenario included).

**Answer:**

Requirements colliding: (1) `equals`/`hashCode` must be **stable across the entity's lifetime** (esp. while sitting in a `HashSet`/`HashMap`); (2) entities get their id **late** (at persist for IDENTITY, at flush time visibility); (3) proxies mean `getClass()` comparisons lie (Q38).

- **All-fields equality (Lombok `@Data`)**: any field change moves the object to a different hash bucket → `set.contains(e)` false for an element *in* the set; also triggers lazy loads. Broken.
- **Naive id equality (`Objects.equals(id, other.id)`)**: two *different* transient entities both have id=null → equal! Put two new entities in a `HashSet`, only one survives... and worse: hashCode from id **changes when id is assigned** at persist → add-to-set-then-persist corrupts the set. Broken for pre-persist collections.

**Correct pattern** (the "JPA-safe" idiom):

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Order other)) return false;      // instanceof: proxy-safe
    return id != null && id.equals(other.getId());       // null id ⇒ never equal
}

@Override
public int hashCode() {
    return getClass().hashCode();                        // CONSTANT per class
}
```

Justification: transient entities are equal only by reference (id null → not equal) — correct, they're distinct until persisted; once persisted, id equality works across contexts/detachment. The **constant hashCode** keeps hash-collection membership stable across id assignment — the cost (all instances share a bucket) is irrelevant for the small collections entities live in, and you say that trade-off out loud. Use getters (`other.getId()`) so proxies initialize/delegate correctly.

Gold-standard alternative: equality on a **natural business key** (`isoCode`, `email`) when one truly exists and is immutable — then hashCode can use it safely.

**Follow-up trap:** *"Why `instanceof` and not `getClass()`?"* — a Hibernate proxy is a runtime subclass; `getClass()` inequality would make `proxy.equals(entity)` false for the same row. (Symmetry pedants: acceptable trade-off, entities aren't general value classes.)

---

### Q60. JPA lifecycle callbacks and entity listeners: `@PrePersist`, `@PostLoad` and friends — give real use cases, the rules/limits inside callbacks, and contrast with Hibernate event listeners and Spring Data's `AuditingEntityListener`/domain events.

**Answer:**

Callbacks: `@PrePersist`, `@PostPersist`, `@PreUpdate`, `@PostUpdate`, `@PreRemove`, `@PostRemove`, `@PostLoad` — either methods on the entity or on a class named in `@EntityListeners(...)`.

**Real use cases:** defaulting fields (`@PrePersist` set `createdAt`, generate UUID codes), invariant normalization (lowercase email), computing derived transient fields after load (`@PostLoad` — e.g., decrypt, or assemble a convenience field), publishing "entity changed" notifications (careful — see limits), `@PreRemove` guarding deletes ("throw if order is PAID").

**Rules & limits to recite:**

- Callbacks run at specific persistence-context moments: `@PreUpdate` fires at **flush**, and only if the entity is actually dirty; `@PrePersist` at persist call (or flush-cascade). Bulk JPQL update/delete **bypass all callbacks** (Q43).
- Inside a callback you must **not** use `EntityManager` or execute queries (spec: portability undefined; can deadlock the flush cycle or recurse), and shouldn't modify *relationships*; modifying the entity's own basic fields in `@PreUpdate/@PrePersist` is the accepted use.
- Exceptions thrown in `Pre*` callbacks abort the operation → transaction marked rollback-only.
- Listener classes can be Spring beans in Boot (injection works via SpringBeanContainer integration) — but keep them thin.

**Contrasts:** Spring Data's `AuditingEntityListener` is just an `@EntityListeners` implementation doing `@CreatedDate/@CreatedBy` (Q94). Hibernate's native event system (`PreInsertEventListener`, interceptors, `Integrator`) sits lower — sees *all* entities generically, can veto/mutate state; used by Envers and multi-tenant filters. **Spring Domain Events** (`@DomainEvents` on aggregate / `AbstractAggregateRoot.registerEvent()`) publish application events **when the repository saves** — pair with `@TransactionalEventListener(phase = AFTER_COMMIT)` so side effects (emails, messages) only fire on successful commit — the pattern to name for "how do you send a message only if the transaction commits?" (and mention the transactional-outbox pattern as the fully-reliable version).

**Follow-up trap:** *"Why did `@PreUpdate` not fire for my change?"* — the entity wasn't dirty (no-op change), the change went through a bulk query, or you modified after the final flush. All three come up in real debugging.

---

# Section 7 — Transactions in Depth (Q61–Q70)

---

### Q61. Explain how `@Transactional` actually works end-to-end in Spring: who starts the transaction, where the EntityManager comes from, and why the annotation "didn't work" on a private method and on a method called from the same class.

**Answer:**

`@Transactional` is implemented via **AOP proxies**. At startup, Spring wraps the bean in a proxy (JDK dynamic proxy for interfaces, CGLIB subclass otherwise). A call *through the proxy* hits `TransactionInterceptor`, which asks the `PlatformTransactionManager` (`JpaTransactionManager` here) to begin/join a transaction; that creates (or reuses) an **EntityManager bound to the thread** via `TransactionSynchronizationManager`. Your repository's injected EM is a *shared proxy* that resolves to this thread-bound EM per call. On normal return → commit (flush first); on rollback-triggering exception → rollback. Physical connection semantics come from the datasource/Hibernate integration.

The two classic failures:

- **Self-invocation:** `this.innerTransactionalMethod()` bypasses the proxy — the interceptor never runs, so no new transaction / no REQUIRES_NEW / no rollback behavior from the inner annotation. Fixes: move the method to another bean, inject the bean into itself (self-injection via `@Lazy`), use `TransactionTemplate` for programmatic control, or AspectJ weaving (mode = ASPECTJ) if you must. Naming `TransactionTemplate` is a strong practical answer:

```java
transactionTemplate.execute(status -> { …; return result; });
```

- **Non-public methods:** classic Spring proxy-based `@Transactional` applies to public methods (CGLIB can technically intercept protected, but Spring's rule is public; private is impossible — no override). Recent Spring 6 relaxed some of this, but the interview-safe statement: put `@Transactional` on public methods and don't call them via `this`.

Also: the annotation is inherited from class-level, interface annotations are fragile with CGLIB; and `@Transactional` on a repository interface method works because the repository *is* a proxy.

**Follow-up trap:** *"Are `SimpleJpaRepository` methods transactional by themselves?"* — Yes: the class is `@Transactional(readOnly = true)` with `@Transactional` overrides on save/delete. Hence single repository calls work without a service transaction — and multi-call *units* still need your own `@Transactional` for atomicity (each call commits separately otherwise!).

---

### Q62. A service method annotated `@Transactional` catches an exception from a failed insert, logs it, and continues — then at commit you get `UnexpectedRollbackException: Transaction silently marked as rollback-only`. Explain rollback rules, checked vs unchecked, `rollbackFor`, and why catching didn't help.

**Answer:**

**Default rollback rule:** Spring rolls back on `RuntimeException` and `Error`; **checked exceptions do NOT trigger rollback** (an EJB-inherited default that surprises everyone — a checked `IOException` thrown through a transactional method commits!). Override with `@Transactional(rollbackFor = Exception.class)` (common team-wide default) or `noRollbackFor` for specific business exceptions.

**Why catching didn't help:** the failed operation happened in an **inner** transactional method (or repository call) participating in the *same physical transaction* (REQUIRED). When the inner proxy saw the RuntimeException, it **marked the shared transaction rollback-only** before rethrowing. Your catch stopped the exception's propagation, but the flag stays: at outer commit, Spring sees rollback-only → throws `UnexpectedRollbackException`. Catch-and-continue can't "un-doom" a transaction.

Correct patterns for "tolerate partial failure":

1. Run the risky part in **`REQUIRES_NEW`** — its failure rolls back only the inner transaction; outer continues (Q64 caveats: separate connection, outer data not yet visible to it).
2. Validate first / make the operation not fail (check constraints up front).
3. Catch **outside** the transactional boundary.
4. For "save what you can" batch semantics: per-item transactions (`TransactionTemplate` per item).

Also mention: Hibernate itself may mark the session dead after certain exceptions (e.g., constraint violation at flush) — even without Spring's flag, continuing to use that persistence context is unsafe; the transaction is the unit of failure, not the statement.

**Follow-up trap:** *"Where does the rollback-only flag live?"* — on the transaction status held by the transaction manager / `TransactionSynchronizationManager` for the current transaction (JPA: `tx.setRollbackOnly()`); any participant can set it, nobody can clear it.

---

### Q63. Walk through every propagation type with a concrete scenario where it's the right choice — REQUIRED, REQUIRES_NEW, SUPPORTS, NOT_SUPPORTED, MANDATORY, NEVER, NESTED — and state which ones don't really work with JPA.

**Answer:**

- **REQUIRED** (default): join caller's transaction or start one. Right for: virtually all service methods — one business use case = one atomic unit.
- **REQUIRES_NEW**: suspend caller's transaction, start an independent one (new connection!). Right for: **audit/outbox writes that must survive business rollback** ("record the attempt even if the order fails"), sequence-like counters, per-item isolation in batch loops. Caveats in Q64.
- **SUPPORTS**: join if present, else run non-transactionally. Right for: read methods callable both inside flows and standalone where a transaction isn't worth it. Honest note: with JPA you usually want a transaction even for reads (consistent PC), so SUPPORTS is rare.
- **NOT_SUPPORTED**: suspend any transaction, run without. Right for: calling slow external systems / sending emails mid-flow without holding the DB transaction open — a real anti-long-transaction tool.
- **MANDATORY**: throw if no active transaction. Right for: defensive DAOs/internal steps that must never manage their own boundary — enforcing "the caller owns the unit of work."
- **NEVER**: throw if a transaction exists. Right for: guarding operations that must not run transactionally (rare; e.g., DDL-ish maintenance ops).
- **NESTED**: same transaction but with a **JDBC savepoint**; inner failure rolls back to the savepoint without dooming the outer. The JPA catch (must state it): **`JpaTransactionManager` does not support NESTED for JPA** — savepoint semantics don't compose with the persistence context/flush model; it works with `DataSourceTransactionManager` (plain JDBC). Interviewers use NESTED to see if you actually know this. So with JPA: NESTED → not available; the practical substitutes are REQUIRES_NEW (different semantics — truly separate) or restructuring.

Scenario answer template interviewers like: "audit log on failure → REQUIRES_NEW; email sending → NOT_SUPPORTED or after-commit listener; enforcing caller-owns-transaction → MANDATORY."

**Follow-up trap:** *"What happens to the suspended transaction during REQUIRES_NEW?"* — it sits open, holding its connection and locks, until the inner commits and it resumes → two connections per thread; under pool pressure this self-deadlocks (pool exhaustion) — a known production incident pattern.

---

### Q64. REQUIRES_NEW deep-dive: a service saves an `Order`, then calls `auditService.record(orderId)` marked REQUIRES_NEW, which reads the order — and finds nothing. Also, under load the app deadlocks with all threads waiting for connections. Explain both, and when REQUIRES_NEW is genuinely correct.

**Answer:**

**Bug 1 — invisibility:** the outer transaction hasn't committed; its INSERT is at best flushed but uncommitted. The REQUIRES_NEW transaction runs on a **separate connection**, and under READ_COMMITTED it cannot see uncommitted data → the audit read finds nothing (or worse, FK violation if it inserts a row referencing the uncommitted order). Fixes: pass the needed **data** (not ids to re-read) into the inner method; or don't use REQUIRES_NEW — use `@TransactionalEventListener(AFTER_COMMIT)` for post-commit actions; or accept that the audit row references data only after outer commit (deferred FK).

**Bug 2 — pool self-deadlock:** each thread inside REQUIRES_NEW holds **two** connections (suspended outer + inner). With pool size N and enough concurrent requests, all connections are held by outer transactions while every thread waits for a second connection → classic deadlock-by-exhaustion (HikariCP timeout storms). Mitigations: sizeable pool headroom, avoid REQUIRES_NEW on hot paths, keep inner work tiny, or move to after-commit listeners/outbox.

**When genuinely correct:** writes that must persist *regardless of business outcome* — audit/attempt logs, idempotency-key reservation, "poison message" markers in batch processing (record failure, roll back item, continue). Also intentional per-chunk commits in long jobs.

Also say: REQUIRES_NEW is **not** a nested transaction — outer rollback does not undo the inner commit, and inner commit happens even if outer later fails → design for the inconsistency (compensations/status flags).

**Follow-up trap:** *"REQUIRES_NEW method called via `this`?"* — self-invocation again (Q61): silently joins the outer transaction; audits vanish on rollback and nobody notices for months. Production story gold.

---

### Q65. Isolation levels through a JPA lens: define dirty read, non-repeatable read, phantom read; state what READ_COMMITTED vs REPEATABLE_READ actually give you on PostgreSQL vs MySQL; and explain why JPA apps mostly stay on READ_COMMITTED + optimistic locking.

**Answer:**

Anomalies: **dirty read** — seeing uncommitted data (allowed only at READ_UNCOMMITTED); **non-repeatable read** — re-reading a row within one transaction yields different data (another tx committed an update in between); **phantom read** — re-running a range query yields new/missing *rows*.

`@Transactional(isolation = Isolation.REPEATABLE_READ)` sets it on the JDBC connection for that transaction (default `DEFAULT` = DB default).

Engine realities (the part that separates readers of docs from users of DBs):

- **PostgreSQL:** READ_COMMITTED = per-*statement* snapshot (MVCC, no read locks). REPEATABLE_READ = per-*transaction* snapshot — actually prevents phantoms too (stronger than ANSI requires) and throws serialization errors on write conflicts (must retry). SERIALIZABLE = SSI, more retry-able aborts.
- **MySQL/InnoDB:** default is REPEATABLE_READ (consistent snapshot; phantoms largely prevented via next-key locking for locking reads). READ_COMMITTED there reduces gap-locking (fewer lock waits, different replication requirements).

Why JPA apps stay on **READ_COMMITTED + optimistic locking**: the persistence context already provides *object-level* repeatable reads within a transaction (Q54) — re-`find`s return the cached instance; and lost updates — the anomaly business logic actually suffers — are handled by `@Version` checks at write time (Q71), which scale (no long read locks) and work across *detached* request/response cycles where DB isolation can't help at all (the user thought for 5 minutes between read and write — no isolation level spans that). Escalate isolation only for genuine multi-read consistency needs (reports → snapshot/REPEATABLE_READ) or use explicit locking for targeted rows (Q73).

**Follow-up trap:** *"Does Hibernate change behavior per isolation?"* — No; Hibernate delegates isolation to the connection. The L1 cache can *mask* non-repeatable reads (returns managed instance) — consistency you observe is the union of DB snapshot + PC identity, and interviewers enjoy that subtlety.

---

### Q66. Where do transaction boundaries belong — controller, service, or repository? Defend the standard answer, then discuss transactions spanning remote calls (the "call payment API inside @Transactional" incident) and read-only boundaries for queries.

**Answer:**

**Service layer.** The transaction should wrap exactly one business use case: all-or-nothing across its repository operations, with the persistence context alive for the whole unit (lazy loading, dirty checking coherent).

- Repository-level-only transactions (the default `SimpleJpaRepository` ones) make each call atomic but the use case non-atomic — save order, crash, never save items → corrupt state.
- Controller-level transactions couple HTTP concerns to DB work: serialization time, request parsing, and *every remote call* would sit inside the transaction. That's the incident: calling a payment gateway (2–30s timeout) inside `@Transactional` holds a DB connection + row locks for the whole call; under a gateway slowdown the connection pool drains and the whole app (including unrelated endpoints) goes down. Remote I/O inside transactions is a top-3 real-world outage cause — structure as: transaction 1 (record intent, state=PENDING) → remote call, no transaction → transaction 2 (record outcome), with idempotency/compensation for the crash-between windows (saga thinking).

Read paths: `@Transactional(readOnly = true)` at the service method — consistent snapshot-ish reads, PC alive for mapping DTOs, flush-skip optimizations (Q58). Queries without any transaction work (JPA allows it; each query gets a short EM) but you lose consistency across multiple reads, lazy loading breaks after each call, and OSIV muddies who owns what (Q40).

Also worth saying: keep transactions **short and code-local** — no user-think-time, no file I/O, no messaging *sends* inside (use after-commit/outbox); and one `@Transactional` at the use-case entry, not sprinkled on every layer (inner REQUIRED joins are fine and harmless, but the *ownership* should be clear).

**Follow-up trap:** *"`@Transactional` on the class vs method?"* — class-level applies to all public methods (convenient for services), method-level overrides; put `readOnly=true` at class level for query services and override on the few writers — a tidy convention to cite.

---

### Q67. Chained question: "Your `@Transactional` method updated an entity, no exception, but the data never changed in the DB. Give me every root cause you can think of." (This is a real interview format — breadth probe.)

**Answer:**

Enumerate — each is a real incident class:

1. **Self-invocation** — the annotation never applied; method ran without a transaction; changes to a *detached-ish* short-EM entity were never flushed (Q61).
2. **Wrong annotation import** — `jakarta.transaction.Transactional` vs Spring's usually both work, but `@Transactional` from some other package/misconfigured weaving silently no-ops; or the class isn't a Spring bean (manual `new`).
3. **readOnly=true** somewhere in the call chain — MANUAL flush mode: modifications silently never flushed (Q58).
4. **Entity was detached** — you modified an object loaded in a previous transaction (cached in memory/HTTP session); dirty checking only tracks *managed* instances. No save/merge → no write.
5. **Exception swallowed + rollback-only** — something inside rolled back the transaction and someone caught the evidence (paired with `UnexpectedRollbackException` or even without, if TX manager quirks) (Q62).
6. **Bulk/native update elsewhere overwrote it** — your change committed, then an unsynchronized bulk statement clobbered the row (Q43); or a trigger reverted it.
7. **Second transaction lost-update** — concurrent overwrite without `@Version` (Q72): both committed, last write won.
8. **`@Modifying` without clear + stale read** — the data *did* change; you're *reading* a stale managed instance in the same context and concluding it didn't (Q43/Q54). Verify in a separate session/SQL client before debugging writes!
9. **Changes to a field marked `updatable = false`** (Q15) — silently excluded from UPDATE.
10. **Two transaction managers / wrong one** — multiple datasources: `@Transactional("otherTxManager")` bound your method to a manager whose EM isn't the one your repository used (Q99); flush happened nowhere.
11. **Test-specific:** `@Transactional` test rolled back at the end by design (Q97) — "works in prod, not in test" inverse case.

Interviewers score both breadth and diagnostic ordering (check the DB directly first, then logs of actual SQL, then boundaries).

**Follow-up trap:** *"How do you verify SQL actually went out?"* — not `show_sql` (formatted logging lies about batching/timing): datasource-proxy/p6spy logging or Hibernate `generate_statistics` + `Session` metrics; and check commit vs rollback in the tx logs.

---

### Q68. Programmatic transactions: when do you reach for `TransactionTemplate`/`PlatformTransactionManager` instead of `@Transactional`? Show chunked commits in a loop and explain interaction with reactive/async code (`@Async`, CompletableFuture).

**Answer:**

Reach for programmatic control when boundaries are **dynamic or data-driven** — per-item commits in loops, retry blocks needing fresh transactions, conditional transactionality, framework/plumbing code, and avoiding self-invocation contortions:

```java
private final TransactionTemplate txTemplate;   // built from PlatformTransactionManager

public BatchResult process(List<Item> items) {
    var failures = new ArrayList<Long>();
    for (Item item : items) {
        try {
            txTemplate.executeWithoutResult(status -> handler.handle(item)); // one tx per item
        } catch (Exception e) {
            failures.add(item.id());            // rollback confined to this item
        }
    }
    return new BatchResult(items.size() - failures.size(), failures);
}
```

`TransactionTemplate` exposes the same knobs (propagation, isolation, readOnly, timeout) as the annotation, but visible in code and immune to proxy pitfalls. `status.setRollbackOnly()` gives explicit doom control.

**Async interaction — the trap cluster:** transactions are **thread-bound** (`ThreadLocal` in `TransactionSynchronizationManager`). Therefore:

- `@Async`/`CompletableFuture.supplyAsync` work runs on another thread → it does **not** inherit the caller's transaction; an `@Transactional @Async` method gets its own new transaction on the worker thread (fine, if intended — but caller can't atomically compose with it, and caller's uncommitted data is invisible to it, echoing Q64).
- Passing **entities** across threads = detached objects + LIE (Q37). Pass ids/DTOs.
- Starting async work *inside* a transaction that must only run on success → `@TransactionalEventListener(AFTER_COMMIT)` or `TransactionSynchronization.afterCommit`, not a raw executor call.
- Virtual threads (Loom) don't change the model — still thread-bound context per (virtual) thread; pinning concerns with synchronized JDBC internals are a modern add-on point.

**Follow-up trap:** *"Timeouts?"* — `@Transactional(timeout = 5)`: Spring-side deadline checked at statement/operation boundaries (Hibernate sets query timeouts accordingly); not a hard DB-side cancel guarantee mid-statement — pair with JDBC/socket timeouts (there's a whole layered-timeout answer: transaction ≥ query ≥ JDBC ≥ socket).

---

### Q69. Explain the complete write path timeline: service method with `@Transactional` calls `repo.save(newOrder)` then modifies a loaded `Customer`, then returns. Narrate every step from proxy entry to database commit — the interviewer wants the mental movie.

**Answer:**

1. **Proxy entry:** call hits the transaction interceptor → `JpaTransactionManager.getTransaction` → no existing tx (REQUIRED) → obtains an EntityManager, binds it to the thread, begins a DB transaction on the pooled connection (connection may be acquired lazily at first SQL, with connection-handling modes deferring acquisition — a nice detail).
2. **`repo.save(newOrder)`:** repository proxy → `SimpleJpaRepository.save` → `isNew` → `em.persist`. With SEQUENCE ids: Hibernate may call the sequence now (or use a cached allocation block) to assign the id; the entity becomes **managed**; an *insert action* is queued in the ActionQueue — **no SQL insert yet** (IDENTITY would force it now).
3. **Loading Customer:** `em.find`/query → SELECT (or L1/L2 hit) → managed instance + snapshot stored.
4. **`customer.setTier(GOLD)`:** just a field write in memory. Nothing else happens — no framework call. (This is the dirty-checking punchline.)
5. **Method returns → commit path begins:** interceptor asks tx manager to commit → triggers **flush**: dirty check compares Customer to snapshot → schedules UPDATE; ActionQueue executes in canonical order (inserts→updates→…): `INSERT INTO orders …` (batched if configured), `UPDATE customer SET tier=…`. `@PrePersist/@PreUpdate` callbacks and `@Version` increments happen around here; optimistic lock check = `UPDATE … WHERE id=? AND version=?`.
6. **DB commit:** `Connection.commit()` — the only moment other transactions can see anything. After-commit synchronizations run (`@TransactionalEventListener(AFTER_COMMIT)`, cache puts for 2LC with transactional strategies, `@DomainEvents` were published at save-time but listeners with AFTER_COMMIT fire now).
7. **Cleanup:** EM closed/unbound, connection returned to pool. Entities are now **detached** — any later lazy touch is Q37.

Rollback variant: exception → flush skipped (or already-flushed statements undone by DB rollback), `Connection.rollback()`, PC discarded — in-memory entity changes remain (objects aren't rolled back!), a subtle final point interviewers appreciate: after rollback your Java objects hold state the DB rejected.

**Follow-up trap:** *"Where can it still fail after your code succeeded?"* — at flush (constraints, optimistic lock) and at commit — so exceptions can surface at the *proxy boundary*, after the visible method body; that's why `saveAndFlush` in tests surfaces constraint bugs earlier.

---

### Q70. Distributed reality check: the service must update the database AND publish a Kafka event / call another service — "make it transactional." Explain why two-phase JPA+JMS/XA is rarely the answer now, and detail the transactional outbox pattern with Spring Data JPA.

**Answer:**

The naive versions fail two ways: publish-then-commit (event for data that rolls back — phantom event) or commit-then-publish (crash between → lost event). `@TransactionalEventListener(AFTER_COMMIT)` fixes ordering but not the crash window — after-commit code can still die before publishing (and it runs *after* commit, so failure can't roll anything back).

**XA/2PC** (JTA, Atomikos/Narayana with an XA datasource + XA-capable broker): a real answer, but rarely chosen now — operational complexity, poor cloud/broker support (Kafka doesn't do XA), throughput cost of prepare/commit phases, and heuristic-outcome failure modes. Know it exists; explain why teams avoid it.

**Transactional outbox** — the industry-standard answer, and it's pure Spring Data JPA:

```java
@Transactional
public void placeOrder(PlaceOrder cmd) {
    Order order = orderRepository.save(Order.from(cmd));
    outboxRepository.save(OutboxEvent.of(          // SAME transaction, SAME database
        "OrderPlaced", order.getId(), toJson(new OrderPlacedEvent(order))));
}
```

The business row and the event row commit **atomically** (single local transaction — that's the whole trick). A separate relay publishes: either a poller (`@Scheduled` reading unpublished rows `ORDER BY id`, publishing to Kafka, marking sent — use `FOR UPDATE SKIP LOCKED` for competing pollers, Q78) or **CDC/Debezium** tailing the outbox table (lower latency, no polling load). Consumers must be **idempotent** — at-least-once delivery is the contract (dedupe by event id).

Complete the picture with: idempotency keys for the inbound operation, saga/compensation for multi-service workflows (each step local-tx + events), and "exactly-once is a lie; effectively-once = at-least-once + idempotency" — the phrase interviewers want to hear.

**Follow-up trap:** *"Why not ChainedTransactionManager?"* — best-effort commit ordering, no atomicity (it was deprecated); a crash between the two commits still splits state — it's the illusion of safety, worse than knowingly choosing outbox.

---

# Section 8 — Locking & Concurrency (Q71–Q78)

---

### Q71. Two users open the same product edit form; both save; the second silently erases the first's changes. Explain the lost-update problem and how `@Version` optimistic locking solves it — including what SQL Hibernate generates and which write paths it does NOT protect.

**Answer:**

**Lost update:** read → think → write cycles interleave; the later writer overwrites the earlier one's committed change without ever seeing it. DB isolation (READ_COMMITTED) does not prevent it — both transactions are individually "correct," and with a web form the read and write aren't even in one DB transaction.

**`@Version`:**

```java
@Version
private Long version;      // or int/short/Timestamp — numeric preferred
```

Every UPDATE/DELETE gets a guard: `UPDATE product SET …, version = version + 1 WHERE id = ? AND version = ?`. If another transaction bumped the version in between, **0 rows** are affected → Hibernate throws `OptimisticLockException` (Spring: `ObjectOptimisticLockingFailureException`) and the transaction rolls back. First committer wins; the loser *finds out* — which is the entire point.

For the web-form (detached, cross-request) case: the form must **carry the version** (hidden field / DTO / ETag). On save you either merge the detached entity (version compared at flush) or load-and-check: if `dto.version != entity.version` → reject/409 with a "record was modified" flow. Without echoing the version to the client, in-app optimistic locking only covers concurrent *transactions*, not the human think-time race — a distinction that wins interviews.

**Not protected:** bulk JPQL/native updates (bypass versioning entirely unless you write the guard yourself / HQL `update versioned`), direct SQL by other apps, and fields updated via `@Modifying` queries. Also `@Version` must be managed by Hibernate only — never set it manually (setting it is not a lock request; changing it manually breaks the mechanism).

**Follow-up trap:** *"What increments the version?"* — any flush that UPDATEs the row (dirty change); also owning-side association changes that touch the row. Not reads. And `LockModeType.OPTIMISTIC_FORCE_INCREMENT` bumps it without data changes (Q75).

---

### Q72. You get sporadic `ObjectOptimisticLockingFailureException` in production on a hot endpoint. Walk through your handling strategy: retry patterns (with code), when retrying is WRONG, and how to shrink the conflict window.

**Answer:**

**First, classify the write:** (a) *mechanical/commutative* updates (recompute totals, apply a delta, idempotent state machines) — safe to auto-retry; (b) *human-intent* edits (user typed content over stale data) — retrying would silently reimpose the lost-update problem; the correct response is surfacing the conflict (HTTP 409 / merge UI / "reload and reapply").

**Retry pattern** — critical detail: the retry must start a **new transaction** with a **fresh load** (the old persistence context holds doomed state):

```java
@Service
public class StockService {
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
               maxAttempts = 3, backoff = @Backoff(delay = 50, multiplier = 2, random = true))
    @Transactional
    public void decrement(Long productId, int qty) {
        Product p = repo.findById(productId).orElseThrow();
        p.decrementStock(qty);                       // fresh read every attempt
    }
}
```

`@Retryable` (spring-retry) sits **outside** `@Transactional` in the proxy chain here (method-level: retry interceptor wraps transaction interceptor — verify order! `@EnableRetry` + correct advice order), so each attempt = new tx. Jittered backoff avoids thundering-herd re-collision. Hand-rolled alternative: loop around `TransactionTemplate` — same rules. Never catch-and-retry *inside* the same transaction — the tx is rollback-only and the context poisoned (Q62).

**Shrink the conflict window:** shorter transactions; update narrower state (split hot counters from wide entities — e.g., a `product_stock` row separate from `product`); replace read-modify-write with **atomic conditional/delta SQL** (`@Modifying update Product set stock = stock - :q where id = :id and stock >= :q` — check affected rows == 1) — often eliminating versioning contention entirely; or move truly hot counters out of the RDBMS. If contention is *persistent* rather than sporadic, optimistic was the wrong tool — switch that operation to pessimistic locking (Q73) or serialized processing (queue per aggregate).

**Follow-up trap:** *"Why did the exception surface at the controller though the service caught 'everything'?"* — version check happens at **flush/commit**, i.e., at the transactional proxy boundary *after* the method body — code inside can't catch it. Wrap at a level outside the transaction.

---

### Q73. Flash-sale problem: decrement inventory, never oversell, high contention on a few rows. Compare optimistic retries vs `PESSIMISTIC_WRITE`. Show the Spring Data pessimistic pattern with timeout, and explain what SQL/locks are involved.

**Answer:**

Under **high contention**, optimistic locking degrades: most transactions do work then fail at commit and retry — wasted load, livelock-ish behavior, terrible tail latency. When conflict probability is high and the critical section is short, **pessimistic locking** (serialize at the row) is correct.

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}

@Transactional
public void purchase(Long productId, int qty) {
    Product p = repo.findByIdForUpdate(productId).orElseThrow(); // SELECT … FOR UPDATE — blocks rivals
    if (p.getStock() < qty) throw new OutOfStockException();
    p.setStock(p.getStock() - qty);                              // safe: we own the row
}   // lock released at commit/rollback
```

Mechanics: `PESSIMISTIC_WRITE` → `SELECT … FOR UPDATE` — an exclusive **row lock held until transaction end**; competitors block (up to the lock timeout → `PessimisticLockException`/`LockTimeoutException` — Spring: `PessimisticLockingFailureException` family, or `CannotAcquireLockException`). The check-then-act (`stock >= qty`) is now race-free. `PESSIMISTIC_READ` → shared lock (`FOR SHARE`) — blocks writers, allows readers; rarer. Lock **timeout** is essential (unbounded waits + pool exhaustion = outage); note timeout support/granularity is dialect-dependent (Postgres: `NOWAIT`/`wait N` support varies; Oracle `WAIT n`).

Rules that show experience: lock rows in a **consistent order** across code paths (deadlock prevention — Q77); keep the locked section tiny (no remote calls while holding!); lock the *narrowest* row (a dedicated stock row, not the product aggregate). And restate the alternative: the single-statement atomic decrement (Q72) needs no explicit lock at all and beats both approaches when logic fits in one UPDATE — the best lock is no lock.

**Follow-up trap:** *"Does `findById` after someone else's FOR UPDATE block?"* — plain MVCC reads don't block on Postgres/InnoDB (readers see the snapshot); only lock-requesting statements queue. Lock scope ends at transaction end — never hold across user interaction.

---

### Q74. `LockModeType` tour: OPTIMISTIC vs OPTIMISTIC_FORCE_INCREMENT vs PESSIMISTIC_FORCE_INCREMENT vs PESSIMISTIC_READ/WRITE and NONE — give a concrete use case for each of the exotic ones and how to apply lock modes in Spring Data / EntityManager APIs.

**Answer:**

Application points first: `@Lock(...)` on repository methods; `em.find(Entity.class, id, lockMode)`; `em.lock(entity, mode)` (lock an already-loaded entity); `query.setLockMode(...)`. Lock modes require an active transaction.

- **NONE** — default; versioned entities still get the version check on *dirty* writes.
- **OPTIMISTIC** (was READ) — beyond default behavior, at commit Hibernate **re-verifies the version of entities you only *read*** (a re-select checking version unchanged). Use case: an invoice total computed from read line-items — ensure the items didn't change under you before committing the total. Ensures repeatable-read-like integrity without blocking.
- **OPTIMISTIC_FORCE_INCREMENT** (was WRITE) — commit **bumps the version even though the row's data didn't change**. Use case: parent-aggregate versioning — adding an `OrderItem` (a *child* insert) force-increments the `Order` version, so any concurrent transaction working on the same order conflicts; encodes "the aggregate changed" for changes that don't touch the root row. Great DDD-flavored answer.
- **PESSIMISTIC_READ** — shared lock (`FOR SHARE`): "no one may *change* this while I read a consistent set"; multiple readers OK. Use: consistency-critical multi-row reads without blocking other readers (dialect support varies; some DBs escalate to write locks).
- **PESSIMISTIC_WRITE** — exclusive `FOR UPDATE` (Q73).
- **PESSIMISTIC_FORCE_INCREMENT** — exclusive lock **and** version bump immediately. Use: pessimistic edit sessions on versioned entities where even lock-holders must invalidate optimistic readers on other nodes.

Also mention: `jakarta.persistence.lock.scope = EXTENDED` (locks join tables/element collections too — rarely used but spec-defined) and that lock-mode queries can't be paired with fetch-joined collections cleanly on all DBs (`FOR UPDATE` with outer joins is restricted — Postgres forbids `FOR UPDATE` with some joins → lock the root only, fetch separately).

**Follow-up trap:** *"Optimistic modes without `@Version`?"* — illegal/no-op: OPTIMISTIC modes require a versioned entity (`PersistenceException` otherwise). Pessimistic modes don't need `@Version`.

---

### Q75. Design an "only one worker processes each row" job queue in Postgres/MySQL with Spring Data JPA — multiple app instances polling a table. The naive `findFirstByStatus('PENDING')` + update approach double-processes. Fix it properly.

**Answer:**

Naive race: two instances SELECT the same pending row before either UPDATEs it → double processing. Optimistic `@Version` prevents the *double commit* but each collision wastes a poll cycle; the throughput answer is **`FOR UPDATE SKIP LOCKED`** — each worker locks-and-claims different rows, skipping ones locked by rivals:

```java
public interface JobRepository extends JpaRepository<Job, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")  // Hibernate: SKIP LOCKED
    })
    @Query("select j from Job j where j.status = 'PENDING' order by j.createdAt")
    List<Job> claimBatch(Pageable pageable);       // limit via PageRequest.of(0, 10)
}
```

(`-2` = `LockOptions.SKIP_LOCKED` in Hibernate; alternatively a native query with literal `FOR UPDATE SKIP LOCKED` — often clearer and I'd say so.) Worker flow: in one transaction — claim batch, mark `status = RUNNING, locked_by, locked_at`, commit (releases locks but ownership is now recorded); process; final transaction marks DONE/FAILED. Keep the *processing* outside the claiming transaction (locks held short — Q66 doctrine) — which is why you persist the claim rather than holding the lock during work.

Production must-haves to volunteer: **stuck-job recovery** (a sweeper re-queues RUNNING jobs older than a lease timeout — process died mid-work), **idempotent processing** (recovery implies at-least-once), ordered claiming (`order by`) only if business needs it (ordering + SKIP LOCKED = per-row ordering only), and an index on `(status, created_at)`. Contrast: `NOWAIT` (`timeout -1`... actually `NOWAIT` = fail immediately) errors on locked rows instead of skipping — for "try one specific row" semantics.

This question doubles as the outbox-relay competing-consumers design (Q70) — connect them explicitly.

**Follow-up trap:** *"Why not `UPDATE … SET status='RUNNING' WHERE id IN (select … LIMIT 10) RETURNING *`?"* — excellent alternative (single-statement claim, Postgres) — showing you know both the JPA idiom and the raw-SQL superior version is exactly the seniority signal.

---

### Q76. `@Version` with DTO round-trips and REST: design optimistic concurrency for a REST API (GET returns the resource, PUT updates it) — ETags vs version-in-body, what status code on conflict, and the "If-Match" flow with Spring.

**Answer:**

Flow: GET `/products/42` → response carries the concurrency token: either in the body (`"version": 7`) or as **`ETag: "7"`** header. Client edits, then PUT with **`If-Match: "7"`** (or version in the DTO). Server:

```java
@Transactional
public ProductDto update(Long id, ProductDto dto, long expectedVersion) {
    Product p = repo.findById(id).orElseThrow(NotFound::new);
    if (p.getVersion() != expectedVersion)
        throw new PreconditionFailedException();          // → 412 (ETag flow) or 409
    p.apply(dto);                                          // field-by-field
}   // flush still enforces version vs concurrent txs — the manual check covers the human race
```

Two layers of protection, name both: the explicit check covers the **cross-request human race** (stale form), while `@Version` at flush covers **concurrent in-flight transactions** — the manual check alone has a tiny race between check and commit, closed by the flush-time guard throwing `ObjectOptimisticLockingFailureException` (map it to the same 409/412).

Conventions: with `If-Match`/ETag semantics, mismatch → **412 Precondition Failed** (and you can require `If-Match` by rejecting its absence with 428 Precondition Required); with version-in-body, **409 Conflict** is customary. Response on conflict should include current state (or its ETag) so clients can merge/retry. Spring bits: `ResponseEntity.ok().eTag(String.valueOf(p.getVersion())).body(dto)`; `@ExceptionHandler(ObjectOptimisticLockingFailureException.class)` → 409; Spring's `ShallowEtagHeaderFilter` is about bandwidth caching, *not* this — distinguishing them is a nice flex.

Also: never let the client set the version on a merged entity blindly (merge treats it as a comparison value — actually Hibernate uses the detached version in the check, which is exactly the desired stale-detection… but only if you merge the *detached entity*, not copy fields onto a fresh load while ignoring version — state clearly which pattern you're in).

**Follow-up trap:** *"PATCH semantics?"* — partial updates still need the token; without it, PATCH is a lost-update generator with a friendlier name.

---

### Q77. Production incident: `Deadlock found when trying to get lock; try restarting transaction` (MySQL) / `deadlock detected` (Postgres) between order processing and inventory sync. How do deadlocks arise in JPA apps specifically, how do you diagnose, and what ordering/design rules prevent them?

**Answer:**

**How JPA apps create deadlocks:** two transactions acquire row/index locks in **opposite orders** — e.g., Tx1 updates Order 1 then Product A; Tx2 updates Product A then Order 1. JPA-specific contributors: (a) **flush-order opacity** — Hibernate writes at flush in its own canonical order (by action type, then entity type, then... within a type, insertion order of *actions*) so two code paths touching the same entities in different *logical* order can still collide, and `order_updates=true` (sorts updates by PK) exists partly to normalize this; (b) unindexed FK columns causing DBs to take broader locks (MySQL gap/next-key locks on scans); (c) cascade graphs touching parent+children in patterns crossing other transactions; (d) explicit pessimistic locks taken in inconsistent order across endpoints; (e) lock + long transaction (remote call inside tx) widening the window.

**Diagnosis:** the DB tells you — Postgres logs both queries in `deadlock detected` detail; MySQL `SHOW ENGINE INNODB STATUS` → LATEST DETECTED DEADLOCK section (both transactions' statements + locks held/waited). Map statements back to code paths; reproduce with two sessions in a SQL client. Note the DB *resolves* deadlocks by killing one victim (Spring surfaces `CannotAcquireLockException`/`DeadlockLoserDataAccessException`) — apps must treat it as retryable.

**Prevention rules:**

1. **Global lock ordering** — always touch shared resources in one canonical order (e.g., by entity type rank then PK ascending); when locking multiple rows, `ORDER BY id` in the locking select.
2. **Short transactions, no external I/O inside** (Q66).
3. **Index every FK** and columns in locking-query predicates (avoid lock-scan escalation).
4. Prefer single-statement atomic updates over read-modify-write (fewer, briefer locks).
5. `order_updates`/`order_inserts` to stabilize flush order; explicit `flush()` where sequence matters.
6. Retry idempotent transactions on deadlock-loser exceptions (`@Retryable` — deadlocks are transient by nature).

**Follow-up trap:** *"Deadlock vs lock-wait timeout?"* — deadlock = cycle, detected & one victim killed quickly; timeout = someone held too long, no cycle — different exceptions, different fixes (retry vs find-the-hog). Conflating them is a junior tell.

---

### Q78. Concurrency capstone: an account-balance transfer between two accounts must be correct under concurrency. Present the full solution space — constraint-backed invariants, pessimistic two-row locking with ordering, optimistic with retry, and serializable isolation — and pick one with justification.

**Answer:**

Invariant: no lost updates, no negative balances, atomic double-entry.

**Layer 0 — DB constraint as the last line:** `CHECK (balance >= 0)`. Whatever the app does wrong, overdrafts become exceptions, not data. Always ship the constraint (defense in depth — leading with this is a senior move).

**Option A — pessimistic, ordered (the classic correct answer):**

```java
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    Long first = fromId < toId ? fromId : toId;        // canonical lock order — no deadlocks
    Long second = fromId < toId ? toId : fromId;
    Account a1 = repo.findByIdForUpdate(first).orElseThrow();
    Account a2 = repo.findByIdForUpdate(second).orElseThrow();
    Account from = a1.getId().equals(fromId) ? a1 : a2;
    Account to   = (from == a1) ? a2 : a1;
    from.withdraw(amount);                              // throws on insufficient funds
    to.deposit(amount);
}
```

Deterministic, no retries, brief exclusive locks on exactly two rows. Cost: blocking; requires the ordering discipline everywhere accounts are locked.

**Option B — optimistic + retry:** `@Version` on Account, `@Retryable` around a fresh-load transaction. Fine for low contention; under hot accounts (a merchant account touched by every sale) retries thrash — hot-row workloads are precisely optimistic locking's worst case.

**Option C — atomic conditional updates:** `update account set balance = balance - :amt where id = :from and balance >= :amt` (check rowcount) + unconditional credit, one transaction. Fast, minimal locking; but business logic drifts into SQL and multi-row invariants get awkward.

**Option D — SERIALIZABLE isolation:** correct by construction, but retry-on-serialization-failure everywhere, throughput cost, and it's a blunt instrument for two known rows.

**Pick:** A (with the check constraint, and an inserted immutable `transfer` ledger row for audit), because correctness is deterministic, contention is bounded to the two accounts, and no retry machinery is needed. Mention the scaling escape hatch: at very high volume, event-sourced/ledger-append designs with async balance projection replace row contention entirely — knowing where the RDBMS pattern ends is the final point.

**Follow-up trap:** *"Why lock in id order instead of always from-then-to?"* — transfer(A→B) concurrent with transfer(B→A) in from-first order = textbook deadlock; canonical global order breaks the cycle. Interviewers specifically probe this.

---

# Section 9 — Performance, Batching & Caching (Q79–Q90)

---

### Q79. An endpoint is "slow because of JPA." Give your systematic performance-diagnosis playbook: what you turn on, what you measure, and the top culprits you check in order.

**Answer:**

**Instrument first, guess never:**

1. **See the real SQL + counts:** not `show_sql` (no bind values, no timing, lies about batching). Use `datasource-proxy` or p6spy (statement + parameters + timing + batch visibility), and Hibernate statistics:

```properties
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE   # bind params, Hibernate 6
```

2. **Count queries per request** — the single most diagnostic number. >10 for a simple endpoint = fetch-strategy smell.
3. **EXPLAIN (ANALYZE)** the slow statements in the DB — is it the ORM (too many queries / wrong queries) or the DB (missing index, bad plan)? These are different diseases.
4. APM/micrometer: time in JDBC vs time in Hibernate flush vs serialization.

**Culprit checklist in priority order:** (1) **N+1** on associations (Q31) — fix with fetch plans; (2) **fetching entities where DTOs belong** — wide rows, LOBs, dirty-check overhead (Q47/Q52); (3) **missing indexes** on FK columns and query predicates; (4) **pagination pathologies** — in-memory collection-fetch paging (Q33), deep OFFSET (Q86); (5) **needless `Page` count queries** (Q86); (6) **no JDBC batching** in write bursts + IDENTITY ids (Q57/Q12); (7) **giant persistence contexts** — long transactions accumulating thousands of managed entities, auto-flush dirty-scans before every query (Q52); (8) **connection pool waits** — pool sized wrong or transactions holding connections across remote calls (Q66) — check Hikari metrics (`pending`, `usage`); (9) **query plan cache misses** from IN-list explosion (Q42) or literal-containing generated SQL; (10) **OSIV render-time query storms** (Q40).

Then fix top-down, re-measure, and codify the win as a test (statement-count assertions) so it can't regress. Interviewers want the *ordering discipline* — measure, then the two ORM classics (N+1, entity-vs-DTO), then the DB.

**Follow-up trap:** *"How do you catch these in CI rather than prod?"* — statement-count/assertion libraries in `@DataJpaTest`s on hot flows + EXPLAIN-based tests for critical queries + load tests with statistics on. Prevention as process, not heroics.

---

### Q80. Your write-heavy service does 5,000 single-row INSERTs per burst. Show the full JDBC batching configuration for Hibernate, explain `order_inserts`/`order_updates` and the versioned-data flag, and how to PROVE batching is active (many teams think it is and it isn't).

**Answer:**

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

- **`batch_size`** — Hibernate buffers statements per JDBC `PreparedStatement` batch and flushes as batches at flush time. Requires **SEQUENCE/assigned ids** — IDENTITY forces immediate per-row INSERT (Q12) and silently disables all of this. The most common reason "batching doesn't work."
- **`order_inserts`/`order_updates`** — flush emits actions grouped/sorted by entity table (and PK for updates), so mixed-entity work (Order, then Item, then Order...) doesn't fragment into tiny batches; a batch breaks whenever the target statement changes.
- **`batch_versioned_data`** — allows batching UPDATEs on `@Version`ed entities (older default was false because ancient drivers misreported per-row update counts, which optimistic locking needs; modern drivers are fine — true is standard now, and it's the Hibernate 5+ default).
- Driver-level multipliers: PostgreSQL `reWriteBatchedInserts=true` rewrites the batch into multi-row `INSERT … VALUES (…),(…)` — big additional win; MySQL `rewriteBatchedStatements=true` similarly.

**Proving it:** `show_sql` prints one line per logical statement *regardless of batching* — useless as evidence. Real proofs: (a) **datasource-proxy/p6spy** logs `batch:true, batchSize:100`; (b) Hibernate statistics: compare entity insert count vs JDBC statement/batch counts (`SessionFactory.getStatistics()`, or the `hibernate.stat` log line per query/flush); (c) Postgres logs showing multi-row VALUES after `reWriteBatchedInserts`; (d) round-trip drop in APM. Also remember flush/clear chunking for context size (Q57) — batching and context management are separate problems that must both be solved.

**Follow-up trap:** *"Why did adding one EAGER child entity type break batch efficiency?"* — interleaved inserts across tables fragment batches without `order_inserts`; and cascading persists of children between parent inserts do the same — grouping is exactly what `order_inserts` repairs.

---

### Q81. Second-level cache: a colleague enables `@Cacheable`-style entity caching for everything and the app gets *slower* and occasionally serves stale data. Explain the 2LC architecture (regions, providers), the four `CacheConcurrencyStrategy` options, what actually gets cached, and a disciplined enablement checklist.

**Answer:**

**Architecture:** the second-level cache is a **SessionFactory-scoped** (cross-transaction, per-JVM unless distributed) cache of entity **data** (dehydrated state arrays keyed by id — not entity instances), organized into **regions** (per entity/collection). Providers plug in via JCache/direct integration: Ehcache, Caffeine (via JCache), Infinispan (the distributed option). Enable per entity:

```java
@Entity
@Cacheable                                                    // JPA
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Country { … }
```

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=…  # provider
```

**Strategies:** `READ_ONLY` — immutable data; fastest; updates throw. `NONSTRICT_READ_WRITE` — invalidate-on-commit without locking; brief stale windows tolerated. `READ_WRITE` — soft-lock protocol guarantees consistency for one JVM/cache; the usual choice for mutable cached entities. `TRANSACTIONAL` — full XA cache participation (Infinispan/JTA); rare.

**What's cached & what's not:** `em.find`/lazy-proxy resolution by id → hits 2LC. **Queries do NOT** — `findByEmail` goes to the DB even if the entity is cached (unless the *query cache* is also on — Q84, which stores id-lists and then hydrates via 2LC). Collections need their own `@Cache` on the collection attribute.

**Why "cache everything" backfired:** write-heavy entities pay locking/invalidation overhead per update and evict constantly (no hit ratio); memory pressure; and in a **multi-node deployment with per-JVM caches, updates on node A leave stale data on node B** — the stale reads observed. External writers (other apps, bulk/native SQL) also silently bypass invalidation (Q43).

**Checklist:** cache only read-mostly, shared-across-users, by-id-accessed data (reference data, catalogs); choose strategy per entity honestly; **measure hit ratios** (statistics) before/after; distributed or versioned-tolerant design for multi-node (or accept TTL-bounded staleness explicitly); document who may write those tables. Default position for a senior: 2LC is a scalpel, not a default — many high-scale teams skip it entirely in favor of DTO caching at the service layer where invalidation is explicit.

**Follow-up trap:** *"Does 2LC help your `findAll()`-style screens?"* — No (queries bypass it) — the most common misconception; that's the query cache's (fragile) job.

---

### Q82. Contrast Hibernate's second-level cache with Spring's `@Cacheable` on service methods (Redis/Caffeine). When do you choose which? What are the invalidation and consistency trade-offs the interviewer wants to hear?

**Answer:**

**Hibernate 2LC** caches *entity state by id*, transparently inside the ORM: every `find`/association navigation benefits without code changes; invalidation is **automatic** on entity writes *through Hibernate on that node* (with distributed providers, cluster-wide). It composes with transactions (strategies control visibility) — the "correctness-aware" cache. Limits: entity-shaped only, per-id access pattern, blind to external writers, provider/ops complexity, easily misused (Q81).

**Spring `@Cacheable`** caches *arbitrary method results* (DTOs, computed views) in Caffeine/Redis: cross-node sharing trivially (Redis), caches exactly the expensive thing (a whole assembled screen/aggregation, an external call), TTL-first mindset. Limits: **invalidation is manual** — `@CacheEvict`/`@CachePut` discipline, and every write path must remember; no transaction awareness by default (a `@Cacheable` populated inside a transaction that later rolls back can cache phantom data — evictions should be after-commit; mention `TransactionAwareCacheDecorator`/transaction-aware cache managers as the fix); stale windows are a design decision (TTL).

**Choosing:** reference/config entities navigated via associations by id → 2LC shines. Read-model/API responses, aggregations, cross-service data, multi-node correctness with explicit control → service-layer caching of **DTOs** (never cache entities in external caches — serialization + detached/lazy landmines, Q37). Many systems: small 2LC for reference data + Redis for read models; or Redis only, for operational simplicity.

**Consistency talking points to volunteer:** cache-aside vs write-through; stampede protection (Caffeine's loading cache, Redis locks/single-flight); TTL as bounded-staleness contract with the business; event-driven eviction (entity listeners/domain events → evict after commit); and "every cache is a consistency decision — name the staleness budget out loud."

**Follow-up trap:** *"Why is caching entities in Redis specifically bad?"* — entities are context-bound object graphs: lazy proxies don't serialize meaningfully, deserialized copies are detached (merge-clobber risks), versions go stale — you'd be caching landmines. DTOs are values; cache values.

---

### Q83. The query plan cache: an app with dynamic IN-lists and many distinct query shapes shows high CPU inside Hibernate's query compilation and growing old-gen memory. Explain the plan cache, the IN-clause padding fix, and the sizing knobs.

**Answer:**

Every distinct JPQL/HQL/Criteria string Hibernate executes must be **compiled** (parse → semantic analysis → SQL generation). Compiled plans are memoized in the **query plan cache** (bounded map on the SessionFactory). Problems arise when the *number of distinct query strings* explodes:

- **IN-lists are the classic driver:** `where id in (?)`, `(?,?)`, `(?,?,?)`… each arity is a *different* string → hundreds of plans per logical query → cache churn (recompilation CPU) + memory (plans aren't tiny).
- Criteria queries with structural variations, dynamically concatenated JPQL, and literal values inlined into strings (never do that — parameters, always) have the same effect.

**Fixes:**

```properties
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true
spring.jpa.properties.hibernate.query.plan_cache_max_size=2048          # default 2048
```

- **`in_clause_parameter_padding`** — pads IN-lists up to the next power of two (5 params → 8 placeholders, extras bound to a repeated value) → arities collapse to log₂ distinct shapes. Cheap, huge win; also stabilizes DB-side statement caches.
- Size the plan cache to your real distinct-query count (monitor via statistics `getQueryPlanCacheHitCount/MissCount` — Hibernate 5.4+/6 expose these; heap-dump inspection of `QueryInterpretationCache` in emergencies).
- Structural fixes: cap/bucket IN-list sizes in code (chunk to fixed 100s), replace giant IN-lists with temp-table joins or `= ANY(:array)` on Postgres (single-parameter array — one plan for any size — an expert-level alternative worth naming).

Hibernate 6 renamed internals (`QueryInterpretationCache`) but the model and knobs are the same conceptually.

**Follow-up trap:** *"Where else do too-many-shapes hurt?"* — the DB's own prepared-statement/plan caches and the driver's statement cache — the pathology is end-to-end, which is why padding helps at every layer.

---

### Q84. The Hibernate query cache: why is it off by default, what exactly does it store, why must it be paired with the second-level cache, and why do most experts advise against it? When is it legitimately useful?

**Answer:**

Enabled by `hibernate.cache.use_query_cache=true` **plus opt-in per query** (`org.hibernate.cacheable` hint / `setCacheable(true)` / `@QueryHints` on the repository method). It stores, keyed by *(query string + bind parameters)*: **result ids + a timestamp** (for entity results — scalar results stored directly). Hydrating a hit then fetches each entity **by id from the 2LC** — if the entity region is cold, you get the infamous pathology: one cache hit followed by **N id-lookups against the DB** (worse than running the query). Hence the pairing rule: query cache without 2LC for the returned entities is a performance *downgrade*.

**Why it's off by default / advised against:** its invalidation is brutally coarse — an **update-timestamps cache** tracks the last-write time *per table*; ANY insert/update/delete on ANY row of a queried table invalidates ALL cached queries touching that table. On tables with regular writes the hit ratio collapses to ~0 while you still pay bookkeeping on every write (the timestamp cache must be strictly consistent — in clusters that's a synchronization point). Plus the usual multi-writer blindness (native/bulk/external writes — though Hibernate's own bulk ops do bump table timestamps).

**Legitimate uses:** genuinely static or write-rarely tables (config, taxonomies) queried with a *small number of distinct parameter combinations*, entities cached READ_ONLY in 2LC — e.g., "countries by region." Even then, measure the hit ratio; and the honest senior alternative is a service-level `@Cacheable` DTO with a TTL — same benefit, explicit and observable invalidation.

**Follow-up trap:** *"Does the query cache store entity data?"* — No, ids only (that's the design linkage to 2LC) — the single most-missed fact in this topic; leading with it signals you've actually read how it works.

---

### Q85. Pagination at depth: `Page` vs `Slice` vs `Window`/keyset. Page 10,000 of a large table takes seconds. Explain why OFFSET degrades, and implement keyset (seek) pagination with Spring Data — including its constraints.

**Answer:**

**Why OFFSET degrades:** `LIMIT 20 OFFSET 200000` forces the DB to *produce and discard* 200,000 ordered rows before returning 20 — cost grows linearly with page number (index scan can help but still walks the whole prefix). Deep pages also skew under concurrent inserts (rows shift between requests → duplicates/skips).

**Keyset (seek) pagination:** remember the last row's sort-key values; next page = `WHERE (sort keys) > (last seen)`:

```java
@Query("""
   select o from Order o
   where (o.createdAt < :lastCreatedAt)
      or (o.createdAt = :lastCreatedAt and o.id < :lastId)
   order by o.createdAt desc, o.id desc""")
List<Order> nextPage(@Param("lastCreatedAt") Instant lastCreatedAt,
                     @Param("lastId") Long lastId, Pageable limitOnly);
```

Constant cost per page (index seek on `(created_at, id)` — the composite index is mandatory, and the **unique tiebreaker column** (id) is what makes it correct with duplicate timestamps). Postgres row-value syntax `(created_at, id) < (:a, :b)` is cleaner — native query. Spring Data now has first-class support: **`Window<T>` + `ScrollPosition`** (`findFirst10ByOrderByCreatedAtDesc(ScrollPosition.keyset())`) — naming `WindowIterator` shows current knowledge.

**Constraints of keyset:** no random access ("jump to page 57" impossible — only next/prev), requires a stable total order with a unique tiebreaker, multi-column sort changes the predicate shape, and filters must be index-compatible with the sort. Perfect for infinite scroll/APIs (cursor-based pagination — the REST framing: opaque `next` cursor tokens encoding the keyset).

Recap the family: **`Page`** = content + total count (extra COUNT query — Q86); **`Slice`** = content + hasNext (fetches size+1, no count); **`Window`/keyset** = scalable cursoring. Choose by UI need, not habit.

**Follow-up trap:** *"Total counts for the UI at scale?"* — approximate counts (Postgres `reltuples`), counted-up-to caps ("10,000+"), or drop the requirement — exact counts on billion-row filtered sets are a product negotiation, not a SQL trick.

---

### Q86. The hidden cost of `Page<T>`: your listing endpoint runs a heavy `COUNT(*)` with five joins on every request. What are your options — count-query optimization, `Slice`, cached counts — and how does Spring Data let you customize the count query?

**Answer:**

`Page` always needs total elements → Spring Data derives a count query. Problems: (a) on complex queries the count can cost as much as the data query; (b) derived count queries from fetch-join JPQL historically trip on joins (hence explicit `countQuery`); (c) it runs even when the client ignores totals. Options:

1. **Drop totals — `Slice` or `Window`:** most UIs only need "is there more" (Q85). The cheapest count is the one you don't run. (Also: Spring Data skips the count when the first page comes back short — page-size heuristics — so small result sets don't pay it.)
2. **Optimize the count:** provide `countQuery` yourself, stripped of unnecessary joins/fetches/ordering:

```java
@Query(value = "select o from Order o join fetch o.customer where o.status = :s",
       countQuery = "select count(o) from Order o where o.status = :s")
Page<Order> findByStatus(@Param("s") Status s, Pageable p);
```

(fetch joins are illegal/pointless in count queries; to-one joins that don't filter can be dropped; `count(distinct o)` only when collection joins multiply rows.)

3. **Cache/estimate totals:** counts change slowly relative to reads — cache per-filter counts with TTL (`@Cacheable`), or estimated counts (Postgres planner estimates via `EXPLAIN` row estimates or `reltuples`) for the "~12,400 results" UX; exact on demand.
4. **Separate endpoints:** `/orders?page=…` returns a Slice; `/orders/count` on explicit request — makes cost visible in the API design.

Also mention `PagedModel`/`PageImpl` custom assembly for the two-query pattern (Q33) — you often construct Pages manually there, reusing one count across pages of the same filter.

**Follow-up trap:** *"Why does `count(*)` differ from `count(o)`/`count(distinct o)` results on joined queries?"* — row multiplication by collection joins; counting entities vs rows is exactly the distinct question, and interviewers use it to check you understand what the join did to cardinality.

---

### Q87. Streaming and huge reads: export 5M rows to CSV without OOM. Compare `Stream<T>` repository methods, `ScrollableResults`, paging loops, and going JDBC — and list the session/context management rules for each.

**Answer:**

**Option 1 — `Stream<T>` repository method:**

```java
@QueryHints(@QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "1000"))
@Query("select o from Order o where o.createdAt >= :since")
Stream<Order> streamForExport(@Param("since") Instant since);
```

Rules: must run **inside an open transaction** (`@Transactional(readOnly = true)`) for the duration of consumption; **try-with-resources** to close the underlying cursor; set **JDBC fetch size** (MySQL needs `Integer.MIN_VALUE` for true streaming; Postgres needs fetchSize > 0 *and* autocommit off — transaction handles that); and critically, **the persistence context still accumulates** every streamed entity → periodically `em.detach(entity)`/`em.clear()` (or use read-only session hints) or you OOM anyway. That last point is the one interviewers fish for.

**Option 2 — DTO stream:** stream projections instead of entities → no persistence-context accumulation at all; usually the correct combination (stream + DTO).

**Option 3 — paging loop (keyset):** stateless chunk reads (Q85), new short transaction per chunk — resilient (restartable by cursor), friendlier to pools/replicas; slightly more code. Best for very long exports where holding one transaction/connection for minutes is unacceptable (vacuum/undo pressure, failover behavior).

**Option 4 — drop to JDBC/`JdbcTemplate` with a `RowCallbackHandler`** (or Spring Batch `JdbcCursorItemReader`): no ORM overhead at all for a flat CSV — often the professional choice for pure exports; JPA adds nothing to row→CSV.

Ranking for the scenario: keyset-chunked DTO reads or JDBC for operational robustness; `Stream` + DTO + fetch-size for simplicity when a bounded single transaction is fine. Entities-with-stream only if you truly need mapped objects, with clear()-discipline.

**Follow-up trap:** *"Why does the naive `findAll()` OOM twice as fast as expected?"* — entities + their snapshots (dirty-checking copies) ≈ 2× memory; `readOnly=true` drops the snapshots — a concrete, quotable win (Q58).

---

### Q88. `@Formula`, `@Where`/`@SQLRestriction`, `@Filter` — Hibernate's query-shaping annotations. Explain each with a use case, their interaction with associations, and their pitfalls (especially `@Where` on collections and native queries).

**Answer:**

- **`@Formula("(select count(*) from review r where r.product_id = id)")`** — read-only virtual column computed by a SQL fragment inlined into every SELECT of the entity. Uses: derived counts/flags, denormalized-ish reads without a real column. Pitfalls: runs per row on *every* load (hidden cost on lists), native SQL fragment = dialect-bound and invisible to JPQL semantics, not filterable efficiently (no index), silently breaks if schema drifts (no validation).
- **`@Where(clause = "deleted = false")`** (Hibernate 6.3+: **`@SQLRestriction`**) — a static predicate appended to every entity load (queries, finds, and — when placed on a collection — collection loads). Primary use: **soft deletes** (Q96) and "active-only" views. Pitfalls to recite: it is *unconditional* — you can't turn it off per query (admin screens that must see deleted rows need native SQL or a separate mapping); **native queries bypass it**; `em.find(id)` on a filtered-out row returns... it *does* apply to find in Hibernate — but a `@ManyToOne` pointing at a soft-deleted row yields `EntityNotFoundException`-style breakage (association resolution finds nothing) — the referential-integrity-vs-filtering tension is THE soft-delete interview point; and on collections it silently hides children (sums/counts wrong without anyone noticing).
- **`@FilterDef`/`@Filter`** — *parameterized, per-session, opt-in* predicates:

```java
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Entity public class Order { … }

session.enableFilter("tenantFilter").setParameter("tenantId", tid);
```

Uses: **row-level multi-tenancy** (enable via an AOP aspect/`OncePerRequestFilter` unwrapping the Hibernate `Session`), effective-dating, region scoping. Strengths over `@Where`: dynamic parameter, toggleable. Pitfalls: must be **enabled per session** — forget once (async job! Q68) and tenant data leaks — so centralize enabling and test it; filters don't apply to `em.find` by id (direct id load bypasses filters — a documented, dangerous nuance: fetch-by-id of another tenant's row succeeds unless you also validate); native queries bypass; caching interactions (filtered collections and 2LC don't mix well).

**Follow-up trap:** *"So how do you make multi-tenant by-id loads safe?"* — never trust raw `findById` for tenant data: repository methods like `findByIdAndTenantId`, or DB-level RLS (Postgres row-level security) as the enforcement backstop — defense in depth again.

---

### Q89. Connection pooling meets JPA: size a HikariCP pool for a service, explain the interaction between pool size, `@Transactional` boundaries, OSIV, and REQUIRES_NEW — and diagnose "connection is not available, request timed out after 30000ms".

**Answer:**

**Sizing:** the counterintuitive truth — small pools outperform big ones: `connections ≈ cores * 2 + effective_spindle/latency factor` (HikariCP's guidance; ~10–20 for typical services). More connections ≠ more throughput once the DB saturates; they just queue inside the DB instead of the app (plus Postgres per-connection overhead). Size for the **DB's** capacity across ALL instances (10 pods × 20 = 200 server connections — do that math out loud; PgBouncer when instance count grows).

**What holds a connection:** by default the connection is acquired at first statement and held until transaction end (Hibernate's delayed-acquisition/`release_mode` defaults do the sane thing in Boot). Therefore connection-hold-time ≈ transaction length ⇒ every anti-pattern that stretches transactions stretches pool occupancy: remote calls inside `@Transactional` (Q66), think-time/OSIV render-time lazy loads (OSIV holds/reacquires during rendering — Q40), giant chunked jobs in one tx, and **REQUIRES_NEW doubling per-thread demand** (Q64's self-deadlock).

**Diagnosing the timeout:** it means all pool connections were busy for 30s — almost never "pool too small" first. Playbook: (1) Hikari metrics — `active`, `pending`, `usage` histogram, and enable **`leakDetectionThreshold=60000`** (logs stack traces of holders — finds the leaking/held path immediately); (2) DB side: `pg_stat_activity` — what are those connections doing (idle-in-transaction = app holding txs open — the smoking gun; long queries = DB problem); (3) look for the classic causes: remote call in tx, missing `@Transactional` causing per-statement autocommit churn (different signature), unclosed streams (Q87), REQUIRES_NEW storms, or a genuinely slow query serializing everything behind row locks (Q77 family).

Then fix the *hold time*, and only then consider size. Also name: separate pools for interactive vs batch workloads, and `connectionTimeout` tuned so requests fail fast into backpressure rather than stacking.

**Follow-up trap:** *"Why is `idle-in-transaction` so bad on Postgres specifically?"* — it blocks vacuum from cleaning dead tuples (bloat) and can hold locks; `idle_in_transaction_session_timeout` is the server-side guard — knowing that setting is a strong ops signal.

---

### Q90. Schema management: the team uses `spring.jpa.hibernate.ddl-auto=update` in production "because it's convenient." Make the case for Flyway/Liquibase, explain each ddl-auto mode and `validate`'s role, and describe a safe expand–contract migration for renaming a column with zero downtime.

**Answer:**

**ddl-auto modes:** `none`; `validate` — compare mappings to schema at startup, fail on mismatch (no changes); `update` — attempt additive sync (never drops columns/constraints reliably, can't rename — it sees rename as add, leaves orphans, no rollback, no review, race-y with multiple instances); `create`, `create-drop` — dev/test only. **`update` in prod is disqualifying**: no history, no code review of DDL, partial failure states, and destructive surprises on mapping refactors.

**Migrations (Flyway/Liquibase):** versioned, ordered, immutable SQL scripts in the repo (`V7__add_customer_tier.sql`), applied transactionally where the DB supports transactional DDL (Postgres yes, MySQL no — worth saying), recorded in a history table, reviewed like code, identical across envs, and runnable in CI against Testcontainers. Pair with **`ddl-auto=validate`** so the app refuses to start when entities and migrated schema drift — migrations are the source of truth, validate is the tripwire.

**Zero-downtime rename (expand–contract)** — because old and new app versions overlap during rolling deploys:

1. **Expand:** migration adds `customer_tier` alongside `tier`; deploy app version writing **both** columns (or a DB trigger syncing), reading old.
2. **Backfill:** batched `UPDATE` migrating existing rows (chunked to avoid long locks/replication lag).
3. **Switch reads:** next app version reads new column (still writing both).
4. **Contract:** once no old version runs, stop writing old; final migration drops `tier`.

Same choreography answers "add NOT NULL column" (add nullable + default → backfill → add constraint `NOT VALID`/validate on Postgres) and "change type." Naming expand–contract, and the constraint that **every deploy must be compatible with schema N and N+1**, is precisely the senior-level answer interviewers seek.

**Follow-up trap:** *"Where do JPA entities fit during step 1?"* — entities can map both fields temporarily (`insertable/updatable` games) or the trigger handles it below the ORM; either way the entity model follows the migration plan, never drives it in prod.

---

# Section 10 — Specifications, Auditing, Inheritance, Soft Deletes & Testing (Q91–Q100)

---

### Q91. Build a product-search endpoint with ~10 optional filters (name contains, price range, category in, in-stock, seller rating…). Show the Specification pattern properly — composable, reusable, join-safe — and compare it honestly with Criteria, Querydsl, and QBE.

**Answer:**

`JpaSpecificationExecutor<T>` adds `findAll(Specification<T>, Pageable)` etc. A `Specification<T>` is a lambda producing a Criteria `Predicate` — the win is **composition**:

```java
public final class ProductSpecs {
    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
    }
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get("price"), min, max);
    }
    public static Specification<Product> inCategories(Collection<Long> ids) {
        return (root, query, cb) -> root.get("category").get("id").in(ids);
    }
    public static Specification<Product> sellerRatingAtLeast(int r) {
        return (root, query, cb) -> {
            var seller = root.join("seller");            // JOIN for filtering
            return cb.ge(seller.get("rating"), r);
        };
    }
}

// service: build only what applies
Specification<Product> spec = Specification.allOf(
    q != null ? ProductSpecs.nameContains(q) : null,      // nulls are skipped
    min != null || max != null ? ProductSpecs.priceBetween(nvl(min), nvl(max)) : null,
    !cats.isEmpty() ? ProductSpecs.inCategories(cats) : null);
Page<Product> page = repo.findAll(spec, pageable);
```

Generated SQL contains **only** the active predicates (unlike the `:x is null or…` hack — Q42), paging/sorting compose free, and specs unit-test individually and recombine across endpoints. Join-safety details that mark seniority: repeated `root.join` across composed specs creates duplicate joins (harmless-ish but ugly — dedupe via checking `root.getJoins()` or structure specs to share joins); collection joins can duplicate roots → `query.distinct(true)` inside the spec, or prefer an `exists` subquery predicate; and count-query quirks (Spring builds the count from your spec — fetch joins inside specs break counts; never fetch in a filtering spec, fetch via `@EntityGraph` alongside).

**Comparison:** raw **Criteria** — same power, no composition sugar, verbose. **Querydsl** — generated Q-classes, fluent + compile-time-safe (`product.price.between(a,b)`), nicer than Criteria for heavy dynamic querying; extra build step (annotation processing), ecosystem maintenance has wobbled — evaluate before adopting. **QBE** — zero-code but capped (no ranges — Q50). Honest ranking for the scenario: Specifications by default in Spring shops; Querydsl if the team lives in dynamic queries; consider **JPA static metamodel** (`Product_.name`) to de-string Specifications — mentioning `root.get(Product_.name)` upgrades the answer.

**Follow-up trap:** *"How do you sort by the joined seller rating?"* — `Sort` by nested path `seller.rating` works when the join exists / Spring can path it; otherwise sort inside the spec via `query.orderBy` — knowing Sort's limits with ad-hoc joins is the expected depth.

---

### Q92. Same search endpoint, but now filters arrive as a JSON query DSL from the frontend (`{"and":[{"field":"price","op":"lt","value":100}, …]}`). Design the translation layer safely. What must you whitelist, and where do Specifications stop scaling?

**Answer:**

Design: parse JSON → validated AST → translate to `Specification` tree.

```java
public Specification<Product> toSpec(FilterNode node) {
    return switch (node) {
        case Group g when g.op() == AND -> Specification.allOf(g.children().stream().map(this::toSpec).toList());
        case Group g                     -> Specification.anyOf(g.children().stream().map(this::toSpec).toList());
        case Leaf leaf                   -> leafSpec(leaf);
    };
}
```

**Safety whitelist — the interview core:**

1. **Fields:** map external names → allowed attribute paths via an explicit registry (`"price" → Product_.price`); never `root.get(userSuppliedString)` — that's schema probing / data exfiltration via arbitrary attributes (imagine filtering on `passwordHash` with binary search!). The registry also carries per-field **allowed operators** and **type coercion** (string → `BigDecimal`/enum with validation).
2. **Operators:** closed enum; no raw SQL fragments ever.
3. **Complexity budget:** cap depth/branch count (DoS via 10,000-clause OR), cap IN-list sizes (Q83), require at least one selective/indexed predicate or force pagination limits — query-cost governance, not just injection defense.
4. **AuthZ predicates appended server-side** (tenant/ownership) — client DSL can only *narrow*, never widen (combine with the mandatory base spec).

**Where Specifications stop scaling:** free-text relevance search (→ Postgres FTS/OpenSearch), aggregation-heavy faceting (counts per filter value — DTO/native/OLAP), cross-aggregate joins that JPQL models poorly, and query plans — arbitrary ad-hoc predicates mean unpredictable index usage; at some point you constrain the product ("filters must include category") or move to a search engine and keep the RDBMS as source of truth (CQRS-lite: index projections, search there, hydrate by ids from JPA — a tidy architecture close).

**Follow-up trap:** *"How do you test this layer?"* — property-style tests per operator + adversarial cases (unknown field, wrong type, huge trees) + `@DataJpaTest` verifying generated SQL touches expected indexes (EXPLAIN assertions on critical shapes).

---

### Q93. Auditing: every entity needs createdAt/By, updatedAt/By; auditors also demand "who changed what field, when, from what to what." Implement Spring Data auditing for the first, and present the options for full change history (Envers vs manual vs CDC).

**Answer:**

**Level 1 — column auditing (Spring Data):**

```java
@Configuration
@EnableJpaAuditing
class AuditConfig {
    @Bean AuditorAware<String> auditorAware() {     // who: from Spring Security
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
    }
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedDate      @Column(updatable = false) private Instant createdAt;
    @CreatedBy        @Column(updatable = false) private String createdBy;
    @LastModifiedDate private Instant updatedAt;
    @LastModifiedBy   private String updatedBy;
}
```

Gotchas to volunteer: bulk `@Modifying` queries bypass it (Q43); background jobs have no `SecurityContext` → AuditorAware must handle "system" principals; `@EnableJpaAuditing(dateTimeProviderRef=…)` for fixed clocks in tests.

**Level 2 — full history:**

- **Hibernate Envers** (`@Audited`): writes `<table>_AUD` rows + a global revision table per transaction; query historical states via `AuditReader` ("entity as of revision/date", diffs between revisions). Strengths: transparent, transactional (history commits atomically with data), mature. Costs: writes amplify (audit insert per change), schema doubles, queries on audit tables are clunky, bulk ops bypass it, and revision tables grow forever (plan archival).
- **Manual audit events**: entity listeners/domain events writing an `audit_log` (JSON diff) — full control of shape/PII redaction, but you must compute diffs (old vs new state — `PostUpdateEvent` gives old/new state arrays via Hibernate event listeners; naming `PostUpdateEventListener` is the depth marker) and it's easy to miss paths.
- **CDC (Debezium)**: tail the WAL/binlog — zero app overhead, catches *every* writer including native SQL and other apps (the auditors' actual requirement!), but infra-heavy and "who" (application user) isn't in the WAL — pair by stamping `updated_by` columns or session variables.

Pick per requirement: compliance "every change with actor" → Envers or app-level with strict discipline; forensic completeness across writers → CDC + actor columns; both is common (Envers for product features like "version history", CDC for the data platform).

**Follow-up trap:** *"Does Envers version your associations?"* — audited associations must also be `@Audited` (or marked `@NotAudited`/`RelationTargetAuditMode.NOT_AUDITED`) — mapping compilation fails otherwise; historical graph reconstruction spanning revisions is Envers' hairiest corner.

---

### Q94. Inheritance mapping: `Payment` with `CardPayment`, `UpiPayment`, `BankTransferPayment`. Compare SINGLE_TABLE, JOINED, TABLE_PER_CLASS and `@MappedSuperclass` — schema, SQL, constraints, polymorphic queries — and pick for (a) 3 subtypes/hot path, (b) subtypes with many non-null-required distinct columns.

**Answer:**

- **SINGLE_TABLE** (default): one table, all subtype columns, `@DiscriminatorColumn` picks the type. SQL: no joins ever — fastest reads/writes; polymorphic queries trivial (`select p from Payment p` = plain scan). Cost: subtype-specific columns must be **nullable** (can't put NOT NULL on `card_last4`) — integrity moves to app/CHECK constraints (`CHECK (dtype <> 'CARD' OR card_last4 IS NOT NULL)` — offering the CHECK is the pro move); wide sparse table.
- **JOINED**: base table + one table per subtype sharing PK. Clean normalized schema, real NOT NULLs per subtype. Cost: loading = join base+subtype (polymorphic load = base outer-joined to *every* subtype table); inserts hit two tables; deep hierarchies multiply joins.
- **TABLE_PER_CLASS**: one complete table per concrete class, no base table. Polymorphic queries = `UNION ALL` across tables (ugly plans); identity can't use IDENTITY (ids must be unique across tables → sequence/table generator). Rarely chosen; know why it exists (no joins for concrete-type access) and its costs.
- **`@MappedSuperclass`**: *not* entity inheritance — shared fields only (audit base, Q93); no polymorphic queries (`select p from Payment p` impossible), no relationships to the supertype. Use when you want code reuse, not a type hierarchy in the DB.

**Choices:** (a) 3 subtypes, few extra columns, hot path → **SINGLE_TABLE** + CHECK constraints (performance + simplicity; the null-ability compromise is contained). (b) many mandatory subtype-specific columns → **JOINED** (schema integrity worth the joins) — or step outside the menu: composition over inheritance — a single `Payment` entity + JSON details column or a `@OneToOne` per detail type; ORM inheritance is often the wrong hammer, and saying so with reasons is senior.

JPQL extras: `TYPE(p) = CardPayment` filters by subtype; `TREAT(p as CardPayment).cardNetwork` downcasts in paths; `@DiscriminatorValue` customizes tags. Proxy caveat from Q38: a lazy `@ManyToOne Payment` proxy is created for the *base* type and never passes `instanceof CardPayment` — polymorphic to-ones + lazy = trouble (avoid or fetch eagerly/via query).

**Follow-up trap:** *"What does `save()` on a subtype write in each strategy?"* — SINGLE_TABLE: 1 insert with discriminator; JOINED: 2 inserts (base+sub); TABLE_PER_CLASS: 1 insert into the concrete table. Walking the SQL confirms you truly hold the model.

---

### Q95. Soft deletes end-to-end: legal wants "deleted" records recoverable; product wants them invisible everywhere. Implement with `@SQLDelete` + `@SQLRestriction`, then enumerate every place the illusion leaks — uniques, associations, cascades, counts, native SQL — and mitigation for each.

**Answer:**

**Implementation:**

```java
@Entity
@SQLDelete(sql = "update customer set deleted = true, deleted_at = now() where id = ? and version = ?")
@SQLRestriction("deleted = false")           // Hibernate 6.3+ (was @Where)
public class Customer {
    // …
    private boolean deleted = false;
    private Instant deletedAt;
}
```

`repository.delete(c)` now issues the UPDATE instead of DELETE (include `version` in the template when versioned — keeps optimistic locking honest); every entity query auto-appends `deleted = false`. Hibernate 6.4+ also offers native **`@SoftDelete`** — annotation-driven, cleaner (converts strategies, works with `@ElementCollection`) — naming it shows currency.

**The leaks (this list is the interview):**

1. **Unique constraints:** deleted row still occupies `email` → re-registration fails. Fix: partial unique index `… where deleted = false` (Postgres), or include `deleted_at` in the unique key (nullable trick).
2. **Associations:** another entity's `@ManyToOne` to a soft-deleted customer — association resolution now finds nothing → `EntityNotFoundException`/broken graphs (Q88). Decide semantics: forbid soft-deleting referenced rows (existence check), cascade soft-deletes down the aggregate (explicit service logic — `@SQLDelete` doesn't cascade!), or tolerate via `@NotFound(IGNORE)` (eager cost).
3. **Cascades/orphanRemoval:** JPA cascade REMOVE triggers the children's `@SQLDelete` only if children are also soft-deletable and loaded; bulk/`ON DELETE` paths bypass entirely. Aggregate-level soft-delete must be orchestrated in the service.
4. **FK constraints with real deletes elsewhere:** mixed hard/soft worlds — document per-table policy.
5. **Native queries & other apps bypass `@SQLRestriction`** → views (`customer_active`) for reporting, or RLS as enforcement.
6. **Counts/aggregations via collections:** filtered collections silently change sums — audits comparing raw SQL to app numbers will "find" discrepancies; brief your analysts.
7. **The escape hatch:** admin/recovery screens need the deleted rows — `@SQLRestriction` can't be disabled per query → separate native queries, a second unfiltered entity mapping, or (better) `@FilterDef`-based soft-delete instead of `@SQLRestriction` when togglability matters (trade: must enable everywhere — Q88).
8. **Uniqueness of the pattern's alternative:** move deleted rows to an archive table (hard delete + copy) — restores clean semantics for the live table; recovery = reinsert. Offering this alternative (and GDPR anonymization instead of deletion) rounds out the answer.

**Follow-up trap:** *"`findById` on a soft-deleted id?"* — with `@SQLRestriction` it returns empty (restriction applies) — verify per Hibernate version; with `@Filter` it would *bypass* (filters skip direct id loads). The two mechanisms differ exactly where it's dangerous — that contrast is the expert beat.

---

### Q96. Multi-tenancy with Spring Data JPA: compare DATABASE, SCHEMA, and DISCRIMINATOR (shared-schema) strategies; sketch the Hibernate integration points (`CurrentTenantIdentifierResolver`, `MultiTenantConnectionProvider`); and harden the shared-schema variant.

**Answer:**

**Strategies:**

- **DATABASE per tenant:** strongest isolation (backup/restore, noisy-neighbor, compliance per tenant); cost: N pools/connections, migrations × N, cross-tenant reporting hard, tenant onboarding = infra.
- **SCHEMA per tenant:** one DB, schema per tenant — middle ground; same migration-multiplication issue; connection switched by `SET search_path`/schema on checkout.
- **DISCRIMINATOR (shared schema):** every table has `tenant_id`; cheapest ops, best density; correctness burden shifts entirely to *filtering discipline* — one missed predicate = data breach.

**Hibernate integration** (DATABASE/SCHEMA modes): implement `CurrentTenantIdentifierResolver` (reads tenant from a request-scoped context — ThreadLocal/MDC populated by a filter from JWT/subdomain) + `MultiTenantConnectionProvider` (hands out a connection for the tenant — separate datasource, or shared datasource with `SET search_path`), configure `hibernate.multiTenancy`/`multi_tenant_connection_provider`. Spring Data repositories are unchanged — tenancy resolves beneath them. Session-per-tenant caveat: 2LC must be tenant-aware (Hibernate keys cache by tenant id — verify), and `@Transactional` boundaries must not span tenant switches.

**Discriminator mode hardened** (Hibernate 6 has first-class discriminator multitenancy — `@TenantId` on a column: auto-filters queries and blocks cross-tenant writes; before that, `@FilterDef`-based — Q88):

1. `@TenantId`/filter applied via one base `@MappedSuperclass` — no per-entity opt-ins to forget.
2. **By-id loads validated** (filters/`@TenantId` gaps around direct loads — repository wrappers enforcing `AndTenantId`).
3. **Postgres RLS as the backstop:** `CREATE POLICY tenant_isolation … USING (tenant_id = current_setting('app.tenant'))` with the setting bound per transaction — the DB enforces isolation even against native queries and bugs. Defense in depth is the phrase to land.
4. Async/batch context propagation (`TaskDecorator` copying the tenant ThreadLocal — Q68's lesson applied).
5. Tests: a CI suite that runs every repository method as tenant A against tenant B's data and asserts zero rows — treat cross-tenant leakage as a test-able invariant.

**Follow-up trap:** *"Composite unique keys?"* — every 'unique' business key becomes unique-per-tenant: `(tenant_id, email)` — schema-wide sweep required when converting single- to multi-tenant; interviewers love this migration detail.

---

### Q97. Testing data-access: `@DataJpaTest` — what it configures, why the default rollback can hide bugs, the H2-vs-production-DB trap, and the `TestEntityManager` flush/clear idiom that makes tests actually meaningful.

**Answer:**

**`@DataJpaTest`** boots a slice: JPA + repositories + a test `DataSource` (by default swaps in embedded H2 if present), Flyway/schema init, `TestEntityManager`; excludes web/services. Each test runs in a **transaction rolled back at the end** — fast, isolated, no cleanup.

**Rollback hides bugs:** if nothing forces a flush, `repository.save(...)` may queue SQL that *never executes* (rollback before flush) — constraint violations, `@Column(nullable=false)` breaches, bad DDL mappings all pass silently. Also, everything in one transaction = one persistence context: reads return the **same managed instances you saved** (L1 cache — Q54), so your "did it persist correctly?" assertion compares an object to itself. The idiom:

```java
@Autowired TestEntityManager em;

@Test
void savesOrderGraph() {
    Order saved = repo.save(order());
    em.flush();                 // force SQL — constraints actually checked
    em.clear();                 // detach everything — next read is REAL
    Order found = repo.findById(saved.getId()).orElseThrow();
    assertThat(found.getItems()).hasSize(2);     // now proves mapping, not memory
}
```

`flush(); clear();` between arrange and assert is the difference between testing Hibernate's cache and testing your mapping. For code relying on commit semantics (after-commit listeners, REQUIRES_NEW interplay), disable rollback (`@Rollback(false)`/`@Commit`) or test at service level with real transactions.

**H2 trap:** H2 isn't your production dialect — native queries, sequences/identity behavior, `on conflict`, JSON ops, constraint messages, locking (`SKIP LOCKED`!) differ or silently no-op. H2 compatibility modes reduce but don't remove drift. Modern standard: **Testcontainers**:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class OrderRepositoryIT {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");
}
```

(`@ServiceConnection` — Boot 3.1+ — wires it automatically; reuse one container across tests for speed.) Run migrations in the container → tests validate SQL, DDL, *and* migrations together. Keep H2 (if at all) only for pure-JPQL crud slices where speed rules.

**Follow-up trap:** *"Why did the `@Transactional` test pass but production fail with LazyInitializationException?"* — the test's single wrapping transaction kept the session open for the whole test — lazy loads worked everywhere; production has real boundaries. Slice tests must mirror boundary structure (or assert fetch plans explicitly) — the best closing insight for this question.

---

### Q98. Beyond repositories in tests: how do you assert *behavior* like "this endpoint issues exactly 3 SQL statements", "no N+1 on this flow", and "this migration applies cleanly to a production-shaped schema"? Tooling and patterns.

**Answer:**

**Statement-count assertions:** wrap the datasource with a proxy that counts. Standard picks: `datasource-proxy` + assertions on `QueryCount` (via e.g. Vlad Mihalcea's `db-util` `SQLStatementCountValidator`: `assertSelectCount(1)`), or Hibernate's `Statistics` (`getPrepareStatementCount`, per-query stats) reset per test:

```java
SQLStatementCountValidator.reset();
orderService.getOrderScreen(id);
SQLStatementCountValidator.assertSelectCount(2);   // fails when someone reintroduces N+1
```

Put these on the top-5 hot flows; an N+1 regression then fails CI with a diff of expected-vs-actual counts — this converts Q31's whole topic from vigilance into automation, and saying that is the point.

**Fetch-plan assertions:** `assertThat(Hibernate.isInitialized(order.getItems())).isTrue()` after the query proves the join fetch/EntityGraph actually applied (catches silently-dropped graphs after refactors); `PersistenceUnitUtil.isLoaded` is the JPA-standard spelling.

**Query-shape/EXPLAIN tests:** for critical queries, integration tests against Testcontainers running `EXPLAIN` and asserting no `Seq Scan` on the big table (brittle if overused — reserve for the queries that have burned you; encode as "plan uses index X").

**Migration tests:** CI job that starts Postgres in Testcontainers, applies **all** Flyway migrations from scratch (validates the full chain), plus — the production-shaped part — restore a masked/sampled schema dump (or schema-only dump) and apply *pending* migrations against it: catches "works on empty DB, locks/fails on real data" (adding NOT NULL to a huge table). Also assert `ddl-auto=validate` passes post-migration → entities and schema agree (Q90 loop closed).

**Concurrency tests:** optimistic-lock and SKIP LOCKED behavior deserve real tests — two threads/two `TransactionTemplate`s racing on one row asserting one `ObjectOptimisticLockingFailureException` (Q71–75) — awkward but writable, and interviewers rarely meet candidates who've done it; describing the two-transaction-template pattern is a differentiator.

**Follow-up trap:** *"Won't count assertions break on harmless changes?"* — yes, they're intentionally sensitive; keep them on flows where a count change *is* news, assert ranges where appropriate, and treat updating the number as a conscious review decision — that's process design, the actual senior skill.

---

### Q99. Two databases in one Spring Boot app (orders DB + legacy reporting DB), each with JPA entities and repositories. Wire it: what must be duplicated, how transactions behave, and the pitfalls (primary flags, entity scanning, migrations, the "one @Transactional spanning both" trap).

**Answer:**

Boot's auto-config handles exactly one datasource; the second requires explicit wiring — per store: `DataSource` → `LocalContainerEntityManagerFactoryBean` (own persistence unit, own `packages`) → `JpaTransactionManager`, with repositories partitioned by package:

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.acme.orders.repo",
    entityManagerFactoryRef = "ordersEmf", transactionManagerRef = "ordersTx")
class OrdersJpaConfig {
    @Bean @Primary @ConfigurationProperties("app.datasource.orders")
    DataSource ordersDs() { return DataSourceBuilder.create().build(); }

    @Bean @Primary
    LocalContainerEntityManagerFactoryBean ordersEmf(EntityManagerFactoryBuilder b) {
        return b.dataSource(ordersDs()).packages("com.acme.orders.domain").build();
    }
    @Bean @Primary
    PlatformTransactionManager ordersTx(@Qualifier("ordersEmf") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
// mirror config (no @Primary) for reporting: own packages, own refs
```

**Pitfalls checklist:**

1. **`@Primary` on exactly one** of each bean type — or every unqualified injection/auto-config breaks; forgetting it yields "expected single matching bean" storms.
2. **Entity/repository package separation is the routing mechanism** — an entity in the wrong package lands in the wrong persistence unit ("Not a managed type: …" — *the* symptom to recognize).
3. **`@Transactional` picks ONE manager:** default = primary. `@Transactional("reportingTx")` for the other. The trap: one method writing to **both** DBs is **not atomic** — two separate transactions; the second can fail after the first committed. Options honestly ranked: restructure so each use case writes one DB (+ events/outbox to sync — Q70); JTA/XA (Atomikos) if you truly need atomicity and can pay for it; never `ChainedTransactionManager` (deprecated illusion — Q70).
4. **JPA properties no longer auto-apply** — dialect, naming strategy, ddl-auto must be set per EMF (`b.properties(map)`); teams lose the snake_case naming strategy on DB2 and wonder why columns mismatch (Q16 recurrence).
5. **Migrations per DB:** two Flyway beans with distinct locations/history tables (Boot auto-configures only one — customize via `Flyway` beans or spring.flyway on primary + manual second).
6. Repositories can't join across DBs — cross-DB "joins" happen in the service (or FDW/replication at the DB layer — name the alternatives).

**Follow-up trap:** *"Read replica of the SAME schema — same solution?"* — No: prefer one persistence unit + routing datasource (`AbstractRoutingDataSource` keyed on `readOnly`, with `LazyConnectionDataSourceProxy` so routing happens at first statement — Q58) — two full JPA stacks for identical schemas is overengineering; distinguishing the two scenarios is the senior answer.

---

### Q100. Capstone — the audit question: you inherit a Spring Data JPA codebase. List your top-15 review checklist — the misconfigurations and anti-patterns you actively hunt for, each with the one-line reason. (This is how staff-level interviews end.)

**Answer:**

1. **`open-in-view=true`** (default!) — decide consciously; usually disable → forces real fetch design (Q40).
2. **EAGER on `@ManyToOne`/`@OneToOne`** — spec defaults; hunt every association without explicit `LAZY` (Q39/Q24).
3. **No `hibernate.default_batch_fetch_size`** — the free N+1 safety net is absent (Q36).
4. **IDENTITY ids on write-heavy tables** — batching silently dead; SEQUENCE + pooled optimizer (Q12).
5. **No JDBC batching config** despite batch jobs — inserts trickle one-by-one (Q57/80).
6. **Entities serialized straight out of controllers** — recursion/LIE/overexposure; demand DTOs (Q30).
7. **`ddl-auto=update` anywhere near prod** — migrate with Flyway + `validate` (Q90).
8. **Checked-exception rollback assumption / `rollbackFor` absent** as team default — silent commits after failures (Q62).
9. **Remote calls inside `@Transactional`** — pool-drain outage pattern; restructure boundaries (Q66/89).
10. **No `@Version` on concurrently-edited aggregates** — lost updates by design (Q71); plus REST layer not echoing versions (Q76).
11. **`findAll()` / unpaged queries on unbounded tables** — time bombs; paging or keyset everywhere data grows (Q85).
12. **Derived `deleteBy…` / cascade REMOVE on huge child sets** — row-by-row massacres; bulk ops with context hygiene (Q6/25).
13. **`@Transactional` self-invocation & private methods** — annotations that never ran (Q61); verify with tx logging once.
14. **Missing FK indexes** — DBs don't auto-index FKs (Postgres!); join/lock pathologies (Q77/79).
15. **Tests only on H2, natives unvalidated, no statement-count assertions** — the safety net has holes exactly where production differs (Q97/98).

Honorable mentions worth saying aloud: Lombok `@Data`/`toString` on entities (Q11), `ordinal` enums (Q14), query-cache enabled "for speed" (Q84), one `JpaRepository` per table instead of per aggregate (design smell), and connection pool sized by folklore (Q89). Close with the meta-point: almost every item is *invisible until load or concurrency arrives* — which is why JPA review is architecture review, and why this checklist is what six years of experience actually means.

**Follow-up trap:** *"Pick your top 3 to fix first."* — (1) transaction boundaries with remote calls (outage-class), (2) OSIV + fetch strategy (perf-class), (3) missing versioning (data-corruption-class) — triage by blast radius, and justify it. That prioritization answer *is* the staff-level signal.

---

## Closing advice

Work these in spaced repetition: Sections 1–3 are table stakes; Sections 4, 6, 7 are where mid-level candidates get rejected; Sections 8–10 are where senior offers get made. For every scenario, practice saying the answer *out loud* in 60–90 seconds: symptom → mechanism → fix → trade-off. That four-beat structure is what interviewers score, at every level.

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