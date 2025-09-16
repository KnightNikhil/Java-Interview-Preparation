package DSA.BinarySearch;

import java.util.Arrays;

public class SmallestDivisorLessThanEqualThreshold {

    public static void main(String[] args) {
        SmallestDivisorLessThanEqualThreshold obj = new SmallestDivisorLessThanEqualThreshold();
        int[] nums = {5,10,15,20,25};
        int threshold = 15;
        System.out.println(obj.smallestDivisor(nums, threshold));
    }

    public int smallestDivisor(int[] nums, int threshold) {
        int max = Arrays.stream(nums).max().getAsInt();
        int left = 1, right = max;
        while(left<=right){
            int mid = left + (right-left)/2;
            int sum = sumWithDivisor(nums, mid);
            /* incorrect condition because there can be scenarios when two numbers can have same sum, and we need to choose the lowest number
            if( sum == threshold){
                return mid;
            } else */ if (sum<threshold) {
                right =mid-1;
                // here it is mid -1 and not mid bcoz ofc mid can be the answer, but if I do right = mid, then it will lead to infinite loop when left = mid
                // e.g left = 3, right =3, mid =3, then right = mid will lead to infinite loop
                // and if I do right = mid -1, there will be case when left will be greater than right and in the last case if the mid is not ans it will go the else and make the left +1,
                // loop will break and I will return left which is the answer
            } else {
                left = mid+1;
            }
        }
        return left;
    }

    private int sumWithDivisor(int[] nums, int divisor){
        int sum = 0;
        for(int num : nums){
            sum += Math.ceil((double)num/divisor);
        }
        return sum;
    }


}
