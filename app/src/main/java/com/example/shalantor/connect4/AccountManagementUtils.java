package com.example.shalantor.connect4;

/*This class will provide al the constants and methods which will be used
  for the account management of the user. It provides everything from constants
  about screen ratios for text, to network services for connecting to the server*/

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountManagementUtils {

    /*Return values for the async tasks used for connecting to server*/
    public static final String OK = "Success";
    public static final String ERROR = "Error";
    public static final String OK_LOGIN = "Login successful";               //Successful login
    public static final String OK_REGISTER = "Register successful";         //Successful register
    public static final String OK_CODE_GENERATION = "Code available";       //Code generated successfully on server
    public static final String OK_CODE_VERIFY = "Code verified";            //User entered right code
    public static final String OK_PASSWORD_RESET = "Password reset ok";     //Password reset ok
    public static final String OK_FB_REGISTER = "Fb register ok";           //Successful facebook registry
    public static final String OK_FB_LOGIN = "Fb login ok";                 //Successful facebook login on server
    public static final String NO_BIND = "No connection to server";         //Network error
    public static final String SOCKET_TIMEOUT = "timeout";                  //Connection timed out
    public static final String IOEXCEPTION = "IoException";                 //IoException occurred with socket
    public static final String NO_SUCH_USER = "User not found";             //Wrong authentication info
    public static final String NO_SUCH_FB_USER = "Fb user not found";       //Couldn't find facebook user
    public static final String CODE_NOT_GENERATED = "Code error";           //Server couldn't generate code for reseting password
    public static final String CODE_NOT_VALID = "Wrong";                    //User entered wrong reset code
    public static final String NEW_PASSWORD_ERROR = "Password reset error"; //New password couldn't be entered on server
    public static final String ALREADY_IN_USE = "Name/email already in use";//Couldn't register user

    /*Corresponding error messages to display for user*/
    public static final String OK_LOGIN_MESSAGE = "Login successful";
    public static final String OK_REGISTER_MESSAGE = "Account created successfully";
    public static final String OK_CODE_GENERATION_MESSAGE = "Code generated successfully on server";
    public static final String OK_CODE_VERIFY_MESSAGE = "Code is correct";
    public static final String OK_PASSWORD_RESET_MESSAGE = "Password was reset";
    public static final String OK_FB_REGISTER_MESSAGE = "Registered successfully";
    public static final String OK_FB_LOGIN_MESSAGE = "Login successful";
    public static final String NO_BIND_MESSAGE = "Couldn't connect to server, please check your connection";
    public static final String SOCKET_TIMEOUT_MESSAGE = "Couldn't reach server, please try again";
    public static final String IOEXCEPTION_MESSAGE = "Couldn't connect to server, please check your connection";
    public static final String NO_SUCH_USER_MESSAGE = "Wrong email or username";
    public static final String NO_SUCH_FB_USER_MESSAGE = "Wrong name, please login again with facebook";
    public static final String CODE_NOT_GENERATED_MESSAGE = "An unexpected error occurred, please try again";
    public static final String CODE_NOT_VALID_MESSAGE = "Wrong code, please enter again";
    public static final String NEW_PASSWORD_ERROR_MESSAGE = "Couldn't reset password, please try again";
    public static final String ALREADY_IN_USE_MESSAGE = "Username or email already in use";
    public static final String OK_CONNECTION = "Connected successfully to server";


    /*Constant for text to screen height ratio*/
    public static final int SCREEN_TO_TEXT_SIZE_RATIO = 20;

    /*ArrayList for components*/
    private static ArrayList<Integer> componentsIds;

    /*Method to adjust components size*/
    public static void adjustComponentsSize(ViewGroup view, Activity activity){

        /*List to store ids of components*/
        componentsIds = new ArrayList<>();

        /*Get screen dimensions*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;

        /*Get IDs of components*/
        getAllComponents(view);

        /*Adjust size*/
        for ( Integer id : componentsIds){
            View v = activity.findViewById(id);
            if ( v instanceof Button){
                Button button = (Button) v;
                button.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
            }
            else if(v instanceof EditText){
                EditText editText = (EditText) v;
                editText.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
            }
            else if(v instanceof TextView){
                TextView textView =  (TextView) v;
                textView.setTextSize(displayHeight / SCREEN_TO_TEXT_SIZE_RATIO);
            }
        }

    }

    /*Find all components in a view*/
    private static void getAllComponents(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            if(child instanceof ViewGroup)
                getAllComponents((ViewGroup)child);
            else
                componentsIds.add(child.getId());
        }
    }

}
