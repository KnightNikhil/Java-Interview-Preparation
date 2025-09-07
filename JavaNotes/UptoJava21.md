
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
```java
record User(String name, int age) {}
```

**Follow-up:** Are records immutable? (Yes)

---

## Java 17 (LTS)

### Sealed Classes
```java
sealed class Shape permits Circle, Square {}
```

**Follow-up:** How are sealed classes different from abstract classes?

---

## Java 18-20 (Preview + Incubator Features)

- Pattern Matching for switch (Preview)
- Record Patterns
- Structured concurrency (incubator)

---

## Java 21 (LTS, 2023)

### Virtual Threads (Project Loom)
**Why:** Lightweight threads, scale thousands of concurrent tasks.
```java
Thread.startVirtualThread(() -> handleRequest());
```

**Follow-up:**
- Difference between platform vs virtual threads?
- How does virtual thread scheduling work?

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