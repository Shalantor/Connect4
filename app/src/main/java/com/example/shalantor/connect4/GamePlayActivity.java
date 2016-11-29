package com.example.shalantor.connect4;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class GamePlayActivity extends SurfaceView implements Runnable{

    private Thread thread = null;
    private SurfaceHolder holder;
    volatile boolean playingConnect4;
    volatile boolean isSinglePlayer;
    volatile boolean isMultiPlayer ;
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
    private int cellHeight;

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

    /*Maximum depth for minimax algorithm*/
    int maxDepth;

    /*Mediaplayer for sound*/
    private MediaPlayer player;
    volatile boolean isMuted;
    volatile boolean needVolumeChange;

    /*static variables*/
    private static final String RANK = "PLAYER_RANK";
    private static final String OFFLINE_WIN = "OFFLINE_WINS";
    private static final String ONLINE_WIN = "ONLINE_WINS";
    private static final String OFFLINE_LOS = "OFFLINE_LOSSES";
    private static final String ONLINE_LOS = "ONLINE_LOSSES";
    private static final String DIFFICULTY = "DIFFICULTY";
    private static final String MUTE = "MUTE";
    private static final String GAME_INFO = "GAME_INFO";

    /*Time variable to reset game*/
    private long gameEndTime;

    /*Async task for network operations if in multiplayer mode*/
    private GameAsyncTask gameNetTask;

    /*Name of opponent*/
    String[] opponentsName;

    public GamePlayActivity(Context context){
        super(context);
        holder = getHolder();
        paint = new Paint();
        associatedActivity = (Activity) context;
        Intent intent = associatedActivity.getIntent();
        if(intent.getIntExtra("MODE",-1) == 0){
            isSinglePlayer = true;
            isMultiPlayer = false;
            isPlayersTurn = true;
        }
        else{
            isMultiPlayer = true;
            isSinglePlayer = false;
            gameNetTask = new GameAsyncTask(GameUtils.getSocket(),associatedActivity,false);
            String[] gameInfo = GameUtils.splitInfo(intent.getStringExtra(GAME_INFO));
            isPlayersTurn = gameInfo[1].equals("1");
            opponentsName = new String[gameInfo.length - 2];
            System.arraycopy(gameInfo,2,opponentsName,0,gameInfo.length - 2);
        }

        /*Set difficulty*/
        int difficulty = intent.getIntExtra(DIFFICULTY,0);

        if(difficulty == 0){
            maxDepth = 1;
        }
        else if(difficulty == 1){
            maxDepth = 3;
        }
        else{
            maxDepth = 4;
        }

        /*set sound volume*/
        isMuted = intent.getBooleanExtra(MUTE,false);

        fallingChip = null;
        gameEndTime = -1;

        /*Boolean variables*/
        isExitMenuVisible = false;
        activeColumnNumber = -1;
        isColorChoiceVisible = true;
        isChipFalling = false;
        isGameOver = false;
        needVolumeChange = false;

        /*Get screen dimensions*/
        Display display = associatedActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;

        /*Set dimensions of field*/
        cellWidth = screenWidth/10;
        cellHeight = screenHeight/6;

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

    /*Mute and unmute player*/
    public void changeVolume(){
        if(isMuted){
            player.setVolume(0,0);
        }
        else{
            player.setVolume(1,1);
        }
        needVolumeChange = false;
    }

    @Override
    public void run(){

        /*Create player object*/
        player = MediaPlayer.create(associatedActivity,R.raw.menusong);
        player.setLooping(true);
        if(isMuted){
            player.setVolume(0,0);
        }
        else {
            player.setVolume(1, 1);
        }
        player.start();

        while(playingConnect4){
            if(needVolumeChange){
                changeVolume();
            }
            if(!isPlayersTurn && !isChipFalling && !isGameOver){   /*wait for chip to fall and then get move of opponent*/
                if (isSinglePlayer) {
                    int move = GameUtils.getMove(howManyChips, gameGrid, enemyChipColorInt, playerChipColorInt, maxDepth);
                    makeMove(move);
                }
            }
            updateGUIObjects();
            drawScreen();
            controlFPS();
            if(gameEndTime > 0) {
                if (System.currentTimeMillis() - gameEndTime > 3000) {
                    resetGame();
                }
            }
        }

        /*Stop and release player*/
        player.pause();
        player.release();
        player = null;
    }

    /*Method to reset game*/
    public void resetGame(){
        fallingChip = null;
        gameEndTime = -1;

        /*Boolean variables*/
        isExitMenuVisible = false;
        activeColumnNumber = -1;
        isColorChoiceVisible = true;
        isChipFalling = false;
        isGameOver = false;
        /*Will change after choosing menu, is just set true to stop AI from making a move*/
        isPlayersTurn = true;
        needVolumeChange = false;

        /*Create array for field, 0 means empty , 1 means red chip, 2 means yellow chip*/
        gameGrid = new int[6][7];
        howManyChips = new int[7];
    }


    /*Method to update GUI components that will render a falling chip*/
    public void updateGUIObjects(){
        /*We have a chip*/
        if(fallingChip != null){
            /*Move down chip a bit*/
            fallingChip.top += cellHeight/8;
            fallingChip.bottom += cellHeight/8;
            if(fallingChip.bottom >= finalChipHeight){
                fallingChip = null;                         //remove

                if(!isPlayersTurn)
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = playerChipColorInt;
                else
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = enemyChipColorInt;

                howManyChips[fallingChipPosition] += 1;
                isChipFalling = false;
                if(GameUtils.hasWon(fallingChipPosition,fallingChipColor,howManyChips,gameGrid)){

                    /*Now save to stats*/
                    SharedPreferences preferences = associatedActivity.getSharedPreferences(associatedActivity.getPackageName(),Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    if(!isPlayersTurn){                     /*the chip which is falling, falls after the turn change*/
                        endScreenMessage = "YOU WIN";
                        if(isSinglePlayer){
                            int wins = preferences.getInt(OFFLINE_WIN,0);
                            wins++;
                            editor.putInt(OFFLINE_WIN,wins);
                        }
                        else{
                            int wins = preferences.getInt(ONLINE_WIN,0);
                            wins++;
                            editor.putInt(ONLINE_WIN,wins);
                        }
                    }
                    else{
                        endScreenMessage = "YOU LOSE";
                        if(isSinglePlayer){
                            int losses = preferences.getInt(OFFLINE_LOS,0);
                            losses++;
                            editor.putInt(OFFLINE_LOS,losses);
                        }
                        else{
                            int losses = preferences.getInt(ONLINE_LOS,0);
                            losses++;
                            editor.putInt(ONLINE_LOS,losses);
                        }
                    }
                    editor.apply();
                    isGameOver = true;
                    gameEndTime = System.currentTimeMillis();
                }
                else if(GameUtils.isGridFull(gameGrid)){
                    endScreenMessage = "TIE";
                    isGameOver = true;
                    gameEndTime = System.currentTimeMillis();
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
                    destRect = new Rect(j*cellWidth,i*cellHeight,(j+1)*cellWidth,(i+1)*cellHeight);
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
            else{
                paint.setTextSize(screenHeight / 15);
                float offsetY = screenHeight / 4;
                for (String part : opponentsName){
                    canvas.drawText(part,8*screenWidth/10 + screenWidth/20,offsetY,paint);
                    offsetY += screenHeight / 15;
                }
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
                    intent.putExtra(MUTE,isMuted);
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
                    isMuted = !isMuted;
                    needVolumeChange = true;
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
                if (!isColorChoiceVisible){
                    isPlayersTurn = Math.random() > 0.5;
                }
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
                    needVolumeChange = true;
                    isMuted = !isMuted;
                }
                else if(isPlayersTurn && !isChipFalling && !isGameOver){     /*Is it the players turn?*/
                    /*Check which column is active*/
                    if(initialX <= screenWidth/10){
                        if(activeColumnNumber == 0 && GameUtils.hasSpace(0,howManyChips)) {
                            makeMove(0);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(0,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 0;
                        }
                    }
                    else if(initialX <= 2*screenWidth/10){
                        if(activeColumnNumber ==1 && GameUtils.hasSpace(1,howManyChips)) {
                            makeMove(1);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(1,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 1;
                        }
                    }
                    else if(initialX <= 3*screenWidth/10){
                        if(activeColumnNumber ==2 && GameUtils.hasSpace(2,howManyChips)) {
                            makeMove(2);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(2,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 2;
                        }
                    }
                    else if(initialX <= 4*screenWidth/10){
                        if(activeColumnNumber ==3 && GameUtils.hasSpace(3,howManyChips)) {
                            makeMove(3);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(3,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 3;
                        }
                    }
                    else if(initialX <= 5*screenWidth/10){
                        if(activeColumnNumber ==4 && GameUtils.hasSpace(4,howManyChips)) {
                            makeMove(4);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(4,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 4;
                        }
                    }
                    else if(initialX <= 6*screenWidth/10){
                        if(activeColumnNumber ==5 && GameUtils.hasSpace(5,howManyChips)) {
                            makeMove(5);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(5,howManyChips)){
                            activeColumnNumber = -1;
                        }
                        else {
                            activeColumnNumber = 5;
                        }
                    }
                    else if(initialX <= 7*screenWidth/10){
                        if(activeColumnNumber ==6 && GameUtils.hasSpace(6,howManyChips)) {
                            makeMove(6);
                            activeColumnNumber = -1;
                        }
                        else if(!GameUtils.hasSpace(6,howManyChips)){
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

    /*Insert chip in specified position*/
    public void makeMove(int columnNumber){
        isChipFalling = true;
        finalChipHeight = screenHeight - howManyChips[columnNumber]*cellHeight;
        fallingChipPosition = columnNumber;

        /*Create new chip and add it to list*/
        fallingChip = new Rect(columnNumber*cellWidth,-cellHeight,(columnNumber+1)*cellWidth,0);

        if(isPlayersTurn){
            fallingChipColor = playerChipColorInt;
        }
        else{
            fallingChipColor = enemyChipColorInt;
        }

        isPlayersTurn = !isPlayersTurn;

    }

}
