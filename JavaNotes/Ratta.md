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