package DSA.GreedyAlgorithms;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;


public class Temp {


    static class Employee {
        private String name;
        private int age;

        public Employee(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }


    public static void main(String[] args) {
        // Collect a list of objects’ names into a Set using Collectors
        List<Employee> employees = Arrays.asList(
                new Employee("Alice", 30),
                new Employee("Bob", 25),
                new Employee("Charlie", 28),
                new Employee("Alice", 30) // duplicate name
        );

        List<String> names =  employees.stream()
                .map(Employee::getName)
                .map(String::toUpperCase)
                .toList();



    }

    public static <T> List<T> filterList(List<T> list, Predicate<T> predicate) {

        return list;
    }

}
