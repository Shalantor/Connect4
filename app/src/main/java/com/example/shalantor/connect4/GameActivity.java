package com.example.shalantor.connect4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

/*Activity that sets up and starts the game*/

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
            gamePlayView.closeSocket();
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        return false;
    }

    /*In case of touchevent foward it to the MenuActivity or the PlayActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gamePlayView.validateTouchEvent(event);
    }
}
