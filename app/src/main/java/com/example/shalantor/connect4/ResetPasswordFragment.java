package com.example.shalantor.connect4;


import android.app.Activity;
import android.app.ProgressDialog;
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

public class ResetPasswordFragment extends Fragment{

    private Activity activity;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    private Socket connectSocket;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.reset_password_fragment, container, false);
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.reset_mail);
        final EditText resetCodePrompt = (EditText) activity.findViewById(R.id.reset_code);
        Button sendCode = (Button) activity.findViewById(R.id.send_code);
        Button submitCode = (Button) activity.findViewById(R.id.submit_code);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_reset);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Change size of embedded text*/
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        resetCodePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        sendCode.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        submitCode.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        textView.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*on click listener for send code button*/
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*get text from emailPrompt*/
                String input = emailPrompt.getText().toString().trim();
                String name = "0";

                if(input.length() == 0){
                    String message = "Please enter your email or name";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check format of input */
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(input);
                if (!matcher.find()){
                    name = input;
                    input = "0";
                }

                /*create async task for getting password*/
                NetworkOps netTask = new NetworkOps();

                String result = "";
                try {
                    result = netTask.execute("3",input,name).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }
            }

        });


        /*Listener for submit button.Submit button sends 2 messages to server, one
        * to confirm the reset code , and the other one to actually change it.*/
        submitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*get text from emailPrompt*/
                String input = emailPrompt.getText().toString().trim();
                String name = "0";

                if(input.length() == 0){
                    String message = "Please enter your email or name";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check format of input */
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(input);
                if (!matcher.find()){
                    name = input;
                    input = "0";
                }

                /*Read code*/
                String code = resetCodePrompt.getText().toString().trim();

                /*create async task for network operation*/
                NetworkOps netTask = new NetworkOps();

                String result = "";
                try {
                    result = netTask.execute("4",input,name,code).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }
            }
        });


    }

    /*Get socket reference from main activity*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

    /*AsyncTask for network operations*/
    private class NetworkOps extends AsyncTask<String, Void, String> {
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

}
