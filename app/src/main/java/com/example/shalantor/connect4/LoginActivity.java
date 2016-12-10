package com.example.shalantor.connect4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import com.facebook.CallbackManager;

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

    public static final int PORT = 1337;
    private AccountFragment accFragment = null;
    private LoginFragment logFragment = null;
    private RegisterFragment regFragment = null;
    private ResetPasswordFragment resetFragment = null;
    private NewPasswordFragment newPassFragment = null;
    private PlayButtonFragment playButtonFragment = null;

    private Socket connectSocket;
    public String address;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        boolean showPlay = intent.getBooleanExtra("PLAY",false);

        if(!showPlay) {
            accFragment = new AccountFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, accFragment, ACCOUNT_FRAGMENT).commit();
        }
        else{
            playButtonFragment = new PlayButtonFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, playButtonFragment, PLAY_BUTTON_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
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
        newPassFragment = new NewPasswordFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,newPassFragment,NEW_PASSWORD_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        newPassFragment.adjustButtons();
        newPassFragment.setSocket(connectSocket);
        accFragment = null;
        regFragment = null;
        resetFragment = null;
        logFragment = null;

    }

    /*Implement interface for new password fragment*/
    @Override
    public void replaceNewPasswordFragment(){
        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        logFragment.adjustButtons();
        logFragment.setConnectSocket(connectSocket);

    }

    /*Implements interface for login fragment*/
    @Override
    public void replaceLoginWithPlayFragment(){
        playButtonFragment = new PlayButtonFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,playButtonFragment,PLAY_BUTTON_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        playButtonFragment.setSocket(connectSocket);
        playButtonFragment.adjustButtons();
    }

    /*Implements interface for register fragment*/
    @Override
    public void replaceRegisterFragment(){
        logFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,logFragment,LOGIN_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        logFragment.setConnectSocket(connectSocket);
        logFragment.adjustButtons();
    }

    /*Implements interface for play button fragment*/
    @Override
    public void goBackToAccountFragment(){
        accFragment = new AccountFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container,accFragment,ACCOUNT_FRAGMENT).commit();
        getSupportFragmentManager().executePendingTransactions();
        accFragment.adjustButtons();
        connectSocket = null;
    }

    @Override
    protected void onStart(){
        super.onStart();
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

    /*On click functions for all buttons*/

    /*Go to login fragment button*/
    public void goToLogin(View view){

        boolean result = true;
        if (connectSocket == null){
            result = isAddressCorrect();
        }

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
        if(connectSocket == null){
            result = isAddressCorrect();
        }

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
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        if(result){
                    /*Read send data from preferences*/
            SharedPreferences preferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
            String username = preferences.getString(FB_USERNAME,null);
            String facebookID = preferences.getString(FACEBOOK_ID,null);

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
        if(connectSocket == null){
            result = isAddressCorrect();
        }

        if (result){
            resetFragment = new ResetPasswordFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,resetFragment,RESET_PASSWORD_FRAGMENT).commit();
            getSupportFragmentManager().executePendingTransactions();
            resetFragment.adjustButtons();
            resetFragment.setSocket(connectSocket);
        }

    }

}
