package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class MainActivity extends Activity {

    private MenuActivity menuActivity;
    private boolean isMenuVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMenuVisible = true;

        menuActivity = new MenuActivity(this);
        setContentView(menuActivity);
    }

    @Override
    protected void onStop(){
        super.onStop();
        menuActivity.pause();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        menuActivity.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        menuActivity.pause();
    }

    /*On key down terminate application*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            menuActivity.pause();
            finish();
            return true;
        }
        return false;
    }

    /*In case of touchevent foward it to the MenuActivity or the PlayActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(isMenuVisible){
            return(menuActivity.validateTouchEvent(event));
        }
        else{
            return false;
        }
    }
}
