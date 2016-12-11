package com.example.shalantor.connect4;

/*This class provides static methods for any utilities the game play activity needs*/

import java.net.Socket;

public class GameUtils {

    /*Variable to pass the socket from the account activity to the game activity*/
    private static Socket socket;
    public static int MAX_TURN_TIME = 60;

    /*Getter and setter for socket*/
    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        GameUtils.socket = socket;
    }


    /*Method to evaluate value of current grid state for the computer or the player
* According to the minimax algorithm for a connect 4 game, the value of fields
* is described below, where X stands for a chip and a 0 stands for an empty position:
* A neutral chip position is given a value of 0 points.
* 2 chips in the same row have a value of 10. (00XX) or (0X0X) or (X00X) or (X0X0) or (XX00)
* If those two chips don't have adjacent chips, they have a value of 20. (0XX0)
* 3 in the same row have a value of 1000 points. (XX0X) or (X0XX) or (XXX0) or (0XXX)
* If those three chips have no adjacent chips, they have a value of 2000 (0XXX0)
*
* The same holds for chips in the same column or diagonal, but there are some
* modifiers for their values.Those modifiers are:
*
* Vertical = 1
* Diagonal = 2
* Horizontal = 3*/

    public static int getGridValue(int[][] grid,int color){

        int vertical = 1;
        int diagonal = 2;
        int horizontal = 3;

        int twoSame = 10;
        int threeSame = 1000;

        int value = 0;

        /*horizontal 2 in the same row */
        for(int row =0;row < 6;row++){
            for(int col = 0;col < 4; col++){
                //XX00
                if(grid[row][col] == color && grid[row][col+1] == color
                        && grid[row][col+2] == 0 && grid[row][col+3] == 0){
                    value += twoSame * horizontal;
                }

                //X0X0
                else if (grid[row][col] == color && grid[row][col+1] == 0
                        && grid[row][col+2] == color && grid[row][col+3] == 0){
                    value += twoSame * horizontal;
                }

                //X00X
                else if (grid[row][col] == color && grid[row][col+1] == 0
                        && grid[row][col+2] == 0 && grid[row][col+3] == color){
                    value += twoSame * horizontal;
                }

                //0XX0
                else if (grid[row][col] == 0 && grid[row][col+1] == color
                        && grid[row][col+2] == color && grid[row][col+3] == 0){
                    value += 2*twoSame * horizontal;
                }

                //0X0X
                else if (grid[row][col] == 0 && grid[row][col+1] == color
                        && grid[row][col+2] == 0 && grid[row][col+3] == color){
                    value += twoSame * horizontal;
                }

                //00XX
                else if (grid[row][col] == 0 && grid[row][col+1] == 0
                        && grid[row][col+2] == color && grid[row][col+3] == color){
                    value += twoSame * horizontal;
                }

            }
        }

        /*Now check for vertical 2 in same column, with a space above like this:
        * 0
        * X
        * X   */

        for(int row =0; row <= 3; row ++){
            for(int col = 0; col < 7; col ++){
                if(grid[row][col] == 0 && grid[row+1][col] == color && grid[row+2][col] == color)
                    value += twoSame * vertical;
            }
        }

        /*Now check for 2 in the same diagonal from left down to right up (/)
         *Possible arrangements are :
         *    0    X    X    X    0    0
         *   0    0    X    0    X    X
         *  X    0    0    X    0    X
         * X    X    0    0    X    0
         * We start from bottom to top this time.
         */

        for(int row = 5; row >= 3; row--){
            for(int col =0; col <= 3; col++){

                if(grid[row][col] == color && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == 0 && grid[row-3][col+3] == 0){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row-1][col+1] == 0
                        && grid[row-2][col+2] == 0 && grid[row-3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row-1][col+1] == 0
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == 0 && grid[row-3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row-1][col+1] == 0
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == 0){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == 0){
                    value += 2 * twoSame * diagonal;
                }

            }
        }

        /*Now check for 2 in the same diagonal that are arranged like this (\)
        * Possible arrangements are:
        * X    X    0    0    X    0
        *  X    0    0    X    0    X
        *   0    0    X    0    X    X
        *    0    X    X    X    0    0*/

        for(int row = 0; row <= 2 ; row ++){
            for(int col = 0; col <= 3; col ++){

                if(grid[row][col] == color && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == 0 && grid[row+3][col+3] == 0){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row+1][col+1] == 0
                        && grid[row+2][col+2] == 0 && grid[row+3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row+1][col+1] == 0
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == 0 && grid[row+3][col+3] == color){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row+1][col+1] == 0
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == 0){
                    value += twoSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == 0){
                    value += 2 * twoSame * diagonal;
                }


            }
        }

        /*Now check for 3 chips in the same row (horizontal)*/
        for (int row=0; row < 6; row++){
            for(int col=0; col < 4; col++){

                /*(XX0X)*/
                if(grid[row][col] == color && grid[row][col+1] == color
                        && grid[row][col+2] == 0 && grid[row][col+3] == color)
                    value += threeSame * horizontal;

                /*(X0XX)*/
                if(grid[row][col] == color && grid[row][col+1] == 0
                        && grid[row][col+2] == color && grid[row][col+3] == color)
                    value += threeSame * horizontal;

                /*(0XXX)*/
                if(grid[row][col] == 0 && grid[row][col+1] == color
                        && grid[row][col+2] == color && grid[row][col+3] == color)
                    value += threeSame * horizontal;

                /*(XXX0)*/
                if(grid[row][col] == color && grid[row][col+1] == color
                        && grid[row][col+2] == color && grid[row][col+3] == 0)
                    value += threeSame * horizontal;
            }

        }

        /*Check for three chips in the same column with an empty space above it
        like this:
        0
        X
        X
        X
         */
        for (int row = 0; row <= 2 ; row ++){
            for(int col = 0; col <= 6; col++){

                if(grid[row][col] == 0 && grid[row+1][col] == color
                        && grid[row+2][col] == color && grid[row+3][col] == color)
                    value += threeSame * vertical;
            }
        }

        /*Now check for three chips in the same diagonal (/)
        * Possible arrangements are:
        *   0     X      X     X
    	   X     0      X     X
    	  X     X      0     X
    	 X     X      X     0*/

        for (int row=5;row >= 3; row--){
            for(int col=0;col <= 3; col ++ ){

                if(grid[row][col] == color && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == 0){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == 0 && grid[row-3][col+3] == color){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row-1][col+1] == 0
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == color){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == 0 && grid[row-1][col+1] == color
                        && grid[row-2][col+2] == color && grid[row-3][col+3] == color){
                    value += threeSame * diagonal;
                }

            }
        }

        /*now check again for 3 chips in the same diagonal (\)
        * possible arrangements are:
        *0     X     X     X
    	  X     0     X     X
    	   X     X     0     X
    	    X     X     X     0*/

        for(int row =0;row <=2;row++){
            for(int col =0; col <=3; col++){

                if(grid[row][col] == 0 && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == color){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row+1][col+1] == 0
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == color){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == 0 && grid[row+3][col+3] == color){
                    value += threeSame * diagonal;
                }

                else if(grid[row][col] == color && grid[row+1][col+1] == color
                        && grid[row+2][col+2] == color && grid[row+3][col+3] == 0){
                    value += threeSame * diagonal;
                }
            }
        }

        /*Now we have to check for 3 in the same row or diagonal which are open ended*/

        /*Same row(0XXX0)*/
        for(int row=0;row <= 5; row++){
            for(int col =0; col <= 2; col++){

                if(grid[row][col] == 0 && grid[row][col+1] == color && grid[row][col+2] == color
                        && grid[row][col+3] == color && grid[row][col+4] == 0)
                    value += 2 * threeSame * horizontal;
            }
        }

        /*Diagonal (\)*/
        for(int row =0;row <= 1; row ++){
            for(int col =0; col<=2; col++){

                if(grid[row][col] == 0 && grid[row+1][col+1] == color && grid[row+2][col+2] == color
                        && grid[row+3][col+3] == color && grid[row+4][col+4] == 0)
                    value += 2 * threeSame * diagonal;
            }
        }

        /*Other Diagonal (/)*/
        for(int row =5; row >= 4; row --){
            for(int col =0; col <= 2; col++){

                if(grid[row][col] == 0 && grid[row-1][col+1] == color && grid[row-2][col+2] == color
                        && grid[row-3][col+3] == color && grid[row-4][col+4] == 0)
                    value += 2 * threeSame * diagonal;

            }
        }

        return value;
    }

    /*Check if grid is full without a player having won*/
    public static boolean isGridFull(int[][] gameGrid){
        for(int i =0; i < 6;i++){
            for(int j =0;j<7;j++){
                if(gameGrid[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    /*Check if chip can be inserted in specified field*/
    public static boolean hasSpace(int columnNumber,int[]chips){
        return chips[columnNumber] < 6;
    }

    /*Check if player has won*/
    public static boolean hasWon(int column,int color,int[] howManyChips, int[][]gameGrid){
        int row = 6 - howManyChips[column];

        /*Check same line*/
        int sameColor = 0;
        for(int i =0; i < 7 ; i++){
            if(gameGrid[row][i] == color){
                sameColor += 1;
                if (sameColor == 4)
                    return true;
            }
            else
                sameColor = 0;
        }

        /*check same column*/
        sameColor = 0;
        for(int i =0;i < 6; i++){
            if(gameGrid[i][column] == color){
                sameColor += 1;
                if(sameColor == 4)
                    return true;
            }
            else
                sameColor = 0;
        }

        /*check same diagonal*/
        sameColor = 0;
        int rowStart = row;
        int columnStart = column;

        /*find the start of diagonal which goes from left up to right down*/
        while(rowStart >= 0 && columnStart >= 0){
            rowStart -= 1;
            columnStart -= 1;
        }

        /*Fix negative values*/
        rowStart += 1;
        columnStart += 1;

        /*Now check the color*/
        while(rowStart <= 5 && columnStart <= 6){
            if(gameGrid[rowStart][columnStart] == color){
                sameColor += 1;
                if(sameColor == 4)
                    return true;
            }
            else
                sameColor = 0;
            rowStart += 1;
            columnStart += 1;
        }


        /*Now check the diagonal going from left down to right up*/
        sameColor = 0;
        rowStart = row;
        columnStart = column;

        /*Again find the start*/
        while(rowStart <= 5 && columnStart >= 0){
            rowStart += 1;
            columnStart -= 1;
        }
        /*Fix values*/
        columnStart += 1;
        rowStart -= 1;
        /*Now check the color*/
        while(rowStart >= 0 && columnStart <= 6){
            if(gameGrid[rowStart][columnStart] == color){
                sameColor += 1;
                if(sameColor == 4)
                    return true;
            }
            else
                sameColor = 0;
            rowStart -= 1;
            columnStart += 1;
        }

        return false;
    }

    /*minimax returns the best move the computer should choose*/
    public static int minimax(int[][] grid,int[]chips,int startValue,int depth,int color,int column,
                       int enemyChipColorInt, int playerChipColorInt, int maxDepth){

        int[][] newGrid = new int[6][7];
        int[] newChips = new int[7];
        int bestMove = 0;
        int bestValue = startValue;

        /*first copy both grids*/

        /*copyGrid*/
        for(int i = 0; i < 6; i++){
            for(int j =0; j < 7; j++){
                newGrid[i][j] = grid[i][j];
            }
        }

        /*Copy counter array*/
        for(int j =0; j < 7; j++){
            newChips[j] = chips[j];
        }

        /*Now if called recursively check if computer can win or if player can win and
        * store the value of those situations from the view of the computer*/
        if(column != -1){
            if(GameUtils.hasWon(column,enemyChipColorInt,newChips,newGrid)){
                bestValue = 1000000;
            }
            else if(GameUtils.hasWon(column,playerChipColorInt,newChips,newGrid)){
                bestValue = -1000000;
            }
        }

        /*Now check if game is a tie, second condition is for case where someone won*/
        if(GameUtils.isGridFull(newGrid) && Math.abs(bestValue) < 1000000) {
            bestValue = 0;
        }
        else if(depth == maxDepth){
            int evaluation = GameUtils.getGridValue(newGrid,enemyChipColorInt);
            if(evaluation != 0){
                bestValue = evaluation;
            }
            else{
                bestValue = 0;
            }
        }
        else if(depth < maxDepth){
            /*now generate moves for each column and test their values*/
            for(int i =0; i < 7; i++){

                /*Add chip to grid if there is space*/
                if(GameUtils.hasSpace(i,newChips)) {
                    newGrid[5 - newChips[i]][i] = color;
                    newChips[i] += 1;

                    int[][] nextGrid = new int[6][7];
                    int[] nextChips = new int[7];

                    /*copyGrid*/
                    for(int k = 0; k < 6; k++){
                        for(int j =0; j < 7; j++){
                            nextGrid[k][j] = newGrid[k][j];
                        }
                    }

                    /*Copy counter array*/
                    for(int j =0; j < 7; j++){
                        nextChips[j] = newChips[j];
                    }

                    /*Set the right color*/
                    int nextColor;
                    if(color == enemyChipColorInt){
                        nextColor = playerChipColorInt;
                    }
                    else{
                        nextColor = enemyChipColorInt;
                    }
                    int nextValue = minimax(nextGrid,nextChips,-1000000,depth+1,nextColor,i,enemyChipColorInt,playerChipColorInt,maxDepth);

                    if(nextValue >= bestValue){
                        bestValue = nextValue;
                        bestMove = i;
                    }
                    newChips[i] -= 1;
                    newGrid[5 - newChips[i]][i] = 0;
                }
            }
        }

        if(depth == 0){
            return bestMove;
        }
        else{
            return bestValue;
        }
    }

    /*Gets the next move from AI or other player*/
    public static int getMove(int[] howManyChips,int[][] gameGrid,int enemyChipColorInt,int playerChipColorInt,int maxDepth){

        int[][] checkGrid = new int[6][7];
        int[] checkGridChipCounter = new int[7];

        /*if it is computers first move choose column 3
         *If column 3 is already taken take column 2
         */
        int spaceCounter = 0;
        for(int i =0; i < 7; i++){
            if(howManyChips[i] > 0){
                spaceCounter += 1;
            }
        }

        if(spaceCounter <= 1){
            if(gameGrid[5][2] == 0)
                return 2;
            else
                return 1;
        }

        /*copyGrid*/
        for(int i = 0; i < 6; i++){
            for(int j =0; j < 7; j++){
                checkGrid[i][j] = gameGrid[i][j];
            }
        }

        /*Copy counter array*/
        for(int j =0; j < 7; j++){
            checkGridChipCounter[j] = howManyChips[j];
        }

        /*Now check if computer can win with a move*/
        for(int i = 0; i < 7; i++){
            if(checkGridChipCounter[i] < 6) {
                checkGrid[5 - checkGridChipCounter[i]][i] = enemyChipColorInt;
                checkGridChipCounter[i] += 1;
                if(GameUtils.hasWon(i,enemyChipColorInt,checkGridChipCounter,checkGrid)){
                    return i;
                }
                checkGridChipCounter[i] -= 1;
                checkGrid[5 - checkGridChipCounter[i]][i] = 0;
            }
        }

        /*Now check if player can win , so that computer will stop him*/
        for(int i = 0; i < 7; i++){
            if(checkGridChipCounter[i] < 6) {
                checkGrid[5 - checkGridChipCounter[i]][i] = playerChipColorInt;
                checkGridChipCounter[i] += 1;
                if(GameUtils.hasWon(i,playerChipColorInt,checkGridChipCounter,checkGrid)){
                    return i;
                }
                checkGridChipCounter[i] -= 1;
                checkGrid[5 - checkGridChipCounter[i]][i] = 0;
            }
        }

        /*If none of the above holds, compute move from minimax algorithm*/

        return GameUtils.minimax(checkGrid,checkGridChipCounter,-1000000,0,enemyChipColorInt,-1,enemyChipColorInt,playerChipColorInt,maxDepth);

    }

    public static String[] splitInfo(String info){

        return info.split(" ");
    }

}
