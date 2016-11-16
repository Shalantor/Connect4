package com.example.shalantor.connect4;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AccountFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    private CallbackManager callbackManager;
    private Activity activity;
    private String facebookId;
    private String username;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = getActivity();
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return inflater.inflate(R.layout.account_fragment, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }



    /*Method to adjust button/text size*/
    public void adjustButtons(){

        final Activity activity = getActivity();

        /*Get references to buttons and editText*/
        final Button loginButton = (Button) activity.findViewById(R.id.login_button);
        Button registerButton = (Button) activity.findViewById(R.id.register_button);
        LoginButton fbButton = (LoginButton) activity.findViewById(R.id.login_fb_button);
        EditText address = (EditText) activity.findViewById(R.id.ip_address);

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*set dimensions of components according to screen size*/
        loginButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        registerButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        fbButton.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
        address.setSelected(false);
        address.clearFocus();

        /*Set permissions of login button for facebook*/
        fbButton.setReadPermissions(Arrays.asList("public_profile","email"));
        fbButton.setFragment(this);

        /*Register callback for fb button*/
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        AccessToken token = loginResult.getAccessToken();

                        /*Asynchronous request task to get user data*/
                        GraphRequestAsyncTask request = GraphRequest.newMeRequest(token,new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user,GraphResponse graphResponse){
                                facebookId = user.optString("id");
                                username = user.optString("name");
                                email = user.optString("email");
                            }
                        }).executeAsync();

                        try{
                            request.get();
                        }
                        catch (InterruptedException ex){
                            Log.d("EX","Interrupted");
                        }
                        catch (ExecutionException ex){
                            Log.d("EX2","Execute exception");
                        }

                        String message = "LOGIN OK " + facebookId + username + email;
                        textView.setText(message, TextView.BufferType.NORMAL);
                    }

                    @Override
                    public void onCancel() {
                        TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        String message = "LOGIN Cancelled";
                        textView.setText(message, TextView.BufferType.NORMAL);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        String message = "LOGIN error";
                        textView.setText(message, TextView.BufferType.NORMAL);
                    }
                });

    }

}
