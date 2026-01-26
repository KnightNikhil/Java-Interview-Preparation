
## Java 9 (2017)

### Module System (JPMS)
**Why:** Better encapsulation, dependency management.

**Follow-up:** What problem do modules solve compared to packages?

### JShell (REPL)
**Why:** Interactive experimentation.

### Stream API Enhancements (`takeWhile`, `dropWhile`)

---

## Java 10

### `var` - Local Variable Type Inference
```java
var name = "ChatGPT";
```

**Follow-up:** Can `var` be used for instance variables or parameters? (No)

---

## Java 11 (LTS)

### New `String` Methods: `isBlank()`, `lines()`, `repeat()`
### `HttpClient` API
```java
HttpClient client = HttpClient.newHttpClient();
```

### Local variable syntax for lambda parameters

---

## Java 12-14 Highlights

### Switch Expressions (preview in 12, standard in 14)
```java
int num = switch(day) {
    case MONDAY -> 1;
    case TUESDAY -> 2;
    default -> 0;
};
```

---

## Java 15

### Text Blocks
```java
String html = """
    <html>
        <body>Hello</body>
    </html>
""";
```

---

## Java 16

### Records
Alright, let’s go deep and interview-ready on Java Records — what they are, how they really work under the hood, where they shine, and where they break.

No emojis, structured, Java-centric.

⸻

1. What are Records in Java?

Records (Java 16, final) are a special kind of class designed to model immutable data carriers.

In one line:

A record is a transparent, shallowly immutable data class where the compiler generates boilerplate code for you.

They eliminate:
•	Getters
•	equals()
•	hashCode()
•	toString()
•	Constructor

⸻

2. Why were Records introduced?

Before records:

public final class Account {
private final String id;
private final double balance;

    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() { return id; }
    public double getBalance() { return balance; }

    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
    @Override
    public String toString() { ... }
}

This is:
•	Verbose
•	Error-prone
•	Distracts from domain meaning

Records reduce this to:

public record Account(String id, double balance) {}


⸻

3. What does the compiler generate?

For:

public record Account(String id, double balance) {}

The compiler generates:

public final class Account extends java.lang.Record {
private final String id;
private final double balance;

    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    public String id() { return id; }
    public double balance() { return balance; }

    // equals(), hashCode(), toString()
}

Important properties
•	Class is implicitly final
•	Fields are implicitly private final
•	Accessors use component name, not getX()

⸻

4. Records are NOT just syntactic sugar

Key differences from normal classes:
•	Semantic contract enforced by compiler
•	Canonical constructor rules
•	Component-based equality
•	JVM awareness (java.lang.Record)

This allows:
•	Pattern matching
•	Exhaustive reasoning
•	Better tooling support

⸻

5. Immutability — but only shallow

Records are:
•	Shallowly immutable

Example:

public record Order(List<String> items) {}

You cannot reassign items, but you can mutate the list.

To enforce deep immutability:

public record Order(List<String> items) {
public Order {
items = List.copyOf(items);
}
}


⸻

6. Canonical Constructor (VERY IMPORTANT)

Canonical constructor signature:

public Account(String id, double balance)

Compact canonical constructor:

public record Account(String id, double balance) {
public Account {
if (balance < 0) {
throw new IllegalArgumentException("Negative balance");
}
}
}

Rules:
•	No field assignment needed
•	Compiler assigns automatically
•	Used for validation & normalization

⸻

7. Custom Constructors in Records

You can add:
•	Compact constructor
•	Overloaded constructors

Example:

public record User(String name, int age) {

    public User(String name) {
        this(name, 18);
    }
}

But:
•	Canonical constructor always exists
•	All constructors must delegate to it

⸻

8. Records + Methods

Records can have:
•	Instance methods
•	Static methods

Example:

public record Account(double balance) {
public boolean isOverdrawn() {
return balance < 0;
}
}

They cannot have:
•	Instance fields (other than components)

⸻

9. Records + Inheritance

Rules:
•	Records cannot extend classes
•	Records can implement interfaces
•	Records are implicitly final

Example:

public record Circle(double radius) implements Shape {}


⸻

10. Records + Sealed Classes (Modern Java Pattern)

Perfect match.

public sealed interface Shape
permits Circle, Rectangle {}

public record Circle(double radius) implements Shape {}
public record Rectangle(double length, double width) implements Shape {}

This gives:
•	Closed hierarchy
•	Immutable data
•	Exhaustive switch

⸻

11. Records and equals() / hashCode()

Generated automatically based on:
•	All components
•	Order matters

Example:

record A(int x, int y) {}
record B(int y, int x) {}

They are not equal, even with same values.

⸻

12. Serialization & Records

Java Serialization
•	Records are serializable if components are serializable
•	Uses canonical constructor during deserialization

JSON (Jackson)
•	Supported out-of-the-box (2.12+)
•	Maps constructor params directly

⚠️ Avoid using records as:
•	JPA entities
•	Hibernate managed objects

⸻

13. Performance Considerations
    •	Same memory footprint as final class
    •	No reflection overhead
    •	Better inlining & optimization
    •	No runtime penalty

Records are zero-cost abstraction.

⸻

14. When to use Records?

Use records when:
•	Class is a pure data carrier
•	State is immutable
•	Identity is based on data
•	You want concise domain models

Examples:
•	DTOs
•	API responses
•	Events
•	Value objects
•	Commands / Queries

⸻

15. When NOT to use Records?

Avoid records when:
•	Mutable state is required
•	Identity ≠ data (e.g., entities)
•	Framework needs proxies
•	Heavy lifecycle logic exists

⸻

16. Records vs Lombok @Data

Records	Lombok
Language feature	Annotation processor
Enforced immutability	Optional
JVM aware	Compile-time only
Safer refactors	Risky
No dependency	Extra dependency

Records are strict and safer.

⸻

17. Advanced Interview Traps
    1.	Records are immutable → ❌ shallow only
    2.	Records can have setters → ❌
    3.	Records replace POJOs → ❌ not always
    4.	Records are slower → ❌
    5.	Records can extend classes → ❌

⸻

18. JVM & Bytecode Detail
    •	All records extend java.lang.Record
    •	JVM marks them with ACC_RECORD
    •	Reflection API exposes record components
    •	Pattern matching leverages this metadata

⸻

19. Real-World Banking Example

public record Transaction(
String txnId,
String accountId,
BigDecimal amount,
Instant timestamp
) {}

Clean, immutable, thread-safe.

⸻

20. Interview Power One-Liner

“Records in Java are immutable, transparent data carriers where the compiler generates boilerplate like constructors, accessors, equals, hashCode, and toString, enabling concise, safe domain modeling with zero runtime cost.”

⸻

Great choice — records are a hot interview topic, and advanced questions test whether you understand their semantics, JVM behavior, and real-world trade-offs, not just syntax.

Below are advanced record interview questions with strong, concise answers, plus follow-up traps interviewers love.

⸻

1. Are records immutable? Explain precisely.

Strong answer

Records are shallowly immutable:
•	Record components are private final
•	You cannot reassign them
•	But referenced objects can still be mutated

Example:

record Order(List<String> items) {}

items.add("x") is allowed.

To enforce deep immutability, defensively copy in the constructor.

Follow-up trap

Can you add setters to records?

Answer: No. Components have no setters, only accessors.

⸻

2. How does equals() work in records?

Strong answer

equals() is:
•	Generated by the compiler
•	Component-based
•	Order-sensitive
•	Final (cannot be overridden)

Two records are equal if:
•	Same record type
•	All components are equal in declared order

⸻

3. Can you override equals(), hashCode(), or toString() in a record?

Strong answer

No.

Records enforce transparent data semantics.
Allowing overrides would break predictability and pattern matching guarantees.

⸻

4. How are records implemented at the JVM level?

Strong answer
•	All records extend java.lang.Record
•	JVM marks them with ACC_RECORD
•	Record components are stored in class metadata
•	Reflection API exposes getRecordComponents()

Records are JVM-recognized, not just syntactic sugar.

⸻

5. What is the canonical constructor and why is it special?

Strong answer

The canonical constructor:
•	Has the same signature as record components
•	Is always present (explicit or implicit)
•	Is used by:
•	Object creation
•	Deserialization
•	Reflection
•	Pattern matching

Compact form:

public record User(String name, int age) {
public User {
if (age < 0) throw new IllegalArgumentException();
}
}


⸻

6. Can records have multiple constructors?

Strong answer

Yes, but:
•	The canonical constructor always exists
•	All other constructors must delegate to it

Example:

public record User(String name, int age) {
public User(String name) {
this(name, 18);
}
}


⸻

7. Why can’t records extend classes?

Strong answer

Because:
•	Records already extend java.lang.Record
•	Allowing inheritance would break:
•	Equality semantics
•	Transparency
•	Pattern matching guarantees

Records favor composition over inheritance.

⸻

8. Can records be abstract?

Strong answer

No.

Records represent concrete data values.
Abstract records would violate their purpose as data carriers.

⸻

9. Can records implement interfaces?

Strong answer

Yes.

This is intentional and common:

public record Circle(double r) implements Shape {}

Records + sealed interfaces are the recommended modern Java model.

⸻

10. How do records behave with serialization?

Strong answer
•	Serializable if all components are serializable
•	Uses canonical constructor during deserialization
•	Ensures invariants are preserved

This is safer than normal Java serialization.

⸻

11. Why are records bad JPA entities?

Strong answer

Because:
•	JPA needs no-arg constructors
•	Entities require mutability
•	Hibernate uses proxies
•	Records are final and immutable

Records are for DTOs and value objects, not entities.

⸻

12. Can records contain static fields or methods?

Strong answer

Yes.

public record Rate(double value) {
public static final Rate ZERO = new Rate(0);
}

But no instance fields beyond components.

⸻

13. Can records be generic?

Strong answer

Yes.

public record Box<T>(T value) {}

All generic constraints apply normally.

⸻

14. How do records interact with pattern matching?

Strong answer

Records support deconstruction patterns:

if (obj instanceof Point(int x, int y)) {
...
}

This relies on:
•	Canonical constructor
•	Component metadata
•	Fixed structure

⸻

15. Why are records safer than Lombok @Data?

Strong answer

Records:
•	Enforce immutability
•	Prevent accidental mutation
•	Are JVM-recognized
•	Have no dependency
•	Provide compile-time guarantees

Lombok:
•	Generates mutable classes
•	Can hide complexity
•	Is annotation-based, not semantic

⸻

16. Can you customize validation logic in records?

Strong answer

Yes, in the canonical constructor.

public record Amount(BigDecimal value) {
public Amount {
if (value.signum() < 0)
throw new IllegalArgumentException();
}
}

This is the only place to enforce invariants.

⸻

17. Are records thread-safe?

Strong answer

Yes, by default, because:
•	State is immutable
•	No setters
•	Safe publication guaranteed

Exception: mutable components can break thread safety.

⸻

18. Can records have mutable components?

Strong answer

Yes — but strongly discouraged.

Example:

record Wrapper(List<String> list) {}

This breaks immutability expectations and should be avoided.

⸻

19. What happens if a new component is added to a record?

Strong answer
•	Binary compatibility is broken
•	All constructors, serialization, and pattern matches change
•	All consumers must be recompiled

Records favor explicit evolution, not silent changes.

⸻

20. Trick Question: Are records just “better POJOs”?

Correct answer

No.

Records are:
•	Opinionated
•	Restricted
•	Semantic data carriers

POJOs are:
•	Flexible
•	Mutable
•	Behavior-oriented

Use each intentionally.

⸻

Interview Closing Power Statement

“Records are immutable, transparent data carriers with JVM-level support that enable safer domain modeling, better pattern matching, and cleaner APIs — but they should be used only where data identity equals state.”

⸻

If you want next:
•	Record + sealed + pattern matching system design
•	Java 21 interview cheat sheet
•	Migration from Lombok DTOs
•	Trick coding problems using records

Tell me what to tackle next.


---

## Java 17 (LTS)

### Sealed Classes
Alright, let’s break sealed classes down cleanly and interview-ready, from why they exist → how they work → JVM/Java specifics → real use cases.

No fluff, just solid understanding.

⸻

1. What are Sealed Classes?

Sealed classes (Java 17+) let you control which classes are allowed to extend or implement a class/interface.

In simple words:

A sealed class restricts inheritance.

You explicitly say:

“Only these classes can extend me — no one else.”

⸻

2. Why were Sealed Classes introduced?

Before sealed classes:

abstract class PaymentStatus {}

❌ Anyone, anywhere could extend it:

class RandomStatus extends PaymentStatus {}

This causes:
•	Uncontrolled class hierarchies
•	Broken domain models
•	Harder reasoning in business logic
•	Unsafe instanceof checks
•	Fragile switch statements

Sealed classes solve this by:
•	Making inheritance explicit
•	Enforcing closed, finite hierarchies
•	Enabling exhaustive checks (especially with switch)

⸻

3. Basic Syntax

public sealed class Payment
permits CreditCardPayment, UpiPayment, NetBankingPayment {
}

Key parts:
•	sealed → restricts inheritance
•	permits → list of allowed subclasses

⸻

4. Rules for Subclasses

Every permitted subclass must declare one of these:

Modifier	Meaning
final	Cannot be extended further
sealed	Can be extended, but with restrictions
non-sealed	Opens inheritance again


⸻

Example

public sealed class Payment
permits CreditCardPayment, UpiPayment {
}

public final class CreditCardPayment extends Payment {
}

public non-sealed class UpiPayment extends Payment {
}

Now:
•	CreditCardPayment → closed
•	UpiPayment → open for extension

⸻

5. Sealed Interfaces

Yes, interfaces can also be sealed.

public sealed interface Transaction
permits DebitTransaction, CreditTransaction {
}

Implementation:

public final class DebitTransaction implements Transaction {}
public final class CreditTransaction implements Transaction {}


⸻

6. Exhaustive switch (BIG INTERVIEW POINT)

With sealed classes, the compiler knows all possible subclasses.

static String handlePayment(Payment p) {
return switch (p) {
case CreditCardPayment c -> "Credit Card";
case UpiPayment u -> "UPI";
};
}

✔ No default required
✔ Compile-time safety
✔ If a new subclass is added → compiler forces update

This is HUGE for correctness.

⸻

7. JVM / Bytecode Level

Under the hood:
•	sealed metadata is stored in the class file
•	JVM enforces inheritance restrictions at class loading time
•	Violations cause IncompatibleClassChangeError

This is not just compiler sugar — it’s JVM-enforced.

⸻

8. Sealed vs Abstract Classes

Abstract Class	Sealed Class
Controls behavior	Controls inheritance
Anyone can extend	Only permitted classes
Open hierarchy	Closed hierarchy
No exhaustiveness	Enables exhaustive switch

They are orthogonal:
A class can be abstract + sealed.

⸻

9. Sealed Classes + Records (Perfect Combo)

Java records are implicitly final, so they work beautifully with sealed hierarchies.

public sealed interface Shape
permits Circle, Rectangle {
}

public record Circle(double radius) implements Shape {}
public record Rectangle(double length, double width) implements Shape {}

This is how modern Java domain modeling is done.

⸻

10. Real-World Banking Example

Domain modeling

public sealed interface Account
permits SavingsAccount, CurrentAccount, LoanAccount {
}

public final class SavingsAccount implements Account {}
public final class CurrentAccount implements Account {}
public final class LoanAccount implements Account {}

Safe business logic

double interest(Account acc) {
return switch (acc) {
case SavingsAccount s -> 4.0;
case CurrentAccount c -> 0.0;
case LoanAccount l -> -8.0;
};
}

✔ No accidental account types
✔ Domain rules enforced at compile time

⸻

11. When should you use Sealed Classes?

Use them when:
•	The domain has finite states
•	You want strict control over inheritance
•	You use switch heavily
•	You are modeling business rules
•	You want compiler-enforced safety

Examples:
•	Payment types
•	Order states
•	User roles
•	Workflow states
•	Event types

⸻

12. When NOT to use Sealed Classes

Avoid when:
•	Frameworks expect open inheritance (e.g., Hibernate proxies)
•	Plugin-based systems
•	Library meant for third-party extension

⸻

13. Common Interview Traps
    1.	“Sealed classes are final” → ❌ No
    2.	“Only classes can be sealed” → ❌ Interfaces too
    3.	“permits is optional” → ❌ Required unless in same file
    4.	“Sealed is compile-time only” → ❌ JVM enforced
    5.	“Sealed replaces abstract” → ❌ Different concerns

⸻

14. Java Version Support

Java Version	Status
Java 15	Preview
Java 16	Second preview
Java 17	Final (LTS)


⸻

15. Interview One-Liner

“Sealed classes restrict which classes can extend or implement a type, enabling closed hierarchies, safer domain modeling, and exhaustive switch expressions enforced by the compiler and JVM.”

⸻

Perfect — these are the advanced, interviewer-level sealed class questions that usually separate “knows syntax” from “knows Java internals & design”.

I’ll give:
•	Question
•	Expected thinking
•	Strong answer
•	Follow-up traps (what interviewers probe next)

No emojis, clean, Java-centric.

⸻

1. How are sealed classes enforced — compile time or runtime?

Expected thinking

Is this just compiler sugar or JVM-level restriction?

Strong answer

Sealed classes are enforced at both compile time and runtime.
•	Compiler:
•	Ensures only permitted subclasses are declared
•	Enforces final / sealed / non-sealed on subclasses
•	JVM:
•	Stores permitted subclasses in the class file metadata
•	Enforces restrictions during class loading
•	Illegal subclassing causes IncompatibleClassChangeError

So sealed classes are not bypassable via bytecode tricks.

Follow-up trap

What happens if a subclass is added via reflection?

Answer:
•	Reflection cannot bypass sealed restrictions
•	Class loading still fails

⸻

2. Can a sealed class be abstract?

Strong answer

Yes. Sealed controls who can extend, abstract controls whether it can be instantiated.

public sealed abstract class Order
permits OnlineOrder, StoreOrder {
}

They solve orthogonal problems and are commonly used together.

⸻

3. Why does every permitted subclass have to be final, sealed, or non-sealed?

Expected thinking

Why is Java forcing explicit choice?

Strong answer

Because Java requires explicit inheritance intent.

Without this rule:
•	The inheritance tree could become unintentionally open
•	Compiler couldn’t reason about exhaustiveness

This design forces developers to consciously decide whether inheritance should:
•	End (final)
•	Continue in a controlled way (sealed)
•	Be reopened (non-sealed)

This is essential for sound type analysis.

⸻

4. What happens if you forget permits?

Strong answer

permits can be omitted only if:
•	All permitted subclasses are in the same source file
•	They are in the same module or package

Otherwise:

error: sealed class must declare permitted subclasses

This supports compact definitions while still preserving safety.

⸻

5. How do sealed classes enable exhaustive switch?

Expected thinking

How does compiler know all cases?

Strong answer

The compiler knows:
•	The sealed parent
•	The complete set of permitted subclasses

Therefore:
•	switch can be exhaustive
•	default is not required
•	Adding a new subclass breaks compilation

This shifts runtime errors → compile-time guarantees.

⸻

6. Sealed classes vs enums — when would you choose sealed?

Strong answer

Enum	Sealed Class
Fixed constants	Rich object hierarchy
No inheritance	Inheritance allowed
Stateless (mostly)	Stateful
Simple behavior	Complex domain modeling

Use sealed classes when:
•	Each type has different fields
•	You need polymorphism
•	You want pattern matching

Enums are better for simple finite states.

⸻

7. Can a sealed class extend another sealed class?

Strong answer

Yes.

public sealed class Event
permits PaymentEvent {
}

public sealed class PaymentEvent extends Event
permits SuccessEvent, FailureEvent {
}

Each level must explicitly control inheritance.

This allows layered domain modeling.

⸻

8. How do sealed classes interact with modules (JPMS)?

Expected thinking

Package vs module visibility

Strong answer
•	Permitted subclasses must be:
•	In the same module
•	Or explicitly exported/opened

Modules add an extra layer of encapsulation on top of sealed classes.

Sealed classes + JPMS together provide strong architectural boundaries.

⸻

9. Why are sealed classes useful for domain-driven design (DDD)?

Strong answer

Sealed classes:
•	Model closed business domains
•	Prevent invalid states
•	Enforce invariants at compile time
•	Improve refactoring safety

Example:

sealed interface LoanState
permits Approved, Rejected, Pending {}

DDD favors explicit, constrained models — sealed fits perfectly.

⸻

10. Can frameworks like Hibernate work with sealed classes?

Strong answer

Usually no, or with limitations.

Reasons:
•	Hibernate creates runtime proxies
•	Proxies often extend entity classes
•	Sealed restrictions block proxy generation

Workarounds:
•	Use non-sealed
•	Use interfaces
•	Avoid sealing entities

Sealed classes are best for domain models, not persistence models.

⸻

11. What happens if a permitted subclass is missing at runtime?

Strong answer

Class loading fails with:

java.lang.IncompatibleClassChangeError

Because:
•	JVM verifies permitted subclasses at load time
•	Missing or mismatched classes violate the sealed contract

This prevents partial or inconsistent deployments.

⸻

12. Can anonymous classes extend sealed classes?

Strong answer

No.

Anonymous classes are not listed in permits, so:
•	Compilation fails
•	This is intentional to prevent inheritance leaks

⸻

13. Sealed classes vs package-private constructors — difference?

Strong answer

Package-private constructor	Sealed class
Restricts instantiation	Restricts inheritance
Package-based	Explicit type-based
Bypassable via reflection	JVM-enforced
Weak guarantees	Strong guarantees

Sealed is far stronger and explicit.

⸻

14. How do sealed classes help pattern matching?

Strong answer

Sealed classes + pattern matching allow:
•	Exhaustive checks
•	Safer refactors
•	Cleaner logic

if (shape instanceof Circle c) { ... }
else if (shape instanceof Rectangle r) { ... }

Compiler knows when this chain is exhaustive and warns otherwise.

⸻

15. Migration Question (Java 8 → Java 21)

Expected answer
1.	Replace:
•	Marker interfaces
•	Deep abstract hierarchies
2.	Introduce:
•	Sealed interfaces
•	Records for data carriers
3.	Use:
•	Exhaustive switch
•	Pattern matching

This modernizes domain models without runtime cost.

⸻

16. Can sealed classes improve security?

Strong answer

Yes, indirectly.

They:
•	Prevent unauthorized extension
•	Reduce attack surface
•	Stop subclass-based exploits
•	Enforce invariants

Especially useful in financial and security-sensitive systems.

⸻

17. Trick Question: Are sealed classes immutable?

Correct answer

No.

Sealed controls inheritance, not mutability.

Immutability comes from:
•	final fields
•	Records
•	Defensive copying

⸻

Final Interview Power Statement

“Sealed classes give Java algebraic data types–like safety by enforcing closed hierarchies at both compile time and runtime, enabling exhaustive pattern matching, safer domain modeling, and stronger architectural boundaries.”

⸻

If you want:
•	Sealed + pattern matching switch questions
•	Code snippets interviewers love
•	System-design usage of sealed classes
•	Comparison with Kotlin sealed classes

Tell me what to go deeper on.

---

## Java 18-20 (Preview + Incubator Features)

- Pattern Matching for switch (Preview)
- Record Patterns
- Structured concurrency (incubator)

---

## Java 21 (LTS, 2023)




### Sequenced Collections
```java
SequencedCollection<T> adds first()/last()/reversed()
```

### Pattern Matching for `switch`
```java
switch (obj) {
    case String s -> System.out.println(s.toLowerCase());
    case Integer i -> System.out.println(i + 1);
}
```

---

## Final Interview Tips

- Know the difference between preview, incubator, and standard features.
- Understand compatibility concerns and migration paths.
- For every feature, ask: Why was this added? When should I use it?

---

## Cheat Sheet Summary

| Version | Major Feature |
|---------|----------------|
| Java 9  | Modules, JShell |
| Java 10 | `var` keyword |
| Java 11 | New String/HttpClient APIs |
| Java 14 | Switch Expressions |
| Java 15 | Text Blocks |
| Java 16 | Records |
| Java 17 | Sealed Classes |
| Java 21 | Virtual Threads, Pattern Matching |

---

Let me know if you want flashcards, diagrams, or mock interview Q&A on these topics.
"""