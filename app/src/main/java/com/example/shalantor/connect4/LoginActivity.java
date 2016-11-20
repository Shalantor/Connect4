package com.example.shalantor.connect4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import com.facebook.CallbackManager;

public class LoginActivity extends AppCompatActivity implements AccountFragment.setSocket {

    /*String tags for sharedpreferences that store user data*/
    public static final String USER_TYPE = "USER_TYPE";
    public static final String FB_USERNAME = "FB_USERNAME";
    public static final String FACEBOOK_ID = "FB_ID";
    public static final String SERVER_ADDRESS = "SERVER";

    /*String tags for fragment identification*/
    public static final String ACCOUNT_FRAGMENT = "ACCOUNT_FRAGMENT";
    public static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    public static final String REGISTER_FRAGMENT = "REGISTER_FRAGMENT";
    public static final String RESET_PASSWORD_FRAGMENT = "RESET_PASSWORD_FRAGMENT";
    public static final String NEW_PASSWORD_FRAGMENT = "NEW_PASSWORD_FRAGMENT";

    public static final int PORT = 1337;
    private AccountFragment accFragment = null;
    private LoginFragment logFragment = null;
    private RegisterFragment regFragment = null;
    private ResetPasswordFragment resetFragment = null;
    private NewPasswordFragment newPassFragment = null;

    private Socket connectSocket;
    public String address;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);
        accFragment = new AccountFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, accFragment,ACCOUNT_FRAGMENT).commit();

    }

    /*Implement interface*/
    @Override
    public Socket getSocketReference(){
        return connectSocket;
    }

    @Override
    public void setSocketReference(){
        boolean result = false;
        if (connectSocket == null){
            result = isAddressCorrect();
        }
    }


    @Override
    protected void onStart(){
        super.onStart();
        if (accFragment != null) {
            accFragment.adjustButtons();
        }
    }


    /*Method to check if server is listening*/
    private boolean isAddressCorrect(){

        /*First get text of edittext*/
        EditText addressText = (EditText) findViewById(R.id.ip_address);
        address = addressText.getText().toString();

        /*Store address in preferences*/
        SharedPreferences preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SERVER_ADDRESS,address);
        editor.apply();

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
                connectSocket.setSoTimeout(5000);
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
            accFragment = (AccountFragment) getSupportFragmentManager().findFragmentByTag(ACCOUNT_FRAGMENT);
            if (accFragment != null) {
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            }
            else if (getSupportFragmentManager().findFragmentByTag(RESET_PASSWORD_FRAGMENT) != null){
                logFragment = new LoginFragment();
                accFragment = null;
                regFragment = null;
                resetFragment = null;
                getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
                getSupportFragmentManager().executePendingTransactions();
                logFragment.adjustButtons();
            }
            else{
                accFragment = new AccountFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,accFragment,ACCOUNT_FRAGMENT).commit();
                getSupportFragmentManager().executePendingTransactions();
                accFragment.adjustButtons();
                logFragment = null;
                regFragment = null;
                resetFragment = null;
            }
        }
        return false;
    }

    /*On click functions for all buttons*/

    /*Go to login fragment button*/
    public void goToLogin(View view){

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

        /*TODO:ONLY REPLACE FRAGMENT WHEN LOGGED IN SUCCESSFULLY*/
        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        logFragment.adjustButtons();
        logFragment.setConnectSocket(connectSocket);
        accFragment = null;
        regFragment = null;

    }

    /*Go to registry fragment button*/
    public void goToRegister(View view){

        boolean result = false;
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        /*Replace fragments*/
        regFragment = new RegisterFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,regFragment,REGISTER_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        regFragment.adjustButtons();
        accFragment = null;
        logFragment = null;

        regFragment.setConnectSocket(connectSocket);
    }

    /*Continue with facebookButton*/
    public void continueFacebook(View view){

        boolean result = false;
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        /*Read send data from preferences*/
        SharedPreferences preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        String username = preferences.getString(FB_USERNAME,null);
        String facebookID = preferences.getString(FACEBOOK_ID,null);

        ContinueFB continueFB = new ContinueFB();
        String OpResult = "";
        try {
            OpResult = continueFB.execute("1","1",facebookID,username,"0","0").get();
        }
        catch(ExecutionException ex){
            Log.d("EXECUTION","Executionexception occured");
        }
        catch(InterruptedException ex){
            Log.d("INTERRUPT","Interrupted exception occured");
        }



    }

    /*Async task for connecting to server*/
    private class ContinueFB extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Connecting to server");


            String message = "Connecting to server";

            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            pDialog.setMessage(ss2);

            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                /*Set up tools for sending and reading from socket*/
                connectSocket.setSoTimeout(5000);
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
                PrintWriter outputStream = new PrintWriter(connectSocket.getOutputStream());

                String messageToSend = "";
                /*Construct message to send*/
                for (int i = 0; i < params.length; i++) {
                    messageToSend += params[i] + " ";
                }

                /*Now send message*/
                outputStream.print(messageToSend);
                outputStream.flush();

                /*Now read answer from socket*/

                try {
                    response = inputStream.readLine();
                    if (response.equals("0")) {
                        return "success";
                    }
                } catch (SocketTimeoutException ex) {
                    return "error";
                }

            } catch (IOException ex) {
                return "error";
            }

            return "success";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();

        }
    }

    /*Listener for the reset password textview*/
    public void goToResetFragment(View view){
        resetFragment = new ResetPasswordFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,resetFragment,RESET_PASSWORD_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        resetFragment.adjustButtons();
        resetFragment.setSocket(connectSocket);
    }

}
