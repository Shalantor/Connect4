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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MenuActivity extends SurfaceView implements Runnable{

    private volatile boolean showingMenu;                //to stop animations on menu if not necessary
    private Canvas canvas;
    private List<CustomRect> listOfFallingChips;      //list of the chips that will fall fro mtop to bottom

    /*Dimensions needed for menu screen*/
    private int screenWidth;
    private int screenHeight;
    private int chipDimension;

    private long lastFrameTime;                 //for fps

    /*Bitmaps with the images*/
    private Bitmap redChip;
    private Bitmap yellowChip;

    private Thread thread = null;
    private SurfaceHolder holder;
    private Paint paint;
    private Paint stkPaint;
    private boolean isStartMenuVisible;

    /*Variables for start menu buttons*/
    private int startButtonHeight;
    private int startButtonWidth;

    private Activity associatedActiviry;

    public MenuActivity(Context context){
        super(context);
        showingMenu = true;
        listOfFallingChips = new ArrayList<>();
        isStartMenuVisible = true;

        /*Type cast to get screen resolution*/
        associatedActiviry = (Activity) context;
        holder = getHolder();

        /*get screen dimensions*/
        Display display = associatedActiviry.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        chipDimension = screenWidth / 10;

        /*Get dimension for start menu*/
        startButtonHeight = screenHeight / 5;
        startButtonWidth = screenWidth / 3;

        /*Load images*/
        redChip = BitmapFactory.decodeResource(getResources(),R.mipmap.redchip);
        yellowChip = BitmapFactory.decodeResource(getResources(),R.mipmap.yellowchip);

    }

    /*Simple run method*/
    @Override
    public void run(){
        while(showingMenu){
            updateElements();
            drawElements();
            controlFPS();
        }
    }

    /*Method to update position of elements in menu screen*/
    public void updateElements(){

        /*Remove chips which have fallen through whole screen*/
        for (Iterator<CustomRect> iterator = listOfFallingChips.iterator(); iterator.hasNext();) {
            CustomRect rect = iterator.next();
            if (rect.getRect().top >= screenHeight) {
                iterator.remove();
            }
        }

        if(listOfFallingChips.size() < 4 && areChipsApartEnough()) {     /*Don't add more than 4 chips at a time*/

            /*Now add a chip at random coordinates*/
            Random generator = new Random();
            int left = generator.nextInt(screenWidth - chipDimension);
            int right = left + chipDimension;
            int bottom = 0;
            int top = -chipDimension;

            /*rect and colors*/
            Rect rect = new Rect(left, top, right, bottom);
            Bitmap chip;
            if (Math.random() > 0.5) {
                chip = yellowChip;
            } else {
                chip = redChip;
            }
            listOfFallingChips.add(new CustomRect(chip, rect));
        }

        /*Now move all chips down a bit*/
        for(CustomRect customRect : listOfFallingChips){
            customRect.getRect().top += chipDimension / 10;
            customRect.getRect().bottom += chipDimension / 10;
        }


    }

    /*Method to render the elements*/
    public void drawElements(){

        if(holder.getSurface().isValid()){
            canvas = holder.lockCanvas();
            paint = new Paint();

            /*Make background blue*/
            canvas.drawColor(Color.BLUE);

            /*Draw the falling chips*/
            for(CustomRect customRect : listOfFallingChips){
                canvas.drawBitmap(customRect.getBitmap(),null,customRect.getRect(),paint);
            }

            /*Check which buttons to draw*/
            if(isStartMenuVisible){
                drawMenuButtons();
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
        showingMenu = false;
        try{
            thread.join();
        }catch (InterruptedException ex){
            Log.d("THREAD_JOIN","Interrupted exception occured");
        }
    }

    /*To continue animation from a pause*/
    public void resume(){
        showingMenu = true;
        thread = new Thread(this);
        thread.start();
    }

    /*Method to check if chips are apart enough from each other*/
    private boolean areChipsApartEnough(){
        for(CustomRect customRect : listOfFallingChips){
            if(customRect.getRect().top <= screenHeight / 4){
                return false;
            }
        }
        return true;
    }

    /*Draws the buttons for the main start menu*/
    public void drawMenuButtons(){

        paint = new Paint();
        paint.setColor(Color.argb(255,252,162,7));
        paint.setTextSize(startButtonHeight);
        paint.setTextAlign(Paint.Align.CENTER);

        stkPaint = new Paint();
        stkPaint.setStyle(Paint.Style.STROKE);
        stkPaint.setStrokeWidth(4);
        stkPaint.setTextSize(startButtonHeight);
        stkPaint.setTextAlign(Paint.Align.CENTER);
        stkPaint.setColor(Color.BLACK);   /*Orange color*/

        /*PLAY button*/
        canvas.drawText("PLAY GAME",(screenWidth / 2) ,
                startButtonHeight + screenHeight / 25,paint);
        canvas.drawText("PLAY GAME",(screenWidth / 2) ,
                startButtonHeight + screenHeight / 25, stkPaint);


        /*OPTIONS button*/
        canvas.drawText("OPTIONS",(screenWidth / 2) ,
                2*startButtonHeight + 2*screenHeight / 25,paint);
        canvas.drawText("OPTIONS",(screenWidth / 2) ,
                2*startButtonHeight + 2*screenHeight / 25,stkPaint);


        /*ABOUT button*/
        canvas.drawText("ABOUT",(screenWidth / 2) ,
                3*startButtonHeight + 3*screenHeight / 25,paint);
        canvas.drawText("ABOUT",(screenWidth / 2) ,
                3*startButtonHeight + 3*screenHeight / 25,stkPaint);


        /*EXIT BUTTON*/
        canvas.drawText("EXIT",(screenWidth / 2) ,
                4*startButtonHeight + 4*screenHeight / 25,paint);
        canvas.drawText("EXIT",(screenWidth / 2) ,
                4*startButtonHeight + 4*screenHeight / 25,stkPaint);

    }

    /*check what player touched on screen*/
    /*TODO:complete method*/
    public boolean validateTouchEvent(MotionEvent event){
        if(isStartMenuVisible){                         /*Player touched the start menu*/
            float initialX,initialY;
            float touchSurfaceWidth = stkPaint.measureText("PLAY GAME");
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                initialY = event.getY();
                /*Now check each button individually*/
                /*EXIT BUTTON*/
                if(initialY <= 4*startButtonHeight + 4*screenHeight / 25
                        && initialY >= 3*startButtonHeight + 4*screenHeight / 25){
                    pause();
                    associatedActiviry.finish();
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

}
