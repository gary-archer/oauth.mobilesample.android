package com.authguidance.basicmobileapp.views.utilities

import android.app.KeyguardManager
import android.content.Context
import android.os.Build

/*
 * An object to ensure that the device is secured before running the app
 */
object DeviceSecurity {

    /*
     * See if the device has a PIN, Pattern or Fingerprint enabled
     */
    fun isDeviceSecured(context: Context): Boolean {

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return keyguardManager.isDeviceSecure
        } else {
            return keyguardManager.isKeyguardSecure
        }
    }
}
