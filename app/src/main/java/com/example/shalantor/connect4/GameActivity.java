package com.example.shalantor.connect4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

/*Activity that sets up and starts the game*/

public class GameActivity extends Activity {

    /*View that will be displayed once game starts*/
    GamePlayActivity gamePlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Set view*/
        gamePlayView = new GamePlayActivity(this);

        setContentView(gamePlayView);
    }

    @Override
    protected void onStop(){
        super.onStop();

        /*Pause animations of view*/
        gamePlayView.pause();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();

        /*Resume game animations*/
        gamePlayView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();

        /*Pause animations of view*/
        gamePlayView.pause();
    }

    /*On key down terminate application*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){

            /*Pause animations and close socket if in multiplayer*/
            gamePlayView.pause();
            gamePlayView.closeSocket();

            /*Finish this activity*/
            finish();

            /*Start menu activity*/
            Intent intent = new Intent(this,MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        return false;
    }

    /*In case of touch event forward it to the PlayActivity*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        return gamePlayView.validateTouchEvent(event);
    }
}
