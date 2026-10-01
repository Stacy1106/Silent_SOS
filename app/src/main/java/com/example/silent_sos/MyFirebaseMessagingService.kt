package com.example.silent_sos

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(
            "FCM",
            "Message received: ${remoteMessage.notification?.body}"
        )
    }

    override fun onNewToken(token: String) {

        Log.d(
            "FCM",
            "New FCM Token: $token"
        )
    }
}