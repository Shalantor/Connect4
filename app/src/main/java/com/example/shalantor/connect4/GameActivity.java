package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class GameActivity extends Activity {

    GamePlayActivity gamePlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gamePlayView = new GamePlayActivity(this);

        setContentView(gamePlayView);
    }

    @Override
    protected void onStop(){
        super.onStop();
        gamePlayView.pause();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gamePlayView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gamePlayView.pause();
    }

    /*On key down terminate application*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            gamePlayView.pause();
            finish();
            return true;
        }
        return false;
    }
}