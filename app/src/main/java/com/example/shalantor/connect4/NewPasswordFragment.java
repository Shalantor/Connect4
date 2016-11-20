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

                /*Check for matching passwords*/
                if ( !password.equals(verifyPassword)){
                    String message = "Passwords don't match";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                /*create async task for network operation*/
                sendNewPassword netTask = new sendNewPassword();

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

                /*Now if successfull go back to login*/
                mCallback.replaceNewPasswordFragment();
            }
        });

    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

    /*AsyncTask for network operations*/
    private class sendNewPassword extends AsyncTask<String, Void, String> {
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
