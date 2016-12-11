package com.example.shalantor.connect4;

/*This asynchronous task is used for connecting to the server*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectToServerTask extends AsyncTask<String, Void, String>{

    private Socket socket;
    private String address;
    private int port;
    private Activity activity;

    public ConnectToServerTask(String address,int port,Activity activity){
        this.address = address;
        this.port = port;
        this.activity = activity;
    }

    /*Get socket reference*/
    public Socket getSocket(){
        return socket;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String response = "";
        try{
            socket = new Socket(address,port);

            /*Now read from socket to validate connection*/
            socket.setSoTimeout(5000);
            BufferedReader inputStream = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            /*Return result corresponding to server answer*/
            try {
                response = inputStream.readLine();
                Log.wtf("RESPONSE","Response is " + response);
                if (response.equals("0")){
                    return AccountManagementUtils.OK_CONNECTION;
                }
                else{
                    return AccountManagementUtils.NO_BIND;
                }
            }
            catch(SocketTimeoutException ex){
                return AccountManagementUtils.SOCKET_TIMEOUT;
            }

        }
        catch(IOException ex){
            return AccountManagementUtils.IOEXCEPTION ;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

    }
}
