package com.example.shalantor.connect4;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements AccountFragment.setSocket,
                                                                ResetPasswordFragment.resetFragmentCallback,
                                                                NewPasswordFragment.newPasswordFragmentCallback,
                                                                LoginFragment.loginCallback,
                                                                RegisterFragment.registerFragmentCallback,
                                                                PlayButtonFragment.goBackToStartFragment{

    /*String tags for sharedpreferences that store user data*/
    public static final String USER_TYPE = "USER_TYPE";
    public static final String FB_USERNAME = "FB_USERNAME";
    public static final String FACEBOOK_ID = "FB_ID";
    public static final String SERVER_ADDRESS = "SERVER";

    /*String tags for fragment identification*/
    public static final String ACCOUNT_FRAGMENT = "ACCOUNT_FRAGMENT";
    public static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";
    public static final String REGISTER_FRAGMENT = "REGISTER_FRAGMENT";
    public static final String RESET_PASSWORD_FRAGMENT = "RESET_PASSWORD_FRAGMENT";
    public static final String NEW_PASSWORD_FRAGMENT = "NEW_PASSWORD_FRAGMENT";
    public static final String PLAY_BUTTON_FRAGMENT = "PLAY_BUTTON_FRAGMENT";

    /*Server port and fragments*/
    public static final int PORT = 1337;
    private AccountFragment accFragment = null;
    private LoginFragment logFragment = null;
    private RegisterFragment regFragment = null;
    private ResetPasswordFragment resetFragment = null;
    private NewPasswordFragment newPassFragment = null;
    private PlayButtonFragment playButtonFragment = null;

    /*Socket and server address*/
    private Socket connectSocket;
    public String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Set layout to show*/
        setContentView(R.layout.activity_login);

        /*Get intent info about which fragment to display*/
        Intent intent = getIntent();
        boolean showPlay = intent.getBooleanExtra("PLAY",false);

        if(!showPlay) {
            /*Show start login menu*/
            accFragment = new AccountFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, accFragment, ACCOUNT_FRAGMENT).commit();
        }
        else{
            /*Show play button menu, this is the case when user just finished a game
            * and wants to play another one without logging in again*/
            playButtonFragment = new PlayButtonFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, playButtonFragment, PLAY_BUTTON_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();

            /*Set socket reference*/
            playButtonFragment.setSocket(GameUtils.getSocket());
        }

    }

    /*Implement interface for account fragment*/
    @Override
    public Socket getSocketReference(){
        return connectSocket;
    }


    @Override
    public void setSocketReference(){
        boolean result = false;
        if (connectSocket == null){
            result = isAddressCorrect();
        }
    }

    /*Implement interface for reset password fragment*/
    @Override
    public void setNewPasswordFragment(){
        /*Display fragment*/
        newPassFragment = new NewPasswordFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,newPassFragment,NEW_PASSWORD_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();

        /*Set this fragment's buttons and socket and nullify other fragments*/
        newPassFragment.adjustButtons();
        newPassFragment.setSocket(connectSocket);

    }

    /*Implement interface for new password fragment*/
    @Override
    public void replaceNewPasswordFragment(){

        /*Display this fragment*/
        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();

        /*Set button sizes and socket reference*/
        logFragment.adjustButtons();
        logFragment.setConnectSocket(connectSocket);

    }

    /*Implements interface for login fragment*/
    @Override
    public void replaceLoginWithPlayFragment(){

        /*Display fragment*/
        playButtonFragment = new PlayButtonFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,playButtonFragment,PLAY_BUTTON_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();

        /*Set button sizes and socket reference*/
        playButtonFragment.setSocket(connectSocket);
        playButtonFragment.adjustButtons();
    }

    /*Implements interface for register fragment*/
    @Override
    public void replaceRegisterFragment(){
        /*Display fragment*/
        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();

        /*Set button sizes and socket reference*/
        logFragment.setConnectSocket(connectSocket);
        logFragment.adjustButtons();
    }

    /*Implements interface for play button fragment*/
    @Override
    public void goBackToAccountFragment(){

        /*Display fragment*/
        accFragment = new AccountFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,accFragment,ACCOUNT_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();

        /*Set button sizes*/
        accFragment.adjustButtons();
        connectSocket = null;
    }

    @Override
    protected void onStart(){
        super.onStart();
        /*Adjust buttons of fragment of right fragment*/
        if (accFragment != null) {
            accFragment.adjustButtons();
        }
        else if (playButtonFragment != null){
            playButtonFragment.adjustButtons();
        }
    }


    /*Method to check if server is listening*/
    private boolean isAddressCorrect(){

        /*First get text of edittext*/
        EditText addressText = (EditText) findViewById(R.id.ip_address);
        address = addressText.getText().toString();

        /*Store address in preferences*/
        SharedPreferences preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SERVER_ADDRESS,address);
        editor.apply();

        /*Show Dialog*/
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Connecting to server");


        String message= "Connecting to server";

        SpannableString ss2 =  new SpannableString(message);
        ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);

        pDialog.setMessage(ss2);

        pDialog.setCancelable(false);
        pDialog.show();

        /*Try connecting to server*/
        ConnectToServerTask connect = new ConnectToServerTask(address,PORT,this);
        String result="";
        try {
            result = connect.execute("").get();
        }
        catch(ExecutionException ex){
            Log.d("EXECUTION","Executionexception occured");
        }
        catch(InterruptedException ex){
            Log.d("INTERRUPT","Interrupted exception occured");
        }

        /*Dismiss dialog*/
        pDialog.dismiss();

        /*Get socket reference*/
        connectSocket = connect.getSocket();

        /*Set text of textview*/
        TextView textView = (TextView) findViewById(R.id.error_messages);

        if (result.equals(AccountManagementUtils.OK_CONNECTION)){
            textView.setText(AccountManagementUtils.OK_CONNECTION, TextView.BufferType.NORMAL);
        }
        else if (result.equals(AccountManagementUtils.NO_BIND)){
            textView.setText(AccountManagementUtils.NO_BIND_MESSAGE, TextView.BufferType.NORMAL);
        }
        else if (result.equals(AccountManagementUtils.SOCKET_TIMEOUT)){
            textView.setText(AccountManagementUtils.SOCKET_TIMEOUT_MESSAGE, TextView.BufferType.NORMAL);
        }
        else{
            textView.setText(AccountManagementUtils.IOEXCEPTION_MESSAGE, TextView.BufferType.NORMAL);
        }

        /*Return result value*/
        return result.equals(AccountManagementUtils.OK_CONNECTION);

    }

    /*Override on back button pressed method to start menu activity*/
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_BACK){
            /*If start fragment is visible, go back to menu activity*/
            accFragment = (AccountFragment) getSupportFragmentManager().findFragmentByTag(ACCOUNT_FRAGMENT);
            if (accFragment != null) {
                finish();
                if (connectSocket != null){
                    try{
                        connectSocket.close();
                    }
                    catch (IOException ex){
                        Log.d("EXCEPTION CLOSE","Close exception");
                    }
                }
                Intent intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            }
            /*Else just go back to previous fragment after checking which fragmetn is visible*/
            /*Display them and set their buttons size and text size and also set their
            * references to sockets*/
            else if (getSupportFragmentManager().findFragmentByTag(RESET_PASSWORD_FRAGMENT) != null){
                logFragment = new LoginFragment();
                accFragment = null;
                regFragment = null;
                resetFragment = null;
                newPassFragment = null;
                getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
                getSupportFragmentManager().executePendingTransactions();
                logFragment.adjustButtons();
                logFragment.setConnectSocket(connectSocket);
            }
            else if (getSupportFragmentManager().findFragmentByTag(NEW_PASSWORD_FRAGMENT) != null){
                resetFragment = new ResetPasswordFragment();
                accFragment = null;
                regFragment = null;
                logFragment = null;
                newPassFragment = null;
                getSupportFragmentManager().beginTransaction().replace(R.id.container,resetFragment,RESET_PASSWORD_FRAGMENT).commit();
                getSupportFragmentManager().executePendingTransactions();
                resetFragment.adjustButtons();
                resetFragment.setSocket(connectSocket);
            }
            else{
                accFragment = new AccountFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.container,accFragment,ACCOUNT_FRAGMENT).commit();
                getSupportFragmentManager().executePendingTransactions();
                accFragment.adjustButtons();
                logFragment = null;
                regFragment = null;
                resetFragment = null;
                newPassFragment = null;
            }
        }
        return false;
    }

    /*On click functions for all visible buttons*/

    /*Go to login fragment button*/
    public void goToLogin(View view){

        boolean result = true;

        /*If not connected, connect to server*/
        if (connectSocket == null){
            result = isAddressCorrect();
        }

        /*If result is ok , go to login fragment*/
        if(result){
            logFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
            logFragment.adjustButtons();
            logFragment.setConnectSocket(connectSocket);
            accFragment = null;
            regFragment = null;
        }

    }

    /*Go to registry fragment button*/
    public void goToRegister(View view){

        boolean result = true;

        /*If not connected, connect to server*/
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        /*If result is ok , go to register fragment*/
        if(result){
            /*Replace fragments*/
            regFragment = new RegisterFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,regFragment,REGISTER_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
            regFragment.adjustButtons();
            accFragment = null;
            logFragment = null;
            regFragment.setConnectSocket(connectSocket);
        }

    }

    /*Continue with facebookButton*/
    public void continueFacebook(View view){

        boolean result = true;

        /*If not connected, connect to server*/
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        /*If result is ok go to play button fragment*/
        if(result){

            /*Read send data from preferences*/
            SharedPreferences preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
            String username = preferences.getString(FB_USERNAME,null);
            String facebookID = preferences.getString(FACEBOOK_ID,null);

            /*Send server user credentials*/
            NetworkOperationsTask continueFB = new NetworkOperationsTask(connectSocket,this);
            String OpResult = "";
            try {
                OpResult = continueFB.execute("1","1",facebookID,username,"0","0").get();
            }
            catch(ExecutionException ex){
                Log.d("EXECUTION","Executionexception occured");
            }
            catch(InterruptedException ex){
                Log.d("INTERRUPT","Interrupted exception occured");
            }

            /*Now replace this with play fragment*/

            playButtonFragment = new PlayButtonFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,playButtonFragment,PLAY_BUTTON_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
            playButtonFragment.setSocket(connectSocket);
            playButtonFragment.adjustButtons();
        }

    }


    /*Listener for the reset password textview*/
    public void goToResetFragment(View view){

        boolean result = true;

        /*If not connected, connect to server*/
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        /*If result is ok go to reset password fragment*/
        if (result){
            resetFragment = new ResetPasswordFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,resetFragment,RESET_PASSWORD_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
            resetFragment.adjustButtons();
            resetFragment.setSocket(connectSocket);
        }

    }

}
