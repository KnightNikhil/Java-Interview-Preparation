# DBMS

## Contents

1. Relational Database Basics  
2. SQL Language Proficiency  
3. Indexing  
4. Transactions & Isolation Levels  
5. Database Normalization  
6. JPA & ORM Integration  
7. Spring Data JPA  
8. Connection Pooling  
9. Database Performance Tuning  
10. NoSQL (optional)  
11. Schema Migration Tools  
12. Testing with Databases  
13. Database in Production

---

## 1. Relational Database Basics

- **What is a relational database?**  
  A relational database stores data in tables (relations) organized into rows and columns. Tables are related using keys; the structure is defined by a schema and data integrity is enforced using constraints.

- **RDBMS vs NoSQL** (see section 2 for details)

- **Key concepts:**  
  - Tables, rows, columns  
  - Primary key, foreign key, indexes  
  - ACID properties (Atomicity, Consistency, Isolation, Durability)

**Example**

CUSTOMER table:

| customer_id | name   | email            |
|-------------|--------|------------------|
| 1           | Nikhil | nikhil@gmail.com |

ORDER table:

| order_id | customer_id | amount |
|----------|-------------|--------|
| 101      | 1           | 5000   |

- `customer_id` links ORDER → CUSTOMER — core relational concept.

**Popular RDBMS:** MySQL, PostgreSQL, Oracle, SQL Server

---

## 2. SQL Language Proficiency

### 1. DDL — Data Definition Language
Purpose: defines and modifies database structure.

```sql
CREATE TABLE ACCOUNT (
  account_id INT PRIMARY KEY,
  customer_name VARCHAR(50),
  balance DECIMAL(10,2)
);
```

- `ALTER TABLE ACCOUNT ADD COLUMN account_type VARCHAR(20);`
- `DROP TABLE ACCOUNT;`️ - Data and structure are lost.

### 2. DML — Data Manipulation Language
Purpose: works with actual data inside tables.

```sql
INSERT INTO ACCOUNT VALUES (101, 'Nikhil', 50000);
SELECT customer_name, balance FROM ACCOUNT WHERE balance > 10000;
UPDATE ACCOUNT SET balance = balance + 1000 WHERE account_id = 101;
DELETE FROM ACCOUNT WHERE account_id = 101;
```

### 3. DCL — Data Control Language
Purpose: controls access and permissions.

```sql
GRANT SELECT, INSERT ON ACCOUNT TO app_user;
REVOKE INSERT ON ACCOUNT FROM app_user;
```

### 4. TCL — Transaction Control Language
Purpose: manages transactions.

```sql
COMMIT;
ROLLBACK;
SAVEPOINT sp1;
ROLLBACK TO sp1;
```

### 5. Joins (VERY IMPORTANT)

**What is a JOIN?**
- A JOIN is used to combine rows from two or more tables based on a related column (usually a Primary Key – Foreign Key relationship).

**Why JOINs exist**
-	Data normalization splits data across tables
-	JOINs reconstruct meaningful information

- INNER, LEFT (LEFT OUTER), RIGHT (RIGHT OUTER), FULL OUTER, SELF JOIN, CROSS JOIN
- Subqueries, `IN`, `EXISTS`, `GROUP BY`, `HAVING`, `ORDER BY`
- Views, stored procedures, triggers (basics)

**Examples:**

Basic JOIN Syntax
```sql
SELECT columns
FROM table1
JOIN table2
ON join_condition;
```
-	ON → join condition
-	Result set is formed logically, not stored

**Tables Used in Examples**

CUSTOMER table:

| customer_id | name   | email            |
|-------------|--------|------------------|
| 1           | Nikhil | nikhil@gmail.com |
| 2           | Rahul  | rahul@mail.com   |
| 3           | Aman   | aman@mail.com    |    

ORDERS table:

| order_id | customer_id | amount |
|----------|-------------|--------|
| 101      | 1           | 5000   |
| 102      | 1           | 7000   |
| 103      | 4           | 3000   |


Notice:
-	Customer 4 does not exist in CUSTOMER
-	Customer 3 has no orders

### INNER JOIN
- Returns only matching rows from both tables.

Query
```sql
SELECT c.name, o.order_id, o.amount
FROM customer c
INNER JOIN orders o
ON c.customer_id = o.customer_id;
```

Result

| name   | order_id  | amount  |
|--------|-----------|---------
| Nikhil | 101       | 5000    |
| Nikhil | 102       | 7000    |

Key Points
-	Excludes unmatched rows
-	Most commonly used join
-	Default JOIN is INNER JOIN

### LEFT JOIN (LEFT OUTER JOIN)

Returns:
-	All rows from left table
-	Matching rows from right table
-	NULL if no match

Query
```sql
SELECT c.name, o.order_id
FROM customer c
LEFT JOIN orders o
ON c.customer_id = o.customer_id;
```
Result

| name    | order_id |
|---------|----------|
| Nikhil  | 101      |
| Nikhil  | 102      |
| Rahul   | NULL     |
| Aman    | NULL     |


Use Case
-	Find customers without orders
```sql
WHERE o.order_id IS NULL
```

### RIGHT JOIN (RIGHT OUTER JOIN)

Returns:
-	All rows from right table
-	Matching rows from left table
-	NULL if no match

Query
```sql
SELECT c.name, o.order_id
FROM customer c
RIGHT JOIN orders o
ON c.customer_id = o.customer_id;
```
Result

| name | order_id |
|------|----------|
| Nikhil | 101 |
| Nikhil | 102 |
| NULL | 103 |

Interview Tip
- RIGHT JOIN is rarely used.
- LEFT JOIN is preferred for readability.

### FULL OUTER JOIN

Returns:
-	All rows from both tables
-	NULL where no match exists

Query
```sql
SELECT c.name, o.order_id
FROM customer c
FULL OUTER JOIN orders o
ON c.customer_id = o.customer_id;
```

Result

| name   | order_id |
|--------|----------|
| Nikhil | 101      |
| Nikhil | 102      |
| Rahul  | NULL     |
| Aman   | NULL     |
| NULL   | 103      |

### SELF JOIN

- A table joins with itself.

Example: Employee–Manager
```sql
SELECT e.name AS employee, m.name AS manager
FROM employee e
LEFT JOIN employee m
ON e.manager_id = m.emp_id;
```

Use Cases
-	Hierarchies
-	Parent-child relations
-	Organization structure

### CROSS JOIN
- A CROSS JOIN returns the Cartesian product of two tables.
- Every row of Table A is combined with every row of Table B

If:
- Table A has m rows
- Table B has n rows

Result = m × n rows
internally:
for each row in employees:
    for each row in departments:
        combine rows

- There is no join condition.

Query
Explicit CROSS JOIN -
```sql
SELECT c.name, o.order_id
FROM customer c
CROSS JOIN orders o;
```
Implicit CROSS JOIN (interview favorite)
```sql
SELECT c.name, o.order_id
FROM customer c, orders o;
```
- Without a WHERE clause → this is a CROSS JOIN.
- Dangerous on large tables.


**Q1. Is CROSS JOIN same as INNER JOIN?**
- ❌ No
- CROSS JOIN → Cartesian product
- INNER JOIN → Matches rows using condition

**Q2. What happens if you forget join condition in INNER JOIN?**
- SQL treats it as CROSS JOIN
```sql
SELECT *
FROM A, B;
```

**Q3. Can CROSS JOIN be optimized using indexes?**
- ❌ No
- Indexes don’t help because every row must be combined.

**Q4. Can CROSS JOIN return zero rows?**
- ❌ No (unless one table is empty)

**Q5. CROSS JOIN vs FULL OUTER JOIN**
CROSS JOIN
- Produces Cartesian Product
- Combines every row of Table A with every row of Table B
- No join condition

FULL OUTER JOIN
- Combines:
- Matching rows
- Non-matching rows from both tables
- Uses a join condition
- Unmatched columns are filled with NULL
Syntax Comparison

CROSS JOIN
```sql
SELECT *
FROM A
CROSS JOIN B;
```
FULL OUTER JOIN
```sql
SELECT *
FROM A
FULL OUTER JOIN B
ON A.id = B.id;
```

Example Tables
Table A: orders

| order_id | customer |
|----------|----------|
| 1        | Nikhil   |
| 2        | Rahul    |

Table B: payments

| payment_id | order_id |
|------------|----------|
| 1          | 1        |
| 3          | 3        |

CROSS JOIN Result
```sql
SELECT *
FROM orders
CROSS JOIN payments;
```
Output (2 × 2 = 4 rows)

| order_id	| customer |	payment_id | 	order_id |
|-----------|----------|---------------|-------------|
| 1	| Nikhil	| 1	| 1	|
| 1	| Nikhil	| 3	| 3	|
| 2	| Rahul	| 1	| 1	|
| 2	| Rahul	| 3	| 3	|
- Every possible combination

FULL OUTER JOIN Result
```sql
SELECT *
FROM orders o
FULL OUTER JOIN payments p
ON o.order_id = p.order_id;
```
Output

| order_id 	 | customer | 	payment_id |
|------------|----------|-------------|
| 1          | Nikhil	  | 1	          |
| 2	         | Rahul	   | NULL	       | 
| NULL	      | NULL	    | 3           | 

- Order 2 has no payment → NULLs
- Payment 3 has no order → NULLs

When to Use Each

Use CROSS JOIN when:
- You intentionally need all combinations
- Generating:
- product × region
- date × metric
- Controlled data size

Use FULL OUTER JOIN when:
- You need all records from both tables
- Auditing / reconciliation
- Finding:
- missing relationships
- data mismatches

**Q. Can FULL OUTER JOIN behave like CROSS JOIN?**
- ❌ Never
- FULL OUTER JOIN always uses a join condition.

**Q. Can CROSS JOIN return NULLs?**
- Only if original table columns contain NULL.

**Q. How to simulate FULL OUTER JOIN in MySQL?**

(MySQL doesn’t support it natively)
```sql
SELECT *
FROM A
LEFT JOIN B ON A.id = B.id

UNION

SELECT *
FROM A
RIGHT JOIN B ON A.id = B.id;
```

“CROSS JOIN generates a Cartesian product with no condition, mainly used for combinations.
FULL OUTER JOIN uses a condition and ensures no data is lost from either table, filling missing matches with NULL.”

### JOIN vs WHERE Clause (Old Style)

❌ Old (Not Recommended)
```sql
SELECT *
FROM customer c, orders o
WHERE c.customer_id = o.customer_id;
```
✔ Modern
```sql
SELECT *
FROM customer c
JOIN orders o
ON c.customer_id = o.customer_id;
```

### JOIN with Multiple Tables
```sql
SELECT c.name, o.order_id, p.payment_status
FROM customer c
JOIN orders o ON c.customer_id = o.customer_id
JOIN payments p ON o.order_id = p.order_id;
```

Execution:
-	Joins are processed left to right
-	Optimizer may reorder internally

### JOIN with Conditions
```sql
SELECT c.name, o.amount
FROM customer c
JOIN orders o
ON c.customer_id = o.customer_id
AND o.amount > 5000;
```

Difference:
-	Condition in ON affects matching
-	Condition in WHERE filters after join

### ON vs WHERE (IMPORTANT INTERVIEW QUESTION)

LEFT JOIN example

-- Condition in ON
```sql
LEFT JOIN orders o
ON c.customer_id = o.customer_id 
AND o.amount > 5000;
```
✔ Keeps customers without orders

-- Condition in WHERE
```sql
WHERE o.amount > 5000;
```
❌ Converts LEFT JOIN into INNER JOIN

### How JOINs Work Internally (High-Level)

Databases use:
-	Nested Loop Join
-	Hash Join
-	Merge Join

Index Impact
-	Indexed join columns → fast
-	No index → full scan

### Performance Tips (Interview Gold)
-	Index join columns (PK/FK)
-	Avoid SELECT *
-	Filter early
-	Prefer EXISTS for presence check
-	LEFT JOIN + IS NULL for missing records


Note- **ON vs WHERE with OUTER JOIN:** placing filter conditions in `WHERE` can convert an outer join into an inner join; prefer conditions in `ON` to preserve outer join semantics.

### Subqueries, IN, EXISTS

**Subquery**
- Query inside another query.
```sql
SELECT name
FROM customer
WHERE customer_id IN (
SELECT customer_id FROM orders WHERE amount > 5000
);
```

**IN**
- Checks multiple values.
```sql
SELECT * FROM ACCOUNT WHERE account_id IN (101,102,103);
```

**EXISTS**
- Checks presence (stops on first match – faster).
- EXISTS is preferred for large datasets.

```sql
SELECT name
FROM customer c
WHERE EXISTS (
SELECT 1 FROM orders o WHERE o.customer_id = c.customer_id
);
```

### GROUP BY / HAVING / ORDER BY

**GROUP BY**
- Groups rows for aggregation.
```sql
SELECT customer_id, SUM(amount)
FROM orders
GROUP BY customer_id;
```

**HAVING**
- Filters groups (used with aggregates).
```sql
SELECT customer_id, SUM(amount)
FROM orders
GROUP BY customer_id
HAVING SUM(amount) > 10000;
```

**ORDER BY**

- Sorts result.
```sql
SELECT * FROM ACCOUNT ORDER BY balance DESC;
```


### Views, Stored Procedures, Triggers
**Views**

- A view is a virtual table.
```sql
CREATE VIEW high_value_accounts AS
SELECT * FROM ACCOUNT WHERE balance > 50000;
```
Benefits:
-	Security
-	Simplified queries
-	Reusability

- No data stored separately (unless materialized).

**Stored Procedures (Basic)**

- Precompiled SQL logic stored in DB.
```sql
CREATE PROCEDURE get_balance(IN acc_id INT)
BEGIN
SELECT balance FROM ACCOUNT WHERE account_id = acc_id;
END;
```
Advantages:
-	Performance
-	Encapsulation
-	Reduced network calls

**Triggers (Basic)**

- Automatically executes on events.
```sql
CREATE TRIGGER update_audit
AFTER UPDATE ON ACCOUNT
FOR EACH ROW
BEGIN
INSERT INTO audit_log VALUES (OLD.account_id, OLD.balance, NEW.balance);
END;
```
Used for:
-	Auditing
-	Validation
-	Logging


**Interview One-Liners (Very Important)**
-	DDL defines structure, DML manipulates data
-	HAVING filters groups, WHERE filters rows
-	INNER JOIN returns common rows only
-	EXISTS is faster than IN for large datasets
-	COMMIT makes data permanent, ROLLBACK reverts

⸻

Common Follow-Up Interview Questions
-	DELETE vs TRUNCATE vs DROP
-	JOIN vs Subquery (which is faster?)
-	View vs Materialized View
-	Procedure vs Function
-	Trigger vs Application logic
-	EXISTS vs IN

---

## 3. Indexing

### What is an index?
An index is a data structure used by the database to speed up data retrieval.

Analogy: 
- Table → Book; 
- Index → Book index.

- Without index → full table scan (O(n)); 
- With index → O(log n) lookups (usually).

**Benefits**
-	Faster SELECT queries
-	Faster JOINs
-	Faster ORDER BY / GROUP BY

### What does an index store?
- Indexed column value
- Pointer to actual row (row id / page location)

### Types of Indexes
#### B-Tree (most common)
- Balanced tree, supports range queries and `ORDER BY`. 
- Default index in MySQL, PostgreSQL, Oracle
**Properties**
-	Always balanced
-	Logarithmic search time
-	Supports range queries

**Used For**
-	=
-	<, >, BETWEEN
-	ORDER BY

**Example:**
```sql
CREATE INDEX idx_account_id ON ACCOUNT(account_id);
```

#### Hash Index
- O(1) lookup for equality (`=`) only.
- No range queries, no `ORDER BY`,No prefix matching.
-	Used internally (e.g., memory engines)
-	Rarely used explicitly


#### Composite (multi-column) Index
- Example:
```sql
CREATE INDEX idx_cust_date ON ORDERS(customer_id, order_date);
```
- Left-most prefix rule applies: index used when query uses leading columns.
- Index works if query uses:
  -	customer_id
  -	customer_id, order_date
Won’t work for:
```sql
WHERE order_date = '2025-01-01';
```

**Q: Why column order matters?**
- A: Index is sorted left-to-right.

#### Unique Index

- Ensures uniqueness: Prevents duplicate values in a column
```sql
CREATE UNIQUE INDEX idx_email ON CUSTOMER(email);
```
- PK vs Unique Index: PK disallows NULL and only one PK per table.
- Unique Index is a constraint while PK is physical identity.


### Clustered vs Non-Clustered
- **Clustered:** - `Index → Actual Rows` 
  - Actual table data is stored in index order (one per table; often PK).
  -	Only ONE clustered index per table
  -	Usually on Primary Key
  
- Pros
  -	Very fast range scans
  -	Faster SELECT by PK

- Cons
  -	Slower INSERTs (reordering)
  -	Page splits
  
- **Non-clustered:** - `Index → Pointer → Row` 
  - index stores pointers to data (multiple allowed). (B-tree), 
  - Data stored separately
  
- Pros
  -	Multiple allowed
  -	Faster lookups on non-PK columns

- Cons
  -	Extra lookup (index + table)



### When indexing helps
- Frequent `SELECT`s, joins, `WHERE`, `ORDER BY`/`GROUP BY`, foreign keys.

### When indexing hurts
- High-write systems (INSERT/UPDATE/DELETE), 
- Low cardinality columns (gender, status), 
- small tables — indexes add write overhead and storage.

**Why?**

Every write must:
1.	Update table
2.	Update ALL related indexes

INSERT
-	Index needs to be updated
-	May cause page split

UPDATE
-	If indexed column updated → index rebuild

DELETE
-	Index entry removed
-	Fragmentation

- More indexes = slower writes

### Covering Index
- Contains all columns needed by a query; 
- avoids table lookup.
```sql
CREATE INDEX idx_cover ON ORDERS(customer_id, amount);
```
Query:
```sql
SELECT amount FROM ORDERS WHERE customer_id = 10;
```

### Interview Q&A (common)
- Why not index every column? Slows writes, memory cost.
- Does index improve INSERT? No.
- Can NULL be indexed? Depends on DB.
- Does `ORDER BY` use index? Yes, if order matches index order.
- Why index foreign keys? Faster joins + referential integrity checks.


---

## 4. Transactions & Isolation Levels

### What is a transaction?
A transaction is a sequence of DB operations executed as a single unit: all succeed or all fail.

Lifecycle: `BEGIN` → read/write → `COMMIT` or `ROLLBACK`.

**Example (bank transfer):**
1. Debit A
2. Credit B  
   If step 2 fails → rollback step 1.

### Java / Spring `@Transactional`
- Declarative transaction management via `@Transactional`.

```java
@Transactional
public void transferMoney(Long from, Long to, BigDecimal amount) {
  debit(from, amount);
  credit(to, amount);
}
```

What Spring does:
1. Opens DB connection
2. Starts transaction
3. Executes method
4. Commits on success
5. Rolls back on exception

Default `@Transactional` behavior:
- Propagation: REQUIRED
- Isolation: DEFAULT (DB-dependent)
- Rollback: runtime exceptions only
- Read-only: false

Rollback rules:
- RuntimeException → rollback
- Checked Exception → no rollback by default (use `rollbackFor` to change)
```java
@Transactional
public void update() {
    throw new RuntimeException(); // rollback
}
```

```java
@Transactional
public void update() throws Exception {
    throw new Exception(); // no rollback by default
}
```

Fix
```java
@Transactional(rollbackFor = Exception.class)
```


Propagation examples: REQUIRED, REQUIRES_NEW, MANDATORY, NOT_SUPPORTED.

### Common concurrency problems

#### Dirty Read
- Reading uncommitted data

Example
-	T1 updates balance (not committed)
-	T2 reads updated value
-	T1 rolls back

- T2 read invalid data

#### Non-Repeatable Read
- Same query returns different results in same transaction

Example
-	T1 reads balance = 5000
-	T2 updates balance = 7000 and commits
-	T1 reads again → 7000

#### Phantom Read
- New rows appear in re-execution of same query

Example
-	T1: SELECT count(*) FROM orders WHERE amount > 5000
-	T2 inserts new order and commits
-	T1 re-executes → count changes


### Isolation levels
Controls visibility of one transaction's changes to others.

- READ_UNCOMMITTED — allows dirty reads (practically unused)
- READ_COMMITTED — prevents dirty reads; allows non-repeatable & phantom reads (used by PostgreSQL, Oracle)
- REPEATABLE_READ — prevents dirty & non-repeatable reads; phantom reads may still occur depending on DB (MySQL InnoDB)
- SERIALIZABLE — strongest; prevents dirty, non-repeatable, and phantom reads — Transactions execute as if serialized, lowest concurrency, Highest consistency

Isolation level vs problems:

| Isolation Level     | Dirty Read | Non-Repeatable | Phantom |
|---------------------|------------|----------------|---------|
| READ_UNCOMMITTED    | yes        | yes            | yes     |
| READ_COMMITTED      | no         | yes            | yes     |
| REPEATABLE_READ     | no         | no             | yes     |
| SERIALIZABLE        | no         | no             | no      |

How databases enforce isolation:
-	Locks (row/table)
-	MVCC (Multi-Version Concurrency Control)
-	Snapshot isolation


### Two-Phase Commit (2PC)

**Why 2PC is Needed?**
- When one transaction spans multiple databases/services.

Example:
-	Payment Service (DB1)
-	Order Service (DB2)
Both must commit or rollback.

**Participants**
-	Coordinator
-	Participants (DBs)

#### Phase 1 – Prepare

Coordinator:
-	Asks participants: “Can you commit?”

Participants:
-	Write changes to log
-	Reply YES / NO

#### Phase 2 – Commit / Rollback

If all YES:
-	Coordinator sends COMMIT

If any NO:
-	Coordinator sends ROLLBACK

**Problems with 2PC (INTERVIEW FAVORITE)**

❌ Blocking protocol
❌ Single point of failure
❌ Poor scalability
❌ Not cloud-friendly

**Why 2PC is Avoided in Microservices**
-	Network failures
-	High latency
-	Tight coupling

Preferred Alternatives
-	Saga Pattern
-	Eventual consistency
-	Compensating transactions


**Q1. What happens if @Transactional method calls another method in same class?**
- A: Transaction is NOT applied (proxy issue).

**Q2. Does @Transactional work with private methods?**
- A: No.

**Q3. Which isolation level prevents phantom reads?**
- A: SERIALIZABLE.

**Q4. Difference between isolation and locking?**
- A: Isolation is logical; locking is physical mechanism.

**Q5. Why not use SERIALIZABLE everywhere?**
- A: Performance degradation.

**Q6. How does Spring implement @Transactional?**
- A: AOP proxies.


---

## 5. Database Normalization

### What is normalization?
- Think of a messy spreadsheet with the same customer info repeated for every order they place (redundancy). 
- Normalization would split this into a Customers table (with unique customer details) and an Orders table (with order details), linking them with a CustomerID (relationships). 
- This way, you update a customer's address in only one place.

- Definition - Process to organize data to minimize redundancy, avoid anomalies, and improve consistency and integrity.

Core idea: each fact stored in exactly one place.

Problems without normalization:
**Insertion Anomaly:** Cannot insert data without unrelated data
**Update Anomaly:** Same data updated in multiple places
- If customer data, order data and book data 
**Deletion Anomaly:** Deleting data unintentionally removes info

### Normal Forms

#### First Normal Form (1NF)
- First Normal Form (1NF) focuses on ensuring that the values in each column of a table are atomic, meaning they cannot be further divided. 
- A table is in 1NF if:
  - All attributes (columns) contain only single, indivisible values
  - Each record is unique and there are no repeating groups

- Scenario:

**Consider a table storing student details:**

| StudentID	 | Name	   | Phone Numbers          |
|------------|---------|------------------------| 
| 101 	      | Robert	 | 9876543210, 9123456780 |

This violates 1NF because the “Phone Numbers” column holds multiple values. To convert this into 1NF, we can split the phone numbers into two rows:

| StudentID	  | Name	     | Phone Number   |
|-------------|-----------|----------------| 
| 101	        | Robert	   | 9876543210     |
| 101	        | Robert	   | 9123456780     | 

**Why 1NF matters:**

- Applying 1NF removes nested or grouped data and lays the groundwork for further normalization. 
- It’s the first step in structuring data for consistency and clarity.

#### Second Normal Form (2NF)
- Second Normal Form (2NF) builds upon 1NF and addresses partial dependencies. 
- A table is in 2NF if:
  - It is already in 1NF
  - Every non-prime attribute (an attribute that’s not part of a candidate key) is fully functionally dependent on the entire primary key
    - For a column, if it is not dependent on the primary key completely, (composite primary key - all the keys that together make it primary key), then it should be a part of diff table 
    - 
  
- Scenario:

**Suppose we have a table for student-course enrollment:**

| StudentID | CourseID | StudentName | CourseName |
|-----------|----------|-------------|------------|
| 101       | CS101    | Robert	     | DBMS       |
| 102       | CS102    | Sam	        | OS         |

Here, the composite primary key is (StudentID, CourseID). But “StudentName” depends only on “StudentID” and “CourseName” depends only on “CourseID”, which indicates partial dependency.

- To convert this into 2NF, we can split the table into three parts:

- Student table
- Course table
- Enrollment table

The student table will look like:

| StudentID | 	StudentName |
|-----------|--------------|
| 101       | Robert       |
| 102       | Sam          |

The course table will look like:

| CourseID | CourseName |
|----------|------------|
| CS101    | DBMS       |
| CS102    | OS         |

The enrollment table will look like:

| StudentID | CourseID |
|-----------|----------|
| 101       | CS101    |
| 102       | CS102    |


**Why 2NF matters:**
-2NF ensures that data is placed in the appropriate tables and that every column is fully dependent on the whole key, not just part of it. This eliminates data duplication and makes updates more efficient.

#### Third Normal Form (3NF)
- Third Normal Form (3NF) eliminates transitive dependencies. 
- A table is in 3NF if:
  - It is already in 2NF
  - No non-prime attribute is dependent transitively on the primary key
  - In simpler terms, non-key attributes should not depend on other non-key attributes.

- Scenario:

Consider a table storing employee details:

| EmpID  | 	EmpName | 	DeptID    | 	DeptName  |
|--------|----------|------------|------------|
| 1      |  	John   | 	D01       | 	HR        |
| 2	     | Emma     | 	D02       | 	IT        |

Here, “DeptName” depends on “DeptID”, which in turn depends on “EmpID”. This is a transitive dependency.

- To convert this into 3NF, we can split the table into two parts:
  - Employee table
  - Department table

The employee table will look like:

| EmpID  | 	EmpName | 	DeptID    |
|--------|----------|------------|
| 1      |  	John   | 	D01       |
| 2	     | Emma     | 	D02       |

The department table will look like:

| DeptID  | 	DeptName  |
|---------|-------------|
| D01     | 	HR         |
| D02     | 	IT         |

**Why 3NF matters:**

3NF promotes data integrity and reduces redundancy by ensuring that each non-key attribute is directly dependent on the primary key.


#### Boyce-Codd Normal Form (BCNF)
- BCNF (Boyce-Codd Normal Form) is a higher version of 3NF. 
- A table is in BCNF if:
  - It is in 3NF
  - Every functional dependency has a super key on the left-hand side
  - BCNF handles certain anomalies that 3NF cannot. 
  - If a table has overlapping candidate keys, it might violate BCNF even while being in 3NF.

- Scenario:

Suppose we have a table for storing course details:

Professor	Course	Time
Smith	DBMS	10AM
Smith	DBMS	2PM
Johnson	OS	11AM
Here, a professor can teach multiple courses, and each course is taught by one professor. However, some professors may teach the same course at different times.

The functional dependencies in this scenario include:

Professor → Course (Each professor teaches only one course)
Course → Professor (Each course is taught by one professor)
But in this table, neither “Professor” nor “Course” is a super key, so it violates BCNF.

To convert this into BCNF, we can split the table into two parts:

Professor-course table
Course-schedule table
The professor-course table will look like:

Professor	Course
Robert	DBMS
Sam	OS
The course-schedule table will look like:

Course	Time
DBMS	10AM
DBMS	2PM
OS	11AM
Why BCNF matters:

BCNF ensures stricter normalization by resolving complex dependencies, especially in tables with multiple candidate keys.

Fourth Normal Form (4NF)
A table is in the Fourth Normal Form (4NF) if:

It is in BCNF
It contains no multi-valued dependencies
Scenario:

Consider this table:

Teacher	Subject	Language
Robert	Math	English
Robert	Math	Spanish
Robert	Physics	English
Robert	Physics	Spanish
In this scenario, a teacher can teach multiple subjects and speak multiple languages.

This has multi-valued dependencies:

Teacher →→ Subject
Teacher →→ Language
These are independent facts stored in the same table, leading to unnecessary repetition.

To convert this into 4NF, we can split the table into two:

Teacher-subject table
Teacher-language table
The teacher-subject table will look like:

Teacher	Subject
Robert	Math
Robert	Physics
The teacher-language table will look like:

Teacher	Language
Robert	English
Robert	Spanish
Why 4NF matters:

4NF separates logically independent data, avoiding data explosion and maintaining clarity.

Fifth Normal Form (5NF)
Fifth Normal Form (5NF), also known as Project-Join Normal Form (PJNF), ensures that a relation is broken down into smaller relations that can be joined back without any loss of information or introduction of invalid combinations, and that all join dependencies are implied by candidate keys.

Scenario:

A school tracks which students are learning which subjects from which teachers. However, each combination (Student, Subject, Teacher) is valid only as a complete triple — it’s not enough to just know who is learning what or who teaches what.

Student	Subject	Teacher
Alice	Math	Mr. A
Alice	Science	Ms. B
Bob	Math	Mr. A
We can’t decompose this into binary relations (e.g., Student-Subject, Student-Teacher, Subject-Teacher) without possibly recreating invalid combinations when we rejoin the data.

To convert this into 5NF, we can create three separate projections:

Student-Subject
Student	Subject
Alice	Math
Alice	Science
Bob	Math
Student-Teacher
Student	Teacher
Alice	Mr. A
Alice	Ms. B
Bob	Mr. A
Subject-Teacher
Subject	Teacher
Math	Mr. A
Science	Ms. B
Why 5NF matters:

5NF handles highly complex relationships and ensures complete reconstruction of data without redundancy.

By understanding these normal forms, we can build databases that are logically structured, scalable, and efficient. Applying these rules during the design phase can save time, reduce maintenance effort, and prevent anomalies during data operations.

With a good understanding of the normal forms, let’s now discuss why normalization is important in DBMS.

Examples and conversions provided for each.

### Denormalization
Intentional redundancy to improve read performance (used in read-heavy systems, reporting, analytics).

Trade-offs:
- Normalization improves consistency; may hurt read performance.
- Denormalization reduces joins and speeds reads but increases redundancy and complexity for writes.

### Interview tips
- Usually normalize up to 3NF.
- BCNF, higher normal forms, trade-offs are follow-ups.
- Difference between normalization and indexing: normalization improves structure; indexing improves access speed.

---

## 6. JPA & ORM Integration

- JPA core concepts: `Entity`, `Table`, `Id`, `GeneratedValue`, `OneToMany`, `ManyToOne`.
- Entity relationships and fetch types: `LAZY` vs `EAGER`.
- JPQL vs native SQL.
- Criteria API and Specifications for dynamic queries.
- N+1 problem and solutions: `JOIN FETCH`, `EntityGraph`.

---

## 7. Spring Data JPA

- Repository abstraction (`JpaRepository`, `CrudRepository`).
- Query methods by naming convention.
- `@Query` annotation for custom queries.
- Pagination and sorting.
- DTO projection and result mapping.

---

## 8. Connection Pooling

- HikariCP (default in Spring Boot).
- Pool tuning: max pool size, idle timeout, leak detection.
- JDBC vs JPA performance considerations.

---

## 9. Database Performance Tuning

- Use `EXPLAIN` to understand query plans.
- Avoid full table scans; add appropriate indexes.
- Use batch inserts/updates for large writes.
- Cache frequently accessed data (Spring Cache, Redis).
- Monitor slow queries and database CPU.

---

## 10. NoSQL (optional but beneficial)

- Types: Document (MongoDB), Key-Value (Redis), Column-family (Cassandra), Graph.
- When to use: NoSQL for flexible schema, high throughput, horizontal scaling. RDBMS for transactions, consistency, joins.
- Spring Data MongoDB basics: `@Document`, `MongoRepository`.
- CAP Theorem: Consistency, Availability, Partition Tolerance.

---

## 11. Schema Migration Tools

- Liquibase or Flyway for version-controlled DDL scripts.
- Manage DB changes across environments.

---

## 12. Testing with Databases

- Use H2/in-memory DB for unit tests.
- `@DataJpaTest` for JPA slice tests and test isolation.
- Testcontainers for integration tests with real DBs.
- Database seeding for integration tests.

---

## 13. Database in Production

- Backup and restore strategies.
- Monitoring (DB CPU, slow queries).
- Read/Write splitting (replication).
- Connection timeouts and retries.
- Secure access and credentials management (e.g., Vaults).

---

## Interview-Ready One-liners & Tips

- ACID: Atomicity, Consistency, Isolation, Durability — ensures correctness and reliability.
- Indexes: speed up reads; slow down writes.
- Use `LEFT JOIN` + `IS NULL` to find missing records.
- `EXISTS` preferred over `IN` for large datasets.
- Avoid `SELECT *` in production; prefer explicit columns.
- `SERIALIZABLE` gives strongest isolation but lowest concurrency.
- For distributed transactions, prefer Saga pattern over 2PC in microservices.

---

## Useful Example Snippets

```sql
-- Create table example
CREATE TABLE ACCOUNT (
  account_id INT PRIMARY KEY,
  customer_name VARCHAR(50),
  balance DECIMAL(10,2)
);
```

```java
// Spring transactional example
@Transactional(
  isolation = Isolation.READ_COMMITTED,
  propagation = Propagation.REQUIRED
)
public void processPayment() {
  debit();
  credit();
}
```

```sql
-- Find accounts with no transactions
SELECT a.account_id
FROM account a
LEFT JOIN transaction t ON a.account_id = t.account_id
WHERE t.account_id IS NULL;
```

---

## Typical Follow-up Questions to Prepare

- Primary Key vs Unique Key
- Index vs Constraint
- Differences among DELETE, TRUNCATE, DROP
- JOIN vs Subquery performance considerations
- View vs Materialized View
- Procedure vs Function
- Explaining execution plans (`EXPLAIN`, `ANALYZE`)
- Index selectivity, partial/bitmap indexes

---
