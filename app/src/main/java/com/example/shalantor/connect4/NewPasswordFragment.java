package com.example.shalantor.connect4;

/*Shows when user enters a new password */

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class NewPasswordFragment extends Fragment{

    private Activity activity;
    private Socket connectSocket;
    private NewPasswordFragment.newPasswordFragmentCallback mCallback;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment*/
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        view =  inflater.inflate(R.layout.new_password_fragment, container, false);
        return view;
    }

    /*Interface for communication with activity*/
    public interface newPasswordFragmentCallback{
        void replaceNewPasswordFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /* This makes sure that the container activity has implemented
         * the callback interface. If not, it throws an exception*/
        try {
            mCallback = (NewPasswordFragment.newPasswordFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement replace new password call back interface");
        }
    }



    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        final EditText newPassword = (EditText) activity.findViewById(R.id.reset_password);
        final EditText newPasswordConfirm = (EditText) activity.findViewById(R.id.reset_password_verify);
        Button submitButton = (Button) activity.findViewById(R.id.reset_submit_button);
        final TextView textView = (TextView) activity.findViewById(R.id.reset_error_messages);

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

                if(result.equals(AccountManagementUtils.OK)) {
                    /*Now replace fragment*/
                    mCallback.replaceNewPasswordFragment();
                }
                else if (result.equals(AccountManagementUtils.NEW_PASSWORD_ERROR_MESSAGE)){
                    textView.setText(AccountManagementUtils.ALREADY_IN_USE_MESSAGE, TextView.BufferType.NORMAL);
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

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

}
