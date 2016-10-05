package com.example.shalantor.connect4;


import android.content.Context;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GamePlayActivity extends SurfaceView implements Runnable{

    private Thread thread = null;
    SurfaceHolder holder;
    volatile boolean playingConnect4;
    Paint paint;

    public GamePlayActivity(Context context){
        super(context);

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

    }


    /*Method to control the fps*/
    public void controlFPS(){
        
    }

}
