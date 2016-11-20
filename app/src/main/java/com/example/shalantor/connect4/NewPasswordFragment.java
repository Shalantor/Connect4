package com.example.shalantor.connect4;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Socket;

public class NewPasswordFragment extends Fragment{

    private Activity activity;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    private Socket connectSocket;

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

                if (password.length() == 0){
                    String message = "Please enter a password";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

                if ( !password.equals(verifyPassword)){
                    String message = "Passwords don't match";
                    textView.setText(message, TextView.BufferType.NORMAL);
                    return;
                }

            }
        });

    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

}
