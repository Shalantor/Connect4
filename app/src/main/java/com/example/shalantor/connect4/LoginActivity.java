package com.example.shalantor.connect4;

import android.content.Context;
import android.content.Intent;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.app.ProgressDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    public static final int PORT = 1337;
    private AccountFragment accFragment = null;
    private LoginFragment logFragment = null;
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
        if (accFragment != null) {
            setupButtons();
        }
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
        String result="";
        try {
            result = connect.execute("").get();
        }
        catch(ExecutionException ex){
            Log.d("EXECUTION","Executionexception occured");
        }
        catch(InterruptedException ex){
            Log.d("INTERRUPT","Interrupted exception occured");
        }

        /*Return result value*/
        return result.equals("success");

    }


    /*AsyncTask for connecting to server*/
    private class Connect extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;

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

            String response = "";
            try{
                connectSocket = new Socket(address,PORT);

                /*Now read from socket to validate connection*/
                connectSocket.setSoTimeout(2);
                BufferedReader inputStream = new BufferedReader( new InputStreamReader(connectSocket.getInputStream()));

                try {
                    response = inputStream.readLine();
                    Log.wtf("RESPONSE","Response is " + response);
                    if (response.equals("0")){
                        return "success";
                    }
                }
                catch(SocketTimeoutException ex){
                    return "error";
                }

            }
            catch(IOException ex){
                return "error" ;
            }

            return "success";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();

        }
    }

    /*Override on back button pressed method to start menu activity*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            this.startActivity(intent);
            return true;
        }
        return false;
    }

    /*On click functions for all buttons*/

    /*Login button*/
    public void login(View view){

        boolean result = false;
        if (connectSocket == null){
            result = isAddressCorrect();
        }

        TextView textView = (TextView) findViewById(R.id.error_messages);
        /*TODO:REMOVE AFTER TESTING*/
        if(result){
            String showText = "Connected successfully to server";
            textView.setText(showText, TextView.BufferType.NORMAL);
        }
        else{
            String showText = "Error connecting to server";
            textView.setText(showText, TextView.BufferType.NORMAL);
        }
        /*TODO:UNTIL HERE*/

        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment).commit();

    }
}
