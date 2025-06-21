`ROADMAP`

✅ 1. Core Interfaces and Hierarchy

Understand the interface hierarchy and what each offers:
•	Collection
•	List → ArrayList, LinkedList, Vector, Stack
•	Set → HashSet, LinkedHashSet, TreeSet
•	Queue → PriorityQueue, Deque, ArrayDeque, LinkedList
•	Map (not part of Collection but crucial)
•	HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap, WeakHashMap

🧠 Interview Expectation: Be able to draw the hierarchy and explain use cases.

✅ 2. Internal Working & Time Complexity
• Table where all the collections are stored with their
Internal Structure, Time complexity of Internal Structure, Insert
, Delete

✅ 3. Ordering and Duplicates
•   Table with following column
•	Maintains Order?
•	Allows Duplicates?
•	Null value allowed
•	Throws exception?

✅ 4. Thread-Safety
•	Not thread-safe: ArrayList, HashMap, HashSet
•	Thread-safe:
•	Legacy: Vector, Hashtable
•	Modern: Collections.synchronizedList(), ConcurrentHashMap, CopyOnWriteArrayList
•	Use ConcurrentHashMap in high-concurrency apps.

✅ 5. Fail-Fast vs Fail-Safe Iterators
•	Fail-Fast: ArrayList, HashMap — throws ConcurrentModificationException
•	Fail-Safe: ConcurrentHashMap, CopyOnWriteArrayList — safe but not real-time data

✅ 6. Java 8+ Enhancements
•	Streams API:
•	filter, map, collect, sorted, forEach
•	Functional Interfaces: Function, Consumer, Predicate
•	Map.forEach, Map.computeIfAbsent(), removeIf() etc.

✅ 7. Comparisons and Custom Sorting
•	Comparable<T> vs Comparator<T>
•	Lambda expressions for sorting
•	Collections.sort(list, comparator)
•	TreeMap and TreeSet with custom comparators

✅ 8. Advanced Topics
•	Load Factor, Capacity in HashMap
•	Resize / Rehash logic
•	Difference between identity-based and equals-based maps: HashMap vs IdentityHashMap
•	WeakHashMap and its use with garbage collection
•	EnumMap, EnumSet for performance with Enums
•	Deque vs Queue, Stack deprecation


✅ 9. Real-World Scenarios & Design
•	Which collection to use when?
•	Replace List<Optional<T>> with filters
•	Use LinkedHashMap for LRU cache
•	Choosing the right Set or Map for memory-sensitive or speed-critical applications