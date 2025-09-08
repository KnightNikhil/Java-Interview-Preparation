package DSA.Recursion;

import java.util.ArrayList;
import java.util.List;

public class PalindromePartioning {

    public static void main(String[] args) {

        String s = "aab";
        List<List<String>> result = partition(s);
        for (List<String> partition : result) {
            System.out.println(partition);
        }
    }

    private static List<List<String>> partition(String s) {
        List<List<String>> rs = new ArrayList<>();
        List<String> current = new ArrayList<>();
        findAllPartition(0, s, rs, current);
        return rs;
    }

    private static void findAllPartition(int index, String s, List<List<String>> rs, List<String> current) {
        if(index == s.length()){
            System.out.println(current);
            rs.add(new ArrayList<>(current));
            return;
        }

        /*
         partioning the string from index to i and checking if it is palindrome or not
         if it is palindrome then we will add it to current list and call the function recursively
         with next index as i+1
         after that we will remove the last added element from current list and continue the loop
         this way we will explore all the possibilities

         partion will happen as: for aab
         first : palindrome, so added to current list > 'a' | recursive call for -> ab
         then: currentList -> a | palindrome, so added to current list > a | recursive call for -> b
         then:   currentList -> a , a, | palindrome, so added to current list > b | recursive call for -> ""
         then: currentList -> a , a, b | index == s.length() so add to result
         then backtracking and similarly for : aa | b
         then: aab (not palindrome so not added)
        */
        for(int i=index;i<s.length();i++){
            String ss = s.substring(index,i+1);
            if(isPalindrome(ss)){
                current.add(ss);
                findAllPartition(i+1, s, rs, current);
                current.remove(current.size()-1);
            }
        }

    }

    private static boolean isPalindrome(String s) {
       int start = 0, end = s.length()-1;
       while(start<end){
           if(s.charAt(start)!= s.charAt(end))
               return false;
           start++;
           end--;
       }
         return true;
    }

}
