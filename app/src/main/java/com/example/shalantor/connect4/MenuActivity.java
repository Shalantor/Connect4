package com.example.shalantor.connect4;


import android.content.Context;
import android.view.SurfaceView;

public class MenuActivity extends SurfaceView implements Runnable{

    private boolean showingMenu;                //to stop animations on menu if not necessary

    public MenuActivity(Context context){
        super(context);
        showingMenu = true;
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

    }

    /*Method to render the elements*/
    public void drawElements(){

    }

    /*Method to control the refresh rate of screen*/
    public void controlFPS(){

    }

}
