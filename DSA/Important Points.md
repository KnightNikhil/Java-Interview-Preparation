1. Arrays.sort(students); - inline sorting of array - no return value
2. For cases like - 
```java
while (pointer1<students.length && pointer2<cookies.length){
if(cookies[pointer2]>=students[pointer1]){
maxStudents++;
pointer2++;
pointer1++;
} else{
pointer2++;
}
}
```

pointer2 needs to be incremented everytime, so we can aslo do
```java
for(pointer2=0;pointer1<students.length && pointer2<cookies.length;pointer2++){
    if(cookies[pointer2]>=students[pointer1]){
        maxStudents++;
        pointer1++;
    } 
}

```

also, since maxStudents and pointer1 both are used only on the same condition and same way, we can return pointer1 as well, no need for maxStudents variable. 