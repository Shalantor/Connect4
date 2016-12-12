package com.example.shalantor.connect4;

/*Fragment that displays the play button*/

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
import android.widget.TextView;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class PlayButtonFragment extends Fragment{

    private Activity activity;
    private Socket connectSocket;
    private View view;
    private PlayButtonFragment.goBackToStartFragment mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment*/
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();
        view =  inflater.inflate(R.layout.play_fragment, container, false);
        return view;
    }

    /*Interface to go back if player cancels match making*/
    public interface goBackToStartFragment{
        void goBackToAccountFragment();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /* This makes sure that the container activity has implemented
          the callback interface. If not, it throws an exception*/
        try {
            mCallback = (PlayButtonFragment.goBackToStartFragment) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement go back to start fragment interface");
        }
    }

    /*Get socket reference*/
    public void setSocket(Socket socket){
        connectSocket = socket;
    }

    /*Adjust size of components*/
    public void adjustButtons(){

        /*Get references*/
        Button playButton = (Button) activity.findViewById(R.id.play_button);
        final TextView textView = (TextView) activity.findViewById(R.id.play_error_messages);

        /*Adjust size of components*/
        AccountManagementUtils.adjustComponentsSize((ViewGroup) view,activity);

        /*Add listener to button*/
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /*create async task for network operation*/
                NetworkOperationsTask netTask = new NetworkOperationsTask(connectSocket,activity);

                String result = "";
                try {
                    result = netTask.execute("5").get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                /*Check result and act accordingly*/
                if(result.equals(AccountManagementUtils.OK)) {
                    textView.setText(AccountManagementUtils.OK_CONNECTION, TextView.BufferType.NORMAL);

                    /*Wait for match*/

                    GameAsyncTask gameNetTask = new GameAsyncTask(connectSocket,activity,true);
                    gameNetTask.setOperation(1);
                    gameNetTask.setCallback(mCallback);

                    gameNetTask.execute("");

                }
                /*Else just inform user what went wrong*/
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
}
