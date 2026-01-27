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
   - ??
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
    - Loads class files (`.class`) and follows the **Parent Delegation Model** to avoid class conflicts and ensure security.
    - Parent Delegation Model ensures security and class consistency by allowing child classloaders to defer loading to parent loaders first.
18. What is stored in the **heap** vs the **stack** in Java? 
19. Can a `NullPointerException` be caused by stack issues or is it purely heap related? Explain. 
    - NPE is about using a null reference, independent of whether that reference is stored in a stack frame or as a field in a heap object. 
    - You can say: “The reference may be stored on the stack (local variable) or on the heap (field), but the exception is thrown when the JVM tries to dereference a null, not because of stack or heap capacity issues.”
20. What is **Metaspace** and how is it different from the old PermGen? 
21. What is the **Program Counter Register** and why does each thread have its own? 
    - ??
22. Explain the role of the **execution engine**, including interpreter and JIT. 
23. Describe how **object allocation** happens when you call `new`. 
24. What does it mean that “most objects die young” in the context of generational GC?
    - ??
25. Compare **Serial GC**, **Parallel GC**, and **G1 GC** conceptually. 
26. When would you consider tuning `-Xms`, `-Xmx`, and `-XX:+UseG1GC`? 
    - ??
27. What are typical causes of `java.lang.OutOfMemoryError: Java heap space` vs `java.lang.OutOfMemoryError: Metaspace`?
    - ??
28. How can a memory leak happen in Java even though there is garbage collection? 
    - ??
29. How would you start troubleshooting an OOM in a production microservice? 
    - ??
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

