package DSA.Recursion;

import java.util.ArrayList;
import java.util.List;

public class CombinationSum3 {

    public static List<List<Integer>> combinationSum3(int k, int n) {
        //your code goes here

        List<List<Integer>> res = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        findFeasibleCombination(1, res, current ,n, k);
//        LessOptimalFindFeasibleCombination(0,nums, res, current ,n, k);
        return res;
    }

    private static void findFeasibleCombination(int last, List<List<Integer>> res, List<Integer> current, int target, int k) {
        if(target==0 && current.size()==k){
            res.add(new ArrayList<>(current));
            return;
        }
        if(target<0 || current.size()>k)
            return;

        // either we add element and then explore the possibilities or we dont add the element and explore the possibilities
        // WAY 1:
        /*
            current.add(nums[index]);
            findFeasibleCombination(index+1, nums, res, current ,target-nums[index], k);
            current.remove(current.size()-1);
            findFeasibleCombination(index+1, nums, res, current ,target,k);
        */

        //WAY 2:
        /* even in this case, it is doing the same thing, we add element, send it to explore the possibilities
           and then we remove the element and explore the possibilities without adding that element
           but here we are using a loop to iterate through all the elements from last added element to 9
           and in above way we are just calling the next index, so in this way we are reducing the number of calls
           as we are directly jumping to the next index instead of calling for each index
        */
        for(int i=last; i<=9;i++){
            if(i<=target){
                current.add(i);
                findFeasibleCombination(i+1, res, current ,target-i, k);
                current.remove(current.size()-1);
            }
            else break;
        }


    }


    // all the valid lists will be checked, if sum and size matches then only add to result
    public static void LessOptimalFindFeasibleCombination(int index,int[] nums, List<List<Integer>> res, List<Integer> current, int target, int k){
        if(target==0 && current.size()==k){
            res.add(new ArrayList<>(current));
            return;
        }

        if(target<0 || index==nums.length || current.size()>k)
            return;

        current.add(nums[index]);
        LessOptimalFindFeasibleCombination(index+1, nums, res, current ,target-nums[index], k);
        current.remove(current.size()-1);
        LessOptimalFindFeasibleCombination(index+1, nums, res, current ,target,k);


    }


    public static void main(String[] args) {
        int k = 3;
        int n = 7;
        List<List<Integer>> result = combinationSum3(k, n);
        System.out.println("Combinations are: " + result);
        for (List<Integer> combination : result) {
            for (int num : combination) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
    }
}
