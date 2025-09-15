package DSA.BinarySearch;

import java.util.ArrayList;
import java.util.Objects;

public class SearchInRotatedArray {

    public static void main(String[] args) {
        SearchInRotatedArray sra = new SearchInRotatedArray();
        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(7); arr.add(8); arr.add(1); arr.add(2); arr.add(3);
        arr.add(3); arr.add(3); arr.add(4); arr.add(5); arr.add(6);
        int target = 10;
        boolean result = sra.searchInARotatedSortedArrayII(arr, target);
        System.out.println("Index of " + target + " is: " + result);
    }

    public boolean searchInARotatedSortedArrayII(ArrayList<Integer> nums, int target) {
        int n = nums.size();
        int left = 0, right = n - 1;

        while (left <= right) {
            int mid = (left + right) / 2;

            if (nums.get(mid) == target) return true;

            // Handle duplicates
            if (nums.get(left).equals(nums.get(mid)) && nums.get(mid).equals(nums.get(right))) {
                left++;
                right--;
            }
            else if (nums.get(left) <= nums.get(mid)) {
                // Left half is sorted
                if (target >= nums.get(left) && target < nums.get(mid)) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            else {
                // Right half is sorted
                if (target > nums.get(mid) && target <= nums.get(right)) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        return false;
    }

    private int search(ArrayList<Integer> nums, int target) {

        int n= nums.size();
        int left=0, right=n-1;


        while(left<=right){
            int mid = (left+right)/2;
            if(nums.get(mid)==target)   return mid;

            // for cases of duplicates
           if(Objects.equals(nums.get(left), nums.get(mid)) && nums.get(mid)==nums.get(right)) {
               left++;
               right--;
               continue;
           }

           /*
           ISSUE WITH THE CODE
           1. This condition says: “If left side is sorted AND target < mid AND target > left.”
	        - But what if target == nums.get(left)? You’ll miss it, since you’re using strict < and >.
	        - we should use <= and >= instead of < and > to include the boundary values, for checking left and right nums[left] and nums[right]
	       2. multiple if blocks instead of else if.
	        - So in some cases, more than one condition may fire, moving both left and right incorrectly in the same iteration.
            */

            // now we have removed the duplicates, we can apply the normal logic
            if(nums.get(left)<=nums.get(mid) && target<nums.get(mid) && target>nums.get(left)) {  // left side is sorted & target exist between left and mid
                right = mid-1;
            }
            if(nums.get(mid)<=nums.get(right) && target>nums.get(mid) && target<nums.get(right)){ // right side is sorted & target exist between mid and right
                left = mid+1;
            }
            if(nums.get(left)>nums.get(mid)){ // left side is unsorted also means that right side is sorted so will search in left side as target is not in right side
                right= mid-1;
            }
            if(nums.get(mid)>nums.get(right)) { // right side is unsorted also means that left side is sorted so will search in right side as target is not in left side
                left = mid + 1;
            }


        }
        return -1;


    }


}
