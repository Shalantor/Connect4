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
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        view =  inflater.inflate(R.layout.play_fragment, container, false);
        return view;
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

        /*Adjust size of components*/
        AccountManagementUtils.adjustComponentsSize((ViewGroup) view,activity);

        /*Add listener to button*/
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /*create async task for network operation*/
                NetworkOperationsTask netTask = new NetworkOperationsTask(connectSocket,activity);

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


                if(result.equals(AccountManagementUtils.OK)) {
                    textView.setText(AccountManagementUtils.OK_CONNECTION, TextView.BufferType.NORMAL);
                }
                else if(result.equals(AccountManagementUtils.SOCKET_TIMEOUT)){
                    textView.setText(AccountManagementUtils.SOCKET_TIMEOUT_MESSAGE, TextView.BufferType.NORMAL);
                }
                else if(result.equals(AccountManagementUtils.NO_BIND)){
                    textView.setText(AccountManagementUtils.NO_BIND_MESSAGE, TextView.BufferType.NORMAL);
                }
                else {
                    textView.setText(AccountManagementUtils.IOEXCEPTION_MESSAGE, TextView.BufferType.NORMAL);
                }
            }
        });

    }
}
