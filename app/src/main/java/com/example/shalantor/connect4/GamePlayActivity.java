package com.example.shalantor.connect4;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GamePlayActivity extends SurfaceView implements Runnable{

    private Thread thread = null;
    private SurfaceHolder holder;
    volatile boolean playingConnect4;
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

    /*Associated activity*/
    private Activity associatedActivity;

    public GamePlayActivity(Context context){
        super(context);
        holder = getHolder();
        paint = new Paint();
        associatedActivity = (Activity) context;

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

}
