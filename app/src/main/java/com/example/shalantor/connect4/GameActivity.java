package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    GamePlayActivity gamePlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gamePlayView = new GamePlayActivity(this);

        setContentView(gamePlayView);
    }
}
