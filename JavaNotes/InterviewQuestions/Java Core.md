### Java OOP and basics (1–10)

1. What is the difference between a **class** and an **object** in Java? 
2. Explain **encapsulation** and how access modifiers help achieve it in Java. 
3. How does **abstraction** differ from encapsulation, and how is abstraction implemented in Java? 
4. What is **inheritance** in Java and how does it promote code reusability? 
5. Define **polymorphism** and differentiate between compile-time and runtime polymorphism with examples. 
6. List the key methods of the **Object** class and explain the purpose of any two of them. 
7. What is the role of the `equals()` method, and how is it different from `==` for objects? 
8. What does the `hashCode()` method represent and why is it important when overriding `equals()`? 
9. When would you override `toString()` in a class, and what benefit does it provide? 
10. Why is `finalize()` deprecated, and what was its original purpose? 

### JVM, memory model, GC (11–20)

11. Describe the main components of the **JVM architecture**, naming at least three major parts. 
12. What is the **ClassLoader subsystem**, and what problem does the **Parent Delegation Model** solve? 
13. Differentiate between **Heap** and **Stack** memory in terms of what they store and how they are scoped. 
14. Explain the roles of **Young Generation**, **Old Generation**, and **Metaspace** in the JVM. 
15. What is the **Program Counter Register**, and why does each thread have its own? 
16. What is the purpose of the **Native Method Stack** in the JVM? 
17. Describe the general lifecycle of **Java memory management** from JVM startup to shutdown. 
18. Compare **Serial GC**, **Parallel GC**, and **G1 GC** in terms of their goals and typical use cases. 
19. What JVM tuning parameters like `-Xms`, `-Xmx`, and `-XX:+UseG1GC` are used for? 
20. What can cause different types of `OutOfMemoryError` in Java, such as “Java heap space” and “Metaspace”? 

### String pool, equals & hashCode, Comparable/Comparator (21–25)

21. Explain how the **String Pool** works and how `String.intern()` affects where a string is stored. 
22. What is the **contract** between `equals()` and `hashCode()` that must always be respected? 
23. Why is it a bad idea to use **mutable fields** inside implementations of `equals()` and `hashCode()`? 
24. Define **Comparable** and **Comparator** and explain when you would choose one over the other. 
25. How does `Collections.sort(List)` behave differently when elements implement `Comparable` vs when a `Comparator` is provided? 

Continuing in the same style, here are questions 26–100. 

### Immutability and defensive copying (26–40)

26. What is an **immutable class** in Java, and name three standard immutable types from the JDK. 
27. Why does making a class `final` help in designing it as immutable? 
28. List the core **rules** you should follow to make a class immutable in Java. 
29. Why must fields in an immutable class typically be both `private` and `final`? 
30. In the `Person` example with an `Address` field, why is a **defensive copy** of `Address` created in the constructor? 
31. How does immutability contribute to **thread safety** in multi-threaded applications? 
32. Explain why immutability is especially useful for **keys in a HashMap**. 
33. What could go wrong if a key’s state used in `hashCode()` changes after inserting it into a `HashMap`? 
34. In the context of immutability, why are **setters** generally not provided? 
35. How does the **builder pattern** relate to immutable classes, and why is it not a problem that it creates multiple instances? 
36. In the provided `Person` class, why does the `getAddress()` method return a new `Address` object instead of the internal one? 
37. How should you handle **collections** (like `List`) inside immutable classes to preserve immutability? 
38. In the `Person` example, why is `new ArrayList<>(hobbies)` returned in the getter rather than the original list? 
39. Explain how immutability helps avoid “**spooky action at a distance**” bugs. 
40. In the example with `order` and `discountedOrder`, how does using immutable objects make the flow more explicit and easier to reason about? 

### Serialization, transient, static, serialVersionUID (41–55)

41. What is **serialization** in Java, and what is its primary purpose? 
42. What is **deserialization**, and how is it related to serialization? 
43. Why does a class need to implement `Serializable` for its objects to be serialized? 
44. What happens if a class contains a field whose type is **not serializable** but the class itself implements `Serializable`? 
45. What is the role of the **`transient`** keyword in the context of serialization? 
46. After deserialization, what value do `transient` fields hold, and why? 
47. Give two examples of data that are good candidates to be marked as `transient`. 
48. Why are **static fields** not serialized in Java object serialization? 
49. What is `serialVersionUID`, and what problem does it solve during deserialization? 
50. What exception is thrown if the `serialVersionUID` of a class and of the serialized object do not match? 
51. When implementing custom serialization, what is the purpose of calling `defaultWriteObject()` inside `writeObject()`? 
52. In custom serialization, why might you write `age * 2` (or transformed data) instead of the raw field directly? 
53. What is the difference between **`Serializable`** and **`Externalizable`** in Java? 
54. In `Externalizable`, which methods must you implement, and what do they do? 
55. What is the purpose of the **`readResolve()`** method in the context of serialization, especially for singletons? 

### Annotations and meta-annotations (56–70)

56. Conceptually, what is an **annotation** in Java, and how does it relate to code behavior? 
57. Give a simple example of using `@Override` and explain what the compiler checks when this annotation is present. 
58. What is the purpose of the `@Deprecated` annotation, and how might tools or IDEs react to it? 
59. How does `@SuppressWarnings` help in managing compiler warnings? Give an example scenario. 
60. What guarantee does the `@FunctionalInterface` annotation provide for an interface? 
61. What is a **meta-annotation**, and why are meta-annotations like `@Target` and `@Retention` important? 
62. What does `@Target(ElementType.METHOD)` indicate for a custom annotation? 
63. Explain the difference between `RetentionPolicy.SOURCE`, `RetentionPolicy.CLASS`, and `RetentionPolicy.RUNTIME`. 
64. Why must an annotation have `@Retention(RUNTIME)` to be accessible via reflection at runtime? 
65. What is the purpose of the `@Inherited` meta-annotation? 
66. How does `@Documented` affect the visibility of an annotation in generated Javadoc? 
67. In the `MyAnnotation` example, what are **annotation elements**, and how do you specify a default value? 
68. Show how you would apply `@MyAnnotation(value = "Hello", version = 2)` to a method and explain what each element means. 
69. In the `AnnotationReader` example, how do you obtain a `Method` instance and then read a custom annotation from it? 
70. Mention two **real-world** annotation use cases from frameworks like Spring, JUnit, or JPA and briefly describe what they do. 

### Reflection API (71–80)

71. What is the **Reflection API** in Java used for? 
72. In the sample code, how is `Class.forName("Employee")` used, and what does it return? 
73. What does `getDeclaredMethod("getName")` do in the reflection example? 
74. Why might you need to call `setAccessible(true)` on a `Method` object? 
75. How is `method.invoke(emp)` used to call a method via reflection, and what does it return? 
76. Name two common **use cases** where reflection is heavily used in Java frameworks. 
77. What are two **drawbacks** of using reflection compared to direct method calls? 
78. How can security managers restrict reflection, and why might that be desirable? 
79. Why is reflection considered more fragile with respect to refactoring (e.g., renaming methods)? 
80. In the context of annotations plus reflection, how do frameworks combine these to implement declarative behavior? 

### Enums, marker interfaces, strategy (81–92)

81. How can an enum in Java define **methods per constant**, as in the `Operation` enum example? 
82. In the `Operation` enum with `ADD` and `SUB`, why is there an **abstract** method `apply(int x, int y)` at the bottom of the enum? 
83. What are **EnumSet** and **EnumMap**, and why are they considered high-performance collections? 
84. How can enums be used to implement the **Strategy Pattern** without long `if-else` or `switch` statements? 
85. Why can enums implement interfaces but cannot extend regular classes? 
86. In the `CalculatorOperation` example, how does the enum implement the `Operation` interface? 
87. In the same example, how would you call the `apply` method on `CalculatorOperation.ADD` from a `main` method? 
88. What is a **marker interface**, and how does it differ from a regular interface? 
89. Give two JDK examples of marker interfaces and describe what they “mark”. 
90. In the `Important` marker interface example, how is `instanceof Important` used at runtime? 
91. Why might you still use a marker interface instead of an annotation in some cases? 
92. How can enums implement a marker interface like `CategoryMarker` to enforce type safety in a method parameter? 

### Functional programming concepts (93–100)

93. What is a **pure function** in Java, and what two key properties must it satisfy? 
94. Why are pure functions inherently safer to use in multi-threaded code? 
95. In the `square(int x)` example, explain why this method qualifies as a pure function. 
96. What is a **higher-order function**, and how do lambdas enable this style in Java? 
97. In the example with `Function<Integer, Integer> square` and `doubler`, how is `map(square)` used with streams? 
98. Give one or two Java functional interfaces from `java.util.function` and briefly describe what they represent. 
99. What is **function composition** conceptually, and how does `f(g(x))` relate to Java’s `Function.andThen` or `compose` methods? 
100. What is **currying**, and how does it conceptually differ from just passing multiple parameters to a method? 


---------------


Here are 100 interviewer-style questions, all based on the topics in your Java Core.md, with many confusing follow‑ups mixed in.

## 1–15: OOP, Object class, basics

1. What is the difference between a class and an object in Java? 
2. In a real project, how would you decide whether something should be modeled as a class vs just a method with parameters? 
   - A good rule is: if something has its own data plus meaningful behavior and identity, model it as a class; if it’s just an operation on existing data, keep it as a method.
3. What is **encapsulation** and how do access modifiers help achieve it? 
4. 
5. Explain **abstraction** and how it differs from encapsulation. 
   - Abstraction is about exposing what an object does and hiding how it does it; encapsulation is about grouping state and behavior and controlling access to that state.
6. When would you choose an abstract class over an interface in modern Java (8+)? 
   - Account - abstract class for accountName, number, type etc.
   - Capability - Withdrawable and deposit accounts
   - Saving account -  extends account and implements both capabilities
   - investment account - extends account and implements deposit capabilities
   - Note: In Java an interface cannot extend a class, it can only extend other interfaces.
7. What is **inheritance** and when can it become harmful in a large codebase? 
8. Why is composition often preferred over inheritance? Give a concrete example. 
   - ?
9. Explain **polymorphism** with an example of overriding in Java. 
10. How does dynamic dispatch work at runtime for overridden methods? 
11. What are the key methods defined in `Object` class? 
12. Why is `finalize()` deprecated and what would you use instead? 
13. How does `clone()` work and what are the pitfalls of using it? 
    - Confusing contract and visibility: clone() is protected in Object and tied to the marker interface Cloneable, making the API inconsistent and non‑intuitive, 
    - To make it usable, a class must implement Cloneable and override clone() as public, usually calling super.clone() and casting.
    - Cloneable is broken design: Cloneable has no methods; Object.clone() just checks its presence at runtime and otherwise throws, so correct behavior is enforced only by convention.
    - Shallow copying and deep copying can cause a lot of misunderstanding.
14. What’s the difference between `==` and `equals()` for objects? 
15. How would you design a domain object so that it plays nicely with collections like `HashSet` and `HashMap`? 
    - always make the key, private final so it can not be changed later
    - override hashcode and equals method

## 16–30: JVM, memory model, GC, tuning, OOM

16. Explain the major components of the **JVM architecture**. 
17. What is the role of the **ClassLoader subsystem** and what is the Parent Delegation Model? 
    - ??
18. What is stored in the **heap** vs the **stack** in Java? 
19. Can a `NullPointerException` be caused by stack issues or is it purely heap related? Explain. 
    - ??
20. What is **Metaspace** and how is it different from the old PermGen? 
21. What is the **Program Counter Register** and why does each thread have its own? 
22. Explain the role of the **execution engine**, including interpreter and JIT. 
23. Describe how **object allocation** happens when you call `new`. 
24. What does it mean that “most objects die young” in the context of generational GC? 
25. Compare **Serial GC**, **Parallel GC**, and **G1 GC** conceptually. 
26. When would you consider tuning `-Xms`, `-Xmx`, and `-XX:+UseG1GC`? 
27. What are typical causes of `java.lang.OutOfMemoryError: Java heap space` vs `java.lang.OutOfMemoryError: Metaspace`? 
28. How can a memory leak happen in Java even though there is garbage collection? 
29. How would you start troubleshooting an OOM in a production microservice? 
30. Explain the **lifecycle of Java memory management** from JVM startup to shutdown. 

## 31–45: String pool, equals & hashCode, Comparable/Comparator

31. What is the **String Pool** and where is it stored in modern JVMs? 
32. How does `String.intern()` work and when could it cause performance problems? 
33. 
34. What is the **contract** between `equals()` and `hashCode()`? 
35. What can go wrong in a `HashMap` if you violate the equals–hashCode contract? 
36. Why is it dangerous to use **mutable fields** inside `equals()` and `hashCode()` implementations? 
37. In a running application, if a key’s field used in `hashCode()` changes after insertion into a `HashMap`, what symptoms will you see? 
38. How do `HashMap` and `HashSet` use `hashCode()` and `equals()` internally? 
39. What is `Comparable` and what is its purpose? 
40. Why might relying only on `Comparable` be limiting in a real application? 
    - ??
41. What is `Comparator` and when would you prefer it over `Comparable`? 
    - ??
42. Can you sort the same list of objects in multiple ways using `Comparable` alone? Explain. 
43. What is the difference between `Collections.sort(List)` and `Collections.sort(List, Comparator)`? 
44. If you implement both `Comparable` and also pass a `Comparator` to sorting, which one takes precedence? 
45. How can Java 8 method references and `Comparator.comparing` simplify comparator creation? 
    - ??

## 46–60: Immutability, defensive copying, builder pattern

46. What is an **immutable class** in Java, and name some standard immutable types. 
47. Why are immutable objects **thread-safe by default**? 
48. Explain why immutability is especially useful when using objects as keys in `HashMap`. 
49. What are the formal **rules** you would follow to make a class immutable? 
50. Why do we often make the class itself `final` in an immutable design? 
51. What is a **defensive copy** and where do you use it in an immutable class? 
52. In the `Person` example with `Address`, why do we create a new `Address` in the constructor and in the getter? 
53. How would you handle **collections** inside an immutable class (e.g., `List<String> hobbies`)? 
54. Does immutability mean “only one instance of the object exists in the JVM”? Clarify. 
55. How does immutability make debugging and reasoning about code easier in large systems? 
56. How is the **Builder pattern** compatible with immutable classes? 
57. Why are **setters** typically not allowed in an immutable class, even if they return `this` (fluent style)? 
58. Can an immutable object contain a reference to a mutable object and still be considered truly immutable? Under what conditions? 
59. In practice, where would you choose immutability over mutability in a microservices backend? 
    - ??
60. Give an example of a bug that could arise if you forget defensive copying in an immutable class. 

## 61–75: Serialization, transient/static, serialVersionUID, Externalizable, readResolve

61. What is **serialization** in Java and why is it used? 
62. What is **deserialization** and what risks does it introduce in a secure system? 
63. What does the `transient` keyword do for a field during serialization? 
64. In your `User` example, what will be the value of `password` after deserialization and why? 
    - ??
65. Why are **static fields** not serialized? 
66. What is `serialVersionUID` and what happens if it mismatches during deserialization? 
    - ??
67. Why is it recommended to explicitly define `serialVersionUID` instead of relying on the default? 
68. What happens if a serializable class contains a non-serializable field? 
    - ??
69. Compare `Serializable` and `Externalizable`. When would you choose `Externalizable`? 
    - ??
70. What is **custom serialization** using `writeObject` and `readObject`, and when would you use it? 
71. Why might you want to encrypt or transform a field (like `age * 2`) in custom serialization? 
72. How does **`readResolve()`** help maintain a Singleton invariant after deserialization? 
73. Can deserialization break immutability or encapsulation guarantees? How would you guard against that? 
74. In distributed systems, why is Java built‑in serialization often avoided in favor of formats like JSON/Protobuf? 
75. How could improper use of serialization lead to an `OutOfMemoryError`? 

## 76–85: Annotations, meta-annotations, custom annotations, reflection

76. What is an **annotation** in Java and what does it not do by itself? 
77. Explain the difference between `@Override`, `@Deprecated`, `@SuppressWarnings`, and `@FunctionalInterface`. 
78. What are **meta-annotations** and what is the purpose of `@Target`? 
79. Explain the three `@Retention` policies: SOURCE, CLASS, and RUNTIME. 
80. Why must an annotation be marked with `@Retention(RUNTIME)` if you want to access it via reflection? 
81. Explain `@Inherited` and a scenario where it might not behave as developers intuitively expect. 
    - ??
82. Show how you would define a custom annotation with a required element and a default element. 
83. How would you read your custom `@MyAnnotation` at runtime using reflection? 
84. Give some real-world uses of annotations in frameworks like Spring, JUnit, and JPA. 
85. Compare **marker interfaces** with annotations as mechanisms for adding metadata. When is each more appropriate? 

## 86–95: Reflection, ClassLoader, enums, marker interfaces

86. What is the **Reflection API** in Java and what are its main capabilities? 
87. How can reflection be used to call a private method and why is that usually discouraged? 
88. What are the performance and security drawbacks of using reflection extensively? 
89. How do frameworks like Spring or Hibernate rely on reflection and annotations together? 
    - ??
90. What is an **enum** in Java and how is it different from a set of `public static final` constants? 
    - ??
91. How can enums have methods and fields, and why is that powerful (e.g., strategy pattern)? 
92. Explain `EnumSet` and `EnumMap`. Why are they more efficient than `HashSet` or `HashMap` for enums?
    - ??
93. How can an enum implement an interface like `Operation` and override the method differently per constant? 
94. What is a **marker interface** and give two examples from the JDK. 
95. How can enums implement marker interfaces and how does that help enforce type-safe APIs? 

## 96–100: Functional style, pure functions, higher-order, JDK functional interfaces

96. What is a **pure function** and why is it naturally thread-safe? 
97. How does functional style (pure functions, immutability) help in concurrent Java applications? 
98. What is a **higher-order function** and how do Java lambdas and functional interfaces enable them? 
    - ??
99. Name a few core functional interfaces from `java.util.function` and typical use cases for each. 
100. Explain function composition and currying conceptually and how you might approximate them in Java using lambdas.
     - ??

