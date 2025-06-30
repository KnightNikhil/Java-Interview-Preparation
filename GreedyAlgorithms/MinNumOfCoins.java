package GreedyAlgorithms;

import java.util.Arrays;
import java.util.Collections;

public class MinNumOfCoins {

    public static void main(String[] args) {
        int value = 52521;
        System.out.println("Denominatiosn to be used are: "+ findMinNumOfCoins(value));
    }

    private static int findMinNumOfCoins(int value) {
        int res = 0;
        Integer[] denominations = new Integer[]{1, 2, 5, 10, 20, 50, 100, 500, 1000};
        Arrays.sort(denominations, Collections.reverseOrder());
        System.out.println(Arrays.toString(denominations));
        int pointer =0;
        while(pointer< denominations.length && value>0){
            res+=value/denominations[pointer];
            value%=denominations[pointer];
            pointer++;
        }
        return res;
    }

    /*
    CORRECT:
    Using the greedy approach,
    min coins when max value being decreased earliest, big denominations used earlier, reduces the value most
    then iterate over the denominations one by one
     */


}
