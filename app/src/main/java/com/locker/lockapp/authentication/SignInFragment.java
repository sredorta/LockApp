
package com.locker.lockapp.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.locker.lockapp.R;
import com.locker.lockapp.dao.CloudFetchr;
import com.locker.lockapp.dao.JsonItem;
import com.locker.lockapp.toolbox.Logs;

import static com.locker.lockapp.authentication.AccountGeneral.*;

/**
 * Created by sredorta on 1/13/2017.
 */
public class SignInFragment extends Fragment {
    private AccountGeneral myAccountSettings;
    private final int REQ_SIGNUP = 1;
    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private String mAccountType;
    String accountName;
    Account myAccount;

    // Constructor
    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get account details from Singleton either from intent of from preferences comming from AuthenticatorActivity
        myAccountSettings = AccountGeneral.getInstance();
        myAccount = myAccountSettings.getAccount();
    }





    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        final EditText passTextView = (EditText) v.findViewById(R.id.fragment_login_EditText_password);

        //Re-enter credentials
        v.findViewById(R.id.fragment_login_Button_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i("Submitting credentials to account manager !", this.getClass());
                if (myAccountSettings.checkPasswordInput(passTextView.getText().toString())) {
                    submit(myAccountSettings.getAccountName(), passTextView.getText().toString());
                    passTextView.setText("");
                } else {
                    passTextView.setText("");
                    Toast.makeText(getActivity(),"Password must be at least 8 chars !", Toast.LENGTH_LONG).show();
                }
            }
        });
        //Create new user account
        v.findViewById(R.id.fragment_login_Button_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i("Starting new activity to create account !", this.getClass());
                // Since there can only be one AuthenticatorActivity, we call the sign up activity, get his results,
                // and return them in setAccountAuthenticatorResult(). See finishLogin().
                Intent signup = new Intent(getActivity().getBaseContext(), SignUpActivity.class);
                //Give the kind of account we want to create
                if (getActivity().getIntent().getExtras() != null) {
                    signup.putExtras(getActivity().getIntent().getExtras());
                    Logs.i("When starting signup extras where found !", this.getClass());
                } else {
                    Logs.i("When starting signup no extras found !", this.getClass());
                }
                startActivityForResult(signup, REQ_SIGNUP);
            }
        });

        return v;
    }
    //This is to inflate the Child fragment that contains the account details
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Fragment childFragment = new AccountListFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_recycleview_accounts, childFragment).commit();
    }





    //Submits the credentials we have introduced
    public void submit(final String userName, final String userPass) {
        myAccountSettings.setAccountPassword(userPass);
        myAccountSettings.setAccountName(userName);

        //accountType should not be null here
        Logs.i("accountType = " + mAccountType, this.getClass());

        new AsyncTask<Void, Void, Void>() {
            String authtoken= null;
            String message = null;
            @Override
            protected Void doInBackground(Void... params) {
                Logs.i("Started authenticating", this.getClass());

                try {
                    authtoken = sServerAuthenticate.userSignIn(userName, userPass, myAccountSettings.getAccountAuthType());

                } catch (Exception e) {
                    message = e.getMessage();
                }
                //Check if we got a token... if it's null it means that we could not LogIn
                if (authtoken == null) {
                    //Redo the query to get server answer with full details
                    JsonItem item = new CloudFetchr().userSignInDetails(userName, userPass,"users");
                    message = item.getMessage();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                myAccountSettings.setAccountToken(authtoken);
                myAccountSettings.setAccountPassword(userPass);
                 if (message != null) {
                    Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                     //Update the Account password/Token
                     Logs.i("Setting password : " + userPass + " and token " + authtoken);
                     myAccountSettings.setAuthToken(myAccount);
                     myAccountSettings.setAuthPassword(myAccount);
                     // Return that we did a good job
                     Bundle data = new Bundle();
                     //Settings for the Account AuthenticatorActivity
                     data.putString(AccountManager.KEY_ACCOUNT_NAME, myAccountSettings.getAccountName());
                     data.putString(AccountManager.KEY_ACCOUNT_TYPE, myAccountSettings.getAccountType());
                     data.putString(AccountManager.KEY_AUTHTOKEN, myAccountSettings.getAccountToken());
                     final Intent res = new Intent();
                     res.putExtras(data);
                     finishLogin(res);
                }
            }
        }.execute();

    }

    //When we come back from new account creation we fall here
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logs.i("onActivityResult", this.getClass());
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == Activity.RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void finishLogin(Intent intent) {
        Logs.i("finishLogin", this.getClass());
        myAccountSettings.saveToPreferences();
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

}