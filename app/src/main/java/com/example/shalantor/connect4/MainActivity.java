package com.example.shalantor.connect4;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private MenuActivity menuActivity;
    private int screenWidth;
    private int screenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        menuActivity = new MenuActivity(this);
        setContentView(menuActivity);
    }
}
