package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
    final static String MESSAGE = "MESSAGE";
    final static String DATA = "DATA";
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("message");
        //Log.d("gcm", "From: " + from);
        //Log.d("gcm", "Message: " + message);

        Intent intent = new Intent();
        intent.setAction(MESSAGE);
        intent.putExtra(DATA, message);
        sendBroadcast(intent);
    }
}
