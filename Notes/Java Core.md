## Java OOP 

Java Interview Prep Guide (Advanced Topics)

🔹 OOP Concepts (Detailed)

Class & Object:
*    A class is a blueprint for creating objects. It defines fields (variables) and methods (functions) to represent behavior.
*    An object is a runtime instance of a class.
*    Example:

class Car {
String model;
void drive() { System.out.println("Driving " + model); }
}
Car c = new Car(); c.model = "Tesla"; c.drive();



Encapsulation:
*    Wrapping data and methods into a single unit and restricting access using access modifiers.
*    Promotes data hiding and control over data.
*    Example:

class Account {
private double balance;
public double getBalance() { return balance; }
public void deposit(double amount) { if(amount > 0) balance += amount; }
}



Abstraction:
*    Hides complex implementation details and shows only the necessary features.
*    Achieved via abstract classes and interfaces.
*    Example:

abstract class Vehicle {
abstract void start();
}
class Car extends Vehicle {
void start() { System.out.println("Car started"); }
}



Inheritance:
*    Allows one class to acquire properties and behaviors of another class.
*    Promotes code reusability.
*    Example:

class Animal {
void eat() { System.out.println("Eating..."); }
}
class Dog extends Animal {
void bark() { System.out.println("Barking..."); }
}



Polymorphism:
*    Ability to take many forms. Two types:
*    Compile-time (method overloading)
*    Runtime (method overriding)
*    Example:

class Shape { void draw() { System.out.println("Drawing shape"); } }
class Circle extends Shape { void draw() { System.out.println("Drawing circle"); } }



Object class methods:
*    equals() – logical equality
*    hashCode() – bucket placement in hashing
*    toString() – string representation
*    clone() – creates copy of object
*    finalize() – called by GC before object removal (deprecated)

⸻

🔹 1. Java Memory Model & JVM Internals

JVM Architecture:
*    ClassLoader Subsystem: Loads class files (.class), follows parent delegation.
*    Runtime Data Areas:
*    Heap: Objects and class instances
*    Stack: Method frames (method call stack)
*    Program Counter Register: Keeps track of current instruction
*    Metaspace: Stores class metadata (replaces PermGen)
*    Native Method Stack: For native method execution
*    Execution Engine:
*    Interpreter: Executes bytecode line-by-line
*    JIT Compiler: Optimizes bytecode to native code

Garbage Collection (GC):
*    Automatic memory management
*    GC Algorithms:
*    Serial GC: Single-threaded, for small apps
*    Parallel GC: Multiple threads, throughput focus
*    CMS (Concurrent Mark Sweep): Low pause, now deprecated
*    G1 GC (Garbage First): Splits heap into regions, concurrent & parallel, default

Parent Delegation Model:
*    Custom class loaders delegate to parent first, ensuring security and avoiding class duplication.

JVM Tuning Parameters:
*    -Xms, -Xmx, -XX:+UseG1GC, etc. control heap size, GC behavior

Memory Leaks / OOM:
*    Memory leak: objects retained beyond usefulness
*    OutOfMemoryError types:
*    Java heap space
*    GC overhead limit exceeded
*    Metaspace

⸻

🔹 2. equals() & hashCode()

Contract:
*    Reflexive, Symmetric, Transitive, Consistent, Null-safe
*    If a.equals(b) == true, then a.hashCode() == b.hashCode() must hold

Best Practices:
*    Don’t use mutable fields in equals/hashCode
*    Use Objects.equals() and Objects.hash() from Java 7+

HashMap/HashSet:
*    Use hashCode() to find bucket, then equals() to compare within bucket

Comparable & Comparator:
*    Comparable<T>: natural order
*    Comparator<T>: custom order

class Person implements Comparable<Person> {
int age;
public int compareTo(Person p) { return Integer.compare(this.age, p.age); }
}

Comparator<Person> byName = Comparator.comparing(p -> p.name);


⸻

🔹 3. Immutability & Defensive Copying

Steps for Immutable Class:
1.	Declare class as final
2.	Make all fields private final
3.	Initialize via constructor only
4.	Return copies of mutable fields

Benefits:
*    Thread-safe by default
*    Makes reasoning about code easier
*    Useful in caching and map keys

Example:

final class Employee {
private final String name;
private final Date dob;

    public Employee(String name, Date dob) {
        this.name = name;
        this.dob = new Date(dob.getTime());
    }
    public Date getDob() { return new Date(dob.getTime()); }
}


⸻

🔹 4. Serialization & Deserialization

Serializable vs Externalizable:
*    Serializable: JVM handles serialization
*    Externalizable: You write writeExternal() and readExternal()

Custom Serialization:

private void writeObject(ObjectOutputStream oos) throws IOException {
oos.defaultWriteObject();
oos.writeInt(age * 2); // custom
}

readResolve():
*    Used to maintain Singleton during deserialization

serialVersionUID:
*    Explicit version ID avoids InvalidClassExceptions

⸻

🔹 5. Annotations

Built-in:
*    @Override, @Deprecated, @SuppressWarnings

Custom Annotations:

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {}

Processing:
*    Reflection (at runtime)
*    APT (compile time)

⸻

🔹 6. Reflection API

Accessing Metadata:

Class<?> clazz = Class.forName("Employee");
Method method = clazz.getDeclaredMethod("getName");
method.setAccessible(true);
Object result = method.invoke(emp);

Use Cases: Frameworks, Test Automation, Serialization tools

Drawbacks:
*    Slower than direct method calls
*    Security manager can restrict access

⸻

🔹 7. ClassLoaders

Types:
*    Bootstrap: loads java.*
*    Extension: loads ext/*
*    Application: loads from classpath

Custom ClassLoader:

class MyClassLoader extends ClassLoader {
protected Class<?> findClass(String name) throws ClassNotFoundException {
byte[] data = loadClassData(name);
return defineClass(name, data, 0, data.length);
}
}

Framework Use: Spring, Hibernate, Tomcat use ClassLoader for isolation

⸻

🔹 8. Enums (Advanced Use)

Enums with Methods:

public enum Operation {
ADD { public int apply(int x, int y) { return x + y; } },
SUB { public int apply(int x, int y) { return x - y; } };
public abstract int apply(int x, int y);
}

EnumSet/EnumMap:
*    High-performance specialized collections for enums

Strategy Pattern:
*    Enums encapsulate behaviors without if-else

⸻

🔹 9. Modules (Java 9+)

JPMS Goals:
*    Strong encapsulation, explicit dependencies

module-info.java:

module com.example.calc {
requires java.base;
exports com.example.calc.api;
}

Encapsulation:
*    Only exported packages are visible

⸻

🔹 10. var, records, sealed classes

var (Java 10):
*    Type inference for local variables
*    Improves readability, reduces boilerplate

record (Java 14+):

public record Person(String name, int age) {}
// generates: constructor, getters, equals, hashCode, toString

sealed (Java 15+):
*    Restrict which classes can implement/extend

sealed interface Shape permits Circle, Square {}
final class Circle implements Shape {}
final class Square implements Shape {}


⸻

🧠 Follow-Up Interview Questions (Answered)
1.	How does the JVM handle class unloading?
*    Classes are unloaded when their classloader becomes unreachable and GC collects it. Mostly happens in dynamic environments (e.g., Tomcat).
2.	Can you override equals() without hashCode()?
*    Yes, but violates contract. Hash-based collections (HashMap/HashSet) will misbehave—equal objects may go into different buckets.
3.	How would you implement a cache using EnumMap?
*    If keys are enums:
```java
enum Status { ACTIVE, INACTIVE }
EnumMap<Status, String> cache = new EnumMap<>(Status.class);
```
*    Faster and memory-efficient vs HashMap.

4.	Explain use cases of sealed classes in domain modeling.
*    Domain boundaries (e.g., only Circle, Rectangle are valid Shape types). Prevents arbitrary extension and improves pattern matching.
5.	What are the performance implications of Reflection?
*    Slower than direct calls due to dynamic dispatch. Also breaks compiler optimizations and introduces security risks.
6.	How does Java ensure backward compatibility in Serialization?
*    Via serialVersionUID. If the UID matches between sender/receiver class, deserialization succeeds even if class evolved.
7.	How is ClassLoader hierarchy useful for application isolation?
*    Different classloaders load the same class name in isolated ways, allowing independent libraries/apps in the same JVM (e.g., servlet containers).
8.	What is the difference between compile-time and runtime polymorphism?
*    Compile-time: Overloading; resolved at compile-time. Runtime: Overriding; resolved via dynamic dispatch.
9.	How do records compare to traditional POJOs?
*    Records auto-generate boilerplate (constructor, getters, equals/hashCode, toString) and are immutable by default.
10.	What are the trade-offs of immutability in large-scale systems?

*    Pros: Thread-safety, predictability, cache-safe. Cons: Object creation overhead, more GC pressure, harder modeling for certain mutable domains.

## 🔹 Functional Programming Concepts

### ✅ Pure Functions

A pure function:

* Has no side effects (does not modify global state or parameters).
* Always returns the same output for the same input.

**Example:**

```java
int square(int x) {
    return x * x;
}
```

**Follow-up Questions:**

* *Why are pure functions important in multi-threading?*
  Because they’re thread-safe by design—no shared mutable state.

---

### ✅ Higher-order Functions

These are functions that:

* Take other functions as arguments.
* Or return functions as results.

**Example:**

```java
Function<Integer, Integer> square = x -> x * x;
Function<Integer, Integer> doubler = x -> x * 2;

List<Integer> result = list.stream().map(square).collect(Collectors.toList());
```

**Follow-up:**

* *What Java features support higher-order functions?*
  Lambdas and functional interfaces like `Function<T,R>`, `Consumer<T>`, `Supplier<T>`.

---

### ✅ Currying and Function Composition

**Currying:**
Breaking a function that takes multiple arguments into a series of unary functions.

**Composition:**
Combining two functions: `f(g(x))`.

**Example:**

```java
Function<Integer, Integer> add2 = x -> x + 2;
Function<Integer, Integer> times3 = x -> x * 3;

Function<Integer, Integer> composed = add2.andThen(times3); // (x + 2) * 3
```

**Follow-up:**

* *How do you compose predicates?*
  Using `Predicate#and`, `Predicate#or`.

---

### ✅ Functional vs Imperative Style

* **Imperative:** You tell how to do it (loops, control flow).
* **Functional:** You declare what to do (Streams, lambdas).

**Example:**

```java
// Imperative
int sum = 0;
for (int n : list) sum += n;

// Functional
int sum = list.stream().mapToInt(Integer::intValue).sum();
```

---

## 🔹 Best Practices & Code Quality

### ✅ Clean Code Principles

* Meaningful names
* Small, focused methods
* Single Responsibility Principle
* Avoid duplication

**Example:**
Avoid:

```java
public void process(Data d) { ... }
```

Use:

```java
public void encryptUserDetails(UserData userData) { ... }
```

---

### ✅ Defensive Programming

* Check for nulls
* Validate inputs
* Fail fast with informative errors

**Example:**

```java
Objects.requireNonNull(userId, "User ID must not be null");
```

---

### ✅ Design for Testability

* Avoid static methods
* Inject dependencies
* Separate concerns
* Return predictable outputs

---

### ✅ Idiomatic Java

* Use enhanced `for` loop or streams
* Prefer `StringBuilder` for concatenation
* Use `Optional` instead of nulls when suitable

---

## 🧠 Bonus Interview Topics

---

### ✅ `final`, `static`, `transient`, `volatile`, `synchronized`

* `final`: Can’t reassign (variables), extend (classes), override (methods)
* `static`: Belongs to class, not instance
* `transient`: Skips field during serialization
* `volatile`: Ensures visibility across threads
* `synchronized`: Ensures atomicity and mutual exclusion

---

### ✅ Boxing/Unboxing Pitfalls

```java
Integer a = 128;
Integer b = 128;
System.out.println(a == b); // false (different objects)
```

Use `.equals()` for comparison.

---

### ✅ String Intern Pool

* String literals are interned by default.
* `String.intern()` adds strings to the pool.

**Example:**

```java
String a = "abc";
String b = new String("abc").intern();
System.out.println(a == b); // true
```

---

### ✅ StringBuilder vs StringBuffer vs `+`

* `StringBuilder`: Fast, not thread-safe.
* `StringBuffer`: Thread-safe but slower.
* `+`: Concatenation (converted to StringBuilder internally in most cases).

---

### ✅ Compile-time vs Runtime Polymorphism

* **Compile-time**: Method overloading
* **Runtime**: Method overriding via inheritance

---

### ✅ Java Agent Basics

Used for instrumentation and profiling.

**Key Methods:**

```java
public static void premain(String args, Instrumentation inst) {
    // Modify bytecode before main()
}
```

---

✅ Let me know if you'd like a downloadable `.md` version or want to go through mock Q\&A for these topics.
