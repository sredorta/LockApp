package com.locker.lockapp.authentication;
import com.locker.lockapp.dao.CloudFetchr;

/**
 * Defines the commands required to request server for authentication
 */
public class LockerServerAuthenticate implements ServerAuthenticate {

    //Send to the server all fields and create a new user and get the token
    @Override
    public String userSignUp(String phone, String email, String firstName, String lastName, String authType) {
        String accountID = new CloudFetchr().userSignUp(phone,email,firstName, lastName, "users");
        return accountID;
    }

    //Send to the server all fields and create a new user and get the token
    @Override
    public Boolean userSetPassword(String account, String password, String authType) {
        Boolean result = new CloudFetchr().userSetPassword(account,password,"users");
        return result;
    }


    //Send to the Server username and password and get corresponding token
    @Override
    public String userSignIn(String user, String password, String authType) {
        String authtoken = new CloudFetchr().userSignIn(user,password, "users");
        return authtoken;
    }

    //Send to the Server the user name and the token we have stored in our device and check if the token is valid
    @Override
    public Boolean userIsTokenValid(String user, String accountToken, String authType) {
        Boolean isTokenValid = new CloudFetchr().userIsTokenValid(user, accountToken, "users");
        return isTokenValid;
    }

    @Override
    public Boolean userRemove(String account, String authType) {
        Boolean isUserRemoved = new CloudFetchr().userRemove(account,"users");
        return isUserRemoved;
    }

}
