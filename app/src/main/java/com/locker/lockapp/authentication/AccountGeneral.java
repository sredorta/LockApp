package com.locker.lockapp.authentication;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

import com.locker.lockapp.dao.QueryPreferences;
import com.locker.lockapp.toolbox.Logs;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General settings of the Locker account
 */
public class AccountGeneral {
    /////////////////////////////// GLOBAL VARIABLES //////////////////////////////////////////////
    // Account type id
    public static final String ACCOUNT_TYPE = "com.locker.lockapp.auth_locker";

    //Auth token types
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to a Locker account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to a Locker account";

    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_ACCOUNT_AUTH_TYPE = "AUTH_TYPE";

    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_TOKEN_TYPE = "USER_AUTH_TYPE";
    public final static String PARAM_USER_ACCOUNT = "USER_ACCOUNT";
    public final static String PARAM_USER_EMAIL = "USER_EMAIL";
    public final static String PARAM_USER_PHONE = "USER_PHONE";
    public final static String PARAM_USER_FIRST_NAME = "USER_FIRST_NAME";
    public final static String PARAM_USER_LAST_NAME = "USER_LAST_NAME";
    public final static String ARG_FINAL_ACTION = "FINAL_ACTION";

    public static final ServerAuthenticate sServerAuthenticate = new LockerServerAuthenticate();
    /////////////////////////////// LOCAL VARIABLES //////////////////////////////////////////////
    private Context mContext;
    private AccountManager mAccountManager;
    private String mAccountName;
    private String mAccountType;
    private String mAccountAuthType;
    private String mAccountToken;
    private String mAccountEmail;
    private String mAccountPhone;
    private String mAccountFirstName;
    private String mAccountLastName;
    private String mAccountPassword;


    //Singleton definition
    private static AccountGeneral Instance = new AccountGeneral();

    private AccountGeneral() {}
    public static AccountGeneral getInstance() {
        return Instance;
    }
    public Intent init(Context context, @Nullable Intent intent) {
        String prefValue;

        mContext = context;
        mAccountManager = AccountManager.get(context);
        //Set default values first
        this.mAccountName = null;
        this.mAccountType = ACCOUNT_TYPE;
        this.mAccountAuthType = AUTHTOKEN_TYPE_FULL_ACCESS;
        //Load from preferences if values are not null
        prefValue = QueryPreferences.getPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_NAME);
        if (prefValue != null) this.mAccountName = prefValue;
        prefValue = QueryPreferences.getPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_TYPE);
        if (prefValue != null) this.mAccountType = prefValue;
        prefValue = QueryPreferences.getPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_AUTH_TYPE);
        if (prefValue != null) this.mAccountAuthType = prefValue;

        //If there is an intent we get the data from the intent
        if (intent!=null) {
            //Reformat arguments for intent to start LogInFragment
            if (intent.hasExtra(ARG_ACCOUNT_NAME))
                this.mAccountName = intent.getStringExtra(ARG_ACCOUNT_NAME);
            else //In the case that the incomming intent was not having the extra we set it from preferences
                intent.putExtra(ARG_ACCOUNT_NAME,this.mAccountName);

            if (intent.hasExtra(ARG_ACCOUNT_TYPE))
                this.mAccountType = intent.getStringExtra(ARG_ACCOUNT_TYPE);
            else
                intent.putExtra(ARG_ACCOUNT_TYPE,this.mAccountType);

            if (intent.hasExtra(ARG_ACCOUNT_AUTH_TYPE))
                this.mAccountAuthType = intent.getStringExtra(ARG_ACCOUNT_AUTH_TYPE);
            else
                intent.putExtra(ARG_ACCOUNT_AUTH_TYPE,this.mAccountAuthType);
        }

        // TODO for the moment when we create an account for settings we are creating by default full_access
        if (mAccountAuthType == null) mAccountAuthType = AUTHTOKEN_TYPE_FULL_ACCESS;

        Logs.i("ACCOUNT_NAME: " + mAccountName, this.getClass());
        Logs.i("ACCOUNT_TYPE: " + mAccountType, this.getClass());
        Logs.i("ACCOUNT_AUTH_TYPE: " + mAccountAuthType, this.getClass());
        //We make sure that we return an intent with the good values as Extras
        if (intent == null) {
            Intent intentTmp = new Intent();
            intentTmp.putExtra(ARG_ACCOUNT_NAME, mAccountName);
            intentTmp.putExtra(ARG_ACCOUNT_TYPE, mAccountType);
            intentTmp.putExtra(ARG_ACCOUNT_AUTH_TYPE, mAccountAuthType);
            return intentTmp;

        }
        //We return the intent with the updated extras if there were
        return intent;
    }

    // Returns all accounts of our app type
    public Account[] getAllAccounts() {
        Logs.i("Found " + mAccountManager.getAccountsByType(this.getAccountType()).length + " accounts !", AccountGeneral.class);
        //Find if there is an account with the correct accountName and get its token
        return mAccountManager.getAccountsByType(this.getAccountType());
    }

    //Gets an existing account that matches the device
    public Account getAccount() {
        Logs.i("Getting account for accountName :" + this.getAccountName(), AccountGeneral.class);
        Account myAccount = null;
        //Find if there is an account with the correct accountName and get its token
        for (Account account : mAccountManager.getAccountsByType(this.getAccountType())) {
            Logs.i(" ----> Account found: " + account.name, AccountGeneral.class);
            if (mAccountManager.getUserData(account, PARAM_USER_ACCOUNT)!= null) {
                if (mAccountManager.getUserData(account, PARAM_USER_ACCOUNT).equals(mAccountName)) {
                    myAccount = account;
                    Logs.i("Found one account matching :" + mAccountName, AccountGeneral.class);
                    break;
                }
            }
        }
        return myAccount;
    }
    //Gets an existing account that matches the device
    public String getAccountAuthToken(Account account) {
        return mAccountManager.peekAuthToken(account, mAccountAuthType);
    }

    public String getAccountAuthToken(Account account, String authType) {
        return mAccountManager.peekAuthToken(account, authType);
    }

    // Creates an account on the Device
    public Boolean createAccount() {
        Logs.i("Creating account with following features:");
        Logs.i("Account ID:"                        + this.getAccountName());
        Logs.i("Account PARAM_USER_FIRST_NAME: "    + this.getAccountFirstName());
        Logs.i("Account PARAM_USER_LAST_NAME: "     + this.getAccountLastName());
        Logs.i("Account EMAIL: "                    + this.getAccountEmail());
        Logs.i("Account PARAM_USER_PHONE: "         + this.getAccountPhone());
        Logs.i("Account ACCOUNT_TYPE: "             + this.getAccountType());
        Logs.i("Account PARAM_USER_PASS: "          + this.getAccountPassword());
        Logs.i("Account PARAM_USER_TOKEN_TYPE: "    + this.getAccountAuthType());
        Logs.i("Account KEY_TOKEN: "                + this.getAccountToken());

        final Account account = new Account(this.getAccountName(), this.getAccountType());
        mAccountManager.addAccountExplicitly(account, this.getAccountPassword(), null);
        mAccountManager.setAuthToken(account, this.getAccountAuthType(), this.getAccountToken());
        mAccountManager.setUserData(account, PARAM_USER_TOKEN_TYPE, this.getAccountAuthType());
        mAccountManager.setUserData(account, PARAM_USER_ACCOUNT, this.getAccountName());
        mAccountManager.setUserData(account, PARAM_USER_EMAIL, this.getAccountEmail());
        mAccountManager.setUserData(account, PARAM_USER_PHONE, this.getAccountPhone());
        mAccountManager.setUserData(account, PARAM_USER_FIRST_NAME, this.getAccountFirstName());
        mAccountManager.setUserData(account, PARAM_USER_LAST_NAME, this.getAccountLastName());

        Account myAccount = null;
        //Verifiy if account was correctly created
        myAccount = getAccount();
        if (myAccount == null)
            return false;
        else
            return true;
    }

    public Boolean setDataFromAccount(Account account) {
        if (account != null) {
            this.setAccountName(mAccountManager.getUserData(account, PARAM_USER_ACCOUNT));
            this.setAccountEmail(mAccountManager.getUserData(account, PARAM_USER_EMAIL));
            this.setAccountPhone(mAccountManager.getUserData(account, PARAM_USER_PHONE));
            this.setAccountFirstName(mAccountManager.getUserData(account, PARAM_USER_FIRST_NAME));
            this.setAccountLastName(mAccountManager.getUserData(account, PARAM_USER_LAST_NAME));
            return true;
        } else
            return false;
    }





    //Remove account
    public Boolean removeAccount(Account account) {
        Boolean isDone = false;

        if (Build.VERSION.SDK_INT<22) {
            //@SuppressWarnings("deprecation")
            final AccountManagerFuture<Boolean> booleanAccountManagerFuture = mAccountManager.removeAccount(account, null, null);
            try {
                isDone = booleanAccountManagerFuture.getResult(1, TimeUnit.SECONDS);
            } catch (OperationCanceledException e) {
                Logs.i("Caught exception : " + e);
            } catch (IOException e) {
                Logs.i("Caught exception : " + e);
            } catch (AuthenticatorException e) {
                Logs.i("Caught exception : " + e);
            }
            if (isDone) Logs.i("Successfully removed account ! ", AccountGeneral.class);

        } else {
            isDone = mAccountManager.removeAccountExplicitly(account);
            if (isDone) Logs.i("Successfully removed account ! ", AccountGeneral.class);
        }
        return isDone;
    }

    public void saveToPreferences() {
        // We only save non critical data
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_NAME, this.getAccountName());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_EMAIL, this.getAccountEmail());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_PHONE, this.getAccountPhone());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_FIRST_NAME, this.getAccountFirstName());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_LAST_NAME, this.getAccountLastName());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_TYPE, this.getAccountType());
        QueryPreferences.setPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_AUTH_TYPE, this.getAccountAuthType());
        //We don't save password neither token !!!
    }

    public void getFromPreferences() {
        //  User.uEmail = QueryPreferences.getPreference(mContext,QueryPreferences.PREFERENCE_USER_ACCOUNT_NAME);
    }


    //Encrypts the password
    public String sha1Password(String password) {
        // Needs to be done !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // String myHash = Hashing.sha1.hashString(password, Charsets.UTF_8).toString();
        return password;
    }

    //Check that password meets the required format
    //  8 chars min
    //  2 numbers min
    //  2 lowercase chars min
    //  2 upercase chars min
    public boolean checkPasswordInput(String password) {
        //Check the length
        if (password.length() < 8) {
            return false;
        }

        //Check that contains at least 2 numbers
        Pattern r;
        Matcher m;
        r = Pattern.compile("[0-9]");
        m = r.matcher(password);
        int count = 0;
        while (m.find()) count++;
        if (count < 2) return false;

        //Check that at least 2 lowercase characters
        r = Pattern.compile("[a-z]");
        m = r.matcher(password);
        count = 0;
        while (m.find()) count++;
        if (count < 2) return false;

        //Check that at least 2 uppercase characters
        r = Pattern.compile("[A-Z]");
        m = r.matcher(password);
        count = 0;
        while (m.find()) count++;
        if (count < 2) return false;
        return true;
    }

    //Check that email meets the required format
    //  @ must exist
    //  . must exist
    //  8 chars min
    public boolean checkEmailInput(String email) {
        //Check the length
        if (email.length() < 8) {
            return false;
        }

        //Check that contains at least 2 numbers
        Pattern r;
        Matcher m;
        r = Pattern.compile("@");
        m = r.matcher(email);
        int count = 0;
        while (m.find()) count++;
        if (count != 1) return false;

        //Check that at least 2 lowercase characters
        r = Pattern.compile("\\.[a-z]+$");
        m = r.matcher(email);
        if (!m.find()) {
            Logs.i("Could not found .com !", AccountGeneral.class);
            return false;
        }
        return true;
    }

    //Check that phone meets the required format
    //  8 numbers min
    //  only numbers
    public boolean checkPhoneInput(String number) {
        //Check the length
        if (number.length() < 8) {
            return false;
        }

        //Check that contains at least 2 numbers
        Pattern r;
        Matcher m;
        r = Pattern.compile("[0-9]");
        m = r.matcher(number);
        int count = 0;
        while (m.find()) count++;
        if (count != number.length()) return false;

        return true;
    }






    /////////////////////////////// Getter / Setter ////////////////////////////////////////////////

    public String getAccountName() { return mAccountName; }
    public void setAccountName(String id) {
        mAccountName =  id;
    }
    public String getAccountType() { return mAccountType; }
    public String getAccountAuthType() { return mAccountAuthType; }

    public void setAccountEmail(String email) {mAccountEmail = email;}
    public String getAccountEmail() { return mAccountEmail;}

    public void setAccountPhone(String phone) {mAccountPhone = phone;}
    public String getAccountPhone() { return mAccountPhone;}

    public void setAccountFirstName(String firstName) {mAccountFirstName = firstName;}
    public String getAccountFirstName() { return mAccountFirstName;}

    public void setAccountLastName(String lastName) {mAccountLastName = lastName;}
    public String getAccountLastName() { return mAccountLastName;}

    public void setAccountPassword(String password) {mAccountPassword = password;}
    public String getAccountPassword() {
        // TODO sha1 password
        //Here we are missing the sha1 password
        //String finalPassword = mAccountPassword + "test_encoding";
        // The same algorithm needs to be run in server and android !
        return mAccountPassword;
    }

    public void setAccountToken(String token) {mAccountToken = token;}
    public String getAccountToken() { return mAccountToken;}

    //Sets token in the account
    public void setAuthToken(Account account) {
        mAccountManager.setAuthToken(account,this.getAccountAuthType(), this.getAccountToken());
    }
    //Sets password in the account
    public void setAuthPassword(Account account) {
        mAccountManager.setPassword(account, this.getAccountPassword());
    }
}
