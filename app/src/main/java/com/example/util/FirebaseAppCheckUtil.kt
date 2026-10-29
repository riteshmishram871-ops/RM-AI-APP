package com.example.util

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

object FirebaseAppCheckUtil {
    private const val TAG = "FirebaseAppCheck"

    fun configureAppCheck(activity: Activity, intent: Intent?) {
        try {
            FirebaseApp.initializeApp(activity)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "Firebase App Check configured successfully")
        } catch (e: Exception) {
            Log.w(TAG, "App Check configuration failed or skipped", e)
        }
    }
}
