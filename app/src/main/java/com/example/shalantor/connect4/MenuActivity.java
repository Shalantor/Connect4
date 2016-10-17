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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MenuActivity extends SurfaceView implements Runnable{

    private static final String RANK = "PLAYER_RANK";
    private static final String OFFLINE_WIN = "OFFLINE_WINS";
    private static final String ONLINE_WIN = "ONLINE_WINS";
    private static final String OFFLINE_LOS = "OFFLINE_LOSSES";
    private static final String ONLINE_LOS = "ONLINE_LOSSES";
    private static final String DIFFICULTY = "DIFFICULTY";

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
    private Bitmap backButton;

    private Thread thread = null;
    private SurfaceHolder holder;
    private Paint paint;
    private Paint stkPaint;

    /*Boolean values to know which page is showing right now*/
    private boolean isStartMenuVisible;
    private boolean isAboutPageVisible;
    private boolean isOptionsPageVisible;
    private boolean isSelectPlayModeVisible;

    /*Variables for start menu buttons*/
    private int startButtonHeight;
    private int startButtonWidth;

    private Activity associatedActiviry;

    /*Current width for button*/
    private float textRectWidth;

    /*variables for sound playing*/
    private MediaPlayer player;
    private boolean isPlayerMuted;
    private boolean needVolumeChange;

    /*Variables for player statistics*/
    private int playerRank;
    private int onlineWins;
    private int onlineLosses;
    private int offlineWins;
    private int offlineLosses;
    private int difficulty;

    /*Width of difficulties for measuring which one was pressed*/
    private float easyTextWidth;
    private float mediumTextWidth;
    private float hardTextWidth;

    public MenuActivity(Context context){
        super(context);
        showingMenu = true;
        listOfFallingChips = new ArrayList<>();
        isStartMenuVisible = true;
        isPlayerMuted = false;
        needVolumeChange = false;

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
        backButton = BitmapFactory.decodeResource(getResources(),R.mipmap.back_button);

        /*Get preferences if there exist any and set variables accordingly*/
        SharedPreferences preferences = associatedActiviry.getPreferences(Context.MODE_PRIVATE);
        playerRank = preferences.getInt(RANK,0);
        onlineLosses = preferences.getInt(ONLINE_LOS,0);
        onlineWins = preferences.getInt(ONLINE_WIN,0);
        offlineLosses = preferences.getInt(OFFLINE_LOS,0);
        offlineWins = preferences.getInt(OFFLINE_WIN,0);
        difficulty = preferences.getInt(DIFFICULTY,0);

    }

    /*Simple run method*/
    @Override
    public void run(){

        /*Load sound*/
        player = MediaPlayer.create(associatedActiviry,R.raw.menusong);
        player.setLooping(true);
        player.setVolume(1,1);
        player.start();

        while(showingMenu){
            if(needVolumeChange){
                changeVolume();
            }
            updateElements();
            drawElements();
            controlFPS();
        }

        /*Stop and release player*/
        player.pause();
        player.release();
        player = null;
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
            else if(isAboutPageVisible){
                drawAboutPageDescription();
            }
            else if(isOptionsPageVisible){
                drawOptionsPage();
            }
            else if(isSelectPlayModeVisible){
                drawSelectPlayMenu();
            }

            /*Draw sound button*/
            drawSoundButton();


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


        Paint rectPaint = new Paint();                          /*Paint for the rectangle that will be around the text*/
        textRectWidth = stkPaint.measureText("PLAY GAME");    /*width of button to highlight*/
        rectPaint.setColor(Color.argb(128,12,246,238));

        /*PLAY button*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,2* screenHeight / 25,
                screenWidth/2 + textRectWidth/2,startButtonHeight + 2*screenHeight / 25,rectPaint);
        canvas.drawText("PLAY GAME",(screenWidth / 2) ,
                startButtonHeight + screenHeight / 25,paint);
        canvas.drawText("PLAY GAME",(screenWidth / 2) ,
                startButtonHeight + screenHeight / 25, stkPaint);


        /*OPTIONS button*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,3* screenHeight / 25 + startButtonHeight,
                screenWidth/2 + textRectWidth/2,2*startButtonHeight + 3*screenHeight / 25,rectPaint);
        canvas.drawText("OPTIONS",(screenWidth / 2) ,
                2*startButtonHeight + 2*screenHeight / 25,paint);
        canvas.drawText("OPTIONS",(screenWidth / 2) ,
                2*startButtonHeight + 2*screenHeight / 25,stkPaint);


        /*ABOUT button*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,4* screenHeight / 25 + 2*startButtonHeight,
                screenWidth/2 + textRectWidth/2,3*startButtonHeight + 4*screenHeight / 25,rectPaint);
        canvas.drawText("ABOUT",(screenWidth / 2) ,
                3*startButtonHeight + 3*screenHeight / 25,paint);
        canvas.drawText("ABOUT",(screenWidth / 2) ,
                3*startButtonHeight + 3*screenHeight / 25,stkPaint);


        /*EXIT BUTTON*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,5* screenHeight / 25 + 3*startButtonHeight,
                screenWidth/2 + textRectWidth/2,4*startButtonHeight + (4.5f)*screenHeight / 25,rectPaint);
        canvas.drawText("EXIT",(screenWidth / 2) ,
                4*startButtonHeight + 4*screenHeight / 25,paint);
        canvas.drawText("EXIT",(screenWidth / 2) ,
                4*startButtonHeight + 4*screenHeight / 25,stkPaint);

    }

    /*check what player touched on screen*/
    /*TODO:complete method*/
    public boolean validateTouchEvent(MotionEvent event){
        float initialY;
        float initialX;
        initialY = event.getY();
        initialX = event.getX();

        /*First of all check if sound button was pressed*/
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            if(initialX <= 19*screenWidth/20
                    && initialX >= screenWidth - (screenWidth / 6 )
                    && initialY >= 5*screenHeight/6 -screenHeight/20
                    && initialY <= screenHeight -screenHeight/20){
                isPlayerMuted = !isPlayerMuted;
                needVolumeChange = true;
                return true;
            }
        }

        if(isStartMenuVisible){                         /*Player touched the start menu*/
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                /*Now check each button individually*/

                /*EXIT BUTTON*/
                if(initialY <= 4*startButtonHeight + (4.5f)*screenHeight / 25
                        && initialY >= 5* screenHeight / 25 + 3*startButtonHeight
                        && initialX >= screenWidth/2 - textRectWidth/2
                        && initialX <= screenWidth/2 + textRectWidth/2){
                    pause();
                    associatedActiviry.finish();
                }
                /*ABOUT BUTTON*/
                else if(initialY <= 3*startButtonHeight + 4*screenHeight / 25
                        && initialY >= 4* screenHeight / 25 + 2*startButtonHeight
                        && initialX >= screenWidth/2 - textRectWidth/2
                        && initialX <= screenWidth/2 + textRectWidth/2){
                    isStartMenuVisible = false;
                    isAboutPageVisible = true;
                    isOptionsPageVisible = false;
                    isSelectPlayModeVisible = false;
                }
                /*OPTIONS BUTTON*/
                else if(initialY <= 2*startButtonHeight + 3*screenHeight / 25
                        && initialY >= 3* screenHeight / 25 + startButtonHeight
                        && initialX >= screenWidth/2 - textRectWidth/2
                        && initialX <= screenWidth/2 + textRectWidth/2){
                    isOptionsPageVisible = true;
                    isStartMenuVisible = false;
                    isAboutPageVisible = false;
                    isSelectPlayModeVisible = false;
                }
                else if(initialY <= startButtonHeight + 2*screenHeight / 25
                        && initialY >= 2* screenHeight / 25
                        && initialX >= screenWidth/2 - textRectWidth/2
                        && initialX <= screenWidth/2 + textRectWidth/2){
                    isSelectPlayModeVisible = true;
                    isOptionsPageVisible = false;
                    isStartMenuVisible = false;
                    isAboutPageVisible = false;
                }

            }
            return true;
        }
        else if(isAboutPageVisible){
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                /*Check if back button was pressed*/
                if(initialY >= 5*screenHeight/6 -screenHeight/20
                        && initialY <= screenHeight - screenHeight/20
                        && initialX >= screenWidth/20
                        && initialX <= screenWidth/20 + screenWidth/6){
                    isStartMenuVisible = true;
                    isAboutPageVisible = false;
                    isOptionsPageVisible = false;
                    isSelectPlayModeVisible = false;
                }
            }
            return true;
        }
        else if(isOptionsPageVisible){
            /*Check if back button was pressed*/
            if(initialY >= 5*screenHeight/6 -screenHeight/20
                    && initialY <= screenHeight - screenHeight/20
                    && initialX >= screenWidth/20
                    && initialX <= screenWidth/20 + screenWidth/6){
                isStartMenuVisible = true;
                isAboutPageVisible = false;
                isOptionsPageVisible = false;
                isSelectPlayModeVisible = false;
            }
            /*one of the difficulty options was pressed*/
            else if(initialY >= 2*screenHeight/15 && initialY <= 3*screenHeight/15){
                /*Check which one*/
                boolean commitChange = false;
                if(initialX >= screenWidth/4 - easyTextWidth/2 && initialX <= screenWidth/4 + easyTextWidth/2){
                    difficulty = 0;
                    commitChange = true;
                }
                else if(initialX >= screenWidth/2 - mediumTextWidth/2 && initialX <= screenWidth/2 + mediumTextWidth/2){
                    difficulty = 1;
                    commitChange = true;
                }
                else if(initialX >= 3*screenWidth/4 - hardTextWidth/2 && initialX <= 3*screenWidth/4 + hardTextWidth/2){
                    difficulty = 2;
                    commitChange = true;
                }
                if(commitChange) {
                    SharedPreferences preferences = associatedActiviry.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(DIFFICULTY,difficulty);
                    editor.apply();
                }

            }
            return true;
        }
        else if(isSelectPlayModeVisible){
                        /*Check if back button was pressed*/
            if(initialY >= 5*screenHeight/6 -screenHeight/20
                    && initialY <= screenHeight - screenHeight/20
                    && initialX >= screenWidth/20
                    && initialX <= screenWidth/20 + screenWidth/6){
                isStartMenuVisible = true;
                isAboutPageVisible = false;
                isOptionsPageVisible = false;
                isSelectPlayModeVisible = false;
            }
            /*Single player button*/
            else if(initialX >= screenWidth/2 - textRectWidth/2 && initialX <= screenWidth/2 +textRectWidth/2
                    && initialY >= screenHeight/3 + 2*screenHeight/25 - startButtonHeight
                    && initialY <= screenHeight/3 + 2*screenHeight/25){
                Intent intent = new Intent(associatedActiviry,GameActivity.class);
                pause();
                intent.putExtra("MODE",0);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                associatedActiviry.startActivity(intent);
                associatedActiviry.finish();
            }
            else if(initialX >= screenWidth/2 - textRectWidth/2 && initialX <= screenWidth/2 +textRectWidth/2
                    && initialY >= 2*screenHeight/3  - startButtonHeight
                    && initialY <= 2*screenHeight/3){
                Intent intent = new Intent(associatedActiviry,GameActivity.class);
                pause();
                intent.putExtra("MODE",1);
                associatedActiviry.startActivity(intent);
            }
            return true;
        }
        else{
            return false;
        }
    }


    /*Method to draw the about page*/
    public void drawAboutPageDescription(){

        Paint aboutPaint = new Paint();
        aboutPaint.setColor(Color.argb(128,0,0,0));

        /*black screen with alpha value, so that text is more visible*/
        canvas.drawRect(screenWidth /10,0,9*screenWidth/10,screenHeight,aboutPaint);

        aboutPaint.setColor(Color.WHITE);
        aboutPaint.setTextAlign(Paint.Align.CENTER);
        aboutPaint.setTextSize(screenHeight / 15);
        canvas.drawText("University of Thessaly ",screenWidth/2,screenHeight / 15 + screenHeight / 30,aboutPaint);
        canvas.drawText("Winter Semester 2016-2017",screenWidth/2,2*screenHeight /15 + screenHeight /30,aboutPaint);

        drawBackButton();

    }

    /*Method to draw options page*/
    public void drawOptionsPage(){

        String tickCharacter = "✔";
        String easy = "EASY";
        String medium = "MEDIUM";
        String hard = "HARD";

        if(difficulty == 0){
            easy += tickCharacter;
        }
        else if(difficulty == 1){
            medium += tickCharacter;
        }
        else{
            hard += tickCharacter;
        }

        Paint aboutPaint = new Paint();
        aboutPaint.setColor(Color.argb(128,0,0,0));

        /*black screen with alpha value, so that text is more visible*/
        canvas.drawRect(screenWidth/10,0,9*screenWidth/10,screenHeight,aboutPaint);

        aboutPaint.setColor(Color.WHITE);
        aboutPaint.setTextAlign(Paint.Align.CENTER);
        aboutPaint.setTextSize(screenHeight / 15);

        canvas.drawText("DIFFICULTY",screenWidth/2,screenHeight / 15 + screenHeight / 30,aboutPaint);
        canvas.drawText(easy,screenWidth/4,3*screenHeight/15,aboutPaint);
        canvas.drawText(medium,screenWidth/2,3*screenHeight/15,aboutPaint);
        canvas.drawText(hard,3*screenWidth/4,3*screenHeight/15,aboutPaint);

        /*Save button widths*/
        easyTextWidth = aboutPaint.measureText(easy);
        mediumTextWidth = aboutPaint.measureText(medium);
        hardTextWidth = aboutPaint.measureText(hard);

        /*Now draw everything about rank of player*/
        aboutPaint.setTextSize(2*screenHeight/15);
        canvas.drawText("RANK " + playerRank,screenWidth/2,screenHeight/3 + screenHeight/15,aboutPaint);
        aboutPaint.setTextSize(screenHeight/15);
        canvas.drawText("ONLINE STATS",screenWidth/2,screenHeight/2,aboutPaint);
        canvas.drawText("WINS:" + onlineWins,screenWidth/3,screenHeight/2 + 2*screenHeight/15,aboutPaint);
        canvas.drawText("LOSSES:" + onlineLosses,2*screenWidth/3,screenHeight/2 + 2*screenHeight/15,aboutPaint);
        canvas.drawText("OFFLINE STATS",screenWidth/2,3*screenHeight/4,aboutPaint);
        canvas.drawText("WINS:" + offlineWins,screenWidth/3,3*screenHeight/4 + 2*screenHeight/15,aboutPaint);
        canvas.drawText("LOSSES:" + offlineLosses,2*screenWidth/3,3*screenHeight/4 + 2*screenHeight/15,aboutPaint);


        drawBackButton();

    }

    /*To draw the menu where player is selecting game mode*/
    public void drawSelectPlayMenu(){

        drawBackButton();

        /*Set up paint objects*/
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


        Paint rectPaint = new Paint();                          /*Paint for the rectangle that will be around the text*/
        textRectWidth = stkPaint.measureText("SINGLEPLAYER");    /*width of button to highlight*/
        rectPaint.setColor(Color.argb(128,12,246,238));

        /*SINGLE PLAYER BUTTON*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,screenHeight/3 + 2*screenHeight/25 - startButtonHeight,
                            screenWidth/2 + textRectWidth/2,screenHeight/3 + 2*screenHeight/25,rectPaint);
        canvas.drawText("SINGLEPLAYER",screenWidth/2,screenHeight/3 + screenHeight/25,paint);
        canvas.drawText("SINGLEPLAYER",screenWidth/2,screenHeight/3 + screenHeight/25,stkPaint);

        /*MULTI PLAYER BUTTON*/
        canvas.drawRect(screenWidth/2 - textRectWidth/2,2*screenHeight/3  - startButtonHeight,
                screenWidth/2 + textRectWidth/2,2*screenHeight/3  ,rectPaint);
        canvas.drawText("MULTIPLAYER",screenWidth/2,2*screenHeight/3 - screenHeight/25,paint);
        canvas.drawText("MULTIPLAYER",screenWidth/2,2*screenHeight/3 - screenHeight/25,stkPaint);

    }

    /*To draw the back button*/
    public void drawBackButton(){

        Rect destRect = new Rect(screenWidth/20,5*screenHeight/6 -screenHeight/20,
                screenWidth/20 + screenWidth/6,screenHeight - screenHeight/20);
        Paint aboutPaint = new Paint();

        canvas.drawBitmap(backButton,null,destRect,aboutPaint);
    }

    /*To draw sound button*/
    public void drawSoundButton(){

        /*Get correct bitmap*/
        Bitmap imageToDraw;
        if(isPlayerMuted){
            imageToDraw = BitmapFactory.decodeResource(getResources(),R.mipmap.sound_off);
        }
        else{
            imageToDraw = BitmapFactory.decodeResource(getResources(),R.mipmap.sound_on);
        }

        Rect destRect = new Rect(screenWidth - (screenWidth / 6 ),
                5*screenHeight/6 -screenHeight/20,19*screenWidth/20,screenHeight -screenHeight/20 );

        Paint soundPaint = new Paint();

        canvas.drawBitmap(imageToDraw,null,destRect,soundPaint);

    }


    /*Mute and unmute player*/
    public void changeVolume(){
        if(isPlayerMuted){
            player.setVolume(0,0);
        }
        else{
            player.setVolume(1,1);
        }
        needVolumeChange = false;
    }

}
