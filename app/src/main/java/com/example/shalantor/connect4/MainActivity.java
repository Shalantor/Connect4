package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

/*This activity is showed whenever the app starts*/
public class MainActivity extends Activity {

    /*View to show*/
    private MenuActivity menuActivity;

    /*Variable to check if menu is visible*/
    private boolean isMenuVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMenuVisible = true;

        /*Show view*/
        menuActivity = new MenuActivity(this);
        setContentView(menuActivity);
    }

    /*In lifecycle methods just pause or resume showing the animations of the manuactivity*/
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

    /*On key down back button terminate application*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            menuActivity.pause();
            finish();
            return true;
        }
        return false;
    }

    /*In case of touchevent foward it to the MenuActivity*/
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
