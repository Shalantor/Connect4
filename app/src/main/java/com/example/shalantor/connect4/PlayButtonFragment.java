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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

public class PlayButtonFragment extends Fragment{

    private Activity activity;
    private Socket connectSocket;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.play_fragment, container, false);
    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        Button playButton = (Button) activity.findViewById(R.id.play_button);
        final TextView textView = (TextView) activity.findViewById(R.id.play_error_messages);

        /*Get screen size*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Adjust text size*/
        playButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        textView.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Add listener to button*/
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /*create async task for network operation*/
                startGame netTask = new startGame();

                String result = "";
                try {
                    result = netTask.execute("5").get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                String message ="";
                if (result.equals("success")){
                    message = "Connection ok";
                }
                else{
                    message = "Connection error";
                }
                textView.setText(message, TextView.BufferType.NORMAL);
            }
        });

    }


    /*AsyncTask for connecting to server*/
    private class startGame extends AsyncTask<String, Void, String> {
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

            /*Check socket for null pointer*/
            if (connectSocket == null){
                return "Null";
            }

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
                    else{
                        return "Wrong credentials";
                    }
                }
                catch(SocketTimeoutException ex){
                    return "Out of range";
                }

            }
            catch(IOException ex){
                return "error";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();

        }
    }
}
