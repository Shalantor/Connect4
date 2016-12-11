package com.example.shalantor.connect4;

/*A class that represents the chips falling from the top of the screen to the bottom*/

import android.graphics.Bitmap;
import android.graphics.Rect;

public class CustomRect {

    private Bitmap bitmap;
    private Rect rect;

    public CustomRect(Bitmap bitmap,Rect rect){
        this.bitmap = bitmap;
        this.rect = rect;
    }

    /*Getter methods*/
    public Bitmap getBitmap(){
        return bitmap;
    }

    public Rect getRect(){
        return rect;
    }

}
