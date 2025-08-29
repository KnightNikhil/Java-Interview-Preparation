## Java 8 Hands-on Exercises

### Beginner

**1.	Print a list using forEach and lambda**

`forEach` - void forEach(Consumer<? super T> action)
```java
    List<String> names = Arrays.asList("John", "Jane", "Mark");
    // Using forEach with lambda
    names.forEach(name -> System.out.println(name));
    // Using forEach with method reference
    names.forEach(System.out::println);
```
```java
    Stream.of(2, 5, 8, 10)
          .filter(n -> n % 2 == 0)
            .forEach(System.out::println);

```

2.	Sort a list of integers using Stream
```java
    numbers.sort() -> exception as comparator is must as argument
    
    // VALID:
    numbers.sort((a,b) -> a-b); 
    numbers.sort((a, b) -> Integer.compare(a, b));
    numbers.sort(Comparator.naturalOrder());
    numbers.sort(Comparator.comparingInt(Integer::intValue));
    
    // Alternatives:
    Collections.sort(numbers);
    List<Integer> sorted = numbers.stream().sorted().collect(Collectors.toList());

    // REVERSE ORDER:
    numbers.sort(Comparator.reverseOrder());
    Collections.sort(numbers, Collections.reverseOrder());
```
3.	Filter even numbers from a list using Stream
4.	Map a list of Strings to their lengths using Stream
5.	Create a method that takes a Predicate and filters a list
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