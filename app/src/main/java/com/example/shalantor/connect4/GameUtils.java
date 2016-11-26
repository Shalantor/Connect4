package com.example.shalantor.connect4;

/*This class provides static methods for any utilities the game play activity needs*/

public class GameUtils {

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

}
