# Java Lambda Expressions – Complete Guide

## Table of Contents
- [1. Overview](#1-overview)
- [2. Java Functional Interfaces – Complete Guide](#java-functional-interfaces--complete-guide)
- [3. Java Streams API](#java-streams-api)
- [4. Java Method and Constructor References](#java-method-and-constructor-references)
- [5. Java Default and Static Methods in Interfaces](#java-default-and-static-methods-in-interfaces)
- [6. Java Optional Class](#java-optional-class)
- [7. Java Date and Time API (java.time)](#java-date-and-time-api-javatime)
- [8. Java Parallel Streams](#java-parallel-streams)

 

##   1. Overview

A **lambda expression** is a short block of code which takes in parameters and returns a value. It can be used to provide the implementation of a **functional interface**.

> Lambda expression = Anonymous function (with optional parameters) + Clean, concise syntax

---

##   2. Syntax and Usage

### Syntax:
```java
(parameters) -> expression
(parameters) -> { statements; }
```

### Examples:

#### Example 1: Simple Lambda
```java
Runnable r = () -> System.out.println("Running thread!");
new Thread(r).start();
```

#### Example 2: Lambda with parameters
```java
BinaryOperator<Integer> add = (a, b) -> a + b;
System.out.println(add.apply(10, 20)); // Output: 30
```

#### Example 3: Lambda with block
```java
Comparator<String> comparator = (s1, s2) -> {
    int result = s1.length() - s2.length();
    return result;
};
```

### When to Use:
- Implementing **functional interfaces**
- **Inline behavior** without boilerplate code
- Common with **Streams**, **Collections**, **Callbacks**

---

##   3. Benefits over Anonymous Classes

| Feature | Anonymous Class | Lambda |
|--------|------------------|--------|
| Syntax | Verbose | Concise |
| Class File | Creates new class | No extra class |
| Readability | Moderate | High |
| Performance | Slightly slower | Better with `invokedynamic` |

### Comparison Example:
```java
// Anonymous class
Runnable r1 = new Runnable() {
    public void run() {
        System.out.println("Running!");
    }
};

// Lambda
Runnable r2 = () -> System.out.println("Running!");
```

---

##   4. Used with Collections & Functional Interfaces

> A **Functional Interface** is an interface with exactly one abstract method.  
Examples: `Runnable`, `Comparator`, `Predicate`, `Function`, `Consumer`, `Supplier`.

### Examples:

#### forEach with List
```java
List<String> names = Arrays.asList("Nikhil", "Amit", "Ravi");
names.forEach(name -> System.out.println(name));
```

#### removeIf with Predicate
```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
nums.removeIf(n -> n % 2 == 0); // Removes evens => [1, 3, 5]
```

#### sort with Comparator
```java
List<String> words = List.of("bat", "apple", "dog");
words.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
```

#### Map Iteration
```java
Map<String, Integer> map = Map.of("A", 1, "B", 2);
map.forEach((key, value) -> System.out.println(key + "=" + value));
```

---

##   5. Why Use Lambda Expressions?

| Use Case | Lambda Advantage |
|----------|------------------|
| Sorting a list | Concise Comparator |
| Filtering | Works with `Stream.filter()` |
| Event handling | Cleaner than anonymous classes |
| Multi-threading | Cleaner Runnable implementation |
| On-the-fly logic | Functional + Flexible |

---

##   6. Interview Questions and Answers

###   Q1: What is a lambda expression and why is it used in Java?

**Answer:**  
A lambda expression is an anonymous function that implements a functional interface. It’s used to pass behavior (code) as a parameter, making code cleaner and concise for operations like filtering, sorting, callbacks, etc.

---

###   Q2: Difference between a lambda expression and an anonymous class?

**Answer:**
- Lambdas are **concise**, better-performing, and reference the enclosing class using `this`.
- Anonymous classes are **verbose**, create extra class files, and reference themselves with `this`.

**Code Example:**
```java
Runnable r = () -> System.out.println(this); // Refers to enclosing class
```

---

###   Q3: Can a lambda expression throw exceptions?

**Answer:**
Yes, if the functional interface method declares the exception.

```java
@FunctionalInterface
interface CheckedFunction {
    void run() throws IOException;
}

CheckedFunction cf = () -> { throw new IOException("Error"); };
```

---

###   Q4: Variable scope in lambda?

**Answer:**
Lambdas can access **effectively final** local variables.

```java
String message = "Hello";
// message = "Changed"; // This causes compile error
Runnable r = () -> System.out.println(message);
```

---

###   Q5: Can lambda expressions be serialized?

**Answer:**
Only if the target functional interface is `Serializable`.  
Avoid serialization of lambdas for portability and maintainability.

---

###   Q6: How does lambda work internally?

**Answer:**
- Compiles using `invokedynamic`
- Uses method handles and `LambdaMetafactory`
- No inner class generated like anonymous class

---

###   Q7: Lambda in multi-threading?

**Answer:**
Yes, great for `Runnable`, `Callable`, `Executors`.

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(() -> System.out.println("Task executed"));
```

---

###   Q8: Can you use lambda to implement multiple abstract methods?

**Answer:**
No. Lambdas only work with **functional interfaces**, i.e., one abstract method.

```java
interface MyInterface {
    void method1();
    void method2(); // Not functional interface
}
```

---

## Summary

| Concept | Key Point |
|--------|-----------|
| Lambda Syntax | `(args) -> expr` or `(args) -> { body }` |
| Used With | Functional Interfaces |
| Benefits | Short, readable, no boilerplate |
| Collections | Works with filter, forEach, sort, etc. |
| Interview Focus | Scope, Serialization, `this`, Exception handling |

---
⸻

# Java Functional Interfaces – Complete Guide

##   What is a Functional Interface?

A **Functional Interface** in Java is an interface that contains exactly **one abstract method**. They may contain multiple default or static methods but only **one unimplemented method**.

> Functional interfaces are the foundation of **lambda expressions** and **method references** in Java.

---

##   Built-in Functional Interfaces (java.util.function package)

### 🔸 1. Predicate<T>
Used for **boolean-valued** expressions (like filtering).
```java
Predicate<String> isLong = str -> str.length() > 5;
System.out.println(isLong.test("Hello")); // false
```

### 🔸 2. Function<T, R>
Takes an input `T`, returns output `R`.
```java
Function<String, Integer> strLength = s -> s.length();
System.out.println(strLength.apply("Hello")); // 5
```

### 🔸 3. Consumer<T>
Takes input but **returns nothing**.
```java
Consumer<String> printer = s -> System.out.println("Name: " + s);
printer.accept("Nikhil");
```

### 🔸 4. Supplier<T>
Takes **no input**, returns `T`.
```java
Supplier<Double> randomGenerator = () -> Math.random();
System.out.println(randomGenerator.get());
```

### 🔸 5. UnaryOperator<T> & BinaryOperator<T>
```java
UnaryOperator<Integer> square = x -> x * x;
System.out.println(square.apply(5)); // 25

BinaryOperator<Integer> add = (a, b) -> a + b;
System.out.println(add.apply(10, 20)); // 30
```

### 🔸 6. BiPredicate<T, U>, BiFunction<T, U, R>, BiConsumer<T, U>

#### BiPredicate<T, U>
```java
BiPredicate<String, String> startsWith = (s1, prefix) -> s1.startsWith(prefix);
System.out.println(startsWith.test("Functional", "Fun")); // true
```

#### BiFunction<T, U, R>
```java
BiFunction<Integer, Integer, String> sumStr = (a, b) -> "Sum is " + (a + b);
System.out.println(sumStr.apply(5, 6)); // Sum is 11
```

#### BiConsumer<T, U>
```java
BiConsumer<String, Integer> printer = (name, age) -> 
    System.out.println(name + " is " + age + " years old");
printer.accept("Nikhil", 25);
```

---

##   Custom Functional Interfaces

You can create your own if needed. Just remember: only **1 abstract method**.
```java
@FunctionalInterface
interface StringProcessor {
    String process(String input);
}
```
Usage:
```java
StringProcessor sp = s -> s.toUpperCase();
System.out.println(sp.process("hello")); // HELLO
```

---

##   The @FunctionalInterface Annotation

This annotation is **optional** but **recommended**. It tells the compiler:
- This interface must have **only one abstract method**
- If violated, the compiler will throw an error

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);

    default void print() {
        System.out.println("Calculating...");
    }
}
```

Violation:
```java
@FunctionalInterface
interface InvalidInterface {
    void method1();
    void method2(); // Compilation error
}
```

---

##   Interview Questions & Answers

###   Q1: What is a functional interface?
**Answer:**  
An interface with exactly one abstract method. It's used as a target for lambda expressions and method references.

---

###   Q2: What is the purpose of `@FunctionalInterface` annotation?
**Answer:**  
It instructs the compiler to ensure that the interface has **exactly one abstract method**. If it has more, the compiler throws an error.

---

###   Q3: Difference between `Predicate`, `Function`, `Consumer`, and `Supplier`?

| Interface | Input | Output | Use Case |
|----------|-------|--------|----------|
| Predicate<T> | T | boolean | Filtering, condition checks |
| Function<T,R> | T | R | Mapping, transforming |
| Consumer<T> | T | void | Performing action, printing |
| Supplier<T> | none | T | Object creation, random value |

---

###   Q4: Can you create your own functional interface?
**Answer:**  
Yes. Just make sure it has one abstract method, and optionally annotate it with `@FunctionalInterface`.

---

###   Q5: Can functional interfaces have default and static methods?
**Answer:**  
Yes. Functional interfaces can have multiple **default** or **static** methods but **only one abstract method**.

---

###   Q6: What's the difference between `Function` and `BiFunction`?
**Answer:**
- `Function<T, R>` takes **1 argument** and returns **R**
- `BiFunction<T, U, R>` takes **2 arguments**

---

###   Q7: What happens if you have more than one abstract method in a `@FunctionalInterface`?
**Answer:**  
Compilation error:
```
Unexpected @FunctionalInterface annotation
```

---

###   Q8: Can functional interfaces be generic?
**Answer:**  
Yes.
```java
@FunctionalInterface
interface Processor<T> {
    T process(T input);
}
```

---

## Summary Table

| Interface | Type | Description | Method |
|-----------|------|-------------|--------|
| `Predicate<T>` | Built-in | Tests a condition | `test(T t)` |
| `Function<T, R>` | Built-in | Transforms T to R | `apply(T t)` |
| `Consumer<T>` | Built-in | Consumes T | `accept(T t)` |
| `Supplier<T>` | Built-in | Supplies T | `get()` |
| `BiPredicate<T, U>` | Built-in | Two-input boolean check | `test(T t, U u)` |
| `BiFunction<T, U, R>` | Built-in | Maps (T, U) to R | `apply(T, U)` |
| `BiConsumer<T, U>` | Built-in | Consumes two inputs | `accept(T, U)` |

---
⸻


# Java Streams API

Java Streams API, introduced in Java 8, allows processing collections of data in a declarative and functional style.

---

## 1. Stream Creation and Pipeline Structure

Streams are created from collections, arrays, or I/O channels. The pipeline consists of:

- **Source**: A collection, array, or generator function.
- **Intermediate Operations**: `map()`, `filter()`, etc.
- **Terminal Operation**: `collect()`, `forEach()`, `reduce()`, etc.

### Example:
```java
List<String> names = List.of("Alice", "Bob", "Charlie");

names.stream()
     .filter(name -> name.startsWith("A"))
     .map(String::toUpperCase)
     .forEach(System.out::println);
```

---

## 2. Intermediate Operations

Intermediate operations return a stream.

- `map(Function)`: Transform each element.
- `filter(Predicate)`: Filter elements by condition.
- `sorted()`: Natural or custom sort.
- `distinct()`: Removes duplicates.
- `limit(n)`, `skip(n)`: Control size/offset.

### Example:
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);
List<Integer> squares = numbers.stream()
                               .map(n -> n * n)
                               .collect(Collectors.toList());
```

---

## 3. Terminal Operations

- `collect()`: Gather results.
- `count()`: Number of elements.
- `reduce()`: Combine elements.
- `min()`, `max()`: Get smallest/largest.
- `forEach()`: Perform action.

### Example:
```java
Optional<Integer> sum = List.of(1, 2, 3, 4).stream()
                                          .reduce((a, b) -> a + b);
```

---

## 4. Stream Laziness and Short-Circuiting

Streams are lazy — operations are not executed until a terminal operation is invoked. Some operations can stop early.

### Example:
```java
List.of("one", "two", "three").stream()
    .filter(s -> {
        System.out.println("Filtering: " + s);
        return s.length() > 3;
    })
    .findFirst(); // Short-circuits after one match
```

---

## 5. Collectors Utility Class

`java.util.stream.Collectors` contains methods for reduction.

- `toList()`, `toSet()`
- `joining()`
- `summarizingInt()`, `averagingInt()`
- `groupingBy()`, `partitioningBy()`

---

## 6. Grouping, Partitioning, and Mapping

### Grouping:
```java
Map<Integer, List<String>> byLength = List.of("a", "bb", "ccc")
    .stream()
    .collect(Collectors.groupingBy(String::length));
```

### Partitioning:
```java
Map<Boolean, List<Integer>> evenOdd = List.of(1, 2, 3, 4, 5)
    .stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

### Mapping:
```java
Map<Integer, List<Character>> charsByLength = List.of("a", "bb")
    .stream()
    .collect(Collectors.groupingBy(String::length,
             Collectors.mapping(s -> s.charAt(0), Collectors.toList())));
```

---

## 7. Primitive Streams

Specialized streams to avoid boxing:

- `IntStream`, `LongStream`, `DoubleStream`

### Example:
```java
int sum = IntStream.range(1, 5).sum(); // 1 + 2 + 3 + 4 = 10
```

---

## Interview Questions

### Q1: Why are streams lazy?
**A**: Intermediate operations are not executed until a terminal operation is triggered. This helps optimize performance and avoid unnecessary computation.

### Q2: What's the difference between `map()` and `flatMap()`?
**A**: `map()` transforms each element, `flatMap()` flattens nested structures.

```java
List<List<String>> data = List.of(List.of("a"), List.of("b"));
List<String> flat = data.stream().flatMap(List::stream).collect(Collectors.toList());
```

### Q3: How does `reduce()` work?
**A**: It's a general-purpose reduction operation that combines stream elements into one.

```java
int product = Stream.of(1, 2, 3).reduce(1, (a, b) -> a * b);
```

---

## Summary

The Streams API enables readable, maintainable, and efficient data processing in Java. Mastering intermediate and terminal operations is key to effective functional-style Java programming.

⸻

# Java Method and Constructor References

Method and constructor references are a shorthand notation of lambda expressions to call methods or constructors.

---

## 1. Static Method References

Syntax: `ClassName::staticMethodName`

Used when a lambda calls a static method.

### Example:
```java
List<String> names = List.of("a", "b", "c");

names.forEach(System.out::println); // Instead of names.forEach(x -> System.out.println(x))
```

Another example:
```java
Function<String, Integer> parse = Integer::parseInt;
System.out.println(parse.apply("123")); // 123
```

---

## 2. Instance Method References

Syntax: `instance::instanceMethodName`  
or `ClassName::instanceMethodName` (when the instance is provided as the first parameter)

### Example 1: Specific instance
```java
String str = "hello";
Supplier<Integer> lengthSupplier = str::length;
System.out.println(lengthSupplier.get()); // 5
```

### Example 2: Arbitrary instance of a particular type
```java
List<String> words = List.of("car", "bus", "train");
words.sort(String::compareToIgnoreCase); // Equivalent to (a, b) -> a.compareToIgnoreCase(b)
```

---

## 3. Constructor References

Syntax: `ClassName::new`

Used to create a new object, especially in streams or functional interfaces.

### Example:
```java
Supplier<List<String>> listSupplier = ArrayList::new;
List<String> list = listSupplier.get(); // Creates new ArrayList
```

Another example:
```java
Function<String, Integer> intCreator = Integer::new;
Integer num = intCreator.apply("42"); // 42
```

With custom classes:
```java
class Person {
    String name;
    Person(String name) {
        this.name = name;
    }
}

Function<String, Person> personCreator = Person::new;
Person p = personCreator.apply("Alice");
```

---

## Summary Table

| Type                     | Syntax                      | Example                        |
|--------------------------|-----------------------------|--------------------------------|
| Static Method            | `Class::staticMethod`       | `Integer::parseInt`           |
| Instance Method (object) | `object::instanceMethod`    | `"hello"::length`             |
| Instance Method (class)  | `Class::instanceMethod`     | `String::compareToIgnoreCase` |
| Constructor              | `Class::new`                | `ArrayList::new`              |

---

## Interview Questions

### Q1: When would you use a method reference over a lambda?
**A**: When the lambda simply calls a method, method references make the code cleaner and more readable.

### Q2: What's the difference between `Class::instanceMethod` and `instance::instanceMethod`?
**A**: `Class::instanceMethod` is used when the first parameter of the lambda is the object, e.g., `list.sort(String::compareToIgnoreCase)`.  
`instance::instanceMethod` is used when calling a method on a specific instance.

### Q3: How are constructor references useful?
**A**: They simplify object creation in streams and functional interfaces like `Supplier`, `Function`, or `BiFunction`.

---

## Conclusion

Method and constructor references improve the readability of lambda expressions when simply calling existing methods or constructors. Understanding their types and proper usage is key to writing idiomatic Java 8+ code.
"""

⸻

# Java Default and Static Methods in Interfaces

Java 8 introduced **default** and **static methods** in interfaces to enable developers to add new methods to interfaces without breaking existing implementations.

---

## 1. Default Methods

Default methods allow interfaces to have concrete methods.

### Syntax:
```java
interface MyInterface {
    default void sayHello() {
        System.out.println("Hello from MyInterface");
    }
}
```

### Example:
```java
interface Vehicle {
    default void start() {
        System.out.println("Vehicle started");
    }
}

class Car implements Vehicle {}

public class Test {
    public static void main(String[] args) {
        Car car = new Car();
        car.start(); // Vehicle started
    }
}
```

### Why Needed:
To provide **backward compatibility**. Suppose a new method needs to be added to an existing interface with many implementations — default methods let you do that without breaking those classes.

---

## 2. Static Methods

Static methods in interfaces are utility methods and cannot be overridden.

### Syntax:
```java
interface Utility {
    static void log(String msg) {
        System.out.println("LOG: " + msg);
    }
}
```

### Usage:
```java
Utility.log("Test message");
```

---

## 3. Rules and Restrictions

- A class can implement multiple interfaces with default methods.
- Static methods cannot be inherited or overridden.
- Default methods cannot override `Object` class methods like `equals()`, `hashCode()`.

---

## 4. Interface Conflict Resolution

### Scenario:
When a class implements two interfaces with the same default method, the class must override it.

### Example:
```java
interface A {
    default void greet() {
        System.out.println("Hello from A");
    }
}

interface B {
    default void greet() {
        System.out.println("Hello from B");
    }
}

class C implements A, B {
    public void greet() {
        A.super.greet(); // or B.super.greet()
    }
}
```

---

## Interview Questions

### Q1: Why were default methods introduced?
**A**: To enable the evolution of interfaces without breaking existing implementations.

### Q2: Can you override default methods?
**A**: Yes. Implementing classes can override default methods.

### Q3: What happens if two interfaces have same default method?
**A**: Compilation error unless the implementing class overrides it and resolves the conflict.

### Q4: Can default methods override methods in `java.lang.Object`?
**A**: No. Default methods cannot override `Object` methods like `equals`, `hashCode`, `toString`.

---

## Summary

| Feature         | Default Method                    | Static Method                         |
|-----------------|------------------------------------|----------------------------------------|
| Purpose         | Add implementation to interface   | Add utility methods                    |
| Overridable     | Yes                                | No                                     |
| Call Syntax     | `obj.method()`                     | `Interface.method()`                   |
| Conflict Case   | Must resolve manually              | Not applicable                         |

---

## Conclusion

Default and static methods help enhance interfaces while maintaining backward compatibility and adding utility logic. However, developers must be cautious about conflict resolution and method inheritance rules.

⸻


# Java Optional Class

`Optional` is a container object introduced in Java 8 to represent a value that may or may not be present. It’s a better alternative to null checks and helps write cleaner and safer code.

---

## 1. Purpose and Benefits

### Why `Optional`?
- Avoids null pointer exceptions.
- Improves code readability and intent.
- Encourages functional-style programming.
- Prevents unnecessary null checks.

### Traditional Way (Without Optional):
```java
String name = person.getName();
if (name != null) {
    System.out.println(name);
}
```

### Using Optional:
```java
Optional<String> name = person.getNameOptional();
name.ifPresent(System.out::println);
```

---

## 2. Creating Optional

```java
Optional<String> empty = Optional.empty();
Optional<String> name = Optional.of("Nikhil");
Optional<String> safeName = Optional.ofNullable(possibleNullValue);
```

---

## 3. Core Methods

### `isPresent()`
Checks if a value is present.
```java
if (name.isPresent()) {
    System.out.println(name.get());
}
```

### `ifPresent(Consumer)`
Executes a lambda if value is present.
```java
name.ifPresent(n -> System.out.println(n));
```

### `orElse(T)`
Returns the value if present, else returns a default.
```java
String result = name.orElse("Default");
```

### `orElseGet(Supplier)`
Lazily returns a default value.
```java
String result = name.orElseGet(() -> "Default");
```

### `orElseThrow(Supplier)`
Throws an exception if value not present.
```java
String result = name.orElseThrow(() -> new IllegalArgumentException("Name not present"));
```

### `map(Function)`
Transforms the value if present.
```java
Optional<Integer> length = name.map(String::length);
```

### `flatMap(Function)`
Used when mapper returns an `Optional`.
```java
Optional<Optional<Integer>> nested = name.map(n -> Optional.of(n.length())); // bad
Optional<Integer> flat = name.flatMap(n -> Optional.of(n.length())); // good
```

---

## 4. Best Practices

Use `Optional` for return types, especially for getters and service calls.  

Do not use `Optional` for:
- Fields in entities or POJOs
- Method parameters
- Collections (use empty list/map instead)

---

## 5. Misuse Cases

- **Avoid chaining multiple isPresent + get**:
  ```java
  if (opt1.isPresent() && opt2.isPresent()) {
      // Bad style
  }
  ```
  Use `flatMap` or `filter` instead.

- **Avoid using Optional in serialization models or JPA entities.**

---

## 6. Interview Questions

### Q1: Why was `Optional` introduced?
**A**: To reduce the number of null pointer exceptions and provide a more functional and expressive approach to handle absence of values.

### Q2: Difference between `orElse` and `orElseGet`?
**A**: `orElse` always evaluates the default value, `orElseGet` evaluates lazily.

### Q3: Can Optional be used for method parameters?
**A**: Not recommended. It complicates the method contract and reduces readability.

### Q4: What is the difference between `map` and `flatMap`?
**A**: `map` transforms the value and wraps it in Optional, `flatMap` unwraps an Optional-returning function.

---

## Summary

| Method          | Purpose                          |
|-----------------|----------------------------------|
| `isPresent()`   | Checks if value is present       |
| `ifPresent()`   | Runs lambda if value is present  |
| `orElse()`      | Returns value or default         |
| `orElseGet()`   | Lazily returns default           |
| `orElseThrow()` | Throws exception if empty        |
| `map()`         | Transforms value (wrapped)       |
| `flatMap()`     | Transforms value (unwrapped)     |

---

## Conclusion

`Optional` encourages better programming practices by explicitly handling cases where values may be absent, making code more robust and intention-revealing.

⸻


# Java Date and Time API (java.time)

The `java.time` package introduced in Java 8 provides a modern, immutable, and thread-safe way of handling dates and times.

---

## 1. LocalDate, LocalTime, LocalDateTime, ZonedDateTime

### LocalDate
Represents a date (year, month, day) without time.

```java
LocalDate today = LocalDate.now();
LocalDate birthDate = LocalDate.of(1990, Month.JULY, 10);
```

### LocalTime
Represents a time (hour, minute, second) without date.

```java
LocalTime now = LocalTime.now();
LocalTime lunchTime = LocalTime.of(13, 30);
```

### LocalDateTime
Combines date and time without timezone.

```java
LocalDateTime now = LocalDateTime.now();
```

### ZonedDateTime
Includes date, time, and timezone.

```java
ZonedDateTime zonedNow = ZonedDateTime.now();
```

---

## 2. Immutability and Thread-Safety

All classes in `java.time` are immutable and thread-safe. Once created, their values cannot be changed.

```java
LocalDate date = LocalDate.of(2024, 7, 20);
LocalDate newDate = date.plusDays(10); // returns a new instance
```

---

## 3. Period and Duration

### Period
Represents date-based amount of time (years, months, days).

```java
Period period = Period.between(LocalDate.of(2023, 1, 1), LocalDate.of(2025, 1, 1));
System.out.println(period.getYears()); // 2
```

### Duration
Represents time-based amount of time (hours, minutes, seconds).

```java
Duration duration = Duration.between(LocalTime.of(10, 0), LocalTime.of(12, 30));
System.out.println(duration.toMinutes()); // 150
```

---

## 4. Formatting and Parsing using DateTimeFormatter

### Formatting
```java
LocalDate date = LocalDate.of(2025, 7, 20);
String formatted = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
System.out.println(formatted); // 20-07-2025
```

### Parsing
```java
String dateStr = "20-07-2025";
LocalDate parsedDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
```

---

## Interview Questions

### Q1: Why use `java.time` over old Date/Calendar classes?
**A**: It's immutable (Date and Calendar are mutable, so they can be changed unexpectedly → not safe for concurrent code.), thread-safe, supports ISO and custom formats, and provides a cleaner API (old one had confusing design like Months are 0-based, Years are offset from 1900 in some constructors, Date mixes date and time, Calendar is overly verbose, 	Formatting and parsing are in SimpleDateFormat, which is mutable and not thread-safe, 	No clear distinction between concepts: date-only, time-only, date-time with/without timezone.)

### Q2: What is the difference between `Period` and `Duration`?
**A**: `Period` is for date-based (days, months, years), `Duration` is for time-based (seconds, minutes, hours).

### Q3: How is `ZonedDateTime` different from `LocalDateTime`?
**A**: `ZonedDateTime` includes timezone information, `LocalDateTime` does not.

### Q4: Is `java.time` thread-safe?
**A**: Yes. All classes in `java.time` are immutable and thread-safe.

### Q5: How do you parse a date from a custom format?
**A**: Use `DateTimeFormatter.ofPattern()` with `LocalDate.parse()`.

---

## Summary Table

| Class           | Description                       |
|------------------|-----------------------------------|
| `LocalDate`      | Date without time                 |
| `LocalTime`      | Time without date                 |
| `LocalDateTime`  | Date + time, no timezone          |
| `ZonedDateTime`  | Date + time + timezone            |
| `Period`         | Difference between dates          |
| `Duration`       | Difference between times          |
| `DateTimeFormatter` | Formatting and parsing dates   |

---

## Conclusion

The `java.time` package offers a powerful, consistent, and thread-safe approach to handle all date and time use cases in Java. It's highly recommended to use this over legacy `Date` and `Calendar` APIs.

⸻


# Java Parallel Streams

Parallel Streams in Java allow parallel processing of data using the Stream API. They internally use the **ForkJoinPool** to divide tasks and execute them concurrently.

---

## 1. How They Work Internally

Parallel streams use the **Fork/Join framework**, introduced in Java 7. When a collection is processed in parallel:

- The stream is split into multiple sub-streams.
- Each sub-stream is processed independently by a different thread.
- The results are combined in the end.

Internally, the default `ForkJoinPool.commonPool()` is used for executing tasks.

### Example:
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
int sum = numbers.parallelStream().mapToInt(Integer::intValue).sum();
System.out.println(sum); // Output: 21
```

---

## 2. When to Use Parallel Streams

Use parallel streams when:

- You have large datasets.
- Your tasks are **CPU-intensive**.
- Operations are **stateless and independent**.
- No dependencies on **shared mutable state**.

Avoid using them for small datasets — parallelization overhead may outweigh the benefits.

---

## 3. Performance Considerations

- **Thread management** is handled by ForkJoinPool.
- Uses available **CPU cores**: ideal for multi-core systems.
- Avoid parallel streams in performance-critical paths where predictability matters.

### Benchmark Example:
```java
List<Integer> list = IntStream.rangeClosed(1, 1_000_000).boxed().collect(Collectors.toList());

// Sequential
long start = System.currentTimeMillis();
list.stream().reduce(0, Integer::sum);
System.out.println("Sequential: " + (System.currentTimeMillis() - start));

// Parallel
start = System.currentTimeMillis();
list.parallelStream().reduce(0, Integer::sum);
System.out.println("Parallel: " + (System.currentTimeMillis() - start));
```

---

## 4. Thread-Safety Concerns

Parallel streams can be **unsafe** if:

- You use shared mutable state.
- You rely on the **order of execution**.
- You access non-thread-safe APIs inside stream operations.

### Unsafe Example:
```java
List<Integer> list = Arrays.asList(1,2,3,4);
List<Integer> result = new ArrayList<>();

list.parallelStream().forEach(result::add); // Risky: ArrayList is not thread-safe
```

Use **Concurrent collections** or `Collectors.toList()`:
```java
List<Integer> result = list.parallelStream().collect(Collectors.toList());
```

---

## Interview Questions

### Q1: How does Java internally implement parallel streams?
**A**: Via the ForkJoinPool and its work-stealing algorithm. Each task is broken into smaller ones and processed in parallel threads.

### Q2: When should you avoid using parallel streams?
**A**: For small datasets, IO-bound operations, or when thread-safety/order of execution is critical.

### Q3: How do you ensure thread safety in parallel streams?
**A**: Avoid shared mutable state; use thread-safe collections or proper collectors like `Collectors.toList()`.

---

## Summary

| Feature              | Parallel Stream                      |
|----------------------|--------------------------------------|
| Threading model       | ForkJoinPool.commonPool()           |
| Best suited for       | CPU-bound, stateless operations     |
| Risk factors          | Shared state, non-thread-safe collections |
| Performance gain      | Only on large, independent datasets |

---

## Conclusion

Parallel Streams can significantly enhance performance but must be used judiciously. Understand the nature of your data and operations before opting for parallelism.

⸻

# Java Collectors Utility Class (java.util.stream.Collectors)

The `Collectors` utility class provides **static methods** for collecting stream elements into various forms like lists, maps, sets, etc. It is a key part of the **Stream API** and is used with the `collect()` terminal operation.

---

## 1. toList(), toSet(), toMap()

### `toList()`
Collect elements into a List.
```java
List<String> names = Stream.of("John", "Jane", "Jake")
    .collect(Collectors.toList());
```

### `toSet()`
Collect elements into a Set (removes duplicates).
```java
Set<String> names = Stream.of("John", "Jane", "John")
    .collect(Collectors.toSet()); // Only one "John"
```

### `toMap()`
Collect elements into a Map.
```java
Map<Integer, String> map = Stream.of("apple", "banana")
    .collect(Collectors.toMap(String::length, Function.identity()));
// Output: {5=apple, 6=banana}
```

🔸 For duplicate keys, you must provide a merge function:
```java
Map<Integer, String> map = Stream.of("apple", "apricot")
    .collect(Collectors.toMap(
        String::length,
        Function.identity(),
        (s1, s2) -> s1 + "," + s2
    ));
```

---

## 2. groupingBy()

Groups elements by a classifier function into a `Map<K, List<T>>`.
```java
Map<Integer, List<String>> grouped = Stream.of("a", "bb", "ccc", "dd")
    .collect(Collectors.groupingBy(String::length));

// Output: {1=[a], 2=[bb, dd], 3=[ccc]}
```

### Nested grouping:
```java
Map<Integer, Map<Character, List<String>>> nested = Stream.of("a", "ab", "cd", "abc")
    .collect(Collectors.groupingBy(
        String::length,
        Collectors.groupingBy(s -> s.charAt(0))
    ));
```

---

## 3. partitioningBy()

Partitions elements based on a **predicate** into a `Map<Boolean, List<T>>`.
```java
Map<Boolean, List<Integer>> partitioned = IntStream.rangeClosed(1, 5)
    .boxed()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

// Output: {false=[1, 3, 5], true=[2, 4]}
```

---

## 4. joining()

Concatenates string representations with optional delimiter, prefix, and suffix.
```java
String result = Stream.of("A", "B", "C")
    .collect(Collectors.joining(", ", "[", "]"));
// Output: [A, B, C]
```

---

## 5. summarizingInt(), summarizingDouble(), summarizingLong()

Collects statistics (count, sum, min, average, max).
```java
IntSummaryStatistics stats = Stream.of(1, 2, 3, 4, 5)
    .collect(Collectors.summarizingInt(Integer::intValue));

System.out.println(stats.getAverage()); // 3.0
System.out.println(stats.getMax());     // 5
```

---

## 6. mapping()

Maps elements before collecting them.
```java
Map<Integer, List<Character>> result = Stream.of("cat", "cow", "car")
    .collect(Collectors.groupingBy(
        String::length,
        Collectors.mapping(s -> s.charAt(0), Collectors.toList())
    ));
// Output: {3=[c, c, c]}
```

---

## Interview Questions

### Q1: Difference between groupingBy and partitioningBy?
**A**: `groupingBy` groups by a key, can have multiple keys. `partitioningBy` is a special case for boolean values (true/false).

### Q2: When do you need a merge function in toMap?
**A**: When multiple elements generate the same key, otherwise it throws `IllegalStateException`.

### Q3: How is mapping used with groupingBy?
**A**: It transforms the grouped values before collecting them (e.g., extracting fields, mapping to another type).

---

## Summary Table

| Method             | Description                                |
|--------------------|--------------------------------------------|
| toList(), toSet()  | Collect to basic collections               |
| toMap()            | Collect to Map, with optional merge        |
| groupingBy()       | Group by classifier function               |
| partitioningBy()   | Partition based on a predicate             |
| joining()          | Concatenate strings                        |
| summarizingInt()   | Get statistics from int stream             |
| mapping()          | Pre-process elements before collection     |

---

## Conclusion

`Collectors` enable powerful, readable, and declarative data processing in Java. Mastering them is essential for real-world Java development, especially in data-heavy applications.

⸻

# CompletableFuture in Java

`CompletableFuture` enables **asynchronous**, **non-blocking**, and **composable** programming. It overcomes the limitations of `Future`.

---

## Why CompletableFuture?

| Problem with `Future`                  | CompletableFuture Solution                     |
|----------------------------------------|------------------------------------------------|
| Can't manually complete a future       | Can explicitly `complete()` or `completeExceptionally()` |
| No chaining of tasks                   | Supports `thenApply`, `thenCompose`, etc.      |
| Blocking `get()` to retrieve result    | Allows non-blocking callbacks (`thenAccept`)   |
| No exception handling mechanism        | Offers `exceptionally`, `handle`, `whenComplete` |
| No combining multiple futures          | Supports `thenCombine`, `allOf`, `anyOf`       |
| Limited thread management              | Can use custom `Executor`                      |

---

## Creating CompletableFuture

### Run async task without return:
```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    // background task
});
```

### Run async task with return:
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "Hello, World!";
});
```

---

## Chaining and Combining Futures

### `thenApply()` – transform result
```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World");
```

### `thenAccept()` – consume result
```java
CompletableFuture
    .supplyAsync(() -> 42)
    .thenAccept(result -> System.out.println("Result: " + result));
```

### `thenCompose()` – flatten nested futures
```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "John")
    .thenCompose(name -> CompletableFuture.supplyAsync(() -> "Hi " + name));
```

### `thenCombine()` – combine two independent futures
```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<String> result = f1.thenCombine(f2, (a, b) -> a + " " + b);
```

---

## Exception Handling

### `exceptionally()` – fallback on failure
```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> { throw new RuntimeException("Oops!"); })
    .exceptionally(ex -> "Default Value");
```

### `handle()` – result + exception access
```java
CompletableFuture<String> handled = CompletableFuture
    .supplyAsync(() -> { throw new RuntimeException("fail"); })
    .handle((res, ex) -> ex == null ? res : "Recovered");
```

### `whenComplete()` – observe success/failure
```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Done")
    .whenComplete((result, ex) -> {
        if (ex == null) System.out.println("Result: " + result);
        else System.out.println("Error: " + ex.getMessage());
    });
```

---

## Threading & Executor Customization

### Use default ForkJoinPool (common pool):
```java
CompletableFuture.runAsync(() -> { /* task */ });
```

### Use custom `ExecutorService`:
```java
ExecutorService executor = Executors.newFixedThreadPool(2);
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    System.out.println("Running in custom pool");
}, executor);
```

---

## Combining Multiple Futures

### `allOf()` – wait for all to complete
```java
CompletableFuture<Void> combined = CompletableFuture.allOf(future1, future2);
```

### `anyOf()` – proceed with first completed
```java
CompletableFuture<Object> first = CompletableFuture.anyOf(f1, f2, f3);
```

---

## Best Practices

| Tip                                          | Why it matters                                     |
|---------------------------------------------|----------------------------------------------------|
| Always handle exceptions                    | Avoid app crashes from uncaught async errors       |
| Use custom executor for CPU-bound tasks     | Avoid ForkJoinPool starvation                      |
| Avoid blocking with `get()` in async chains | Prefer `thenXxx()` for non-blocking design         |
| Use `thenCompose()` for dependent tasks     | Prevents nesting and increases readability         |

---

## 🧠 Interview Follow-ups

### Q: Difference between `thenApply()` and `thenCompose()`?
- `thenApply()` wraps result in a `CompletableFuture<CompletableFuture<T>>`
- `thenCompose()` flattens nested futures (used for dependent tasks)

### Q: How to handle exceptions globally?
- Use `handle()` or `exceptionally()` in the final stage of the pipeline

### Q: How is `CompletableFuture` better than `FutureTask`?
- More fluent API, non-blocking chaining, better error handling, async combining


## Q. Difference between this keyword and super keyword in java and how does it differ wrt lambda function?
- `this` refers to the current instance of the class, while `super` refers to the parent class. In lambda expressions, `this` refers to the enclosing instance where the lambda is defined, not the lambda itself. `super` cannot be used in lambdas as they do not have a superclass.
- example - 


# Anonymous Inner Classes in Java

Perfect, let’s zoom in on Anonymous Inner Classes (AICs) and go deep. 🚀

⸻

  1. What is an Anonymous Inner Class?

An Anonymous Inner Class is:
•	An inner class without a name, defined and instantiated at the same time.
•	Usually used when you need to provide a short-term implementation of an interface or abstract class.
•	It cannot have a constructor (since it has no name), but it can have an instance initializer block.

In other words:
👉 Instead of creating a separate .java file or even a named inner class, you just “inline” the definition at the point of use.

⸻

  2. General Syntax

Type ref = new Type() {
// body of the anonymous inner class
// override methods of Type
};

Here, Type can be:
•	An interface (must implement all abstract methods).
•	An abstract class (must implement its abstract methods).
•	A concrete class (can override some methods).

⸻

  3. Examples

Example 1: Implementing an Interface

```java
Runnable r = new Runnable() {
@Override
public void run() {
System.out.println("Running from Anonymous Inner Class!");
}
};
r.run();
```

	•	No need to separately create a RunnableImpl class.
	•	The compiler generates something like Outer$1.class behind the scenes.

⸻

Example 2: Extending an Abstract Class
```java
abstract class Animal {
abstract void makeSound();
}

public class Demo {
public static void main(String[] args) {
Animal dog = new Animal() {
@Override
void makeSound() {
System.out.println("Woof Woof!");
}
};
dog.makeSound();
}
}
```
Here, Animal is abstract → we provide the implementation inline.

⸻

Example 3: Extending a Concrete Class
```java
class Hello {
void greet() {
System.out.println("Hello");
}
}

public class Demo {
public static void main(String[] args) {
Hello h = new Hello() {
@Override
void greet() {
System.out.println("Hi from Anonymous Inner Class");
}
};
h.greet(); // prints: Hi from Anonymous Inner Class
}
}
```
You can override methods of a normal class too.

⸻

Example 4: With Arguments
```java
Button button = new Button("Click Me");
button.addActionListener(new ActionListener() {
@Override
public void actionPerformed(ActionEvent e) {
System.out.println("Button clicked!");
}
});
```
Classic Swing/GUI use case → you don’t want to write a whole new class for a single button listener.

⸻

  4. Characteristics of Anonymous Inner Classes
1.	No Name
•	You can’t reuse them since they don’t have identifiers.
•	Generated class names look like Outer$1.class, Outer$2.class, etc.
2.	Single Use
•	They’re best for one-off implementations.
3.	Scope of this
•	Inside an anonymous inner class, this refers to the anonymous inner class object, not the enclosing class.
```java
Runnable r = new Runnable() {
public void run() {
System.out.println(this.getClass().getName());
}
};
r.run();
// Prints something like Demo$1
```

	4.	Can Capture Variables
	•	Like lambdas, anonymous inner classes can access effectively final variables from the enclosing scope.
```java
int num = 10;
Runnable r = new Runnable() {
public void run() {
System.out.println(num); // ok if num is effectively final
}
};
```

	5.	No Constructors
	•	Since they have no name, they cannot define a constructor.
	•	But they can use instance initializers ({} block) for setup.
```java
Hello h = new Hello() {
{ System.out.println("Instance initializer block runs!"); }
};
```


⸻

  5. Behind the Scenes

When you write:
```java
Runnable r = new Runnable() {
public void run() {
System.out.println("Anonymous Inner Class");
}
};
```
The compiler generates something like:
```java
class Demo$1 implements Runnable {
public void run() {
System.out.println("Anonymous Inner Class");
}
}
```
So even though you didn’t give it a name, the compiler does.

⸻

  6. Limitations
•	Verbose (compared to lambdas).
•	Creates a new .class file at compile time.
•	Harder to read if overused.
•	Cannot have multiple constructors.
•	Cannot be reused elsewhere.

⸻

  7. When to Use Anonymous Inner Classes
•	When you only need an implementation once.
•	For GUI event handling (ActionListener, MouseListener, etc.).
•	To quickly override a method for a small tweak.
•	When you’re on Java < 8 (since lambdas don’t exist yet).

⸻

⚡ So, an Anonymous Inner Class is like a quick disposable “on-the-spot worker” that you hire for a job and never keep on payroll again.

⸻

### Inner classes

Got it 👍 let’s build this step by step, starting from zero.

⸻

  1. Inner Classes in Java

An inner class is simply a class defined inside another class.

Why do we need them?
•	To logically group classes that are only used inside another class.
•	To get direct access to the outer class’s members (even private ones).
•	To make the code more readable when a class has only one usage.

Types of Inner Classes
1.	Non-static Inner Class (Member inner class)

class Outer {
private String msg = "Hello";

    class Inner {
        void display() {
            System.out.println(msg); // can access Outer’s private members
        }
    }
}

public class Demo {
public static void main(String[] args) {
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner(); // needs an Outer object
inner.display(); // prints Hello
}
}


	2.	Static Nested Class

class Outer {
static class Inner {
void display() {
System.out.println("Static nested class");
}
}
}

public class Demo {
public static void main(String[] args) {
Outer.Inner inner = new Outer.Inner(); // no Outer object needed
inner.display();
}
}


	3.	Local Inner Class (defined inside a method)

class Outer {
void show() {
class LocalInner {
void display() {
System.out.println("Inside method-local inner class");
}
}
LocalInner inner = new LocalInner();
inner.display();
}
}


	4.	Anonymous Inner Class (no name, defined on the spot, usually for interfaces/abstract classes)

Runnable r = new Runnable() {
public void run() {
System.out.println("Running in anonymous inner class");
}
};
r.run();


### Anonymous Inner Classes in Java vs Lmabda this keyword

Perfect, let’s expand this subtle but very important distinction between lambdas and anonymous inner classes 🧩.

⸻

1. Lambdas: this = enclosing class

```java

class Test {
Runnable r1 = () -> System.out.println(this.getClass().getName());
}
```

	•	A lambda does not create a new object scope.
	•	It’s like a lightweight “inline method”.
	•	So when you use this, it refers to the instance of the enclosing class (Test).

Example:
```java
public class Test {
Runnable r1 = () -> System.out.println(this.getClass().getName());

    public static void main(String[] args) {
        new Test().r1.run();
    }
}
```
Output:
```
Test
```
👉 Because this points to the Test object, not the lambda.

⸻

2. Anonymous inner classes: this = the inner class itself
```java
class Test {
Runnable r2 = new Runnable() {
public void run() {
System.out.println(this.getClass().getName());
}
};
}
```
	•	An anonymous inner class creates a new class at runtime.
	•	Inside it, this refers to the anonymous object, not the outer instance.

Example:
```java
public class Test {
Runnable r2 = new Runnable() {
public void run() {
System.out.println(this.getClass().getName());
}
};

    public static void main(String[] args) {
        new Test().r2.run();
    }
}
```
Output (compiler-dependent, but usually something like):
```
Test$1
```

👉 That Test$1 is the compiler-generated class for the anonymous inner class.

⸻

3. Why is this distinction important?
   •	With lambdas:
   •	this always refers to the enclosing object.
   •	Cleaner access to fields/methods of the enclosing class.
   •	With anonymous inner classes:
   •	this refers to the anonymous instance itself.
   •	If you need to refer to the enclosing class, you must use EnclosingClassName.this.

Example:
```java
class Outer {
String name = "Outer";

    void test() {
        Runnable r1 = () -> System.out.println(this.name);  // Outer.this
        Runnable r2 = new Runnable() {
            public void run() {
                System.out.println(this.getClass().getName()); // anonymous class
                System.out.println(Outer.this.name); // explicit reference to Outer
            }
        };

        r1.run();
        r2.run();
    }
}
```

⸻

🔑 Key Takeaways
1.	Lambda this → the enclosing class instance (Outer).
2.	Anonymous inner class this → the anonymous inner class instance (Outer$1).
3.	To access enclosing class explicitly inside an anonymous inner class, you must qualify it with Outer.this.
4.	This difference makes lambdas more predictable and lightweight — they behave like methods, not objects.

⸻

Great question ⚡ — this digs into API design philosophy in Java.

⸻

### Why getters don’t return Optional<T>

Normally, a getter looks like this:
```java

public String getName() {
return name;
}
```
If name is absent, it can return null.
So why not write:
```java
public Optional<String> getName() {
return Optional.ofNullable(name);
}
```
Seems safe, right? But there are strong reasons why JavaBeans-style getters don’t use Optional:

⸻

1. Optional was never meant for fields
   •	The JDK docs say: “Optional is primarily intended for return types where there is a clear need to represent ‘no result’.”
   •	It was not designed to be a property type or used as a field in classes.

So this is discouraged:
```java
class Person {
private Optional<String> name; // bad practice
}
```

⸻

2. Serialization and Frameworks
   •	JavaBeans spec (which libraries like Hibernate, Jackson, Spring rely on) expects getters/setters with plain types.
   •	If getters return Optional, frameworks might fail to serialize/deserialize automatically.
   •	Example: JSON libraries would produce {"name":{}} instead of {"name":null}.

⸻

3. Performance & Overhead
   •	Wrapping every field in Optional creates extra objects → heap churn.
   •	Getters are often in performance-sensitive code (e.g., POJOs in collections).

⸻

4. API Usability
   •	For fields, callers already know if a value might be null.
   •	Adding Optional everywhere makes code noisy:
```java
// with Optional getter
String name = person.getName().orElse("Unknown");

// with normal getter
String name = person.getName() != null ? person.getName() : "Unknown";
```
→ In practice, the second form is simpler in bean-like objects.

⸻

5. Optional is for APIs, not DTOs
   •	You often see Optional in method return types, where absence is part of the protocol:
```java
Optional<User> findUserById(String id);
```

•	But DTOs/entities/POJOs stick with plain getters.

⸻

  In summary
•	Getters don’t return Optional because:
•	It’s against the intended use of Optional.
•	It complicates frameworks/serialization.
•	It adds overhead and verbosity.
•	Instead:
•	Use Optional in service methods or APIs where absence is meaningful.
•	Use plain getters in data models (with null if absent).

⸻

✨ Rule of thumb:
•	Entity/POJO getters → never return Optional.
•	Service/repository/business methods → may return Optional.

⸻

Would you like me to show you an example where Optional is good (service API) vs bad (getter in entity) side by side?




