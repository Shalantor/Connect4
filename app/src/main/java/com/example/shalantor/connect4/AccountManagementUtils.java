package com.example.shalantor.connect4;

/*This class will provide al the constants and methods which will be used
  for the account management of the user. It provides constants for network services for connecting to the server*/

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
    public static final String OK_FB_REGISTER = "Fb register ok";           //Successful facebook registry
    public static final String NO_BIND = "No connection to server";         //Network error
    public static final String SOCKET_TIMEOUT = "timeout";                  //Connection timed out
    public static final String IOEXCEPTION = "IoException";                 //IoException occurred with socket

    /*Corresponding error messages to display for user*/
    public static final String OK_CODE_GENERATION_MESSAGE = "Code generated successfully on server";
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

}
