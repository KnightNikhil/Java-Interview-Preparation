package DSA.Recursion;

import java.util.ArrayList;
import java.util.List;

public class NQueen {

    public static void main(String[] args) {
        NQueen nq = new NQueen();
        int n = 4;

        List<List<String>> result = nq.solveNQueens(n);
        System.out.println("Total solutions for " + n + "-Queens: " + result.size());
        for (List<String> solution : result) {
            for (String row : solution) {
                System.out.println(row);
            }
            System.out.println();
        }
    }


    public List<List<String>> solveNQueens(int n) {
        //your code goes here

        List<List<String>> res = new ArrayList();
        List<String> board = new ArrayList();

        char[] charArr = new char[n];
        for(int i=0;i<n;i++)
            charArr[i]='.';
        for(int i=0;i<n;i++)
            board.add(new String(charArr));

        validNQueens(0, board, res);
        return res;
    }


    /*
    idea is, we will start from the first row and for every column in that row, we will check if there is a possibility to place a 'Q' at that location
    if yes, we will place a 'Q' at that location and make a recursive call to the next row
    if no, we will move to the next column and check again
    if we reach the end of the board, we will add the current board to the result
    backtrack and remove the 'Q' from that location and move to the next column
     */
    private void validNQueens(int row, List<String> board, List<List<String>> res){

        if(row==board.size()){
            res.add(new ArrayList(board));
            return ;
        }

        // we will iterate over all the columns one by one
        for(int column =0; column<board.size(); column++){

            //check if there is a possibility to place a 'Q' at that location
            if(isPlacementPossible(board, row, column)){

                // since possible, place a 'Q' at that location
                char[] charArr = board.get(row).toCharArray();
                charArr[column] = 'Q';
                board.set(row, new String(charArr));

                // recursive call to the next row as there exist a 'Q' at that location
                validNQueens(row+1, board, res);

                // backtracking if there does not exist a 'Q' at that location
                charArr[column] = '.';
                board.set(row, new String(charArr));
            }
        }
    }

    private boolean isPlacementPossible(List<String> board, int row, int column){
        int r = row, c= column;

        // checks if there is a 'Q' to the top (check vertically upwards)
        while(r>=0){
            if(board.get(r).charAt(c)=='Q')
                return false;
            r--;
        }

        r = row;
        c = column;

        // checks if there is a 'Q' to the right diagonal upwards
        while(r>=0 && c<board.size()){
            if(board.get(r).charAt(c)=='Q')
                return false;
            r--;
            c++;
        }

        r = row;
        c= column;

        // checks if there is a 'Q' to the left diagonal upwards
        while(r>=0 && c>=0){
            if(board.get(r).charAt(c)=='Q')
                return false;
            r--;
            c--;
        }

        return true;
    }



    // row, column,



}
