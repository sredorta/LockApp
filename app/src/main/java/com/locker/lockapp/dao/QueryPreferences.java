package com.locker.lockapp.dao;

import android.content.Context;
import android.preference.PreferenceManager;

import com.locker.lockapp.toolbox.Logs;

/**
 * Created by sredorta on 1/12/2017.
 */
public class QueryPreferences {

    public static final String PREFERENCE_USER_ACCOUNT_NAME = "account_name";
    public static final String PREFERENCE_USER_ACCOUNT_TYPE = "account_type";
    public static final String PREFERENCE_USER_ACCOUNT_AUTH_TYPE = "account_auth_type";
    public static final String PREFERENCE_USER_FIRST_NAME = "account_first_name";
    public static final String PREFERENCE_USER_LAST_NAME = "account_last_name";
    public static final String PREFERENCE_USER_EMAIL = "account_email";
    public static final String PREFERENCE_USER_PHONE = "account_phone";


    public static void setPreference(Context context, String preference, String value) {
        Logs.i( "Stored into preferences: " + preference + " : " + value, QueryPreferences.class);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(preference, value)
                .apply();
    }

    public static String getPreference(Context context, String preference) {
        Logs.i("Preference queried : " + preference + " : " +  PreferenceManager.getDefaultSharedPreferences(context).getString(preference, null), QueryPreferences.class);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(preference, null);
    }

}
