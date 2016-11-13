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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    /*Method to adjust button size and text*/
    public void adjustButtons(){
        Activity activity = getActivity();

        /*Get references to GUI objects*/
        EditText usernamePrompt = (EditText) activity.findViewById(R.id.username);
        EditText passwordPrompt = (EditText) activity.findViewById(R.id.password);
        TextView forgotPassword = (TextView) activity.findViewById(R.id.forgot_password);
        CheckBox rememberMe = (CheckBox) activity.findViewById(R.id.remember_me);
        Button loginButton = (Button) activity.findViewById(R.id.login);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*SET SIZES*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        forgotPassword.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        rememberMe.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        loginButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

    }

}
