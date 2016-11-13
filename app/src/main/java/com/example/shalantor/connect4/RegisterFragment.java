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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    public void adjustButtons(){

        Activity activity = getActivity();

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Get references to components*/
        EditText usernamePrompt = (EditText) activity.findViewById(R.id.register_username);
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.register_email);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.register_password);
        final EditText passwordVerify = (EditText) activity.findViewById(R.id.register_password_verify);
        Button registerButton = (Button) activity.findViewById(R.id.register_button_final);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_register);

        /*Set text sizes*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordVerify.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Add listener to register button*/
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Check if email is in right format */
                String emailInput = emailPrompt.getText().toString();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailInput);
                if (!matcher.find()){
                    String errorMessage = "Please enter a valid email address format";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                }

                /*Check if password prompts have the same password*/
                String password = passwordPrompt.getText().toString();
                String verifyPassword = passwordVerify.getText().toString();

                if (!password.equals(verifyPassword)){
                    String errorMessage = "Passwords don't match";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                }

            }
        });

    }
}
