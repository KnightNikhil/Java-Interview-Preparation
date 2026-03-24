## 1. Collections framework basics

1. What is the Java Collections Framework and what problem does it solve?
    - Follow‑up: Can you list the core root interfaces and what they conceptually represent (Collection, List, Set, Queue, Map)?


2. What are the main benefits of using the Collections Framework?
- Follow‑up: Which of those benefits matter most in a large enterprise codebase and why?
- Interoperability and standardized APIs are critical because many teams and services must integrate; using common List/Map/Set types keeps boundaries simple and decoupled.
- Performance and quality matter a lot at scale; relying on well‑optimized, battle‑tested implementations avoids bugs and performance regressions from home‑grown data structures. 
- Reduced design and learning effort is also key: new developers can be productive quickly, and new modules can plug into existing ones just by exchanging standard collections.


3. Explain the high‑level hierarchy of the Collections Framework.
    - Follow‑up: Why does `Map` not extend `Collection`? 
    - data model is fundamentally diff

4. What are the key methods defined in the `Collection` interface?
    - Follow‑up: For each of `add`, `addAll`, `remove`, `retainAll`, `clear`, `contains`, `size`, `isEmpty`, give a short use‑case.

5. Compare List, Set, Queue, and Map in terms of ordering, duplicates, and null handling.
    - Follow‑up: In which real‑world situation would you prefer each one?

***

## 2. Generics and collections

6. What is the benefit of using Generics with collections?
- Follow‑up: How do Generics help avoid `ClassCastException` in collections?
```java
List<String> list = new ArrayList<>();
list.add("Nikhil");
// list.add(42);                  // compile‑time error
```

7. When would you still need `instanceof` with collections even with Generics? 
- You have a List of `Object` and want to process only the `String` elements.



***

## 3. Iteration: fail‑fast vs fail‑safe

8. What is a fail‑fast iterator? Give an example from the Collections Framework.
- Follow‑up: What is considered a “structural modification” that triggers `ConcurrentModificationException`?
- add/remove of element from a collection while any threading is processing it

9. What is a fail‑safe iterator? Give examples of collections providing fail‑safe iterators.
- Follow‑up: Why are fail‑safe iterators typically less memory‑efficient?
- CopyOnWriteArrayList, ConcurrentHashMap bcoz they are iterated on clone of the collection

10. Conceptual question: Why is it dangerous to catch and ignore `ConcurrentModificationException`?
- Follow‑up: How should you safely remove elements while iterating over a `List`?
- Safest is to use the `Iterator` (or `ListIterator`)’s own `remove()` while iterating, instead of calling `list.remove(...)` directly inside a loop.
- Using Iterator
```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));

Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    Integer val = it.next();
    if (val % 2 == 0) {          // remove even numbers
        it.remove();            // safe: iterator's remove
    }
}
```
- `it.remove()` removes the last element returned by `next()` and keeps the iterator’s internal modification count in sync, so no `ConcurrentModificationException`.
- Using ListIterator (if you also need bidirectional traversal)
```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
ListIterator<Integer> it = list.listIterator();

while (it.hasNext()) {
    Integer val = it.next();
    if (val % 2 == 0) {
        it.remove();           // same rule: use iterator's remove
    }
}
```
- Calling `list.remove(...)` or modifying the list structurally from outside the iterator during iteration is what triggers fail‑fast behavior. 

***

## 4. Enumeration vs Iterator vs ListIterator

11. What is the difference between `Enumeration` and `Iterator`?
- Follow‑up: Why is `Enumeration` considered legacy, and where is it still used?
- Using Enumeration, you can only traverse the collection. You can’t do any modifications to collection while traversing it.
- Still usec with vector, hashtable etc


12. What are the methods available in `Iterator`? Which one is not present in `Enumeration`?
- remove


13. What is `ListIterator` and how does it differ from `Iterator`?
- Follow‑up: Show how you would iterate a list forward and backward using `ListIterator`.
- `ListIterator` is a richer iterator for lists that can move both forward and backward and also modify elements, whereas `Iterator` only moves forward and can at most remove the last returned element.
- Direction: `Iterator` supports only forward traversal (`hasNext`, `next`); `ListIterator` supports forward and backward traversal (`hasNext`/`next` and `hasPrevious`/`previous`). 
- Modification: `Iterator` only allows `remove()`; `ListIterator` additionally allows `add(E)` and `set(E)` while iterating. 
- Position info: `ListIterator` can report indices via `nextIndex()` and `previousIndex()`, which `Iterator` cannot. 
- Applicability: `Iterator` works for all `Collection`s; `ListIterator` is only for `List` implementations (e.g., `ArrayList`, `LinkedList`). 


14. When would you choose `ListIterator` over `Iterator`?
- Follow‑up: Can `ListIterator` be used with a `Set`? Why or why not?
- No. `ListIterator` can only be obtained from a `List`, not from a `Set`, because it relies on the concept of ordered, index‑based positions which `Set` does not provide.
- `Set` does not guarantee positional indexing (no `get(int index)`, no stable index order in general), so the bidirectional, index‑aware operations of `ListIterator` (`nextIndex()`, `previousIndex()`, `add` at current position, etc.) simply do not make sense for a `Set`. 


***

## 5. `equals` and `hashCode` contract

15. Why is it important to override `equals` and `hashCode` when using custom objects as keys in hash‑based collections?
    - Follow‑up: What happens if you override `equals` but not `hashCode`?

16. Explain the contract between `equals` and `hashCode`.
    - Follow‑up: Provide a simple implementation of `equals` and `hashCode` for a `Student(id, name)` class.

17. What problem arises if two logically equal objects return different hash codes?
    - Follow‑up: How does this impact `HashSet` or `HashMap` behavior when adding and retrieving?

***

## 6. Map interface and implementations

18. What are the main general‑purpose `Map` implementations and their key characteristics: `HashMap`, `LinkedHashMap`, `TreeMap`, `Hashtable`?
    - Follow‑up: For each, explain ordering guarantees, null key/value support, and synchronization. 


19. What are the “collection views” provided by `Map`?
    - Follow‑up: How are `keySet()`, `values()`, and `entrySet()` views backed by the underlying map?


20. Explain the purpose of `Map.Entry`.
    - Follow‑up: Show how to iterate through a `Map` using `entrySet()` and print both keys and values.


21. Compare `HashMap` and `LinkedHashMap` internal workings.
    - Follow‑up: How does `LinkedHashMap` maintain insertion order on top of hashing?


22. Compare `HashMap` and `TreeMap`.
- Follow‑up: When would you choose `TreeMap` even though it is slower?
- Yes, in TreeMap all data is stored directly in a single red‑black tree structure; there is no hash table + linked list combination like in HashMap


23. Compare `HashMap` and `Hashtable`.
    - Follow‑up: Why is `Hashtable` considered a legacy class, and what is the recommended replacement?


24. How do these `Map` implementations handle `ConcurrentModificationException`?
    - Follow‑up: How does that differ from concurrent collections like `ConcurrentHashMap`?

***

## 7. Hash‑based structures, mutability, and collisions

25. Describe the internal structure of a `HashMap` bucket (list vs tree).
    - Follow‑up: At what threshold does Java convert the collision list to a tree, and why?


26. What happens if you use a mutable object as a key in a `HashMap` and mutate a field that affects `hashCode`/`equals` after insertion? 
    - Follow‑up: How can this lead to “lost” entries?


27. What happens if two different keys have the same hash code but are not equal?
    - Follow‑up: How does `HashMap` still maintain correctness in this case?


28. Explain how `HashSet` is implemented internally using `HashMap`. 
    - Follow‑up: What key and value does `HashSet` store in the backing `HashMap`?

***

## 8. Special map types: `WeakHashMap`, `IdentityHashMap`, `EnumMap`

29. What is `WeakHashMap` and how does it work?
    - Follow‑up: Under what circumstances will an entry in a `WeakHashMap` be automatically removed?
    - If key is mutable and later changed to null, that will mean that this key is no longer required, garbage collector can remove the entry then


30. Give a real‑world use case for `WeakHashMap`.
    - Follow‑up: Why is it not suitable as a general‑purpose cache without careful design?


31. What is `IdentityHashMap` and how does it differ from `HashMap` in terms of key comparison? 
    - Follow‑up: Show a small code example where two logically equal keys produce one entry in `HashMap` but two entries in `IdentityHashMap`.


32. When would you use `IdentityHashMap`?
    - Follow‑up: Why is it generally not recommended for everyday business logic?


33. What is `EnumMap`, and what restrictions does it have on key types? 
    - Follow‑up: Why is `EnumMap` more efficient than `HashMap<Enum, V>` in both speed and memory?


34. How does `EnumMap` maintain iteration order?
    - Follow‑up: What happens if you try to insert a `null` key into an `EnumMap`?

***

## 9. Arrays vs `ArrayList`

35. Compare Java arrays and `ArrayList`: size, type safety, primitives support, and resizing behavior. 
    - Follow‑up: When would you prefer a raw array over an `ArrayList`?

36. Explain how `ArrayList` handles resizing internally. copy to an array and paste
    - Follow‑up: What is the default initial capacity, and by how much does it grow? 10 and 1.5x

37. What is the complexity of `get`, `add` (at end), and `remove` (in middle) for `ArrayList`?
    - Follow‑up: Why is `remove` from the middle slower? all O(n) - shift elements

***

## 10. `ArrayList` vs `LinkedList` vs `Vector`

38. How is `LinkedList` implemented internally?
    - Follow‑up: Show the node structure conceptually (prev, data, next).

39. Compare `ArrayList` and `LinkedList` in terms of:
    - Random access
    - Insertions/removals in the middle
    - Memory overhead
    - Follow‑up: Which would you use for an LRU cache implementation and why?

40. Compare `ArrayList` and `Vector`. 
    - Follow‑up: Why is `Vector` generally discouraged in modern Java code?

41. How does a `Stack` work in Java, and what is its relationship to `Vector`? 
    - Follow‑up: Why is `ArrayDeque` often recommended instead of `Stack`?

***

## 11. Removing duplicates, ordering, and list utilities

42. How can you remove duplicates from an `ArrayList` while preserving insertion order? 
    - Follow‑up: Explain why `LinkedHashSet` is a good fit for this.

43. Show code that converts an `ArrayList<Integer>` with duplicates into a duplicate‑free list preserving order.
    - Follow‑up: What is the time complexity of this approach?

44. When would you use `Collections.unmodifiableList` or `List.copyOf`?
    - Follow‑up: What exception is thrown if you try to modify such an unmodifiable list?

***

## 12. Set interface: `HashSet`, `LinkedHashSet`, `TreeSet`

45. What is `Set` and what main implementations does Java provide? 
    - Follow‑up: How do they differ in terms of ordering and performance?

46. How does `HashSet` maintain uniqueness? 
    - Follow‑up: What methods on the element type does it rely on?

47. Explain the internal working of `HashSet` with respect to collisions.
    - Follow‑up: What happens when the number of entries in a bucket exceeds the treeification threshold?

48. How does `LinkedHashSet` differ from `HashSet`?
    - Follow‑up: How does `LinkedHashSet` maintain insertion order internally?

49. Compare `HashSet` and `TreeSet`.
    - Follow‑up: What are the complexity differences for `add`, `remove`, `contains`?

50. What does `TreeSet` require from its elements?
    - Follow‑up: What happens if you add objects that don’t implement `Comparable` and no `Comparator` is supplied?

***

## 13. Queue, Deque, PriorityQueue, and concurrent queues

51. What are the core concepts of `Queue` and `Deque`?
    - Follow‑up: What is the difference between FIFO and LIFO in this context?

52. Explain how `LinkedList` can act as both `List` and `Deque`.
    - Follow‑up: Give example methods used when treating it as a queue vs as a stack.

53. What is `PriorityQueue` and how does it order its elements?
    - Follow‑up: Does iteration over a `PriorityQueue` guarantee sorted order?

54. How do blocking queues like `ArrayBlockingQueue` differ from non‑blocking ones?
    - Follow‑up: Give a use case in a producer–consumer scenario.

***

## 14. Thread safety and concurrent collections

55. Which standard collections are synchronized by default and which are not?
    - Follow‑up: How can you obtain a synchronized view of a non‑synchronized collection?

56. What is a fail‑fast iterator and how does it behave under concurrent modification?
    - Follow‑up: How do iterators of `ConcurrentHashMap` differ?

57. When should you prefer `ConcurrentHashMap` over `Collections.synchronizedMap(new HashMap<>())`?
    - Follow‑up: How does `ConcurrentHashMap` avoid locking the whole map for every operation?

58. Explain why `CopyOnWriteArrayList` is efficient for read‑heavy workloads.
    - Follow‑up: Why is it not a good choice for write‑heavy scenarios?

***

## 15. Identity, ordering, and custom comparators

59. How does `TreeMap` decide where to store keys?
    - Follow‑up: What is the role of `compareTo` or `Comparator` in `TreeMap` and `TreeSet`? 

60. What is the difference between equality as defined by `equals` and ordering as defined by `compareTo`?
    - Follow‑up: What can go wrong if `compareTo` and `equals` define inconsistent notions of equality?

61. How would you design a `Comparator<Employee>` that sorts by salary descending, then by name ascending?
    - Follow‑up: How would you use it in a `TreeSet<Employee>`?

***

If you want, next step I can turn this into a table grouped by “basic / intermediate / advanced” to structure your practice sets.