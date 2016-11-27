package com.example.shalantor.connect4;


import android.app.Activity;
import android.app.ProgressDialog;
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
import java.net.SocketTimeoutException;

public class NetworkOperationsTask extends AsyncTask<String, Void, String> {

    ProgressDialog pDialog;
    private Activity activity;
    private Socket socket;

    public NetworkOperationsTask(Socket socket,Activity activity){
        this.socket = socket;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pDialog = new ProgressDialog(activity);
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
        if (socket == null){
            return AccountManagementUtils.NO_BIND;
        }
        try {

            /*Set up tools for sending and reading from socket*/
            socket.setSoTimeout(5);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream());

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
                    return AccountManagementUtils.OK;
                }
                else{
                    return AccountManagementUtils.ERROR;
                }
            } catch (SocketTimeoutException ex) {
                return AccountManagementUtils.SOCKET_TIMEOUT;
            }

        } catch (IOException ex) {
            return AccountManagementUtils.IOEXCEPTION;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        pDialog.dismiss();

    }
}
