package com.example.shalantor.connect4;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.app.ProgressDialog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    public static final int PORT = 1337;
    private AccountFragment accFragment;
    private Socket connectSocket;
    public String address;

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
        EditText address = (EditText) findViewById(R.id.ip_address);

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

    /*Method to check if server is listening*/
    private boolean isAddressCorrect(){

        /*First get text of edittext*/
        EditText addressText = (EditText) findViewById(R.id.ip_address);
        address = addressText.getText().toString();

        /*Try connecting to server*/
        Connect connect = new Connect();
        connect.execute("");
        return true;

    }

    /*AsyncTask for connecting to server*/
    private class Connect extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;
        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Connecting to server");


            String message= "Connecting to server";

            SpannableString ss2 =  new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            pDialog.setMessage(ss2);

            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try{
                connectSocket = new Socket(address,PORT);
            }
            catch(IOException ex){
                return "error";
            }

            return "Executed!";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();

        }
    }

    /*On click functions for all buttons*/

    /*Login button*/
    public void login(View view){
        boolean result = isAddressCorrect();
    }
}
