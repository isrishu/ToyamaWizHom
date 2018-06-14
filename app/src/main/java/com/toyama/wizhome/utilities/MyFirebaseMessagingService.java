package com.toyama.wizhome.utilities;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Srishu Indrakanti on 18-08-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "WizHom_FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
//                JSONObject json = new JSONObject(remoteMessage.getData().toString());
//                JSONObject data = json.getJSONObject("data");
//                String command = data.getString("command");
//                Log.d(TAG, "Notification Command: " + command);
                //sendMessageToMainActivity(command);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    // Send an Intent with an action named "ApplyScene".
    private void sendMessageToMainActivity(String command) {
        Intent intent = new Intent("RemoteCommand");
        intent.putExtra("command",command);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
