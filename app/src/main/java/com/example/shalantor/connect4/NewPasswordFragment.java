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

public class NewPasswordFragment extends Fragment{

    private Activity activity;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.new_password_fragment, container, false);
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        EditText newPassword = (EditText) activity.findViewById(R.id.reset_password);
        EditText newPasswordConfirm = (EditText) activity.findViewById(R.id.reset_password_verify);
        Button submitButton = (Button) activity.findViewById(R.id.reset_submit_button);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Set sizes*/
        newPassword.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        newPasswordConfirm.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        submitButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);



    }

}
