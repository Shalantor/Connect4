package com.example.shalantor.connect4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameAsyncTask extends AsyncTask<String, Void, String> {

    ProgressDialog pDialog;
    private Socket socket;
    private int operation;  /*Will be used to either send or receive a message, 0 is for send, 1 is for receive*/
    private Activity activity;
    private boolean showDialog; /*Should the task show a pdialog?*/
    private static final String MUTE = "MUTE";
    private static final String GAME_INFO = "GAME_INFO";

    public GameAsyncTask(Socket socket,Activity activity,boolean showDialog){
        this.socket = socket;
        this.activity = activity;
        this.showDialog = showDialog;
    }

    public void setOperation(int operation){
        this.operation = operation;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (showDialog) {
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Waiting for match...");


            String message = "Waiting for match...";

            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            pDialog.setMessage(ss2);

            pDialog.setCancelable(true);
            pDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {

        String response = "";
        try {

            /*Set up tools for sending and reading from socket*/
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream());

            if (operation == 0) {
                String messageToSend = "";

                /*Construct message to send*/
                for (int i = 0; i < params.length; i++) {
                    messageToSend += params[i] + " ";
                }

                /*Now send message*/
                outputStream.print(messageToSend);
                outputStream.flush();
            }
            else {
                response = inputStream.readLine();
            }

        } catch (IOException ex) {
            return AccountManagementUtils.IOEXCEPTION;
        }

        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (showDialog) {
            pDialog.dismiss();

            /*Start new game*/
            Intent intent = new Intent(activity, GameActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("MODE", 1);
            intent.putExtra(MUTE, activity.getIntent().getBooleanExtra(MUTE, false));
            intent.putExtra(GAME_INFO, result);
            GameUtils.setSocket(socket);
            activity.startActivity(intent);
            activity.finish();
        }
    }

}
