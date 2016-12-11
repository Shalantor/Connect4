package com.example.shalantor.connect4;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/*This Fragment displays the initial login screen, where
* the user can choose how he wants to proceed with the
* authentication from the server, to start a new game*/

public class AccountFragment extends Fragment{

    /*Constants for shared preferences*/
    public static final String USER_TYPE = "USER_TYPE";
    public static final String FB_USERNAME = "FB_USERNAME";
    public static final String FACEBOOK_ID = "FB_ID";
    public static final String SERVER_ADDRESS = "SERVER";

    /*Used for getting the result from the facebook button*/
    private CallbackManager callbackManager;

    /*Reference to parent activity*/
    private Activity activity;

    /*User data from facebook*/
    private String facebookId;
    private String username;
    private String email;

    private Socket connectSocket;

    /*Callback to activity*/
    public AccountFragment.setSocket mCallback;

    /*Reference to parent view*/
    private View view;


    /*Interface to communicate with fragment*/
    public interface setSocket{
        Socket getSocketReference();
        void setSocketReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Inflate the layout for this fragment and initialize facebook sdk*/
        activity = getActivity();
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        /*Set portrait mode*/
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        view = inflater.inflate(R.layout.account_fragment, container, false);

        return view;
    }

    /*Used for getting data after facebook login*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /* This makes sure that the container activity has implemented
         the callback interface. If not, it throws an exception*/
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
        LoginButton fbButton = (LoginButton) activity.findViewById(R.id.login_fb_button);
        EditText address = (EditText) activity.findViewById(R.id.ip_address);
        Button continueButton = (Button) activity.findViewById(R.id.continue_button);

        /*Adjust components size*/
        AccountManagementUtils.adjustComponentsSize((ViewGroup) view,activity);

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

        /*Set visibility of facebook continue button. It should only
        * show up if the user has already logged in with facebook before*/
        String oldUsername = preferences.getString(FB_USERNAME,null);
        if(oldUsername != null){
            continueButton.setVisibility(View.VISIBLE);
        }


        /*Register callback for fb button*/
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        /*Get reference for textview, to display result*/
                        final TextView textView = (TextView) activity.findViewById(R.id.error_messages);

                        /*User token returned by the facebook sdk*/
                        AccessToken resultToken = loginResult.getAccessToken();

                        /*Asynchronous request task to get user data*/
                        GraphRequestAsyncTask request = GraphRequest.newMeRequest(resultToken,new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject user,GraphResponse graphResponse){

                                /*User info*/
                                facebookId = user.optString("id");
                                username = user.optString("name");
                                email = user.optString("email");

                                /*Store data in shared preferences*/
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(USER_TYPE,1);
                                editor.putString(FB_USERNAME,username);
                                editor.putString(FACEBOOK_ID,facebookId);
                                editor.apply();

                                /*Now connect to server and authenticate*/
                                mCallback.setSocketReference();
                                connectSocket = mCallback.getSocketReference();

                                /*Create async task*/
                                NetworkOperationsTask loginfb = new NetworkOperationsTask(connectSocket,activity);
                                String result = "";

                                /*Wait for completion*/
                                try {
                                    result = loginfb.execute("0","1",facebookId,username,"0","0").get();
                                }
                                catch(ExecutionException ex){
                                    Log.d("EXECUTION","Executionexception occured");
                                }
                                catch(InterruptedException ex){
                                    Log.d("INTERRUPT","Interrupted exception occured");
                                }

                                /*Set result of operation to textview to inform user*/
                                if (result.equals(AccountManagementUtils.OK)){
                                    textView.setText(AccountManagementUtils.OK_FB_REGISTER, TextView.BufferType.NORMAL);
                                }
                                else if (result.equals(AccountManagementUtils.ERROR)){
                                    textView.setText(AccountManagementUtils.NO_SUCH_FB_USER_MESSAGE, TextView.BufferType.NORMAL);
                                }
                                else if (result.equals(AccountManagementUtils.SOCKET_TIMEOUT)){
                                    textView.setText(AccountManagementUtils.SOCKET_TIMEOUT_MESSAGE);
                                }
                                else if (result.equals(AccountManagementUtils.IOEXCEPTION)){
                                    textView.setText(AccountManagementUtils.IOEXCEPTION_MESSAGE);
                                }
                                else{
                                    textView.setText(AccountManagementUtils.NO_BIND_MESSAGE, TextView.BufferType.NORMAL);
                                }
                            }
                        }).executeAsync();

                    }

                    /*Case login is cancelled, just show a message*/
                    @Override
                    public void onCancel() {
                        TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        String message = "LOGIN Cancelled";
                        textView.setText(message, TextView.BufferType.NORMAL);
                    }

                    /*Case facebook login ended with an error , just show a message*/
                    @Override
                    public void onError(FacebookException exception) {
                        TextView textView = (TextView) activity.findViewById(R.id.error_messages);
                        String message = "LOGIN error";
                        textView.setText(message, TextView.BufferType.NORMAL);
                    }
                });

    }


}
