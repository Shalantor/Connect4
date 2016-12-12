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
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.net.Socket;

/*This class handles a match, both singleplayer and multiplayer matches*/

public class GamePlayActivity extends SurfaceView implements Runnable{

    /*Thread that will run on GUI and control animations*/
    private Thread thread = null;

    /*Used for the surface*/
    private SurfaceHolder holder;

    /*Boolean variables for game state*/
    volatile boolean playingConnect4;
    volatile boolean isSinglePlayer;
    volatile boolean isMultiPlayer ;
    volatile boolean isExitMenuVisible ;
    volatile boolean isPlayersTurn ;
    volatile boolean isColorChoiceVisible;
    volatile boolean isChipFalling;
    volatile boolean isGameOver;

    /*Tools to paint and render graphics*/
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

    /*static variables for shared preferences and intent extras*/
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

    /*Last move of player*/
    int lastMove;

    /*Async task for network operations if in multiplayer mode*/
    private GameAsyncTask sendTask;
    private GameAsyncTask receiveTask;
    private Socket gameSocket;

    /*Name of opponent*/
    String[] opponentsName;

    /*Handler for updating remaining turn time*/
    private Handler handler = new Handler();
    private Runnable runnable;
    private int time = GameUtils.MAX_TURN_TIME;


    public GamePlayActivity(Context context){
        super(context);

        /*Initialize graphic tools*/
        holder = getHolder();
        paint = new Paint();

        /*Get reference to activity*/
        associatedActivity = (Activity) context;

        /*Get intent reference to get data about game*/
        Intent intent = associatedActivity.getIntent();

        /*Case of singeplayer game*/
        if(intent.getIntExtra("MODE",-1) == 0){

            /*Set variables*/
            isSinglePlayer = true;
            isMultiPlayer = false;

            /*For start set player to have turn, so that the AI
            *wont start playing, it will change later before game starts*/
            isPlayersTurn = true;

        }
        else{
            /*Case of multiplayer*/
            isMultiPlayer = true;
            isSinglePlayer = false;

            /*Tasks to connect to server*/
            sendTask = new GameAsyncTask(GameUtils.getSocket(),associatedActivity,false);
            sendTask.setOperation(0);
            receiveTask = new GameAsyncTask(GameUtils.getSocket(),associatedActivity,false);
            receiveTask.setOperation(1);

            /*Split message from server*/
            String[] gameInfo = GameUtils.splitInfo(intent.getStringExtra(GAME_INFO));

            isPlayersTurn = gameInfo[1].equals("1");

            /*Get opponents name*/
            opponentsName = new String[gameInfo.length - 2];
            System.arraycopy(gameInfo,2,opponentsName,0,gameInfo.length - 2);

            /*Get reference to socket from final static variable*/
            gameSocket = GameUtils.getSocket();

            /*Listen on the task responsible for receiving messages*/
            receiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");

            /*handler to update turn time that runs in intervals of one second*/
            runnable = new Runnable() {
                @Override
                public void run() {
                    time -= 1;
                    handler.postDelayed(this, 1000);
                }
            };

            /*Start handler*/
            handler.postDelayed(runnable, 1000);

        }

        /*Set difficulty*/
        int difficulty = intent.getIntExtra(DIFFICULTY,0);

        /*Set maximum depth of minimax algorithm for singleplayer*/
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

        /*Initialize some variables*/
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

    /*Mute player or set volume to max*/
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

        /*Create media player object*/
        player = MediaPlayer.create(associatedActivity,R.raw.menusong);
        player.setLooping(true);

        if(isMuted){
            player.setVolume(0,0);
        }
        else {
            player.setVolume(1, 1);
        }
        player.start();

        /*Primary game loop*/
        while(playingConnect4){

            /*Do we need to change the volume of music?*/
            if(needVolumeChange){
                changeVolume();
            }

            /*It isn't the players turn, but the opponent has to wait for chip to fall*/
            if(!isPlayersTurn && !isChipFalling && !isGameOver){   /*wait for chip to fall and then get move of opponent*/

                /*Its single player, so the AI calculates its next move*/
                if (isSinglePlayer) {
                    int move = GameUtils.getMove(howManyChips, gameGrid, enemyChipColorInt, playerChipColorInt, maxDepth);
                    makeMove(move);
                }

                else{
                    /*Player waits for enemy move*/
                    if (receiveTask.getStatus() == AsyncTask.Status.FINISHED ){

                        /*Check if connection is ok*/
                        if (!receiveTask.getConnectionStatus()){
                            /*Got disconnected from server*/
                            isGameOver = true;
                            endScreenMessage = "DISCONNECTED";
                            gameEndTime = System.currentTimeMillis();
                        }
                        else if (receiveTask.getTimeoutStatus() == 2){
                            /*Didn't make a move in time*/
                            isGameOver = true;
                            endScreenMessage = "YOU LOSE";
                            gameEndTime = System.currentTimeMillis();
                        }
                        else if (receiveTask.getTimeoutStatus() == 3){
                            /*Enemy didn't make a move in time*/
                            isGameOver = true;
                            endScreenMessage = "YOU WIN";
                            gameEndTime = System.currentTimeMillis();
                        }
                        else {
                            /*Got a valid move from the opponent*/
                            int move = receiveTask.getMove();
                            int state = receiveTask.getState();
                            makeMove(move);

                            /*Start new receive task to listen to server messages*/
                            receiveTask = new GameAsyncTask(gameSocket, associatedActivity, false);
                            receiveTask.setOperation(1);
                            receiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");

                            /*Check state of game*/
                            if (state == 3) {
                                /*It's a tie*/
                                isGameOver = true;
                                endScreenMessage = "TIE";
                                gameEndTime = System.currentTimeMillis();
                            } else if (state == 2) {
                                /*It's a loss*/
                                isGameOver = true;
                                endScreenMessage = "YOU LOSE";
                                gameEndTime = System.currentTimeMillis();
                            }

                            /*Change turn and reset time*/
                            isPlayersTurn = !isPlayersTurn;
                            time = GameUtils.MAX_TURN_TIME;

                        }
                    }

                    /*Reset asynctask that was used for sending move*/
                    if(sendTask.getStatus() == AsyncTask.Status.FINISHED){
                        sendTask = new GameAsyncTask(gameSocket,associatedActivity,false);
                        sendTask.setOperation(0);
                    }

                }
            }
            else if(isMultiPlayer && isPlayersTurn){
                /*Its still players turn until server confirms move*/
                if (receiveTask.getStatus() == AsyncTask.Status.FINISHED){

                    if (!receiveTask.getConnectionStatus()){
                        /*Got disconnected*/
                        isGameOver = true;
                        endScreenMessage = "DISCONNECTED";
                        gameEndTime = System.currentTimeMillis();
                    }
                    else if (receiveTask.getTimeoutStatus() == 2){
                        /*Lost because no move*/
                        isGameOver = true;
                        endScreenMessage = "YOU LOSE";
                        gameEndTime = System.currentTimeMillis();
                    }
                    else if (receiveTask.getTimeoutStatus() == 3){
                        /*Won because no enemy move*/
                        isGameOver = true;
                        endScreenMessage = "YOU WIN";
                        gameEndTime = System.currentTimeMillis();
                    }
                    else {
                        /*Else check validity of move that was sent*/
                        int validity = receiveTask.getMove();

                        if (validity == 0) {
                            /*Move was ok*/
                            isPlayersTurn = !isPlayersTurn;
                            time = GameUtils.MAX_TURN_TIME;

                            int state = receiveTask.getState();
                            if (state == 1) {
                                /*It's a win*/
                                isGameOver = true;
                                endScreenMessage = "YOU WIN";
                                gameEndTime = System.currentTimeMillis();
                            } else if (state == 3) {
                                /*It's a loss*/
                                isGameOver = true;
                                endScreenMessage = "TIE";
                                gameEndTime = System.currentTimeMillis();
                            }

                            /*Reset async task to listen to server messages*/
                            receiveTask = new GameAsyncTask(gameSocket, associatedActivity, false);
                            receiveTask.setOperation(1);
                            receiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");

                        } else {
                            /*Undo previous move, because it was not valid*/
                            howManyChips[lastMove] -= 1;
                            gameGrid[howManyChips[lastMove]][lastMove] = 0;

                            /*Reset async task for sending*/
                            sendTask = new GameAsyncTask(gameSocket, associatedActivity, false);
                            sendTask.setOperation(0);
                        }
                    }
                }
            }

            /*Update screen and check frames per second*/
            updateGUIObjects();
            drawScreen();
            controlFPS();

            /*If the game is over, wait a few seconds and then start a new activity*/
            Log.d("TIME","Game end time is " + gameEndTime);
            if(gameEndTime > 0 ) {
                if (isSinglePlayer) {
                    /*If singleplayer just reset game*/
                    if (System.currentTimeMillis() - gameEndTime > 3000) {
                        resetGame();
                    }
                }
                else if (isMultiPlayer && System.currentTimeMillis() - gameEndTime > 3000){
                    /*Multiplayer case*/
                    if (endScreenMessage.equals("DISCONNECTED")){

                        /*Disconnected from server , so go to start menu*/
                        Intent intent = new Intent(associatedActivity,LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("MODE",1);
                        intent.putExtra(MUTE,isMuted);
                        associatedActivity.startActivity(intent);
                        associatedActivity.finish();

                    }
                    else{

                        /*Game ended normally so go back to play menu*/
                        Intent intent = new Intent(associatedActivity,LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("MODE",1);
                        intent.putExtra(MUTE,isMuted);
                        intent.putExtra("PLAY",true);
                        GameUtils.setSocket(gameSocket);
                        associatedActivity.startActivity(intent);
                        associatedActivity.finish();

                    }
                }
            }
        }

        /*Stop and release/destroy player*/
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
        if (isSinglePlayer ){
            isPlayersTurn = true;
        }

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

                /*Chip has reached its destination*/
                fallingChip = null;                         //remove chip

                /*Update board*/
                if(!isPlayersTurn)
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = playerChipColorInt;
                else
                    gameGrid[5 - howManyChips[fallingChipPosition]][fallingChipPosition] = enemyChipColorInt;

                howManyChips[fallingChipPosition] += 1;
                isChipFalling = false;

                /*Check for win to update stats*/
                if(GameUtils.hasWon(fallingChipPosition,fallingChipColor,howManyChips,gameGrid)){

                    /*Now save to stats*/
                    SharedPreferences preferences = associatedActivity.getSharedPreferences(associatedActivity.getPackageName(),Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    if(!isPlayersTurn){                     /*the chip which is falling, falls after the turn change*/
                        endScreenMessage = "YOU WIN";
                        /*Save stats*/
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
                        /*Save stats*/
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

            /*Draw background*/
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
                    /*Check color of chip in cell if there is one*/
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

            /*Draw opponent name, which is AI in singleplayer and the enemy name in multiplayer*/
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

            /*Draw text for time, in multiplayer it displays time , in singleplayer there is no time limit*/
            canvas.drawText("TIME:",8*screenWidth/10 + screenWidth/20,screenHeight/2,paint);

            if(isSinglePlayer){
                canvas.drawText("-",8*screenWidth/10 + screenWidth/20,screenHeight/2 + 2*screenHeight/15,paint);
            }
            else{
                canvas.drawText("" + time,8*screenWidth/10 + screenWidth/20,screenHeight/2 + 2*screenHeight/15,paint);
            }

            /*Draw back button and and sound button*/
            int buttonDimension = screenWidth/10;

            /*Destination rect for sound button*/
            destRect = new Rect(7*screenWidth/10,screenHeight - buttonDimension,
                                7*screenWidth/10+ buttonDimension,screenHeight );

            /*Draw sound button based on volume (if muted or not)*/
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

            /*Draw winning message if game is over*/
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

            /*Unlock surface*/
            holder.unlockCanvasAndPost(canvas);
        }
    }


    /*Method to control the refresh rate of screen*/
    public void controlFPS(){

        long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
        long timeToSleep = 10 - timeThisFrame;

        /*Make thread sleep a bit so that it won't run too fast*/
        if(timeToSleep > 0){
            try{
                thread.sleep(timeToSleep);
            }catch (InterruptedException ex){
                Log.d("THREAD_SLEEP","Interrupted exception occured");
            }
        }

        lastFrameTime = System.currentTimeMillis();

    }

    /*To pause animations and thread*/
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

        /*Coordinates of user touch*/
        float initialY = event.getY();
        float initialX = event.getX();

        /*Check in which area the user did click*/
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){

            /*Case where the exit menu is visible*/
            if(isExitMenuVisible){
                /*YES BUTTON, players want to exit game*/
                if(initialX >= screenWidth/3 - yesTextWidth && initialX <= screenWidth/3 + yesTextWidth
                        && initialY >= 2*screenHeight/3 - screenHeight/5
                        && initialY <= 2*screenHeight/3){
                    pause();

                    /*Close socket if in multiplayer*/
                    if(isMultiPlayer) {
                        try {
                            gameSocket.close();
                        } catch (IOException ex) {
                            Log.d("SOCKET_CLOSE", "Error closing socket");
                        }
                    }

                    /*Start the menu activity*/
                    Intent intent = new Intent(associatedActivity,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(MUTE,isMuted);
                    associatedActivity.startActivity(intent);
                    associatedActivity.finish();
                }
                /*NO BUTTON, player wants to go back to game*/
                if(initialX >= 2*screenWidth/3 - noTextWidth && initialX <= 2*screenWidth/3 + noTextWidth
                        && initialY >= 2*screenHeight/3 - screenHeight/5
                        && initialY <= 2*screenHeight/3){
                    isExitMenuVisible = false;
                }
                /*Return true because touch event was validated*/
                return true;
            }
            /*Case where the color choice menu is visible*/
            else if(isColorChoiceVisible){
                /*BACK BUTTON,Player wants to exit game, so show exit menu*/
                if(initialX >= 9*screenWidth/10 && initialX <= screenWidth
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    isExitMenuVisible = true;
                }
                /*SOUND BUTTON, players wants to mute or up volume of sound*/
                else if(initialX >= 7*screenWidth/10 && initialX <= 8*screenWidth/10
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight) {
                    isMuted = !isMuted;
                    needVolumeChange = true;
                }
                /*RED CHIP ICON, player chose red chip*/
                else if(initialX >= screenWidth/10 && initialX <= 3*screenWidth/10
                        && initialY >= screenHeight/3 && initialY <= 2*screenHeight/3){
                    /*Set variables accordingly*/
                    playerChipColorInt = 1;
                    playerChipColor = redChip;
                    enemyChipColorInt = 2;
                    enemyChipColor = yellowChip;
                    isColorChoiceVisible = false;
                }
                /*YELLOW CHIP ICON, player chose yellow chip*/
                else if(initialX >= 4*screenWidth/10 && initialX <= 6*screenWidth/10
                        && initialY >= screenHeight/3 && initialY <= 2*screenHeight/3){
                    /*Set variables accordingly*/
                    playerChipColorInt = 2;
                    playerChipColor = yellowChip;
                    enemyChipColor = redChip;
                    enemyChipColorInt = 1;
                    isColorChoiceVisible = false;
                }
                if (!isColorChoiceVisible && isSinglePlayer){
                    /*If in singleplayer calculate whose turn it is, after the player chose a color*/
                    isPlayersTurn = Math.random() > 0.5;
                }
            }
            else{
                /*Only the game gris is visible*/
                /*BACK BUTTON,show exit menu*/
                if(initialX >= 9*screenWidth/10 && initialX <= screenWidth
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    isExitMenuVisible = true;
                }
                /*SOUND BUTTON, change volume of sound*/
                else if(initialX >= 7*screenWidth/10 && initialX <= 8*screenWidth/10
                        && initialY >= screenHeight - screenWidth/10 && initialY <= screenHeight){
                    needVolumeChange = true;
                    isMuted = !isMuted;
                }
                else if(isPlayersTurn && !isChipFalling && !isGameOver){     /*Is it the players turn?*/
                    /*Check which column was touched*/
                    /*In all cases check if the particular column was already active and make move
                    * If there is sufficient space, else set no active column. If there was no active column
                    * set his one as the active one*/
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
                    /*Touch event was validated*/
                    return true;
                }
            }
        }
        /*Touch event was not validated*/
        return false;
    }

    /*Insert chip in specified position*/
    public void makeMove(int columnNumber){

        /*Get chip and grid info*/
        lastMove = columnNumber;
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

        /*If it is multiplayer send move to server*/
        if (isMultiPlayer && isPlayersTurn) {
            Log.d("SEND_MOVE","Sending move " + columnNumber);
            sendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"" + columnNumber);
        }

        if (isSinglePlayer) {
            isPlayersTurn = !isPlayersTurn;
        }

    }

    /*Close socket if in multiplayer*/
    public void closeSocket(){
        if (isMultiPlayer){
            try{
                gameSocket.close();
            }
            catch(IOException ex){
                Log.d("IOEXCEPTION","Error closing socket");
            }
        }
    }

}
