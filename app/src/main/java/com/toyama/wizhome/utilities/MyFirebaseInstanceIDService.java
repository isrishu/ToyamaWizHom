package com.toyama.wizhome.utilities;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.toyama.includes.utilities.Globals;

/**
 * Created by Srishu Indrakanti on 18-08-2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "WizHom_FCMIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        //Globals.fcmTokenString=refreshedToken;
        Globals.isFCMTokenRefreshed=true;
    }
}
