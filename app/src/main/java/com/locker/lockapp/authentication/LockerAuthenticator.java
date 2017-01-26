package com.locker.lockapp.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.locker.lockapp.toolbox.Logs;
import com.locker.lockapp.toolbox.Toolbox;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static com.locker.lockapp.authentication.AccountGeneral.*;

/* Authenticator creator
 *    This class creates the Locker Authenticator and sents intent to AuthenticatorActivity
 *          addAccount method   : Allows to create a new account by starting the Activity AuthenticatorActivity
 *          getAuthToken method : Gets last auth-token stored in the device from last successful login
 *                                When token could not be found then start activity of login again AuthenticatorActivity
 *
 *  This Class is the only one that allows us to access the account from Settings
 */

public class LockerAuthenticator extends AbstractAccountAuthenticator {
    private final Context mContext;


    public LockerAuthenticator(Context context) {
        super(context);
        // I hate you! Google - set mContext as protected!
        this.mContext = context;
    }

    //Create a new Account request... mainly we say that when we want to create an account to the service we go to addAccount
    //   then we set all the parameters in the bundle and start AuthenticatorActivity.class
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Logs.i("addAccount", this.getClass());
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(ARG_ACCOUNT_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        Logs.i("Details of the intent sent to AuthenticatorActivity : ", this.getClass());
        Toolbox.dumpIntent(intent);
        return bundle;
    }

    //Get last token
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
         Logs.i("getAuthToken", this.getClass());
        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        Logs.i("peekAuthToken returned :: " + authToken, this.getClass());

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    Logs.i("Re-authenticating with the existing password", this.getClass());
                    authToken = sServerAuthenticate.userSignIn(account.name, password, authTokenType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // We check if the token from the account is valid (i.e. a valid session exists in the server)
        if (!TextUtils.isEmpty(authToken)) {
            Boolean isValidToken = false;
            try {
                Logs.i("Checking if the Token of the account is valid on the server side !", this.getClass());
                isValidToken = sServerAuthenticate.userIsTokenValid(account.name, authToken, authTokenType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If the token from the account is not valid on the server side then we invalidate the token of the account
            if (!isValidToken) authToken = null;
        }
        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_ACCOUNT_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) {
        Logs.i("getAccountRemovalAllowed", this.getClass());
        Bundle result = new Bundle();
        boolean allowed = true; // or whatever logic you want here
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, allowed);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Logs.i("getAuthTokenLabel", this.getClass());
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Logs.i("hasFeatures", this.getClass());
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Logs.i("editProperties", this.getClass());
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Logs.i("confirmCredentials", this.getClass());
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Logs.i("updateCredentials", this.getClass());
        return null;
    }

}
