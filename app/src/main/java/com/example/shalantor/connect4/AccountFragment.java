package com.example.shalantor.connect4;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AccountFragment extends Fragment{

    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;
    public static final String USER_TYPE = "USER_TYPE";
    public static final String FB_USERNAME = "FB_USERNAME";
    public static final String FACEBOOK_ID = "FB_ID";
    private CallbackManager callbackManager;
    private Activity activity;
    private String facebookId;
    private String username;
    private String email;
    private Socket connectSocket;
    public AccountFragment.setSocket mCallback;
    public static final String SERVER_ADDRESS = "SERVER";


    /*Interface to communicate with fragment*/
    public interface setSocket{
        Socket getSocketReference();
        void setSocketReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity = getActivity();
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        return inflater.inflate(R.layout.account_fragment, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (AccountFragment.setSocket) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetSocket interface");
        }
    }



    /*Method to adjust button/text size*/
    public void adjustButtons(){

        final Activity activity = getActivity();

        /*Get references to buttons and editText*/
        final Button loginButton = (Button) activity.findViewById(R.id.login_button);
        Button registerButton = (Button) activity.findViewById(R.id.register_button);
        LoginButton fbButton = (LoginButton) activity.findViewById(R.id.login_fb_button);
        EditText address = (EditText) activity.findViewById(R.id.ip_address);
        Button continueButton = (Button) activity.findViewById(R.id.continue_button);

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
        continueButton.setTextSize( displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);

        address.setSelected(false);
        address.clearFocus();

        /*Set permissions of login button for facebook*/
        fbButton.setReadPermissions(Arrays.asList("public_profile","email"));
        fbButton.setFragment(this);

        /*Set text of server field*/
        final SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(),Context.MODE_PRIVATE);
        String serverAddress = preferences.getString(SERVER_ADDRESS,null);
        if(serverAddress != null){
            address.setText(serverAddress, TextView.BufferType.NORMAL);
        }

        /*Set visibility of facebook continue button*/
        String oldUsername = preferences.getString(FB_USERNAME,null);
        if(oldUsername != null){
            continueButton.setVisibility(View.VISIBLE);
        }


        /*Register callback for fb button*/
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        final TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        AccessToken resultToken = loginResult.getAccessToken();

                        /*Asynchronous request task to get user data*/
                        GraphRequestAsyncTask request = GraphRequest.newMeRequest(resultToken,new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user,GraphResponse graphResponse){
                                facebookId = user.optString("id");
                                username = user.optString("name");
                                email = user.optString("email");

                                /*Store data in shared preferences*/
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(USER_TYPE,1);
                                editor.putString(FB_USERNAME,username);
                                editor.putString(FACEBOOK_ID,facebookId);

                                editor.apply();

                                /*Now connect to server */
                                mCallback.setSocketReference();
                                connectSocket = mCallback.getSocketReference();

                                /*Create async task*/
                                LoginFB loginfb = new LoginFB();
                                String result = "";
                                try {
                                    result = loginfb.execute("0","1",facebookId,username,"0","0").get();
                                }
                                catch(ExecutionException ex){
                                    Log.d("EXECUTION","Executionexception occured");
                                }
                                catch(InterruptedException ex){
                                    Log.d("INTERRUPT","Interrupted exception occured");
                                }
                            }
                        }).executeAsync();


                        String message = "LOGIN OK ";
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


    /*Async task for connecting to server*/
    private class LoginFB extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Connecting to server");


            String message = "Connecting to server";

            SpannableString ss2 = new SpannableString(message);
            ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
            ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

            pDialog.setMessage(ss2);

            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = "";
            try {

                /*Set up tools for sending and reading from socket*/
                connectSocket.setSoTimeout(5000);
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
                PrintWriter outputStream = new PrintWriter(connectSocket.getOutputStream());

                String messageToSend = "";
                /*Construct message to send*/
                for (int i = 0; i < params.length; i++) {
                    messageToSend += params[i] + " ";
                }

                /*Now send message*/
                outputStream.print(messageToSend);
                outputStream.flush();

                /*Now read answer from socket*/

                try {
                    response = inputStream.readLine();
                    if (response.equals("0")) {
                        return "success";
                    }
                } catch (SocketTimeoutException ex) {
                    return "error";
                }

            } catch (IOException ex) {
                return "error";
            }

            return "success";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();

        }
    }

}
