# JPA Relationship Mapping 

## 1. What Is a Relationship? (Very Basic)

In real life:
- One **Customer** can place many **Orders**
- One **Bank Account** can have many **Transactions**
- One **User** can have one **Profile**

These are **relationships between data**.

In software, this data is stored in **tables** inside a **database**.

---

## 2. How Databases Store Relationships

Databases do **not** understand Java objects.

They only understand:
- Tables
- Rows
- Columns
- Keys

### Example: Bank System

```
ACCOUNT
-------
id (Primary Key)
balance

TRANSACTION
-----------
id (Primary Key)
amount
account_id (Foreign Key)
```

Here:
- `ACCOUNT.id` uniquely identifies an account
- `TRANSACTION.account_id` stores **which account a transaction belongs to**

This `account_id` column is called a **Foreign Key**.

---

## 3. What Is a Foreign Key? (Foundation)

A **Foreign Key (FK)** is:
> A column in one table that references the primary key of another table.

Purpose:
1. Ensure data correctness
2. Prevent invalid relationships
3. Enforce rules at database level

### Why FK Is Important

Without FK:
- A transaction could point to a non-existing account
- Deleting an account could leave orphan transactions

With FK:
- Database blocks invalid inserts
- Database controls deletes and updates safely

---

## 4. Parent and Child Tables

Rule:
> The table that owns the primary key is the **parent**  
> The table that contains the foreign key is the **child**

In our example:
- ACCOUNT → Parent
- TRANSACTION → Child

Direction:
```
TRANSACTION ---> ACCOUNT
   (FK)        (PK)
```

---

## 5. There Is NO OneToMany in Database

This is critical to understand.

Databases do NOT have:
- OneToMany
- ManyToOne

They ONLY have:
> **Many rows holding a foreign key pointing to one row**

So:
- One Account
- Many Transactions

is implemented using:
```
TRANSACTION.account_id
```

---

## 6. What Is JPA?

**JPA (Java Persistence API)** is a specification that allows Java objects to be stored in databases.

JPA does NOT:
- Replace database rules
- Enforce foreign keys

JPA ONLY:
> Maps database columns to Java fields

Hibernate is the most common JPA implementation.

---

## 7. What Is Relationship Mapping in JPA?

Relationship mapping means:
> Telling JPA how Java objects are connected **through foreign keys**

JPA must know:
1. Where is the foreign key?
2. Which object owns it?
3. How objects should load and save together?

---

## 8. The Owning Side (Most Important Concept)

> The entity that contains the foreign key is the **owning side**

Owning side:
- Controls database updates
- Writes the FK value
- Uses `@JoinColumn`

Inverse side:
- Does NOT control FK
- Uses `mappedBy`
- Exists only for navigation

---

## 9. @ManyToOne – The Most Important Mapping

This is the **only mandatory relationship mapping**.

```java
@Entity
class Transaction {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
```

What this means:
- Each Transaction belongs to ONE Account
- Foreign key column = `account_id`
- Transaction table owns the relationship

This alone is enough for:
- Inserts
- Updates
- Deletes
- Joins

---

## 10. @JoinColumn – Connecting Java to FK

`@JoinColumn` tells JPA:
> “This Java field maps to a foreign key column”

```java
@JoinColumn(name = "account_id")
```

Important attributes:
- `name` → FK column name
- `nullable = false` → child cannot exist without parent
- `unique = true` → one-to-one mapping
- `updatable = false` → FK cannot change
- `referencedColumnName` → reference non-PK (must be unique)

Without `@JoinColumn`, Hibernate guesses column names — risky in production.

---

## 11. Is @OneToMany Mandatory? (Very Important Question)

**NO.**

```java
@OneToMany(mappedBy = "account")
private List<Transaction> transactions;
```

This mapping:
- Does NOT create a foreign key
- Does NOT affect database
- Exists only for Java convenience

Database relationship already exists via `@ManyToOne`.

---

## 12. Why @OneToMany Is Optional

You can always fetch children using queries:

```java
findTransactionsByAccountId(accountId)
```

Using `@OneToMany`:
- Increases memory usage
- Risks loading huge collections
- Introduces synchronization complexity

---

## 13. When You SHOULD Use @OneToMany

Use it ONLY when:
- Parent controls child lifecycle
- Child cannot exist independently
- Cascade and orphan removal are needed

Example:
- Order → OrderItems
- Invoice → LineItems

```java
@OneToMany(
    mappedBy = "order",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

---

## 14. Bidirectional Relationship (Two-Way Navigation)

When both entities reference each other:

```java
Transaction -> Account
Account -> Transactions
```

JPA does NOT sync both sides automatically.

You must do:

```java
tx.setAccount(account);
account.getTransactions().add(tx);
```

---

## 15. Cascade – What Moves Together?

Cascade controls:
> What happens to child when parent changes

Common cascades:
- PERSIST → save child
- REMOVE → delete child (dangerous)
- ALL → everything

Cascade does NOT:
- Create foreign keys
- Enforce database rules

---

## 16. Fetch Types – When Data Loads

- EAGER → load immediately
- LAZY → load only when accessed

Best practice:
> Use LAZY everywhere and control loading via queries

---

## 17. ManyToMany – Use Carefully

Many-to-many requires a join table:

```
USER_ROLE
---------
user_id
role_id
```

This adds complexity and should be avoided in core business models.

---

## 18. Common Beginner Mistakes

1. Thinking @OneToMany creates FK
2. Using EAGER everywhere
3. Cascading REMOVE blindly
4. Not syncing both sides
5. Designing relationships only in Java

---

## 19. Production-Level Advice

- Always design database first
- Always know where the foreign key is
- Prefer unidirectional mappings
- Use repositories instead of collections
- Index foreign keys

---

## 20. Final Golden Rule

> **Databases define relationships.  
> JPA only maps them into Java objects.**

If you understand where the foreign key is,
you understand JPA relationship mapping.
