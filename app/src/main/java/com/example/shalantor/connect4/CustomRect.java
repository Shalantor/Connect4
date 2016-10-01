package com.example.shalantor.connect4;


import android.graphics.Bitmap;
import android.graphics.Rect;

public class CustomRect {

    private Bitmap bitmap;
    private Rect rect;

    public CustomRect(Bitmap bitmap,Rect rect){
        this.bitmap = bitmap;
        this.rect = rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public Rect getRect(){
        return rect;
    }

}
