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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final String USER_TYPE = "USER_TYPE";
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";
    public Activity activity;
    private Socket connectSocket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();

        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    /*AsyncTask for connecting to server*/
    private class Register extends AsyncTask<String, Void, String> {
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


    public void adjustButtons(){

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Get references to components*/
        final EditText usernamePrompt = (EditText) activity.findViewById(R.id.register_username);
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.register_email);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.register_password);
        final EditText passwordVerify = (EditText) activity.findViewById(R.id.register_password_verify);
        Button registerButton = (Button) activity.findViewById(R.id.register_button_final);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_register);
        final CheckBox remember = (CheckBox) activity.findViewById(R.id.remember_me_register);

        /*Set text sizes*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordVerify.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        remember.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Add listener to register button*/
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Check if user entered anything in username prompt*/
                String usernameInput = usernamePrompt.getText().toString().trim();
                if (usernameInput.length() == 0){
                    String errorMessage = "Please enter a username";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if email is in right format */
                String emailInput = emailPrompt.getText().toString();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailInput);
                if (!matcher.find()){
                    String errorMessage = "Please enter a valid email address format";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if password prompts have the same password*/
                String password = passwordPrompt.getText().toString().trim();
                String verifyPassword = passwordVerify.getText().toString().trim();

                /*Check password length first*/
                if (password.length() < PASSWORD_MIN_LENGTH){
                    String errorMessage = "Password must contain at least 8 characters";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                if (!password.equals(verifyPassword)){
                    String errorMessage = "Passwords don't match";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*If user clicked remember me then save his credentials*/
                if (remember.isEnabled()) {

                    SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

                    /*Store user data*/
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(USER_TYPE, 0);
                    editor.putString(USERNAME, usernameInput);
                    editor.putString(EMAIL, emailInput);
                    editor.apply();

                }

                /*Now send message to server and wait for answer*/
                Register register = new Register();
                String result = "";
                try {
                    result = register.execute("0","0","0",usernameInput,emailInput,password).get();
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

    /*Get socket from activity*/

    public void setConnectSocket(Socket socket){
        connectSocket = socket;
    }
}
