package com.example.shalantor.connect4;


import android.graphics.Rect;

public class CustomRect {

    private int color;
    private Rect rect;

    public CustomRect(int color,Rect rect){
        this.color = color;
        this.rect = rect;
    }

    public int getColor(){
        return color;
    }

    public Rect getRect(){
        return rect;
    }

}
