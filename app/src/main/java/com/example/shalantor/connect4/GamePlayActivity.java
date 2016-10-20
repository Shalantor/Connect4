package com.example.shalantor.connect4;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GamePlayActivity extends SurfaceView implements Runnable{

    private Thread thread = null;
    private SurfaceHolder holder;
    volatile boolean playingConnect4;
    /*TODO: remove hard coded values from boolean variables*/
    volatile boolean isSinglePlayer;
    volatile boolean isMultiPlayer ;
    volatile boolean isMuted;
    volatile boolean isExitMenuVisible ;
    volatile boolean isPlayersTurn ;
    volatile boolean isColorChoiceVisible;
    volatile boolean isChipFalling;
    volatile boolean isGameOver;

    private Paint paint;
    private Canvas canvas;

    /*fps*/
    private long lastFrameTime;

    /*Screen dimensions info*/
    private int screenHeight;
    private int screenWidth;
    private int cellWidth;
    private int cellheight;

    /*arrays to store game info*/
    private int[][] gameGrid;
    private int[] howManyChips;

    /*image variables*/
    private Bitmap redChip;
    private Bitmap yellowChip;
    private Bitmap emptyCell;
    private Bitmap yellowCell;
    private Bitmap redCell;
    private Bitmap soundOn;
    private Bitmap soundOff;
    private Bitmap backButton;
    private Bitmap playerChipColor;
    private Bitmap enemyChipColor;

    /*Width of texts*/
    private float noTextWidth;
    private float yesTextWidth;

    /*Associated activity*/
    private Activity associatedActivity;

    /*Variable to store which column was chose by the player, where -1 means that no column is chosen right now*/
    private int activeColumnNumber;

    /*where will next chip be inserted*/
    private int finalChipHeight;
    private Rect fallingChip;
    private int fallingChipPosition;
    private int playerChipColorInt;
    private int enemyChipColorInt;
    private int fallingChipColor;

    /*End screen Message for player after match*/
    private String endScreenMessage;

    public GamePlayActivity(Context context){
        super(context);
        holder = getHolder();
        paint = new Paint();
        associatedActivity = (Activity) context;
        Intent intent = associatedActivity.getIntent();
        if(intent.getIntExtra("MODE",-1) == 0){
            isSinglePlayer = true;
            isMultiPlayer = false;
        }
        else{
            isMultiPlayer = true;
            isSinglePlayer = false;
        }

        fallingChip = null;

        /*Boolean variables*/
        isExitMenuVisible = false;
        isMuted = false;
        activeColumnNumber = -1;
        isColorChoiceVisible = true;
        isChipFalling = false;
        isGameOver = false;
        /*Will change after choosing menu, is just set true to stop AI from making a move*/
        isPlayersTurn = true;

        /*Get screen dimensions*/
        Display display = associatedActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;

        /*Set dimensions of field*/
        cellWidth = screenWidth/10;
        cellheight = screenHeight/6;

        /*Load images*/
        redChip = BitmapFactory.decodeResource(getResources(),R.mipmap.redchip);
        yellowChip = BitmapFactory.decodeResource(getResources(),R.mipmap.yellowchip);
        emptyCell = BitmapFactory.decodeResource(getResources(),R.mipmap.emptycell);
        redCell = BitmapFactory.decodeResource(getResources(),R.mipmap.redcell);
        yellowCell = BitmapFactory.decodeResource(getResources(),R.mipmap.yellowcell);
        soundOff = BitmapFactory.decodeResource(getResources(),R.mipmap.sound_off);
        soundOn = BitmapFactory.decodeResource(getResources(),R.mipmap.sound_on);
        backButton = BitmapFactory.decodeResource(getResources(),R.mipmap.back_button);

        /*Create array for field, 0 means empty , 1 means red chip, 2 means yellow chip*/
        gameGrid = new int[6][7];
        howManyChips = new int[7];


    }

    @Override
    public void run(){
        while(playingConnect4){
            if(!isPlayersTurn && !isChipFalling && !isGameOver){   /*wait for chip to fall and then get move of AI*/
                int move = getMove();
                makeMove(move);
            }
            updateGUIObjects();
            drawScreen();
            controlFPS();
        }
    }


    /*Method to update GUI components that will animate the menu*/
    public void updateGUIObjects(){
        /*We have a chip*/
        if(fallingChip != null){
            /*Move down chip a bit*/
            fallingChip.top += cellheight/8;
            fallingChip.bottom += cellheight/8;
            if(fallingChip.bottom >= finalChipHeight){
                fallingChip = null;                         //remove
                if(!isPlayersTurn)
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = playerChipColorInt;
                else
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = enemyChipColorInt;
                howManyChips[fallingChipPosition] += 1;
                isChipFalling = false;
                if(hasWon(fallingChipPosition,fallingChipColor,howManyChips,gameGrid)){
                    if(!isPlayersTurn){                     /*the chip which is falling, falls after the turn change*/
                        endScreenMessage = "YOU WIN";
                    }
                    else{
                        endScreenMessage = "YOU LOSE";
                    }
                    isGameOver = true;
                }
                else if(isGridFull()){
                    endScreenMessage = "TIE";
                    isGameOver = true;
                }
            }
        }

    }


    /*Method to draw the game fields and the menu on screen*/
    public void drawScreen(){
        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);                  /*Background*/
            paint = new Paint();

            /*Check if chip is falling and draw it if it is*/
            if(isChipFalling){
                if(!isPlayersTurn)
                    canvas.drawBitmap(playerChipColor,null,fallingChip,paint);
                else
                    canvas.drawBitmap(enemyChipColor,null,fallingChip,paint);
            }

            /*Game grid*/
            Rect destRect;
            Bitmap imageToDraw;

            /*Draw each cell*/
            for( int i = 0; i < 6;i++){
                for(int j =0; j < 7;j++){
                    destRect = new Rect(j*cellWidth,i*cellheight,(j+1)*cellWidth,(i+1)*cellheight);
                    if(gameGrid[i][j] == 0){
                        imageToDraw = emptyCell;
                    }
                    else if(gameGrid[i][j] == 1){
                        imageToDraw = redCell;
                    }
                    else{
                        imageToDraw = yellowCell;
                    }
                    canvas.drawBitmap(imageToDraw,null,destRect,paint);
                }
            }

            /*Now draw rect for menu*/
            paint.setColor(Color.argb(255,153,255,255));
            canvas.drawRect(7*screenWidth/10,0,screenWidth,screenHeight,paint);

            /*Write some info*/
            paint.setTextSize(screenHeight/15);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.BLACK);
            canvas.drawText("OPPONENT:",8*screenWidth/10 + screenWidth/20,screenHeight/15,paint);

            if(isSinglePlayer){
                paint.setTextSize(screenHeight/10);
                canvas.drawText("AI",8*screenWidth/10 + screenWidth/20,screenHeight/4,paint);
                paint.setTextSize(screenHeight/15);
            }

            canvas.drawText("TIME:",8*screenWidth/10 + screenWidth/20,screenHeight/2,paint);

            if(isSinglePlayer){
                canvas.drawText("-",8*screenWidth/10 + screenWidth/20,screenHeight/2 + 2*screenHeight/15,paint);
            }

            /*Draw back button and and sound button*/
            int buttonDimension = screenWidth/10;

            /*Destination rect for sound button*/
            destRect = new Rect(7*screenWidth/10,screenHeight - buttonDimension,
                                7*screenWidth/10+ buttonDimension,screenHeight );

            /*Draw sound button based on volume*/
            if(isMuted){
                imageToDraw = soundOff;
            }
            else{
                imageToDraw = soundOn;
            }
            canvas.drawBitmap(imageToDraw,null,destRect,paint);

            /*Destination for back button*/
            destRect = new Rect(9*screenWidth/10,screenHeight - buttonDimension, screenWidth,screenHeight);
            canvas.drawBitmap(backButton,null,destRect,paint);

            /*Highlight the active column if there is one*/
            if(activeColumnNumber >= 0) {
                paint.setColor(Color.argb(128, 0, 255, 0));
                canvas.drawRect(screenWidth/10 * activeColumnNumber,0,
                        (activeColumnNumber + 1) * screenWidth/10,screenHeight,paint);
            }

            /*Draw the chip color choice panel if visible*/
            if(isColorChoiceVisible){
                /*Draw background*/
                paint.setColor(Color.argb(224,0,0,0));
                canvas.drawRect(0,0,7*screenWidth/10,screenHeight,paint);

                /*Draw text*/
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(screenHeight/8);
                paint.setColor(Color.WHITE);
                canvas.drawText("Choose a color:",screenWidth/3,screenHeight/4,paint);

                /*Draw chips*/

                Rect redChipDestRect = new Rect(screenWidth/10,screenHeight/3,3*screenWidth/10,2*screenHeight/3);
                Rect yellowChipDestRect = new Rect(4*screenWidth/10,screenHeight/3,6*screenWidth/10,2*screenHeight/3);

                canvas.drawBitmap(redChip,null,redChipDestRect,paint);
                canvas.drawBitmap(yellowChip,null,yellowChipDestRect,paint);
            }

            /*Draw winning message*/
            if(isGameOver){
                paint.setColor(Color.argb(200,0,0,0));
                canvas.drawRect(0,0,7*screenWidth/10,screenHeight,paint);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(screenHeight/8);
                paint.setColor(Color.WHITE);
                canvas.drawText(endScreenMessage,screenWidth/3,screenHeight/4,paint);
            }

            /*Draw the exit menu if it is visible*/
            if(isExitMenuVisible){
                canvas.drawColor(Color.argb(200,0,0,0));
                paint.setTextSize(screenHeight/4);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.WHITE);
                canvas.drawText("EXIT?",screenWidth/2,screenHeight/3,paint);
                paint.setTextSize(screenHeight/5);
                canvas.drawText("YES",screenWidth/3,2*screenHeight/3,paint);
                canvas.drawText("NO",2*screenWidth/3,2*screenHeight/3,paint);
                yesTextWidth = paint.measureText("YES");
                noTextWidth = paint.measureText("NO");
            }



            holder.unlockCanvasAndPost(canvas);
        }
    }


    /*Method to control the refresh rate of screen*/
    public void controlFPS(){

        long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
        long timeToSleep = 10 - timeThisFrame;

        if(timeToSleep > 0){
            try{
                thread.sleep(timeToSleep);
            }catch (InterruptedException ex){
                Log.d("THREAD_SLEEP","Interrupted exception occured");
            }
        }

        lastFrameTime = System.currentTimeMillis();

    }

    /*To pause animation*/
    public void pause(){
        playingConnect4 = false;
        boolean run = true;
        while(run) {
            try {
                thread.join();
                run = false;
            } catch (InterruptedException ex) {
                Log.d("THREAD_JOIN", "Interrupted exception occured");
            }
        }
    }

    /*To continue animation from a pause*/
    public void resume(){
        playingConnect4 = true;
        thread = new Thread(this);
        thread.start();
    }

    /*Validate touchevent*/
    public boolean validateTouchEvent(MotionEvent event){

        float initialY = event.getY();
        float initialX = event.getX();

        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            if(isExitMenuVisible){
                /*YES BUTTON*/
                if(initialX >= screenWidth/3 - yesTextWidth && initialX <= screenWidth/3 + yesTextWidth
                        && initialY >= 2*screenHeight/3 - screenHeight/5
                        && initialY <= 2*screenHeight/3){
                    pause();
                    Intent intent = new Intent(associatedActivity,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    associatedActivity.startActivity(intent);
                    associatedActivity.finish();
                }
                /*NO BUTTON*/
                if(initialX >= 2*screenWidth/3 - noTextWidth && initialX <= 2*screenWidth/3 + noTextWidth
                        && initialY >= 2*screenHeight/3 - screenHeight/5
                        && initialY <= 2*screenHeight/3){
                    isExitMenuVisible = false;
                }
                return true;
            }
            else if(isColorChoiceVisible){
                /*BACK BUTTON*/
                if(initialX >= 9*screenWidth/10 && initialX <= screenWidth
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    isExitMenuVisible = true;
                }
                /*SOUND BUTTON*/
                else if(initialX >= 7*screenWidth/10 && initialX <= 8*screenWidth/10
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight) {
                    /*TODO:add code to mute sound when we have a soundtrack*/
                    isMuted = !isMuted;
                }
                /*RED CHIP ICON*/
                else if(initialX >= screenWidth/10 && initialX <= 3*screenWidth/10
                        && initialY >= screenHeight/3 && initialY <= 2*screenHeight/3){
                    playerChipColorInt = 1;
                    playerChipColor = redChip;
                    enemyChipColorInt = 2;
                    enemyChipColor = yellowChip;
                    isColorChoiceVisible = false;
                }
                /*YELLOW CHIP ICON*/
                else if(initialX >= 4*screenWidth/10 && initialX <= 6*screenWidth/10
                        && initialY >= screenHeight/3 && initialY <= 2*screenHeight/3){
                    playerChipColorInt = 2;
                    playerChipColor = yellowChip;
                    enemyChipColor = redChip;
                    enemyChipColorInt = 1;
                    isColorChoiceVisible = false;
                }
                if(!isColorChoiceVisible)
                    isPlayersTurn = Math.random() > 0.5;
            }
            else{
                /*BACK BUTTON*/
                if(initialX >= 9*screenWidth/10 && initialX <= screenWidth
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    isExitMenuVisible = true;
                }
                /*SOUND BUTTON*/
                else if(initialX >= 7*screenWidth/10 && initialX <= 8*screenWidth/10
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    /*TODO:add code to mute sound when we have a soundtrack*/
                    isMuted = !isMuted;
                }
                else if(isPlayersTurn && !isChipFalling && !isGameOver){     /*Is it the players turn?*/
                    /*Check which column is active*/
                    if(initialX <= screenWidth/10){
                        if(activeColumnNumber == 0 && hasSpace(0)) {
                            makeMove(0);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(0)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 0;
                        }
                    }
                    else if(initialX <= 2*screenWidth/10){
                        if(activeColumnNumber ==1 && hasSpace(1)) {
                            makeMove(1);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(1)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 1;
                        }
                    }
                    else if(initialX <= 3*screenWidth/10){
                        if(activeColumnNumber ==2 && hasSpace(2)) {
                            makeMove(2);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(2)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 2;
                        }
                    }
                    else if(initialX <= 4*screenWidth/10){
                        if(activeColumnNumber ==3 && hasSpace(3)) {
                            makeMove(3);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(3)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 3;
                        }
                    }
                    else if(initialX <= 5*screenWidth/10){
                        if(activeColumnNumber ==4 && hasSpace(4)) {
                            makeMove(4);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(4)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 4;
                        }
                    }
                    else if(initialX <= 6*screenWidth/10){
                        if(activeColumnNumber ==5 && hasSpace(5)) {
                            makeMove(5);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(5)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 5;
                        }
                    }
                    else if(initialX <= 7*screenWidth/10){
                        if(activeColumnNumber ==6 && hasSpace(6)) {
                            makeMove(6);
                            activeColumnNumber = -1;
                        }
                        else if(!hasSpace(6)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 6;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /*Check if chip can be inserted in specified field*/
    private boolean hasSpace(int columnNumber){
        return howManyChips[columnNumber] < 6;
    }

    /*Insert chip in specified position*/
    public void makeMove(int columnNumber){
        isChipFalling = true;
        finalChipHeight = screenHeight - howManyChips[columnNumber]*cellheight;
        fallingChipPosition = columnNumber;

        /*Create new chip and add it to list*/
        fallingChip = new Rect(columnNumber*cellWidth,-cellheight,(columnNumber+1)*cellWidth,0);

        if(isPlayersTurn){
            fallingChipColor = playerChipColorInt;
        }
        else{
            fallingChipColor = enemyChipColorInt;
        }

        isPlayersTurn = !isPlayersTurn;

    }

    /*Check if player has won*/
    private boolean hasWon(int position,int color,int[] howManyChips, int[][]gameGrid){
        int row = 6 - howManyChips[position];
        int column = fallingChipPosition;

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

    /*Check if grid is full without a player having won*/
    private boolean isGridFull(){
        for(int i =0; i < 6;i++){
            for(int j =0;j<7;j++){
                if(gameGrid[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    /*TODO:complete method*/
    /*Gets the next move from AI or other player*/
    private int getMove(){

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
                if(hasWon(i,enemyChipColorInt,checkGridChipCounter,checkGrid)){
                    Log.d("WIN","COLUMN " + i);
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
                if(hasWon(i,playerChipColorInt,checkGridChipCounter,checkGrid)){
                    Log.d("WIN_STOP","COLUMN " + i);
                    return i;
                }
                checkGridChipCounter[i] -= 1;
                checkGrid[5 - checkGridChipCounter[i]][i] = 0;
            }
        }

        /*If none of the above holds, compute move from minimax algorithm*/


        return 0;

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
    private int getGridValue(int[][] grid,int color){

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
                if(grid[col][row] == color && grid[col+1][row] == color
                    && grid[col+2][row] == 0 && grid[col+3][row] == 0){
                    value += twoSame * horizontal;
                }

                //X0X0
                else if (grid[col][row] == color && grid[col+1][row] == 0
                        && grid[col+2][row] == color && grid[col+3][row] == 0){
                    value += twoSame * horizontal;
                }

                //X00X
                else if (grid[col][row] == color && grid[col+1][row] == 0
                        && grid[col+2][row] == 0 && grid[col+3][row] == color){
                    value += twoSame * horizontal;
                }

                //0XX0
                else if (grid[col][row] == 0 && grid[col+1][row] == color
                        && grid[col+2][row] == color && grid[col+3][row] == 0){
                    value += 2*twoSame * horizontal;
                }

                //0X0X
                else if (grid[col][row] == 0 && grid[col+1][row] == color
                        && grid[col+2][row] == 0 && grid[col+3][row] == color){
                    value += twoSame * horizontal;
                }

                //00XX
                else if (grid[col][row] == 0 && grid[col+1][row] == 0
                        && grid[col+2][row] == color && grid[col+3][row] == color){
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


        return value;
    }

}
