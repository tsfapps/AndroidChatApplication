package com.videocall.tsfchat.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.qiscus.sdk.service.QiscusFirebaseIdService;


public class FirebaseIdService extends QiscusFirebaseIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d("FirebaseIdService", "Fcm token: " + FirebaseInstanceId.getInstance().getToken());

        //Code for specific qisme apps here
    }
}