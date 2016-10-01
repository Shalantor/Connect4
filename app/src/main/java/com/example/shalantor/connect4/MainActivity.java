package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class MainActivity extends Activity {

    private MenuActivity menuActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            menuActivity.pause();
            finish();
            return true;
        }
        return false;
    }
}
