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
import android.view.Display;
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

    public MenuActivity(Context context){
        super(context);
        showingMenu = true;
        listOfFallingChips = new ArrayList<>();

        /*Type cast to get screen resolution*/
        Activity parent = (Activity) context;

        /*get screen dimensions*/
        Display display = parent.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        chipDimension = screenWidth / 10;

        /*Load images*/
        redChip = BitmapFactory.decodeResource(getResources(),R.mipmap.redchip);
        yellowChip = BitmapFactory.decodeResource(getResources(),R.mipmap.yellowChip);

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
        if(listOfFallingChips.size() >= 3){     /*Don't add more than 3 chips at a time*/
            return;
        }

        /*Remove chips which have fallen through whole screen*/
        for (Iterator<CustomRect> iterator = listOfFallingChips.iterator(); iterator.hasNext();) {
            CustomRect rect = iterator.next();
            if (rect.getRect().top >= 3) {
                iterator.remove();
            }
        }

        /*Now add a chip at random*/
        if(Math.random() > 0.5){
            /*Coordinates*/
            Random generator = new Random();
            int left = generator.nextInt(screenWidth - chipDimension);
            int right = left + chipDimension;
            int bottom = 0;
            int top = -chipDimension;

            /*rect and colors*/
            Rect rect = new Rect(left,top,right,bottom);
            int chipColor;
            if(Math.random() > 0.5){
                chipColor = Color.YELLOW;
            }
            else{
                chipColor = Color.RED;
            }
            listOfFallingChips.add(new CustomRect(chipColor,rect));
        }

    }

    /*Method to render the elements*/
    public void drawElements(){

    }

    /*Method to control the refresh rate of screen*/
    public void controlFPS(){

    }

}
