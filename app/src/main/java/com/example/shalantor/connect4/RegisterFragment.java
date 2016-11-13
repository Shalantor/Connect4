package com.example.shalantor.connect4;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final String USER_TYPE = "USER_TYPE";
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    public void adjustButtons(){

        final Activity activity = getActivity();

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Get references to components*/
        final EditText usernamePrompt = (EditText) activity.findViewById(R.id.register_username);
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.register_email);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.register_password);
        final EditText passwordVerify = (EditText) activity.findViewById(R.id.register_password_verify);
        Button registerButton = (Button) activity.findViewById(R.id.register_button_final);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_register);
        CheckBox remember = (CheckBox) activity.findViewById(R.id.remember_me_register);

        /*Set text sizes*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordVerify.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        remember.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Add listener to register button*/
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Check if user entered anything in username prompt*/
                String usernameInput = usernamePrompt.getText().toString().trim();
                if (usernameInput.length() == 0){
                    String errorMessage = "Please enter a username";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if email is in right format */
                String emailInput = emailPrompt.getText().toString();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailInput);
                if (!matcher.find()){
                    String errorMessage = "Please enter a valid email address format";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if password prompts have the same password*/
                String password = passwordPrompt.getText().toString().trim();
                String verifyPassword = passwordVerify.getText().toString().trim();

                /*Check password length first*/
                if (password.length() < PASSWORD_MIN_LENGTH){
                    String errorMessage = "Password must contain at least 8 characters";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                if (!password.equals(verifyPassword)){
                    String errorMessage = "Passwords don't match";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*If user clicked remember me then save his credentials*/
                SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

                /*Store user data*/
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(USER_TYPE,0);
                editor.putString(USERNAME,usernameInput);
                editor.putString(EMAIL,emailInput);
                editor.putString(PASSWORD,password);
                editor.apply();

            }
        });

    }
}
