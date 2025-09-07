package Recursion;

import java.util.*;
import java.util.stream.Collectors;

public class CombinationSumRepeatConstrainUnique {


    public static void main(String[] args) {

        // Sample input
        int[] candidates = {10, 1, 2, 7, 6, 1, 5};
        int target = 8;

        // Call the combinationSum2 function
        List<List<Integer>> result = combinationSum2(candidates, target);

        // Output the result
        System.out.println("Combinations are: "+ result);
        for (List<Integer> combination : result) {
            for (int num : combination) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }

    public static List<List<Integer>> combinationSum2(int[] nums, int k) {
        //your code goes here
        int n = nums.length;
        Arrays.sort(nums);
        List<Integer> current = new ArrayList<>();
        List<List<Integer>>  res = new ArrayList<>();
        whereSumExist(0, res, current, nums, n , k);
        return res;
    }

    private static void whereSumExist(int index, List<List<Integer>> res, List<Integer> current, int[] nums , int n, int k){

        if(k==0){
            res.add(new ArrayList(current));
            return;
        }

        if(k<0 || index == n)
            return;

        current.add(nums[index]);
        whereSumExist(index+1, res, current, nums, n, k-nums[index]);

        current.remove(current.size()-1);


        // skip the duplicates
        // lets say we have 1,1,2,5 and we are at index 0
        // we will include the first 1 and explore all the possibilities
        // but when we are backtracking and we want to explore the possibility of not including 1
        // we will skip all the duplicates of 1 and move to 2 directly
        // this way we will not have duplicate combinations in our result
        for(int i=index+1;i<n;i++){
            if(nums[i]!=nums[index]){
                whereSumExist(i, res, current, nums, n, k);
                break;
            }
        }
    }


}
