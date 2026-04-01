# Java Features — Interview‑Ready Notes (Up to Java 21)

## Index
- [Java 9](#java-9-2017)
    - [Module System (JPMS)](#module-system-jpms)
    - [JShell (REPL)](#jshell-repl)
    - [Stream API Enhancements](#stream-api-enhancements)
- [Java 10](#java-10)
    - [`var` — Local Variable Type Inference](#var---local-variable-type-inference)
- [Java 11 (LTS)](#java-11-lts)
    - [New String Methods & `HttpClient`](#new-string-methods--httpclient)
    - [Local variable syntax for lambda parameters](#local-variable-syntax-for-lambda-parameters)
- [Java 12–14 Highlights](#java-12-14-highlights)
    - [Switch Expressions](#switch-expressions)
- [Java 15](#java-15)
    - [Text Blocks](#text-blocks)
- [Java 16](#java-16)
    - [Records](#records)
- [Java 17 (LTS)](#java-17-lts)
    - [Sealed Classes](#sealed-classes)
- [Java 18–20](#java-18-20)
    - [Pattern Matching for `switch`, Record Patterns, Structured Concurrency (incubator)](#pattern-matching-and-structured-concurrency)
- [Java 21 (LTS, 2023)](#java-21-lts-2023)
    - [Sequenced Collections, Pattern Matching for `switch`, Virtual Threads](#java-21-features)
- [Final Interview Tips](#final-interview-tips)
- [Cheat Sheet Summary](#cheat-sheet-summary)

---

## Java 9 (2017)

### Module System (JPMS)
- Why:
    - Better encapsulation and explicit dependency management at the module level; avoids split packages and improves large-app packaging.
- How:
    - Create `module-info.java` declaring `module`, `requires`, `exports`, `opens`, `uses`, `provides`.
    - Example: `module com.example.app { requires com.example.lib; exports com.example.api; }`
- When to use:
    - For large codebases, libraries that want to restrict public API, or when controlling reflection/serialization boundaries.
- Interview follow-ups:
    - What problem do modules solve compared to packages?
    - How do `exports` and `opens` differ?
    - How does JPMS interact with the classpath and the module path?

### JShell (REPL)
- Why:
    - Fast feedback loop for experimenting with APIs, prototyping, and learning.
- How:
    - Run `jshell` from the JDK; evaluate statements, methods, and classes interactively.
- When to use:
    - Learning, quick experiments, prototyping algorithm ideas.
- Interview follow-ups:
    - Differences between REPL and running JUnit/test harness?
    - How to persist `jshell` sessions?

### Stream API Enhancements (`takeWhile`, `dropWhile`)
- Why:
    - Declarative, efficient operations on streamed sequences with early termination.
- How:
    - `stream.takeWhile(predicate)` consumes until predicate false; `dropWhile` skips until predicate false.
- When to use:
    - When working with ordered streams where sequence-based short-circuiting is needed.
- Interview follow-ups:
    - How do `takeWhile`/`dropWhile` behave on parallel streams?
    - Use cases vs. `filter()`.

---

## Java 10

### `var` \- Local Variable Type Inference
- Why:
    - Reduces boilerplate for local variable declarations while keeping static typing.
- How:
    - `var name = "Alice";` — compiler infers the static type at compile time.
    - Restrictions: only for local variables with initializer and not for method parameters, fields, or return types.
- When to use:
    - When the initializer makes type obvious or type is verbose; avoid when type clarity matters.
- Interview follow-ups:
    - Can `var` be used for instance variables or parameters? (No)
    - How does `var` affect readability and refactoring?

---

## Java 11 (LTS)

### New `String` Methods & `HttpClient`
- Why:
    - Convenience methods for common string tasks; a modern HTTP client replaces `HttpURLConnection`.
- How:
    - `String.isBlank()`, `String.lines()`, `String.repeat(n)`.
    - `HttpClient client = HttpClient.newHttpClient();` then build requests with `HttpRequest` and `HttpResponse`.
- When to use:
    - Use `HttpClient` for async and modern HTTP interactions.
- Interview follow-ups:
    - How to perform asynchronous requests with `HttpClient`?
    - Differences between `isBlank()` and `isEmpty()`.

### Local variable syntax for lambda parameters
- Why:
    - Consistency when using annotations on lambda parameters.
- How:
    - `(var x, var y) -> x + y` allows parameter annotations like `@Nonnull`.
- When to use:
    - When annotating lambda parameters or improving consistency.
- Interview follow-ups:
    - Can you mix `var` and explicit types in lambda parameter lists?

---

## Java 12–14 Highlights

### Switch Expressions (preview in 12, standard in 14)
- Why:
    - Concise, expression-oriented `switch` with `->` labels and `yield` for returning values.
- How:
    - Example:
      ```java
      int num = switch(day) {
          case MONDAY -> 1;
          case TUESDAY -> 2;
          default -> 0;
      };
      ```
    - Use pattern matching for `switch` in later versions.
- When to use:
    - Replace verbose `switch` statements, especially when computing a value.
- Interview follow-ups:
    - How does `yield` work?
    - Exhaustiveness checks with enums and sealed types.

---

## Java 15

### Text Blocks
- Why:
    - Multi-line string literals without manual escaping; improved readability for SQL, JSON, HTML.
- How:
    - Use `"""` delimiters:
      
      ```java
      String html = """
          <html>
              <body>Hello</body>
          </html>
      """;
      ```
- When to use:
    - Long multi-line literals and templates.
- Interview follow-ups:
    - How are indentation and trailing newlines handled?
    - Interaction with `String` methods and formatting.

---

## Java 16

### Records
- Why:
    - Concise, immutable data carriers that remove boilerplate for value classes.
- How:
    - Declare: `public record Account(String id, double balance) { }`
    - Compiler generates: final class, private final fields, canonical constructor, accessors (component-named), `equals`, `hashCode`, `toString`.
    - Key rules:
        - Shallow immutability — mutate referenced objects unless defensive copies are made.
        - Canonical constructor and compact constructors for validation.
        - Records implicitly `final` and cannot declare additional instance fields.
- When to use:
    - DTOs, value objects, API responses, events — where identity equals data and state should be immutable.
- Interview follow-ups:
    - Are records just syntactic sugar? (No — JVM recognition, semantics, and metadata.)
    - How to enforce deep immutability?
    - Can records extend classes? (No)
    - Can you override `equals`/`hashCode`/`toString`? (Avoid; semantics are compiler-generated.)
    - How do records interact with serialization and frameworks like Jackson?

Advanced record notes (interview-ready):
- Canonical constructor is always present and used for deserialization and pattern matching.
- Records are component-based in equality (order matters).
- Use defensive copying (e.g., `List.copyOf`) in the compact constructor to enforce deep immutability.
- Records are unsuitable for JPA entities and Hibernate-managed objects due to immutability and proxies.

---

## Java 17 (LTS)

### Sealed Classes
- Why:
    - Control which types may extend or implement a type; enables closed hierarchies and exhaustive reasoning.
- How:
    - `public sealed class Payment permits CreditCardPayment, UpiPayment { }`
    - Subclasses must be declared `final`, `sealed`, or `non-sealed`.
    - Sealed interfaces are also supported.
- When to use:
    - Modeling finite domain states, algebraic data-type patterns, improving switch exhaustiveness and maintainability.
- Interview follow-ups:
    - How are sealed classes enforced? (Both compile-time and JVM/runtime via metadata; violations cause `IncompatibleClassChangeError`.)
    - Can sealed classes be abstract? (Yes.)
    - Interaction with modules: permitted subclasses must be in the same module or exported/opened.
    - Differences vs enums — when to choose sealed classes over enums?

Advanced sealed notes:
- Sealed classes enable exhaustive `switch` and pattern matching.
- Avoid sealing types used by frameworks that require runtime proxies (e.g., Hibernate), or mark subclasses `non-sealed` as appropriate.

---

## Java 18–20 (Preview + Incubator Features)

### Pattern Matching for `switch`, Record Patterns, Structured Concurrency (incubator)
- Why:
    - Pattern matching reduces boilerplate for type checks and casts; structured concurrency simplifies concurrent task orchestration.
- How:
    - Use `case` patterns in `switch`, deconstruction patterns with records.
    - Structured concurrency (incubator) introduces scoped concurrency constructs to manage lifetimes of tasks.
- When to use:
    - Cleaner polymorphic code paths, safer and clearer concurrent code in scoped contexts.
- Interview follow-ups:
    - Differences between pattern matching for `instanceof` and `switch`.
    - Status of preview/incubator features and migration considerations.

---

## Java 21 (LTS, 2023)

### Java 21 Features
- Sequenced Collections
    - Why: Provide collections with stable encounter order plus efficient insertion/removal at ends.
    - How: `SequencedCollection<T>` adds `first()`, `last()`, `addFirst()`, `addLast()`, `reversed()`.
    - When to use: When APIs need predictable iteration order and queue/deque semantics with collection guarantees.
- Pattern Matching for `switch`
    - Why: Unified pattern matching across `instanceof` and `switch` with exhaustive checks.
    - Example:
      
      ```java
      switch (obj) {
          case String s -> System.out.println(s.toLowerCase());
          case Integer i -> System.out.println(i + 1);
      }
      ```
      
- Virtual Threads (previewed earlier, finalized in later updates)
    - Why: Simplify massively concurrent I/O-bound programming using lightweight threads.
    - How: Create virtual threads via `Thread.ofVirtual().start(...)` or `Executors.newVirtualThreadPerTaskExecutor()`.
    - When to use: High-concurrency network servers, asynchronous workloads that are I/O-bound.
- Interview follow-ups:
    - How do virtual threads differ from platform threads?
    - How do sequenced collections differ from `Deque` and `List`?
    - How pattern matching affects API evolution and exhaustive switching.

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
| Java 21 | Virtual Threads, Pattern Matching, Sequenced Collections |