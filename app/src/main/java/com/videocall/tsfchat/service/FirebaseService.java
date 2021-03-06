package com.videocall.tsfchat.service;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.service.QiscusFirebaseService;

import java.util.Map;


public class FirebaseService extends QiscusFirebaseService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        Log.d("onMessageReceived", "onMessageReceived: " + data.toString());

        if (QiscusFirebaseService.handleMessageReceived(remoteMessage)) { // SDK PN, contains key qiscus_sdk
            return;
        }

        //Below is specific qisme apps PN
    }
}
