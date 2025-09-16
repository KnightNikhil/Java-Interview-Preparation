package DSA.BinarySearch;

import java.util.ArrayList;

public class MinInRotatedSortedArray {

    public static void main(String[] args) {
        MinInRotatedSortedArray obj = new MinInRotatedSortedArray();
        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(3);
        arr.add(4);
        arr.add(5);
        arr.add(1);
        arr.add(2);
        System.out.println(obj.findMin(arr));
    }

    public int findMin(ArrayList<Integer> arr) {
        int n = arr.size();
        int left = 0, right = n - 1;
        int min = arr.get(left);
        while (left < right) {
            int mid = (left + right) / 2;
            if (arr.get(mid) > arr.get(mid + 1)) return arr.get(mid + 1);
            if (arr.get(left) < arr.get(mid)) { // left sorted
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return min;
    }
}
