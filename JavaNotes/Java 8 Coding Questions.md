## Java 8 Hands-on Exercises


Note:
```java
Integer[] arr = {4,6,1,4,8,1,2,3,4,5,6};
List<Integer> list = Arrays.stream(arr).toList();
```
This list of integers is immutable (fixed-size). So, operations like removeIf will throw UnsupportedOperationException. To create a modifiable list, use:
```java
List<Integer> list = new ArrayList<>(Arrays.asList(arr));
```

### Beginner

**1.	Print a list using forEach and lambda**

`forEach` - void forEach(Consumer<? super T> action)

| Approach                                              | Performance (Time/Space)  | Notes                                 |
|-------------------------------------------------------|:-------------------------:|---------------------------------------|
| `names.forEach(name -> System.out.println(name))`     | Very efficient (in-place) | Lambda, concise, readable             |
| `names.forEach(System.out::println)`                  |             Very efficient (in-place)              | Method reference, most concise        |
| `Stream.of(2, 5, 8, 10).filter(n -> n % 2 == 0)`<br>`  .forEach(System.out::println)` | Very efficient (stream, lambda) | Combines filtering and printing       |

2.	Sort a list of integers using Stream

`sort()` -> void sort(Comparator<? super E> c)

| Approach                                                                 | Modifies Original List | Performance (Time/Space)         | Notes                                                      |
|--------------------------------------------------------------------------|:---------------------:|:-------------------------------:|------------------------------------------------------------|
| `numbers.sort((a, b) -> a - b)`                                          | Yes                   | Very efficient (in-place)        | Simple lambda, modifies original list                      |
| `numbers.sort((a, b) -> Integer.compare(a, b))`                          | Yes                   | Very efficient (in-place)        | Safer for overflow, modifies original list                 |
| `numbers.sort(Comparator.naturalOrder())`                                | Yes                   | Very efficient (in-place)        | Readable, modifies original list                           |
| `numbers.sort(Comparator.comparingInt(Integer::intValue))`               | Yes                   | Very efficient (in-place)        | Useful for custom objects, modifies original list          |
| `Collections.sort(numbers)`                                               | Yes                   | Very efficient (in-place)        | Classic approach, modifies original list                   |
| `List<Integer> sorted = numbers.stream().sorted().collect(Collectors.toList())` | No                    | Moderate (stream, lambda, new list) | Java 8+, creates a new sorted list, does not modify input  |
| `numbers.sort(Comparator.reverseOrder())`                                | Yes                   | Very efficient (in-place)        | Sorts in reverse, modifies original list                   |
| `Collections.sort(numbers, Collections.reverseOrder())`                   | Yes                   | Very efficient (in-place)        | Classic reverse sort, modifies original list               |

**Note -** a, b -> a-b ❌ (a,b) -> a-b ✅
a, b -> a - b, which looks like a two-parameter lambda, but you’ve accidentally written it with a comma before the arrow.


3.	Filter even numbers from a list using Stream 

`filter()` -> Stream<T> filter(Predicate<? super T> predicate)

`removeIf()` -> boolean removeIf(Predicate<? super E> filter)

| Approach                                                                          | Modifies Original List  | Performance (Time/Space)            | Notes                                                                 |
|-----------------------------------------------------------------------------------|:---------------------:|:------------------------------------|-----------------------------------------------------------------------|
| `numbers = numbers.stream().filter(x -> x % 2 == 0).toList()`                     | No                    | Moderate (stream, lambda, new list) | Java 16+, creates a new list, not in-place                            |
| `numbers = numbers.stream().filter(x -> x % 2 == 0).collect(Collectors.toList())` | No                    | Moderate (stream, lambda, new list) | Java 8+, creates a new list, not in-place                             |
| `numbers.removeIf(x -> x % 2 != 0)`                                               | Yes                   | Very efficient (in-place)           | Modifies original list, internal iteration                            |
| `CollectionUtils.filter(numbers, x -> x % 2 == 0)`                                | Yes                   | Efficient (in-place)                | Apache Commons Collections, modifies original list                     |
| `Traditional for-loop (copy evens to new list)`                                     | No                    | Very efficient (simple iteration)   | Manual, creates new list, no streams or lambdas                       |

4.	Map a list of Strings to their lengths using Stream

`collect()` -> <R,A> R collect(Collector<? super T,A,R> collector)

`Collectors` -> static <T,K,U> Collector<T,?,Map<K,U>> toMap(Function<? super T,? extends K> keyMapper, Function<? super T,? extends U> valueMapper)

| Approach                                                                                      | Modifies Original Map | Performance (Time/Space)         | Notes                                         |
|-----------------------------------------------------------------------------------------------|:--------------------:|:-------------------------------:|-----------------------------------------------|
| `list.forEach(x -> map.put(x, x.length()))`                                                   | Yes                  | Very efficient (in-place)        | Simple lambda, modifies original map          |
| `for(String key: list) map.put(key, key.length());`                                           | Yes                  | Very efficient (in-place)        | Classic for-loop, modifies original map       |
| `list.stream().forEach(x -> map.put(x, x.length()))`                                          | Yes                  | Very efficient (in-place)        | Stream, modifies original map                 |
| `Map<String, Integer> map = list.stream().collect(Collectors.toMap(x -> x, x -> x.length()))` | No                   | Moderate (stream, lambda, new map) | Java 8+, creates a new map, does not modify input |

5.	Create a method that takes a Predicate and filters a list

`predicate.test(item)` -> `boolean test(T t)` -> Evaluates this predicate on the given argument.

`list.removeIf(predicate.negate())` -> `boolean removeIf(Predicate<? super E> filter)` -> Removes all of the elements of this collection that satisfy the given predicate.

Note - Arrays.asList(…) gives you a fixed-size list, so calling removeIf will throw UnsupportedOperationException so use new ArrayList<>(Arrays.asList(...)) to create a modifiable list.

`Collectors.partitioningBy(predicate)` -> `Map<Boolean, List<T>> `

So, partitioningBy is a broader use-case tool than simple .filter(...) or removeIf(...).
-	If you just want one list → use filter.
-	If you need both matching & non-matching groups together → use partitioningBy.
-   It does not work with primitive data type, only with Object
```java
Map<Boolean, List<Integer>> partitioned = numbers.stream().collect(Collectors.partitioningBy(isEven));
System.out.println("Even numbers: " + partitioned.get(true));   // [2, 4, 6, 8, 10]
System.out.println("Odd numbers: " + partitioned.get(false));  // [1, 3, 5, 7, 9]
        
```

| Approach                                                                                    | Modifies Original List | Performance (Time/Space)            | Use Case                                              | Notes                                                                                 |
|---------------------------------------------------------------------------------------------|:---------------------:|:------------------------------------:|------------------------------------------------------|---------------------------------------------------------------------------------------|
| `list.stream().filter(predicate).collect(Collectors.toList())`                              | No                    | Moderate (stream, lambda, new list)  | General filtering, concise, functional style          | Java 8+, concise, creates a new filtered list                                         |
| `for (T item : list) {`<br>` if (predicate.test(item)) `<br>` result.add(item); }`          | No                    | Very efficient (simple iteration)    | When avoiding streams/lambdas, classic code           | Classic for-loop, manual filtering, creates a new list                                |
| `list.removeIf(predicate.negate()); `<br>` return list;`                                    | No                    | Efficient (in-place copy, predicate) | Copy and filter in-place, keep only matching elements | Copies list, removes non-matching, keeps only matching elements                       |
| `list.stream().collect(Collectors.partitioningBy(predicate)).get(true)`                     | No                    | Moderate (stream, lambda, new map)   | When partitioning into matching/non-matching groups   | Partitions into map, then gets matching list, less direct than filter                 |
| `list.forEach(`<br>`item -> { `<br>` if (predicate.test(item)) `<br>`result.add(item); });` | No                    | Efficient (lambda, manual)           | ForEach with lambda, manual filtering                 | Uses forEach with lambda, manual filtering, creates a new list                        |
| `list.parallelStream().filter(predicate).collect(Collectors.toList())`                      | No                    | Moderate (parallel stream, new list) | Large lists, parallel filtering                       | Parallelizes filtering, may improve performance for large lists, creates new list      |

6.	Find the first element greater than 50 in a list using Stream

`.findFirst()` -> Optional<T> findFirst() -> Returns an Optional describing the first element of this stream, or an empty Optional if the stream is empty.
- this is better than usimg filter and get(0) as it stops at first match. If the list is [6, 1, 2, ...], it finishes in O(1).

`removeIf` -> O(n) always (checks entire list).

| Approach                                                                 | Modifies Original List | Performance (Time/Space)         | Notes                                                      |
|--------------------------------------------------------------------------|:---------------------:|:-------------------------------:|------------------------------------------------------------|
| `numbers.removeIf(pred.negate(n -> n >= 5));`<br>`numbers.getFirst();`   | Yes                   | O(n)                            | Removes elements < 5, then gets first; checks all elements |
| `numbers.stream().filter(n -> n > 50).findFirst();`                      | No                    | O(k), stops at first match       | Stream, stops at first match, returns Optional             |

7.	Use Optional to avoid NullPointerException

| Approach & Method                              | Null Safety | Conciseness | Custom Default | Exception on Absent | Side Effects | Notes                                      |
|------------------------------------------------|:-----------:|:-----------:|:--------------:|:------------------:|:------------:|--------------------------------------------|
| `isPresent() / get()`                          | Yes         | Moderate    | No             | No                 | No           | Classic, can be verbose                    |
| `ifPresent(Consumer)`                          | Yes         | High        | No             | No                 | No           | Executes action if present                 |
| `ifPresentOrElse(Consumer, Runnable)`          | Yes         | High        | No             | No                 | No           | Handles both present and absent cases      |
| `orElse(defaultValue)`                         | Yes         | High        | Yes            | No                 | No           | Returns default if absent                  |
| `orElseGet(Supplier)`                          | Yes         | High        | Yes (lazy)     | No                 | Yes (if run) | Default computed only if absent            |
| `orElseThrow(Supplier)`                        | Yes         | High        | No             | Yes                | No           | Throws exception if absent                 |

**Performance:**  
- All methods are O(1) except for the Supplier in `orElseGet`, which is only invoked if the value is absent.  
- No significant memory overhead; Optional is a lightweight wrapper.  
- `ifPresentOrElse` requires Java 9+.

```java
        Optional<Integer> first = numbers.stream()
        .filter(n -> n > 5)
        .findFirst();

        //isPresent
        if(first.isPresent()) {
        System.out.println("Found: " + first.get());
        } else {
        System.out.println("Not found");
        }
        
        //ifPresent
        first.ifPresent(v -> System.out.println("Found: " + v));

        //  ifPresentOrElse
        first.ifPresentOrElse(
        v -> System.out.println("Found: " + v),
                () -> System.out.println("Not found")
        );

        //  Default value
        int safe = first.orElse(-1);

        // Lazy default
        int lazy = first.orElseGet(() -> {
        System.out.println("Computing default...");
        return 99;});

        //  Throw exception if absent
        int mustExist = first.orElseThrow(() -> new RuntimeException("No number > 5"));
```

8.	Sum all elements in an integer list

| Approach                                                        | Returns              | Performance (Time/Space) | Notes                                              |
|-----------------------------------------------------------------|----------------------|:-----------------------:|----------------------------------------------------|
| `Optional<Integer> sum = numbers.stream().reduce(Integer::sum)` | Optional<Integer>    | O(n) / O(1)             | Returns Optional, may be empty if list is empty     |
| `numbers.stream().reduce(0, Integer::sum)`                      | int                  | O(n) / O(1)             | Returns 0 for empty list, avoids Optional           |
| `numbers.stream().mapToInt(Integer::intValue).sum()`            | int                  | O(n) / O(1)             | Most concise, uses primitive stream, avoids boxing  |

Both approaches avoid returning an `Optional`. The second is the most concise.

`numbers.forEach(x -> sum += x); // ❌ Compilation error`
- Here, sum is reassigned (sum += x). So it’s no longer “effectively final”.

9.	Collect a list of objects’ names into a Set using Collectors

| Approach                                                                                                       | Performance                | Use Case / Notes                                      |
|----------------------------------------------------------------------------------------------------------------|----------------------------|-------------------------------------------------------|
| `Set<String> names = employees.stream().map(Employee::getName).collect(Collectors.toSet())`                    | O(n), concise, no order    | Most common, collects unique names into a Set         |
| `for (Employee e : employees){ `<br>`  names.add(e.getName()); }`                                              | O(n), explicit, no order   | Useful for custom logic inside loop                   |
| `Set<String> names = employees.stream().map(Employee::getName).collect(Collectors.toCollection(HashSet::new))` | O(n), explicit Set type    | Specify Set implementation (e.g., HashSet, LinkedHashSet) |
| `Set<String> names = employees.stream().map(e -> e.getName()).collect(Collectors.toSet())`                     | O(n), concise, no order    | Lambda version, same as method reference              |

**Use Case:**  
- Use Stream API for concise, functional code and when you want to avoid manual iteration.  
- Use classic for-loop if you need more control or additional logic per element.  
- Use `toCollection(...)` to specify a particular Set implementation.

10.	Convert a String list to uppercase using Stream#map

| Approach                                                                                                               | Performance                | Use Case / Notes                                              |
|------------------------------------------------------------------------------------------------------------------------|----------------------------|---------------------------------------------------------------|
| `List<String> names = employees.stream().map(e -> e.getName().toUpperCase()).collect(Collectors.toList())`             | O(n), concise, functional | Best for concise, readable code using streams and lambdas     |
| `names.add(e.getName().toUpperCase())`                                                                                 | O(n), explicit, imperative | Useful when you need more control or additional logic per item |
| `List<String> names = employees.stream().map(Employee::getName).map(String::toUpperCase).collect(Collectors.toList())` | O(n), concise, functional | Shows method reference chaining, very readable                |
| `List<String> names = employees.forEach(e -> names.add(e.getName().toUpperCase()))`                                                         | O(n), explicit, imperative | Good for side effects or when collecting into an existing list |

- Between #1 and #3, 
  - is better for readability and maintainability (clean separation of steps, easier to extend).
  - #1 is fine for short, simple transformations when you don’t care about chaining clarity.


### Intermediate
11.	Flatten a list of lists 

`flatMap()` -> <R> Stream<R> flatMap(Function<? super T,? extends Stream<? extends R>> mapper) -> Returns a stream consisting of the results of replacing each element of this stream with the contents of a mapped stream produced by applying the provided mapping function to each element.

| Approach                                                                                         | Performance                | Notes                                                      |
|--------------------------------------------------------------------------------------------------|----------------------------|------------------------------------------------------------|
| `List<Integer> flatList = listOfLists.stream().flatMap(List::stream).collect(Collectors.toList())` | O(n), concise, functional | Most common, concise, uses method reference               |
| `List<Integer> flatList = new ArrayList<>();`<br>`for (List<Integer> sublist : listOfLists) {`<br>` flatList.addAll(sublist); }` | O(n), explicit, imperative | Classic for-loop, useful for custom logic per sublist |
| `List<Integer> flatList = listOfLists.stream().flatMap(l -> l.stream()).collect(Collectors.toList())` | O(n), concise, functional | Lambda version, same as method reference                    |
| `List<Integer> flatList = listOfLists.stream().reduce(new ArrayList<>(), (acc, sublist) -> { acc.addAll(sublist); return acc; });` | O(n), less efficient       | Uses reduce, less common, more complex than flatMap |

12.	Group list of employees by department using Collectors.groupingBy

`Collectors.groupingBy()` -> static <T,K> Collector<T,?,Map<K,List<T>>> groupingBy(Function<? super T,? extends K> classifier) -> Returns a Collector implementing a "group by" operation on input elements of type T, grouping elements according to a classification function, and returning the results in a Map.

| Approach                                                                                                      | Performance                | Notes                                                      |
|---------------------------------------------------------------------------------------------------------------|----------------------------|------------------------------------------------------------|
| `Map<String, List<Employee>> grouped = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment))` | O(n), concise, functional | Most common, concise, uses method reference               |
| `Map<String, List<Employee>> grouped = new HashMap<>();`<br>`for (Employee e : employees) {`<br>` grouped.computeIfAbsent(e.getDepartment(), k -> new ArrayList<>()).add(e); }` | O(n), explicit, imperative | Classic for-loop, useful for custom logic per employee |
| `Map<String, List<Employee>> grouped = employees.stream().collect(Collectors.groupingBy(e -> e.getDepartment()))` | O(n), concise, functional | Lambda version, same as method reference                    |
| `Map<String, List<Employee>> grouped = new HashMap<>();`<br>`for (Employee e : employees) {`<br>` String dept = e.getDepartment();`<br>` if (!grouped.containsKey(dept)) { grouped.put(dept, new ArrayList<>()); }`<br>` grouped.get(dept).add(e); }` | O(n), explicit, imperative | | More verbose, manual check for key existence |

13.	Find the max/min salary among employees using Stream

- `max((e1, e2) -> Double.compare(e1.getSalary(), e2.getSalary()))` -> Optional<T> max(Comparator<? super T> comparator) -> Returns the maximum element of this stream according to the provided Comparator.
- `reduce((e1, e2) -> e1.getSalary() > e2.getSalary() ? e1 : e2)` -> Optional<T> reduce(BinaryOperator<T> accumulator) -> Performs a reduction on the elements of this stream, using an associative accumulation function, and returns an Optional describing the reduced value, if any.
- 

| Approach	                          | Code Snippet	                                                                                                                                 | Efficiency	                   | Readability	        | Safety (Null/Empty Handling)	                | Notes                                                                 |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|---------------------|----------------------------------------------|-----------------------------------------------------------------------|
| 1. Stream + max() (Method Reference)	 | `Arrays.stream(employees).max(Comparator.comparingDouble(Employee::getSalary)).get();`	                                                        | ✅ O(n), efficient	            | ✅ Very clean	       | ⚠️ .get() throws if empty (use orElseThrow)	 | Best balance of clarity + efficiency.                                 |
| 2. Stream + max() (Lambda)	        | `Arrays.stream(employees).max((e1, e2) -> Double.compare(e1.getSalary(), e2.getSalary())).get();`	                                             | ✅ O(n)	                       | ➖ Slightly verbose	 | ⚠️ Same .get() issue	                        | Good if you prefer explicit comparator logic.                         |
| 3. Stream + reduce()	              | `Arrays.stream(employees).reduce((e1, e2) -> e1.getSalary() > e2.getSalary() ? e1 : e2).get();`	                                               | ✅ O(n)	                       | ➖ More complex	     | ⚠️ .get() issue	                             | Useful if custom tie-breaking logic needed.                           |
| 4. Collections.max()	              | `Collections.max(Arrays.asList(employees), Comparator.comparingDouble(Employee::getSalary));`	                                                 | ✅ O(n)	                       | ✅ Very simple	      | ⚠️ Throws if empty	                          | Best when working with lists instead of streams.                      |
| 5. Traditional For-loop	           | `Employee highest = employees[0]; for (Employee e : employees) { if (e.getSalary() > highest.getSalary()) highest = e; }`                      | ✅ O(n)	                       | ➖ Verbose	          | ⚠️ Breaks if array empty	                    | Old-school, explicit. Useful in interviews or non-Java 8+ codebases.  |
| 6. Stream + sorted().findFirst()	  | `Arrays.stream(employees).sorted(Comparator.comparingDouble(Employee::getSalary).reversed()).findFirst().get();`	                               | ❌ O(n log n), less efficient	 | ➖ Verbose	          | ⚠️ .get() issue	                             | Avoid for max — sorts entire array unnecessarily.                     |
| 7. Stream + Collectors.maxBy()	    | `Arrays.stream(employees).collect(Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))).get();`	                                   | ✅ O(n)	                       | ➖ More boilerplate	 | ⚠️ .get() issue	                             | Overkill for simple max, but fits if you’re already using collectors. |
| 8. IntStream.range()	              | `IntStream.range(0, employees.length).mapToObj(i -> employees[i]).max(Comparator.comparingDouble(Employee::getSalary)).get();`	                 | ✅ O(n)                        | 	❌ Least readable	  | ⚠️ .get() issue	                             | Useful if you need indexes alongside objects.                         |
| 9. Stream + orElseThrow()	         | `Arrays.stream(employees).max(Comparator.comparingDouble(Employee::getSalary)).orElseThrow(() -> new RuntimeException("No employees found"));`	 | ✅ O(n)	                       | ✅ Clean	            | ✅ Safe (no silent nulls)	                    | Best practice in production code — avoids .get().                     | 

---

14.	Use Stream#peek for debugging a pipeline

`peek()` -> Stream<T> peek(Consumer<? super T> action) -> Returns a stream consisting of the elements of this stream, additionally performing the provided action on each element as elements are consumed from the resulting stream.

```java
employees.stream()
            .peek(e -> System.out.println("Original: " + e))  // Debug input
            .filter(e -> e.getSalary() > 55000)
            .peek(e -> System.out.println("After filter (>55k): " + e)) // Debug after filter
            .max(Comparator.comparingDouble(Employee::getSalary))
            .orElseThrow();
```

1.	Intermediate operation: Runs lazily (only when a terminal op like max, collect, forEach is invoked).
2.	Non-interfering: Should not modify elements — only observe. (Think: logging, metrics, debugging).
3.	Danger: Some devs misuse it to mutate data → violates functional programming principles. Use map instead if you want to transform.
4.	Best Use Case: Debugging complex pipelines (like filter, map, flatMap chains).

---


15.	Implement a custom functional interface
```java
@FunctionalInterface
public interface A {
    public abstract void show(); // ONLY ONE ABSTRACT METHOD
}

class B {
    public static void main(String[] args) {
        A a = () -> System.out.println("Hello from lambda show method"); // Implementation of show() method Using lambda expression
        a.show();
    }
}
```
---

16.	Write a function that chains two consumers

| Approach                                                                 | Performance                | Notes                                                      |
|--------------------------------------------------------------------------|----------------------------|------------------------------------------------------------|
| `c1.andThen(c2)`                                                        | O(n), concise, functional  || Most common, concise, uses method reference                           |
| `for (Num n : nums) { c1.accept(n); c2.accept(n); }` | O(n), explicit, imperative | Classic for-loop, useful for custom logic per item |
| `Arrays.stream(nums).forEach(n -> { c1.accept(n); c2.accept(n); });` | O(n), concise, functional | Lambda version, same as method reference                    |  

```java
Num[] nums = new Num[5];
for(int i=0;i<5;i++){
    nums[i] = new Num(i+1);
}

Consumer<Num> c1 = System.out::println;
Consumer<Num> c2 = a -> a.val+=1;
```

```java
1. Arrays.stream(nums).forEach(c2.andThen(c1));

2. Arrays.stream(nums).forEach(c1.andThen(c2).andThen(c1));

3. Arrays.stream(nums).forEach(x -> {
    c1.accept(x);
    c2.accept(x);
    c1.accept(x);});

4. Arrays.stream(nums).forEach(x -> c1.andThen(c2).andThen(c1).accept(x));
```

17.	Remove duplicates from a list using Stream#distinct

- distinct() keeps the first occurrence and removes duplicates later in the stream.
- O(n) average (hashing)  worst O(n²) if many hash collisions
- Avoid parallel streams with distinct unless dataset is extremely large.

```java
Num[] arr = {new Num(1), new Num(2), new Num(1)};
System.out.println(Arrays.stream(arr).distinct().toList());
```

- Output without hashcode and equals overridden:
```text
[Num{val=1}, Num{val=2}, Num{val=1}]
```

- Output with hashcode and equals overridden:
```text
[Num{val=1}, Num{val=2}]
```
**Alternate ways:**
1. Using Set:
```java
Set<Num> set = new HashSet<>(Arrays.asList(arr));
System.out.println(new ArrayList<>(set));
```
2. Using LinkedHashSet to maintain order:
```java
List<Integer> unique = new ArrayList<>(new LinkedHashSet<>(numbers));
``` 
- Faster (no Stream overhead)
- Maintains insertion order


Note: Unique by a specific field (avoid full object compare)
```java
List<Employee> uniqueByName = employees.stream()
    .collect(Collectors.collectingAndThen(
        Collectors.toMap(Employee::getName, e -> e, (e1, e2) -> e1),
        m -> new ArrayList<>(m.values())
    ));
```
or
```java
List<Employee> uniqueByName = employees.stream()
    .filter(new HashSet<>()::add) // Uses Set to track seen names with overridden equals/hashcode
    .collect(Collectors.toList());
```
or
```java
List<Person> uniqueByName = people.stream()
    .collect(Collectors.toMap(
        Person::getName,
        Function.identity(),
        (existing, replacement) -> existing,
        LinkedHashMap::new
    ))
    .values()
    .stream().toList();
```

18.	Find average of list of doubles using Stream

| Approach                                                                 | Performance                | Notes                                                      |
|--------------------------------------------------------------------------|----------------------------|------------------------------------------------------------|
| `double avg = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);` | O(n), concise, functional  | Most common, concise, uses method reference                           |
| `double sum = 0; for (Double d : numbers) { sum += d; } double avg = numbers.isEmpty() ? 0 : sum / numbers.size();` | O(n), explicit, imperative | Classic for-loop, useful for custom logic per item |
| `double avg = numbers.stream().mapToDouble(d -> d).average().orElse(0.0);` | O(n), concise, functional | Lambda version, same as method reference                    |  

**Note:**
- Stream<T> (object streams) → no average(). Must map to primitive first (mapToInt, mapToDouble, mapToLong).
- IntStream, DoubleStream, LongStream → have average() because they are numeric.

Case 1: Double[] arr
```java
Double[] arr = {4.0,6.1,1.3,4.4,8.3,1.4,2.5,3.5,43.4,5.6};

Arrays.stream(arr)  // ➝ Stream<Double> (object stream)
    .average();     // ❌ ERROR: Stream<T> has no average()
```
- Arrays.stream(Double[]) produces a Stream<Double>.
- Generic Stream<T> has no numeric methods like sum(), average(), max(), etc.
- Fix → convert to a primitive stream:
```java
double avg = Arrays.stream(arr)
.mapToDouble(Double::doubleValue) // Stream<Double> → DoubleStream
.average()
.orElse(0.0);
```

Case 2: int[] arr
```java
int[] arr = {4,6,1,4,8,1,2,3,43,5};

double avg = Arrays.stream(arr)  // ➝ IntStream (primitive stream)
.average()    // ✅ available here
.orElse(0.0);
System.out.println(avg);
```
- Arrays.stream(int[]) produces an IntStream.
- Primitive streams (IntStream, DoubleStream, LongStream) support numeric aggregations directly (sum(), average(), max(), min(), etc.).
- boxing/unboxing overhead.

19.	
20.	

### Advanced
21.	Write a custom Collector to collect elements into a LinkedList

- Current Java implementation for `Collectors.toCollection(LinkedList::new)` is already efficient and concise.
Internally :
```java
public static <T, C extends Collection<T>>
    Collector<T, ?, C> toCollection(Supplier<C> collectionFactory) {
        return new CollectorImpl<>(collectionFactory, Collection::add,
                                   (r1, r2) -> { r1.addAll(r2); return r1; },
                                   CH_ID);
    }
```
This is generic for all the Collection types.
Explanation of each part:


1. Generics
```java
<T, C extends Collection<T>>
```
	-	T → the type of elements being collected (e.g., String, Integer).
	-	C → a specific collection type that can hold T (e.g., ArrayList<String>, HashSet<Integer>).

So this method returns a Collector<T, ?, C> that collects elements of type T into a custom collection type C.


2. Collector Signature

Collector<T, A, R> has 3 type params:
	-	T → Input element type.
	-	A → Accumulation type (intermediate container).
	-	R → Result type (final container).

Here:
```java
Collector<T, ?, C>
```
	-	Input: T (elements being streamed).
	-	Intermediate: ? (hidden by CollectorImpl, in practice same as C).
	-	Result: C (the collection itself).


3. Supplier
`
collectionFactory
`
	-	Provides a new empty collection when needed.
	-	Example: ArrayList::new, LinkedList::new, HashSet::new.
	-	This ensures flexibility, you decide what kind of collection the stream should end up in.


4. Accumulator
`
Collection::add
`
	-	Defines how elements are added to the collection.
	-	For each element t, do collection.add(t).


5. Combiner
`
(r1, r2) -> { r1.addAll(r2); return r1; }
`
	-	Defines how two partial results are merged (important in parallel streams).
	-	Merges r2 into r1 and returns r1.
	-	Example: combine two ArrayLists by doing list1.addAll(list2).


6. Characteristics
`CH_ID`
	-	Characteristics tell the stream framework about the collector’s behavior.
	-	Common ones:
	-	IDENTITY_FINISH → The accumulator type is also the result type (no finishing step needed).
	-	CONCURRENT → Collector can be used by multiple threads safely.
	-	UNORDERED → Collector doesn’t care about encounter order.

Here CH_ID means Identity Finish.

**Custom Collector for LinkedList**

```java
import java.util.*;
import java.util.stream.Collector;

public class LinkedListCollector<T> implements Collector<T, LinkedList<T>, LinkedList<T>> {

    @Override
    public Supplier<LinkedList<T>> supplier() {
        // Creates a new empty LinkedList
        return LinkedList::new;
    }

    @Override
    public BiConsumer<LinkedList<T>, T> accumulator() {
        // Adds an element into the LinkedList
        return LinkedList::add;
    }

    @Override
    public BinaryOperator<LinkedList<T>> combiner() {
        // Merges two partial LinkedLists (important in parallel streams)
        return (list1, list2) -> {
            list1.addAll(list2);
            return list1;
        };
    }

    @Override
    public Function<LinkedList<T>, LinkedList<T>> finisher() {
        // Identity finish: accumulation type == result type
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        // Identity finish → no extra transformation needed
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    }
}
```

**How to Use**
```java
import java.util.*;
import java.util.stream.*;

public class TestCustomCollector {
    public static void main(String[] args) {
        LinkedList<String> result = Stream.of("A", "B", "C", "D")
            .collect(new LinkedListCollector<>());

        System.out.println(result); // [A, B, C, D]
    }
}
```
---

22.	Parallelize a stream and measure the time taken for sum



23.	Implement lazy evaluation with infinite streams (Stream#iterate)
24.	Collect statistics (sum, average, min, max) using IntSummaryStatistics
25.	Implement chain of operations with Function#andThen
26.	Use BiFunction to merge entries in a Map
27.	Write a comparator using Comparator.comparing thenComparing
28.	Build a pipeline that transforms, filters, and summarizes data
29.	Use map.computeIfAbsent() for caching expensive computations
30.	Refactor a classic for-loop-based code to use Streams and lambdas