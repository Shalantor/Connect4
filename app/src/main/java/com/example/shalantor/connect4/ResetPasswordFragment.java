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

public class ResetPasswordFragment extends Fragment{

    private Activity activity;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.reset_password_fragment, container, false);
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        EditText emailPrompt = (EditText) activity.findViewById(R.id.reset_mail);
        EditText resetCodePrompt = (EditText) activity.findViewById(R.id.reset_code);
        Button sendCode = (Button) activity.findViewById(R.id.send_code);
        Button submitCode = (Button) activity.findViewById(R.id.submit_code);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Change size of embedded text*/
        emailPrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        resetCodePrompt.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        sendCode.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        submitCode.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

    }

}
