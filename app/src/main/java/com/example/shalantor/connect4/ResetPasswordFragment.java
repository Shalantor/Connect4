package com.example.shalantor.connect4;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
    private Socket connectSocket;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public ResetPasswordFragment.resetFragmentCallback mCallback;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        view = inflater.inflate(R.layout.reset_password_fragment, container, false);
        return view;
    }

    /*Interface to communicate with login activity*/
    public interface resetFragmentCallback{
        void setNewPasswordFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ResetPasswordFragment.resetFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement reset fragment call back interface");
        }
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.reset_mail);
        final EditText resetCodePrompt = (EditText) activity.findViewById(R.id.reset_code);
        Button sendCode = (Button) activity.findViewById(R.id.send_code);
        Button submitCode = (Button) activity.findViewById(R.id.submit_code);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_reset);

        /*Adjust size of components*/
        AccountManagementUtils.adjustComponentsSize((ViewGroup) view,activity);

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
                NetworkOperationsTask netTask = new NetworkOperationsTask(connectSocket,activity);

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

                /*Check format of input*/
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(input);
                if (!matcher.find()){
                    name = input;
                    input = "0";
                }

                /*Read code*/
                String code = resetCodePrompt.getText().toString().trim();


                /*create async task for network operation*/
                NetworkOperationsTask netTask = new NetworkOperationsTask(connectSocket,activity);

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
                mCallback.setNewPasswordFragment();
            }
        });


    }

    /*Get socket reference from main activity*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

}
