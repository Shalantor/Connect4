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

import java.net.Socket;

public class PlayButtonFragment extends Fragment{

    private Activity activity;
    private Socket connectSocket;
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        return inflater.inflate(R.layout.play_fragment, container, false);
    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        Button playButton = (Button) activity.findViewById(R.id.play_button);

        /*Get screen size*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Adjust text size*/
        playButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        /*Add listener to button*/
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
