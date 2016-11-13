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

public class RegisterFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

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
        EditText emailPrompt = (EditText) activity.findViewById(R.id.register_email);
        EditText passwordPrompt = (EditText) activity.findViewById(R.id.register_password);
        EditText passwordVerify = (EditText) activity.findViewById(R.id.register_password_verify);
        Button registerButton = (Button) activity.findViewById(R.id.register_button_final);

        /*Set text sizes*/
        usernamePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        passwordVerify.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

    }
}
