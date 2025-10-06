# Bean Lifecycle in Spring Framework

1. **Bean definition**
2. **Bean initialization**
3. **Bean usage**
4. **Bean destruction**

---

## BeanDefinition

### What is a BeanDefinition?

- A `BeanDefinition` is metadata: a description of a Spring bean.
- It is not the bean instance. It tells the container how to create/configure/manage a bean: which class, what scope, constructor args, property values, lifecycle callbacks, qualifiers, etc.
- Think of a `BeanDefinition` as a recipe; the container reads recipes and later uses them to actually bake (instantiate) beans.

### Core pieces of information in a BeanDefinition

A `BeanDefinition` (usually an `AbstractBeanDefinition` implementation) typically holds: (No need to remember)

- `beanClassName` / `beanClass` — the class (or its name) that will be instantiated.
- `scope` — "singleton" or "prototype" (and web scopes like request/session).
- `isLazyInit` — whether to delay instantiation until first request.
- `isPrimary` — whether it’s the primary candidate for autowiring.
- `autowireCandidate` — can this bean be autowired into others?
- `constructorArgumentValues` — arguments to call the constructor with.
- `propertyValues` — values to set on bean properties after construction.
- `factoryBeanName` / `factoryMethodName` — if bean is created via factory method.
- `initMethodName` / `destroyMethodName` — lifecycle callback names.
- `dependsOn` — beans that must be initialized first.
- `role` — `ROLE_APPLICATION` / `ROLE_SUPPORT` / `ROLE_INFRASTRUCTURE` (helps tooling).
- `resourceDescription` / source — where the definition came from (file/class).
- `abstract` flag — whether it’s abstract (not instantiable).
- `qualifiers` / custom attributes — `@Qualifier`/custom metadata used for resolution.
- `synthetic` — internal framework-created bean.

These fields let the container know how to build and manage the bean.

**Common BeanDefinition implementations**

Spring provides several implementations you’ll see in different flows:

- `GenericBeanDefinition` — general purpose definition you can create programmatically.
- `RootBeanDefinition` — a “merged” or primary form used by the factory during instantiation.
- `ChildBeanDefinition` — inherits from a parent bean definition (legacy).
- `AnnotatedGenericBeanDefinition` / `ScannedGenericBeanDefinition` — created from `@Component`/annotation scanning; they keep annotation metadata.
- `ConfigurationClassBeanDefinition` variants — for `@Configuration` and `@Bean` processing.

---

### How BeanDefinitions are created & registered (common paths)

1. **XML config**
   - `XmlBeanDefinitionReader` parses `<bean>` elements, converts each into a `BeanDefinition` and calls `registry.registerBeanDefinition(beanName, beanDefinition)`.
   - Example XML:
     ```xml
     <bean id="myService" class="com.example.MyService" scope="prototype" init-method="init"/>
     ```
   - This becomes a `GenericBeanDefinition` with `beanClassName="com.example.MyService"`, `scope="prototype"`, `initMethodName="init"`.

2. **Annotation scanning (`@Component`)**
   - `ClassPathBeanDefinitionScanner` finds candidate classes (via ASM/MetadataReader) and creates `ScannedGenericBeanDefinition` or `AnnotatedGenericBeanDefinition` containing `AnnotationMetadata` and registers them.

3. **`@Bean` methods in `@Configuration` classes**
   - `ConfigurationClassPostProcessor` parses `@Configuration` classes, creates bean definitions for `@Bean` methods (factory-method style), and registers them.

4. **Programmatic registration**
   - Code can create and register definitions:
     ```java
     GenericBeanDefinition bd = new GenericBeanDefinition();
     bd.setBeanClass(MyService.class);
     bd.setScope(BeanDefinition.SCOPE_SINGLETON);
     bd.getPropertyValues().add("name","nikhil");
     registry.registerBeanDefinition("myService", bd);
     ```

5. **BeanDefinitionRegistryPostProcessor / BeanFactoryPostProcessor**
   - These can add/modify bean definitions programmatically before beans are instantiated.

---

### BeanDefinition lifecycle inside the container — step by step

1. **Read / Scan**
   - The container reads configuration (XML, annotations, Java config) and creates `BeanDefinition` objects.
2. **Register**
   - Definitions are registered in the `BeanDefinitionRegistry` (usually `DefaultListableBeanFactory` inside the `ApplicationContext`).
3. **Post-processing of definitions**
   - `BeanDefinitionRegistryPostProcessor` and `BeanFactoryPostProcessor` (e.g., `PropertySourcesPlaceholderConfigurer`, custom processors) can modify definitions before instantiation.
4. **Name generation / aliasing**
   - If no explicit id is given, a bean name is generated (e.g., decapitalized class name, or via `AnnotationBeanNameGenerator`).
5. **Merging / resolving**
   - When the factory needs to create a bean, it calls `getMergedLocalBeanDefinition(name)` which merges parent-child definitions and resolves defaults into a `RootBeanDefinition`.
6. **Bean instantiation**
   - `createBean()` uses the merged definition: resolves constructor args, picks constructor, instantiates via reflection or factory method, sets properties, applies `BeanPostProcessors`, calls `init-method`, etc.
7. **Lifecycle management**
   - Singleton beans are cached; prototype beans are created each time. Scoped beans handled via `Scope` implementations.

---

**Constructor args vs property values**

- `constructorArgumentValues` (held in `ConstructorArgumentValues`) tell the container which constructor to call and with what parameters. Spring uses constructor argument metadata to choose the constructor (via `ConstructorResolver`) and do autowiring by type/qualifier if needed.
- `propertyValues` are applied after object creation to set bean properties after construction (setter injection / field injection may later be applied by post-processors).

---

**Example: programmatically registering a BeanDefinition**

```java
DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

GenericBeanDefinition bd = new GenericBeanDefinition();
bd.setBeanClass(com.example.PaymentServiceImpl.class);
bd.setScope(BeanDefinition.SCOPE_SINGLETON);
bd.getPropertyValues().add("timeout", 30);
bd.setLazyInit(false);
bd.setAutowireCandidate(true);

registry.registerBeanDefinition("paymentService", bd);

// later you can get the bean:
Object payment = registry.getBean("paymentService");
```

---

**How annotation metadata is preserved**

- Scanned definitions keep `AnnotationMetadata` (via ASM) — this lets post-processors inspect annotations (`@Primary`, `@Scope`, `@Lazy`, `@Qualifier`) and set the corresponding flags on the `BeanDefinition` (e.g., `bd.setPrimary(true)`).
- For `@Bean` methods, the bean definition records `factoryBeanName` and `factoryMethodName` so the factory invokes the method to create the bean.

---

**Merged BeanDefinition & runtime use**

- The factory often works with a merged bean definition (`RootBeanDefinition`) that has the final resolved settings.
- `DefaultListableBeanFactory#getMergedLocalBeanDefinition` resolves parent-child inheritance, default values, and caches the merged result for performance.

---

**Qualifiers, primary, autowire candidate**

- `@Primary` → sets `bd.setPrimary(true)`.
- `@Qualifier("name")` → registers qualifiers or custom attributes on the `BeanDefinition` so the autowire resolution process can prefer a specific candidate.
- `autowireCandidate` flag decides whether the bean can be considered when autowiring.

---

**Scopes and proxies**

- `bd.setScope("request")` or `"session"` → the factory uses `Scope` implementations to produce proxy beans or scoped instances.
- `ScopedProxyMode` (from `@Scope`) may trigger creation of a proxy bean definition that delegates to the actual scoped target.

---

**Parent/Child bean definitions & inheritance**

- A `BeanDefinition` can declare a `parentName`. The child inherits settings from the parent and can override specific properties. The container merges these before instantiation.

---

**Inspecting bean definitions at runtime**

You can inspect raw definitions before beans are created:

```java
ConfigurableApplicationContext ctx = ...;
DefaultListableBeanFactory bf = (DefaultListableBeanFactory) ctx.getBeanFactory();
String[] names = bf.getBeanDefinitionNames();
for (String n : names) {
    BeanDefinition bd = bf.getBeanDefinition(n);
    System.out.println(n + " -> " + bd.getBeanClassName() + ", scope=" + bd.getScope());
}
```
This is useful for debugging how Spring interpreted your configuration.

---

**When should you touch BeanDefinitions?**

- Add/modify definitions before beans are created → use `BeanDefinitionRegistryPostProcessor` or `BeanFactoryPostProcessor`.
- Register beans programmatically for dynamic wiring (plugins, modular loads).
- For most apps you’ll rely on annotations and Java config — but understanding `BeanDefinition` lets you implement advanced wiring and framework extensions.

---

**Short mapping: XML element → BeanDefinition fields**

```xml
<bean id="userRepo" class="com.app.UserRepository" scope="singleton" lazy-init="true"/>
```
maps to:
- beanName = "userRepo"
- beanClassName = "com.app.UserRepository"
- scope = "singleton"
- lazyInit = true

---

**Key ideas to remember**

- `BeanDefinition` = recipe / metadata (not the instance).
- Holds class, scope, constructor args, properties, lifecycle info, qualifiers, etc.
- Created by readers (XML, annotations, Java config) and registered in `BeanDefinitionRegistry` (`DefaultListableBeanFactory`).
- Can be modified by `BeanFactoryPostProcessor` / `BeanDefinitionRegistryPostProcessor` before bean creation.
- The container merges & resolves definitions and uses them to instantiate and manage bean lifecycles.

---

## Bean Initialization

Bean initialization is the process of creating and configuring a Spring bean instance based on its `BeanDefinition`.

1. Creates a bean instance (using constructors or factory methods).
2. Sets dependencies (constructor/setter/field injection).
3. Applies framework services (AOP proxies, lifecycle interfaces, custom init methods).
4. Puts the bean into a ready-to-use state before returning it from the `ApplicationContext`.

### Steps in Bean Initialization

1. **Instantiation**: The container reads the `BeanDefinition` and decides how to create the instance. Happens in `createBeanInstance()`.
    - Constructor injection → chooses constructor & invokes via reflection.
    - No-arg constructor → if none specified, calls `newInstance()`.
    - Factory method → if `BeanDefinition` has `factoryMethodName`.

2. **Populate Properties**: The container sets the bean's properties using dependency injection (constructor injection, setter injection, field injection). Happens in `populateBean()`.

3. **BeanPostProcessors (Before Initialization)**: The container applies any registered `BeanPostProcessors`' `postProcessBeforeInitialization` methods to the bean instance.
    - Common uses:
        - Apply proxies.
        - Validate beans.
        - Handle `@Autowired` / `@Value`.
        - Process `@PostConstruct`.

4. **Aware Interfaces**: If the bean implements any Aware interfaces (e.g., `BeanNameAware`, `BeanFactoryAware`), the container calls the corresponding methods to provide context information.
    - `BeanNameAware` → injects bean name.
    - `BeanFactoryAware` → injects `BeanFactory` reference.
    - `ApplicationContextAware` → injects `ApplicationContext`.

   Example: `AutowiredAnnotationBeanPostProcessor`.

5. **Initialization Callbacks**: The container invokes any initialization methods specified in the `BeanDefinition` (e.g., `init-method`) or annotated with `@PostConstruct`.
    - If bean implements `InitializingBean`, call `afterPropertiesSet()`.
    - If `@PostConstruct` is present, invoke that method (via `CommonAnnotationBeanPostProcessor`).
    - If `init-method` is defined in XML/Java config, call it.

   This ensures the bean has finished setup logic.

6. **BeanPostProcessors (After Initialization)**: The container applies any registered `BeanPostProcessors`' `postProcessAfterInitialization` methods to the bean instance.
    - Call `postProcessAfterInitialization()` on all `BeanPostProcessors`.
    - AOP proxies (transactional beans, security, etc.) are typically created here.
    - The final object (possibly wrapped in proxy) is returned.

7. **Ready for Use**: The bean is now fully initialized and ready for use within the application. If scope is singleton and bean implements `DisposableBean` or has `destroy-method`, it’s registered for destruction callback when context closes.

```
BeanDefinition
      |
      v
Instantiate bean (constructor/factory)
      |
      v
Populate dependencies (DI)
      |
      v
postProcessBeforeInitialization()
      |
      v
Aware interfaces callbacks
      |
      v
@PostConstruct / afterPropertiesSet() / init-method
      |
      v
postProcessAfterInitialization() (proxies, etc.)
      |
      v
Bean is READY to use
```

**Key Points:**

1. Order of initialization hooks
    - `@PostConstruct` → `afterPropertiesSet()` → custom `init-method`.
2. `BeanPostProcessor` hooks wrap before & after initialization.
3. AOP proxies are applied after initialization but before the bean is available.
4. Spring manages destruction for singletons but not for prototypes.
5. Initialization is lazy if `@Lazy` or `lazy-init="true"` is used.

---

## Object Creation

In a Spring application, you typically don’t do:

```java
Engine engine = new Engine();
Car car = new Car(engine);
```
- Because then you are managing dependencies yourself, which defeats the purpose of DI (Dependency Injection).
- Instead, Spring’s IoC Container (`ApplicationContext` / `BeanFactory`) does that for you:

### How it works in Spring

1. You define beans (via `@Component`, `@Bean`, or XML)
   ```java
   @Component
   class Engine {
       void start() { System.out.println("Engine started!"); }
   }

   @Component
   class Car {
       private final Engine engine;

       // Constructor injection
       @Autowired
       public Car(Engine engine) {   // Spring injects Engine here
           this.engine = engine;
       }

       public void drive() {
           engine.start();
           System.out.println("Car is driving...");
       }
   }
   ```

2. You start the Spring Context

   ```java
   @SpringBootApplication
   public class App {
       public static void main(String[] args) {
           ApplicationContext context = SpringApplication.run(App.class, args);

           Car car = context.getBean(Car.class); // Spring gives you a ready-made Car
           car.drive();
       }
   }
   ```

3. What Spring does internally
    1. Scans for beans (`@Component`, `@Service`, `@Repository`, etc.)
    2. Finds Car → sees it needs an Engine in the constructor.
    3. Finds Engine bean → creates it.
    4. Injects the Engine into Car automatically.
    5. Registers Car and Engine inside the IoC container.
    6. When you call `context.getBean(Car.class)` → you get a fully initialized Car with Engine inside.

---

## Autowired Annotation

### Internal Working of `@Autowired` in Spring

#### What is `@Autowired`?

- An annotation used by Spring IoC Container to perform Dependency Injection (DI) automatically.
- Instead of manually wiring beans in `applicationContext.xml`, Spring scans, finds, and injects dependencies at runtime.

**Example:**
```java
@Component
class Engine {}

@Component
class Car {
  @Autowired
  private Engine engine;  // injected automatically
}
```

#### Behind the Scenes – Key Components

Several classes in Spring Framework make `@Autowired` work:

- **BeanFactory / ApplicationContext** → Core container managing beans.
- **AutowiredAnnotationBeanPostProcessor** → A `BeanPostProcessor` that handles `@Autowired` injection.
- **Reflection API** → To access private fields/methods and inject values.
- **Type Matching / Qualifier** → To decide which bean should be injected.

---

#### Step-by-Step Internal Flow

**Step 1: Bean Scanning**
- Spring scans classes annotated with `@Component`, `@Service`, `@Repository`, etc.
- It registers them as beans inside `ApplicationContext`.

**Step 2: Bean Creation**
- When Spring creates a bean (Car), it notices the `@Autowired` annotation.
- This happens before bean initialization using `BeanPostProcessor`.

**Step 3: AutowiredAnnotationBeanPostProcessor Kicks In**
- This special class implements `BeanPostProcessor`.
- During the post-processing phase, it inspects bean definitions for `@Autowired`.

**Pseudo-code (simplified):**
```java
for (Field field : beanClass.getDeclaredFields()) {
    if (field.isAnnotationPresent(Autowired.class)) {
        Object dependency = beanFactory.getBean(field.getType()); // find matching bean
        field.setAccessible(true); // bypass private
        field.set(beanInstance, dependency); // inject dependency
    }
}
```

**Step 4: Dependency Resolution**
- Spring resolves the dependency based on:
    1. Type (default behavior: find bean by type)
    2. Qualifier (`@Qualifier("beanName")` to disambiguate)
    3. Primary (`@Primary` bean wins if multiple candidates)
    4. Required (if `@Autowired(required=false)`, injection is optional)

**Step 5: Reflection Injection**
- Spring uses Java Reflection API (`setAccessible(true)`) to inject the dependency even if the field is private.
- This is why you don’t need setters in modern Spring apps.

**Step 6: Bean is Ready**
- Once dependencies are injected, the bean proceeds to initialization (`@PostConstruct` if defined, etc.) and is stored in the context for future use.

---

**Example: Payment Service with Multiple Implementations**

Step 1: Define an Interface
```java
public interface PaymentService {
    void pay(double amount);
}
```

Step 2: Multiple Implementations
```java
import org.springframework.stereotype.Component;

@Component
public class CreditCardPaymentService implements PaymentService {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using Credit Card.");
    }
}

@Component
public class PaypalPaymentService implements PaymentService {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using PayPal.");
    }
}

@Component
@Primary  // Default choice if multiple beans exist
public class UpiPaymentService implements PaymentService {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount + " using UPI.");
    }
}
```

Step 3: Injecting with `@Autowired`
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckoutService {

    // Will inject UpiPaymentService by default because it's marked as @Primary
    @Autowired
    private PaymentService paymentService;

    // Example with @Qualifier to override default
    @Autowired
    @Qualifier("paypalPaymentService")
    private PaymentService paypalService;

    public void checkout() {
        paymentService.pay(100.0);   // Injected UpiPaymentService
        paypalService.pay(200.0);    // Injected PaypalPaymentService
    }
}
```

Step 4: Run the Application
```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PaymentApp implements CommandLineRunner {

    private final CheckoutService checkoutService;

    public PaymentApp(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }

    @Override
    public void run(String... args) {
        checkoutService.checkout();
    }
}
```

Output
```
Paid 100.0 using UPI.
Paid 200.0 using PayPal.
```

**What Happened Internally?**
1. Spring scanned all `@Component` classes.
2. Found 3 beans implementing `PaymentService`.
3. `@Autowired` by type → ambiguity (multiple beans).
4. Spring resolved by:
    - Choosing `@Primary` bean (`UpiPaymentService`) for default injection.
    - Using `@Qualifier("paypalPaymentService")` when explicitly asked.
5. `AutowiredAnnotationBeanPostProcessor` used reflection to inject dependencies into `CheckoutService`.
    - `Field.setAccessible(true)` → bypass private
    - `Field.set(bean, dependency)` → assign dependency

Example with simulation code of reflection:
```java
public class ManualAutowiredSimulation {
    public static void main(String[] args) throws Exception {
        // Simulating Spring's BeanFactory
        Map<String, PaymentService> beans = new HashMap<>();
        beans.put("creditCardPaymentService", new CreditCardPaymentService());
        beans.put("paypalPaymentService", new PaypalPaymentService());
        beans.put("upiPaymentService", new UpiPaymentService()); // @Primary

        CheckoutService checkoutService = new CheckoutService();

        // Simulating AutowiredAnnotationBeanPostProcessor
        for (Field field : CheckoutService.class.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.getName().equals("paymentService")) {
                // Inject the @Primary bean
                field.set(checkoutService, beans.get("upiPaymentService"));
            } else if (field.getName().equals("paypalService")) {
                // Inject the @Qualifier("paypalPaymentService") bean
                field.set(checkoutService, beans.get("paypalPaymentService"));
            }
        }

        // Now checkoutService has its dependencies injected
        checkoutService.checkout();
    }
}
```

---

#### Constructor and Setter Injection with `@Autowired`

- Field Injection (most common) → inject directly into fields.
- Constructor Injection → Spring calls the constructor with dependencies.
  ```java
  @Component
  class Car {
      private final Engine engine;
      @Autowired
      Car(Engine engine) { this.engine = engine; }
  }
  ```
- Setter Injection → Spring calls the setter method and passes the dependency.

---

#### Real Example with Reflection

Let’s simulate what Spring does internally:
```java
class Engine {}

class Car {
    @Autowired
    private Engine engine;
}

public class SimpleDI {
    public static void main(String[] args) throws Exception {
        Engine engine = new Engine();   // Bean created
        Car car = new Car();            // Bean created

        // Simulating AutowiredAnnotationBeanPostProcessor
        for (var field : Car.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(car, engine);  // injecting dependency
            }
        }

        System.out.println("Car has engine? " + (car != null));
    }
}
```

**Summary**
- `@Autowired` works via `AutowiredAnnotationBeanPostProcessor`, a Spring `BeanPostProcessor`.
- It detects fields/methods/constructors annotated with `@Autowired`.
- Uses reflection to inject matching beans.
- Resolves conflicts with `@Qualifier`, `@Primary`, etc.
- Ensures beans are ready before being exposed from the container.

---

### Internal Working of `@Autowired` with Constructor Injection

In constructor injection, Spring chooses a constructor (either the only one, or the one marked with `@Autowired`) and calls it with resolved dependencies at bean creation time.

```java
@Component
class Car {
    private final Engine engine;

    @Autowired   // optional if only 1 constructor
    Car(Engine engine) {
        this.engine = engine;
    }
}
```

#### How It Works Internally

Let’s follow the lifecycle:

**Step 1: Bean Definition**
- Spring scans Car and Engine.
- Both are registered as bean definitions in `ApplicationContext`.

**Step 2: Constructor Detection**
- Spring sees that Car has a constructor.
- If there’s only one constructor, Spring uses it automatically.
- If multiple constructors exist → Spring looks for `@Autowired` annotation.

**Step 3: Dependency Resolution**
- Spring resolves the constructor’s parameters by:
    1. Type (default behavior)
    2. Qualifier (if `@Qualifier` present)
    3. Primary (if multiple candidates exist)

Example:
```java
Car(Engine engine)  // parameter type = Engine.class
```
**Spring asks BeanFactory → “Give me a bean of type Engine.”**

**Step 4: Constructor Invocation**
- Unlike field injection, Spring does not use reflection to set private fields.
- Instead, it calls the constructor directly with resolved dependencies.

Pseudo-code (simplified):
```java
Engine engineBean = beanFactory.getBean(Engine.class);
Car carBean = new Car(engineBean);  // Constructor Injection
```

**Step 5: Post-Processing & Initialization**
- After construction, bean goes through:
    - `@PostConstruct`
    - Aware interfaces
    - `init-method` (if any)

Now the bean is ready for use.

#### Real-World Example

Multiple Implementations with `@Qualifier`
```java
@Component
class CreditCardPaymentService implements PaymentService {}

@Component
class UpiPaymentService implements PaymentService {}

@Component
class CheckoutService {
    private final PaymentService paymentService;

    @Autowired
    public CheckoutService(@Qualifier("upiPaymentService") PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void checkout() {
        paymentService.pay(500.0);
    }
}
```
Here Spring:
- Sees CheckoutService constructor.
- Resolves paymentService → multiple beans → `@Qualifier` helps.
- Calls `new CheckoutService(new UpiPaymentService())`.

**Summary**
- Constructor injection = dependencies resolved before object creation.
- Spring chooses constructor → resolves parameters → calls constructor directly.
- Safer than field injection → ensures immutability & testability.
- Internally, no reflection on fields, but constructor resolution + invocation.

#### Java Simulation of Constructor Injection

1. Define the Dependencies

```java
// Dependency 1
class TransactionRepository {
    public void save(double amount) {
        System.out.println("Transaction saved: " + amount);
    }
}

// Dependency 2
class NotificationService {
    public void notifyUser(String message) {
        System.out.println("Notification sent: " + message);
    }
}
```

2. Define the Service with Constructor Injection

```java
class PaymentService {

    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    // Dependencies injected via constructor
    public PaymentService(TransactionRepository transactionRepository,
                         NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    public void processPayment(double amount) {
        transactionRepository.save(amount);
        notificationService.notifyUser("Payment of " + amount + " processed.");
    }
}
```
Notice:
- Dependencies are marked as final.
- The class cannot be instantiated unless both dependencies are provided → enforces immutability and correctness.

3. Simulate the “Container” (Manual Wiring)

```java
public class MainApp {
    public static void main(String[] args) {
        // Step 1: Create dependencies manually
        TransactionRepository repo = new TransactionRepository();
        NotificationService notifier = new NotificationService();

        // Step 2: Inject them into the constructor
        PaymentService paymentService = new PaymentService(repo, notifier);

        // Step 3: Use the service
        paymentService.processPayment(500.0);
    }
}
```

4. Output
```
Transaction saved: 500.0
Notification sent: Payment of 500.0 processed.
```

- In Spring, the IoC container (`ApplicationContext`) automatically does Step 3 (wiring) using reflection.
- You only declare dependencies (constructor params), and Spring resolves them from the `BeanDefinition` metadata and injects them.

---

## Setter Injection

**What is Setter Injection?**
- In Setter-based Dependency Injection, dependencies are provided after object creation using setter methods (or public methods).
- Unlike constructor injection, the object can be created with a default constructor first, then dependencies are “injected” later.

**Characteristics**
1. Flexibility – You can set or change dependencies after the object is created.
2. Optional dependencies – You can choose to inject some and leave others.
3. Potential issue – Object may remain in an incomplete state if a setter is not called (not fully initialized).

---

#### Pure Java Example (without Spring)

1. Dependencies
```java
class TransactionRepository {
    public void save(double amount) {
        System.out.println("Transaction saved: " + amount);
    }
}

class NotificationService {
    public void notifyUser(String message) {
        System.out.println("Notification sent: " + message);
    }
}
```

2. Service with Setter Injection
```java
class PaymentService {

    private TransactionRepository transactionRepository;
    private NotificationService notificationService;

    // Default constructor (no injection yet)
    public PaymentService() {}

    // Setters for dependencies
    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void processPayment(double amount) {
        if (transactionRepository == null || notificationService == null) {
            throw new IllegalStateException("Dependencies not set!");
        }
        transactionRepository.save(amount);
        notificationService.notifyUser("Payment of " + amount + " processed.");
    }
}
```
**Key notes**
- In this case the `paymentService` is not tightly coupled to its dependencies. I can use null check to make sure that dependencies are set before use.
- If `notificationService` is not set, I can still use `transactionRepository` if needed.
- This allows more flexibility but requires careful handling to avoid incomplete state.

3. Manual Wiring (Container Simulation)
```java
public class MainApp {
    public static void main(String[] args) {
        // Step 1: Create dependencies
        TransactionRepository repo = new TransactionRepository();
        NotificationService notifier = new NotificationService();

        // Step 2: Create service (empty, no deps yet)
        PaymentService paymentService = new PaymentService();

        // Step 3: Inject dependencies via setters
        paymentService.setTransactionRepository(repo);
        paymentService.setNotificationService(notifier);

        // Step 4: Use service
        paymentService.processPayment(1000.0);
    }
}
```

Output
```
Transaction saved: 1000.0
Notification sent: Payment of 1000.0 processed.
```

**How Spring Does Setter Injection**
- In Spring, you can use `@Autowired` on setter methods:

```java
@Service
class PaymentService {

    private TransactionRepository transactionRepository;
    private NotificationService notificationService;

    @Autowired
    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```
**Internally:**
1. Spring creates the bean using the default constructor.
2. Spring finds `@Autowired` on setters.
3. Using reflection, it calls those setter methods with resolved dependencies.

---

## Dependency Injection (DI) types into real-world examples, compare them, and explain when to use which.

**Types of Dependency Injection in Spring**
1. Constructor Injection
2. Setter Injection
3. Field Injection

(Technically there’s also Interface Injection, but Spring doesn’t support it natively — it’s more of a theoretical DI pattern.)

---

#### 1. Constructor Injection

**Concept:** Dependencies are provided via the constructor.  
**Guarantee:** Object cannot exist without its required dependencies.  
**Usage:** Best for mandatory dependencies.

**Real-life Example**

Imagine building a Car.
- A car must have an engine at creation.
- If the engine is missing, the car is useless.

```java
class Engine {}
class Car {
    private final Engine engine;
    public Car(Engine engine) {
        this.engine = engine; // mandatory dependency
    }
}
```
You cannot create a Car without passing an Engine.

**When Constructor Injection is Better**
- Mandatory, non-optional dependencies.
- Immutable objects (dependencies are final).
- Unit testing → easy to pass mocks.
- Fail-fast principle: app won’t even start if dependencies are missing.

Note: Spring uses constructor injection internally for things like DataSource, EntityManagerFactory, etc.

---

#### 2. Setter Injection

**Concept:** Dependencies are provided via setter methods, after object creation.  
**Flexibility:** Dependencies can be changed later.  
**Usage:** Best for optional or late dependencies.

**Real-life Example**
Think of a Smartphone.
- A smartphone can exist without a SIM card.
- You can “inject” a SIM later via a slot (setter).

```java
class SimCard {}
class Smartphone {
    private SimCard simCard;
    public void setSimCard(SimCard simCard) {
        this.simCard = simCard; // optional dependency
    }
}
```
You can create a Smartphone without a SimCard, then add/change it later.

**When Setter Injection is Better**
- Optional dependencies.
- Configurable objects that may change after creation.
- When you don’t control object creation but want to “plug in” dependencies later.

**Example in Spring:**
`DataSourceInitializer` bean may or may not need a `DatabasePopulator`. Setter injection works well here.

- **“Since we do not inject these manually since Spring. How is it possible that one of the dependencies is already injected, and while the other one is not?”**
    - Spring injects all dependencies automatically.
    - It is not normal for one to be injected and the other not — unless one bean is missing or optional.
    - If you see a dependency as null at runtime, it usually means:
        - You’re calling methods too early, e.g., in the constructor.
        - Bean does not exist in context.
        - Setter is optional (`@Autowired(required = false)`)
        - Circular dependency issues, in which Spring sometimes injects proxies or fails.

- **Why Setter Injection Sometimes Feels “Partial”**
    - With constructor injection, all required dependencies are injected before the object is created, so it’s impossible for a dependency to be missing.
    - With setter injection, the object is first instantiated with the default constructor, and then setters are called.
    - If you accidentally call a method before Spring injects dependencies, you may get null.

Example (dangerous!):
```java
@Service
public class MyService {

    @Autowired
    private NotificationService notificationService;

    public MyService() {
        // At this point, notificationService is still null!
    }
}
```
- That’s why constructor injection is preferred for mandatory dependencies. Setter injection is mainly for:
    - Optional dependencies (`required=false`)
    - Avoiding circular dependencies
    - Allowing bean to exist before fully initialized

**Explain different ways provided by Spring Boot to resolve circular dependencies.**
- In Spring Boot, circular dependencies can be resolved by using setter injection instead of constructor injection, allowing beans to be instantiated before their dependencies are set. Another method is using the @Lazy annotation, which defers the initialization of a bean until it is actually needed, thus breaking the dependency cycle. Additionally, redesigning the application architecture to better separate concerns and reduce coupling between beans can also effectively address circular dependencies
- To prevent cyclic dependencies in Spring, you can redesign your classes to remove direct dependencies, use setter or field injection instead of constructor injection, or introduce interfaces to decouple the components. This approach involves rethinking class designs to reduce tight coupling, employing different types of dependency injections that don't force immediate object creation, or using interfaces that abstract the implementation details. By doing so, you prevent the scenario where two or more classes depend on each other to be instantiated, which can cause the application to fail at runtime.
---

#### 3. Field Injection

**Concept:** Dependencies are injected directly into fields using reflection.  
**Convenient:** Less boilerplate (no constructor/setter code).  
**Usage:** Downside: Harder to test, not explicit, can’t make fields final.

**Real-life Example**
Think of a Gaming PC.
- You order a pre-built gaming PC.
- All components (CPU, GPU, RAM) are installed directly on the motherboard.
- You didn’t wire them, the manufacturer did “internally”.

```java
class GPU {}
class GamingPC {
    @Autowired
    private GPU gpu; // injected directly, no constructor or setter
}
```

**When Field Injection is Better**
- Quick prototyping, small projects.
- When boilerplate must be minimized.

**Avoid in:**
- Large enterprise apps (harder to test).
- When immutability is important.

**NOTE:**
- Field injection is considered an anti-pattern in professional Spring projects — constructor injection is preferred.
- Field injection is simple and tempting, but it has concrete downsides in real-world, production-grade code.

**What field injection actually does**

When you write:
```java
@Component
class MyService {
    @Autowired
    private Repo repo;   // field injection
}
```
Spring (or another DI container) creates the MyService instance first (constructor runs), then later — during post-processing — uses reflection (`field.setAccessible(true)`) to assign repo into the private field. The injection happens after construction.

---

#### Why field injection is considered an anti-pattern (detailed reasons)

**1. Hidden dependencies → poor readability & harder to reason about**
```java
class OrderService {
    @Autowired private PaymentGateway gateway;   // hidden requirement
}
```
Compare with constructor injection — constructor signature immediately shows what this class needs:
```java
class OrderService {
    private final PaymentGateway gateway;
    OrderService(PaymentGateway gateway) { this.gateway = gateway; }
}
```
**Why it matters:** reviewers, new team members, or someone instantiating the class (tests) immediately see requirements with constructor injection.

**2. No immutability / cannot use final**

Field injection prevents marking dependencies final. Final fields give stronger invariants and safe publication guarantees:
```java
// NOT possible with field injection:
@Autowired private Repo repo; // cannot be final

// With constructor injection:
private final Repo repo;  // immutable after construction — safer
```
Final fields also have JVM memory-model benefits for thread-safety.

**3. Harder, less explicit unit testing**

With constructor injection testing is simple and explicit:
```java
OrderService s = new OrderService(mockGateway);
```
With field injection you either:
- Use `@InjectMocks` + `@Mock` (Mockito reflection magic) — less explicit; or
- Use `ReflectionTestUtils.setField(...)` — brittle and reflective.

Example: constructor test vs field-injected test (Mockito):
```java
// constructor test (explicit)
var svc = new OrderService(mockGateway);

// field injection test (less explicit)
@Mock PaymentGateway mockGateway;
@InjectMocks OrderService svc;  // Mockito injects via reflection
```
**Why it matters:** explicit tests are easier to understand and maintain.

**4. Lifecycle and initialization pitfalls**

Because field injection occurs after construction, code that uses the injected dependency in the constructor will see null and throw NPE:
```java
@Component
class A {
    @Autowired private B b;    // injected later

    A() {
        b.doSomething();       // NPE — b is not injected yet
    }
}
```
Constructor injection avoids this: dependencies are present during construction.

**5. Breaks encapsulation via reflection**
Field injection requires `setAccessible(true)` to populate private fields. That bypasses normal encapsulation and security boundaries — not ideal for robust, maintainable code.

**6. Tooling & static analysis dislike it**
Linters and static analysis tools (and many code review guidelines) flag field injection as a smell. It makes automated analysis and dependency graph extraction harder.

**7. Encourages hidden circular dependencies**
- Field/setter injection can sometimes hide circular dependency problems (container may resolve some circular refs for singletons), which masks poor design.
- Constructor injection fails fast for circular deps, which is preferable (you fix the design instead of hiding the problem).

---

**When field injection can be acceptable**

It isn’t evil in every context — there are practical, limited uses:
- Quick prototypes / PoCs / demos where speed beats correctness.
- Test classes (test code often uses `@Autowired` fields for brevity; acceptable).
- Small throwaway scripts or examples where boilerplate is costly.
- Framework-required fields in some rare legacy integration cases (but rare).

Even then, prefer local scope: keep field injection out of production core services.

---

**Best practices / recommendations (concrete)**
1. Default to constructor injection for all required dependencies.
2. Use setter injection only for optional or replaceable dependencies (and document optionality).
3. Avoid field injection in production/business logic classes. Use it sparingly in tests or tiny demos.
4. Keep constructors small — if a class needs many dependencies, consider refactoring (split responsibilities).
5. Use Lombok to reduce boilerplate without giving up constructor injection:
   ```java
   @RequiredArgsConstructor
   @Component
   class OrderService {
       private final PaymentGateway gateway; // injected via generated constructor
   }
   ```
6. Fail fast on missing deps — constructor injection enforces it at creation time, which is preferable.

---

**Quick checklist for code review**
- Does the constructor show required dependencies explicitly? → ✅
- Are any dependencies final? → ✅
- Are optional deps handled via setters with null checks? → ✅
- Are there no constructor uses of dependencies that are field-injected? → ✅
- Are tests explicit (pass mocks to constructor) instead of using reflection? → ✅

---

**Summary with Real-Life Analogies**

| DI Type      | Real-Life Analogy                        | Best For                   |  Pros                             | Cons                                          | 
|--------------|------------------------------------------|----------------------------|------------------------------------|-----------------------------------------------|
| Constructor  | Car needs Engine to exist                | Mandatory dependencies     | Immutable, safe, testable         | More boilerplate if many deps                 | 
| Setter       | Smartphone with SIM slot                 | Optional/configurable deps | Flexible, allows reconfiguration  | Object may be incomplete if setter not called | 
| Field        | Pre-built Gaming PC with parts soldered in | Quick wiring, prototypes   | Minimal code, easy                | Hidden deps, hard to test, no immutability    | 

---

**Which One Should You Use?**
- Constructor Injection → Default choice. Use for required dependencies. Promotes immutability and clean contracts.
- Setter Injection → Use when dependencies are optional or may change later.
- Field Injection → Use only in simple apps or prototypes, avoid in production-quality, test-heavy systems.

---

## How Setter Injection Works in Spring

When you do setter injection, your service looks like this:
```java
@Service
public class MyService {

    private TransactionService transactionService;
    private NotificationService notificationService;

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void doSomething() {
        transactionService.process();
        notificationService.notifyUser();
    }
}
```
- Spring scans the class, finds the `@Autowired` setters.
- During bean creation, Spring:
    1. Instantiates the service object (using default constructor).
    2. Calls the setter methods and injects dependencies.
    3. Calls any `@PostConstruct` methods if present.

Important: Spring injects dependencies automatically, you never call these setters manually.

## 🔍 Interview Follow-Up Questions

### Q. What is the difference between @PostConstruct and InitializingBean?
**A:** `@PostConstruct` is annotation-based and preferred for modern development. `InitializingBean` is interface-based and ties your bean to Spring.

### Q. What is the use of BeanPostProcessor?
**A:** It allows for modification of new bean instances, like wrapping them with proxies or performing validations.

### Q. When would you use a prototype scope?
**A:** When you need a new instance every time a bean is requested, like in the case of stateful beans.
