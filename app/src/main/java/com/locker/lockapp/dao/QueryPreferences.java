package com.locker.lockapp.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.locker.lockapp.R;
import com.locker.lockapp.toolbox.Logs;

import java.util.Map;

/**
 * Created by sredorta on 1/12/2017.
 */
public class QueryPreferences  extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREFERENCE_USER_ACCOUNT_NAME         = "account.name";
    public static final String PREFERENCE_USER_ACCOUNT_TYPE         = "account.type";
    public static final String PREFERENCE_USER_ACCOUNT_AUTH_TYPE    = "account.auth_type";
    public static final String PREFERENCE_USER_FIRST_NAME           = "account.first_name";
    public static final String PREFERENCE_USER_LAST_NAME            = "account.last_name";
    public static final String PREFERENCE_USER_EMAIL                = "account.email";
    public static final String PREFERENCE_USER_PHONE                = "account.phone";
    public static final String PREFERENCE_ACCOUNT_DEBUG_LOGCAT      = "debug.logs";
    public static final String PREFERENCE_ACCOUNT_DEBUG_SERVER      = "debug.server";
    private SharedPreferences sharedPrefs;



    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Logs.i("onCreate");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //Register the listener for the preferences changed
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    //Listener of the changes
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Logs.i("The following preference was changed :" + key);

        //Change the host depending on PREFERENCE
        if (key.equals(PREFERENCE_ACCOUNT_DEBUG_SERVER)) {
            boolean isDebug = sharedPrefs.getBoolean(PREFERENCE_ACCOUNT_DEBUG_SERVER, false);
            if (isDebug)
                CloudFetchr.URI_BASE = CloudFetchr.URI_BASE_DEBUG;
            else
                CloudFetchr.URI_BASE = CloudFetchr.URI_BASE_PROD;
            Logs.i("Set URI_BASE to :" + CloudFetchr.URI_BASE);
        }

        printAllPreferences();

    }

    //Here we set the value of all PREFERENCE_* Variables to the variable name
    private void printAllPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


        Map<String,?> keys = sharedPrefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Logs.i("---->   " + entry.getKey() + ": " + entry.getValue().toString());
        }
    }




    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.account_prefs);
        }
    }






    // To set a preference programatically
    public static void setPreference(Context context, String preference, String value) {
        Logs.i( "Stored into preferences: " + preference + " : " + value, QueryPreferences.class);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(preference, value)
                .apply();
    }

    //To get a preference programatically
    public static String getPreference(Context context, String preference) {
        Logs.i("Preference queried : " + preference + " : " +  PreferenceManager.getDefaultSharedPreferences(context).getString(preference, null), QueryPreferences.class);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(preference, null);
    }

}
