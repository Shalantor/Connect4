package com.example.shalantor.connect4;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    private AccountFragment accFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accFragment = new AccountFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, accFragment).commit();
    }

    @Override
    protected void onStart(){
        super.onStart();
        setupButtons();
    }

    /*Method to adjust text size*/
    private void setupButtons(){

        /*Get references to buttons and editText*/
        Button continueButton = (Button) findViewById(R.id.continue_button);
        Button loginButton = (Button) findViewById(R.id.login_button);
        Button registerButton = (Button) findViewById(R.id.register_button);
        Button fbButton = (Button) findViewById(R.id.login_fb_button);
        final EditText address = (EditText) findViewById(R.id.ip_address);

        /*Get screen dimensions*/
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        /*set dimensions of components according to screen size*/
        continueButton.setTextSize(height / SCREEN_TO_TEXT_SIZE_RATIO);
        loginButton.setTextSize(height / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(height / SCREEN_TO_TEXT_SIZE_RATIO);
        fbButton.setTextSize(height / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setTextSize(height / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setSelected(false);
        address.clearFocus();

        /*If user is new , disable the current account button*/
        /*This will be checked with the sharedPreferences*/
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        int userType = preferences.getInt(USER_TYPE,-1);

        /*No user*/
        if (userType == -1){
            continueButton.setEnabled(false);
        }

    }
}
