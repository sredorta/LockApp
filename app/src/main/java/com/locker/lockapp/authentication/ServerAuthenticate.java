package com.locker.lockapp.authentication;

/**
 * Defines the interface with the server for authentication
 */
public interface ServerAuthenticate {
    public Boolean userSetPassword(final String user, final String password, String authType);
    public String userSignUp(final String phone, final String email, final String firstName, final String lastName, String authType);
    public String userSignIn(final String user, final String pass, String authType);
    public Boolean userIsTokenValid(final String user, final String accountToken, String authType);
    public Boolean userRemove(final String user, String authType);
}