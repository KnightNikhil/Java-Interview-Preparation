## Java OOP

### Java Interview Prep Guide (Advanced Topics)

### 🔹 OOP Concepts (Detailed)

### Class & Object:
- A class is a blueprint for creating objects. It defines fields (variables) and methods (functions) to represent behavior.
- An object is a runtime instance of a class.

```java
class Car {
    String model;
    void drive() { System.out.println("Driving " + model); }
}
Car c = new Car(); 
c.model = "Tesla"; 
c.drive();
```

### Encapsulation:
- Wrapping data and methods into a single unit and restricting access using access modifiers.
- Promotes data hiding and control over data.

```java
class Account {
    private double balance;
    public double getBalance() { return balance; }
    public void deposit(double amount) { if(amount > 0) balance += amount; }
}
```

### Abstraction:
- Hides complex implementation details and shows only the necessary features.
- Achieved via abstract classes and interfaces.

```java
abstract class Vehicle {
    abstract void start();
}
class Car extends Vehicle {
    void start() { System.out.println("Car started"); }
}
```

### Inheritance:
- Allows one class to acquire properties and behaviors of another class.
- Promotes code reusability.

```java
class Animal {
    void eat() { System.out.println("Eating..."); }
}
class Dog extends Animal {
    void bark() { System.out.println("Barking..."); }
}
```

### Polymorphism:
- Ability to take many forms. Two types:
  - Compile-time (method overloading)
  - Runtime (method overriding)

```java
class Shape { void draw() { System.out.println("Drawing shape"); } }
class Circle extends Shape { void draw() { System.out.println("Drawing circle"); } }
```

### Object class methods:
- `equals()` – logical equality
- `hashCode()` – bucket placement in hashing
- `toString()` – string representation
- `clone()` – creates copy of object
- `finalize()` – called by GC before object removal (deprecated)

⸻


## 🔹 1. Java Memory Model & JVM Internals

### 🔸 JVM Architecture:

#### 📦 ClassLoader Subsystem
- Loads class files (`.class`) and follows the **Parent Delegation Model** to avoid class conflicts and ensure security.

#### 🧠 Runtime Data Areas
- **Heap:** Stores all objects and class instances.
- **Stack:** Contains method call frames including local variables and operand stacks.
- **Program Counter Register:** Holds the address of the current executing instruction for a thread.
- **Metaspace:** Replaces PermGen to store class metadata.
- **Native Method Stack:** Manages native (non-Java) method calls.

#### ⚙️ Execution Engine
- **Interpreter:** Reads and executes bytecode line-by-line.
- **JIT Compiler (Just-In-Time):** Converts bytecode to native machine code for performance optimization.

---

### 🔸 Garbage Collection (GC)

#### 🗑️ Purpose
- Automates memory management by collecting and freeing unused objects.

#### 🔄 GC Algorithms
- **Serial GC:** Single-threaded, best for small applications.
- **Parallel GC:** Multi-threaded, optimized for throughput.
- **CMS (Concurrent Mark-Sweep):** Low-latency GC (deprecated).
- **G1 GC (Garbage First):** Default collector; splits heap into regions, supports concurrent and parallel collection.

---

### 🔸 Parent Delegation Model
- Class loaders delegate loading to parent loaders before attempting to load themselves.
- Helps maintain consistency and avoids loading same class multiple times.

---

### 🔸 JVM Tuning Parameters
- Control memory and GC behavior using flags:
  - `-Xms`: Initial heap size
  - `-Xmx`: Maximum heap size
  - `-XX:+UseG1GC`: Enable G1 Garbage Collector

---

### 🔸 Memory Leaks / OutOfMemoryError (OOM)

#### 🚨 Memory Leaks
- Caused when references to unused objects are unintentionally retained.

#### 💥 Types of OOM Errors
- `java.lang.OutOfMemoryError: Java heap space`
- `java.lang.OutOfMemoryError: GC overhead limit exceeded`
- `java.lang.OutOfMemoryError: Metaspace`

---

### 🔸 Interview Follow-up Questions

#### Q1: What are the major components of the JVM?
**A:** ClassLoader, Runtime Data Areas (Heap, Stack, Metaspace, etc.), Execution Engine (Interpreter & JIT), and Garbage Collector.

#### Q2: What is the Parent Delegation Model and why is it important?
**A:** It ensures security and class consistency by allowing child classloaders to defer loading to parent loaders first.

#### Q3: Difference between Heap and Stack memory?
**A:** Heap stores objects and instances; Stack stores method calls and local variables. Stack is thread-specific, Heap is shared.

#### Q4: What causes OutOfMemoryError in Java?
**A:** It occurs when memory regions (Heap, Metaspace, etc.) are exhausted due to memory leaks or insufficient configuration.

#### Q5: How does G1 GC work?
**A:** G1 divides the heap into regions and collects them in parallel and concurrently, optimizing for predictable pause times.

---

⸻

### 🔹 2. equals() & hashCode()

#### Contract:

- Must follow these properties:
  - Reflexive: `a.equals(a)` must be true
  - Symmetric: `a.equals(b)` ⇔ `b.equals(a)`
  - Transitive: `a.equals(b)` & `b.equals(c)` ⇒ `a.equals(c)`
  - Consistent: Repeated calls must return the same result
  - Null-safe: `a.equals(null)` must return false
- If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` must also hold

#### Best Practices:

- Avoid using mutable fields in `equals()` and `hashCode()`
- Use `Objects.equals()` and `Objects.hash()` (Java 7+)

#### HashMap/HashSet:

- **hashCode()** determines the bucket
- **equals()** determines object equality within the bucket

#### Comparable & Comparator:

- **Comparable**: Defines natural order

```java
class Person implements Comparable<Person> {
    int age;
    public int compareTo(Person p) {
        return Integer.compare(this.age, p.age);
    }
}
```

- **Comparator**: Defines custom order

```java
Comparator<Person> byName = Comparator.comparing(p -> p.name);
```

---

⸻

### 🔹 3. Immutability & Defensive Copying

#### Steps for Immutable Class:
1.	Declare class as final
2.	Make all fields private final
3.	Initialize via constructor only
4.	Return copies of mutable fields

#### Benefits:
*    Thread-safe by default
*    Makes reasoning about code easier
*    Useful in caching and map keys

#### Example:
```java
final class Employee {
private final String name;
private final Date dob;

    public Employee(String name, Date dob) {
        this.name = name;
        this.dob = new Date(dob.getTime());
    }
    public Date getDob() { return new Date(dob.getTime()); }
}
```


⸻

### 🔹 4. Serialization & Deserialization

#### Serializable vs Externalizable:
*    **Serializable:** JVM handles serialization
*    **Externalizable:** You write writeExternal() and readExternal()

Custom Serialization:
```java
private void writeObject(ObjectOutputStream oos) throws IOException {
oos.defaultWriteObject();
oos.writeInt(age * 2); // custom
}
```

**readResolve():**
*    Used to maintain Singleton during deserialization

**serialVersionUID:**
*    Explicit version ID avoids InvalidClassExceptions

⸻

### 🔹 5. Annotations

**Built-in:**
*    @Override, @Deprecated, @SuppressWarnings

**Custom Annotations:**
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {}
```

**Processing:**
*    Reflection (at runtime)
*    APT (compile time)

⸻

### 🔹 6. Reflection API

#### Accessing Metadata:
```java
Class<?> clazz = Class.forName("Employee");
Method method = clazz.getDeclaredMethod("getName");
method.setAccessible(true);
Object result = method.invoke(emp);
```

**Use Cases:** Frameworks, Test Automation, Serialization tools

**Drawbacks:**
*    Slower than direct method calls
*    Security manager can restrict access

⸻

### 🔹 7. ClassLoaders

##### Types:
*    **Bootstrap:** loads java.*
*    **Extension:** loads ext/*
*    **Application:** loads from classpath

Custom ClassLoader:
```java
class MyClassLoader extends ClassLoader {
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] data = loadClassData(name);
    return defineClass(name, data, 0, data.length);
  }
}
```

**Framework Use:** Spring, Hibernate, Tomcat use ClassLoader for isolation

⸻

### 🔹 8. Enums (Advanced Use)

**Enums with Methods:**
```java
public enum Operation {
ADD { public int apply(int x, int y) { return x + y; } },
SUB { public int apply(int x, int y) { return x - y; } };
public abstract int apply(int x, int y);
}
```


**EnumSet/EnumMap:**
*    High-performance specialized collections for enums

**Strategy Pattern:**
*    Enums encapsulate behaviors without if-else


⸻

🧠 Follow-Up Interview Questions (Answered)
1.	**How does the JVM handle class unloading?**
*    Classes are unloaded when their classloader becomes unreachable and GC collects it. Mostly happens in dynamic environments (e.g., Tomcat).
2.	**Can you override equals() without hashCode()?**
*    Yes, but violates contract. Hash-based collections (HashMap/HashSet) will misbehave—equal objects may go into different buckets.
3.	**How would you implement a cache using EnumMap?**
*    If keys are enums:
```java
enum Status { ACTIVE, INACTIVE }
EnumMap<Status, String> cache = new EnumMap<>(Status.class);
```
*    Faster and memory-efficient vs HashMap.
4.	**Explain use cases of sealed classes in domain modeling.**
*    Domain boundaries (e.g., only Circle, Rectangle are valid Shape types). Prevents arbitrary extension and improves pattern matching.
5.	**What are the performance implications of Reflection?**
*    Slower than direct calls due to dynamic dispatch. Also breaks compiler optimizations and introduces security risks.
6.	**How does Java ensure backward compatibility in Serialization?**
*    Via serialVersionUID. If the UID matches between sender/receiver class, deserialization succeeds even if class evolved.
7.	**How is ClassLoader hierarchy useful for application isolation?**
*    Different classloaders load the same class name in isolated ways, allowing independent libraries/apps in the same JVM (e.g., servlet containers).
8.	**What is the difference between compile-time and runtime polymorphism?**
*    Compile-time: Overloading; resolved at compile-time. Runtime: Overriding; resolved via dynamic dispatch.
9.	**How do records compare to traditional POJOs?**
*    Records auto-generate boilerplate (constructor, getters, equals/hashCode, toString) and are immutable by default.
10.	**What are the trade-offs of immutability in large-scale systems?**
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

