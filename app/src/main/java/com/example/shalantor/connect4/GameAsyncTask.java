package com.example.shalantor.connect4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*Asynchronous task that is used from tha game play activity , to communicate with server*/

public class GameAsyncTask extends AsyncTask<String, Void, String> {

    /*Dialog to show to the user*/
    ProgressDialog pDialog;

    private Socket socket;

    /*Will be used to either send or receive a message, 0 is for send, 1 is for receive*/
    private int operation;
    private Activity activity;

    /*Should the task show a pdialog?*/
    private boolean showDialog;

    /*Constants for the intent that will start other activities*/
    private static final String MUTE = "MUTE";
    private static final String GAME_INFO = "GAME_INFO";

    /*Interface to communicate with parent activity*/
    private PlayButtonFragment.goBackToStartFragment mCallback;

    /*Info used for the game*/
    private Integer move;
    private Integer state;
    private boolean isConnected = true;

    /*This variable represents a time out from one of the user, 1 means everything is alright,
    * 2 means that this player timed out, 3 means that the enemy player timed out*/
    private Integer timeoutStatus = 1;

    public GameAsyncTask(Socket socket,Activity activity,boolean showDialog){
        this.socket = socket;
        this.activity = activity;
        this.showDialog = showDialog;

    }

    /*Setters and getters*/
    public void setOperation(int operation){
        this.operation = operation;
    }

    public void setCallback(PlayButtonFragment.goBackToStartFragment mCallback){
        this.mCallback = mCallback;
    }

    public int getMove(){
        return move;
    }

    public int getState(){
        return state;
    }

    public int getTimeoutStatus(){
        return timeoutStatus;
    }

    public boolean getConnectionStatus(){
        return isConnected;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (showDialog) {

            /*Show dialog to user*/
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Waiting for match...");


            String message = "Waiting for match...";

            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            /*Set dialog message*/
            pDialog.setMessage(ss2);

            /*Let user cancel dialog*/
            pDialog.setCancelable(true);

            /*If dialog gets cancelled close the socket*/
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    try {
                        socket.close();
                    }
                    catch(IOException ex){
                        Log.d("CANCEL","User cancelled dialog and IO Exception occurred in socket");
                    }

                    /*Communicate with parent activity*/
                    mCallback.goBackToAccountFragment();
                    cancel(true);
                }
            });

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

            /*Set infinite timeout*/
            socket.setSoTimeout(0);

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
                while (true){

                    try {
                        /*Get response*/
                        response = inputStream.readLine();
                    }catch(IOException ex){
                        isConnected = false;
                        return response;
                    }

                    /*Socket shut down on server side*/
                    if (response == null){
                        isConnected= false;
                        return null;
                    }

                    /*Ignore message is 0 or 1 and wait for new message*/
                    if (!response.equals("0") && !response.equals("1")){
                        break;
                    }
                }

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
            /*Close dialog*/
            pDialog.dismiss();
            Log.d("RESULT",result);

            /*Start new game*/
            Intent intent = new Intent(activity, GameActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            /*Put extra info into intent*/
            intent.putExtra("MODE", 1);
            intent.putExtra(MUTE, activity.getIntent().getBooleanExtra(MUTE, false));
            intent.putExtra(GAME_INFO, result);

            /*Set socket reference for game play activity*/
            GameUtils.setSocket(socket);

            activity.startActivity(intent);
            activity.finish();
        }
        else if (operation == 1){

            /*Socket shut down on server side*/
            if (result == null){
                isConnected = false;
                return;
            }

            /*Split message*/
            String[] answer = result.split(" ");

            /*Check Message*/
            if (answer[0].equals("1")) {
                move = Integer.parseInt(answer[1]);
                state = Integer.parseInt(answer[2]);
            }
            else if (answer[0].equals("2")){
                timeoutStatus = 2;
            }
            else if (answer[0].equals("3")){
                timeoutStatus = 3;
            }
        }
    }

    @Override
    protected void onCancelled(){
        /*If showing a dialog dismiss it*/
        if (this.pDialog != null){
            this.pDialog.dismiss();
        }
    }

}
