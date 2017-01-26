package com.locker.lockapp.toolbox;

import android.util.Log;

import com.locker.lockapp.activity.MainActivity;
import com.locker.lockapp.authentication.AuthenticatorActivity;
import com.locker.lockapp.authentication.LockerAuthenticator;
import com.locker.lockapp.authentication.SignInFragment;
import com.locker.lockapp.authentication.SignUpFragment;
import com.locker.lockapp.dao.CloudFetchr;
import com.locker.lockapp.dao.JsonItem;
import com.locker.lockapp.dao.QueryPreferences;

import java.util.HashMap;
import java.util.Map;


/**
 * CLASS :      Logs
 * DESCRIPTION:
 *      This class is a way to centralize the logging
 *      Use Logs.i instead of Log.i in order to use this
 *      To disable Logs of a specific Class, just add it in
 *      the settings HashTable with the false value
 *
 *      If no Class is given to Logs.i then the message is always output
 *      The tag argument is optional
 */

public class Logs {
    private static final String mAppName = "SERGI";     // Used to set the first common part of all logs

    //Defines for each class if we want logs or not
    private static HashMap<Class,Boolean> defineLog() {
        final HashMap<Class, Boolean> settings = new HashMap<>();

        //  Define here all the Classes where you want to be able to enable/disable Logs
        ///////////////////////////////////////////////////////////////////////////////////
        //  Start of Table
        ///////////////////////////////////////////////////////////////////////////////////
        settings.put(MainActivity.class,                    true );
        settings.put(LockerAuthenticator.class,             true );
        settings.put(CloudFetchr.class,                     true );
        settings.put(JsonItem.class,                        true );
        settings.put(QueryPreferences.class,                true );
        settings.put(AuthenticatorActivity.class,           true );
        settings.put(Toolbox.class,                         true );
        settings.put(SignUpFragment.class,                  true );
        settings.put(SignInFragment.class,                  true );
        //settings.put(.class,            true );


        ///////////////////////////////////////////////////////////////////////////////////
        //  End of Table
        ///////////////////////////////////////////////////////////////////////////////////

        return settings;
    }

    //Constructors
    private Logs() {}

    public static int i(String message,Class myClass) {
       String className =  myClass.getSimpleName();
       Logs.defineLog();
        if (Logs.getLogStatus(Logs.defineLog(),className)) {
            String tag = mAppName + "::" + className + ":";
            return Log.i(tag, message);
        } else
            return 0;
    }

    public static int i(String tag, String message,Class myClass) {
        String className =  myClass.getSimpleName();
        Logs.defineLog();
        if (Logs.getLogStatus(Logs.defineLog(),className)) {
            tag = mAppName + "::" + className + "::" + tag + ":";
            return Log.i(tag, message);
        } else
            return 0;
    }

    //In case no class is given, then always Log
    public static int i(String message) {
        String tag = mAppName + ":";
        return Log.i(tag,message);
    }

    public static int i(String tag, String message) {
        tag = mAppName + "::" + tag + ":";
        return Log.i(tag,message);
    }


    //Defines for each class if we want logs or not
    private static boolean getLogStatus(HashMap<Class, Boolean> settings,String className) {
        for(Map.Entry<Class, Boolean> entry : settings.entrySet()){
            if (entry.getKey().getSimpleName().equals(className))
                return entry.getValue();
        }
        //If we could not find the class then log it anyway
        return true;
    }

}
