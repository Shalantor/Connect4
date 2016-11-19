package com.example.shalantor.connect4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

public class LoginFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";
    public Activity activity;
    public Socket connectSocket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.login_fragment, container, false);
    }
    

    /*Get socket from activity*/
    public void setConnectSocket(Socket socket){
        connectSocket = socket;
    }

    /*AsyncTask for connecting to server*/
    private class Login extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(activity);
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

                /*Set up tools for sending and reading from socket*/
                connectSocket.setSoTimeout(5000);
                BufferedReader inputStream = new BufferedReader( new InputStreamReader(connectSocket.getInputStream()));
                PrintWriter outputStream = new PrintWriter(connectSocket.getOutputStream());

                String messageToSend = "";
                /*Construct message to send*/
                for(int i =0; i < params.length; i++ ){
                    messageToSend += params[i] + " ";
                }

                /*Now send message*/
                outputStream.print(messageToSend);
                outputStream.flush();

                /*Now read answer from socket*/

                try {
                    response = inputStream.readLine();
                    if (response.equals("0")){
                        return "success";
                    }
                }
                catch(SocketTimeoutException ex){
                    return "error";
                }

            }
            catch(IOException ex){
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


    /*Method to adjust button size and text*/
    public void adjustButtons(){
        final Activity activity = getActivity();

        /*Get references to GUI objects*/
        final EditText usernamePrompt = (EditText) activity.findViewById(R.id.username);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.password);
        TextView forgotPassword = (TextView) activity.findViewById(R.id.forgot_password);
        CheckBox rememberMe = (CheckBox) activity.findViewById(R.id.remember_me);
        Button loginButton = (Button) activity.findViewById(R.id.login);
        final TextView textView = (TextView) activity.findViewById(R.id.login_error_messages);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*SET SIZES*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        forgotPassword.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        rememberMe.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        loginButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        textView.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Set texts if there are saved credentials*/
        SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

        if ( preferences.getInt(USER_TYPE,-1) == 0){
            usernamePrompt.setText(preferences.getString(USERNAME,null));
        }

        /*Now set on click listener for login button*/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Get texts from edit texts*/
                String username = usernamePrompt.getText().toString().trim();
                String password = passwordPrompt.getText().toString().trim();
                String email;

                /*Check if fields are empty*/
                if (username.length() == 0 || password.length() == 0){
                    String errorMessage = "Please enter a username and a password";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                }

                /*Find out if username is an email or not*/
                /*TODO:Do not allow @ character in name*/
                if (username.contains("@")){
                    email = username;
                    username = "0";
                }
                else{
                    email = "0";
                }

                /*Now send message to server and wait for answer*/
                Login login = new Login();
                String result = "";
                try {
                    result = login.execute("1","0","0",username,email,password).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                /*Check result*/

                if(result.equals("success")) {
                    String message = "Registered succesfully";
                    textView.setText(message, TextView.BufferType.NORMAL);
                }
                else{
                    String message = "Problem reaching server ";
                    textView.setText(message, TextView.BufferType.NORMAL);
                }
            }
        });


    }

}
