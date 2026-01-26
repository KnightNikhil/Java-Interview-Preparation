# JAVA CORE

---

## Table of Contents

1. [Java OOP](#java-oop)
2. [Java Memory Model & JVM Internals](#java-memory-model--jvm-internals)
3. [equals() & hashCode()](#equals--hashcode)
4. [Immutability & Defensive Copying](#immutability--defensive-copying)
5. [Serialization & Deserialization](#serialization--deserialization)
6. [Annotations](#annotations)
7. [Reflection API](#reflection-api)
8. [ClassLoaders](#classloaders)
9. [Enums (Advanced Use)](#enums-advanced-use)
10. [Functional Programming Concepts](#functional-programming-concepts)
11. [Best Practices & Code Quality](#best-practices--code-quality)
12. [Bonus Interview Topics](#bonus-interview-topics)

---
 
## Java OOP

### Class & Object

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

### Encapsulation

- Wrapping data and methods into a single unit and restricting access using access modifiers.
- Promotes data hiding and control over data.

```java
class Account {
    private double balance;
    public double getBalance() { return balance; }
    public void deposit(double amount) { if(amount > 0) balance += amount; }
}
```

### Abstraction

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

### Inheritance

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

### Polymorphism

- Ability to take many forms. Two types:
  - Compile-time (method overloading)
  - Runtime (method overriding)

```java
class Shape { void draw() { System.out.println("Drawing shape"); } }
class Circle extends Shape { void draw() { System.out.println("Drawing circle"); } }
```

### Object class methods

- `equals()` – logical equality
- `hashCode()` – bucket placement in hashing
- `toString()` – string representation
- `clone()` – creates copy of object
- `finalize()` – called by GC before object removal (deprecated)

---

## Java Memory Model & JVM Internals

### JVM Architecture

#### ClassLoader Subsystem

- Loads class files (`.class`) and follows the **Parent Delegation Model** to avoid class conflicts and ensure security.

#### Runtime Data Areas

- **Heap:** The main region for dynamic memory allocation where all objects, arrays, and class instances are stored. The heap is managed by the garbage collector and is shared by all threads.
- **Stack:** Each thread has its own stack that holds method frames, local variables, and references to objects in the heap. This memory is reclaimed automatically when a method call ends or the thread exits.
  - **Young Generation:** Holds newly created objects and is optimized for fast allocation and frequent garbage collections (Minor GCs).
  - **Old Generation (Tenured):** Contains long-lived objects that survived multiple garbage collections.
  - **Permanent Generation (PermGen) / Metaspace:** Stores class metadata, method objects, and interned strings. (PermGen is replaced by Metaspace in Java 8+.)
- **Program Counter Register:** Holds the address of the current executing instruction for a thread.
- **Metaspace:** This area keeps metadata about classes and methods (formerly the PermGen in older JVMs). Class definitions, static variables, and other runtime structures live here.
- **Native Method Stack:** Used for native code execution via JNI (Java Native Interface). It operates alongside JVM stacks but serves native (non-Java) methods.

#### Execution Engine

- **Interpreter:** Reads and executes bytecode line-by-line.
- **JIT Compiler (Just-In-Time):** Converts bytecode to native machine code for performance optimization.

---

### Object Allocation and Garbage Collection

- **Object Creation:** When a new object is created, memory is allocated in the heap, and the reference is stored on the stack.
- **Garbage Collection:** Java uses automatic garbage collection to free up memory occupied by objects that are no longer referenced. There are several GC algorithms (like G1, ZGC, Shenandoah), and the heap’s generational division optimizes collection by focusing on short-lived objects first—most objects die young and are collected quickly in the young generation. Objects that survive multiple collections are promoted to the old generation, where they are collected less frequently.

#### GC Algorithms

- **Serial GC:** Single-threaded, best for small applications.
- **Parallel GC:** Multi-threaded, optimized for throughput.
- **CMS (Concurrent Mark-Sweep):** Low-latency GC (deprecated).
- **G1 GC (Garbage First):** Default collector; splits heap into regions, supports concurrent and parallel collection.

### JVM Tuning Parameters

- Control memory and GC behavior using flags:
  - `-Xms`: Initial heap size
  - `-Xmx`: Maximum heap size
  - `-XX:+UseG1GC`: Enable G1 Garbage Collector

### String Pool
- A special memory region in the heap where string literals are stored to optimize memory usage and performance. Strings created using double quotes are interned in the pool, while those created with `new` are not unless explicitly interned using `String.intern()`.
- Helps avoid duplicate string objects and saves memory.
- Example:
```java
String s1 = "hello"; // in string pool
String s2 = new String("hello"); // in heap
String s3 = s2.intern(); // refers to string pool
System.out.println(s1 == s3); // true
System.out.println(s1 == s2); // false
```
---

### Lifecycle of Java memory management

1. JVM Startup and Memory Area Creation - JVM initializes memory areas (Heap, Stack, Metaspace), sized based on JVM arguments like `-Xms` and `-Xmx`
2. Class Loading - ClassLoader loads classes into Metaspace
3. Object Creation - New objects are allocated in the Heap, with references stored in the Stack
4. Method Invocation - Each method call creates a new frame on the Stack, holding local variables and references
5. Garbage Collection - The GC periodically reclaims memory from unreachable objects, focusing on the Young Generation first. Stack and native method stack memory is released when threads terminate
6. JVM Shutdown - On application exit, JVM cleans up resources and memory

---

### Parent Delegation Model

- Class loaders delegate loading to parent loaders before attempting to load themselves.
- Helps maintain consistency and avoids loading same class multiple times.

---

### Memory Leaks / OutOfMemoryError (OOM)

#### Memory Leaks

- Caused when references to unused objects are unintentionally retained.

#### Types of OOM Errors

- `java.lang.OutOfMemoryError: Java heap space`
- `java.lang.OutOfMemoryError: GC overhead limit exceeded`
- `java.lang.OutOfMemoryError: Metaspace`

---

### Interview Follow-up Questions

**Q1: What are the major components of the JVM?**  
A: ClassLoader, Runtime Data Areas (Heap, Stack, Metaspace, etc.), Execution Engine (Interpreter & JIT), and Garbage Collector.

**Q2: What is the Parent Delegation Model and why is it important?**  
A: It ensures security and class consistency by allowing child classloaders to defer loading to parent loaders first.

**Q3: Difference between Heap and Stack memory?**  
A: Heap stores objects and instances; Stack stores method calls and local variables. Stack is thread-specific, Heap is shared.

**Q4: What causes OutOfMemoryError in Java?**  
A: It occurs when memory regions (Heap, Metaspace, etc.) are exhausted due to memory leaks or insufficient configuration.

**Q5: How does G1 GC work?**  
A: G1 divides the heap into regions and collects them in parallel and concurrently, optimizing for predictable pause times.

---

## equals() & hashCode()

### Contract

- Must follow these properties:
  - Reflexive: `a.equals(a)` must be true
  - Symmetric: `a.equals(b)` ⇔ `b.equals(a)`
  - Transitive: `a.equals(b)` & `b.equals(c)` ⇒ `a.equals(c)`
  - Consistent: Repeated calls must return the same result
  - Null-safe: `a.equals(null)` must return false
- If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` must also hold

### Best Practices

- Avoid using mutable fields in `equals()` and `hashCode()`
- Use `Objects.equals()` and `Objects.hash()` (Java 7+)

### HashMap/HashSet

- **hashCode()** determines the bucket
- **equals()** determines object equality within the bucket

---

## Comparable & Comparator

- **Comparable:** Defines natural order

```java
class Person implements Comparable<Person> {
    int age;
    public int compareTo(Person p) {
        return Integer.compare(this.age, p.age);
    }
}
```

- **Comparator:** Defines custom order

```java
Comparator<Person> byName = Comparator.comparing(p -> p.name);
```



### Q. What is Comparable and Comparator Interface in java?

Comparable and Comparator both are interfaces and can be used to sort collection elements.

| Comparable | Comparator |
|------------|------------|
| Comparable provides a single sorting sequence. In other words, we can sort the collection on the basis of a single element such as id, name, and price. | The Comparator provides multiple sorting sequences. In other words, we can sort the collection on the basis of multiple elements such as id, name, and price etc. |
| Comparable affects the original class, i.e., the actual class is modified. | Comparator doesn't affect the original class, i.e., the actual class is not modified. |
| Comparable provides compareTo() method to sort elements. | Comparator provides compare() method to sort elements. |
| Comparable is present in java.lang package. | A Comparator is present in the java.util package. |
| We can sort the list elements of Comparable type by Collections.sort(List) method. | We can sort the list elements of Comparator type by Collections.sort(List, Comparator) method. |

**Example:**

```java
// Java Program to demonstrate the use of Java Comparable.
import java.util.*;
import java.io.*;

class Student implements Comparable<Student>{
    int rollno;
    String name;
    int age;
    Student(int rollno,String name,int age){
        this.rollno=rollno;
        this.name=name;
        this.age=age;
    }
    public int compareTo(Student st){
        if(age==st.age)
          return 0;
        else if(age>st.age)
          return 1;
        else
          return -1;
    }
}

// Creating a test class to sort the elements
public class ComparableMain {
    public static void main(String args[]) {
        ArrayList<Student> al=new ArrayList<Student>();
        al.add(new Student(101,"Ryan Frey",23));
        al.add(new Student(106,"Kenna Bean",27));
        al.add(new Student(105,"Jontavius Herrell",21));

        Collections.sort(al);
        for(Student st:al){
            System.out.println(st.rollno+" "+st.name+" "+st.age);
        }
    }
}
```

**Example:** Java Comparator

Student.java

```java
class Student {
    int rollno;
    String name;
    int age;
    Student(int rollno,String name,int age) {
      this.rollno=rollno;
      this.name=name;
      this.age=age;
    }
}
```

AgeComparator.java

```java
import java.util.*;

class AgeComparator implements Comparator<Student> {
    public int compare(Student s1,Student s2) {
    if(s1.age==s2.age)
      return 0;
    else if(s1.age>s2.age)
      return 1;
    else
      return -1;
    }
}
```

NameComparator.java

```java
import java.util.*;

class NameComparator implements Comparator<Student> {
    public int compare(Student s1,Student s2) {
        return s1.name.compareTo(s2.name);
    }
}
```

TestComparator.java

```java
// Java Program to demonstrate the use of Java Comparator
import java.util.*;
import java.io.*;

class TestComparator {

    public static void main(String args[]) {
        // Creating a list of students
        ArrayList<Student> al=new ArrayList<Student>();
        al.add(new Student(101,"Caelyn Romero",23));
        al.add(new Student(106,"Olivea Gold",27));
        al.add(new Student(105,"Courtlyn Kilgore",21));

        System.out.println("Sorting by Name");
        // Using NameComparator to sort the elements
        Collections.sort(al,new NameComparator());
        // Traversing the elements of list
        for(Student st: al){
          System.out.println(st.rollno+" "+st.name+" "+st.age);
        }

        System.out.println("sorting by Age");
        // Using AgeComparator to sort the elements
        Collections.sort(al,new AgeComparator());
        // Travering the list again
        for(Student st: al){
          System.out.println(st.rollno+" "+st.name+" "+st.age);
        }
    }
}
```

**Output:**

```
Sorting by Name
106 Caelyn Romero 23
105 Courtlyn Kilgore 21
101 Olivea Gold 27

Sorting by Age
105 Courtlyn Kilgore 21
101 Caelyn Romero 23
106 Olivea Gold 27
```

---

## Immutability & Defensive Copying

### What is an Immutable Class?

- An immutable class is one whose state (fields) cannot be changed after the object is created. 
- A final class in Java prevents inheritance, ensuring no other class can extend it. This design choice promotes security and immutability by completing the class’s implementation. Developers use it when subclassing could compromise behavior or performance

- Example: String, LocalDate, Integer.
- Once constructed, their values never change.

#### Benefits

- **Thread-safe by default**
  - Concurrency issues arise when:
    -	Multiple threads
    -	Shared mutable state
    -	At least one write 
    - Immutability removes the “write” entirely.
  - Note:
    - Immutability ≠ singleton
    -	Immutability ≠ one instance
    -	Immutability = no state change after construction
    -	Multiple identical immutable objects are harmless



- **Makes reasoning about code easier**
    - Mutable:
  ```java
    Order order = orderService.getOrder();
    
    validate(order);
    calculateDiscount(order);
    save(order);
    ```
    - If calculateDiscount() mutates:
      1. Debugging becomes a nightmare
      2. Side effects propagate silently
    - Immutable:
  ```java
    Order order = orderService.getOrder();
    Order discountedOrder = discountService.apply(order);
    save(discountedOrder);
  ```
    1. Flow is explicit
    2. No hidden side effects 
    3. Each method is pure
    - Why this matters at scale
      -	Fewer defensive copies
      -	Easier debugging
      -	Easier testing
      -	Fewer “spooky action at a distance” bugs


- **Useful in caching and map keys**
  - How HashMap works (critical understanding)
    1.	Key’s hashCode() decides bucket
    2.	equals() resolves collision
    3.	HashMap assumes key never changes
  - What happens if the hashCode() changed?
    1. Entry is in wrong bucket
    2. map.get(key) → null
  - Immutable key = safe forever
    1. Make key final `private final String userId;`
    2. Since, equals + hashCode based on userId
    3. Hash never changes


#### Builder pattern and immutable class
- Point to remember: 
  - builder creates a new object everytime
    - So, basically builder acts as a scalable constructor
    - there's no prblem with creating multiple instances of a class, the problem is it should change it's state/value once created
  - Setter can change the state of a files so they can not be used 


**Rules**

1. Make class final.
2. Make fields private and final.
3. Initialize via constructor only.
4. No setters.
5. Return defensive copies of mutable fields.

#### Steps to Make a Class Immutable

Let’s say we want an immutable Person class.

1. **Declare the class final**  
   Prevents subclassing, because a subclass could add mutable behavior.

    ```java
    public final class Person {
    ```

2. **Make all fields private and final**  
- Making fields final in Java prevents reassignment of their references after initialization, promoting immutability and clear intent. This design choice enhances thread safety and enables compiler optimizations.
-  Prevents external modification.  
-  final ensures they can be assigned only once (in constructor).

    ```java
    private final String name;
    private final int age;
    private final Address address; // mutable object
    ```

3. **Initialize fields only in constructor**

    ```java
    public Person(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        // Defensive copy if mutable object
        this.address = new Address(address.getCity(), address.getZip());
    }
    ```

4. **Do not provide setters**  
   Only getters (read-only).

    ```java
    public String getName() { return name; }
    public int getAge() { return age; }
    ```

5. **Return defensive copies for mutable fields**

    ```java
    public Address getAddress() {
        return new Address(address.getCity(), address.getZip()); // defensive copy
    }
    ```

   For collections:

    ```java
    private final List<String> hobbies;

    public List<String> getHobbies() {
        return new ArrayList<>(hobbies); // return copy
    }
    ```

**Complete Example**

```java
public final class Person {
    private final String name;
    private final int age;
    private final Address address; // mutable object

    public Person(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        // defensive copy to avoid external modification
        this.address = new Address(address.getCity(), address.getZip());
    }

    public String getName() { return name; }
    public int getAge() { return age; }

    // defensive copy
    public Address getAddress() {
        return new Address(address.getCity(), address.getZip());
    }
}

// Mutable Address class (not immutable!)
class Address {
    private String city;
    private String zip;

    public Address(String city, String zip) {
        this.city = city;
        this.zip = zip;
    }

    public String getCity() { return city; }
    public String getZip() { return zip; }
    public void setCity(String city) { this.city = city; }
    public void setZip(String zip) { this.zip = zip; }
}
```

**How this works**

```java
Address addr = new Address("Pune", "411001");
Person p1 = new Person("Nikhil", 25, addr);

// Try to modify original address
addr.setCity("Mumbai");

System.out.println(p1.getAddress().getCity()); // Still Pune ✅
```

Because we made defensive copies, Person stays immutable.

---

## Serialization & Deserialization

Serialization in Java is the process of converting an object’s state into a byte stream, enabling the object to be easily persisted to a file, sent over a network, or stored in memory for later retrieval. Deserialization is the reverse process, where this byte stream is used to recreate the original Java object in memory—retaining its data and structure.

### Transient Fields

- The transient keyword is used to prevent sensitive or temporary instance variables from being serialized. When a field is marked as transient, its value is not saved to the serialization stream. During deserialization, the transient field is set to its default value (null, 0, false depending on type) rather than its original value.
- Transient fields are useful for excluding data that must not be persisted, such as passwords, authentication tokens, runtime states, or derived values.

**Example:**

```java
class User implements Serializable {
    String username;
    transient String password; // Will not be serialized
}
```

After deserialization, `password` is set to `null` regardless of its original value.

### Static Fields

- Static fields are class variables, shared across all instances and belonging to the class itself, not individual objects. 
- Because serialization captures the state of an object, and **static variables are not part of individual object state, they are never serialized**.

### serialVersionUID

- A unique version identifier ensures compatibility between the serialized object and class definition for deserialization. 
- If mismatched, an `InvalidClassException` occurs.

### Associated Objects

- Objects referenced by a serializable class must also be serializable; otherwise, `NotSerializableException` is thrown.

#### Serializable vs Externalizable

- **Serializable:** JVM handles serialization
- **Externalizable:** You write writeExternal() and readExternal()

**Custom Serialization:**

```java
private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeInt(age * 2); // custom
}
```

#### **readResolve():**
- Used to maintain Singleton during deserialization
- Better understanding in Singleton Design Principle

---

## Annotations

### What is an Annotation?

- An annotation in Java is metadata (extra information) about your code.
- They do not change the execution directly, but compilers, tools, or frameworks can use them to generate code, enforce rules, or provide behavior.

**Example:**

```java
@Override
public String toString() {
    return "Hello";
}
```

Here, @Override tells the compiler this method must override a superclass method.

### Built-in Annotations in Java

**Common Annotations**

1. **@Override** → Checks if method overrides a superclass method.
2. **@Deprecated** → Marks code as deprecated.
3. **@SuppressWarnings** → Tells compiler to ignore specific warnings.
4. **@FunctionalInterface** → Ensures the interface has exactly one abstract method.

**Meta-Annotations (annotations applied to annotations)**

- **@Target** → Where the annotation can be applied (method, field, class, etc.).
- **@Retention** → How long the annotation is retained:
  - **SOURCE** → discarded at compile time (e.g., @Override).
  - **CLASS** → kept in .class file but ignored by JVM.
  - **RUNTIME** → available via reflection at runtime (used in frameworks).
- **@Inherited** → Marks annotation as inheritable by subclasses.
- **@Documented** → Marks annotation for inclusion in Javadoc.

### Custom Annotations

You can define your own annotations:

```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)   // Available at runtime
@Target(ElementType.METHOD)           // Can be applied on methods only
public @interface MyAnnotation {
    String value();                   // Annotation element
    int version() default 1;          // Element with default value
}
```

**Usage:**

```java
public class Test {
    @MyAnnotation(value = "Hello", version = 2)
    public void sayHello() {
        System.out.println("Hello with annotation!");
    }
}
```

### Accessing Annotations via Reflection
- Accessed via reflection at runtime if @Retention(RUNTIME) is used.
```java
import java.lang.reflect.Method;

public class AnnotationReader {
    public static void main(String[] args) throws Exception {
        Method method = Test.class.getMethod("sayHello");

        if (method.isAnnotationPresent(MyAnnotation.class)) {
            MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
            System.out.println("Value: " + annotation.value());
            System.out.println("Version: " + annotation.version());
        }
    }
}
```

**Output:**

```
Value: Hello
Version: 2
```

**Real-World Use Cases**

1. **JUnit**

    ```java
    @Test
    public void testMethod() { ... }
    ```

   Tells JUnit to treat this as a test case.

2. **Spring**

    ```java
    @Autowired
    private UserService service;
    ```

   Spring injects a dependency at runtime.

3. **Hibernate / JPA**

    ```java
    @Entity
    @Table(name = "users")
    public class User { ... }
    ```

   Marks this class as a DB entity.

4. **Servlets**

    ```java
    @WebServlet("/home")
    public class HomeServlet extends HttpServlet { ... }
    ```

**Annotations vs Marker Interfaces**

- **Marker interface** → tagging done at type level, checked by instanceof.
- **Annotations** → more flexible, can be applied at methods, fields, classes, etc., and can carry extra metadata.

---

## Reflection API

### Accessing Metadata

```java
Class<?> clazz = Class.forName("Employee");
Method method = clazz.getDeclaredMethod("getName");
method.setAccessible(true);
Object result = method.invoke(emp);
```

**Use Cases:** Frameworks, Test Automation, Serialization tools

**Drawbacks:**

- Slower than direct method calls
- Security manager can restrict access

---

## Enums (Advanced Use)

### Enums with Methods

```java
public enum Operation {
    ADD { public int apply(int x, int y) { return x + y; } },
    SUB { public int apply(int x, int y) { return x - y; } };
    public abstract int apply(int x, int y);
}
```

### EnumSet/EnumMap

- High-performance specialized collections for enums

### Strategy Pattern

- Enums encapsulate behaviors without if-else

### ENUM and interface implementation

In Java an enum can implement interfaces.

#### Why is it possible?

- An enum in Java is just a special class that extends java.lang.Enum.
- Since classes can implement interfaces, so can enums.
- But enums cannot extend another class (because they already extend Enum).

#### Example 1: Enum implementing an interface

```java
interface Operation {
    int apply(int a, int b);
}

enum CalculatorOperation implements Operation {
    ADD {
        public int apply(int a, int b) {
            return a + b;
        }
    },
    SUBTRACT {
        public int apply(int a, int b) {
            return a - b;
        }
    },
    MULTIPLY {
        public int apply(int a, int b) {
            return a * b;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(CalculatorOperation.ADD.apply(5, 3));      // 8
        System.out.println(CalculatorOperation.SUBTRACT.apply(5, 3)); // 2
        System.out.println(CalculatorOperation.MULTIPLY.apply(5, 3)); // 15
    }
}
```

#### Example 2: Using marker interface

```java
interface Marker {}

enum Status implements Marker {
    ACTIVE,
    INACTIVE
}
```

Here, the enum doesn’t define any method but still implements the interface.

---

### What is a Marker Interface?

- A marker interface is an interface with no methods or fields.
- Its only purpose is to “mark” or “tag” a class, so the runtime (JVM) or frameworks can treat it specially.
- Example in JDK:
  - Serializable (marks a class whose objects can be serialized).
  - Cloneable (marks a class that supports clone()).

Marker interfaces are a way of adding metadata to a class (before Java introduced annotations).

#### Example 1: Built-in marker interface (Serializable)

```java
import java.io.*;

class Person implements Serializable {   // Marker interface
    private String name;
    Person(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}

public class MarkerDemo {
    public static void main(String[] args) throws Exception {
        Person p = new Person("Nikhil");

        // Serialize (convert object into byte stream)
        FileOutputStream fos = new FileOutputStream("person.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(p);
        oos.close();

        // Deserialize (read back object)
        FileInputStream fis = new FileInputStream("person.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Person deserialized = (Person) ois.readObject();
        ois.close();

        System.out.println("Deserialized: " + deserialized.getName());
    }
}
```

Here:

- Person implements Serializable but no methods are required.
- JVM checks: “Oh, this class is Serializable → allow serialization.”
- If you don’t mark it, you’ll get NotSerializableException.

#### Example 2: Custom marker interface

Let’s say you want to mark classes that are “important”.

```java
// Custom marker interface
interface Important { }

// Two classes
class Report implements Important {
    // some code
}

class Draft {
    // some code
}

public class MarkerDemo2 {
    public static void main(String[] args) {
        checkImportance(new Report());
        checkImportance(new Draft());
    }

    public static void checkImportance(Object obj) {
        if (obj instanceof Important) {
            System.out.println(obj.getClass().getSimpleName() + " is IMPORTANT!");
        } else {
            System.out.println(obj.getClass().getSimpleName() + " is not important.");
        }
    }
}
```

**Output**

```
Report is IMPORTANT!
Draft is not important.
```

Here:

- Important interface has no methods.
- But by marking Report with it, we can use instanceof to treat it differently at runtime.

#### Why use marker interfaces?

1. Metadata before annotations: Before Java 5 annotations, marker interfaces were the only way to give “extra information” to the JVM or frameworks.
2. Type safety: Unlike annotations, marker interfaces participate in type checking at compile time.
  - Example: You cannot accidentally pass a Draft into a method expecting Important.
3. Framework usage: Many libraries check for marker interfaces to apply special behavior.

#### Marker Interfaces vs Annotations

- Today, annotations (like @Deprecated, @Override, @Entity) are more common.
- Marker interfaces are still relevant when you want type checking along with marking.

#### Key Points

1. An enum can implement one or more interfaces.
2. If the interface has abstract methods, each enum constant must implement them (unless the enum provides a common implementation).
3. This is often used in strategy-like patterns.

### Enums implementing marker interfaces

Enums can implement marker interfaces just like classes.

#### Use cases

1. Tagging / grouping enums for common treatment.
2. Framework support (serialization, persistence, reflection-based logic).
3. Type safety → restrict methods to accept only certain enums.

```java
// Marker interface
interface CategoryMarker {}

// Two enums implementing it
enum Fruit implements CategoryMarker {
    APPLE, ORANGE, BANANA
}

enum Animal implements CategoryMarker {
    DOG, CAT, LION
}

class CategoryPrinter {
    public static void printCategory(CategoryMarker marker) {
        System.out.println("Category: " + marker);
    }
}

public class TestEnumMarker {
    public static void main(String[] args) {
        CategoryPrinter.printCategory(Fruit.APPLE);  // ✅ allowed
        CategoryPrinter.printCategory(Animal.DOG);   // ✅ allowed
        // CategoryPrinter.printCategory("Hello");   // ❌ compile error
    }
}
```

---

## Functional Programming Concepts

### Pure Functions

A pure function:

- Has no side effects (does not modify global state or parameters).
- Always returns the same output for the same input.

**Example:**

```java
int square(int x) {
    return x * x;
}
```

**Follow-up Questions:**

- *Why are pure functions important in multi-threading?*  
  Because they’re thread-safe by design—no shared mutable state.

---

### Higher-order Functions

These are functions that:

- Take other functions as arguments.
- Or return functions as results.

**Example:**

```java
Function<Integer, Integer> square = x -> x * x;
Function<Integer, Integer> doubler = x -> x * 2;

List<Integer> result = list.stream().map(square).collect(Collectors.toList());
```

**Follow-up:**

- *What Java features support higher-order functions?*  
  Lambdas and functional interfaces like `Function<T,R>`, `Consumer<T>`, `Supplier<T>`.

---

### Currying and Function Composition

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

- *How do you compose predicates?*  
  Using `Predicate#and`, `Predicate#or`.

---

### Functional vs Imperative Style

- **Imperative:** You tell how to do it (loops, control flow).
- **Functional:** You declare what to do (Streams, lambdas).

**Example:**

```java
// Imperative
int sum = 0;
for (int n : list) sum += n;

// Functional
int sum = list.stream().mapToInt(Integer::intValue).sum();
```

---

## Best Practices & Code Quality

### Clean Code Principles

- Meaningful names
- Small, focused methods
- Single Responsibility Principle
- Avoid duplication

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

### Defensive Programming

- Check for nulls
- Validate inputs
- Fail fast with informative errors

**Example:**

```java
Objects.requireNonNull(userId, "User ID must not be null");
```

---

### Design for Testability

- Avoid static methods
- Inject dependencies
- Separate concerns
- Return predictable outputs

---

### Idiomatic Java

- Use enhanced `for` loop or streams
- Prefer `StringBuilder` for concatenation
- Use `Optional` instead of nulls when suitable

---

## Bonus Interview Topics

---

### ✅ `final`, `static`, `transient`, `volatile`, `synchronized`

- `final`: Can’t reassign (variables), extend (classes), override (methods)
- `static`: Belongs to class, not instance
- `transient`: Skips field during serialization
- `volatile`: Ensures visibility across threads
- `synchronized`: Ensures atomicity and mutual exclusion

---

### Boxing/Unboxing Pitfalls

```java
Integer a = 128;
Integer b = 128;
System.out.println(a == b); // false (different objects)
```

Use `.equals()` for comparison.

---

### String Intern Pool

- String literals are interned by default.
- `String.intern()` adds strings to the pool.

**Example:**

```java
String a = "abc";
String b = new String("abc").intern();
System.out.println(a == b); // true
```

---

### StringBuilder vs StringBuffer vs `+`

- `StringBuilder`: Fast, not thread-safe.
- `StringBuffer`: Thread-safe but slower.
- `+`: Concatenation (converted to StringBuilder internally in most cases).

---

### Compile-time vs Runtime Polymorphism

- **Compile-time**: Method overloading
- **Runtime**: Method overriding via inheritance

---

### Java Agent Basics

Used for instrumentation and profiling.

**Key Methods:**

```java
public static void premain(String args, Instrumentation inst) {
    // Modify bytecode before main()
}
```


---

## Follow-Up Interview Questions (Answered)

1. **How does the JVM handle class unloading?**
  - Classes are unloaded when their classloader becomes unreachable and GC collects it. Mostly happens in dynamic environments (e.g., Tomcat).
2. **Can you override equals() without hashCode()?**
  - Yes, but violates contract. Hash-based collections (HashMap/HashSet) will misbehave—equal objects may go into different buckets.
3. **How would you implement a cache using EnumMap?**
  - If keys are enums:
    ```java
    enum Status { ACTIVE, INACTIVE }
    EnumMap<Status, String> cache = new EnumMap<>(Status.class);
    ```
  - Faster and memory-efficient vs HashMap.
4. **Explain use cases of sealed classes in domain modeling.**
  - Domain boundaries (e.g., only Circle, Rectangle are valid Shape types). Prevents arbitrary extension and improves pattern matching.
5. **What are the performance implications of Reflection?**
  - Slower than direct calls due to dynamic dispatch. Also breaks compiler optimizations and introduces security risks.
6. **How does Java ensure backward compatibility in Serialization?**
  - Via serialVersionUID. If the UID matches between sender/receiver class, deserialization succeeds even if class evolved.
7. **How is ClassLoader hierarchy useful for application isolation?**
  - Different classloaders load the same class name in isolated ways, allowing independent libraries/apps in the same JVM (e.g., servlet containers).
8. **What is the difference between compile-time and runtime polymorphism?**
  - Compile-time: Overloading; resolved at compile-time. Runtime: Overriding; resolved via dynamic dispatch.
9. **How do records compare to traditional POJOs?**
  - Records auto-generate boilerplate (constructor, getters, equals/hashCode, toString) and are immutable by default.
10. **What are the trade-offs of immutability in large-scale systems?**
  - Pros: Thread-safety, predictability, cache-safe. Cons: Object creation overhead, more GC pressure, harder modeling for certain mutable domains.

---

# String... (varargs)

**1. What is String...?**
- String... (read as “String varargs”) is a variable-length argument syntax introduced in Java 5.
- It allows a method to accept zero or more arguments of the specified type.

- Essentially, String... is syntactic sugar for an array of Strings (String[]), but it gives flexibility when calling methods — you can pass:
  -	No argument,
  -	A single argument, or
  -	Multiple arguments.

⸻

**Basic Example**
```java
public class VarargsExample {
public static void printNames(String... names) {
for (String name : names) {
System.out.println(name);
}
}

    public static void main(String[] args) {
        printNames();                          // No arguments
        printNames("Nikhil");                  // One argument
        printNames("Nikhil", "Rahul", "Aman"); // Multiple arguments
    }
}
```

Output:

```
Nikhil
Rahul
Aman
```


⸻

**What Happens Internally**
-	The String... parameter is compiled as a String[].
-	So internally, the method signature becomes:
```java
public static void printNames(String[] names)
```

-	When you call printNames("Nikhil", "Rahul"), the compiler automatically creates a String[] array:
```java
printNames(new String[] { "Nikhil", "Rahul" });
```


⸻

**Key Rules for Varargs**
1.	Only one varargs parameter per method is allowed.
```java
public void method(String... a, int... b); // ❌ Not allowed
```

2.	The varargs parameter must be the last parameter in the method signature.
```java
public void method(String fixed, String... others); // ✅ Allowed
public void method(String... others, String fixed); // ❌ Not allowed
```

3.	You can pass either:
-	Individual arguments: method("a", "b", "c")
-	An array explicitly: method(new String[] {"a", "b", "c"})

⸻

**Real-World Example**

Logging Utility
```java
public class Logger {
public static void log(String message, Object... params) {
System.out.println(String.format(message, params));
}

    public static void main(String[] args) {
        log("User %s logged in at %s", "Nikhil", "10:00 AM");
    }
}
```
Output:
```
User Nikhil logged in at 10:00 AM
```
Here, Object... params allows any number of placeholders to be replaced dynamically.

⸻

**Comparison: Varargs vs Array**

| Feature                  | Varargs (String...)           | Array (String[])                  |
|--------------------------|-------------------------------|-----------------------------------|
| Call syntax              | method("a", "b", "c")         | method(new String[]{"a", "b", "c"}) |
| Can pass 0 arguments?    | Yes                           | No (need empty array)             |
| Cleaner syntax           | ✅                            | ❌                                |
| Internally represented as| Array                         | Array                             |


⸻

**When to Use**

Use varargs when:
-	You want to pass variable number of parameters.
-	Method arguments are of same type.
-	Example use cases:
-	Logging (Logger.info("msg", args...))
-	String concatenation
-	SQL query parameter binding

⸻

**Best Practices**
1.	Keep varargs as the last parameter.
2.	Avoid using varargs when:
      -	Performance is critical (creates an array each call).
      -	Parameter count is known or fixed.
3.	Combine with annotations like @SafeVarargs to avoid warnings in generics.

⸻


# Shared Object Reference (Shared Mutable State)
- Object Identity Sharing

- Both users are operating on the same object identity, not copies.

- This matters because:
  - Locks work per object
  -	Synchronization works only if the object is shared
```bash
// Thread/User 1
BankAccount a1 = accountRepo.get(1);
BankAccount a2 = accountRepo.get(2);

// Thread/User 2
BankAccount b1 = accountRepo.get(1);
BankAccount b2 = accountRepo.get(2);

// Truth
a1 == b1   // true
a2 == b2   // true
```



================================================

Advanced:

### Q1. So, if I am making everything in my class final, variables, methods (so I cant not override methods now), why do I need to make the class final?
### Answer

1. Subclass constructors can break guarantees (even if everything is final)

Base class (looks immutable & safe)

class Money {
private final int amount;

    public Money(int amount) {
        this.amount = amount;
    }

    public final int getAmount() {
        return amount;
    }
}

Looks safe:
-	final field
-	final method
-	No setters

⸻

Subclass introduces dangerous behavior

class EvilMoney extends Money {
public EvilMoney(int amount) {
super(amount);
GlobalRegistry.register(this);
}
}


⸻

What exactly breaks?

The problem: this escapes during construction

Object creation steps:
1.	Memory allocated
2.	Base constructor starts
3.	Base fields initialized
4.	Control returns to subclass constructor
5.	this is published to other threads

Another thread may now see:
-	Object reference
-	But not guaranteed visibility of all final fields

Why this matters

The Java Memory Model guarantees final field visibility ONLY if the constructor finishes normally and object does not escape during construction.

Subclass constructors can violate that guarantee.

⸻

Result

Even though fields are final, another thread could see:

money.getAmount() == 0   // default value

This is extremely subtle and very real.

⸻

Why final class prevents this

public final class Money { ... }

No subclass → no subclass constructor → no escape → guarantee preserved.

⸻

2. Subclasses can add mutable state (breaks immutability contract)

Base class (advertised as immutable)

class User {
private final String name;

    public User(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}

Team assumes:

“User is immutable”

⸻

Subclass adds mutable state

class MutableUser extends User {
private int loginCount;

    public MutableUser(String name) {
        super(name);
    }

    public void increment() {
        loginCount++;
    }
}


⸻

What exactly breaks?

Liskov Substitution Principle (LSP)

Code expects:

User user = getUser();

Assumption:
-	Thread-safe
-	Immutable
-	Safe to cache
-	Safe to share

But at runtime:

user instanceof MutableUser

Now:
-	Shared mutable state exists
-	Thread safety assumptions are violated
-	Caching becomes unsafe

⸻

Real production bug example

static final Map<User, Session> cache = new HashMap<>();

Mutable subclass mutates internal state → equality assumptions break → cache corruption.

⸻

Why final class prevents this

No subclass → no hidden mutable state → immutability guarantee holds globally.

⸻

3. Equality & hashCode contracts can silently break

Base class defines equality

class Point {
private final int x;
private final int y;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(x, y);
    }
}


⸻

Subclass adds new dimension

class ColoredPoint extends Point {
private final String color;

    public ColoredPoint(int x, int y, String color) {
        super(x, y);
        this.color = color;
    }
}


⸻

What exactly breaks?

Symmetry violation

Point p = new Point(1, 2);
ColoredPoint cp = new ColoredPoint(1, 2, "RED");

p.equals(cp)  // true
cp.equals(p)  // false (color not compared)

This violates:
-	equals symmetry rule
-	hashCode consistency

⸻

Consequences
-	HashMap behavior becomes undefined
-	Sets contain duplicates
-	Caches misbehave

⸻

Why final class prevents this

No subclass → equality logic remains complete → contracts preserved.

⸻

4. Subclasses can violate thread-safety assumptions

Base class used as thread-safe value

class Config {
private final String url;

    public Config(String url) {
        this.url = url;
    }

    public final String getUrl() {
        return url;
    }
}

Shared across threads safely.

⸻

Subclass introduces race condition

class MutableConfig extends Config {
private String temp;

    public void setTemp(String t) {
        temp = t;
    }
}

Now:
-	Reads + writes
-	No synchronization
-	Data race introduced

⸻

Breakage
-	Base class was assumed thread-safe
-	Subclass invalidates that assumption
-	Bugs appear far from the source

⸻

5. Security assumptions can be broken

Base security-sensitive class

class AuthToken {
private final String token;

    public final boolean isValid() {
        return token != null && token.length() > 10;
    }
}


⸻

Subclass manipulates behavior indirectly

Even without overriding isValid, subclass can:
-	Add state
-	Leak token
-	Store it insecurely
-	Break audit expectations

Security-sensitive classes must not be extensible.

That’s why:
-	String
-	Integer
-	ClassLoader

are all final.

⸻

6. Why final methods + final fields is NOT enough

Protection	What it stops	What it doesn’t
final fields	Reassignment	Constructor escape
final methods	Overriding	New behavior
final class	Subclassing	Everything above

You need all three to fully lock the design.

⸻

7. Why Java Records solve this cleanly

public record Money(int amount) {}

Records are:
-	Final
-	Fields final
-	No subclassing
-	Immutable by default

Java added records because this pattern was so error-prone manually.

⸻

8. Interview-level summary (very important)

Even if all fields and methods are final, allowing subclassing still permits constructor escape, hidden mutable state, broken equality contracts, and violated thread-safety assumptions. Making the class final closes these extension loopholes and preserves immutability guarantees.

⸻

Mental rule to lock forever

Immutability is a design guarantee, not just a syntax choice.
Final classes enforce the guarantee globally.

⸻

If you want next, we can:
-	Show a real HashMap corruption demo
-	Explain why records are better than builders sometimes
-	Compare sealed vs final
-	Tie this to Spring proxy limitations

Just say which direction you want to go.


------------

## ClassLoaders

A ClassLoader in Java is a part of the Java Runtime Environment (JRE) that dynamically loads classes into memory when they are required.

- Java doesn’t load all classes upfront — it loads them on demand using ClassLoaders.
- ClassLoaders also define the namespace of a class (i.e., where it belongs in the JVM).

### Types of ClassLoaders

Java uses a delegation hierarchy model for class loading:

1. **Bootstrap ClassLoader (Primordial ClassLoader)**
- Written in native code (C/C++).
- Loads core Java classes (java.lang.*, java.util.*, etc.) from rt.jar (or jmods in Java 9+).
- Has no parent.
2. **Extension (Platform) ClassLoader**
- Loads classes from the extension directories (jre/lib/ext or modules in newer Java).
- Example: classes for cryptography or XML processing.
3. **System (Application) ClassLoader**
- Loads classes from the classpath (-cp or CLASSPATH env variable).
- This is the default ClassLoader for user-defined classes.
4. **Custom ClassLoaders**
- You can create your own by extending ClassLoader.
- Useful in frameworks (Spring, Hibernate, Tomcat) and application servers to load/unload classes dynamically (plugins, hot deployment).

### Delegation Model (How ClassLoaders Work)

- A ClassLoader first delegates the request to its parent before trying to load a class itself.
- This prevents duplicate loading of core classes and maintains security.

**Example:**

If you try to load java.lang.String with your custom ClassLoader:

1. Delegated to Bootstrap ClassLoader → finds it → returns.
2. Your ClassLoader never gets the chance to override it.

### Why are ClassLoaders important?

- Frameworks & Containers (Spring Boot, Tomcat, OSGi) → load/unload classes dynamically.
- Plugins → allow hot deployment without restarting JVM.
- Security → prevents overriding of core classes.
- Interview favorite → ties into JVM internals, reflection, and custom frameworks.

### Custom ClassLoader

**Steps**

1. Create a normal Java class (to be loaded dynamically).
2. Write a CustomClassLoader by extending ClassLoader.
3. Use it to load the .class file at runtime.

**1. A Simple Class (to be loaded)**

Save this as Hello.java and compile (javac Hello.java → generates Hello.class).

```java
public class Hello {
  public void sayHello() {
    System.out.println("Hello from custom class loader!");
  }
}
```

**2. Custom ClassLoader**

We’ll read the .class file as bytes, then use defineClass() to load it.

```java
import java.io.*;

public class CustomClassLoader extends ClassLoader {

    private String classPath;

    public CustomClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // Convert class name to file system path
            String fileName = classPath + name.replace('.', '/') + ".class";

            // Read class file bytes
            byte[] bytes = loadClassData(fileName);

            // Define the class in JVM
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] loadClassData(String fileName) throws IOException {
        InputStream input = new FileInputStream(fileName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int data;
        while ((data = input.read()) != -1) {
            buffer.write(data);
        }
        input.close();

        return buffer.toByteArray();
    }
}
```

**3. Using the Custom ClassLoader**

Now create a runner class:

```java
public class LoaderTest {
    public static void main(String[] args) throws Exception {
        // Path to directory containing Hello.class
        String classPath = "/path/to/classes/";

        // Use custom class loader
        CustomClassLoader loader = new CustomClassLoader(classPath);

        // Load class dynamically
        Class<?> helloClass = loader.loadClass("Hello");

        // Create instance
        Object obj = helloClass.getDeclaredConstructor().newInstance();

        // Call method via reflection
        helloClass.getMethod("sayHello").invoke(obj);
    }
}
```

**Output**

```
Hello from custom class loader!
```

**Where this is useful**

- Application Servers (Tomcat, JBoss) → load webapps with separate classloaders.
- Frameworks (Spring Boot, OSGi) → load/unload plugins dynamically.
- Hot Reloading → reload a class without restarting JVM.

### What happens if the same class is loaded by two different ClassLoaders?

**Class Identity in Java**

In the JVM, a class is uniquely identified not just by its name (e.g., com.example.MyClass) but by:

```
<class name, defining ClassLoader>
```

That means:

- If two different ClassLoaders load a class with the same name and package, the JVM treats them as two completely different classes.
- Even if the .class bytecode is identical, they live in different namespaces.

**Example**

Imagine you have Hello.class. If you load it with two different custom ClassLoaders:

```java
CustomClassLoader loader1 = new CustomClassLoader("/path/to/classes/");
CustomClassLoader loader2 = new CustomClassLoader("/path/to/classes/");

Class<?> class1 = loader1.loadClass("Hello");
Class<?> class2 = loader2.loadClass("Hello");

System.out.println(class1 == class2); // false
```

Even though both refer to Hello, they are not equal because loader1 ≠ loader2.

**Consequence**

This can cause:

- ClassCastException

```java
Object obj = class1.newInstance();
class2.cast(obj); // Throws ClassCastException!
```

Because JVM thinks Hello from loader1 ≠ Hello from loader2.

**Why this Matters**

- Application Servers (Tomcat, JBoss, WebSphere, etc.)
    - Each web application runs in its own ClassLoader.
    - This prevents class conflicts between different apps using different versions of the same library.
- Plugins / OSGi
    - Allows loading multiple versions of the same library at runtime.
- Hot Reloading
    - You can unload and reload a new version of a class by discarding the old ClassLoader and creating a new one.

**Parent Delegation Model Recap**

- By default, a ClassLoader first asks its parent to load a class.
- This ensures core classes (java.lang.String) are never overridden.
- Custom loaders can break this delegation model (but it’s dangerous unless you know what you’re doing).

**Summary**

- A class is identified by (name + defining ClassLoader).
- Same class name loaded by two different ClassLoaders = different classes in JVM.
- Leads to isolation (good for modularity), but also ClassCastException pitfalls.

#### Tomcat ClassLoader Hierarchy

In Apache Tomcat, the ClassLoader hierarchy is designed to provide isolation between web applications while allowing shared access to common libraries. Here’s a simplified overview of the ClassLoader hierarchy in Tomcat:

When Tomcat starts, it builds a tree of ClassLoaders on top of the JVM ones:

1. **Bootstrap ClassLoader (JVM)**
- Loads rt.jar / core modules (java.lang.*, java.util.*, etc.).
- Part of every Java process.
2. **System (Application) ClassLoader (JVM)**
- Loads classes from the classpath ($JAVA_HOME/lib).
- Example: If you run Tomcat with java -cp, these classes are here.
3. **Tomcat-specific ClassLoaders**
- **Common ClassLoader**
    - Loads classes/jars in CATALINA_HOME/lib.
    - Shared by Tomcat internals + all deployed webapps.
    - Example: JDBC drivers, logging frameworks.
- **Catalina ClassLoader**
    - Loads Tomcat’s internal server classes (org.apache.catalina.*, etc.).
    - Isolated so webapps cannot override Tomcat’s core.
- **Shared ClassLoader**
    - (Optional, depending on Tomcat version/config).
    - Loads jars that should be shared across multiple apps but not part of Tomcat itself.
- **WebApp ClassLoader (per deployed app!)**
    - Loads classes from /WEB-INF/classes and /WEB-INF/lib/*.jar.
    - Each webapp gets its own loader → isolation.
    - Two apps can use different versions of the same library without conflicts.

**Diagram**

```
Bootstrap ClassLoader
  ↑
System (App) ClassLoader
  ↑
Common ClassLoader   ← (CATALINA_HOME/lib)
  ↑
Catalina ClassLoader  ← (Tomcat internals)
  ↑
WebApp ClassLoader(s) ← (WEB-INF/classes, WEB-INF/lib)
```

**Why this matters**

1. **Isolation**
- App A can use commons-logging-1.1.jar
- App B can use commons-logging-1.2.jar
- No conflict because they’re loaded in different WebApp ClassLoaders.
2. **Security**
- A webapp cannot override Tomcat internals (org.apache.catalina.*) since those are loaded higher up (Catalina ClassLoader).
3. **Hot Deployment**
- When you redeploy a webapp, Tomcat discards the old WebApp ClassLoader and creates a new one.
- This avoids memory leaks, but if references to old classes remain (e.g., static singletons, threads), you get PermGen/Metaspace leaks.

**Real-World Example**

Imagine you deploy two WARs:

- App1.war → WEB-INF/lib/mysql-connector-5.1.jar
- App2.war → WEB-INF/lib/mysql-connector-8.0.jar

Even though both define com.mysql.jdbc.Driver, they’re loaded by different WebApp ClassLoaders, so Tomcat keeps them separate.

If instead you put mysql-connector.jar in CATALINA_HOME/lib, both apps would share the same driver → useful for connection pooling.

**Summary**

- Tomcat builds a ClassLoader tree on top of JVM loaders.
- Each webapp has its own WebApp ClassLoader → isolation.
- Shared libs go in CATALINA_HOME/lib.
- Prevents conflicts but can cause memory leaks if class references escape after undeploy.

---