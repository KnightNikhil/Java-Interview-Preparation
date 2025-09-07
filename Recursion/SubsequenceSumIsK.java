package Recursion;

import java.util.ArrayList;
import java.util.List;

public class SubsequenceSumIsK {

    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 4};
        int target = 5;
        System.out.println(checkSubsequenceSum(nums, target)); // Expected output: true
    }

    // This method initiates the recursive process
    public static boolean checkSubsequenceSum(int[] nums, int target) {
        int n = nums.length; // Get the length of the input array
        if(n==1)    return nums[0]==target;

        return solve(0, n, nums, target); // Start the recursive process
    }

    // This method recursively checks for the subsequence with the given sum
    public static boolean solve(int i, int n, int[] arr, int k) {

        // Base case: if k is 0, a valid subsequence has been found, with target sum
        if (k == 0) {
            return true;
        }
        // Base case: if k is negative, no valid subsequence can be found, no need to explore further
        if (k < 0) {
            return false;
        }

        // Base case: if all elements are processed, index =n, check if k is 0
        if (i == n) {
            return k == 0;
        }

        // Recursive call: include the current element in the subsequence
        // or exclude the current element from the subsequence
        return solve(i + 1, n, arr, k - arr[i]) || solve(i + 1, n, arr, k);
    }

    /*
    In this solution, we are using recursion to generate all possible subsequences of the given array and checking if any of those subsequences sum up to k.
     */
    class UnOptimalSolution {
        public boolean checkSubsequenceSum(int[] nums, int k) {
            //your code goes here
            int n = nums.length;
            if(n==1)    return nums[0]==k;
            List<Integer> current = new ArrayList<>();
            return ifSumExist(0, false, current, nums, n , k);

        }

        private boolean ifSumExist(int index, boolean res, List<Integer> current, int[] nums, int n, int k){
            if(index == n){
                if(k==current.stream().reduce(0, Integer::sum))
                    res= true;
                return res;
            }
            current.add(nums[index]);
            if (!res)
                res = ifSumExist(index+1, res, current, nums, n, k);
            current.remove(current.size()-1);
            if (!res)
                res = ifSumExist(index+1, res, current, nums, n, k);
            return res;
        }
    }
}
