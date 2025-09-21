# Java Design Principles

## Table of Contents
1. [Singleton Pattern](#singleton-pattern)
   - [Achieving Singleton in Java](#achieving-singleton-in-java)
   - [Scenarios That Can Break Singleton](#scenarios-that-can-break-singleton)
     - [1. Breaking via Reflection](#1-breaking-via-reflection)
     - [2. Breaking via Serialization/Deserialization](#2-breaking-via-serializationdeserialization)
     - [3. Breaking via Cloning](#3-breaking-via-cloning)
     - [4. Breaking via Multithreading](#4-breaking-via-multithreading)
   - [How to Use Enum Singleton](#how-to-use-enum-singleton)
   - [Best Practices for Robust Singleton](#best-practices-for-robust-singleton)

2. [Factory Pattern](#factory-pattern)
3. [Abstract Factory Pattern](#abstract-factory-pattern)
4. [Builder Pattern](#builder-pattern)
5. [Prototype Pattern](#prototype-pattern)
6. [Observer Pattern](#observer-pattern)
7. [Decorator Pattern](#decorator-pattern)
8. [Proxy Pattern](#proxy-pattern)
9. [Strategy Pattern](#strategy-pattern)
10. [Adapter Pattern](#adapter-pattern)
11. [Facade Pattern](#facade-pattern)
12. [Command Pattern](#command-pattern)
13. [Template Method Pattern](#template-method-pattern)

### Singleton Pattern
* Ensure a class has only one instance and provide a global point of access to it.
* Useful for managing shared resources like configuration settings or database connections.
* `Example:` Database connection pool manager.
* `Example:` Logger class to ensure consistent logging across the application.

**How to achieve Singleton in Java:**
To achieve a **Singleton** in Java, a class is structured so that only one instance can ever exist, with a global access point to that instance. However, this Singleton guarantee can be broken by several mechanisms if not protected carefully. Here is how Singleton is implemented, how it can be broken, and strategies to prevent such breakage[1][2][3][4].

### Achieving Singleton in Java

Common steps for a thread-safe Singleton:
- **Private constructor**: Prevents direct instantiation.
- **Private static instance**: Holds the single class instance.
- **Public static getInstance() method**: Returns the single instance, often with lazy initialization and thread safety measures.

**How it works:**
- Since no constructor is accessible outside the class, no other class can create an instance.
- The static method ensures that only one instance is created and returned.
- If two threads call getInstance() simultaneously in a lazy-initialized singleton, multiple objects can be created.
    - Use thread-safe initialization techniques
    - Double-checked locking with volatile Static inner helper class (Bill Pugh Singleton)

Example (Lazy Initialization with thread safety):
```java
public class Singleton {
    // Volatile variable to ensure visibility of changes across threads
    private static volatile Singleton instance;

    // Private constructor prevents instantiation from outside
    private Singleton() {}

    public static Singleton getInstance() {
        // Double-checked locking for thread safety
        if (instance == null) {
            synchronized (Singleton.class) {
                // Check again within synchronized block
                if (instance == null)
                    instance = new Singleton();
            }
        }
        return instance;
    }
}
```

```java
Singleton.getInstance();
```

`Double-checked locking with volatile Static inner helper class (Bill Pugh Singleton)`

**How it works:**
- The Singleton instance is held inside a private static inner helper class.
- The inner helper class is not loaded into memory until the getInstance() method is called for the first time.
- When the getInstance() method is called, the inner class is loaded, and the Singleton instance is created.
- JVM ensures that class loading is thread-safe, so this approach avoids synchronization overhead.

1. Class Loading in Java
    - In Java, classes are not loaded into memory until they are first referenced.
    - That means the inner static class (Holder) is not loaded when the outer class (Singleton) is loaded.
    - Instead, it is loaded only when you call getInstance() for the first time.

2. Lazy Initialization
    - The INSTANCE object is created inside the Holder class.
    - Since Holder is only loaded when needed (first call to getInstance()), the Singleton instance is created lazily (on-demand).
    - If you never call getInstance(), the Singleton object is never created.

3. Thread Safety
    - The JVM ensures class initialization is thread-safe.
    - That means when Holder is loaded, its static fields are initialized in a thread-safe manner, without needing synchronized or double-checked locking.

```java
public class Singleton {
    // Private constructor prevents instantiation from outside
    private Singleton() {}

    // Static inner helper class holding the Singleton instance
    private static class SingletonHelper {
        private static final Singleton INSTANCE = new Singleton();
    }

    // Public method to provide access to the Singleton instance
    public static Singleton getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
```

**Key Advantages**
- Lazy Initialization: The Singleton instance is created only when needed.
- Thread Safety: The JVM class loader guarantees thread-safe loading of the inner class.
- No Synchronization Overhead: Unlike synchronized `getInstance()` methods, this is efficient and scalable.

### Scenarios That Can Break Singleton

| Breaking Method          | How Singleton is Broken                                           | Solution                                                   |
|-------------------------|-------------------------------------------------------------------|------------------------------------------------------------|
| Reflection              | Reflection can access private constructors and create new objects | Throw exception inside constructor if instance already exists[5][6][7]   |
| Serialization/Deserialization | Deserialization creates a new instance separate from the original  | Implement `readResolve()` to return the existing instance[8][6][7]         |
| Cloning                 | Cloning can create a copy if `Cloneable` is implemented           | Override `clone()` to return the existing instance or throw exception[6][7] |
| Multithreading          | Race conditions may create multiple instances                     | Use synchronized block, double-checked locking, or enum[3][4]                    |

#### 1. Breaking via Reflection
Reflection bypasses private constructor restrictions:
```java
Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
constructor.setAccessible(true);
Singleton instance2 = constructor.newInstance();
```
**Solution**: Throw an exception in the constructor if an instance is already created:
```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to create");
        }
    }

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```


#### 2. Breaking via Serialization/Deserialization
Serialization creates a new object instance on deserialization:
```java
ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("file.obj"));
oos.writeObject(Singleton.getInstance());

ObjectInputStream ois = new ObjectInputStream(new FileInputStream("file.obj"));
Singleton instance2 = (Singleton) ois.readObject(); // New instance!
```
```java
protected Object readResolve() {
    return getInstance();
}
```
This method ensures that deserialization returns the existing instance

#### 3. Breaking via Cloning
If `Cloneable` is implemented, cloning can create a new object:
```java
Singleton instance2 = (Singleton) instance.clone();
```
Fix: Override clone() to prevent cloning.
```java
@Override
protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException("Singleton cannot be cloned");
}
```
Or, return the existing instance from `clone()`

#### 4. Breaking via Multithreading
Without proper synchronization, multiple threads could create multiple instances. Use double-checked locking or `enum` Singleton:

**How to Use Enum Singleton**

- Step 1: Define Singleton with Enum
```java
public enum Singleton {
    INSTANCE;  // Single instance

    public void showMessage() {
        System.out.println("Hello from Enum Singleton!");
    }
}
```

- Step 2: Access Singleton
```java
public class TestEnumSingleton {
    public static void main(String[] args) {
        Singleton singleton1 = Singleton.INSTANCE;
        Singleton singleton2 = Singleton.INSTANCE;

        singleton1.showMessage();

        System.out.println("Are both instances same? " + (singleton1 == singleton2));
    }
}
```

- Step 3: Store State if Needed

You can also store fields and methods just like a normal class.

```java
public enum DatabaseConnection {
    INSTANCE;

    private Connection connection;

    DatabaseConnection() {
        try {
            // Initialize connection once
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
```

```java
public class TestDB {
    public static void main(String[] args) {
        Connection conn1 = DatabaseConnection.INSTANCE.getConnection();
        Connection conn2 = DatabaseConnection.INSTANCE.getConnection();

        System.out.println("Same connection? " + (conn1 == conn2));
    }
}
```
**Advantages of Enum Singleton:**
- Enums are immune to serialization and reflection, cloning  issues. The Java language guarantees that any enum value is instantiated only once in a Java program. This makes it a robust way to implement Singleton.
- Thread-safe by default: Enum instances are created when the enum class is loaded, ensuring thread safety without additional synchronization.
- Simplicity: The syntax is concise and easy to understand.

### Best Practices for Robust Singleton

- Use **enum Singleton** for simple and robust pattern.
- Defend against reflection and serialization in the private constructor and with `readResolve()`.
- Control cloning by overriding `clone()` appropriately.
- Use thread-safe instantiation (double-checked locking or static holder)

This ensures Singleton’s one-instance guarantee even in advanced scenarios and hostile environments

---