package com.example.shalantor.connect4;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

public class LoginFragment extends Fragment{

    public static final String USER_TYPE = "USER_TYPE";
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";
    public Activity activity;
    public Socket connectSocket;
    private LoginFragment.loginCallback mCallback;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        view = inflater.inflate(R.layout.login_fragment, container, false);
        return view;
    }

    /*Interface for callback*/
    public interface loginCallback{
        void replaceLoginWithPlayFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (LoginFragment.loginCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetSocket interface");
        }
    }
    

    /*Get socket from activity*/
    public void setConnectSocket(Socket socket){
        connectSocket = socket;
    }


    /*Method to adjust button size and text*/
    public void adjustButtons(){
        final Activity activity = getActivity();

        /*Adjust size of components*/
        AccountManagementUtils.adjustComponentsSize((ViewGroup) view,activity);

        /*Get references to GUI objects*/
        final EditText usernamePrompt = (EditText) activity.findViewById(R.id.username);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.password);
        final CheckBox rememberMe = (CheckBox) activity.findViewById(R.id.remember_me);
        Button loginButton = (Button) activity.findViewById(R.id.login);
        final TextView textView = (TextView) activity.findViewById(R.id.login_error_messages);

        /*Set texts if there are saved credentials*/
        SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

        if ( preferences.getInt(USER_TYPE,-1) == 0){
            usernamePrompt.setText(preferences.getString(USERNAME,null));
        }

        /*Now set on click listener for login button*/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Get texts from edit texts*/
                String username = usernamePrompt.getText().toString().trim();
                String password = passwordPrompt.getText().toString().trim();
                String email;

                /*Check if fields are empty*/
                if (username.length() == 0 || password.length() == 0){
                    String errorMessage = "Please enter a username and a password";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                }

                /*Find out if username is an email or not*/
                boolean isEmail;
                /*Remove @ character from username*/
                username = username.replaceAll("@","");

                if (username.contains("@")){
                    email = username;
                    username = "0";
                    isEmail = true;
                }
                else{
                    email = "0";
                    isEmail = false;
                }


                /*Check value of checkbox*/
                if(rememberMe.isChecked()){
                    SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

                    /*Store user data*/
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(USER_TYPE, 0);
                    if (!isEmail) {
                        editor.putString(USERNAME, username);
                    }
                    else{
                        editor.putString(EMAIL, email);
                    }
                    editor.apply();

                }

                /*Now send message to server and wait for answer*/
                NetworkOperationsTask login = new NetworkOperationsTask(connectSocket,activity);
                String result = "";
                try {
                    result = login.execute("1","0","0",username,email,password).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                /*Check result*/

                if(result.equals(AccountManagementUtils.OK)) {
                    /*Now replace fragment*/
                    mCallback.replaceLoginWithPlayFragment();

                }
                else if (result.equals(AccountManagementUtils.ERROR)){
                    textView.setText(AccountManagementUtils.NO_SUCH_USER_MESSAGE, TextView.BufferType.NORMAL);
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
