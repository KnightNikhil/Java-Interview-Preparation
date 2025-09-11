package DSA.Recursion;

public class SudokuSolver {

    public static void main(String[] args) {
        SudokuSolver ss = new SudokuSolver();
        char[][] board = {
                {'5','3','.','.','7','.','.','.','.'},
                {'6','.','.','1','9','5','.','.','.'},
                {'.','9','8','.','.','.','.','6','.'},
                {'8','.','.','.','6','.','.','.','3'},
                {'4','.','.','8','.','3','.','.','1'},
                {'7','.','.','.','2','.','.','.','6'},
                {'.','6','.','.','.','.','2','8','.'},
                {'.','.','.','4','1','9','.','.','5'},
                {'.','.','.','.','8','.','.','7','9'}
        };
        ss.solve(board);
        for(int i=0;i<board.length;i++){
            for(int j=0;j<board[0].length;j++){
                System.out.print(board[i][j]+" ");
            }
            System.out.println();
        }

    }

    private boolean solve(char[][] board){
        int n= board.length;

        //iterate over all the cells of the board
        for(int row=0;row<n;row++){
            for(int column=0;column<n;column++){
                if(board[row][column]!='.')
                    continue;
                // we have found an empty cell, we will try to fill it with numbers from 1 to 9
                for(char num='1';num<='9';num++){
                    if(isNumberPossible(board, row, column, num)){
                        board[row][column]=num;
                        if(solve(board))
                            return true;
                        else // backtrack
                            board[row][column]='.';
                    }
                }
                // if no number is possible, we will return false
                return false;
            }
        }
        // if we have filled all the cells, we will return true
        return true;
    }



    private static boolean isNumberPossible(char[][] board, int row, int column, char num){
        int n= board.length;

        // check in the row
        for(int i=0;i<n;i++){
            if(board[row][i]==num)
                return false;
        }

        // check in the column
        for(int i=0;i<n;i++){
            if(board[i][column]==num)
                return false;
        }

        // check in the 3x3 grid
        int rowStart = 3* (row/3);
        int colStart = 3* (column/3);
        for(int i=rowStart;i<rowStart+3;i++){
            for(int j=colStart;j<colStart+3;j++){
                if(board[i][j]==num)
                    return false;
            }
        }

        // if the number is not present in the row, column and 3x3 grid, we can place it
        return true;

    }
}
