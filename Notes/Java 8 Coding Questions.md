## Java 8 Hands-on Exercises

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

`Collectors.partitioningBy(predicate)` -> `Map<Boolean, List<T>> `

So, partitioningBy is a broader use-case tool than simple .filter(...) or removeIf(...).
-	If you just want one list → use filter.
-	If you need both matching & non-matching groups together → use partitioningBy.
```java
Map<Boolean, List<Integer>> partitioned =
        numbers.stream().collect(Collectors.partitioningBy(isEven));
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


7.	Use Optional to avoid NullPointerException


8.	Sum all elements in an integer list with Stream#reduce


9.	Collect a list of objects’ names into a Set using Collectors


10.	Convert a String list to uppercase using Stream#map

### Intermediate
11.	Flatten a list of lists using flatMap
12.	Group list of employees by department using Collectors.groupingBy
13.	Find the max/min salary among employees using Stream
14.	Use Stream#peek for debugging a pipeline
15.	Implement a custom functional interface
16.	Write a function that chains two consumers
17.	Remove duplicates from a list using Stream#distinct
18.	Partition numbers into even and odd using partitioningBy
19.	Use method reference to sort objects
20.	Find average of list of doubles using Stream

### Advanced
21.	Write a custom Collector to collect elements into a LinkedList
22.	Parallelize a stream and measure the time taken for sum
23.	Implement lazy evaluation with infinite streams (Stream#iterate)
24.	Collect statistics (sum, average, min, max) using IntSummaryStatistics
25.	Implement chain of operations with Function#andThen
26.	Use BiFunction to merge entries in a Map
27.	Write a comparator using Comparator.comparing thenComparing
28.	Build a pipeline that transforms, filters, and summarizes data
29.	Use map.computeIfAbsent() for caching expensive computations
30.	Refactor a classic for-loop-based code to use Streams and lambdas