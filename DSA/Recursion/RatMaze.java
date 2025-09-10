package DSA.Recursion;

import java.util.ArrayList;
import java.util.List;

public class RatMaze {

    public static void main(String[] args) {
        RatMaze rm = new RatMaze();
        int[][] grid = {
                {1,1,1},{1,1,1},{0,1,1}
        };
        List<String> result = rm.findPath(grid);
        System.out.println("Total paths: " + result.size());
        for (String path : result) {
            System.out.println(path);
        }
    }

    public List<String> findPath(int[][] grid) {
        //your code goes here
        List<String> res = new ArrayList();
        int n= grid.length;
        if(grid[n-1][n-1]==0 || grid[0][0]==0)   return res;
        StringBuffer path = new StringBuffer();
        findTotalPaths(0,0, grid, path, res, n);
        return res;
    }

    public void findTotalPaths(int  row, int column, int[][] grid, StringBuffer path, List<String> res, int n){

        // base case- if we have reached the destination cell, we will add the path to the result list
        if(row==n-1 && column==n-1){
            res.add(path.toString());
            return;
        }

        // if the cell is blocked, we will return
        if(grid[row][column]==0)  return;

        // if the cell is not already bloacked or visited, we will mark it as visited by making it 0, so it will be blocked for next recursive calls
        // we also need to make it 1 again after all the recursive calls are done, so that it can be used in other paths, as done in last
        grid[row][column]=0;

        if(row<n-1){
            findTotalPaths(row+1, column, grid, path.append('D'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(column<n-1){
            findTotalPaths(row, column+1, grid, path.append('R'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(row>0){
            findTotalPaths(row-1, column, grid, path.append('U'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(column>0){
            findTotalPaths(row, column-1, grid, path.append('L'), res, n);
            path.deleteCharAt(path.length()-1);
        }

        // backtrack and unmark the cell as visited, so that it can be used in other paths
        grid[row][column] = 1;

    }

    // PROBLEM WITH THIS SOLUTION WAS IT DOES NOT TAKE CARE OF LEFT AND UPWARDS DIRECTION PROPERLY, AND WHEN TRIED DOING SO, IT DOES NOT INCLUDE ALL THE POSSIBLE PATHS, LIKE ZIG-ZAG PATHS
    /*
    public void findTotalPaths(int  row, int column, int[][] grid, StringBuffer path, List<String> res, int n){
        if(row==n-1 && column==n-1){
            path.deleteCharAt(0);
            res.add(path.toString());
            return;
        }

        if(!isDownPossible(path, row, column, grid, n) && !isRightPossible(path, row, column,grid, n) && !isUpwardsPossible(path, row, column, grid, n) && !isLeftPossible(path, row, column, grid, n))
            return;


        if(isDownPossible(path, row, column, grid, n)){
            findTotalPaths(row+1, column, grid, path.append('D'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(isRightPossible(path, row, column, grid, n)){
            findTotalPaths(row, column+1, grid, path.append('R'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(isUpwardsPossible(path, row, column, grid, n)){
            findTotalPaths(row-1, column, grid, path.append('U'), res, n);
            path.deleteCharAt(path.length()-1);
        }
        if(isLeftPossible(path, row, column, grid, n)){
            findTotalPaths(row, column-1, grid, path.append('L'), res, n);
            path.deleteCharAt(path.length()-1);
        }

    private boolean isRightPossible(int row, int column, int[][] grid, int n) {
        return column<n-1 && grid[row][column + 1] == 1;
    }

    private boolean isDownPossible(int row, int column,int[][] grid, int n) {
        return row<n-1 && grid[row + 1][column] == 1;
    }

    }
    */


}
