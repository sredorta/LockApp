package com.locker.lockapp.authentication;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.locker.lockapp.toolbox.Logs;
import com.locker.lockapp.toolbox.Toolbox;

import static com.locker.lockapp.authentication.AccountGeneral.*;
/**
 * Created by sredorta on 1/25/2017.
 */

//We get here by two means:
//    When using settings then we get here thanks to the service
//    When using the app we will send an intent to check if valid auth
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    private Intent myIntent;
    private AccountGeneral myAccountSettings;
    private final int REQ_SIGNIN = 1;
    private final int REQ_SIGNUP = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Dump the input intent contents
        Logs.i("onCreate: Intent details for input of AuthenticatorActivity:", this.getClass());
        Toolbox.dumpIntent(getIntent());

        //Init the Singleton with account name, account type and account auth type from preference or intent
        myAccountSettings = AccountGeneral.getInstance();
        myIntent = myAccountSettings.init(getApplicationContext(),getIntent());

        //We only allow one account per device... so we check how many accounts exists



        //If the incomming intent was specifying that we want to add an account then start the SignUp
        if (myIntent.getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT,false)) {
                startSignInOrSignUp(REQ_SIGNUP);
        } else if (myAccountSettings.getAllAccounts().length == 0) {
                //If there is no account on the device then start SignUp
                startSignInOrSignUp(REQ_SIGNUP);
        } else  {
                //We have accounts in the device so we start SignIn
                //Here we check if we have an accountName we try to see if we have a valid token and if not then we
                if (myAccountSettings.getAccountName() != null) {
                    checkFastLogin();
                //If we are here is that we could not get a valid token... so we need to star the Sign In
                } else {
                    startSignInOrSignUp(REQ_SIGNIN);
                }
        }
    }


    private void checkFastLogin() {
        Logs.i("AccountName was found, so checking if there is a valid account for: " + myAccountSettings.getAccountName(), this.getClass());
        final Account myAccount = myAccountSettings.getAccount();
        if (myAccount != null) {
            //Get the account token in the device
            final String myToken = myAccountSettings.getAccountAuthToken(myAccount);
            new AsyncTask<String, Void, Intent>() {
                @Override
                protected Intent doInBackground(String... params) {
                    Bundle data = new Bundle();
                    Logs.i("Account Name: " + myAccountSettings.getAccountName());
                    Logs.i("Token : " + myToken);
                    Logs.i("Token type: " + myAccountSettings.getAccountAuthType());
                    Boolean isValidToken =false;
                    if (myToken !=null)
                         isValidToken = sServerAuthenticate.userIsTokenValid(myAccountSettings.getAccountName() ,myToken, myAccountSettings.getAccountAuthType());

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, myAccountSettings.getAccountName());
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, myAccountSettings.getAccountType());
                    data.putString(AccountManager.KEY_AUTHTOKEN, myToken);
                    data.putBoolean("isValidToken", isValidToken);
                    Logs.i("We are checking if Token is valid !", AuthenticatorActivity.class);
                    final Intent res = new Intent();
                    res.putExtras(data);

                    return res;
                }

                @Override
                protected void onPostExecute(Intent intent) {
                    Logs.i("We are checking if Token is valid ! onPostExecute", AuthenticatorActivity.class);
                    if (intent.getBooleanExtra("isValidToken",false)) {
                        setResult(RESULT_OK);
                        Logs.i("Exit the activity !", this.getClass());
                        finishLogin(intent);
                    } else {
                        startSignInOrSignUp(REQ_SIGNIN);
                    }
                }
            }.execute();
        }

    }
    //Start signIn or SignUP acticty for result
    private void startSignInOrSignUp(int inOrUp) {
        Class myClass = SignUpActivity.class;
        if (inOrUp == REQ_SIGNIN) {
            Logs.i("SignIn starting...", this.getClass());
            myClass = SignInActivity.class;
        } else
            Logs.i("SignUp starting...", this.getClass());

        // start the SignIn/Up activity
        Intent signInUp = new Intent(getBaseContext(), myClass);
        //Give the kind of account we want to create
        signInUp.putExtras(myIntent.getExtras());
        Logs.i("Intent details for intent to start SignUpActivity:", this.getClass());
        Toolbox.dumpIntent(signInUp);
        startActivityForResult(signInUp, inOrUp);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else if (requestCode == REQ_SIGNIN && resultCode == RESULT_OK) {
            finishLogin(data);
        } else if (resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCELED);
            finish();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void finishLogin(Intent intent){
        //Check if somehting needs to be done
/*        if (intent.hasExtra(ARG_FINAL_ACTION)) {
            if (intent.getStringExtra(ARG_FINAL_ACTION).equals("add")) {
                myAccountSettings.createAccount();
            }

        }
*/
         /*                   Logs.i("We are here !");
                    if (!myAccountSettings.createAccount()) {
                        //We could not create the device account so removing the server account
                        Boolean test = sServerAuthenticate.userRemove(myAccountSettings.getAccountName(),
                                myAccountSettings.getAccountAuthType());
                        Logs.i("Removing server account as we could not create device account !");
                        data.putString(KEY_ERROR_MESSAGE, "Could not create device account !");
                    }
                    */


        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }


}
