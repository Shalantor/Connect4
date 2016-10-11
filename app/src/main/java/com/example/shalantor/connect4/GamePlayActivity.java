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
    volatile boolean isMultiplayer = false;
    volatile boolean isMuted;
    volatile boolean isExitMenuVisible ;
    volatile boolean isPlayersTurn = true;

    private Paint paint;
    private Canvas canvas;

    /*fps*/
    private long lastFrameTime;

    /*Screen dimensions info*/
    private int screenHeight;
    private int screenWidth;
    private int cellWidth;
    private int cellheight;
    private int[][] gameGrid;

    /*image variables*/
    private Bitmap redChip;
    private Bitmap yellowChip;
    private Bitmap emptyCell;
    private Bitmap yellowCell;
    private Bitmap redCell;
    private Bitmap soundOn;
    private Bitmap soundOff;
    private Bitmap backButton;

    /*Width of texts*/
    private float noTextWidth;
    private float yesTextWidth;

    /*Associated activity*/
    private Activity associatedActivity;

    /*Variable to store which column was chose by the player, where -1 means that no column is chosen right now*/
    private int activeColumnNumber;

    public GamePlayActivity(Context context){
        super(context);
        holder = getHolder();
        paint = new Paint();
        associatedActivity = (Activity) context;
        Intent intent = associatedActivity.getIntent();
        if(intent.getIntExtra("MODE",-1) == 0){
            isSinglePlayer = true;
            isMultiplayer = false;
        }
        else{
            isMultiplayer = true;
            isSinglePlayer = false;
        }

        /*Boolean variables*/
        isExitMenuVisible = false;
        isMuted = false;
        activeColumnNumber = -1;

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


    }

    @Override
    public void run(){
        while(playingConnect4){
            updateGUIObjects();
            drawScreen();
            controlFPS();
        }
    }


    /*Method to update GUI components that will animate the menu*/
    public void updateGUIObjects(){

    }


    /*Method to draw the game fields and the menu on screen*/
    public void drawScreen(){
        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);                  /*Background*/
            paint = new Paint();

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

            /*Highlight the ative column if there is one*/
            if(activeColumnNumber >= 0) {
                paint.setColor(Color.argb(128, 0, 255, 0));
                canvas.drawRect(screenWidth/10 * activeColumnNumber,0,
                        (activeColumnNumber + 1) * screenWidth/10,screenHeight,paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }


    /*Method to control the refresh rate of screen*/
    public void controlFPS(){

        long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
        long timeToSleep = 50 - timeThisFrame;

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
        try{
            thread.join();
        }catch (InterruptedException ex){
            Log.d("THREAD_JOIN","Interrupted exception occured");
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
                    Intent intent = new Intent(associatedActivity,MainActivity.class);
                    pause();
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
                else if(isPlayersTurn){     /*Is it the players turn?*/
                    /*Check which column is active*/
                    if(initialX <= screenWidth/10){
                        activeColumnNumber = 0;
                    }
                    else if(initialX <= 2*screenWidth/10){
                        activeColumnNumber = 1;
                    }
                    else if(initialX <= 3*screenWidth/10){
                        activeColumnNumber = 2;
                    }
                    else if(initialX <= 4*screenWidth/10){
                        activeColumnNumber = 3;
                    }
                    else if(initialX <= 5*screenWidth/10){
                        activeColumnNumber = 4;
                    }
                    else if(initialX <= 6*screenWidth/10){
                        activeColumnNumber = 5;
                    }
                    else if(initialX <= 7*screenWidth/10){
                        activeColumnNumber = 6;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /*Insert chip in specified position*/

}
