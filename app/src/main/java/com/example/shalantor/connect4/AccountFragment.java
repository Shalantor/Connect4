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
import android.widget.EditText;

public class AccountFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return inflater.inflate(R.layout.account_fragment, container, false);
    }

    /*Method to adjust button/text size*/
    public void adjustButtons(){

        Activity activity = getActivity();

        /*Get references to buttons and editText*/
        Button continueButton = (Button) activity.findViewById(R.id.continue_button);
        Button loginButton = (Button) activity.findViewById(R.id.login_button);
        Button registerButton = (Button) activity.findViewById(R.id.register_button);
        Button fbButton = (Button) activity.findViewById(R.id.login_fb_button);
        EditText address = (EditText) activity.findViewById(R.id.ip_address);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*set dimensions of components according to screen size*/
        continueButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        loginButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        fbButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setSelected(false);
        address.clearFocus();

        /*If user is new , disable the current account button*/
        /*This will be checked with the sharedPreferences*/
        SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

        int userType = preferences.getInt(USER_TYPE,-1);

        /*No user*/
        if (userType == -1){
            continueButton.setEnabled(false);
        }

    }

}
