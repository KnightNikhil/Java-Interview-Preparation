package DSA.BinarySearch;

public class LowerBound {

    public static void main(String[] args) {
        LowerBound lb = new LowerBound();
        int[] nums = {3,5,8,15,19};
        int x = 9;
        int result = lb.lowerBound(nums, x);
        System.out.println("Lower bound index for " + x + " is: " + result);
    }

    public int lowerBound(int[] nums, int x) {

        int n=nums.length;
        int left=0, right=n-1;
        if(x>nums[right])   return right;
        if(x< nums[left])   return left;
        if(x==nums[0])  return 0;
        while(left<right-1){
            int mid= (left+right)/2;
            if(nums[mid]==x) return mid-1;
            if(nums[left]<=x && x<nums[mid]){
                right=mid;
            }
            else if(nums[mid]<x && x<=nums[right]){
                left=mid;
            }
        }
        return left;
    }

}
