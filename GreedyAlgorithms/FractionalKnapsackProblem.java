package GreedyAlgorithms;

import java.util.Arrays;

public class FractionalKnapsackProblem {
    public static void main(String[] args) {
        int[] values = {5, 10, 15};
        int[] weights = {2, 3, 5};
        int capacity = 5;

        double maxProfit = fractionalKnapsack(weights, values, capacity);
        System.out.println("Maximum profit from fractional knapsack: " + maxProfit);
    }

    private static double fractionalKnapsack(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        ItemValue[] itemValues = new ItemValue[n];
        for(int i=0;i<n;i++){
            itemValues[i] = new ItemValue(weights[i], values[i]);
        }
        Arrays.sort(itemValues, (a, b) -> Double.compare((double) b.value / b.weight, (double) a.value / a.weight));
        System.out.println(Arrays.toString(itemValues));
        int currentCapacity = 0;
        int index=0;
        double result=0;
        while(currentCapacity<capacity && index<n)
        {
            int capacityUsed = Math.min(itemValues[index].weight, capacity-currentCapacity);
            result+= (double) (capacityUsed * itemValues[index].value) /itemValues[index].weight;
            currentCapacity+=capacityUsed;
            index++;
        }
        return result;
    }


    private static double fractionalKnapsackINCORRECT(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        ItemValue[] itemValues = new ItemValue[n];
        for(int i=0;i<n;i++){
            itemValues[i] = new ItemValue(weights[i], values[i]);
        }
        Arrays.sort(itemValues, (a, b) -> b.value/ b.weight - a.value/a.weight );
        System.out.println(Arrays.toString(itemValues));
        int currentCapacity = 0;
        int index=0;
        int result=0;
        while(currentCapacity<capacity)
        {
            int capacityUsed = Math.min(itemValues[index].weight, capacity-currentCapacity);
            result+=capacityUsed*itemValues[index].value/itemValues[index].weight;
            currentCapacity+=capacityUsed;
            index++;
        }
        return result;

    }

    /*
    the above solution will fail in 3 edge cases,
    if value per object is floating number, might give incorrect values as integer
    if capacity is more than availability -> all items will be exhausted and index will go out of bound, no index check here
    result should be declared as double not integer, might fail in value/weight floating values.
     */

    static class ItemValue {
        int weight;
        int value;

        ItemValue(int weight, int value) {
            this.weight = weight;
            this.value = value;
        }

        @Override
        public String toString() {
            return "ItemValue{" +
                    "weight=" + weight +
                    ", value=" + value +
                    '}';
        }
    }
}
