package com.locker.lockapp.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by sredorta on 1/12/2017.
 */
public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        LockerAuthenticator authenticator = new LockerAuthenticator(this);
        return authenticator.getIBinder();
    }
}
