Comparab'le' -> Natur'al' -> a.compareTo(b) (implements Comparable interface)
Comparat'or' -> iterat'or' -> compare() (implements Comparator interface)

map.forEach(
list -> max = Math.max(list.size(), max)
); ??????


- **Fail-Fast iterators immediately throw `ConcurrentModificationException` if there is **structural modification** of the collection.**
- **Structural modification means adding, removing or updating any element from collection while a thread is iterating over that collection.** 
- **Iterator on CopyOnWriteArrayList, ConcurrentHashMap classes are examples of fail-safe Iterator.**
- **WeakHashMap: An entry in a WeakHashMap will automatically be removed when its key is no longer in ordinary use.**
- **IdentityHashMap: Similar to hashmap but uses reference instead of hashcode and equals**

**SpringBoot LifeCycle:**
1. psvm, application.run
2. prepare environment
3. listener and initaliser rus
4. logs startup banner
5. application context created
6. @ComponentSCan - beans registered, scans component
7. @Enable Auto Configuration - imports from meta-inf
8. applies conditions to these decide which beans to load
9. DI
10. server starup
```text
JVM starts
↓
Classpath prepared (all pom.xml dependencies loaded)
↓
SpringApplication.run()
↓
ApplicationContext creation
↓
Configuration processing
↓
Bean definition phase
↓
Bean instantiation phase
↓
Application ready
```

- Using @Component instead of @Configuration wih @Bean, this will result in a new instance everytime, bcoz new ObjectMapper(), for singleton, @Configuration

---

## Serialization & Deserialization

## Transient
The transient keyword is used to prevent sensitive or temporary instance variables from being serialized. When a field is marked as transient, its value is not saved to the serialization stream. During deserialization, the transient field is set to its default value (null, 0, false depending on type) rather than its original value.

### Static Fields

- Static fields are class variables, shared across all instances and belonging to the class itself, not individual objects.
- Because serialization captures the state of an object, and **static variables are not part of individual object state, they are never serialized**.

- Objects referenced by a serializable class must also be serializable; otherwise, NotSerializableException is thrown.


---

## Reflection API

### Accessing Metadata

```java
Class<?> clazz = Class.forName("Employee");
Method method = clazz.getDeclaredMethod("getName");
method.setAccessible(true);
Object result = method.invoke(emp);
```

## Enum

### Strategy Pattern

- Enums encapsulate behaviors without if-else

### ENUM and interface implementation

In Java an enum can implement interfaces.

#### Why is it possible?

- An enum in Java is just a special class that extends java.lang.Enum.
- Since classes can implement interfaces, so can enums.
- But enums cannot extend another class (because they already extend Enum).

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

### StringBuilder vs StringBuffer vs `+`

- `StringBuilder`: Fast, not thread-safe.
- `StringBuffer`: Thread-safe but slower.
- `+`: Concatenation (converted to StringBuilder internally in most cases).


---

### Compile-time vs Runtime Polymorphism

- **Compile-time**: Method overloading
- **Runtime**: Method overriding via inheritance

---

3. **How would you implement a cache using EnumMap?**
- If keys are enums:
  ```java
  enum Status { ACTIVE, INACTIVE }
  EnumMap<Status, String> cache = new EnumMap<>(Status.class);
  ```
  
----

