## Lambda expressions

1. What is a lambda expression in Java 8, and how is it related to functional interfaces?
    - Follow‑up: How does a lambda differ from a regular method in terms of where it can be declared?


2. Write the general **syntax** of a lambda expression in Java and explain each part.
    - Follow‑up: When can you omit parameter types, parentheses, or curly braces in a lambda?


3. How would you implement a `Runnable` using a lambda instead of an anonymous class?
- Follow‑up: In which situations would you still prefer an anonymous class over a lambda?
- You still use an anonymous class instead of a lambda in a few specific cases.
  - When you need to implement an interface or class with **more than one abstract method** (i.e., not a functional interface). In that case a lambda cannot be used at all. 
  - When you need a **named or distinct `this` context** that refers to the anonymous object itself (for example, to differentiate between the enclosing class and the inner implementation). Lambdas’ `this` refers to the enclosing instance instead. 
  - When you want to **override multiple methods** from a superclass or abstract class in one inline definition (e.g., subclassing `Thread` or an abstract class with several methods). 
  - When you need to define **fields or initialization logic** inside the inline type (instance initializers, extra state, or helper methods) which lambdas cannot declare. 


4. Show how to write a lambda for a `BinaryOperator<Integer>` that adds two numbers.
    - Follow‑up: How would you modify that lambda to include logging before returning the sum?


5. Explain the difference between a single‑expression lambda and a block lambda.
    - Follow‑up: When is an explicit `return` statement required inside a lambda body?


`6. What are the key **benefits** of lambda expressions compared to anonymous inner classes?`
- Follow‑up: Explain how `invokedynamic` helps lambda implementation at the JVM level conceptually.
- Internally, a lambda is compiled as a normal method plus an `invokedynamic` call that asks the JVM to create a functional-interface instance at runtime, usually without generating a separate class file. 
- No explicit synthetic inner class like `Outer$1.class` is generated; instead, the behavior is described and linked dynamically via `invokedynamic`. 
- Because lambdas are “just” method handles plus metadata, they capture scope differently (`this` and effectively-final variables) and are more amenable to JIT optimizations. 


7. In the context of collections, how would you use a lambda with `forEach` on a `List<String>`?
    - Follow‑up: How would you pass a method reference instead of an explicit lambda in the same example?


`8. Demonstrate using `removeIf` with a lambda `Predicate<Integer>` to remove even numbers from a list.`
- Follow‑up: What happens if the predicate itself throws a checked exception?
- If a `Predicate` in your lambda throws a checked exception, the code will not compile unless that exception is declared in the functional interface’s abstract method signature.
- Standard `Predicate<T>`’s `test(T t)` does not declare any checked exceptions, so you cannot write `n -> { throw new IOException(); }` directly with it; the compiler will force you to either wrap the checked exception in an unchecked one or handle it inside the lambda with try–catch. 
- To propagate a checked exception from such a lambda, you must define and use a custom functional interface whose single abstract method declares that checked exception in its `throws` clause. 


9. Show how to use a lambda to provide a `Comparator<String>` for sorting a list.
- `Follow‑up: How would you change the sorting to be case‑insensitive using a lambda?` - words.sort((s1, s2) -> s1.compareToIgnoreCase(s2));


`10. How do you iterate a `Map<String, Integer>` using `forEach` and a lambda?`
- Follow‑up: Could you replace your lambda with a BiConsumer method reference in that example?
- Yes. In that `Map.forEach` example you can replace the lambda with a `BiConsumer` method reference.
- For example, instead of:

```java
map.forEach((key, value) -> System.out.println(key + "=" + value));
```

you can use a `BiConsumer<Map.Entry<K,V>, PrintStream>`-style helper or, more simply, a method reference to a custom `BiConsumer` method:

```java
BiConsumer<String, Integer> printer = MyClass::printEntry;

map.forEach(printer);
```

where

```java
static void printEntry(String key, Integer value) {
    System.out.println(key + "=" + value);
}
```


11. Explain why lambdas improve **readability** and **boilerplate** reduction in multi‑threaded code.
    - Follow‑up: Show how to submit a task to an `ExecutorService` using a lambda `Callable`.


12. Compare lambdas and anonymous classes in terms of:  
    a) Syntax, b) Class file generation, c) Performance, d) `this` reference.
- Follow‑up: In a lambda inside an inner class, what does `this` refer to?
- No explicit synthetic inner class like `Outer$1.class` is generated; instead, the behavior is described and linked dynamically via `invokedynamic`.
- Because lambdas are “just” method handles plus metadata, they capture scope differently (`this` and effectively-final variables) and are more amenable to JIT optimizations.


13. Can a lambda expression throw checked exceptions? Under what condition?
- Follow‑up: How would you design a custom functional interface to allow throwing `IOException`?
- Custom functional interface wholse abstract method throws exception -  void accept(T t) throws IOException;
    

`14. Explain what is meant by **effectively final** in the context of lambdas and local variables.`
- Follow‑up: Give an example that fails to compile because the captured variable is not effectively final.
- An **effectively final** local variable is a variable whose value never changes after it is assigned, even if it is not explicitly declared `final`; such variables are the only local variables you can capture in a lambda. 
- **This restriction exists because lambdas capture a **copy** of the local variable’s value, not a mutable slot, so allowing mutation would be unsafe and confusing.**
- What “effectively final” means
  - A local variable is effectively final if you assign it once and never reassign it (no `=` or `++/--` that change its value later in the method). 
  - The compiler treats it as if it were declared `final`; if you try to modify it after using it in a lambda, you get a compile-time error.
  Example:
```java
String message = "Hello";
// message = "Changed"; // Uncommenting this breaks compilation

Runnable r = () -> System.out.println(message); // OK: message is effectively final
```


15. Are lambda expressions serializable? Under what circumstances?
- Follow‑up: Why is serializing lambdas generally discouraged?
- Only if the target functional interface is `Serializable`.  
- Avoid serialization of lambdas for portability and maintainability.

16.

17. Why can’t lambda expressions be used to implement an interface with multiple abstract methods?
    - Follow‑up: How could you refactor such an interface to make it compatible with lambdas?


***

## Functional interfaces (java.util.function)

18. What is a **functional interface** in Java, and why is it important for lambda expressions?
- Follow‑up: Can a functional interface have more than one default method? More than one static method?

19. What is the purpose of the `@FunctionalInterface` annotation? Is it mandatory?
- Follow‑up: What compiler error do you get if you add a second abstract method to a `@FunctionalInterface`?

20. List the core built‑in functional interfaces: `Predicate`, `Function`, `Consumer`, `Supplier`. Describe each.
- Follow‑up: For each of these, name the single abstract method and its signature.

21. Show a code snippet using `Predicate<String>` to test whether a string length is greater than 5.
- Follow‑up: How would you compose two predicates using `and()` and `or()`?

22. Show how to use `Function<String, Integer>` to get the length of a string.
- Follow‑up: Demonstrate `andThen` and `compose` with `Function`.

23. Demonstrate a `Consumer<String>` that prints a formatted name.
- Follow‑up: How can you chain two consumers using `andThen`?

24. Demonstrate a `Supplier<Double>` that generates a random value.
- Follow‑up: Why is `Supplier` typically used in lazy evaluation scenarios?

25. Explain `UnaryOperator<T>` and `BinaryOperator<T>` and give an example of each.
- Follow‑up: When would you prefer `BinaryOperator<T>` over `BiFunction<T, T, T>`?
- `BinaryOperator<T>` is a specialization of `BiFunction<T,T,T>` for this exact pattern, so it makes the intent clearer and works nicely with APIs like `Stream.reduce(BinaryOperator<T>)` and factory methods such as `BinaryOperator.minBy/maxBy`

26. What are `BiPredicate`, `BiFunction`, and `BiConsumer`? Provide one use case for each.
- Follow‑up: How would you use a `BiFunction<Integer, Integer, String>` to format a sum message?

27. 

28. Can a functional interface be generic? Show an example `Processor<T>`.
- Follow‑up: How would you use this generic interface with `Integer` and with `String`?

29. Can a functional interface declare default and static methods? Explain with an example.
- Follow‑up: Do those default methods participate in the “functional” abstract method count?

30. What happens if a functional interface has **zero** abstract methods or **more than one** abstract method?
- Follow‑up: Does the compiler treat an interface without annotation but with one abstract method as functional?

***

## Streams API

31. What is the Java Streams API, and how does it relate to functional programming in Java?


32. Describe the three main parts of a stream pipeline: source, intermediate operations, and terminal operation.
    - Follow‑up: Why is it important that intermediate operations are lazy?


33. Show an example where you:  
    a) Create a stream from a `List<String>`,  
    b) Filter names starting with “A”,  
    c) Map to uppercase,  
    d) Print them.
    - Follow‑up: How would you collect the result into a `List<String>` instead of printing?

34. List some common **intermediate operations** and explain each: `map`, `filter`, `sorted`, `distinct`, `limit`, `skip`.
    - Follow‑up: Which of these operations are stateful and which are stateless?

35. Show how to use `map` to square all integers in a list and collect the results.
    - Follow‑up: How would you implement the same logic using a traditional `for` loop and compare readability?

36. List common **terminal operations**: `collect`, `count`, `reduce`, `min`, `max`, `forEach`.
    - Follow‑up: What is the return type for each of those terminal operations?

37. Demonstrate a `reduce` operation that computes the sum of a list of integers.
    - Follow‑up: Show a `reduce` that computes the product with an identity value and explain the identity parameter.

38. Explain what it means that streams are **lazy**.
    - Follow‑up: Use an example with `filter` and `findFirst` to illustrate short‑circuiting.

39. What is **short‑circuiting** in streams? Name some short‑circuiting terminal operations.
    - Follow‑up: How can short‑circuiting improve performance in large streams?

40. What does `Collectors.toList()` do in a stream pipeline?
    - Follow‑up: What is the difference between `toList()` and `toSet()` in terms of resulting collection semantics?

41. Explain `Collectors.joining()` with an example.
- Follow‑up: How do you specify a delimiter, prefix, and suffix in `joining()`?
- Collectors.joining() is a collector that concatenates a stream of CharSequence elements into a single String, optionally with a delimiter, prefix, and suffix.
```java
List<String> names = List.of("Nikhil", "Amit", "Ravi");

String result = names.stream()
.collect(Collectors.joining());

System.out.println(result); // "NikhilAmitRavi"
```


42. How would you use `Collectors.summarizingInt()` or `averagingInt()` on a collection of numbers?
    - Follow‑up: What information does `IntSummaryStatistics` provide?

43. Explain `groupingBy` in collectors with an example that groups strings by their length.
    - Follow‑up: How do you specify a downstream collector like `mapping` or `counting` with `groupingBy`?

44. Explain `partitioningBy` and how it differs from `groupingBy`.
    - Follow‑up: Provide an example partitioning integers into even and odd.

45. What is the purpose of `Collectors.mapping`? Show an example using `groupingBy` and `mapping` together.
    - Follow‑up: In your example, what would be the type of the resulting map?

46. What are primitive streams (`IntStream`, `LongStream`, `DoubleStream`), and why are they useful?
    - Follow‑up: Show how to compute the sum of an `IntStream` range from 1 to 4.

47. Explain the difference between `map` and `flatMap` in the context of streams.
    - Follow‑up: Show an example where using `map` produces a `Stream<List<T>>` and `flatMap` produces `Stream<T>`.

48. How does `findFirst()` differ from `findAny()`?
    - Follow‑up: In parallel streams, why might `findAny()` be preferred in some cases?

49. Discuss the difference between **intermediate** and **terminal** operations in terms of when they execute.
    - Follow‑up: How can this affect debugging when you add `println` statements inside lambdas?

50. What happens if you reuse a stream after a terminal operation?
    - Follow‑up: How can you design your code to avoid this problem?

***

## Method and constructor references

51. What is a **method reference** in Java 8, and when would you use it instead of a lambda?
    - Follow‑up: Give an example where converting a lambda to a method reference improves clarity.

52. Explain **static method references** and give the syntax.
    - Follow‑up: Convert `x -> Integer.parseInt(x)` into a method reference.

53. 

54. 

55. What is a **constructor reference** and what is its syntax?
    - Follow‑up: Provide an example using `Supplier<List<String>>` with `ArrayList::new`.

56. How can you use constructor references with a custom `Person(String name)` class?
    - Follow‑up: Show a `Function<String, Person>` using `Person::new`.

57. Explain the difference between `Class::instanceMethod` and `instance::instanceMethod`.
- Follow‑up: In the case of `list.sort(String::compareToIgnoreCase)`, which object does the method actually get invoked on?
- `Class::instanceMethod` refers to an instance method where the instance is supplied as the first argument at call time, while `instance::instanceMethod` binds the receiver up front to a specific object. 
**Difference in meaning**
- `instance::instanceMethod`: The target is a **particular object**; no receiver parameter is passed in the lambda, only the method’s normal parameters. Example: `str::length` becomes `() -> str.length()`. 
- `Class::instanceMethod`: The target is an instance method of that **type**, and the functional interface’s first parameter becomes the receiver; e.g., `String::compareToIgnoreCase` is equivalent to `(a, b) -> a.compareToIgnoreCase(b)`. 
**list.sort(String::compareToIgnoreCase)**
- `List.sort(Comparator)` expects a `Comparator<String>`, whose method is `int compare(String a, String b)`. 
- With `String::compareToIgnoreCase`, the comparator is `(a, b) -> a.compareToIgnoreCase(b)`, so for each comparison the method is **invoked on the first element** (`a`), passing the second element (`b`) as the argument. 

58. How do method and constructor references interact with functional interfaces?
- Follow‑up: What determines whether a specific method reference is compatible with a given functional interface?
- Method and constructor references are just shorthand for lambdas that implement a functional interface whose single abstract method’s signature matches the referenced method or constructor. 
- How they interact
  - A method reference like `String::toUpperCase` is converted to a lambda compatible with some functional interface (for example, `Function<String,String>` becomes `s -> s.toUpperCase()`). 
  - A constructor reference like `ArrayList::new` is converted to a lambda that calls that constructor (for example, `Supplier<List<String>>` becomes `() -> new ArrayList<>()`).
- What defines compatibility
  - The **SAM** (single abstract method) of the functional interface and the referenced method/constructor must match in:
      - Number and types of parameters (including how the receiver is mapped for `Class::instanceMethod`).
      - Return type (must be the same or a compatible subtype). 
- For `Class::instanceMethod`, the SAM’s first parameter becomes the receiver, e.g. `Comparator<String>` with `String::compareToIgnoreCase` maps to `(a, b) -> a.compareToIgnoreCase(b)`. 



***

## Default and static methods in interfaces

59. Why did Java 8 introduce **default methods** in interfaces?
- Follow‑up: How do default methods help with backward compatibility?
- Java 8 introduced default methods so interfaces could evolve (gain new behavior) without forcing every existing implementing class to change, solving the “interface evolution” problem.
- Why default methods were added
  - Before Java 8, adding a new abstract method to a published interface broke all existing implementations because they had to implement the new method.
    - Default methods let you add a method with a body in the interface itself, so existing classes automatically inherit a working implementation.
- Backward compatibility benefit
  - Libraries like Collection and List could add methods such as forEach, stream, or removeIf as default methods, so old code compiled against older JDKs keeps working without modification.
  - Implementing classes may override the default if they want custom behavior, but they are not required to, which preserves binary and source compatibility across versions.

60. Show the syntax of a default method in an interface and give a simple example. - default in front
    - Follow‑up: How does a class implementing that interface invoke the default method? - just likne new instance methods

61. Can classes override default methods? Demonstrate with an example. - yes
    - Follow‑up: If a class overrides a default method, how can it still call the original default implementation? - InterfaceName.super.methodName()

62. What are **static methods** in interfaces and how are they called?
- Follow‑up: Why can static methods in interfaces not be overridden by implementing classes? 
- Static methods in interfaces are not overridden because they belong to the interface type itself, are resolved statically, and are always called using the interface name, not through an instance polymorphism mechanism.
- **Static binding**: Static methods are looked up at compile time by the declared type (`InterfaceName.method()`), so dynamic dispatch (which is what overriding relies on) never applies. 
- **Ownership**: A static method in an interface is more like a namespaced utility function owned by that interface; an implementing class can declare a method with the same signature, but that is a separate, unrelated static method, not an override.
Example:
```java
interface Util {
    static void log(String msg) {
        System.out.println("Interface: " + msg);
    }
}

class Impl implements Util {
    static void log(String msg) {
        System.out.println("Class: " + msg);
    }
}

Util.log("hi");   // calls Util.log
Impl.log("hi");   // calls Impl.log
```
- Here `Impl.log` does not override `Util.log`; each call is bound to the type used at the call site. 

63. Can interface static methods be inherited by implementing classes?
- Follow‑up: How do you call an interface static method from client code?
- Interface static methods are not inherited by implementing classes; they belong only to the interface type and must always be called via the interface name.
- Implementing classes cannot use them as if they were instance methods (no `obj.staticMethod()` via the interface). 
- From client code, you call them using `InterfaceName.staticMethod(args)` exactly like static methods on classes.
Example:

```java
interface Utility {
    static void log(String msg) {
        System.out.println("LOG: " + msg);
    }
}

class Service implements Utility { }

public class Test {
    public static void main(String[] args) {
        Utility.log("hello");   // correct
        // Service.log("hello");   // separate static if declared, not inherited
    }
}
```


64. Why can default methods not override methods from `java.lang.Object` such as `equals`, `hashCode`, `toString`?
- Methods like equals, hashCode, and toString are defined in Object and are always resolved based on the concrete class, not interfaces, to keep dispatch rules simple and unambiguous.
- Allowing interfaces to provide default implementations for these would create conflicts and ambiguity when multiple interfaces provide different defaults, so Java explicitly disallows default methods from overriding Object methods.

- Follow‑up: If you need a common `equals` behavior across implementations, how would you approach it?
- Create a base class with includes the implementation of equals, and extend that class to all to classes that implement the interface


65. Describe the ** problem** scenario with default methods when a class implements two interfaces with the same default method.
- Follow‑up: How do you resolve this conflict in the implementing class syntactically?
- Compilation error unless the implementing class overrides it and resolves the conflict.
- override in the impl class with the InterfaceA or InterfaceB.super.defaultMethod(args) .

66. In a conflict scenario, how do you explicitly call a particular interface’s default method implementation from the class?
    - Follow‑up: Provide code using `A.super.greet()` and explain what it does.

67. Compare **default** and **static** methods in interfaces in terms of purpose, overriding, and call syntax.
    - Follow‑up: In what situations would you prefer a static interface method over a utility class method?

***

## Optional class

68. What is `Optional` in Java 8 and what problem does it aim to solve?
    - Follow‑up: How does `Optional` help with null pointer exceptions conceptually?


69. Show how you would traditionally handle a possibly null return value without `Optional`.
    - Follow‑up: Rewrite this example using `Optional` and `ifPresent`.


70. Explain how to create an empty Optional, a non‑empty Optional, and an Optional from a potentially null value.
    - Follow‑up: What happens if you pass `null` into `Optional.of()`?


71. What does `isPresent()` do? Show a short example.
- Follow‑up: Why is `ifPresent()` often preferred over explicit `isPresent()` and `get()`?
- ifPresent() is preferred because it keeps the “value may be absent” handling inside the Optional API, instead of leaking it back out into manual presence checks.


72. Explain `ifPresent(Consumer)` with an example.
- Follow‑up: How would you perform an action if value is present and another action if it is absent?
```java
Optional<String> nameOpt = person.getNameOptional();

nameOpt.ifPresentOrElse(
        n -> System.out.println("Name: " + n),      // action when present
        () -> System.out.println("Name not set")    // action when absent
);
```


73. What is the difference between `orElse` and `orElseGet`? Provide code to illustrate.
- Follow‑up: In which case can `orElse` be less efficient than `orElseGet`?
- `orElse` can be less efficient when the default value is expensive to create, because it is evaluated eagerly even if the `Optional` already contains a value, while `orElseGet` evaluates the default lazily only when needed. 
Example:

```java
String value = name.orElse(createExpensiveDefault());      // createExpensiveDefault() runs always
String value2 = name.orElseGet(() -> createExpensiveDefault()); // runs only if name is empty
```

74. Explain `orElseThrow(Supplier)` and give an example throwing `IllegalArgumentException`.
    - Follow‑up: How does this compare to manually checking for null and throwing the exception?


75. Describe how `map(Function)` works on an Optional.
- Follow‑up: What is the type of the result when you call `map` on an `Optional<String>` with `String::length`?
- Optional<T> -> Optional<R> by applying the mapper only when there is a value, else return Optional.empty()


76. Explain `flatMap(Function)` in the context of Optional.
- Follow‑up: Provide an example that demonstrates the “nested Optional” problem solved by `flatMap`.
- Use `map` when your mapping function returns a plain value, and `flatMap` when your mapping function itself returns an `Optional` so you avoid getting `Optional<Optional<T>>`. 
- Nested Optional problem with `map`
```java
Optional<String> name = Optional.of("Nikhil");

// Function returns Optional<Integer>
Optional<Optional<Integer>> nested = name.map(
        n -> Optional.of(n.length())
);
// Type: Optional<Optional<Integer>>
```

Here `map` wraps the `Optional<Integer>` again, giving you an extra layer you usually do not want. 
- Solving it with `flatMap`

```java
Optional<String> name = Optional.of("Nikhil");

Optional<Integer> flat = name.flatMap(
        n -> Optional.of(n.length())
);
// Type: Optional<Integer>
```

`flatMap` “flattens” the result by not wrapping the inner `Optional` again, giving you a single `Optional<Integer>` instead of `Optional<Optional<Integer>>`. 


77. What are recommended use cases for `Optional`?
- Follow‑up: Why is it typically recommended as a return type but not as a field type?
- As a return type, Optional<T> forces the caller to consciously handle absence, which is where the decision belongs.
- As a field type, it adds indirection, complicates serialization/JPA mapping, and you still risk Optional fields being null, so it brings overhead without clear benefit; using null or empty collections as field defaults is simpler


78. Why is using `Optional` as a method parameter or in entity fields generally discouraged?
- Follow‑up: How would you represent “no values” in a collection return type without using `Optional<List<T>>`?
- Using `Optional` as a parameter or entity field is discouraged because it complicates APIs and object models without real safety gains, and it interacts poorly with frameworks like JPA and serializers. 
- Why not parameters and fields
  - Method parameters: A parameter of type `Optional<T>` just moves the null-check responsibility to the caller, who can still pass `null`, and it makes call sites noisy (`foo(Optional.of(x))`) without adding real guarantees. 
  - Entity/field types: `Optional` fields are awkward for JPA/ORM and JSON mapping, add an extra wrapper layer, and can themselves be `null`, so you end up with two notions of “absence” (null and empty Optional).
- Representing “no values” for collections
  - For collections, the idiomatic way is to return an **empty collection** instead of `null` or `Optional<List<T>>`. 
  - For example, a repository method should be `List<User> findByRole(String role)` and return `Collections.emptyList()` when there are no users, rather than `Optional<List<User>>`, so callers can just iterate safely without null checks. 


79. Give an example of **misusing** `Optional` with multiple `isPresent()` checks.
    - Follow‑up: Rewrite the same logic using functional style (`map`, `flatMap`, or `filter`).

***

## Date and time API (java.time)

80. What are the main goals of the `java.time` API introduced in Java 8?
- Follow‑up: How does it improve on the old `java.util.Date` and `Calendar` classes?
- Date and Calendar are mutable, awkward to use, and mix multiple concepts (instant, local time, time zone) in a single type; java.time uses immutable value types with clear semantics.
- Many Date methods are deprecated and Calendar has a verbose, error‑prone API; java.time replaces them with consistent factory methods (now, of, parse) and chainable operations like plusDays, minusHours, withZoneSameInstant. 
- Time‑zone handling was cumbersome and fragile with TimeZone and Calendar; java.time brings first‑class zone support and better DST handling via ZoneId and ZonedDateTime.


81. What is `LocalDate` and what does it represent?
    - Follow‑up: Show code to get today’s date and create a specific birth date.

 
82. What is `LocalTime` and what does it represent?
    - Follow‑up: Show code to get current time and create a specific time of 13:30.


83. What is `LocalDateTime` and how does it differ from `ZonedDateTime`?
    - Follow‑up: When would you choose `ZonedDateTime` instead of `LocalDateTime`?


84. Explain how to obtain the current `LocalDate`, `LocalTime`, and `LocalDateTime`.
    - Follow‑up: How do these classes achieve immutability?


85. Describe what `ZonedDateTime` represents.
- Follow‑up: How do you create a `ZonedDateTime` for a specific time zone like `"Asia/Kolkata"`?
```java
ZonedDateTime nowInKolkata =
        ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

ZonedDateTime specificInKolkata =
        ZonedDateTime.of(
                2026, 2, 1, 10, 0, 0, 0,
                ZoneId.of("Asia/Kolkata")
        );
```


86. What is `ZoneId` and how is it used in the new date‑time API?
    - Follow‑up: How would you convert a `LocalDateTime` to a `ZonedDateTime` using a `ZoneId`?


87. How do you **format** and **parse** dates using `DateTimeFormatter`?
    - Follow‑up: Show an example of custom pattern formatting like `"dd-MM-yyyy HH:mm"`.


88. Explain how to perform date‑time arithmetic using methods like `plusDays`, `minusWeeks`, etc.
    - Follow‑up: Are these operations mutating the original object or returning a new one? No, new instance created


89. What utility do classes like `Period` and `Duration` provide?
- Follow‑up: How would you calculate number of days between two `LocalDate` instances?
- Period - days only - LocalDate
- Duration - exact time
- ChronoUnit - usually used to get the duration, period 


90. Are classes in `java.time` thread‑safe? Why does that matter in concurrent applications?
- Follow‑up: How does this compare with `SimpleDateFormat`?
- All `java.time` objects are immutable, so many threads can share one instance safely; `SimpleDateFormat` is mutable, so sharing it between threads causes wrong results or exceptions. 
- Thread‑safe `java.time` example
```java
// One shared formatter (thread‑safe)
DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

ExecutorService pool = Executors.newFixedThreadPool(10);
ZonedDateTime now = ZonedDateTime.now();

for (int i = 0; i < 100; i++) {
    pool.submit(() -> {
        // Safe: same FMT and now used from many threads
        String s = FMT.format(now);
        System.out.println(s);
    });
}
```
Here `FMT` and `now` can be shared across threads with no synchronization because they never change.
- Non‑thread‑safe `SimpleDateFormat` example
```java
// One shared formatter (NOT thread‑safe)
SimpleDateFormat sdf =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

ExecutorService pool = Executors.newFixedThreadPool(10);
Date nowDate = new Date();

for (int i = 0; i < 100; i++) {
    pool.submit(() -> {
        // Unsafe: sdf has mutable internal state
        String s = sdf.format(nowDate); // may throw or print garbage
        System.out.println(s);
    });
}
```
Because `SimpleDateFormat` mutates internal fields during `format`/`parse`, concurrent access from multiple threads can corrupt its state, producing incorrect strings or `NumberFormatException`. 

***

## Parallel streams

91. What is a **parallel stream** in Java 8, and how do you create one?
- Follow‑up: How does `parallelStream()` differ from `stream().parallel()`?
- collection.parallelStream() creates a parallel stream whose source is that collection; it is already marked parallel.
- collection.stream().parallel() starts as a sequential stream from the collection and then calls parallel(), which returns an equivalent stream flagged as parallel; the resulting behavior is the same.


92. What are potential advantages of using parallel streams?
    - Follow‑up: In what scenarios can parallel streams actually degrade performance?


93. What are the key considerations before converting a stream pipeline to use parallel streams? 
- Follow‑up: Why is the cost of each operation important when deciding on parallelism?
- You have large datasets.
  - Your tasks are **CPU-intensive**.
  - Operations are **stateless and independent**.
  - No dependencies on **shared mutable state**.
- Avoid using them for small datasets — parallelization overhead may outweigh the benefits.


94. How do parallel streams internally split work across threads? - divide stream into smaller substreams, eventually join them using Fork.ForkJoinPool
- Follow‑up: Which common `ForkJoinPool` do they typically use by default? - ForkJoinPool.commonPool()


95. Why must operations in a parallel stream be **stateless** and **non‑interfering**?
    - Follow‑up: Give an example of a side‑effect that would cause issues in a parallel stream. - counter increment, add to arrayList - race condition 


96. How does `findAny()` behave differently from `findFirst()` on a parallel stream?
- Follow‑up: Why might `findAny()` be more efficient in that context?
- On a parallel stream, `findFirst()` respects encounter order and returns the first element in that order, whereas `findAny()` may return any element that a thread finds first, ignoring order.
  - `findFirst()` must coordinate between threads to ensure it really returns the first element according to the source’s ordering, which can require extra synchronization and limit parallel optimization. 
  - `findAny()` gives the implementation more **freedom** to stop as soon as any thread finds a matching element, reducing coordination overhead and therefore often being more efficient on parallel streams. 


97. What precautions should you take when using mutable shared state with parallel streams? - dont do any write operations on the mutable state, if any write operation make it synchronized or use  Collections.synchronizedList or other concurrent collections.
- Follow‑up: How can you design your pipeline to avoid shared mutable state?
 

98. Explain why `collect()` with non‑thread‑safe mutable collectors can be problematic in parallel.
    - Follow‑up: How does `Collectors.toList()` handle concurrency under the hood?

