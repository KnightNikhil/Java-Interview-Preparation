package Recursion;

import java.util.ArrayList;
import java.util.List;

public class PowerSet {
    public static List<List<Integer>> powerSet(int[] nums) {
        //your code goes here
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> arr = new ArrayList<>();
        int n = nums.length, index =0;
        calAllSubset(index, arr, res, nums, n);
        return res;
    }

    private static void calAllSubset(int index, List<Integer>  arr, List<List<Integer>> res, int[] nums, int n){
        if(index == n) {
            res.add(new ArrayList<>(arr));
            return;
        }

        calAllSubset(index+1, arr, res, nums, n);
        arr.add(nums[index]);
        calAllSubset(index+1, arr, res, nums, n);
        arr.removeLast();
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};
        List<List<Integer>> result = powerSet(nums);

        // Print the result
        for (List<Integer> subset : result) {
            System.out.println(subset);
        }
    }


}
