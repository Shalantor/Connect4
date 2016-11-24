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

public class NewPasswordFragment extends Fragment{

    private Activity activity;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    private Socket connectSocket;
    private NewPasswordFragment.newPasswordFragmentCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.new_password_fragment, container, false);
    }

    /*Interface for communication with activity*/
    public interface newPasswordFragmentCallback{
        void replaceNewPasswordFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (NewPasswordFragment.newPasswordFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement reset fragment call back interface");
        }
    }



    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        final EditText newPassword = (EditText) activity.findViewById(R.id.reset_password);
        final EditText newPasswordConfirm = (EditText) activity.findViewById(R.id.reset_password_verify);
        Button submitButton = (Button) activity.findViewById(R.id.reset_submit_button);
        final TextView textView = (TextView) activity.findViewById(R.id.reset_error_messages);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Set sizes*/
        newPassword.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        newPasswordConfirm.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        submitButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        textView.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Set listener for button*/
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Get text from edit texts and compare them*/
                String password = newPassword.getText().toString().trim();
                String verifyPassword = newPasswordConfirm.getText().toString().trim();

                /*Check for empty fields*/
                if (password.length() == 0){
                    String message = "Please enter a password";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check for password length*/
                if (password.length() < 8){
                    String message = "Password must contain at least 8 characters";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check for matching passwords*/
                if ( !password.equals(verifyPassword)){
                    String message = "Passwords don't match";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*create async task for network operation*/
                NetworkOperationsTask netTask = new NetworkOperationsTask(connectSocket,activity);

                String result = "";
                try {
                    result = netTask.execute("2",password).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                /*Check result*/
                String message = "";

                if (result.equals("success")){
                    mCallback.replaceNewPasswordFragment();
                }
                else if (result.equals("Invalid password")){
                    message = "An unexpected error occurred, please try again";
                }
                else if (result.equals("Out of range")){
                    message = "Couldn't reach server, please check your connection";
                }
                else if(result.equals("Socket fail")){
                    message = "Couldn't instantiate connection, please try again";
                }
                else{
                    message = "No socket, please follow the on screen instructions";
                }

                textView.setText(message, TextView.BufferType.NORMAL);

            }
        });

    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

}
