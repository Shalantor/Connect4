package com.example.shalantor.connect4;

/*Shows the fragment where user can register a new account*/

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment{

    /*Regular expression to check the email validity*/
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /*Minimum password length*/
    public static final int PASSWORD_MIN_LENGTH = 8;

    /*Constants for shared preferences*/
    public static final String USER_TYPE = "USER_TYPE";
    public static final String USERNAME = "USERNAME";
    public static final String EMAIL = "EMAIL";

    public Activity activity;
    private Socket connectSocket;
    private View view;

    /*Interface to communicate with register fragment*/
    private RegisterFragment.registerFragmentCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment*/
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activity = getActivity();

        view = inflater.inflate(R.layout.register_fragment, container, false);
        return view;
    }

    public interface registerFragmentCallback{
        void replaceRegisterFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /* This makes sure that the container activity has implemented
          the callback interface. If not, it throws an exception*/
        try {
            mCallback = (RegisterFragment.registerFragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement register interface");
        }
    }

    public void adjustButtons(){

        /*Get references to components*/
        final EditText usernamePrompt = (EditText) activity.findViewById(R.id.register_username);
        final EditText emailPrompt = (EditText) activity.findViewById(R.id.register_email);
        final EditText passwordPrompt = (EditText) activity.findViewById(R.id.register_password);
        final EditText passwordVerify = (EditText) activity.findViewById(R.id.register_password_verify);
        Button registerButton = (Button) activity.findViewById(R.id.register_button_final);
        final TextView textView = (TextView) activity.findViewById(R.id.error_messages_register);
        final CheckBox remember = (CheckBox) activity.findViewById(R.id.remember_me_register);

        /*Add listener to register button*/
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Check if user entered anything in username prompt*/
                String usernameInput = usernamePrompt.getText().toString().trim();
                if (usernameInput.length() == 0){
                    String errorMessage = "Please enter a username";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if email is in right format */
                String emailInput = emailPrompt.getText().toString();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailInput);
                if (!matcher.find()){
                    String errorMessage = "Please enter a valid email address format";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*Check if password prompts have the same password*/
                String password = passwordPrompt.getText().toString().trim();
                String verifyPassword = passwordVerify.getText().toString().trim();

                /*Check password length first*/
                if (password.length() < PASSWORD_MIN_LENGTH){
                    String errorMessage = "Password must contain at least 8 characters";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                if (!password.equals(verifyPassword)){
                    String errorMessage = "Passwords don't match";
                    textView.setText(errorMessage, TextView.BufferType.NORMAL);
                    return;
                }

                /*If user clicked remember me then save his credentials*/
                if (remember.isChecked()) {

                    SharedPreferences preferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);

                    /*Store user data*/
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(USER_TYPE, 0);
                    editor.putString(USERNAME, usernameInput);
                    editor.putString(EMAIL, emailInput);
                    editor.apply();

                }

                /*Now send message to server and wait for answer*/
                NetworkOperationsTask register = new NetworkOperationsTask(connectSocket,activity);
                String result = "";
                try {
                    result = register.execute("0","0","0",usernameInput,emailInput,password).get();
                }
                catch(ExecutionException ex){
                    Log.d("EXECUTION","Executionexception occured");
                }
                catch(InterruptedException ex){
                    Log.d("INTERRUPT","Interrupted exception occured");
                }

                /*Check result*/

                if(result.equals(AccountManagementUtils.OK)) {
                    /*Now replace fragment*/
                    mCallback.replaceRegisterFragment();
                }
                else if (result.equals(AccountManagementUtils.ERROR)){
                    textView.setText(AccountManagementUtils.ALREADY_IN_USE_MESSAGE, TextView.BufferType.NORMAL);
                }
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

    /*Get socket from activity*/

    public void setConnectSocket(Socket socket){
        connectSocket = socket;
    }
}
