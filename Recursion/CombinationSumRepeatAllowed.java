package Recursion;

import java.util.ArrayList;
import java.util.List;

public class CombinationSumRepeatAllowed {


    public static void main(String[] args) {
        int[] candidates = {2, 3, 6, 7};
        int target = 7;
        List<List<Integer>> result = combinationSum(candidates, target);
        System.out.println(result);
    }


    public static List<List<Integer>> combinationSum(int[] nums, int k) {
        //your code goes here
        int n = nums.length;
        List<Integer> current = new ArrayList<>();
        List<List<Integer>>  res = new ArrayList<>();
        whereSumExist(0, res, current, nums, n , k);
        return res;
    }

    private static void whereSumExist(int index, List<List<Integer>> res, List<Integer> current, int[] nums, int n, int k){

        // if k is zero means we have the list with the target sum
        if(k==0){
            res.add(new ArrayList<>(current));
            return;
        }

        // check if k is negative or we have reached the end of the array
        // in both the cases we dont need to explore that possibility further
        if(k<0 || index == n)
            return;

        // include the current element
        // not incrementing the index because we can use the same element again
        current.add(nums[index]);
        whereSumExist(index, res, current, nums, n, k-nums[index]);

        // backtrack and remove the last added element
        // now explore the possibility of not including the current element
        current.remove(current.size()-1);
        whereSumExist(index+1, res, current, nums, n, k);
    }
}
