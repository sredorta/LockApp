package com.locker.lockapp.authentication;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.locker.lockapp.activity.SingleFragmentActivity;


//Create new user account activity
public class SignUpActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return SignUpFragment.newInstance();
    }


    @Override
    public void onBackPressed() {
        Log.i("SERGI", "OnBackPressed was done, and we are now sending RESULT_CANCELED");
        setResult(RESULT_CANCELED);
        finish();
    }
}
