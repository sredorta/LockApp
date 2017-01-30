package com.locker.lockapp.authentication;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.locker.lockapp.R;
import com.locker.lockapp.toolbox.Logs;

import static com.locker.lockapp.authentication.AccountGeneral.*;

//Create new user account activity
public class SignUpFragment extends Fragment {
    private Intent myIntent;
    private AccountGeneral myAccountSettings;

    TextView firstNameTextView;
    TextView lastNameTextView;
    TextView phoneTextView;
    TextView accountEmailTextView;
    TextView accountPasswordTextView;
    private String authtoken = null;

    // Constructor
    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get account details from Singleton
        myAccountSettings = AccountGeneral.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);
        final Button submitButton = (Button) v.findViewById(R.id.fragment_signup_Button_submit);
        firstNameTextView = (TextView) v.findViewById(R.id.fragment_signup_EditText_FirstName);
        lastNameTextView = (TextView) v.findViewById(R.id.fragment_signup_EditText_LastName);
        phoneTextView =             (TextView) v.findViewById(R.id.fragment_signup_EditText_phone);
        accountEmailTextView =      (TextView) v.findViewById(R.id.fragment_signup_EditText_email);
        accountPasswordTextView  = (TextView) v.findViewById(R.id.fragment_signup_EditText_password);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logs.i("Submit clicked !", this.getClass());
                boolean fieldsOk = true;
                //Here we need to check the parameters and we should most probably change the color and display a snackbar
                // TODO Snackbar with errors and highlight errors
                // To be done later !!!!!!!!!!!!!
                if (firstNameTextView.getText().toString() == null)
                    fieldsOk = false;
                if (lastNameTextView.getText().toString() == null)
                    fieldsOk = false;
                if (!myAccountSettings.checkPasswordInput(accountPasswordTextView.getText().toString())) {
                    fieldsOk = false;
                    accountPasswordTextView.setText("");
                }

                if (!myAccountSettings.checkEmailInput(accountEmailTextView.getText().toString())) {
                    fieldsOk = false;
                    accountEmailTextView.setText("");
                }
                if (!myAccountSettings.checkPhoneInput(phoneTextView.getText().toString())) {
                    fieldsOk = false;
                    phoneTextView.setText("");
                }
                //Create the account only if Fields meet criteria
                if (fieldsOk) createAccount();
            }
        });

        return v;
    }

    private void createAccount() {
        Logs.i("Creating account in the server without password...");
        //We first try to create the account in the server without getting a token and get the ID of the account
        //Then we set the password if we could create the server account
        //Finally we create the Device account if the server account was created
        // If the server account was not created then we don't create the device account
        // If there is an error on the Device Account then we delete the server account
        new AsyncTask<Void, Void, Intent>() {
            String accountFirstName = firstNameTextView.getText().toString().trim();
            String accountLastName = lastNameTextView.getText().toString().trim();
            String accountPhone = phoneTextView.getText().toString().trim();
            String accountEmail = accountEmailTextView.getText().toString().trim();
            String accountPassword = accountPasswordTextView.getText().toString().trim();
            String accountID;
            Boolean resultSetPassword = null;
            @Override
            protected Intent doInBackground(Void... params) {
                myAccountSettings.setAccountFirstName(accountFirstName);
                myAccountSettings.setAccountLastName(accountLastName);
                myAccountSettings.setAccountPhone(accountPhone);
                myAccountSettings.setAccountEmail(accountEmail);
                myAccountSettings.setAccountPassword(accountPassword);

                Bundle data = new Bundle();
                accountID = sServerAuthenticate.userSignUp(myAccountSettings.getAccountPhone(),
                                                        myAccountSettings.getAccountEmail(),
                                                        myAccountSettings.getAccountFirstName(),
                                                        myAccountSettings.getAccountLastName(),
                                                        myAccountSettings.getAccountAuthType());
                if (accountID !=null) {
                    //Set the accountID that we got in the Singleton
                    myAccountSettings.setAccountName(accountID);
                    resultSetPassword = sServerAuthenticate.userSetPassword(myAccountSettings.getAccountName(),
                                                                        myAccountSettings.getAccountPassword(),
                                                                        myAccountSettings.getAccountAuthType());
                    if (resultSetPassword) {
                        //Now that everything is setup get the token finally
                        authtoken = sServerAuthenticate.userSignIn(myAccountSettings.getAccountName(),
                                                                    myAccountSettings.getAccountPassword(),
                                                                    myAccountSettings.getAccountAuthType());
                    } else {
                        data.putString(KEY_ERROR_MESSAGE, "Could not get token !");
                    }
                } else {
                    data.putString(KEY_ERROR_MESSAGE, "Could not create server account !");
                }
                //If everything was ok we create the account in the device
                if ((accountID!=null) && resultSetPassword && (authtoken!=null)) {
                    //Update the Singleton
                    myAccountSettings.setAccountName(accountID);
                    myAccountSettings.setAccountFirstName(accountFirstName);
                    myAccountSettings.setAccountLastName(accountLastName);
                    myAccountSettings.setAccountPhone(accountPhone);
                    myAccountSettings.setAccountEmail(accountEmail);
                    myAccountSettings.setAccountToken(authtoken);
                    //We need to tell the authenticator to create the new account
//                    data.putString(ARG_FINAL_ACTION, "add");
                    Logs.i("We are here !");
                    if (!myAccountSettings.createAccount()) {
                        //We could not create the device account so removing the server account
                        Boolean test = sServerAuthenticate.userRemove(myAccountSettings.getAccountName(),
                                myAccountSettings.getAccountAuthType());
                        Logs.i("Removing server account as we could not create device account !");
                        data.putString(KEY_ERROR_MESSAGE, "Could not create device account !");
                    } else {
                        //Save now password and token
                        Logs.i("Storing token and password in the account !");
                        myAccountSettings.setAuthPassword(myAccountSettings.getAccount());
                        myAccountSettings.setAuthToken(myAccountSettings.getAccount());
                    }

                }

                //Settings for the Account AuthenticatorActivity
                data.putString(AccountManager.KEY_ACCOUNT_NAME, myAccountSettings.getAccountName());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, myAccountSettings.getAccountType());
                data.putString(AccountManager.KEY_AUTHTOKEN, myAccountSettings.getAccountToken());

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                Logs.i("ACCOUNT_ID got: " + accountID);
                Logs.i("ACCOUNT_TOKEN got: " + authtoken);

                if (intent.hasExtra(KEY_ERROR_MESSAGE))
                    Toast.makeText(getActivity().getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                else {
                        //Save the account created in the preferences (all except critical things)
                        myAccountSettings.saveToPreferences();
                        //Finish and send to AuthenticatorActivity that we where successfull
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        getActivity().finish();
                }
            }
        }.execute();

    }

}
