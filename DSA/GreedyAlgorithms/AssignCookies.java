package DSA.GreedyAlgorithms;

import java.util.Arrays;

// Maximize the number of students assigned with cookies and output the maximum number.
public class AssignCookies {
    public static void main(String[] args) {
        int[] student = new int[]{1, 2, 3};
        int[] cookie = new int[]{1,1,3, 1};
        System.out.println("Maximum number of students assigned with cookies: "+ maxNumOfStudentWithCookies(student, cookie));

    }

    private static int maxNumOfStudentWithCookies(int[] students, int[] cookies) {
        int maxStudents = 0;
        Arrays.sort(students);
        Arrays.sort(cookies);
        int pointer1 = 0, pointer2= 0;
        while (pointer1<students.length && pointer2<cookies.length){
            if(cookies[pointer2]>=students[pointer1]){
                maxStudents++;
                pointer2++;
                pointer1++;
            } else{
                pointer2++;
            }
        }
        return maxStudents;
    }

    /*
    CORRECT APPROACH::
     sort the students with min num of cookies req to satisfy bcoz we want to max the no of students
     also, it is good to sort cookies count bcoz through this we will be easily able to identify the satisfaction possibility of student present in line
     if the student is satisfied with the cookie present in line, give him
     else, try with the next possible cookie
     it does not matter to us, if the student wants 2 cookies, and we gave 3 and the student who wanted 3 doesn't get any
     as in both the scenario the count is still 1, count matters not the student.



     */
}
