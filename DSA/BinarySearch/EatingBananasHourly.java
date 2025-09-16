package DSA.BinarySearch;

import java.util.Arrays;

public class EatingBananasHourly {

    public static void main(String[] args) {
        EatingBananasHourly obj = new EatingBananasHourly();
        int[] piles = {805306368,805306368,805306368};
        int h = 1000000000;
        System.out.println(obj.minimumRateToEatBananas(piles, h));
    }

    public int minimumRateToEatBananas(int[] nums, int h) {
        int max = Arrays.stream(nums).max().getAsInt();
        int left = 1, right = max;
        while(left<=right){
            int mid = left + (right-left)/2;
            long sum = sumForDivisor(nums, mid);
            if( sum <= h){
                right =mid-1;
            } else {
                left = mid+1;
            }
        }
        return left;
    }


    long sumForDivisor(int[] nums, int rate){
        return (long) Arrays.stream(nums)
                .mapToDouble(x -> Math.ceil((double) x / rate))
                .sum();
    }
}
