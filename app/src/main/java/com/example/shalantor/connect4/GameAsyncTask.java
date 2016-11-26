package com.example.shalantor.connect4;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameAsyncTask extends AsyncTask<String, Void, String> {

    private Socket socket;
    private int operation;  /*Will be used to either send or receive a message, 0 is for send, 1 is for receive*/

    public GameAsyncTask(Socket socket){
        this.socket = socket;
    }

    public void setOperation(int operation){
        this.operation = operation;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String response = "";
        try {

            /*Set up tools for sending and reading from socket*/
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream());

            if (operation == 1) {
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

    }


}
